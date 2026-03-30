package com.myfinal.redis;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisDemoApplicationTests {

    @Test
    void contextLoads() {
    }
    /**
     * 测试 redis 是否连接成功
     */
    @Test
    public void testRedis() {
        // 获取 redisTemplate 对象
        RedisTemplate redisTemplate = SpringUtil.getBean("redisTemplate");
        // 测试 redis 连接
        redisTemplate.opsForValue().set("test", "test");
        System.out.println(redisTemplate.opsForValue().get("test"));
        System.out.println("连接成功");
    }

}
