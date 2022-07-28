package com.negen;

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
    private long defaultExpireTime = 1000 * 3600 * 24 * 30;

    private static CacheUtil instance;


    public static CacheUtil getInstance(){
        if (null == instance)instance = new CacheUtil();
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
        this.put(key, value, defaultExpireTime);
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
