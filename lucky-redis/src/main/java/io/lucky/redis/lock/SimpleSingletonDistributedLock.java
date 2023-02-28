package io.lucky.redis.lock;

import io.lucky.redis.domain.LockParam;
import io.lucky.redis.exception.RedisException;
import io.lucky.utils.JVMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 单机模式下实现分布式锁
 */
@Deprecated
public class SimpleSingletonDistributedLock implements DistributedLock {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSingletonDistributedLock.class);

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

    /**
     * 该组脚本对于可重入性的支持不如hash,故废弃
     */
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

            String clientId = this.getClientId();

            logger.debug("客户端[{}]-开始获取锁[{}],锁参数:{}", clientId, lockParam.getKey(), lockParam.toString());

            // 将加锁脚本缓存到redis服务器
            LUA_SHA_LOCK.compareAndSet(null, redisLettuceClient.scriptLoad(LOCK_SCRIPT.getBytes(StandardCharsets.UTF_8)));

            // 执行加锁脚本
            boolean lockResult = executeLockScript(lockParam.getKey(),clientId, lockParam.getExpirationTime());

            logger.info("客户端[{}]-获取锁[{}]结果:{}", clientId, lockParam.getKey(), lockResult);

            if (!lockResult && !lockParam.getSpin()) {
                logger.warn("客户端[{}]-获取锁[{}]失败且未自旋!", clientId, lockParam.getKey());
                return false;
            }
            if (!lockResult && lockParam.getSpin()) {
                SPIN_NUMBER.set(0);
                while (true && SPIN_NUMBER.get() < lockParam.getSpinNumber()) {
                    try {
                        Thread.sleep(lockParam.getSpinAwaitTime() * 1000);
                    } catch (Exception e) {
                        logger.error("客户端[{}]-获取锁[{}]失败,自旋过程中等待自旋时间异常:{}", clientId, lockParam.getKey(), e);
                        // 调用remove()方法避免内存泄漏
                        SPIN_NUMBER.remove();
                        return false;
                    }
                    lockResult = executeLockScript(lockParam.getKey(), clientId, lockParam.getExpirationTime());
                    if (lockResult) {
                        // 调用remove()方法避免内存泄漏
                        SPIN_NUMBER.remove();
                        logger.info("客户端[{}]-通过自旋成功获取到锁[{}]!", clientId, lockParam.getKey());
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
            String clientId = this.getClientId();

            logger.debug("客户端[{}]-释放锁[{}]", clientId, key);

            // 将释放锁脚本缓存到redis服务器
            LUA_SHA_UNLOCK.compareAndSet(null, redisLettuceClient.scriptLoad(UNLOCK_SCRIPT.getBytes(StandardCharsets.UTF_8)));

            // 执行释放锁脚本
            return executeUnLockScript(key, clientId);
        } catch (Exception e) {
            throw new RedisException(e.getMessage());
        }
    }

    @Deprecated
    private Boolean executeLockScript(String key, String value, Integer expirationTime) {
        return this.redisLettuceClient.evalSha(LUA_SHA_LOCK.get(), ReturnType.BOOLEAN, 1,
                key.getBytes(StandardCharsets.UTF_8),
                String.valueOf(expirationTime * 1000).getBytes(StandardCharsets.UTF_8),
                value.getBytes(StandardCharsets.UTF_8));
    }

    @Deprecated
    private Boolean executeUnLockScript(String key, String value) {
        return this.redisLettuceClient.evalSha(LUA_SHA_UNLOCK.get(), ReturnType.BOOLEAN, 1,
                key.getBytes(StandardCharsets.UTF_8),
                value.getBytes(StandardCharsets.UTF_8));
    }

    private String getClientId() throws UnknownHostException {
        String jvmPid = JVMUtil.getJVMPid();
        String threadId = String.valueOf(Thread.currentThread().getId());
        InetAddress addr = InetAddress.getLocalHost();
        String hostAddress = addr.getHostAddress();
        String hostname = addr.getHostName();
        return hostAddress + "-pid:" + jvmPid + "-threadId:" + threadId;
    }
}
