package com.zjl.cache;

import android.support.v4.util.LruCache;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 生成安全的key，避免数据泄露
 * Created by zhangjianliang on 2018/3/22
 */
class SafeKeyCreator {

    private final LruCache<String, String> loadIdToSafeHash = new LruCache<String, String>(100);

    public String getSafeKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        String safeKey;
        synchronized (loadIdToSafeHash) {
            safeKey = loadIdToSafeHash.get(key);
        }
        if (safeKey == null) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                messageDigest.update(key.getBytes("UTF-8"));
                safeKey = CacheUtil.sha256BytesToHex(messageDigest.digest());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            synchronized (loadIdToSafeHash) {
                loadIdToSafeHash.put(key, safeKey);
            }
        }
        return safeKey;
    }
}