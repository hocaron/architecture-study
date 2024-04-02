package com.architecturestudy.concurrencycontrol.coupon.annotation;

import org.redisson.RedissonFairLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockManager {

	private final RedissonClient redissonClient;

	public Object executeWithLock(DistributedLock distributedLock, String key, LockCallback callback) {

		RLock rLock = redissonClient.getFairLock(key);

		try {
			boolean isPossible = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());

			if (!isPossible) {
				throw new RuntimeException();
			}

			return callback.executeLocked();
		} catch (RuntimeException be) {
			throw be;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException();
		} finally {
			rLock.unlock();
		}
	}
}

