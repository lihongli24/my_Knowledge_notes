# jvm调优

## 参数介绍

|   分代   | 参数   | 含义 |
| ---- | ---- | ---- |
|    整个堆  |  -Xms    | 初始堆大小，默认为物理内存的1/64(<1GB)|
|  | -Xmx| 最大堆大小，默认(MaxHeapFreeRatio参数可以调整)空余堆内存大于70%时，JVM会减少堆直到 -Xms的最小限制 |
| 新生代 | -XX:NewSize | 新生代空间大小初始值 |
| | -XX:MaxNewSize | 新生代空间大小最大值|
| | -Xmn | 新生代空间大小，此处的大小是(eden+2 survivor space) <br/>如果设置的情况下： -XX:newSize = -XX:MaxnewSize　=　-Xmn |
|直接内存| -XX：MaxDirectMemorySize||
| 栈 | -Xss | 栈大小 |
| 老年代 | |老年代的空间大小会根据新生代的大小隐式设定<br/> 初始值=-Xmx减去-XX:NewSize的值<br/>最小值=-Xmx值减去-XX:MaxNewSize的值|
| 元数据区 |-XX:MetaspaceSize | 元数据区最小值|
| | -XX:MaxMetaspaceSize | 元数据区最大值|

> 通常会将-Xms 与-Xmx两个参数的配置相同的值，其目的是为了能够在java垃圾回收机制清理完堆区后不需要重新分隔计算堆区的大小而浪费资源。

## 常用规律
| 空间 | 命令参数 | 建议扩大倍数 |
| ---- | ---- | ---- |
| java heap | -Xms和-Xmx | 3-4倍FullGC后的老年代空间占用|
| 新生代| -Xmn | 1-1.5倍FullGC之后的老年代空间占用|
| 老年代 || 2-3倍FullGC后的老年代空间占用|

## OOM dump设置
```
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/www/logs
```

## gc日志打印

```
-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/var/www/logs/gc-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10m
```
## 设置java 程序临时目录
```
-Djava.io.tmpdir=/var/www/tmp
```



## 线上启动命令


![](https://tva1.sinaimg.cn/large/006y8mN6ly1g9cuqp8o9bj31ko04kwg4.jpg)

```  
-Xmn1024m -Xms2500m -Xmx2500m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:MaxDirectMemorySize=256m -XX:+UseCMSInitiatingOccupancyOnly -XX:SurvivorRatio=8 -XX:+ExplicitGCInvokesConcurrent -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:-OmitStackTraceInFastThrow -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/var/www/logs/gc-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/www/logs -Djava.io.tmpdir=/var/www/tmp
```





## 什么情况下不需要调优

注意： 如果满足下面的指标，**则一般不需要进行 GC 优化：**

> MinorGC 执行时间不到50ms； Minor GC 执行不频繁，约10秒一次； Full GC 执行时间不到1s； Full GC 执行频率不算频繁，不低于10分钟1次。







