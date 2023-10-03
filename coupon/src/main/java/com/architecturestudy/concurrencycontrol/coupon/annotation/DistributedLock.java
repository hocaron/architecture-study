package com.architecturestudy.concurrencycontrol.coupon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String key();

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    // wait time : Attempt to acquire the lock for the specified wait time, and if the time elapses without success, return false for the lock acquisition.
    long waitTime() default 5L;

    // lease time : After successfully acquiring the lock, it automatically releases the lock once the lease time expires.
    long leaseTime() default 3L;

}
