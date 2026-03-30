package com.myfinal.redis.controller;

import com.alibaba.fastjson2.JSON;
import com.myfinal.redis.dto.PingcodeTokenDTO;
import com.myfinal.redis.exception.BusinessException;
import com.myfinal.redis.service.PingcodeAuthService;
import com.myfinal.redis.vo.ApiResponse;
import com.myfinal.redis.vo.PingcodeTokenVO;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pingcode")
public class PingcodeController {

    private static final String ENTERPRISE_TOKEN_CACHE_KEY = "pingcode:token:enterprise";

    private final PingcodeAuthService pingcodeAuthService;
    private final StringRedisTemplate stringRedisTemplate;

    public PingcodeController(PingcodeAuthService pingcodeAuthService, StringRedisTemplate stringRedisTemplate) {
        this.pingcodeAuthService = pingcodeAuthService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @GetMapping("/token")
    public ApiResponse<PingcodeTokenVO> getEnterpriseToken() {
        PingcodeTokenDTO tokenDTO = pingcodeAuthService.getEnterpriseToken();
        cacheEnterpriseToken(tokenDTO);
        PingcodeTokenVO tokenVO = new PingcodeTokenVO(tokenDTO.getAccessToken(), tokenDTO.getTokenType(), tokenDTO.getExpiresIn());
        return ApiResponse.success("获取token成功", tokenVO);
    }

    private void cacheEnterpriseToken(PingcodeTokenDTO tokenDTO) {
        if (!StringUtils.hasText(tokenDTO.getAccessToken())) {
            throw new BusinessException(502, "PingCode返回token为空");
        }
        long ttlSeconds = calculateTtlSeconds(tokenDTO.getExpiresIn());
        stringRedisTemplate.opsForValue().set(
                ENTERPRISE_TOKEN_CACHE_KEY,
                JSON.toJSONString(tokenDTO),
                ttlSeconds,
                TimeUnit.SECONDS);
    }

    private long calculateTtlSeconds(Long expiresIn) {
        if (expiresIn == null || expiresIn <= 0) {
            return 30L * 24 * 60 * 60;
        }
        long now = Instant.now().getEpochSecond();
        if (expiresIn > now) {
            return Math.max(expiresIn - now, 60L);
        }
        return Math.max(expiresIn, 60L);
    }
}
