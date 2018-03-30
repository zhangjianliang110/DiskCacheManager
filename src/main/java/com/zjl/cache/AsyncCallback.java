package com.zjl.cache;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by zhangjianliang on 2018/3/19
 */

public class AsyncCallback<T> {

    public void onResult(byte[] bytes) {
    }

    public void onResult(T object) {

    }

    public void onError(String errMsg) {

    }

    public Type getType() {
        Type superClass = getClass().getGenericSuperclass();
        return  ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }
}
