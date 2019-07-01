package com.lhc.simplethreadutils.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleThreadFactory implements ThreadFactory {
    private final AtomicLong threadNum = new AtomicLong(1);

    @Override
    public Thread newThread(Runnable r) {
        String name = "thread-" + r.getClass() + "-" + this.threadNum.getAndIncrement();
        Thread t = new Thread(r, name);
        System.out.println(name + " is created");
        return t;
    }
}
