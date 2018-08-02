package com.zjl.cache;


import android.content.Context;
import android.os.Handler;

import java.lang.reflect.Type;

/**
 * 异步写入、删除、清空
 * Created by zhangjianliang on 2018/3/20
 */
final class AsyncFileCache<T> extends FileCache {

    public AsyncFileCache(Context context, String dirPath) {
        super(context, dirPath);
    }

    public void asyncPut(final String key, final byte[] value) {
        if (value == null) {
            return;
        }
        ThreadPoolFactory.instance().fixExecutor(new Runnable() {
            @Override
            public void run() {
                AsyncFileCache.this.put(key, value);
            }
        });
    }

    public void asyncRemove(final String key) {
        ThreadPoolFactory.instance().fixExecutor(new Runnable() {
            @Override
            public void run() {
                AsyncFileCache.this.remove(key);
            }
        });
    }

    public void asyncClear() {
        ThreadPoolFactory.instance().fixExecutor(new Runnable() {
            @Override
            public void run() {
                AsyncFileCache.this.clear();
            }
        });
    }

    public void asyncGet(final String key, final AsyncCallback<T> callback) {
        if (callback == null) {
            return;
        }
        ThreadPoolFactory.instance().fixExecutor(new Runnable() {
            @Override
            public void run() {
                Type t = callback.getType();
                Object object = AsyncFileCache.this.get(key);
                if (object == null || !(object.getClass().equals(t))) {
                    callback.onError("no cache data");
                } else {
                    try {
                        callback.onResult((T) object);
                    } catch (Exception e) {
                        callback.onError("cache cast error");
                    }
                }
            }
        });
    }

    public void asyncPutObject(final String key, final Object value) {
        if (value == null) {
            return;
        }
        ThreadPoolFactory.instance().fixExecutor(new Runnable() {
            @Override
            public void run() {
                AsyncFileCache.this.putObject(key, value);
            }
        });
    }

    public void asyncGetObject(final String key, final AsyncCallback<T> callback) {
        if (callback == null) {
            return;
        }
        ThreadPoolFactory.instance().fixExecutor(new Runnable() {
            @Override
            public void run() {
                Object object = AsyncFileCache.this.getObject(key);
                if (object == null) {
                    callback.onError("no cache data");
                } else {
                    try {
                        callback.onResult((T) object);
                    } catch (Exception e) {
                        callback.onError("cache cast error");
                    }
                }
            }
        });
    }

    /** 增加线程处理，读取缓存后在主线程回调 */
    public void asyncGetObject(final String key, final SyncCallback<T> callback) {
        if (callback == null) {
            return;
        }
        final Handler handler = new Handler();
        ThreadPoolFactory.instance().fixExecutor(new Runnable() {
            @Override
            public void run() {
                final Object object = AsyncFileCache.this.getObject(key);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (object == null) {
                            callback.onError("no cache data");
                        } else {
                            try {
                                callback.onResult((T) object);
                            } catch (Exception e) {
                                callback.onError("cache cast error");
                            }
                        }
                    }
                });

            }
        });
    }
}