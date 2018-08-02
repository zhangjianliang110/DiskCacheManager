package com.zjl.cache;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件缓存管理
 * Created by zhangjianliang on 2018/3/20
 */

public class CacheManager {

    private static volatile CacheManager mCacheManager;
    //每个文件夹下对应一个单例管理类，统一文件操作入口，避免多个入口导致并发问题
    private static Map<String, DiskCache> mControllMap = new HashMap<>();

    private Context mContext;

    private CacheManager() {}

    private CacheManager(Context context) {
        mContext = context;
    }

    public static CacheManager getInstance(Context context) {
        if (mCacheManager == null) {
            synchronized (CacheManager.class) {
                if (mCacheManager == null) {
                    mCacheManager = new CacheManager(context);
                }
            }
        }
        return mCacheManager;
    }

    public DiskCache getDiskCache(String cacheDir, long maxSize) {
        if (TextUtils.isEmpty(cacheDir)) {
            throw new RuntimeException("cacheDir is null");
        }
        if (maxSize == 0) {
            throw new RuntimeException("max size must > 0");
        }
        String key = cacheDir + myPid();
        DiskCache controll = mControllMap.get(key);
        if (controll == null) {
            controll = DiskCache.getInstance(mContext, cacheDir, maxSize);
            mControllMap.put(key, controll);
        }
        return controll;
    }

    public DiskCache getDiskCache() {
        String key = FileCache.FILE_DIR + myPid();
        DiskCache controll = mControllMap.get(key);
        if (controll == null) {
            controll = DiskCache.getInstance(mContext, FileCache.FILE_DIR, AutoClearController.MAX_SIZE);
            mControllMap.put(key, controll);
        }
        return controll;
    }

    private String myPid() {
        return "_" + android.os.Process.myPid();
    }
}