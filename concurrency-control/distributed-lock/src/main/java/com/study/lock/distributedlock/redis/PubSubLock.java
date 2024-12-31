package com.study.lock.distributedlock.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PubSubLock {
    private final RedisClient redisClient;
    private static final String LOCK_PREFIX = "pubsub_lock:";
    private static final String CHANNEL_PREFIX = "lock_channel:";
    private static final String KEYSPACE_CHANNEL_PREFIX = "__keyspace@0__:";
    private static final Duration MAX_LOCK_DURATION = Duration.ofMinutes(30);
    private static final Duration LOCK_ATTEMPT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration LOCK_RENEWAL_INTERVAL = Duration.ofSeconds(10);

    private final ConcurrentHashMap<String, CountDownLatch> lockWaiters = new ConcurrentHashMap<>();
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private void initPubSubListener() {
        pubSubConnection.addListener(new RedisPubSubAdapter<String, String>() {
            @Override
            public void message(String channel, String message) {
                if (message.equals("RELEASED") ||
                    (channel.startsWith(KEYSPACE_CHANNEL_PREFIX) &&
                        (message.equals("expired") || message.equals("del")))) {
                    String jobName;
                    if (channel.startsWith(CHANNEL_PREFIX)) {
                        jobName = channel.substring(CHANNEL_PREFIX.length());
                    } else { // Keyspace notification
                        jobName = channel.substring(KEYSPACE_CHANNEL_PREFIX.length() + LOCK_PREFIX.length());
                    }
                    CountDownLatch latch = lockWaiters.remove(jobName);
                    if (latch != null) {
                        latch.countDown();
                    }
                }
            }
        });
    }

    private void enableKeyspaceNotifications() {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisCommands<String, String> commands = connection.sync();
            commands.configSet("notify-keyspace-events", "Ex"); // Enable keyspace events for expired keys
        }
    }

    public boolean acquireAndExecute(String jobName, Runnable task) {
        String lockKey = LOCK_PREFIX + jobName;
        String lockValue = UUID.randomUUID().toString();
        String channelName = CHANNEL_PREFIX + jobName;

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisCommands<String, String> commands = connection.sync();
            RedisPubSubCommands<String, String> pubSubCommands = pubSubConnection.sync();

            pubSubCommands.subscribe(channelName);
            pubSubCommands.subscribe(KEYSPACE_CHANNEL_PREFIX + lockKey);

            try {
                long startTime = System.currentTimeMillis();
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
                            releaseLock(commands, lockKey, lockValue, channelName);
                        }
                    }

                    CountDownLatch latch = new CountDownLatch(1);
                    lockWaiters.put(jobName, latch);
                    latch.await(LOCK_ATTEMPT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                }
                return false;
            } finally {
                pubSubCommands.unsubscribe(channelName);
                pubSubCommands.unsubscribe(KEYSPACE_CHANNEL_PREFIX + lockKey);
            }
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

    private void releaseLock(RedisCommands<String, String> commands, String lockKey, String lockValue, String channelName) {
        String script =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "   redis.call('del', KEYS[1]) " +
                "   redis.call('publish', KEYS[2], 'RELEASED') " +
                "   return 1 " +
                "else " +
                "   return 0 " +
                "end";
        commands.eval(script, io.lettuce.core.ScriptOutputType.INTEGER,
            new String[]{lockKey, channelName}, lockValue);
    }

    public void close() {
        pubSubConnection.close();
        scheduler.shutdownNow();
    }
}
