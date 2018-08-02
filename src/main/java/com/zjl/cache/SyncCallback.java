package com.zjl.cache;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Aron on 18-5-4.
 * <p>
 * Description: 对缓存类回调的补充,回调到调用者线程
 */

public class SyncCallback<T> {
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
