package com.zjl.cache;

import android.text.TextUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 扫描缓存目录，当缓存超过上限时，从lastModify最小的开始删除，直到缓存文件低于大小限制
 * Created by zhangjianliang on 2018/3/20
 */

abstract class AutoClearController {

    public static final long MAX_SIZE = 50 * 1024 * 1024;//默认一个文件夹下缓存上限为50M

    private final Map<File, Long> mLastUseDateMap = Collections.synchronizedMap(new HashMap<File, Long>());

    private AtomicLong mCacheSize;

    private long mMaxSize;

    private boolean mAutoClear;

    protected File mCacheDir;

    public void setAutoClearEnable(boolean enable) {
        this.mAutoClear = enable;
    }

    private AutoClearController() {}

    public AutoClearController(String cacheDir, long maxSize) {
        if (!mAutoClear) {
            return;
        }
        this.mMaxSize = maxSize;
        mCacheSize = new AtomicLong();
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        if (TextUtils.isEmpty(cacheDir)) {
            throw new IllegalArgumentException("directory is null");
        }
        mCacheDir = new File(cacheDir);
        if (!mCacheDir.exists() || !mCacheDir.isDirectory()) {
            mCacheDir.mkdirs();
        }
        computeSizeAndCount();
    }

    /**
     * 计算 cacheSize和cacheCount
     */
    private synchronized void computeSizeAndCount() {
        ThreadPoolFactory.instance().fixExecutor(new Runnable() {
            @Override
            public void run() {
                File[] cacheFiles = mCacheDir.listFiles();
                if (cacheFiles == null || cacheFiles.length == 0) {
                    return;
                }
                int size = 0;
                for (File file : cacheFiles) {
                    if (file == null || !file.exists()) {
                        continue;
                    }
                    if (file.isDirectory()) {//文件夹就干掉好了
                        AutoClearController.this.deleteFile(file);
                        continue;
                    }
                    size += AutoClearController.this.getFileSize(file);
                    mLastUseDateMap.put(file, file.lastModified());
                }
                mCacheSize.set(size);
            }
        });
    }

    public synchronized void put(File file) {
        if (!mAutoClear) {
            return;
        }
        long fileSize = getFileSize(file);
        if (fileSize > mMaxSize) {
            return;
        }
        long cacheSize = getCacheSize();
        while (cacheSize + fileSize > mMaxSize) {
            long freedSize = removeOldestFile();
            cacheSize = addCacheSize(-freedSize);
        }
        addCacheSize(fileSize);
        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        mLastUseDateMap.put(file, currentTime);
    }

    public synchronized void get(File file) {
        if (!mAutoClear) {
            return;
        }
        if (file == null || !file.exists()) {
            return;
        }
        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        mLastUseDateMap.put(file, currentTime);
    }

    public synchronized boolean remove(File file) {
        if (!mAutoClear) {
            return false;
        }
        if (file == null || !file.exists()) {
            return true;//文件不存在，就当是删除成功好了
        }
        Iterator<Map.Entry<File,Long>> iterator = mLastUseDateMap.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<File,Long> entry = iterator.next();
            if (entry != null && entry.getKey() == file) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public synchronized void clear() {
        if (!mAutoClear) {
            return;
        }
        mLastUseDateMap.clear();
        mCacheSize.set(0);
    }

    /**
     * 移除旧的文件
     */
    private long removeOldestFile() {
        if (mLastUseDateMap.isEmpty()) {
            return 0;
        }
        Long oldestMills = null;
        File oldestFile = null;
        Iterator<Map.Entry<File, Long>> iterator = mLastUseDateMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<File, Long> entry = iterator.next();
            File file = entry.getKey();
            if (file == null) {
                continue;
            }
            if (oldestFile == null) {
                oldestFile = entry.getKey();
                oldestMills = entry.getValue();
            } else {
                Long lastValueUsage = entry.getValue();
                if (lastValueUsage < oldestMills) {
                    oldestMills = lastValueUsage;
                    oldestFile = entry.getKey();
                }
            }
        }
        if (oldestFile == null || !oldestFile.exists()) {
            return 0;
        }
        if (deleteFile(oldestFile)) {
            mLastUseDateMap.remove(oldestFile);
        }
        return getFileSize(oldestFile);
    }

    private long getFileSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.length();
    }

    /**
     * 删除文件的具体操作抛出去，使得自动清理与文件读写管理使用同一个同步的删除方法，避免并发问题
     * @param file
     * @return
     */
    abstract boolean deleteFile(File file);

    private long addCacheSize(long size) {
        mCacheSize.addAndGet(size);
        if (mCacheSize.get() < 0) {
            mCacheSize.set(0);
        }
        return mCacheSize.get();
    }

    private long getCacheSize() {
        long size = mCacheSize.get();
        return size >= 0 ? size : 0;
    }
}
