package com.architecturestudy.concurrencycontrol.coupon.annotation;

@FunctionalInterface
public interface LockCallback {
	Object executeLocked() throws Throwable;
}
