package com.myfinal.redis.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myfinal.redis.dto.PingcodeTokenDTO;
import com.myfinal.redis.service.PingcodeAuthService;
import com.myfinal.redis.service.TokenCacheService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * TokenCacheService 单元测试
 */
class TokenCacheServiceTest {

    private PingcodeAuthService mockAuthService;
    private StringRedisTemplate mockRedisTemplate;
    private ValueOperations<String, String> mockValueOps;
    private SimpleMeterRegistry meterRegistry;

    private TokenCacheService tokenCacheService;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(PingcodeAuthService.class);
        mockRedisTemplate = mock(StringRedisTemplate.class);
        mockValueOps = mock(ValueOperations.class);
        meterRegistry = new SimpleMeterRegistry();

        when(mockRedisTemplate.opsForValue()).thenReturn(mockValueOps);

        tokenCacheService = new TokenCacheServiceImpl(mockAuthService, mockRedisTemplate, meterRegistry);
    }

    @Test
    @DisplayName("首次获取 token 时，缓存未命中应调用 API")
    void getEnterpriseToken_firstCall_shouldCallApi() throws Exception {
        // Given
        PingcodeTokenDTO expectedToken = new PingcodeTokenDTO("access_token_123", "Bearer", 3600L);
        when(mockAuthService.getEnterpriseToken()).thenReturn(expectedToken);
        when(mockValueOps.get(anyString())).thenReturn(null);

        // When
        PingcodeTokenDTO result = tokenCacheService.getEnterpriseToken();

        // Then
        assertNotNull(result);
        assertEquals("access_token_123", result.getAccessToken());
        verify(mockAuthService).getEnterpriseToken();
    }

    @Test
    @DisplayName("缓存命中时不应调用 API")
    void getEnterpriseToken_cached_shouldNotCallApi() throws Exception {
        // Given
        PingcodeTokenDTO cachedToken = new PingcodeTokenDTO("cached_token", "Bearer", 3600L);
        String cachedJson = com.alibaba.fastjson2.JSON.toJSONString(cachedToken);
        when(mockValueOps.get("pingcode:token:enterprise")).thenReturn(cachedJson);

        // When
        PingcodeTokenDTO result = tokenCacheService.getEnterpriseToken();

        // Then
        assertNotNull(result);
        assertEquals("cached_token", result.getAccessToken());
        verify(mockAuthService, never()).getEnterpriseToken();
    }

    @Test
    @DisplayName("强制刷新应绕过缓存直接调用 API")
    void refreshEnterpriseToken_shouldAlwaysCallApi() throws Exception {
        // Given
        PingcodeTokenDTO newToken = new PingcodeTokenDTO("new_token", "Bearer", 7200L);
        when(mockAuthService.getEnterpriseToken()).thenReturn(newToken);

        // When
        PingcodeTokenDTO result = tokenCacheService.refreshEnterpriseToken();

        // Then
        assertNotNull(result);
        assertEquals("new_token", result.getAccessToken());
        verify(mockAuthService).getEnterpriseToken();
    }

    @Test
    @DisplayName("清除缓存应删除本地和 Redis 缓存")
    void clearCache_shouldRemoveAllCache() {
        // When
        tokenCacheService.clearCache();

        // Then
        verify(mockRedisTemplate).delete("pingcode:token:enterprise");
    }

    @Test
    @DisplayName("获取缓存统计应返回正确的命中率")
    void getCacheStats_shouldReturnCorrectHitRate() {
        // Given & When - 先访问几次
        when(mockValueOps.get(anyString())).thenReturn(null);
        when(mockAuthService.getEnterpriseToken()).thenReturn(
                new PingcodeTokenDTO("token", "Bearer", 3600L)
        );

        tokenCacheService.getEnterpriseToken();
        tokenCacheService.getEnterpriseToken();

        TokenCacheService.CacheStats stats = tokenCacheService.getCacheStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats.localHitRate() >= 0.0 && stats.localHitRate() <= 1.0);
    }
}
