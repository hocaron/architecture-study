package com.study.coupon.annotation;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AspectDistributedLock {

	private final DistributedLockManager lockManager;
	private final RedissonCallTransaction redissonCallTransaction;

	@Around("@annotation(com.study.coupon.annotation.DistributedLock)")
	public Object lock(final ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		DistributedLock distributedLock = signature.getMethod().getAnnotation(DistributedLock.class);

		String key = this.createKey(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key().split(","));
		log.info("[CREATE REDISSON KEY] KEY : {}", key);

		return lockManager.executeWithLock(distributedLock, key, () -> redissonCallTransaction.proceed(joinPoint));
	}

	private String createKey(String[] parameterNames, Object[] args, String[] keys) {
		StringBuilder resultKey = new StringBuilder();

		IntStream.range(0, parameterNames.length)
			.filter(i -> Arrays.asList(keys).contains(parameterNames[i]))
			.forEach(i -> resultKey.append(parameterNames[i])
				.append("-")
				.append(args[i])
				.append("-"));

		if (resultKey.length() > 0) {
			resultKey.deleteCharAt(resultKey.length() - 1);
		}

		return resultKey.toString();
	}
}
