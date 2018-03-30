package com.zjl.cache;


import java.lang.reflect.Type;

import io.silvrr.installment.common.controller.ThreadPoolFactory;

/**
 * 异步写入、删除、清空
 * Created by zhangjianliang on 2018/3/20
 */
final class AsyncFileCache<T> extends FileCache {

    public AsyncFileCache(String dirPath) {
        super(dirPath);
    }

    public void asyncPut(final String key, final byte[] value) {
        if (value == null) {
            return;
        }
        ThreadPoolFactory.instance().fixExecutor(() -> put(key, value));
    }

    public void asyncRemove(final String key) {
        ThreadPoolFactory.instance().fixExecutor(() -> remove(key));
    }

    public void asyncClear() {
        ThreadPoolFactory.instance().fixExecutor(() -> clear());
    }

    public void asyncGet(String key, AsyncCallback<T> callback) {
        if (callback == null) {
            return;
        }
        ThreadPoolFactory.instance().fixExecutor(() -> {
            Type t = callback.getType();
            Object object = get(key);
            if (object == null || !(object.getClass().equals(t))) {
                callback.onError("no cache data");
            } else {
                try {
                    callback.onResult((T) object);
                }catch (Exception e) {
                    callback.onError("cache cast error");
                }
            }
        });
    }

    public void asyncPutObject(final String key, final Object value) {
        if (value == null) {
            return;
        }
        ThreadPoolFactory.instance().fixExecutor(() -> putObject(key, value));
    }

    public void asyncGetObject(final String key, AsyncCallback<T> callback) {
        if (callback == null) {
            return;
        }
        ThreadPoolFactory.instance().fixExecutor(() -> {
            Object object = getObject(key);
            if (object == null) {
                callback.onError("no cache data");
            } else {
                try {
                    callback.onResult((T) object);
                }catch (Exception e) {
                    callback.onError("cache cast error");
                }
            }
        });
    }
}