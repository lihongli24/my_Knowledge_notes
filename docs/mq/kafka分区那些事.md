# kafka的分区那些事

* AR = ISR + OSR
* AR:分区中的所有副本统称为 `AR` (Assigned Replicas)
* ISR:所有与leader副本保持一定程度同步的副本（包括leader副本在内）组成 `ISR` (In Sync Replicas)。
* OSR:当follower副本落后太多或失效时，leader副本会把它从 ISR 集合中剔除

## 服务端分区重分配
因为只有Leader提供对外的读写，如果leader挂掉了，再回来，他就只是一个fllower了，如果大量的不通分区的leader都集中于一台broker上面，那肯定为导致改broker的负载很大。分区重分配就是说重新对分区分配leader,执行脚本`kafka-preferred-replica-election.sh`即可，也可以在该脚本的参数中指定json文件，只对1个分区做重分配

> 分担broker中的写压力


### partition的leader选举
> partation只有leader提供读写服务，其他的fllower都只是进行数据的备份，当leader挂掉了的时候，从中选举出一个成为新的leader.
1. 原leader下线的时候
  
    在AR中第一个存活的副本，并且他还在ISR中 
    
2. 分区重分配的时候
  
    因为只有Leader提供对外的读写，如果leader挂掉了，再回来，他就只是一个fllower了，如果大量的不通分区的leader都集中于一台broker上面，那肯定为导致改broker的负载很大。有两种解决方式，

    1. 自动的分区重分配(可能导致关键时刻自动进行了分区重分配，严重影响系统运行)
    2. 手动的分区重分配,执行kafka/bin的脚本`kafka-preferred-replica-election.sh`
    
      这个时候的选举策略是:选举优先副本，即AR中的第一个节点
    
3. 某一个broker优雅下线的时候

    AR中第一个存活的副本&&该副本在ISR中&&该副本不在下线的broker中

## 

## 客户端的分区分配

### 分区策略

由客户端参数`partition.assignment.stratgy`来指定支持的分区分配策略

#### RangeAssignor

消费者总数和分区总数进行整除得到一个跨度，将分区按照跨度进行平均分配。



```
假设：
n=分区数/消费者数
m=分区数%消费者数

那么:
前m的消费者分配到n+1个分区
后面的消费者(消费者数-m)分配n个分区
```

1. 情况1：
消费者组里面有2个消费者订阅了2个主题
每个主题4个分区，分别为t0p0、t0p1、t0p2、t0p3、t1p0、t1p1、t1p2、t1p3
n = 4/2 = 2
m = 4%2=2
那么分配如下：
```
c1 = t0p0、t0p1、t1p0、t1p1、
c2 = t0p2、t0p3、t1p2、t1p3
```

2. 情况2：
如果只要3个分区，分别是t0p0、t0p1、t0p2、t1p0、t1p1、t1p2
n = 3/2 = 1
m = 3 %2 = 1
所以分配如下：

```
c1 = 	t0p0、t0p1 、 t1p0、t1p1
c2 = t0p2、t1p2
```


所以很明显，这个分配策略存在明显的不均匀

#### RoundRobinAssignor

按照字典序逐个分配

1. 上面的情况2，如果用RoundRobinAssignor来分配应该是，能解决这个问题

```
c1=t0p0、t0p2、t1p1
c2=t0p1、t1p0、t1p2
```

2. 情况3：

   如果有3个topic,分区如下：

   t0p0,t1p0,t1p1,t2p0,t2p1,t2p2

   有3个消费者：

   c1订阅了t0;

   c2订阅了t0和t1;
   
   c3订阅了t1和t2

   按照RoundRobinAssignor，分配如下
   
   ```
   c1:t0p0
   c2:t1p0
   c3:t1p1,t2p0,t2p1,t2p2
   ```
   
   这种情况下又出现了不均匀，t1p1完全可以交给c2去消费

#### StickAssignor

kafka 0.11.x版本引入的分区分配策略

1. 尽量分配均匀
2. 黏连分配，rebalance的时候，原先分配的分区还是分配到原来的消费者上。


### rebalance（消费者组）
1. 发现协调器(GroupCoordinator)
	
	每个Broker启动的时候都会启动一个GroupCoordinator
	
	1. 分区号 = `Utils.abs(groupId.hashCode)%_consumer_offset这个topic的分区数`
	2. GroupCoordinator所在的Broker =  1中分区号所在分区的leader的Broker
	3. 消费者需要找到GroupCoordinator所在的Broker并且与之建立网络连接。
	
2. 消费者组中的消费者向GroupCoordinator中发送JoinGroupRequest请求

   1. groupId
   2. memberId 首次加入的为null,之后由GroupCoordinator分配
   3. 支持的分配策略列表

3. GroupCoordinator处理JoinGroupRequest请求

   1. 选取消费者组的leader,很随意
   2. 选举分区分配策略
   3. 将结果返回给消费者们，其中leader消费者带一个标记isLeader=true

4. 消费者们发送SyncGroupRequest请求，leader根据选举出来的分区分配策略进行分区分配实施，通过SyncGroupRequest请求通过GroupCoordinator转发给消费者组里面的其他消费者

   1. 将分配信息存入_consumer_offset这个topic中
   2. 通知消费者们的分区分配结果

5. 消费者们与GroupCoordinator建立心跳连接

   1. GroupCoordinator通过心跳确认消费者是否在正常消费

6. 以下情况会触发rebalance

   1. 新消费者加入消费者组
   2. 有消费者宕机下线（长时间未进行心跳）
   3. GroupCoordinator节点发生变动
   4. 消费者组订阅的主题或者主题分区有变化

### _consumer_offset

这也是kafka的一个topic，默认有50个分区、保存了每个分区的消费offset大致结构如下

```json
{
    "group_id":"消费者组Id",
    "generation_id":"group进过的rebalance的代数",
    "member_id":"消费者的Id",
    "retention_time":"保留时间",
    "topics":{
        "topic":"topic名称",
        "partitions":[
            {
                "partition":"分区",
                "offset":"消费位移"
            }
        ]
    }
}
```





## kafka怎么保证消息的唯一





##  参考资料

[spring使用kafka](https://blog.csdn.net/qq_26323323/article/details/84938892)

```

```