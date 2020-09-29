package com.pa.paperless.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by xlk on 2019/9/16.
 */
public class MyObservable<T> {
    // 存储观察者的链表
    private CopyOnWriteArrayList<MyObserver<T>> observers = new CopyOnWriteArrayList<MyObserver<T>>();

    // 注册观察者
    public void register(MyObserver<T> observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }
        synchronized (this) {
            if (!observers.contains(observer))
                observers.add(observer);
        }
    }

    // 注销观察者
    public void unregister(MyObserver<T> observer) {
        observers.remove(observer);
    }

    // 通知观察者
    public void notifyObserver(T data) {
        for (MyObserver<T> observer : observers) {
            observer.update(data);
        }
    }

    public void notifyObserver(List<T> datas){
        for (MyObserver<T> observer : observers) {
            observer.update(datas);
        }
    }

    /**
     * 观察者
     *
     * @param <T>
     * @author Administrator
     */
    public interface MyObserver<T> {
        void update(T data);
        void update(List<T> datas);
    }
}
