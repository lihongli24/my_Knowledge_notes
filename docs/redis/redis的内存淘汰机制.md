# redis的内存淘汰机制

[toc]
## 为什么需要内存淘汰机制
redis之所以响应速度快，其中一个原因是redis是一个内存数据库，内存访问速度比磁盘会快很多。但是当redis运行很长一段时间之后，肯定会有大量数据在redis内存中，如果没有某种机制来做清理操作，那内存肯定会有被用完的时候。所以redis做了一些操作

1. 对于设置了过期时间的key,使用下面的策略进行清除
   1. 定期删除
   2. 惰性删除

对于过期key的操作，也不能保证过期的key都被删除掉。

1. 定期删除：因为定期删除的时候如果是全量删除的话，那执行的时间可能会很长，所以在每次执行定期的删除操作的时候只会对一部分的key做过期删除操作，那么我们还会剩下一部分的key没被删掉
2. 惰性删除：只有key被访问到的时候才会判断它是否过期了，如果过期了从数据库中删除，但是如果不是常被访问到的key,那么就没办法被删除掉。
3. 其他情况：如果大量的key没有设置过期时间，那就不会被过期策略给删除到。

接合上面的3种情况，内存中的数据会出现一直增加的情况，总会有一个时间达到硬件设备的限制。影响redis的功能和性能。

**所以当内存占用到某一个程度的情况下，通过一个机制对不尝试用的key做一个淘汰操作是很有必要的。**

## 什么情况下会触发内存淘汰机制

**redis server收到命令后会先检查空间是否足够，如果不能清空出足够的空间，就跑出OOM**

**freeMemoryIfNeededAndSafe方法进行检查和清除**

文件：redis/src/server.c

```c
int processCommand(client *c) {
  //运行一些module的filter
    moduleCallCommandFilters(c);
  //对命令进行有效性的认证
  //执行的权限认证
  //重定向逻辑
  if (server.maxmemory && !server.lua_timedout) {
    //真正的执行判断和淘汰逻辑
    int out_of_memory = freeMemoryIfNeededAndSafe() == C_ERR;
    /* freeMemoryIfNeeded may flush slave output buffers. This may result
         * into a slave, that may be the active client, to be freed. */
    //
    if (server.current_client == NULL) return C_ERR;

    /* It was impossible to free enough memory, and the command the client
         * is trying to execute is denied during OOM conditions or the client
         * is in MULTI/EXEC context? Error. */
    //如果无法腾出足够的内存,抛出oom异常
    if (out_of_memory &&
        (c->cmd->flags & CMD_DENYOOM ||
         (c->flags & CLIENT_MULTI &&
          c->cmd->proc != execCommand &&
          c->cmd->proc != discardCommand)))
    {
      flagTransaction(c);
      addReply(c, shared.oomerr);
      return C_OK;
    }
  }
  
  //命令的执行
  
}
```







## redis支持哪些内存淘汰策略
**redis 提供 6种数据淘汰策略：**

1. **volatile-lru**：从已设置过期时间的数据集（server.db[i].expires）中挑选最近最少使用的数据淘汰
2. **volatile-ttl**：从已设置过期时间的数据集（server.db[i].expires）中挑选将要过期的数据淘汰
3. **volatile-random**：从已设置过期时间的数据集（server.db[i].expires）中任意选择数据淘汰
4. **allkeys-lru**：当内存不足以容纳新写入数据时，在键空间中，移除最近最少使用的key（这个是最常用的）
5. **allkeys-random**：从数据集（server.db[i].dict）中任意选择数据淘汰
6. **no-eviction**：禁止驱逐数据，也就是说当内存不足以容纳新写入数据时，新写入操作会报错。这个应该没人使用吧！

4.0版本后增加以下两种：

1. **volatile-lfu**：从已设置过期时间的数据集(server.db[i].expires)中挑选最不经常使用的数据淘汰
2. **allkeys-lfu**：当内存不足以容纳新写入数据时，在键空间中，移除最不经常使用的key

## 这些内存淘汰策略是怎么实现的

上面在触发内存淘汰的时机的时候介绍过，会执行一个方法freeMemoryIfNeededAndSafe方法

```c
int freeMemoryIfNeededAndSafe(void) {
    if (server.lua_timedout || server.loading) return C_OK;
    return freeMemoryIfNeeded();
}
```

看了**freeMemoryIfNeeded**这个方法的代码有点长，下面把主要的逻辑点写一下

```c
int freeMemoryIfNeeded(void) {
  //对当前内存情况进行分析，统计出需要释放的内存大小mem_tofree
  if (getMaxmemoryState(&mem_reported,NULL,&mem_tofree,NULL) == C_OK)
    return C_OK;
  
  //需要清除出mem_tofree大小的空间
  while (mem_freed < mem_tofree) {
    //如果是lfu/lru/ttl走这个分之
    if (server.maxmemory_policy & (MAXMEMORY_FLAG_LRU|MAXMEMORY_FLAG_LFU) ||
            server.maxmemory_policy == MAXMEMORY_VOLATILE_TTL)
        {
            struct evictionPoolEntry *pool = EvictionPoolLRU;

            while(bestkey == NULL) {
                unsigned long total_keys = 0, keys;

                /* We don't want to make local-db choices when expiring keys,
                 * so to start populate the eviction pool sampling keys from
                 * every DB. */
              //遍历redis机器上DB---redis是支持16个db的，所以需要遍历
                for (i = 0; i < server.dbnum; i++) {
                    db = server.db+i;
                  //按照策略来判断，如果是allkeyXX的从数据的dict中获取，如果是volatileXX的从过期key的dict中获取。
                    dict = (server.maxmemory_policy & MAXMEMORY_FLAG_ALLKEYS) ?
                            db->dict : db->expires;
                    if ((keys = dictSize(dict)) != 0) {
                      //遍历出需要淘汰的数据到pool中，这里面有上面支持的几种逻辑的实现
                        evictionPoolPopulate(i, dict, db->dict, pool);
                        total_keys += keys;
                    }
                }
            }
    }
    
    //random策略下的实现
    /* volatile-random and allkeys-random policy */
        else if (server.maxmemory_policy == MAXMEMORY_ALLKEYS_RANDOM ||
                 server.maxmemory_policy == MAXMEMORY_VOLATILE_RANDOM)
        {
            /* When evicting a random key, we try to evict a key for
             * each DB, so we use the static 'next_db' variable to
             * incrementally visit all DBs. */
            for (i = 0; i < server.dbnum; i++) {
                j = (++next_db) % server.dbnum;
                db = server.db+j;
                dict = (server.maxmemory_policy == MAXMEMORY_ALLKEYS_RANDOM) ?
                        db->dict : db->expires;
                if (dictSize(dict) != 0) {
                    de = dictGetRandomKey(dict);
                    bestkey = dictGetKey(de);
                    bestdbid = j;
                    break;
                }
            }
        }
    //对统计出来的进行清除
  }
}
```

上面代码中，最终找哪些key需要淘汰的逻辑应该是在**evictionPoolPopulate**中

```c
void evictionPoolPopulate(int dbid, dict *sampledict, dict *keydict, struct evictionPoolEntry *pool) {
    int j, k, count;
    dictEntry *samples[server.maxmemory_samples];

  //按照server.maxmemory_samples这个配置项取一定量的key---->这也是说redis的lru和lfu算法都只是走的近似的lru和lfu，目的是省空间
    count = dictGetSomeKeys(sampledict,samples,server.maxmemory_samples);
  //开始遍历这些key,计算这些key的idle
    for (j = 0; j < count; j++) {
        unsigned long long idle;
        sds key;
        robj *o;
        dictEntry *de;

        de = samples[j];
        key = dictGetKey(de);

       
      //如果刚刚是volatileXX走的过期时间dict，需要重新获kv的dict中对应的数据
        if (server.maxmemory_policy != MAXMEMORY_VOLATILE_TTL) {
            if (sampledict != keydict) de = dictFind(keydict, key);
            o = dictGetVal(de);
        }

        //lru
        if (server.maxmemory_policy & MAXMEMORY_FLAG_LRU) {
            idle = estimateObjectIdleTime(o);
        } else if (server.maxmemory_policy & MAXMEMORY_FLAG_LFU) {
           //lfu
            idle = 255-LFUDecrAndReturn(o);
        } else if (server.maxmemory_policy == MAXMEMORY_VOLATILE_TTL) {
            //ttl只需要比较过期时间val
            idle = ULLONG_MAX - (long)dictGetVal(de);
        } else {
            serverPanic("Unknown eviction policy in evictionPoolPopulate()");
        }
    }
  
  //后面是把dbid和idle放入pool
```

### redis的lru实现

1. redis Object中维护了一个lru字段

   ```c
   typedef struct redisObject {
       unsigned type:4;
       unsigned encoding:4;
       unsigned lru:LRU_BITS; /* LRU time (relative to global lru_clock) or
                               * LFU data (least significant 8 bits frequency
                               * and most significant 16 bits access time). */
       int refcount;
       void *ptr;
   } robj;
   ```

2. 访问key的时候，更新lru成server.lruclock;

   ```c
   robj *lookupKey(redisDb *db, robj *key) {
     dictEntry *de = dictFind(db->dict,key->ptr);
     if (de) {
         robj *val = dictGetVal(de);
   
         /* Update the access time for the ageing com.demo.li.algorithm.
          * Don't do it if we have a saving child, as this will trigger
          * a copy on write madness. */
         if (server.rdb_child_pid == -1 && server.aof_child_pid == -1)
             val->lru = server.lruclock;
         return val;
     } else {
         return NULL;
     }
   }
   ```

3. 这个server.lruclockyou由系统定期更新，减少访问系统时间的次数

4. 需要内存淘汰的时候，根据配置参数`maxmemory_samples`，随机从redis中获取maxmemory_samples个key,放入需要比较的范围内

5. 使用对象的lru，计算每个key的热度

   ```c
   unsigned long estimateObjectIdleTime(robj *o) {
     if (server.lruclock >= o->lru) {
         return (server.lruclock - o->lru) * REDIS_LRU_CLOCK_RESOLUTION;
     } else {
         return ((REDIS_LRU_CLOCK_MAX - o->lru) + server.lruclock) *
                     REDIS_LRU_CLOCK_RESOLUTION;
     }
   }
   ```

6. 淘汰那些最凉的

### lfu的实现

还是redisObject这个字段，lru字段在lfu策略下，24位分为两端。

高16位:访问时间

低8位：访问次数

```c
typedef struct redisObject {
    unsigned type:4;
    unsigned encoding:4;
    unsigned lru:LRU_BITS; /* LRU time (relative to global lru_clock) or
                            * LFU data (least significant 8 bits frequency
                            * and most significant 16 bits access time). */
    int refcount;
    void *ptr;
} robj;
```

但是只有8位最多只能存放255次，对于那些频率很高的key肯定不够用。

redis用了一个概率学的概念

```c
/* Logarithmically increment a counter. The greater is the current counter value
 * the less likely is that it gets really implemented. Saturate it at 255. */
uint8_t LFULogIncr(uint8_t counter) {
    if (counter == 255) return 255;
    double r = (double)rand()/RAND_MAX;
    double baseval = counter - LFU_INIT_VAL;
    if (baseval < 0) baseval = 0;
    double p = 1.0/(baseval*server.lfu_log_factor+1);
    if (r < p) counter++;
    return counter;
}

```

下面是redis计算增加counter的计算方式

1. 如果到了255了，到顶了，直接返回
2. LFU_INIT_VAL = 5， 第一次设值，不能因为访问频率低就被删除，所以使用了默认值5，能保活一段时间
3. double baseval = counter - LFU_INIT_VAL;   counter越大，baseval肯定越大。
4. double p = 1.0/(baseval*server.lfu_log_factor+1);
   1. server.lfu_log_factor是个配置值，默认是10 
   2. baseval越大，那计算出来的p肯定越小
5. double r = (double)rand()/RAND_MAX; 计算出一个0-1之间的随机数
6. if (r < p) counter++; 如果本次计算出来的p比r大，就给counter加1，因为counter的增长下，p会越来越小，导致counter的增加几率变得越来越小。逐渐收敛到255。
7. 就像下面的图，p就是这条曲线，r就像这条竖轴上随便去一个点(0-1之间)，r<p就是最终取的点在这条黄线下的概率，越来越低。

![image.png](redis%E7%9A%84%E5%86%85%E5%AD%98%E6%B7%98%E6%B1%B0%E6%9C%BA%E5%88%B6.assets/fb2aaea50acbbe1d84dfe6966117f363.png)

```
# +--------+------------+------------+------------+------------+------------+
# | factor | 100 hits   | 1000 hits  | 100K hits  | 1M hits    | 10M hits   |
# +--------+------------+------------+------------+------------+------------+
# | 0      | 104        | 255        | 255        | 255        | 255        |
# +--------+------------+------------+------------+------------+------------+
# | 1      | 18         | 49         | 255        | 255        | 255        |
# +--------+------------+------------+------------+------------+------------+
# | 10     | 10         | 18         | 142        | 255        | 255        |
# +--------+------------+------------+------------+------------+------------+
# | 100    | 8          | 11         | 49         | 143        | 255        |
# +--------+------------+------------+------------+------------+------------+
```

最终使用这种方式能用255这个数值表示很大的范围。

>  如果一个key很久以前被访问了很多次，导致它的counter到了255，但是最近很长一段时间没被用过了，如果只是用counter来计算的话，那它肯定不会被淘汰。
>
> 这种情况下，redis增加了一个衰减操作。

回到上面计算idle的逻辑

```c
idle = 255-LFUDecrAndReturn(o);
```

```c
unsigned long LFUDecrAndReturn(robj *o) {
  //右移8位得到了高16位，表示上次的访问时间
    unsigned long ldt = o->lru >> 8;
  //位与操作，得到低8位，表示counter
    unsigned long counter = o->lru & 255;
  //计算上次被访问了之后经历了多少个衰减期，
    unsigned long num_periods = server.lfu_decay_time ? LFUTimeElapsed(ldt) / server.lfu_decay_time : 0;
  //如果衰减期大于0，counter = counter - 衰减期
    if (num_periods)
        counter = (num_periods > counter) ? 0 : counter - num_periods;
    return counter;
}
```

> counter + 衰减策略 = lfu

### ttl

ttl策略只支持**volatile-ttl**模式

```c
else if (server.maxmemory_policy == MAXMEMORY_VOLATILE_TTL) {
  //ttl只需要比较过期时间val
   /* In this case the sooner the expire the better. */
  idle = ULLONG_MAX - (long)dictGetVal(de);
} 
```

idle= ULLONG_MAX - key的过期时间，

越早过期的，越早被删除。