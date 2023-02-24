package io.lucky.redis.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 单机模式下实现分布式锁
 */
public class SingletonDistributedLock implements DistributedLock{


    @Override
    public Boolean lock(String key, String clientId, String expirationTime) {
        return null;
    }

}
