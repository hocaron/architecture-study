# 💸 Coupon Issuance Service

## Requirements
- Only one coupon is issued to each user.
- Coupons are issued to users in the order of their requests.
- The quantity of event coupons is limited to 2000.
- The goal is to achieve a throughput of 10,000 to 1,000,000 TPS (Transactions Per Second).

## Concurrency Control

### Concurrency Control Using JAVA synchronized
- Advantages
  - It is easy to use as an inherent synchronization mechanism provided in Java.
  - It provides a straightforward and clear way to control race conditions between threads.
- Disadvantages
  - Only the thread holding the lock can execute the synchronized block, which can lead to the risk of deadlock.
  - It may be less efficient in terms of performance compared to other concurrency control mechanisms.
  - When using database locks, it is not possible to read data for sequential decrement until the transaction is committed
  - It cannot be used in a multi-server environment.
### Concurrency Control Using DB Lock
- Advantages
  - It is a robust method to ensure data consistency using database transactions.
  - Useful for resolving concurrency issues in a multi-server environment.
- Disadvantages
  - Using database locks can lead to performance degradation.
  - Mishandling database locks can lead to deadlocks, requiring complex logic to handle them.
### Concurrency Control Using Redis Distributed Lock
- Advantages
  - Useful for handling concurrency issues in distributed systems.
  - Various distributed lock algorithms allow for fine-grained concurrency control.
- Disadvantages
  - Implementation and management can be complex and challenging to get right.
  - Performance may be affected by external factors such as network latency.

# 💸 쿠폰 발행 서비스

## 요구사항
- 쿠폰 수량은 2000 으로 한정되어있다.
- 10,000 ~ 1,000,000 TPS 를 목표로 한다.
- 한명의 사용자에게 하나의 쿠폰만 발행한다.
- 사용자들이 요청한 순서대로 쿠폰을 발행한다.

## 동시성 제어
### synchronized 를 이용한 동시성 제어
- 장점
  - 자바에서 제공하는 내장 동기화 메커니즘으로 사용하기 쉽다.
  - 간단하고 명료한 방법으로 스레드 간의 경쟁 조건을 제어할 수 있다.
- 단점
  - 락을 획득한 스레드만 해당 블록을 실행할 수 있으므로 데드락의 위험이 존재한다.
  - 성능 면에서 다른 동시성 제어 메커니즘에 비해 비효율적일 수 있다.
  - 다중 서버 환경에서 사용할 수 없다.
### DB 배타락을 이용한 동시성 제어
- 장점
  - 데이터베이스 트랜잭션을 활용하여 데이터 일관성을 보장하는 강력한 방법이다.
  - 다중 서버 환경에서 동시성 문제를 해결하는데 유용하다.
- 단점
  - 데이터베이스 락을 사용하면 성능 저하가 발생할 수 있다.
  - 데이터베이스 락을 오용하면 데드락이 발생할 수 있으며, 이를 처리하는데 복잡한 로직이 필요하다.
  - 데이터베이스 락을 사용하면, 해당 트랜잭션 커밋전까지 읽기가 불가능하여, 순차차감 데이터에 대해 조회가 불가능하다.
### 분산락을 이용한 동시성 제어
- 장점
  - 분산 시스템에서의 동시성 문제를 처리하는데 유용하다.
  - 다양한 분산락 알고리즘을 통해 세밀한 동시성 제어를 가능하게 한다.
- 단점
  - 구현과 관리가 복잡하며, 올바르게 구현하기 어렵다.
  - 네트워크 지연 등과 같은 외부 요인에 의해 성능이 영향을 받을 수 있다.
