package com.zjl.cache;

import java.io.File;

import io.silvrr.installment.common.utils.XLogUtil;

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

    private DiskCache(String cacheDir, long maxSize) {
        mAsyncFileCache = new AsyncFileCache(cacheDir);
        mAutoClearController = new AutoClearController(cacheDir, maxSize) {
            @Override
            boolean deleteFile(File file) {
                XLogUtil.d(TAG, "~~~~deleteFile " + file == null ? null : file.getAbsolutePath() + "~~~~");
                return mAsyncFileCache != null && mAsyncFileCache.deleteFile(file);
            }
        };
        mAsyncFileCache.setAutoClearController(mAutoClearController);
    }

    public static DiskCache getInstance(String cacheDir, long maxSize) {
        if (mDiskCache == null) {
            synchronized (DiskCache.class) {
                if (mDiskCache == null) {
                    mDiskCache = new DiskCache(cacheDir, maxSize);
                }
            }
        }
        return mDiskCache;
    }

    public void setAutoClearEnable(boolean enable) {
        if (mAutoClearController != null) {
            mAutoClearController.setAutoClearEnable(enable);
        }
        XLogUtil.d(TAG, "~~~~setAutoClearEnable: " + enable + "~~~~");
    }

    public void put(String key, byte[] value) {
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncPut(key, value);
        }
        XLogUtil.d(TAG, "~~~~put: " + key + "~~~~");
    }

    public void putObject(String key, Object value) {
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncPutObject(key, value);
        }
        XLogUtil.d(TAG,"~~~~putObject: " + key + "~~~~");
    }

    public void get(String key, AsyncCallback callback) {
        if (callback == null) {
            return;
        }
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncGet(key, callback);
        }
        XLogUtil.d(TAG, "~~~~get: " + key + "~~~~");
    }

    public void getObject(String key, AsyncCallback callback) {
        if (callback == null) {
            return;
        }
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncGetObject(key, callback);
        }
        XLogUtil.d(TAG,"~~~~getObject: " + key + "~~~~");
    }

    public void remove(String key) {
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncRemove(key);
        }
        XLogUtil.d(TAG,"~~~~remove: " + key + "~~~~");
    }

    public void clear() {
        if (mAsyncFileCache != null) {
            mAsyncFileCache.asyncClear();
        }
        XLogUtil.d(TAG,"~~~~clear~~~~");
    }
}