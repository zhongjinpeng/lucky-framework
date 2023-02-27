package io.lucky.redis.lock;

import io.lucky.redis.domain.LockParam;
import io.lucky.redis.exception.RedisException;

public interface DistributedLock {

    Boolean lock(LockParam lockParam) throws RedisException;

    Boolean unLock(String key) throws RedisException;

}
