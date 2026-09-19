package com.forgex.job.core.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Job 分布式锁服务。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Service
public class JobLockService {

    private final RedissonClient redissonClient;

    public JobLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    public boolean tryLock(RLock lock, long waitMs, long leaseSeconds) throws InterruptedException {
        return lock.tryLock(waitMs, leaseSeconds * 1000, TimeUnit.MILLISECONDS);
    }

    public void unlockQuietly(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
