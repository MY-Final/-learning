package com.myfinal.redis.service.impl;

import com.myfinal.redis.config.PingcodeProperties;
import com.myfinal.redis.constant.PingcodeApiConstants;
import com.myfinal.redis.dto.PingcodeTokenDTO;
import com.myfinal.redis.exception.BusinessException;
import com.myfinal.redis.service.PingcodeAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PingcodeAuthServiceImpl implements PingcodeAuthService {

    private final PingcodeProperties pingcodeProperties;

    private final RestClient.Builder restClientBuilder;

    @Override
    public PingcodeTokenDTO getEnterpriseToken() {
        String clientId = pingcodeProperties.getToken().getClientId();
        String clientSecret = pingcodeProperties.getToken().getClientSecret();
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            throw new BusinessException(400, "请先在 application.yml 或环境变量中配置 PingCode client-id 和 client-secret");
        }

        String url = UriComponentsBuilder.fromHttpUrl(pingcodeProperties.getRestApiRoot())
                .path(PingcodeApiConstants.TOKEN_PATH)
                .queryParam("grant_type", pingcodeProperties.getToken().getGrantType())
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .toUriString();

        RestClient restClient = restClientBuilder.build();
        PingcodeTokenDTO tokenDTO = restClient.get()
                .uri(url)
                .retrieve()
                .body(PingcodeTokenDTO.class);
        if (tokenDTO == null || !StringUtils.hasText(tokenDTO.getAccessToken())) {
            throw new BusinessException(502, "PingCode返回token为空");
        }
        return tokenDTO;
    }
}
