package io.lucky.redis.lock;

public interface DistributedLock {

    /**
     * 加锁
     *
     * @param key            锁名称
     * @param clientId       加锁客户端唯一标识
     * @param expirationTime 锁时效,单位:秒
     * @return
     */
    Boolean lock(String key, String clientId, String expirationTime);

}
