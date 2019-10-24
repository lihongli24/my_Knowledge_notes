package algorithm.lru;

import java.util.LinkedHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by lihongli on 2019/10/24.
 */
public class LRUTest {

    @Test
    public void test001() {
        LRU001 lru001 = new LRU001(4);
        lru001.add("1", "1");
        lru001.add("2", "2");
        lru001.add("3", "3");
        lru001.add("4", "4");
        lru001.get("1");
        lru001.add("5", "5");

        LinkedHashMap<String, String> cache = lru001.getCache();
        Assert.assertEquals("1", cache.get("1"));
        Assert.assertEquals("3", cache.get("3"));
        Assert.assertEquals("4", cache.get("4"));
        Assert.assertEquals("5", cache.get("5"));
        Assert.assertNull(cache.get("2"));

    }

    @Test
    public void test002() {
        LRU002 lru001 = new LRU002(4);
        lru001.add("1", "1");
        lru001.add("2", "2");
        lru001.add("3", "3");
        lru001.add("4", "4");
        lru001.get("1");
        lru001.add("5", "5");

        LinkedHashMap<String, String> cache = lru001.getCache();
        Assert.assertEquals("1", cache.get("1"));
        Assert.assertEquals("3", cache.get("3"));
        Assert.assertEquals("4", cache.get("4"));
        Assert.assertEquals("5", cache.get("5"));
        Assert.assertNull(cache.get("2"));

    }

}
