package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void testLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        if (lock) {
            String value = redisTemplate.opsForValue().get("num");
            if (StringUtils.isEmpty(value)) {
                return;
            }
            Integer num = Integer.valueOf(value);
            redisTemplate.opsForValue().set("num", String.valueOf(++num));
            // 定义lua 脚本
            String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setResultType(Long.class);
            redisScript.setScriptText(script);
            redisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);

        }else {
            try {
                Thread.sleep(3);
                this.testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
