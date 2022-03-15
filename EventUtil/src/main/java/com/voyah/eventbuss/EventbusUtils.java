package com.voyah.eventbuss;

import android.os.Handler;
import android.os.Looper;

import com.voyah.compiler.EventConstance;
import com.voyah.compiler.Scription;
import com.voyah.compiler.SubscribeMethod;
import com.voyah.compiler.SubscriptionIndexesInterfaces;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 * Copyright (c) 2021-.
 * All Rights Reserved by Software.
 * --
 * You may not use, copy, distribute, modify, transmit in any form this file.
 * except in compliance with szLanyou in writing by applicable law.
 * --
 * brief   brief function description.
 * 主要功能.
 * --
 * date last_modified_date.
 * 时间.
 * --
 * version 1.0.
 * 版本信息。
 * --
 * details detailed function description
 * 功能描述。
 * --
 * DESCRIPTION.
 * Create it.
 * --
 * Edit History.
 * DATE.
 * 2022/3/11.
 * --
 * NAME.
 * anyq.
 * --
 */
public class EventbusUtils {

    private static ArrayList<Class> subscirbes = new ArrayList<>();
    private static CopyOnWriteArrayList<Scription> normalCache = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<Scription> stickyCache = new CopyOnWriteArrayList<>();
    private volatile static EventbusUtils instance;
    private final ExecutorService executorService;
    private final Handler handler;

    public static EventbusUtils getDefault() {
        if (instance == null) {
            instance = new EventbusUtils();
        }
        return instance;
    }


    public EventbusUtils() {
        executorService = Executors.newCachedThreadPool();
        handler = new Handler(Looper.getMainLooper());
    }

    public void register(Object subscribe) {
        Class<?> aClass = subscribe.getClass();
        String name = aClass.getPackage().getName();
        try {
            SubscriptionIndexesInterfaces indexesInterfaces = (SubscriptionIndexesInterfaces) Class.forName(name + "." + EventConstance.className).newInstance();
            //订阅
            subscribe(indexesInterfaces, aClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void subscribe(SubscriptionIndexesInterfaces indexesInterfaces, Class<?> aClass) {
        try {
            if (!subscirbes.contains(aClass)) {
                subscirbes.add(aClass);
            }
            CopyOnWriteArrayList<Scription> copyOnWriteArrayList = indexesInterfaces.loadSubscription();
            if (copyOnWriteArrayList != null && copyOnWriteArrayList.size() > 0) {
                for (Scription scription : copyOnWriteArrayList) {
                    Class clazz = scription.getClazz();
                    if (subscirbes.contains(clazz)) {
                        if (scription.getSubscribeMethod().isSticky()) {
                            addToStickyCache(scription);
                        } else {
                            addToNormalCache(scription);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addToNormalCache(Scription scription) {
        if (!normalCache.contains(scription)) {
            normalCache.add(scription);
        }
    }

    private void addToStickyCache(Scription scription) {
        if (!stickyCache.contains(scription)) {
            stickyCache.add(scription);
        }
    }


    public void unRegister(Object subscribe) {
        Class<?> aClass = subscribe.getClass();
        if (subscirbes.contains(aClass)) {
            subscirbes.remove(aClass);
        }

        unSubScribe(aClass);
    }

    private void unSubScribe(Class<?> aClass) {

        normalCache.removeIf(scription -> scription.getClazz() == aClass);

        stickyCache.removeIf(scription -> scription.getClazz() == aClass);
    }

    public void postSticky(Object msg) {

    }


    public void post(Object msg) {
        filterWithType(msg, msg.getClass());
    }

    private void filterWithType(Object msg, Class<?> aClass) {
        for (Scription scription : normalCache) {
            Class clazz = scription.getClazz();
            if (subscirbes.contains(clazz) && aClass == clazz) {
                postToScription(scription, msg);
            }
        }
    }

    private void postToScription(Scription scription, Object msg) {
        Looper looper = Looper.myLooper();//当前线程
        SubscribeMethod subscribeMethod = scription.getSubscribeMethod();
        Method method = subscribeMethod.getMethod();
        Class clazz = scription.getClazz();

        switch (scription.getSubscribeMethod().getThreadMode()) {
            case AYSNC:
                if (looper == Looper.getMainLooper()) {
                    //当前是主线程--->子线程
                    executorService.execute(() -> {
                        invokeMethod(clazz, method, msg);
                    });
                } else {
                    // 子线程--->子线程
                    invokeMethod(clazz, method, msg);
                }
                break;
            case MAINTHREAD:
                if (looper == Looper.getMainLooper()) {
                    //主线程--->主线程
                    invokeMethod(clazz, method, msg);
                } else {
                    //子线程--->主线程
                    handler.post(() -> {
                        invokeMethod(clazz, method, msg);
                    });
                }
                break;
        }
    }

    private void invokeMethod(Class clazz, Method method, Object msg) {
        try {
            method.invoke(clazz, msg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
