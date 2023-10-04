package com.architecturestudy.concurrencycontrol.coupon.event;

import java.util.Random;

import javax.persistence.EntityExistsException;

import org.springframework.stereotype.Service;

import com.architecturestudy.concurrencycontrol.coupon.annotation.DistributedLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

	private final CouponRepository couponRepository;

	private final EventRepository eventRepository;

	@DistributedLock(key = "eventId", waitTime = 20L, leaseTime = 7L)
	public Coupon issue(final Long eventId) {

		Event event = eventRepository.findById(eventId)
			.orElseThrow();

		Long memberId = new Random().nextLong();
		if (couponRepository.findByMemberId(memberId).isPresent()) {
			throw new EntityExistsException();
		}

		Coupon coupon = couponRepository.save(Coupon.from(event.getId(), memberId));
		event.decreaseCouponQuantity();

		log.info("[COUPON ISSUED] COUPON ID : {}, MEMBER ID : {}", coupon.getId(), coupon.getMemberId());

		return coupon;
	}
}
