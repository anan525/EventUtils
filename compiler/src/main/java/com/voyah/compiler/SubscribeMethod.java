package com.voyah.compiler;

import java.lang.reflect.Method;

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
public class SubscribeMethod {

    private ThreadMode threadMode;

    private int priority;

    private boolean isSticky;

    private Method method;

    public Method getMethod() {
        return method;
    }

    private Class eventType;

    public Class getEventType() {
        return eventType;
    }

    public SubscribeMethod setEventType(Class eventType) {
        this.eventType = eventType;
        return this;
    }

    public SubscribeMethod setMethod(Class clazz, String method, Class<?>... var2) {
        try {
            this.method = clazz.getDeclaredMethod(method, var2);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return this;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public SubscribeMethod setThreadMode(ThreadMode threadMode) {
        this.threadMode = threadMode;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public SubscribeMethod setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public SubscribeMethod setSticky(boolean sticky) {
        isSticky = sticky;
        return this;
    }
}
