package com.study.coupon.annotation;

@FunctionalInterface
public interface LockCallback {
	Object executeLocked() throws Throwable;
}
