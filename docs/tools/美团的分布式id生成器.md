# 美团的分布式id生成器

[美团的分布式id生成器](https://tech.meituan.com/2019/03/07/open-source-project-leaf.html?utm_source=gank.io%2Fxiandu&utm_medium=website)
1. 在上个周期的数据使用完之前(文章里面说的是前一段数据使用到10%的时候)，异步去预取下一段。
2. 本地容灾:基于机器号是通过zk的方式获取的，所以对机器号要做本地备份，方式服务重启的时候zk刚好挂掉了的情况。

