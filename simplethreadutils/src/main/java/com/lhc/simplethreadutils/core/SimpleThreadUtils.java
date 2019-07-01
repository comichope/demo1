package com.lhc.simplethreadutils.core;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleThreadUtils {
    private static ThreadPoolExecutor pool;
    private static int size = 10;
    private static int maxSize = 20;
    private static long keepAliveTime = 2;
    private static TimeUnit timeUnit = TimeUnit.MINUTES;
    private static BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(Integer.MAX_VALUE);
    private static Map<Class<?>, BlockingQueue<Runnable>> runnableMap = new ConcurrentHashMap<>();
    private static Map<Class<?>, BlockingQueue<Callable>> callableMap = new ConcurrentHashMap<>();
    private final static AtomicBoolean doing = new AtomicBoolean(false);

    private static void initPool() {
        if (pool == null || pool.isShutdown()) {
            pool = getInitPool(size, maxSize, keepAliveTime, timeUnit);
            new Thread(SimpleThreadUtils::doWork).start();
        }
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    private static ThreadPoolExecutor getInitPool(int s, int ms, long kat, TimeUnit tu) {
        return new ThreadPoolExecutor(s, ms, kat, tu, blockingQueue, new SimpleThreadFactory(), new SimpleRejectExecutionHandler());
    }

    private static void initPool(int s, int ms, long kat, TimeUnit tu) {
        initParam(s, ms, kat, tu);
        initPool();
    }

    private static void initParam(int s, int ms, long kat, TimeUnit tu) {
        size = s;
        maxSize = ms;
        keepAliveTime = kat;
        timeUnit = tu;
    }

    public static void reInitPool(int s, int ms, long kat, TimeUnit tu) {
        if (pool != null) {
            ExecutorService tmp = pool;
            pool = getInitPool(s, ms, kat, tu);
            tmp.shutdown();
        } else {
            initPool(s, ms, kat, tu);
        }
    }

    static void submit(@NotNull SimpleRunnable r) {
        initPool();
        runnableMap.putIfAbsent(r.getClass(), new LinkedBlockingQueue<>());
        runnableMap.get(r.getClass()).add(r);
    }

    static void submit(@NotNull SimpleCallable r) {
        initPool();
        callableMap.putIfAbsent(r.getClass(), new LinkedBlockingQueue<>());
        callableMap.get(r.getClass()).add(r);
    }

    private static void startWork() {
        doing.set(true);
    }

    private static void doWork() {
        while (pool != null) {
            for (Map.Entry<Class<?>, BlockingQueue<Runnable>> entry : runnableMap.entrySet()) {
                BlockingQueue<Runnable> queue = entry.getValue();
                for (Runnable runnable : queue) {
                    pool.execute(runnable);
                    queue.remove(runnable);
                }
            }
            for (Map.Entry<Class<?>, BlockingQueue<Callable>> entry : callableMap.entrySet()) {
                BlockingQueue<Callable> queue = entry.getValue();
                for (Callable callable : queue) {
                    pool.submit(callable);
                    queue.remove(callable);
                }
            }
            System.out.println(pool.getQueue().size());
        }
    }


}
