# gc 日志查看
gc日志的查看
![](https://tva1.sinaimg.cn/large/0081Kckwgy1gk36ys52ewj30zl0k2n4k.jpg)

## 启动设置
JAVA进程启动的时候，设置gclog的参数
```java
-XX:+PrintGCDetails -XX:+PrintGCDateStamps  -Xloggc:/logs/gc-
-pmp-%t.log  -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/skyee/skyee-jars/logs
```
* -Xloggc:/logs/gc--pmp-%t.log  : 设置gc日志地址
* -XX:NumberOfGCLogFiles=5  最多5个gc文件
* -XX:GCLogFileSize=10m 一个gc文件大小
* -XX:+UseGCLogFileRotation 使用滚动方式记录日志，否则日志会很费资源

