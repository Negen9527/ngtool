## 站在巨人的肩上，基于Java8手写本地缓存

#### 0、写在最前面

> 1、大多数业务场景下 ，为了提高系统的响应速度，使用缓存是最佳的选择；
>
> 2、由于本人工作性质特殊纯内网开发，在用maven导依赖无果后一气之下决定自己手写一个 简单的本地缓存来满足自己的业务需求
>
> 3、本文仅用于分享学习！！！



#### 1、缓存类具备的功能

> - 存储键值对  put()
> - 存储一定有效期的键值对  put()
> - 获取键值对  get()
> - 删除键值对  remove()
> - 定期清理过期的键值对 handleExpiredKeys()



项目目录结构如下：



![image-20220728211853664](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220728211853664.png)

#### 2、创建缓存类

创建 CacheUtil.java 详细代码如下：

```java
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Negen
 * @date 2022年7月28日20:25:47
 * @description 缓存工具类
 */
public class CacheUtil {
    /**
     * 存储键值对
     */
    private ConcurrentHashMap<String, Object> cacheMap = null;
    /**
     * 存储键值对的有效时间
     */
    private ConcurrentHashMap<String, Long> expireMap = null;
    /**
     * 默认过期时间(毫秒 )
     */
    private final long DEFAULT_EXPIRE_TIME = 1000 * 3600 * 24 * 30;

    private static CacheUtil instance = null;


    public static CacheUtil getInstance(){
        if (null == instance){
            synchronized (CacheUtil.class){
                if (null == instance)instance = new CacheUtil();
            }
        }
        return instance;
    }

    CacheUtil(){
        cacheMap = new ConcurrentHashMap<>();
        expireMap = new ConcurrentHashMap<>();
    }
}
```



#### 3、添加键值对方法

在缓存工具类中实现添加键值对的方法

```java
    /**
     * 添加键值对 默认失效时间30天
     * @param key
     * @param value
     */
    public void put(String key, Object value){
        this.put(key, value, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 添加键值对 自定义失效时间（毫秒）
     * @param key
     * @param value
     * @param expire
     */
    public void put(String key, Object value, long expire){
        cacheMap.put(key, value);
        expireMap.put(key, System.currentTimeMillis() + expire);
    }
```

#### 4、根据 key 获取相应的value

```java
    /**
     * 根据 key 获取相应的 value
     * @param key
     * @return
     */
    public Object get(String key){
        return cacheMap.get(key);
    }
```

#### 5、删除键值对

根据提供的键值从map中删除相应的键值 对并返回被删除的键值对个数

```java
    /**
     * 删除键值对
     * @param key
     * @return 被删除的键值对个数
     */
    public int remove(String key){
        int beforeRemoveCount = cacheMap.size();

        int afterRemoveCount = cacheMap.size();
        return beforeRemoveCount - afterRemoveCount;
    }
```

#### 6、处理已经失效的键值对

> 1）、基于过期时间map的过期时间进行排序
>
> 2）、遍历排序后的map
>
> 3）、判断键值对是否过期，过期则删除键值对

详细代码如下 ：

```java
    /**
     * 处理已经过期的键值对
     * @return
     */
    public int handleExpireKeys(){
        long now = System.currentTimeMillis();
        int beforeRemoveCount = cacheMap.size();
        List<Map.Entry<String, Long>> collect = expireMap.entrySet().stream()
                .sorted((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                .collect(Collectors.toList());
        Iterator<Map.Entry<String, Long>> iterator = collect.iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Long> kv = iterator.next();
            String key = kv.getKey();
            Long expireTime = kv.getValue();
            if (expireTime < now){
                this.remove(key);
                System.out.println(String.format("%s过期，已删除", key));
            }
        }
        int afterRemoveCount = cacheMap.size();
        int removeCount = beforeRemoveCount - afterRemoveCount;
        if (removeCount < 0)removeCount = 0;
        System.out.println("当前过期键值对：" + removeCount);
        return removeCount;
    }
```



#### 7、基于java提供的Timer定时执行缓存类的失效监测处理方法

```java
import java.util.Timer;
import java.util.TimerTask;

public class CacheHandleSchedule {
    public void start(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                CacheUtil.getInstance().handleExpireKeys();
            }
        };
        // 在指定延迟0毫秒后开始，随后地执行以1000毫秒间隔执行timerTask
        new Timer().schedule(timerTask, 0L, 1000L);
    }
}
```



#### 8、CacheUtil 完整代码

```java
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Negen
 * @date 2022年7月28日20:25:47
 * @description 缓存工具类
 */
public class CacheUtil {
    /**
     * 存储键值对
     */
    private ConcurrentHashMap<String, Object> cacheMap = null;
    /**
     * 存储键值对的有效时间
     */
    private ConcurrentHashMap<String, Long> expireMap = null;
    /**
     * 默认过期时间(毫秒 )
     */
    private final long DEFAULT_EXPIRE_TIME = 1000 * 3600 * 24 * 30;

    private static CacheUtil instance = null;


    public static CacheUtil getInstance(){
        if (null == instance){
            synchronized (CacheUtil.class){
                if (null == instance)instance = new CacheUtil();
            }
        }
        return instance;
    }

    CacheUtil(){
        cacheMap = new ConcurrentHashMap<>();
        expireMap = new ConcurrentHashMap<>();
    }

    /**
     * 添加键值对 默认失效时间30天
     * @param key
     * @param value
     */
    public void put(String key, Object value){
        this.put(key, value, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 添加键值对 自定义失效时间（毫秒）
     * @param key
     * @param value
     * @param expire 有效期
     */
    public void put(String key, Object value, long expire){
        cacheMap.put(key, value);
        expireMap.put(key, System.currentTimeMillis() + expire);
    }

    /**
     * 根据 key 获取相应的 value
     * @param key
     * @return
     */
    public Object get(String key){
        return cacheMap.get(key);
    }

    /**
     * 删除键值对
     * @param key
     * @return 被删除的键值对个数
     */
    public int remove(String key){
        int beforeRemoveCount = cacheMap.size();
        cacheMap.remove(key);
        expireMap.remove(key);
        int afterRemoveCount = cacheMap.size();
        return beforeRemoveCount - afterRemoveCount;
    }

    /**
     * 处理已经过期的键值对
     * @return
     */
    public int handleExpireKeys(){
        long now = System.currentTimeMillis();
        int beforeRemoveCount = cacheMap.size();
        List<Map.Entry<String, Long>> collect = expireMap.entrySet().stream()
                .sorted((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                .collect(Collectors.toList());
        Iterator<Map.Entry<String, Long>> iterator = collect.iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Long> kv = iterator.next();
            String key = kv.getKey();
            Long expireTime = kv.getValue();
            if (expireTime < now){
                this.remove(key);
                System.out.println(String.format("%s过期，已删除", key));
            }
        }
        int afterRemoveCount = cacheMap.size();
        int removeCount = beforeRemoveCount - afterRemoveCount;
        if (removeCount < 0)removeCount = 0;
        System.out.println("当前过期键值对：" + removeCount);
        return removeCount;
    }
}
```



#### 9、测试类

```java
import com.negen.CacheHandleSchedule;
import com.negen.CacheUtil;

public class TestCacheUtil {
    public static void main(String[] args) {
        CacheHandleSchedule cacheHandleSchedule = new CacheHandleSchedule();
        cacheHandleSchedule.start();
        CacheUtil cacheUtil = CacheUtil.getInstance();
        cacheUtil.put("zhangsan", "zhangsan", 100);
        cacheUtil.put("lisi", "lisi", 1200);
        cacheUtil.put("w5", "w5", 1300);
        Object zhangsan = cacheUtil.get("zhangsan");
        if (null != zhangsan)
            System.out.printf((String)zhangsan);
    }
}
```

#### 10、测试结果如下

![image-20220728211622479](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220728211622479.png)



11、项目下载地址

https://download.csdn.net/download/qq_36657751/86264631