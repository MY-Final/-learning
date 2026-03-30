package com.myfinal.redis.vo;

import lombok.Data;

@Data
public class PingcodeTokenVO {

    private String accessToken;

    private String tokenType;

    private Long expiresIn;

    public PingcodeTokenVO() {
    }

    public PingcodeTokenVO(String accessToken, String tokenType, Long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }
}
