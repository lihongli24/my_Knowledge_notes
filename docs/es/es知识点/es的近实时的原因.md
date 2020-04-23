# 为什么说es是近实时的
![](https://tva1.sinaimg.cn/large/007S8ZIlly1ge2vht0203j30jo0ccta2.jpg)

对于用户的请求来说，数据被写入index buffer之后就认为已经提交成功。


按照上面图里面说的，***当数据仅仅被写入index buffer之后是不会被用户搜索到的。***
这也就是es不能称为实时的原因.

> ***为什么会在index buffer和磁盘之间用到filesystem cache***
> 1. index buffer 写入可能比较频繁
> 2. 写入磁盘操作比较耗时，不能在每条数据写入index buffer之后就开始刷盘
> 3. 写入index buffer的数据定时刷新到 filesystem cache中，默认为1s
> 4. 写入filesystem cache中的数据，定期写入磁盘，这个时间可以稍微长一点。
> 5. 当index buffer的数据被写入filesystem cache之后就能被搜索到


> ***如何保证数据在写入磁盘前不丢失***
> * translog机制



## 网上的参考资料
[为什么说Elasticsearch搜索是近实时的？](https://cloud.tencent.com/developer/article/1122703)

