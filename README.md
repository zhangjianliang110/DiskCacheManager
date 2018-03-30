# DiskCacheManager
its a file cache manager tool for android developer, with function of auto clear old files , its safe、stable!

Created by zhangjianliang on 2018/3/20

1、使用
    文件缓存管理入口，都用CacheManager调用就好了，其他类都设置为非public的，避免调用错了
    CacheManager.java
    Demo:
        1)写入 save data
            CacheManager.getInstance(applicationContext).getDiskCache("dir").putObject("fileName", data);
            如果要自定义文件夹
            CacheManager.getInstance(applicationContext).getDiskCache("dir", "maxLimitSize").putObject("fileName", data);

        2)读取 read data
            同步读取：
            CacheManager.getInstance(applicationContext).getDiskCache("dir").get("fileName");
            异步读取
            CacheManager.getInstance(applicationContext).getDiskCache("dir").getObject("fileName", new AsyncCallback<String>(){
                        @Override
                        public void onResult(String object) {

                        }
                    });
2、内容
    CacheManager.java  封装了一个Map，key为文件夹路径，value为该文件夹的管理器(DiskCache)

    缓存文件夹及其管理器是为了统一对该文件的操作入口，通过同一个对象操作该文件，方便处理并发问题

    DiskCache.java  内部包含自动清理控制器 AutoClearController、文件操作对象AsyncFileCache

    AutoClearController  记录该文件下所有文件及文件的最后修改时间、文件总大小，当写入数据时，会判断是否超过缓存大小限制，超过则从最老的文件开始清理
                         默认是关闭自动清理的，开启需要调用（enable auto clear old files when out of max size）
						 CacheManager.getInstance(applicationContext).getDiskCache("dir").setAutoClearEnable(true)

    AsyncFileCache  异步文件操作类，文件的读、写、删除等操作

    DiskCacheWriteLocker 文件写入锁，防止从不同线程操作该文件时并发写入的问题
