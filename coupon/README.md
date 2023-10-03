# 💸 Coupon Issuance Service

## Requirements
- Only one coupon is issued to each user.
- Coupons are issued to users in the order of their requests.
- The quantity of event coupons is limited to 2000.
- The goal is to achieve a throughput of 10,000 to 1,000,000 TPS (Transactions Per Second).

### Concurrency Control Using JAVA synchronized

### Concurrency Control Using DB Lock

### Concurrency Control Using Redis Distributed Lock

# 💸 쿠폰 발행 서비스

## 요구사항
- 쿠폰 수량은 2000 으로 한정되어있다.
- 10,000 ~ 1,000,000 TPS 를 목표로 한다.
- 한명의 사용자에게 하나의 쿠폰만 발행한다.
- 사용자들이 요청한 순서대로 쿠폰을 발행한다.

### synchronized 를 이용한 동시성 제어

### DB 배타락을 이용한 동시성 제어

### 분산락을 이용한 동시성 제어
