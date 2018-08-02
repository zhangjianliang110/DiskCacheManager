package com.zjl.cache;

import android.content.Context;

import java.io.File;

/**
 * 1、记录文件大小，达到上限时删除最老的文件
 * 2、文件读写
 * Created by zhangjianliang on 2018/3/19
 */
public class DiskCache {

    private static final String TAG = "DiskCache";

    private static volatile DiskCache mDiskCache;

    private AutoClearController mAutoClearController;

    private AsyncFileCache mAsyncFileCache;

    private DiskCache() {
    }

    private DiskCache(Context context, String cacheDir, long maxSize) {
        mAsyncFileCache = new AsyncFileCache(context, cacheDir);
        mAutoClearController = new AutoClearController(cacheDir, maxSize) {
            @Override
            boolean deleteFile(File file) {
                return mAsyncFileCache != null && mAsyncFileCache.deleteFile(file);
            }
        };
        mAsyncFileCache.setAutoClearController(mAutoClearController);
    }

    public static DiskCache getInstance(Context context, String cacheDir, long maxSize) {
        if (mDiskCache == null) {
            synchronized (DiskCache.class) {
                if (mDiskCache == null) {
                    mDiskCache = new DiskCache(context, cacheDir, maxSize);
                }
            }
        }
        return mDiskCache;
    }

    public void setAutoClearEnable(boolean enable) {
        if (mAutoClearController != null) {
            mAutoClearController.setAutoClearEnable(enable);
        }
    }

    public void put(String key, byte[] value) {
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncPut(key, value);
        }
    }

    public void putObject(String key, Object value) {
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncPutObject(key, value);
        }
    }

    public void get(String key, AsyncCallback callback) {
        if (callback == null) {
            return;
        }
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncGet(key, callback);
        }
    }

    public void getObject(String key, AsyncCallback callback) {
        if (callback == null) {
            return;
        }
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncGetObject(key, callback);
        }
    }

    public void getObject(String key, SyncCallback callback) {
        if (callback == null) {
            return;
        }
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncGetObject(key, callback);
        }
    }

    public void remove(String key) {
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncRemove(key);
        }
    }

    public void clear() {
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncClear();
        }
    }
}