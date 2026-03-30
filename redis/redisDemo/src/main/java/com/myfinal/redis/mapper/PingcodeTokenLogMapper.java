package com.myfinal.redis.mapper;

import com.myfinal.redis.pojo.PingcodeTokenLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 34861
* @description 针对表【pingcode_token_log(PingCode token获取记录表)】的数据库操作Mapper
* @createDate 2026-03-30 22:50:13
* @Entity com.myfinal.redis.pojo.PingcodeTokenLog
*/
@Mapper
public interface PingcodeTokenLogMapper extends BaseMapper<PingcodeTokenLog> {

}




