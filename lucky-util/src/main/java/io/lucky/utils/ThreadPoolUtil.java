package io.lucky.utils;

import java.util.concurrent.*;

/**
 * @className ThreadPoolUtils
 * @description
 * @author zhongjinpeng
 * @date 2021/8/12
 * @version 1.0
 **/
public class ThreadPoolUtil {

    private static volatile ThreadPoolExecutor threadPoolExecutor;

    private static final int BLOCK_QUEUE_SIZE = 1000;

    /**
     * 核心线程数 Runtime.getRuntime().availableProcessors() + 1
     */
    private static final int CORE_POOL_SIZE = 20;
    /**
     * 最大线程数 Runtime.getRuntime().availableProcessors() * 2
     */
    private static final int MAX_POOL_SIZE = 40;
    private static final int KEEP_ALIVE_TIME = 5;
    private static final TimeUnit timeUnit = TimeUnit.SECONDS;
    /**
     * 任务队列
     */
    private static final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(BLOCK_QUEUE_SIZE);

    /**
     * 拒绝策略
     */
    private static final RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

    /**
     * util类均为静态方法,不应该初始化这个类所以需要构造一个private构造函数
     */
    private ThreadPoolUtil() {
    }

    public static void executor(Runnable runnable) {
        getInstance().execute(runnable);
    }

    public static <T> Future<T> submit(Callable<T> callable) {
        return getInstance().submit(callable);
    }

    public static void shutdown(){
        getInstance().shutdown();
    }

    /**
     * 获取线程池对象
     *
     * @return
     */
    private static ThreadPoolExecutor getInstance() {
        if(threadPoolExecutor == null){
            synchronized (ThreadPoolUtil.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                            MAX_POOL_SIZE,
                            KEEP_ALIVE_TIME,
                            timeUnit,
                            workQueue,
                            handler);
                }
            }
        }
        return threadPoolExecutor;
    }
}
