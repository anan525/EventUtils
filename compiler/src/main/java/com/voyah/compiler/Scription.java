package com.voyah.compiler;

import java.util.ArrayList;
import java.util.Objects;

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
public class Scription {

    private Class clazz;

    private SubscribeMethod subscribeMethod;

    public Class getClazz() {
        return clazz;
    }

    public Scription setClazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public SubscribeMethod getSubscribeMethod() {
        return subscribeMethod;
    }

    public Scription setSubscribeMethod(SubscribeMethod subscribeMethod) {
        this.subscribeMethod = subscribeMethod;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scription scription = (Scription) o;
        return Objects.equals(clazz, scription.clazz) &&
                Objects.equals(subscribeMethod, scription.subscribeMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, subscribeMethod);
    }
}
