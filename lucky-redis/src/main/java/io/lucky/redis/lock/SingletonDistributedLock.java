package io.lucky.redis.lock;

import io.lucky.redis.domain.LockParam;
import io.lucky.redis.exception.RedisException;
import io.lucky.utils.JVMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 单机模式下实现分布式锁
 */
@Configuration
@ConditionalOnExpression("T(org.apache.commons.lang3.StringUtils).isNotEmpty('${spring.redis.host}')")
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

    /**
     * 首先通过exists命令判断当前这个锁是否存在
     * 如果锁不存在的话，直接使用hincrby创建一个键为 lock hash 表，并且为 Hash 表中键为 uuid 初始化为 0，然后再次加 1，最后再设置过期时间
     * 如果当前锁存在，则使用 hexists判断当前 lock 对应的 hash 表中是否存在 uuid 这个键，如果存在,再次使用 hincrby 加 1，最后再次设置过期时间
     */
    private static final String LOCK_SCRIPT =
            "if (redis.call('exists', KEYS[1]) == 0) then\n" +
                    "    redis.call('hincrby', KEYS[1], ARGV[2], 1);\n" +
                    "    redis.call('pexpire', KEYS[1], ARGV[1]);\n" +
                    "    return 1;\n" +
                    "end ;\n" +
                    "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then\n" +
                    "    redis.call('hincrby', KEYS[1], ARGV[2], 1);\n" +
                    "    redis.call('pexpire', KEYS[1], ARGV[1]);\n" +
                    "    return 1;\n" +
                    "end ;\n" +
                    "return 0;";

    /**
     * 首先通过hexists 判断 Redis Hash 表是否存给定的域
     * 如果 lock 对应 Hash 表不存在，或者 Hash 表不存在 uuid 这个 key，直接返回 nil
     * 若存在的情况下，代表当前锁被其持有，首先使用 hincrby使可重入次数减 1 ，然后判断计算之后可重入次数，若小于等于 0，则使用 del 删除这把锁
     */
    private static final String UNLOCK_SCRIPT =
            "if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then\n" +
                    "    return nil;\n" +
                    "end ;\n" +
                    "local counter = redis.call('hincrby', KEYS[1], ARGV[1], -1);\n" +
                    "if (counter > 0) then\n" +
                    "    return 0;\n" +
                    "else\n" +
                    "    redis.call('del', KEYS[1]);\n" +
                    "    return 1;\n" +
                    "end ;\n" +
                    "return nil;";

    @Override
    public Boolean lock(LockParam lockParam) throws RedisException {
        try {
            String clientId = this.getClientId();
            logger.debug("客户端[{}]-开始获取锁[{}],锁参数:{}", clientId, lockParam.getKey(), lockParam.toString());

            // 将加锁脚本缓存到redis服务器
            LUA_SHA_LOCK.compareAndSet(null, redisLettuceClient.scriptLoad(LOCK_SCRIPT.getBytes(StandardCharsets.UTF_8)));

            // 执行加锁脚本
            boolean lockResult = executeLockScript(lockParam.getKey(), clientId, lockParam.getExpirationTime());

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

    private Boolean executeLockScript(String key, String uuid, Integer expirationTime) {
        return this.redisLettuceClient.evalSha(LUA_SHA_LOCK.get(), ReturnType.BOOLEAN, 1,
                key.getBytes(StandardCharsets.UTF_8),
                String.valueOf(expirationTime * 1000).getBytes(StandardCharsets.UTF_8),
                uuid.getBytes(StandardCharsets.UTF_8));
    }

    private Boolean executeUnLockScript(String key, String uuid) {
        return this.redisLettuceClient.evalSha(LUA_SHA_UNLOCK.get(), ReturnType.BOOLEAN, 1,
                key.getBytes(StandardCharsets.UTF_8),
                uuid.getBytes(StandardCharsets.UTF_8));
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
