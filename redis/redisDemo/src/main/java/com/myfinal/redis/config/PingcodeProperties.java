package com.myfinal.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "pingcode")
public class PingcodeProperties {

    private String appName;

    private String restApiRoot;

    private String oauth2Root;

    private final Token token = new Token();

    @Data
    public static class Token {
        private String grantType;
        private String clientId;
        private String clientSecret;
    }
}
