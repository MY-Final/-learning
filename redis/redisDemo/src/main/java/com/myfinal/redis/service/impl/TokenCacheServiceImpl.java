package com.myfinal.redis.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.myfinal.redis.dto.PingcodeTokenDTO;
import com.myfinal.redis.exception.BusinessException;
import com.myfinal.redis.service.PingcodeAuthService;
import com.myfinal.redis.service.TokenCacheService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Token 缓存服务实现
 *
 * 双层缓存架构：
 * 1. L1: Caffeine 本地缓存（快速访问，减少 Redis 网络开销）
 * 2. L2: Redis 分布式缓存（多实例共享）
 *
 * 特性：
 * - 缓存预热：应用启动时预加载
 * - 自动刷新：令牌过期前主动刷新
 * - 监控指标：缓存命中率、请求延迟
 */
@Service
public class TokenCacheServiceImpl implements TokenCacheService {

    private static final Logger log = LoggerFactory.getLogger(TokenCacheServiceImpl.class);

    private static final String ENTERPRISE_TOKEN_CACHE_KEY = "pingcode:token:enterprise";
    private static final String TOKEN_LOCK_KEY = "pingcode:token:lock";

    /** 本地缓存过期时间（秒），比 Redis 短以确保一致性 */
    private static final int LOCAL_CACHE_TTL_SECONDS = 1800;

    /** 令牌刷新阈值（秒），在过期前 300 秒刷新 */
    private static final int REFRESH_THRESHOLD_SECONDS = 300;

    /** 分布式锁超时时间（秒） */
    private static final int LOCK_TIMEOUT_SECONDS = 10;

    private final PingcodeAuthService pingcodeAuthService;
    private final StringRedisTemplate redisTemplate;
    private final MeterRegistry meterRegistry;

    /** L1 本地缓存 */
    private final Cache<String, PingcodeTokenDTO> localCache;

    /** 监控指标 */
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter refreshTokenCounter;
    private final Timer refreshTokenTimer;
    private final AtomicLong lastRefreshTime;

    public TokenCacheServiceImpl(PingcodeAuthService pingcodeAuthService,
                                  StringRedisTemplate redisTemplate,
                                  MeterRegistry meterRegistry) {
        this.pingcodeAuthService = pingcodeAuthService;
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;

        // 初始化 Caffeine 本地缓存
        this.localCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(LOCAL_CACHE_TTL_SECONDS, TimeUnit.SECONDS)
                .recordStats()
                .build();

        // 初始化监控指标
        this.cacheHitCounter = Counter.builder("token_cache_hits_total")
                .description("Token 缓存命中次数")
                .register(meterRegistry);

        this.cacheMissCounter = Counter.builder("token_cache_misses_total")
                .description("Token 缓存未命中次数")
                .register(meterRegistry);

        this.refreshTokenCounter = Counter.builder("token_refreshes_total")
                .description("Token 刷新次数")
                .register(meterRegistry);

        this.lastRefreshTime = new AtomicLong(0);
        Gauge.builder("token_cache_last_refresh_timestamp", lastRefreshTime, AtomicLong::get)
                .description("上次 Token 刷新时间戳")
                .register(meterRegistry);

        this.refreshTokenTimer = Timer.builder("token_refresh_duration")
                .description("Token 刷新耗时")
                .register(meterRegistry);

        log.info("TokenCacheService 初始化完成 - 双层缓存已启用");
    }

    @Override
    public PingcodeTokenDTO getEnterpriseToken() {
        long startTime = System.currentTimeMillis();

        // 1. 尝试从 L1 本地缓存获取
        PingcodeTokenDTO token = localCache.getIfPresent(ENTERPRISE_TOKEN_CACHE_KEY);
        if (token != null && isValidToken(token)) {
            cacheHitCounter.increment();
            log.debug("L1 缓存命中 token");
            return token;
        }

        // 2. 尝试从 L2 Redis 缓存获取
        String redisJson = redisTemplate.opsForValue().get(ENTERPRISE_TOKEN_CACHE_KEY);
        if (StringUtils.hasText(redisJson)) {
            try {
                token = com.alibaba.fastjson2.JSON.parseObject(redisJson, PingcodeTokenDTO.class);
                if (token != null && isValidToken(token)) {
                    // 回写 L1 缓存
                    localCache.put(ENTERPRISE_TOKEN_CACHE_KEY, token);
                    cacheHitCounter.increment();
                    log.debug("L2 Redis 缓存命中 token");
                    return token;
                }
            } catch (Exception e) {
                log.warn("解析 Redis 缓存 token 失败", e);
            }
        }

        // 3. 缓存未命中，获取新令牌
        cacheMissCounter.increment();
        log.info("缓存未命中，从 PingCode API 获取新 token");

        return refreshTokenWithLock();
    }

    @Override
    public PingcodeTokenDTO refreshEnterpriseToken() {
        return refreshTokenTimer.record(() -> {
            log.info("强制刷新 token");
            refreshTokenCounter.increment();

            PingcodeTokenDTO newToken = pingcodeAuthService.getEnterpriseToken();
            cacheToken(newToken);
            lastRefreshTime.set(System.currentTimeMillis());

            return newToken;
        });
    }

    @Override
    public void clearCache() {
        localCache.invalidate(ENTERPRISE_TOKEN_CACHE_KEY);
        redisTemplate.delete(ENTERPRISE_TOKEN_CACHE_KEY);
        log.info("缓存已清除");
    }

    @Override
    public TokenCacheService.CacheStats getCacheStats() {
        long hitCount = localCache.stats() .hitCount();
        long missCount = localCache.stats().missCount();
        double hitRate = (hitCount + missCount > 0) ?
                (double) hitCount / (hitCount + missCount) : 0.0;

        return new TokenCacheService.CacheStats(hitCount, missCount, 0, 0, hitRate);
    }

    /**
     * 使用分布式锁刷新令牌（防止多实例同时刷新）
     */
    private PingcodeTokenDTO refreshTokenWithLock() {
        // 尝试获取分布式锁
        Boolean locked = tryLock();
        if (Boolean.TRUE.equals(locked)) {
            try {
                // 双重检查：获取锁后再次检查缓存（可能被其他实例刷新）
                String redisJson = redisTemplate.opsForValue().get(ENTERPRISE_TOKEN_CACHE_KEY);
                if (StringUtils.hasText(redisJson)) {
                    try {
                        PingcodeTokenDTO token = com.alibaba.fastjson2.JSON.parseObject(redisJson, PingcodeTokenDTO.class);
                        if (token != null && isValidToken(token)) {
                            localCache.put(ENTERPRISE_TOKEN_CACHE_KEY, token);
                            log.debug("获取锁后发现缓存已被其他实例刷新");
                            return token;
                        }
                    } catch (Exception e) {
                        log.warn("解析 Redis 缓存失败", e);
                    }
                }

                // 获取新令牌
                refreshTokenCounter.increment();
                PingcodeTokenDTO newToken = pingcodeAuthService.getEnterpriseToken();
                cacheToken(newToken);
                lastRefreshTime.set(System.currentTimeMillis());
                log.info("成功获取新 token 并缓存");
                return newToken;
            } finally {
                unlock();
            }
        } else {
            // 未能获取锁，等待其他实例刷新
            log.debug("未能获取分布式锁，等待后重试");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 重试一次
            return getEnterpriseToken();
        }
    }

    /**
     * 尝试获取分布式锁
     */
    private Boolean tryLock() {
        return redisTemplate.opsForValue()
                .setIfAbsent(TOKEN_LOCK_KEY, "locked", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 释放分布式锁
     */
    private void unlock() {
        redisTemplate.delete(TOKEN_LOCK_KEY);
    }

    /**
     * 缓存令牌到双层缓存
     */
    private void cacheToken(PingcodeTokenDTO tokenDTO) {
        if (tokenDTO == null || !StringUtils.hasText(tokenDTO.getAccessToken())) {
            throw new BusinessException(502, "PingCode 返回 token 为空");
        }

        long ttlSeconds = calculateTtlSeconds(tokenDTO.getExpiresIn());

        // 写入 L1 本地缓存
        localCache.put(ENTERPRISE_TOKEN_CACHE_KEY, tokenDTO);

        // 写入 L2 Redis 缓存
        redisTemplate.opsForValue().set(
                ENTERPRISE_TOKEN_CACHE_KEY,
                com.alibaba.fastjson2.JSON.toJSONString(tokenDTO),
                ttlSeconds,
                TimeUnit.SECONDS);

        log.debug("Token 已缓存到 L1 和 L2，TTL={} 秒", ttlSeconds);
    }

    /**
     * 计算 TTL（秒）
     */
    private long calculateTtlSeconds(Long expiresIn) {
        if (expiresIn == null || expiresIn <= 0) {
            return 30L * 24 * 60 * 60; // 默认 30 天
        }
        long now = Instant.now().getEpochSecond();
        if (expiresIn > now) {
            return Math.max(expiresIn - now, 60L);
        }
        return Math.max(expiresIn, 60L);
    }

    /**
     * 验证令牌是否有效（未过期）
     */
    private boolean isValidToken(PingcodeTokenDTO token) {
        if (token == null || token.getAccessToken() == null) {
            return false;
        }

        Long expiresIn = token.getExpiresIn();
        if (expiresIn == null || expiresIn <= 0) {
            return true; // 无法判断，假设有效
        }

        long now = Instant.now().getEpochSecond();
        // 如果 expiresIn 是过期时间戳
        if (expiresIn > now) {
            return (expiresIn - now) > REFRESH_THRESHOLD_SECONDS;
        }
        // 如果 expiresIn 是相对过期时间（秒）
        return true;
    }

    /**
     * 定时任务：检查并刷新即将过期的令牌
     * 每 60 秒执行一次
     */
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void scheduledRefreshCheck() {
        try {
            String redisJson = redisTemplate.opsForValue().get(ENTERPRISE_TOKEN_CACHE_KEY);
            if (!StringUtils.hasText(redisJson)) {
                log.debug("定时检查：Redis 缓存为空，跳过");
                return;
            }

            PingcodeTokenDTO token = com.alibaba.fastjson2.JSON.parseObject(redisJson, PingcodeTokenDTO.class);
            if (token == null || !isValidToken(token)) {
                log.info("定时检查：Token 即将过期，主动刷新");
                refreshEnterpriseToken();
            }
        } catch (Exception e) {
            log.warn("定时刷新检查失败", e);
        }
    }
}
