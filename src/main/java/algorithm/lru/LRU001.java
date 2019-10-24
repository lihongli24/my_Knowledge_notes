package algorithm.lru;

import java.util.LinkedHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by lihongli on 2019/10/24.
 */
public class LRU001 {

    private LinkedHashMap<String, String> cache;
    private int totalSize;

    public LRU001(int totalSize) {
        cache = new LinkedHashMap<>();
        this.totalSize = totalSize;
    }

    /**
     * 新增的时候
     * 1. 如果原来存在，先删除原来的
     * 2. 如果容量不够，删除第一个
     * 3. 插入本记录，那肯定是在最后面
     * @param key
     * @param value
     */
    public void add(String key, String value) {
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
        if (cache.size() >= totalSize) {
            cache.remove(cache.entrySet().iterator().next().getKey());
        }
        cache.put(key, value);
    }

    /**
     * 获取的时候
     * 1. 如果存在改key,先删除
     * 2. 重新放入，保证本记录在最新的位置
     * @param key
     * @return
     */
    public String get(String key) {
        String value = cache.get(key);
        if (null == value) {
            return null;
        }
        cache.remove(key);
        cache.put(key, value);
        return value;
    }

    public LinkedHashMap getCache() {
        return cache;
    }



}
