package com.architecturestudy.concurrencycontrol.coupon.event;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"eventId", "memberId"}))
public class Coupon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long eventId;

	private Long memberId;

	public static Coupon from(Long eventId, Long memberId) {

		return new Coupon(eventId, memberId);
	}

	protected Coupon(Long eventId, Long memberId) {

		this.eventId = eventId;
		this.memberId = memberId;
	}
}
