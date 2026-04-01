package com.myfinal.redis.service;

import com.myfinal.redis.dto.PingcodeTokenDTO;

/**
 * Token 缓存服务接口
 */
public interface TokenCacheService {

    /**
     * 获取企业令牌（优先从缓存获取）
     * @return 令牌 DTO
     */
    PingcodeTokenDTO getEnterpriseToken();

    /**
     * 强制刷新令牌（绕过缓存）
     * @return 新的令牌 DTO
     */
    PingcodeTokenDTO refreshEnterpriseToken();

    /**
     * 清除缓存
     */
    void clearCache();

    /**
     * 获取缓存统计信息
     * @return 缓存统计
     */
    CacheStats getCacheStats();

    /**
     * 缓存统计信息
     */
    record CacheStats(
            long localHitCount,
            long localMissCount,
            long redisHitCount,
            long redisMissCount,
            double localHitRate
    ) {}
}
