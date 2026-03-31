package com.myfinal.redis.controller;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSON;
import com.myfinal.redis.config.PingcodeProperties;
import com.myfinal.redis.dto.PingcodeTokenDTO;
import com.myfinal.redis.exception.BusinessException;
import com.myfinal.redis.pojo.PingcodeTokenLog;
import com.myfinal.redis.service.PingcodeAuthService;
import com.myfinal.redis.service.PingcodeTokenLogService;
import com.myfinal.redis.vo.ApiResponse;
import com.myfinal.redis.vo.PingcodeTokenVO;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
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
    private final PingcodeTokenLogService pingcodeTokenLogService;
    private final PingcodeProperties pingcodeProperties;

    public PingcodeController(PingcodeAuthService pingcodeAuthService,
                              StringRedisTemplate stringRedisTemplate,
                              PingcodeTokenLogService pingcodeTokenLogService,
                              PingcodeProperties pingcodeProperties) {
        this.pingcodeAuthService = pingcodeAuthService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.pingcodeTokenLogService = pingcodeTokenLogService;
        this.pingcodeProperties = pingcodeProperties;
    }

    @GetMapping("/token")
    public ApiResponse<PingcodeTokenVO> getEnterpriseToken() {
        PingcodeTokenDTO tokenDTO = pingcodeAuthService.getEnterpriseToken();
        cacheEnterpriseToken(tokenDTO);
        saveTokenLog(tokenDTO);
        PingcodeTokenVO tokenVO = new PingcodeTokenVO(tokenDTO.getAccessToken(), tokenDTO.getTokenType(), tokenDTO.getExpiresIn());
        return ApiResponse.success("获取token成功", tokenVO);
    }

    private void saveTokenLog(PingcodeTokenDTO tokenDTO) {
        PingcodeTokenLog log = new PingcodeTokenLog();
        log.setRequestId(UUID.randomUUID().toString());
        log.setSource("pingcode_openapi");
        log.setAppName(pingcodeProperties.getAppName());
        log.setClientId(pingcodeProperties.getToken().getClientId());
        log.setTokenHash(SecureUtil.sha256(tokenDTO.getAccessToken()));
        log.setTokenPrefix(tokenDTO.getAccessToken().substring(0, Math.min(8, tokenDTO.getAccessToken().length())));
        log.setAcquireStatus(1);
        log.setAcquiredAt(LocalDateTime.now());
        log.setExpiresIn(toIntSafely(tokenDTO.getExpiresIn()));
        log.setExpiresAt(resolveExpiresAt(tokenDTO.getExpiresIn()));
        log.setHttpStatus(200);
        log.setResponseBody(JSON.toJSONString(tokenDTO));
        log.setRemark("手动接口获取企业token并写入redis");
        boolean saved = pingcodeTokenLogService.save(log);
        if (!saved) {
            throw new BusinessException(500, "token日志写入失败");
        }
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
