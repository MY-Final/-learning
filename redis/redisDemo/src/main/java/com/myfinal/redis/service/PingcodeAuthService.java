package com.myfinal.redis.service;

import com.myfinal.redis.dto.PingcodeTokenDTO;

public interface PingcodeAuthService {

    PingcodeTokenDTO getEnterpriseToken();
}
