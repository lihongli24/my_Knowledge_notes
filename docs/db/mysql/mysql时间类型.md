# mysql中的 time、datetime和timestamp

## 验证流程（直接照搬了参考资料里面的内容）

#### 建标

```sql
CREATE TABLE `ee` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date1` datetime(6) DEFAULT NULL,
  `date2` time(2) DEFAULT NULL,
  `date3` timestamp(3) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
```
#### 数据插入
```sql
INSERT INTO iris.ee(date1,date2,date3) VALUES(NOW(),NOW(),NOW());
```

#### 首次查询
```sql
select * from iris.ee;
+----+----------------------------+-------------+-------------------------+
| id | date1                      | date2       | date3                   |
+----+----------------------------+-------------+-------------------------+
|  1 | 2018-12-07 09:55:52.000000 | 09:55:52.00 | 2018-12-07 09:55:52.000 |
+----+----------------------------+-------------+-------------------------+

```
#### 修改时区
```sql
set time_zone='+8:00';
```
#### 再次插入
```sql
INSERT INTO iris.ee(date1,date2,date3) VALUES(NOW(),NOW(),NOW());
```
#### 再次查询

```sql
select * from iris.ee;
+----+----------------------------+-------------+-------------------------+
| id | date1                      | date2       | date3                   |
+----+----------------------------+-------------+-------------------------+
|  1 | 2018-12-07 09:55:52.000000 | 09:55:52.00 | 2018-12-07 17:55:52.000 |
|  2 | 2018-12-07 17:57:29.000000 | 17:57:29.00 | 2018-12-07 17:57:29.000 | 
```



> 两次插入，相隔时间差了两分钟，但是因为***datetime***和***time***不具有时区的概念，导致切换时区之后，查询出来的时间，相差了很多


## timestamp和datetime、time的区别
* ***timestamp具有时区性***

* datetime取值范围：0000-00-00 00:00:00 ~ 9999-12-31 23:59:59；

  timestamp取值范围：1970-01-01 08:00:01！2038-01-19 11:14:07 




## 参考资料

[csdn上对时间类型的介绍](https://blog.csdn.net/iris_xuting/article/details/84886483)