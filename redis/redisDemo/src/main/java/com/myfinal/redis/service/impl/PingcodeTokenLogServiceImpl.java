package com.myfinal.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myfinal.redis.pojo.PingcodeTokenLog;
import com.myfinal.redis.service.PingcodeTokenLogService;
import com.myfinal.redis.mapper.PingcodeTokenLogMapper;
import org.springframework.stereotype.Service;

/**
* @author 34861
* @description 针对表【pingcode_token_log(PingCode token获取记录表)】的数据库操作Service实现
* @createDate 2026-03-30 22:50:13
*/
@Service
public class PingcodeTokenLogServiceImpl extends ServiceImpl<PingcodeTokenLogMapper, PingcodeTokenLog>
    implements PingcodeTokenLogService{

}




