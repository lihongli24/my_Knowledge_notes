# redis的热点key

[toc]



## 热点key的威胁

hotkey就是某一个key在短时间内访问量突然增加到很大。redis的架构大概有下面几种

1. 单机
2. 哨兵模式
3. redis集群

不管是上面哪一种，对于一个热点key访问到的都是同一台机器，如果量特别大的情况，确实会导致机器挂掉。

1. 流量集中，达到物理网卡上限。 

2. 请求过多，缓存分片服务被打垮。

3. DB 击穿，引起业务雪崩。

## 热点key的发现

### 1. 客户端监控

实现方式：在请求redis命令之前，记录下请求的命令和key等。比如

```java
public static final AtomicLongMap<String> ATOMIC_LONG_MAP = AtomicLongMap.create();
public Connection sendCommand(final ProtocolCommand cmd, final byte[]... args) {
    //从参数中获取key
    String key = analysis(args);
    //计数
    ATOMIC_LONG_MAP.incrementAndGet(key);
    //ignore
  ..命令请求
}
```

### 2. proxy监控

针对通过proxy去访问redis的架构下，可以在proxy层增加对命令请求的校验

###  3. redis server的monitor命令

monitor命令是监控一段时间内redis server被请求的命令。

使用他的监控结果我们在使用自己的实现做热点key上的监控。

![img](redis%E7%9A%84%E7%83%AD%E7%82%B9key%E5%8F%91%E7%8E%B0.assets/208620683574b21868fa7b5ed948d2f9.jpeg)

下面为一次monitor命令执行后部分结果。

```verilog
1477638175.920489 [0 10.16.xx.183:54465] "GET" "tab:relate:kp:162818"
1477638175.925794 [0 10.10.xx.14:35334] "HGETALL" "rf:v1:84083217_83727736"
1477638175.938106 [0 10.16.xx.180:60413] "GET" "tab:relate:kp:900"
1477638175.939651 [0 10.16.xx.183:54320] "GET" "tab:relate:kp:15907"
...
1477638175.962519 [0 10.10.xx.14:35334] "GET" "tab:relate:kp:3079"
1477638175.963216 [0 10.10.xx.14:35334] "GET" "tab:relate:kp:3079"
1477638175.964395 [0 10.10.xx.204:57395] "HGETALL" "rf:v1:80547158_83076533"
```

java实现，统计最近10000条命令的热点key,伪代码
```java
//获取10万条命令
List<String> keyList = redis.monitor(100000);
//存入到字典中，分别是key和对应的次数
AtomicLongMap<String> ATOMIC_LONG_MAP = AtomicLongMap.create(); 
//统计
for (String command : commandList) {
 //从参数中获取key
		String key = analysis(command);
    ATOMIC_LONG_MAP.incrementAndGet(key);
}
//后续统计和分析热点key
statHotKey(ATOMIC_LONG_MAP);
```
业界的使用示例：[Facebook开源的redis-faina](https://github.com/facebookarchive/redis-faina/blob/master/redis-faina.py)
基本上也是将monitor的输出作为输入，一行一行分析，分析出热点key和热点command

### 4. redis server的--hotkeys命令

Redis在4.0.3中为redis-cli提供了--hotkeys，用于找到热点key.

实现原理是4.0之后支持的lfu内存淘汰策略，在原来的lru字段上记录key的热点情况。

[redis的lfu参考资料](https://yq.aliyun.com/articles/278922)

它的执行结果是这样的：

```verilog
-------- summary -------
Sampled 382562 keys in the keyspace!
hot key found with counter: 231 keyname: user:125
.......
```

### 5. 网络抓包

因为redis server提供服务是通过tcp对外提供服务的。而且redis的通信是基于简单的文本，如果能进行网络抓包再做解析是可以实现的。



![img](redis%E7%9A%84%E7%83%AD%E7%82%B9key%E5%8F%91%E7%8E%B0.assets/574997c9f94c4674e0a9d1fdbf64d07b.jpeg)

### 几种方式的比较

既然存在这么多种方法，那就对这种方式进行比较，看看他们的优缺点

| 方式         | 实现方式                                                     | 优点                                                         | 缺点                                                         |
| :----------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 客户端监控   | 在发出redis命令之前记录请求内容，在做统计                    | 1. 实现起来最简单                                            | 1. 对业务代码有侵入.多个语言的场景下需要各自维护一套<br/>2. 因为无法预知key的数量，可能导致统计的map非常大，存在内存溢出的风险。 |
| proxy监控    | 不需要客户端分别实现，统一在proxy层做数据的监控              | 1. 只需要对代理层做统一的监控逻辑，维护点比较少              | 1. 存在架构的局限性，不是所有的公司都是使用proxy的架构<br/>2. proxy的维护成本和稳定性考量 |
| montior命令  | montior命令监控近期的命令执行，自实现或者使用开源的方式来分析最近的命令 | 1. 不受架构的限制<br/>2. 实现不复杂，facebook有开源faina     | 1. 在高并发的情况下可能引起内存暴增和影响redis性能，只能在短时间内使用<br/>2. 只能对单台redis server进行监控，需要自己去合并统计结果 |
| --hotkey命令 | 基于lfu的淘汰策略中维护的lru字段来分析key热度。              | 1. redis天然支持的统计方式--需要在内存淘汰策略为lfu的情况下<br/>2. 不额外占用过多内存 | 1. 如果键比较多的话，执行效率可能不高<br/>2. 热度定义的不够准确。(网上的观点，不太确定是什么原因，可能是因为redis的lru和lfu使用的是近似策略，不对全部的key进行淘汰判断) |
| 网络抓包     | 通过网络抓包的方式，再进行自定义的解析                       | 1. 对客户端和服务端完全没有侵入<br/>2. 有框架已支持，例如ELK(ElasticSearch Logstash Kibana)体系下的packetbeat[2] 插件，可以实现对Redis、MySQL等众多主流服务的数据包抓取、分析、报表展示 | 1.  高访问量的情况下，对redis的网络可能有影响<br/>2. 可能会有丢包的问题<br/>3. 维护成本比较高，可能需要专业团队 |



## 发现后怎么处理

### 1. 服务端做本地缓存

可以通过配置中心之类的系统，在统计出热点key之后，通过配置中心自动分发到服务端，服务端对这个key做一份本地的缓存。因为流量肯定要进过服务端，所以能在服务端直接返回最好。**需要对服务端本地缓存做好内存淘汰策略，防止本地缓存把服务高挂了。**

### 2. 在热点key后面加上后缀

还是上面的逻辑，当发现出现热点key的时候，同步到所有服务器上，在key的后面加上n,n可以是数字，让他被均匀的分到不通的单例机器上。

> 因为同一个key被分配到多台机器上的情况，会出现数据不一致的情况，有些机器上是新数据了，有些还是老数据，这个问题怎么解决?????

### 3. proxy做读写分离

![img](redis%E7%9A%84%E7%83%AD%E7%82%B9key%E5%8F%91%E7%8E%B0.assets/1620.jpeg)

通过代理的方式，将读写做分离，写走master机器，读从slave上获取，因为热点key的大多数应用场景都是读取，这样可以为master减轻压力。同时proxy可以做slave读取操作的负载均衡，更进一步的分摊压力。

## redis运维工具

* [cachecloud](https://github.com/sohutv/cachecloud)
* [禁用不安全的命令](https://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&mid=2247487198&idx=2&sn=eea95b2fbad114cabbfbcf1804b8b081&chksm=eb538be8dc2402fee685771deff4852214bc908448ca3cc3644794d66d72d8af843289f0d65d&scene=21#wechat_redirect)



## 参考资料

[热点key](https://toutiao.io/posts/xq4s10/preview)

