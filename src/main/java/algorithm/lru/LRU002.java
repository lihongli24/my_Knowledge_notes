package algorithm.lru;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Created by lihongli on 2019/10/24.
 */
public class LRU002 {
    private LinkedHashMap<String, String> cache;

    /**
     * 依靠linkedHashMap自己提供的该功能
     * @param totalSize
     */
    public LRU002(int totalSize){
        cache = new LinkedHashMap<String, String>(totalSize, 0.45f, true){
            @Override
            protected boolean removeEldestEntry(Entry eldest) {
                return this.size() > totalSize;
            }
        };
    }

    public void add(String key, String value){
        cache.put(key, value);
    }

    public String get(String key){
        return cache.get(key);
    }

    public LinkedHashMap<String, String> getCache() {
        return cache;
    }
}
