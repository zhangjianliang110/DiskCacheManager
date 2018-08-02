###文件缓存管理：
1、同步读写/异步读写：提供简单易用的api，可以同步读写文件、异步读写文件
2、异步读取并回调到主线程：提供SyncCallback，该回调函数，在异步读取文件后会回调的主线程
3、自动从最老的文件开始清理超过缓存大小限制的文件：
	CacheManager.getInstance(applicationContext).getDiskCache("dir").setAutoClearEnable(true);
4、线程安全 
	文件写入有加读写锁，每个文件一把锁，保证线程安全，锁放在对象池中，高效高性能
5、直接读写对象：提供了api，直接读写对象即可。

# DiskCacheManager
1、使用
    文件缓存管理入口，都用CacheManager调用就好了，其他类都设置为非public的，避免调用错了
    CacheManager.java
    Demo:
        
	1)写入 put data
	
            CacheManager.getInstance(applicationContext).getDiskCache("dir").putObject("fileName", data);
            put with maxSize
            CacheManager.getInstance(applicationContext).getDiskCache("dir", "maxLimitSize").putObject("fileName", data);

        2)读取 
            sync read data同步读取：
            CacheManager.getInstance(applicationContext).getDiskCache("dir").get("fileName");
            async read data异步读取
            CacheManager.getInstance(applicationContext).getDiskCache("dir").getObject("fileName", new AsyncCallback<String>(){
                        @Override
                        public void onResult(String object) {

                        }
                    });
	3)
2、内容
    CacheManager.java  封装了一个Map，key为文件夹路径，value为该文件夹的管理器(DiskCache)

    缓存文件夹及其管理器是为了统一对该文件的操作入口，通过同一个对象操作该文件，方便处理并发问题

    DiskCache.java  内部包含自动清理控制器 AutoClearController、文件操作对象AsyncFileCache

    AutoClearController  记录该文件下所有文件及文件的最后修改时间、文件总大小，当写入数据时，会判断是否超过缓存大小限制，超过则从最老的文件开始清理
                         默认是关闭自动清理的，开启需要调用（enable auto clear old files when out of max size）
						 CacheManager.getInstance(applicationContext).getDiskCache("dir").setAutoClearEnable(true)

    AsyncFileCache  异步文件操作类，文件的读、写、删除等操作

    DiskCacheWriteLocker 文件写入锁，防止从不同线程操作该文件时并发写入的问题
