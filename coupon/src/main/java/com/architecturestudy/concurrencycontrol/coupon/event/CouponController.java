package com.architecturestudy.concurrencycontrol.coupon.event;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/coupons")
public class CouponController {

	private final CouponService couponService;

	@PostMapping("events/{eventId}")
	public Coupon issue(@PathVariable Long eventId) {

		return couponService.issue(eventId);
	}
}
