package com.architecturestudy.concurrencycontrol.coupon.annotation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AspectRedissonLock {

    private final RedissonClient redissonClient;
    private final RedissonCallTransaction redissonCallTransaction;

    @Around("@annotation(com.architecturestudy.concurrencycontrol.coupon.annotation.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributeLock = method.getAnnotation(DistributedLock.class);

        String key = this.createKey(signature.getParameterNames(), joinPoint.getArgs(), distributeLock.key().split(","));

        RLock rLock = redissonClient.getLock(key);

        try {
            boolean isPossible = rLock.tryLock(distributeLock.waitTime(), distributeLock.leaseTime(), distributeLock.timeUnit());

            if (!isPossible) {
                throw new RuntimeException(key);
            }

            log.info("[CREATE REDISSON KEY] KEY : {}", key);

            return redissonCallTransaction.proceed(joinPoint);
        } catch (RuntimeException be) {
            throw be;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally {
            rLock.unlock();
        }
    }

    private String createKey(String[] parameterNames, Object[] args, String[] keys) {
        StringBuilder resultKey = new StringBuilder();

        for (int i = 0; i < parameterNames.length; i++) {
            for (String key : keys) {
                if (parameterNames[i].equals(key)) {
                    resultKey.append(key)
                        .append("-")
                        .append(args[i])
                        .append("-");
                }
            }
        }
        resultKey.deleteCharAt(resultKey.length() - 1);
        return resultKey.toString();
    }
}
