package com.study.lock.distributedlock.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpinLock {
    private final RedisClient redisClient;
    private static final String LOCK_PREFIX = "spinlock:";
    private static final Duration MAX_LOCK_DURATION = Duration.ofMinutes(30);
    private static final Duration SPIN_WAIT_TIME = Duration.ofMillis(100);
    private static final Duration LOCK_ATTEMPT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration LOCK_RENEWAL_INTERVAL = Duration.ofSeconds(10);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public boolean acquireAndExecute(String jobName, Runnable task) {
        String lockKey = LOCK_PREFIX + jobName;
        String lockValue = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisCommands<String, String> commands = connection.sync();

            while (System.currentTimeMillis() - startTime < LOCK_ATTEMPT_TIMEOUT.toMillis()) {
                SetArgs setArgs = SetArgs.Builder.nx().px(MAX_LOCK_DURATION.toMillis());
                String result = commands.set(lockKey, lockValue, setArgs);
                if ("OK".equals(result)) {
                    try {
                        scheduleLockRenewal(commands, lockKey, lockValue);
                        task.run();
                        return true;
                    } finally {
                        stopLockRenewal();
                        releaseLock(commands, lockKey, lockValue);
                    }
                }
                Thread.sleep(SPIN_WAIT_TIME.toMillis());
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void scheduleLockRenewal(RedisCommands<String, String> commands, String lockKey, String lockValue) {
        scheduler.scheduleAtFixedRate(() -> {
            String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('pexpire', KEYS[1], ARGV[2]) " +
                    "else " +
                    "   return 0 " +
                    "end";
            Long result = commands.eval(script, io.lettuce.core.ScriptOutputType.INTEGER,
                new String[]{lockKey}, lockValue, String.valueOf(MAX_LOCK_DURATION.toMillis()));
            if (result == 0L) {
                stopLockRenewal();
            }
        }, LOCK_RENEWAL_INTERVAL.toMillis(), LOCK_RENEWAL_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void stopLockRenewal() {
        scheduler.shutdownNow();
    }

    private void releaseLock(RedisCommands<String, String> commands, String lockKey, String lockValue) {
        String script =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "   return redis.call('del', KEYS[1]) " +
                "else " +
                "   return 0 " +
                "end";
        commands.eval(script, io.lettuce.core.ScriptOutputType.INTEGER, new String[]{lockKey}, lockValue);
    }
}
