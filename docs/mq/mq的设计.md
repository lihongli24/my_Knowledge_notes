# 如果让你设计一个mq怎么设计

## 可伸缩性，可拓展

kafa的拓展方式topic>> partation,如果某个topic中的消息量特别大，可能超出一台机器的物理存储范围，怎么办，kafka的partation就是对topic的分区，让partation分布在不同的机器上，实现了数据存储的水平拓展



## 消息读取和存储的效率

1. 顺序写，顺序读 kafak
2. 

## mq服务的高可用

1. 多副本，防止单点崩溃问题
2. 发送消息后的确认模式，leader确认、无需确认、leader+follwers确认，按照应用场景选择
3. broker 挂了重新选举 leader 即可对外服务。

