package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * SneakyThrows小辣椒的异常处理注解
     * Around环绕通知
     * @param joinPoint
     * @return
     */
    @SneakyThrows
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) {

        Object object = new Object();
        //获取到使用这个注解的方法 invoke
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
        //获取注解的前缀
        String prefix = gmallCache.prefix();
        //获取使用这个注解的方法的参数
        Object[] args = joinPoint.getArgs();
        String key = prefix + Arrays.asList(args).toString();
        try {
            object = cacheHit(key, signature);
            if (object == null) {
                String lockKey = prefix + ":lock:";
                RLock lock = redissonClient.getLock(lockKey);
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (result) {
                    try {
                        //执行方法中的方法体
                        object = joinPoint.proceed(joinPoint.getArgs());
                        if (object == null) {
                            Object object1 = new Object();
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(object1), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return object1;
                        }
                        redisTemplate.opsForValue().set(key, JSON.toJSONString(object), RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return object;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }else {
                    Thread.sleep(1000);
                    return cacheAroundAdvice(joinPoint);
                }
            }else {
                return object;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return joinPoint.proceed(joinPoint.getArgs());
    }

    /**
     * 从缓存中获取数据
     * @param key
     * @param signature
     * @return
     */
    private Object cacheHit(String key, MethodSignature signature) {

        String strJson = (String) redisTemplate.opsForValue().get(key);
        //  表示从缓存中获取到了数据
        if (!StringUtils.isEmpty(strJson)){
            //  字符串存储的数据是什么?   就是方法的返回值类型
            Class returnType = signature.getReturnType();
            //  将字符串变为当前的返回值类型
            return JSON.parseObject(strJson,returnType);
        }
        return null;
    }
}
