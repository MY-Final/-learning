package com.myfinal.redis.controller;

import com.myfinal.redis.dto.PingcodeTokenDTO;
import com.myfinal.redis.service.PingcodeAuthService;
import com.myfinal.redis.vo.ApiResponse;
import com.myfinal.redis.vo.PingcodeTokenVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pingcode")
public class PingcodeController {

    private final PingcodeAuthService pingcodeAuthService;

    public PingcodeController(PingcodeAuthService pingcodeAuthService) {
        this.pingcodeAuthService = pingcodeAuthService;
    }

    @GetMapping("/token")
    public ApiResponse<PingcodeTokenVO> getEnterpriseToken() {
        PingcodeTokenDTO tokenDTO = pingcodeAuthService.getEnterpriseToken();
        PingcodeTokenVO tokenVO = new PingcodeTokenVO(tokenDTO.getAccessToken(), tokenDTO.getTokenType(), tokenDTO.getExpiresIn());
        return ApiResponse.success("获取token成功", tokenVO);
    }
}
