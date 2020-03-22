# MySQL中的count(*)、count(1) 和 count(列)

> count(XXX)的计算逻辑是，查询结果中XXX不为null的行数
> 
> 可以认为是 select XXX from table_name ..... 这个查询语句的结果中，XXX不为null的记录数

那么，XXX 可以是列名、*(全部)、1(常量) 等等

[参考网络文章](https://www.cnblogs.com/careyson/p/differencebetweencountstarandcount1.html)


综上所述，我们对比count(*)、count(1) 和 count(列)这三种情况的区别，其实就是在判断 ***列名、*(全部)、1(常量)***这三种下，我们对比的是什么

* count(列A): 获取表中列A值不为空的记录数
* count(1)和count(*): 排除表中全部列都为null的记录之外的记录数，找出不是全部列都为null的列

count(1)和count(*) 找出不是全部列都为null的列, 也就是所有行（如果一行值全为NULL则该行相当于不存在）。那么最简单的执行办法是找一列NOT NULL的列，如果该列有索引，则使用该索引，当然，为了性能，SQL Server会选择最窄的索引以减少IO。


mysql之类的数据库性能关键还是在io上，所以提示性能的办法就是提升io效率，窄的索引对于每次读取固定大小的数据时，肯定能读取到更多的数据。

## count(1)和count(*)是选择主键索引还是二级索引呢？
nnodb的主键索引是聚簇索引（包含了KEY，除了KEY之外的其他字段值，事务ID和MVCC回滚指针）所以主键索引一定会比二级索引（包含KEY和对应的主键ID）大，也就是说在有二级索引的情况下，一般COUNT()都不会通过主键索引来统计行数，在有多个二级索引的情况下选择占用空间最小的。


* 只有主键，使用主键索引
* 有二级索引，使用二级索引
* 多个二级索引，使用最窄的索引列来判断





## 提升count效率的办法
如果某个表上Count（*)用的比较多时，考虑在一个最短的列建立一个单列索引，会极大的提升性能。


## 参考资料
[知乎知识](https://zhuanlan.zhihu.com/p/28397595)