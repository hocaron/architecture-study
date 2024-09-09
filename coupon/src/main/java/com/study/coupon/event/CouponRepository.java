package com.study.coupon.event;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

	Optional<Coupon> findByMemberId(Long memberId);
}
