package com.study.lock.distributedlock.mysql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShedLock {
    private final JdbcTemplate jdbcTemplate;
    private static final long LOCK_DURATION_MINUTES = 15; // 최대 락 유지 시간
    private static final long MAX_LOCK_DURATION_MINUTES = 30; // 최대 락 유지 시간

    /**s
     * 분산 환경에서 배치 작업의 중복 실행을 방지하는 메서드
     */
    @Transactional
    public boolean acquireAndExecuteLock(String jobName, Runnable task) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String serverIdentifier = getServerIdentifier();

            // 락 획득 시도
            boolean lockAcquired = insertLock(jobName, serverIdentifier, now);
            if (!lockAcquired) {
                return false;
            }

            try {
                // 작업 실행
                task.run();

                // 작업 성공 시 상태 업데이트
                updateLockStatus(jobName, "COMPLETED", null);
                return true;
            } catch (Exception e) {
                // 작업 실패 시 상태 업데이트
                updateLockStatus(jobName, "FAILED", getErrorMessage(e));
                throw e;
            }
        } catch (Exception e) {
            log.error("Failed to execute job: {}", jobName, e);
            return false;
        }
    }

    /**
     * 서버 식별자 생성
     *
     * @return 유니크한 서버 식별자
     */
    private String getServerIdentifier() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error("Failed to get server hostname", e);
            return UUID.randomUUID().toString();
        }
    }

    /**
     * 오류 메시지 추출 (길이 제한)
     */
    private String getErrorMessage(Exception e) {
        String message = e.toString();
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    /**
     * 새로운 락을 데이터베이스에 삽입
     */
    private boolean insertLock(String jobName, String serverIdentifier, LocalDateTime lockedAt) {
        try {
            int updatedRows = jdbcTemplate.update(
                "INSERT INTO shedlock " +
                    "(name, locked_at, locked_by, lock_until, status) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "locked_at = ?, " +
                    "locked_by = ?, " +
                    "lock_until = ?, " +
                    "status = 'RUNNING' " +
                    "WHERE TIMESTAMPDIFF(MINUTE, locked_at, ?) >= ?",
                jobName,
                lockedAt,
                serverIdentifier,
                lockedAt.plusMinutes(MAX_LOCK_DURATION_MINUTES),
                "RUNNING",
                lockedAt,
                serverIdentifier,
                lockedAt.plusMinutes(MAX_LOCK_DURATION_MINUTES),
                lockedAt,
                MAX_LOCK_DURATION_MINUTES
            );
            return updatedRows > 0;
        } catch (Exception e) {
            log.error("Failed to insert or update lock", e);
            return false;
        }
    }

    /**
     * 락의 상태를 업데이트
     */
    private void updateLockStatus(String jobName, String status, String errorMessage) {
        jdbcTemplate.update(
            "UPDATE shedlock " +
                "SET status = ?, " +
                "    completed_at = NOW(), " +
                "    error_message = ? " +
                "WHERE name = ?",
            status,
            errorMessage,
            jobName
        );
    }
}
