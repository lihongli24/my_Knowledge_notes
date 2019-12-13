# kafka

* AR = ISR + OSR
* AR:分区中的所有副本统称为 `AR` (Assigned Replicas)
* ISR:所有与leader副本保持一定程度同步的副本（包括leader副本在内）组成 `ISR` (In Sync Replicas)。
* OSR:当follower副本落后太多或失效时，leader副本会把它从 ISR 集合中剔除

## 分区重分配
因为只有Leader提供对外的读写，如果leader挂掉了，再回来，他就只是一个fllower了，如果大量的不通分区的leader都集中于一台broker上面，那肯定为导致改broker的负载很大。分区重分配就是说重新对分区分配leader,执行脚本`kafka-preferred-replica-election.sh`即可，也可以在该脚本的参数中指定json文件，只对1个分区做重分配


## leader选举
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





##  参考资料

[spring使用kafka](https://blog.csdn.net/qq_26323323/article/details/84938892)