package com.example.aspect;

import com.example.SpElUtil;
import com.example.annotion.RepeatExecuteLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RepeatExecuteLimitAspect {

    private final StringRedisTemplate redisTemplate;

    // 幂等标识前缀
    private static final String PREFIX_NAME = "REPEAT_LIMIT:";
    // 成功标识
    private static final String SUCCESS_FLAG = "SUCCESS";

    @Around("@annotation(repeatLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RepeatExecuteLimit repeatLimit) throws Throwable {
        long durationTime = repeatLimit.durationTime();
        String message = repeatLimit.message();

        // 1. 解析出锁名
        String lockName = generateLockName(joinPoint, repeatLimit);
        String repeatFlagName = PREFIX_NAME + lockName;

        // 2. 第一次判定：如果在防重窗口内，已经标记过成功，则直接拦截
        String flagObject = redisTemplate.opsForValue().get(repeatFlagName);
        if (SUCCESS_FLAG.equals(flagObject)) {
            throw new RuntimeException(message); // 这里可以换成你们项目自定义的业务异常
        }

        // 3. 尝试获取分布式锁 (利用 Redis SETNX，等待时间为 0)
        Boolean locked = redisTemplate.opsForValue().setIfAbsent("LOCK:" + lockName, "1", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(locked)) {
            try {
                // 4. 双重判定：拿到锁后，再查一次，防止前面刚执行完释放了锁
                flagObject = redisTemplate.opsForValue().get(repeatFlagName);
                if (SUCCESS_FLAG.equals(flagObject)) {
                    throw new RuntimeException(message);
                }

                // 5. 执行真实业务逻辑
                Object result = joinPoint.proceed();

                // 6. 业务执行成功，设置幂等成功标识，过期时间为 durationTime
                if (durationTime > 0) {
                    redisTemplate.opsForValue().set(repeatFlagName, SUCCESS_FLAG, durationTime, TimeUnit.SECONDS);
                }

                return result;
            } finally {
                // 7. 释放分布式锁
                redisTemplate.delete("LOCK:" + lockName);
            }
        } else {
            // 没拿到锁，说明有其他线程正在执行该业务，直接拒绝当前请求
            throw new RuntimeException(message);
        }
    }

    /**
     * 生成 Redis Key
     */
    private String generateLockName(ProceedingJoinPoint joinPoint, RepeatExecuteLimit repeatLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 解析 SpEL
        List<String> dynamicKeys = SpElUtil.parse(method, joinPoint.getArgs(), repeatLimit.keys());

        // 拼接 Key： 前缀 + 业务名 + 动态参数
        StringBuilder sb = new StringBuilder();
        sb.append(repeatLimit.name());
        for (String key : dynamicKeys) {
            sb.append(":").append(key);
        }

        return sb.toString();
    }
}
