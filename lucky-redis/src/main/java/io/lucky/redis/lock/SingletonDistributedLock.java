package io.lucky.redis.lock;

import io.lucky.redis.domain.LockParam;
import io.lucky.redis.exception.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 单机模式下实现分布式锁
 */
@Service
public class SingletonDistributedLock implements DistributedLock {

    private static final Logger logger = LoggerFactory.getLogger(SingletonDistributedLock.class);

    @Autowired
    @Qualifier("redisLettuceClient")
    private RedisConnection redisLettuceClient;

    /**
     * 两个原子变量，用于存储加锁和解锁脚本的sha ID
     */
    private final AtomicReference<String> LUA_SHA_LOCK = new AtomicReference<>();
    private final AtomicReference<String> LUA_SHA_UNLOCK = new AtomicReference<>();

    /**
     * 保存每个线程自旋次数
     */
    private static ThreadLocal<Integer> SPIN_NUMBER = new ThreadLocal<Integer>();

    private static final String LOCK_SCRIPT = "local result = redis.call('setnx', KEYS[1], ARGV[2])\n" +
            "if result == 1 then\n" +
            "return redis.call('pexpire', KEYS[1], tonumber(ARGV[1]))\n" +
            "else\n" +
            "return 0\n" +
            "end";
    private static final String UNLOCK_SCRIPT = "local result = redis.call('get', KEYS[1])\n" +
            "if result == ARGV[1] then\n" +
            "redis.call('del', KEYS[1])\n" +
            "return 1\n" +
            "else\n" +
            "return 0\n" +
            "end";

    @Override
    public Boolean lock(LockParam lockParam) throws RedisException {
        try {
            String threadId = String.valueOf(Thread.currentThread().getId());

            logger.debug("线程[{}]开始获取锁[{}],锁参数:{}", threadId, lockParam.getKey(), lockParam.toString());

            // 重构value值以便于实现谁加锁谁解锁以及可重入性
            lockParam.setValue(threadId);

            // 将加锁脚本缓存到redis服务器
            LUA_SHA_LOCK.compareAndSet(null, redisLettuceClient.scriptLoad(LOCK_SCRIPT.getBytes(StandardCharsets.UTF_8)));

            // 执行加锁脚本
            boolean lockResult = executeLockScript(lockParam.getKey(), lockParam.getValue(), lockParam.getExpirationTime());

            logger.info("线程[{}]获取锁[{}]结果:{}", threadId, lockParam.getKey(), lockResult);

            if (!lockResult && !lockParam.getSpin()) {
                logger.warn("线程[{}]获取锁[{}]失败且未自旋!", threadId, lockParam.getKey());
                return false;
            }
            if (!lockResult && lockParam.getSpin()) {
                SPIN_NUMBER.set(0);
                while (true && SPIN_NUMBER.get() < lockParam.getSpinNumber()) {
                    try {
                        Thread.sleep(lockParam.getSpinAwaitTime() * 1000);
                    } catch (Exception e) {
                        logger.error("线程[{}]获取锁[{}]失败,自旋过程中等待自旋时间异常:{}", threadId, lockParam.getKey(), e);
                        // 调用remove()方法避免内存泄漏
                        SPIN_NUMBER.remove();
                        return false;
                    }
                    lockResult = executeLockScript(lockParam.getKey(), lockParam.getValue(), lockParam.getExpirationTime());
                    if (lockResult) {
                        // 调用remove()方法避免内存泄漏
                        SPIN_NUMBER.remove();
                        logger.info("线程[{}]通过自旋成功获取到锁[{}]!", threadId, lockParam.getKey());
                        return true;
                    }
                    SPIN_NUMBER.set(SPIN_NUMBER.get() + 1);
                }
            }
            // 调用remove()方法避免内存泄漏
            SPIN_NUMBER.remove();
            return lockResult;
        } catch (Exception e) {
            throw new RedisException(e.getMessage());
        }
    }

    @Override
    public Boolean unLock(String key) throws RedisException {
        try {
            String threadId = String.valueOf(Thread.currentThread().getId());

            logger.debug("线程[{}]释放锁[{}]", threadId, key);

            // 将释放锁脚本缓存到redis服务器
            LUA_SHA_UNLOCK.compareAndSet(null, redisLettuceClient.scriptLoad(UNLOCK_SCRIPT.getBytes(StandardCharsets.UTF_8)));

            // 执行释放锁脚本
            return executeUnLockScript(key, threadId);
        }catch (Exception e){
            throw new RedisException(e.getMessage());
        }
    }

    private Boolean executeLockScript(String key, String value, Integer expirationTime) {
        return this.redisLettuceClient.evalSha(LUA_SHA_LOCK.get(), ReturnType.BOOLEAN, 1,
                key.getBytes(StandardCharsets.UTF_8),
                String.valueOf(expirationTime * 1000).getBytes(StandardCharsets.UTF_8),
                value.getBytes(StandardCharsets.UTF_8));
    }

    private Boolean executeUnLockScript(String key, String value) {
        return this.redisLettuceClient.evalSha(LUA_SHA_UNLOCK.get(), ReturnType.BOOLEAN, 1,
                key.getBytes(StandardCharsets.UTF_8),
                value.getBytes(StandardCharsets.UTF_8));
    }
}
