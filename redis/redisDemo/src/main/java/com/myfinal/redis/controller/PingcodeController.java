package com.myfinal.redis.controller;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSON;
import com.myfinal.redis.config.PingcodeProperties;
import com.myfinal.redis.dto.PingcodeTokenDTO;
import com.myfinal.redis.pojo.PingcodeTokenLog;
import com.myfinal.redis.service.PingcodeTokenLogService;
import com.myfinal.redis.service.TokenCacheService;
import com.myfinal.redis.vo.ApiResponse;
import com.myfinal.redis.vo.PingcodeTokenVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PingCode Token 管理", description = "企业 Token 获取和缓存管理")
@RestController
@RequestMapping("/api/pingcode")
public class PingcodeController {

    private static final Logger log = LoggerFactory.getLogger(PingcodeController.class);

    private final TokenCacheService tokenCacheService;
    private final PingcodeTokenLogService pingcodeTokenLogService;
    private final PingcodeProperties pingcodeProperties;

    public PingcodeController(TokenCacheService tokenCacheService,
                              PingcodeTokenLogService pingcodeTokenLogService,
                              PingcodeProperties pingcodeProperties) {
        this.tokenCacheService = tokenCacheService;
        this.pingcodeTokenLogService = pingcodeTokenLogService;
        this.pingcodeProperties = pingcodeProperties;
    }

    @GetMapping("/token")
    @Operation(summary = "获取企业 token", description = "从双层缓存获取 token，如果缓存失效则从 PingCode API 刷新")
    public ApiResponse<PingcodeTokenVO> getEnterpriseToken() {
        log.info("接收到获取 token 请求");

        PingcodeTokenDTO tokenDTO = tokenCacheService.getEnterpriseToken();
        saveTokenLog(tokenDTO, "auto_cache");

        PingcodeTokenVO tokenVO = new PingcodeTokenVO(
                tokenDTO.getAccessToken(),
                tokenDTO.getTokenType(),
                tokenDTO.getExpiresIn()
        );
        return ApiResponse.success("获取 token 成功", tokenVO);
    }

        @GetMapping("/token/refresh")
    @Operation(summary = "强制刷新 token", description = "绕过缓存，直接从 PingCode API 获取新 token")
    public ApiResponse<PingcodeTokenVO> refreshEnterpriseToken() {
        log.info("接收到强制刷新 token 请求");

        PingcodeTokenDTO tokenDTO = tokenCacheService.refreshEnterpriseToken();
        saveTokenLog(tokenDTO, "manual_refresh");

        PingcodeTokenVO tokenVO = new PingcodeTokenVO(
                tokenDTO.getAccessToken(),
                tokenDTO.getTokenType(),
                tokenDTO.getExpiresIn()
        );
        return ApiResponse.success("刷新 token 成功", tokenVO);
    }

    @DeleteMapping("/token/cache")
    @Operation(summary = "清除 token 缓存", description = "清除本地和 Redis 中的 token 缓存")
    public ApiResponse<Void> clearCache() {
        log.info("接收到清除缓存请求");
        tokenCacheService.clearCache();
        return ApiResponse.success("清除缓存成功", null);
    }

    @GetMapping("/token/cache/stats")
    @Operation(summary = "获取缓存统计", description = "返回缓存命中率等统计信息")
    public ApiResponse<TokenCacheService.CacheStats> getCacheStats() {
        TokenCacheService.CacheStats stats = tokenCacheService.getCacheStats();
        return ApiResponse.success("获取缓存统计成功", stats);
    }

    private void saveTokenLog(PingcodeTokenDTO tokenDTO, String acquireType) {
        try {
            PingcodeTokenLog logEntry = new PingcodeTokenLog();
            logEntry.setRequestId(UUID.randomUUID().toString());
            logEntry.setSource("pingcode_openapi");
            logEntry.setAppName(pingcodeProperties.getAppName());
            logEntry.setClientId(pingcodeProperties.getToken().getClientId());
            logEntry.setTokenHash(SecureUtil.sha256(tokenDTO.getAccessToken()));
            logEntry.setTokenPrefix(tokenDTO.getAccessToken().substring(0, Math.min(8, tokenDTO.getAccessToken().length())));
            logEntry.setAcquireStatus(1);
            logEntry.setAcquiredAt(LocalDateTime.now());
            logEntry.setExpiresIn(toIntSafely(tokenDTO.getExpiresIn()));
            logEntry.setExpiresAt(resolveExpiresAt(tokenDTO.getExpiresIn()));
            logEntry.setHttpStatus(200);
            logEntry.setResponseBody(JSON.toJSONString(tokenDTO));
            logEntry.setRemark(acquireType + " - 双层缓存优化版本");

            boolean saved = pingcodeTokenLogService.save(logEntry);
            if (!saved) {
                log.warn("token 日志写入失败");
            } else {
                log.debug("token 日志写入成功，requestId={}", logEntry.getRequestId());
            }
        } catch (Exception e) {
            log.error("保存 token 日志失败", e);
            // 不抛出异常，避免影响主流程
        }
    }

    private LocalDateTime resolveExpiresAt(Long expiresIn) {
        if (expiresIn == null || expiresIn <= 0) {
            return null;
        }
        long now = Instant.now().getEpochSecond();
        if (expiresIn > now) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(expiresIn), ZoneId.systemDefault());
        }
        return LocalDateTime.now().plusSeconds(expiresIn);
    }

    private Integer toIntSafely(Long value) {
        if (value == null) {
            return null;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return value.intValue();
    }
}
