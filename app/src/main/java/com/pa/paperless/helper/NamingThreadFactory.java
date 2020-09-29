package com.pa.paperless.helper;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author by xlk
 * @date 2020/7/25 17:47
 * @desc 线程工厂，它设置线程名称，有利于我们定位问题。
 */
public class NamingThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNum = new AtomicInteger();
    private final String name;

    /**
     * 创建一个带名字的线程池生产工厂
     */
    public NamingThreadFactory(String name) {
        this.name = name; // TODO consider uniquifying this
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r,name + " [#" + threadNum.incrementAndGet() + "]");
    }
}
