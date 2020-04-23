# es的translog机制

![es数据处理逻辑](https://tva1.sinaimg.cn/large/007S8ZIlly1ge2vht0203j30jo0ccta2.jpg)

里面有一个transLog的操作，
目的：数据被添加到index buffer之后，对于用户来说他的写入请求已经完成了，后续不应该出现数据丢失的情况。
但是如果这个时候，服务器挂了，index buffer(内存)中的数据会丢失，如果没有一个机制保障数据的安全性，那数据就会丢失，用户收到了写入成功，但是数据没了。***transLog***就是做这个事情的。
1. 数据写入index buffer的时候保证数据被写入transLog中,即使后面内存数据中的数据丢了，但是tranLog是一个文本类型的数据，数据不会丢失。
2. 等机器重启之后，会对已经写入tranLog,但是还没提交索引(写入索引成功)进行重放，具体内容看[ES学习笔记之-Translog实现机制的理解](https://blog.51cto.com/sbp810050504/2393306)中的chekpoint逻辑


> transLog也存在刷盘的时间间隔，不能完全保证数据不丢失。
## 网上参考数据
1. [ES学习笔记之-Translog实现机制的理解](https://blog.51cto.com/sbp810050504/2393306)