package com.zjl.cache;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.silvrr.installment.MyApplication;
import io.silvrr.installment.common.utils.XLogUtil;

/**
 * 文件读写
 * Created by zhangjianliang on 2018/3/19
 */
class FileCache {

    private static final String TAG = "FileCache";

    public static final String FILE_DIR = "FileCache";

    private File mDirFile;

    private DiskCacheWriteLocker mWriteLocker = new DiskCacheWriteLocker();

    private AutoClearController mAutoClearController;

    private final SafeKeyCreator mKeyCreator;

    public void setAutoClearController(AutoClearController controller) {
        mAutoClearController = controller;
    }

    public FileCache(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            dirPath = FILE_DIR;
        }
        mDirFile = getFilesDir(dirPath);
        mKeyCreator = new SafeKeyCreator();
    }

    /**
     * 获取默认文件存储路径
     */
    private File getFilesDir(String childDir) {
        Context context = MyApplication.getInstance();
        File baseDir = new File(context.getFilesDir(), childDir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        XLogUtil.d(TAG, "-------- cache directionary is: " + baseDir.getAbsolutePath() + "---------");
        return baseDir;
    }

    public File getFile(String safeKey) {
        return new File(mDirFile, safeKey);
    }

    private String getSafeKey(String key) {
        if (mKeyCreator == null) {
            return key;
        }
        String safeKey = mKeyCreator.getSafeKey(key);
        return TextUtils.isEmpty(safeKey) ? key : safeKey;
    }

    public boolean put(String key, byte[] value) {
        String safeKey = getSafeKey(key);
        mWriteLocker.acquire(safeKey);
        File f = getFile(safeKey);
        OutputStream fos = null;
        try {
            fos = new BufferedOutputStream(new FileOutputStream(f));
            fos.write(value);
            fos.flush();
            if (mAutoClearController != null) {
                mAutoClearController.put(f);
            }
            return true;
        } catch (IOException e) {
            deleteFile(f);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mWriteLocker.release(safeKey);
        }
    }

    public byte[] get(String key) {
        String safeKey = getSafeKey(key);
        File f = getFile(safeKey);
        if (f.exists()) {
            try {
                if (mAutoClearController != null) {
                    mAutoClearController.get(f);
                }
                FileInputStream fin = new FileInputStream(f);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len = 0;
                while ((len = fin.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                fin.close();
                return outStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public byte[] get(String key, long time) {
        String safeKey = getSafeKey(key);
        File f = getFile(safeKey);
        if (!f.exists()) {
            return null;
        }
        if (time == -1 || System.currentTimeMillis() - f.lastModified() < time) {
            return get(key);
        }
        return null;
    }

    public void clear() {
        if (mAutoClearController != null) {
            mAutoClearController.clear();
        }
        deleteFile(mDirFile);
    }

    public void remove(String key) {
        File f = getFile(getSafeKey(key));
        if (mAutoClearController != null) {
            mAutoClearController.remove(f);
        }
        deleteFile(f);
    }

    public boolean putObject(String key, Object value) {
        if (key == null || null == value) {
            return false;
        }
        return put(key, CacheUtil.toByteArray(value));
    }

    public Object getObject(String key) {
        if (key == null) {
            return null;
        }
        byte[] bytes = get(key);
        if (bytes == null) {
            return null;
        }
        return CacheUtil.toObject(bytes);
    }

    public Object getObject(String key, long time) {
        File f = new File(mDirFile, key);
        if (!f.exists()) {
            return null;
        }
        if (time == -1 || System.currentTimeMillis() - f.lastModified() < time) {
            return getObject(key);
        }
        return null;
    }

    public synchronized boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        boolean success = true;
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                if (!file.delete()) {
                    success = false;
                }
            }
            for (int i = 0; i < childFiles.length; i++) {
                if (!deleteFile(childFiles[i])) {
                    success = false;
                }
            }
            if (!file.delete()) {
                success = false;
            }
        }
        return success;
    }
}