package com.example.coupon.annotation;

@FunctionalInterface
public interface LockCallback {
	Object executeLocked() throws Throwable;
}
