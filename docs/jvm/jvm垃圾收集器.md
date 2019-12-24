# [Java虚拟机垃圾回收(三) 7种垃圾收集器](https://www.cnblogs.com/cxxjohnson/p/8625713.html)

Java虚拟机垃圾回收(三) 7种垃圾收集器 

主要特点 应用场景 设置参数 基本运行原理

 

> ​    在[《Java虚拟机垃圾回收(一) 基础》](http://blog.csdn.net/tjiyu/article/details/53982412)中了解到如何判断对象是存活还是已经死亡？在[《Java虚拟机垃圾回收(二) 垃圾回收算法》](http://blog.csdn.net/tjiyu/article/details/53983064)了解到Java虚拟机垃圾回收的几种常见算法。
>
> ​    下面先来了解HotSpot虚拟机中的7种垃圾收集器：Serial、ParNew、Parallel Scavenge、Serial Old、Parallel Old、CMS、G1，先介绍一些垃圾收集的相关概念，再介绍它们的主要特点、应用场景、以及一些设置参数和基本运行原理。

## 1、垃圾收集器概述

> ​    垃圾收集器是垃圾回收算法（标记-清除算法、复制算法、标记-整理算法、火车算法）的具体实现，不同商家、不同版本的JVM所提供的垃圾收集器可能会有很在差别，本文主要介绍HotSpot虚拟机中的垃圾收集器。

### 1-1、垃圾收集器组合

> ​    JDK7/8后，HotSpot虚拟机所有收集器及组合（连线），如下图：

![img](jvm垃圾收集器.assets/20170102225015393.png)

> （A）、图中展示了7种不同分代的收集器：
>
> ​    Serial、ParNew、Parallel Scavenge、Serial Old、Parallel Old、CMS、G1；
>
> （B）、而它们所处区域，则表明其是属于新生代收集器还是老年代收集器：
>
>    新生代收集器：Serial、ParNew、Parallel Scavenge；
>
>    老年代收集器：Serial Old、Parallel Old、CMS；
>
>    整堆收集器：G1；
>
> （C）、两个收集器间有连线，表明它们可以搭配使用：
>
> ​    Serial/Serial Old、Serial/CMS、ParNew/Serial Old、ParNew/CMS、Parallel Scavenge/Serial Old、Parallel Scavenge/Parallel Old、G1；
>
> （D）、其中Serial Old作为CMS出现"Concurrent Mode Failure"失败的后备预案（后面介绍）；

### 1-2、并发垃圾收集和并行垃圾收集的区别

> （A）、并行（Parallel）
>
> ​    指多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态；
>
> ​    如ParNew、Parallel Scavenge、Parallel Old；
>
> （B）、并发（Concurrent）
>
> ​    指用户线程与垃圾收集线程同时执行（但不一定是并行的，可能会交替执行）；
>
>    用户程序在继续运行，而垃圾收集程序线程运行于另一个CPU上；  
>
> ​    如CMS、G1（也有并行）；

### 1-3、Minor GC和Full GC的区别

> （A）、Minor GC
>
> ​    又称新生代GC，指发生在新生代的垃圾收集动作；
>
> ​    因为Java对象大多是朝生夕灭，所以Minor GC非常频繁，一般回收速度也比较快；
>
> （B）、Full GC
>
> ​    又称Major GC或老年代GC，指发生在老年代的GC；
>
> ​    出现Full GC经常会伴随至少一次的Minor GC（不是绝对，Parallel Sacvenge收集器就可以选择设置Major GC策略）；
>
>    Major GC速度一般比Minor GC慢10倍以上；

​    

> 下面将介绍这些收集器的特性、基本原理和使用场景，并重点分析CMS和G1这两款相对复杂的收集器；但需要明确一个观点：
>
> ​    没有最好的收集器，更没有万能的收集；
>
>    选择的只能是适合具体应用场景的收集器。

## 2、Serial收集器

> ​    Serial（串行）垃圾收集器是最基本、发展历史最悠久的收集器；
>
> ​    JDK1.3.1前是HotSpot新生代收集的唯一选择；
>
> 1、特点
>
>    针对新生代；
>
>    采用复制算法；
>
>    单线程收集；
>
> ​    进行垃圾收集时，必须暂停所有工作线程，直到完成；      
>
> ​    即会"Stop The World"；
>
>    Serial/Serial Old组合收集器运行示意图如下：

![img](jvm垃圾收集器.assets/20170102225015841.png)

> 2、应用场景
>
>    依然是HotSpot在Client模式下默认的新生代收集器；
>
>    也有优于其他收集器的地方：
>
> >    简单高效（与其他收集器的单线程相比）；
> >
> >    对于限定单个CPU的环境来说，Serial收集器没有线程交互（切换）开销，可以获得最高的单线程收集效率；
> >
> >    在用户的桌面应用场景中，可用内存一般不大（几十M至一两百M），可以在较短时间内完成垃圾收集（几十MS至一百多MS）,只要不频繁发生，这是可以接受的
>
> 3、设置参数
>
>    "-XX:+UseSerialGC"：添加该参数来显式的使用串行垃圾收集器；
>
> 4、Stop TheWorld说明
>
>    JVM在后台自动发起和自动完成的，在用户不可见的情况下，把用户正常的工作线程全部停掉，即GC停顿；
>
>    会带给用户不良的体验；
>
> >    从JDK1.3到现在，从Serial收集器-》Parallel收集器-》CMS-》G1，用户线程停顿时间不断缩短，但仍然无法完全消除；
>
>    更多"Stop The World"信息请参考：[《Java虚拟机垃圾回收(一) 基础》](http://blog.csdn.net/tjiyu/article/details/53982412)"2-2、可达性分析算法"
>
> 更多Serial收集器请参考：
>
>    《Memory Management in the Java HotSpot™ Virtual Machine》 4.3节 Serial Collector（内存管理白皮书）：http://www.oracle.com/technetwork/java/javase/tech/memorymanagement-whitepaper-1-150020.pdf
>
>    《Java Platform, Standard Edition HotSpot Virtual Machine Garbage Collection Tuning Guide》 第5节 Available Collectors（官方的垃圾收集调优指南）：http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/collectors.html#sthref27

## 3、ParNew收集器

>    ParNew垃圾收集器是Serial收集器的多线程版本。
>
> 1、特点
>
>    除了多线程外，其余的行为、特点和Serial收集器一样；
>
>    如Serial收集器可用控制参数、收集算法、Stop The World、内存分配规则、回收策略等；
>
>    两个收集器共用了不少代码；
>
>    ParNew/Serial Old组合收集器运行示意图如下：

![img](jvm垃圾收集器.assets/20170102225016331.png)

> 2、应用场景
>
>    在Server模式下，ParNew收集器是一个非常重要的收集器，因为除Serial外，目前只有它能与CMS收集器配合工作；
>
>    但在单个CPU环境中，不会比Serail收集器有更好的效果，因为存在线程交互开销。
>
> 3、设置参数
>
>    "-XX:+UseConcMarkSweepGC"：指定使用CMS后，会默认使用ParNew作为新生代收集器；
>
>    "-XX:+UseParNewGC"：强制指定使用ParNew；  
>
>    "-XX:ParallelGCThreads"：指定垃圾收集的线程数量，ParNew默认开启的收集线程与CPU的数量相同；
>
> 4、为什么只有ParNew能与CMS收集器配合
>
>    CMS是HotSpot在JDK1.5推出的第一款真正意义上的并发（Concurrent）收集器，第一次实现了让垃圾收集线程与用户线程（基本上）同时工作；
>
>    CMS作为老年代收集器，但却无法与JDK1.4已经存在的新生代收集器Parallel Scavenge配合工作；
>
>    因为Parallel Scavenge（以及G1）都没有使用传统的GC收集器代码框架，而另外独立实现；而其余几种收集器则共用了部分的框架代码；
>
>    关于CMS收集器后面会详细介绍。

## 4、Parallel Scavenge收集器

>    Parallel Scavenge垃圾收集器因为与吞吐量关系密切，也称为吞吐量收集器（Throughput Collector）。
>
> 1、特点
>
> > （A）、有一些特点与ParNew收集器相似
> >
> >    新生代收集器；
> >
> >    采用复制算法；
> >
> >    多线程收集；
> >
> > （B）、主要特点是：它的关注点与其他收集器不同
> >
> >    CMS等收集器的关注点是尽可能地缩短垃圾收集时用户线程的停顿时间；
> >
> >    而Parallel Scavenge收集器的目标则是达一个可控制的吞吐量（Throughput）；
> >
> >    关于吞吐量与收集器关注点说明详见本节后面；

> 2、应用场景
>
>    高吞吐量为目标，即减少垃圾收集时间，让用户代码获得更长的运行时间；
>
>    当应用程序运行在具有多个CPU上，对暂停时间没有特别高的要求时，即程序主要在后台进行计算，而不需要与用户进行太多交互；
>
>    例如，那些执行批量处理、订单处理、工资支付、科学计算的应用程序；
>
> 3、设置参数
>
>    Parallel Scavenge收集器提供两个参数用于精确控制吞吐量：
>
> > （A）、"-XX:MaxGCPauseMillis"
> >
> >    控制最大垃圾收集停顿时间，大于0的毫秒数；
> >
> >    MaxGCPauseMillis设置得稍小，停顿时间可能会缩短，但也可能会使得吞吐量下降；
> >
> >    因为可能导致垃圾收集发生得更频繁；
> >
> > （B）、"-XX:GCTimeRatio"
> >
> >    设置垃圾收集时间占总时间的比率，0<n<100的整数；
> >
> >    GCTimeRatio相当于设置吞吐量大小；
> >
> >    垃圾收集执行时间占应用程序执行时间的比例的计算方法是：
> >
> > >    1 / (1 + n)
> > >
> > >    例如，选项-XX:GCTimeRatio=19，设置了垃圾收集时间占总时间的5%--1/(1+19)；
> >
> >    默认值是1%--1/(1+99)，即n=99；

> > 垃圾收集所花费的时间是年轻一代和老年代收集的总时间；
> >
> > 如果没有满足吞吐量目标，则增加代的内存大小以尽量增加用户程序运行的时间；
>
>    此外，还有一个值得关注的参数：

> > （C）、"-XX:+UseAdptiveSizePolicy"
> >
> >    开启这个参数后，就不用手工指定一些细节参数，如：
> >
> > >    新生代的大小（-Xmn）、Eden与Survivor区的比例（-XX:SurvivorRation）、晋升老年代的对象年龄（-XX:PretenureSizeThreshold）等；
> >
> >    JVM会根据当前系统运行情况收集性能监控信息，动态调整这些参数，以提供最合适的停顿时间或最大的吞吐量，这种调节方式称为GC自适应的调节策略（GC Ergonomiscs）；  

> >    这是一种值得推荐的方式：
> >
> > >    (1)、只需设置好内存数据大小（如"-Xmx"设置最大堆）；
> > >
> > >    (2)、然后使用"-XX:MaxGCPauseMillis"或"-XX:GCTimeRatio"给JVM设置一个优化目标；
> > >
> > >    (3)、那些具体细节参数的调节就由JVM自适应完成；    
> >
> >    这也是Parallel Scavenge收集器与ParNew收集器一个重要区别；  
>
>    更多目标调优和GC自适应的调节策略说明请参考：      

>    《Memory Management in the Java HotSpot™ Virtual Machine》 5节 Ergonomics -- Automatic Selections and Behavior Tuning：http://www.oracle.com/technetwork/java/javase/tech/memorymanagement-whitepaper-1-150020.pdf
>
>    《Java Platform, Standard Edition HotSpot Virtual Machine Garbage Collection Tuning Guide》 第2节 Ergonomics：[http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/ergonomics.html#ergonomics](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/ergonomics.html%23ergonomics)

> 4、吞吐量与收集器关注点说明

> > （A）、吞吐量（Throughput）
> >
> >    CPU用于运行用户代码的时间与CPU总消耗时间的比值；
> >
> >    即吞吐量=运行用户代码时间/（运行用户代码时间+垃圾收集时间）；  
> >
> >    高吞吐量即减少垃圾收集时间，让用户代码获得更长的运行时间；
> >
> > （B）、垃圾收集器期望的目标（关注点）
> >
> > > （1）、停顿时间  
> > >
> > >    停顿时间越短就适合需要与用户交互的程序；
> > >
> > >    良好的响应速度能提升用户体验；
> > >
> > > （2）、吞吐量
> > >
> > >    高吞吐量则可以高效率地利用CPU时间，尽快完成运算的任务；
> > >
> > >    主要适合在后台计算而不需要太多交互的任务；
> > >
> > > （3）、覆盖区（Footprint）
> > >
> > >    在达到前面两个目标的情况下，尽量减少堆的内存空间；
> > >
> > >    可以获得更好的空间局部性；
>
> 更多Parallel Scavenge收集器的信息请参考：
>
>    官方的垃圾收集调优指南 第6节：[http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/parallel.html#parallel_collector](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/parallel.html%23parallel_collector)
>
> [ ](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/parallel.html%23parallel_collector)
>
> 上面介绍的都是新生代收集器，接下来开始介绍老年代收集器；[
> ](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/parallel.html%23parallel_collector)

## 5、Serial Old收集器

>    Serial Old是 Serial收集器的老年代版本；
>
> 1、特点
>
>    针对老年代；
>
>    采用"标记-整理"算法（还有压缩，Mark-Sweep-Compact）；
>
>    单线程收集；
>
>    Serial/Serial Old收集器运行示意图如下：

![img](jvm垃圾收集器.assets/20170102225016763.png)

> 2、应用场景
>
>    主要用于Client模式；
>
>    而在Server模式有两大用途：
>
> >    （A）、在JDK1.5及之前，与Parallel Scavenge收集器搭配使用（JDK1.6有Parallel Old收集器可搭配）；
> >
> >    （B）、作为CMS收集器的后备预案，在并发收集发生Concurrent Mode Failure时使用（后面详解）；
>
> 更多Serial Old收集器信息请参考：
>
>    内存管理白皮书 4.3.2节：http://www.oracle.com/technetwork/java/javase/tech/memorymanagement-whitepaper-1-150020.pdf

## 6、Parallel Old收集器

>    Parallel Old垃圾收集器是Parallel Scavenge收集器的老年代版本；
>
>    JDK1.6中才开始提供；
>
> 1、特点
>
>    针对老年代；
>
>    采用"标记-整理"算法；
>
>    多线程收集；
>
>    Parallel Scavenge/Parallel Old收集器运行示意图如下：

![img](jvm垃圾收集器.assets/20170102225017065.png)

> 2、应用场景
>
>    JDK1.6及之后用来代替老年代的Serial Old收集器；
>
>    特别是在Server模式，多CPU的情况下；
>
>    这样在注重吞吐量以及CPU资源敏感的场景，就有了Parallel Scavenge加Parallel Old收集器的"给力"应用组合；
>
> 3、设置参数
>
>    "-XX:+UseParallelOldGC"：指定使用Parallel Old收集器；
>
> 更多Parallel Old收集器收集过程介绍请参考：
>
>    《内存管理白皮书》 4.5.2节：  [  http://www.oracle.com/technetwork/java/javase/tech/memorymanagement-whitepaper-1-150020.pdf](http://blog.csdn.net/tjiyu/article/details/53983650)

## 7、CMS收集器

>    并发标记清理（Concurrent Mark Sweep，CMS）收集器也称为并发低停顿收集器（Concurrent Low Pause Collector）或低延迟（low-latency）垃圾收集器；
>
>    在前面ParNew收集器曾简单介绍过其特点；
>
> 1、特点
>
>    针对老年代；
>
>    基于"标记-清除"算法(不进行压缩操作，产生内存碎片)；      
>
>    以获取最短回收停顿时间为目标；
>
>    并发收集、低停顿；
>
>    需要更多的内存（看后面的缺点）；
>
> ​      
>
>    是HotSpot在JDK1.5推出的第一款真正意义上的并发（Concurrent）收集器；
>
>    第一次实现了让垃圾收集线程与用户线程（基本上）同时工作；
>
> 2、应用场景
>
>    与用户交互较多的场景；    
>
>    希望系统停顿时间最短，注重服务的响应速度；
>
>    以给用户带来较好的体验；
>
>    如常见WEB、B/S系统的服务器上的应用；

> 3、设置参数
>
>    "-XX:+UseConcMarkSweepGC"：指定使用CMS收集器；
>
> 4、CMS收集器运作过程
>
>    比前面几种收集器更复杂，可以分为4个步骤:

> > （A）、初始标记（CMS initial mark）
> >
> >    仅标记一下GC Roots能直接关联到的对象；
> >
> >    速度很快；
> >
> >    但需要"Stop The World"；
> >
> > （B）、并发标记（CMS concurrent mark）
> >
> >    进行GC Roots Tracing的过程；
> >
> >    刚才产生的集合中标记出存活对象；
> >
> >    应用程序也在运行；
> >
> >    并不能保证可以标记出所有的存活对象；
> >
> > （C）、重新标记（CMS remark）
> >
> >    为了修正并发标记期间因用户程序继续运作而导致标记变动的那一部分对象的标记记录；
> >
> >    需要"Stop The World"，且停顿时间比初始标记稍长，但远比并发标记短；
> >
> >    采用多线程并行执行来提升效率；
> >
> > （D）、并发清除（CMS concurrent sweep）
> >
> >    回收所有的垃圾对象；

>    整个过程中耗时最长的并发标记和并发清除都可以与用户线程一起工作；
>
>    所以总体上说，CMS收集器的内存回收过程与用户线程一起并发执行；
>
>    CMS收集器运行示意图如下：

![img](jvm垃圾收集器.assets/20170102225017372.png)

 

​    5、CMS收集器3个明显的缺点

​           （A）、对CPU资源非常敏感

> >    并发收集虽然不会暂停用户线程，但因为占用一部分CPU资源，还是会导致应用程序变慢，总吞吐量降低。
> >
> >    CMS的默认收集线程数量是=(CPU数量+3)/4；
> >
> >    当CPU数量多于4个，收集线程占用的CPU资源多于25%，对用户程序影响可能较大；不足4个时，影响更大，可能无法接受。
> >
> >  
> >
> >    增量式并发收集器：
> >
> > >    针对这种情况，曾出现了"增量式并发收集器"（Incremental Concurrent Mark Sweep/i-CMS）；
> > >
> > >    类似使用抢占式来模拟多任务机制的思想，让收集线程和用户线程交替运行，减少收集线程运行时间；
> > >
> > >    但效果并不理想，JDK1.6后就官方不再提倡用户使用。
> > >
> > > 更多请参考：
> > >
> > >    官方的《垃圾收集调优指南》8.8节 Incremental Mode：[http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/cms.html#CJAGIIEJ](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/cms.html%23CJAGIIEJ)
> > >
> > >    《内存管理白皮书》 4.6.3节可以看到一些描述；

> > （B）、无法处理浮动垃圾,可能出现"Concurrent Mode Failure"失败

> > > （1）、浮动垃圾（Floating Garbage）
> > >
> > >    在并发清除时，用户线程新产生的垃圾，称为浮动垃圾；
> > >
> > >    这使得并发清除时需要预留一定的内存空间，不能像其他收集器在老年代几乎填满再进行收集；
> > >
> > >    也要可以认为CMS所需要的空间比其他垃圾收集器大；
> > >
> > >    "-XX:CMSInitiatingOccupancyFraction"：设置CMS预留内存空间；
> > >
> > >    JDK1.5默认值为68%；
> > >
> > >    JDK1.6变为大约92%；        

> > > （2）、"Concurrent Mode Failure"失败
> > >
> > >    如果CMS预留内存空间无法满足程序需要，就会出现一次"Concurrent Mode Failure"失败；
> > >
> > >    这时JVM启用后备预案：临时启用Serail Old收集器，而导致另一次Full GC的产生；
> > >
> > >    这样的代价是很大的，所以CMSInitiatingOccupancyFraction不能设置得太大。

> > （C）、产生大量内存碎片
> >
> >    由于CMS基于"标记-清除"算法，清除后不进行压缩操作；
> >
> >    前面[《Java虚拟机垃圾回收(二) 垃圾回收算法》](http://blog.csdn.net/tjiyu/article/details/53983064)"标记-清除"算法介绍时曾说过：
> >
> > >    产生大量不连续的内存碎片会导致分配大内存对象时，无法找到足够的连续内存，从而需要提前触发另一次Full GC动作。
> >
> >    解决方法：        
> >
> > > （1）、"-XX:+UseCMSCompactAtFullCollection"
> > >
> > >    使得CMS出现上面这种情况时不进行Full GC，而开启内存碎片的合并整理过程；
> > >
> > >    但合并整理过程无法并发，停顿时间会变长；
> > >
> > >    默认开启（但不会进行，结合下面的CMSFullGCsBeforeCompaction）；
> > >
> > > （2）、"-XX:+CMSFullGCsBeforeCompaction"
> > >
> > >    设置执行多少次不压缩的Full GC后，来一次压缩整理；
> > >
> > >    为减少合并整理过程的停顿时间；
> > >
> > >    默认为0，也就是说每次都执行Full GC，不会进行压缩整理；

> >    由于空间不再连续，CMS需要使用可用"空闲列表"内存分配方式，这比简单实用"碰撞指针"分配内存消耗大；
> >
> >    更多关于内存分配方式请参考：《[Java对象在Java虚拟机中的创建过程](http://blog.csdn.net/tjiyu/article/details/53923392)》
>
>    总体来看，与Parallel Old垃圾收集器相比，CMS减少了执行老年代垃圾收集时应用暂停的时间；
>
>    但却增加了新生代垃圾收集时应用暂停的时间、降低了吞吐量而且需要占用更大的堆空间；

> 更多CMS收集器信息请参考：
>
>    《垃圾收集调优指南》 8节 Concurrent Mark Sweep (CMS) Collector：[http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/cms.html#concurrent_mark_sweep_cms_collector](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/cms.html%23concurrent_mark_sweep_cms_collector)
>
>    《内存管理白皮书》 4.6节 Concurrent Mark-Sweep (CMS) Collector：http://www.oracle.com/technetwork/java/javase/tech/memorymanagement-whitepaper-1-150020.pdf

## 8、G1收集器

>    G1（Garbage-First）是JDK7-u4才推出商用的收集器；
>
> 1、特点

> > （A）、并行与并发
> >
> >    能充分利用多CPU、多核环境下的硬件优势；
> >
> >    可以并行来缩短"Stop The World"停顿时间；
> >
> >    也可以并发让垃圾收集与用户程序同时进行；
> >
> > （B）、分代收集，收集范围包括新生代和老年代  
> >
> >    能独立管理整个GC堆（新生代和老年代），而不需要与其他收集器搭配；
> >
> >    能够采用不同方式处理不同时期的对象；
> >
> > ​        
> >
> >    虽然保留分代概念，但Java堆的内存布局有很大差别；
> >
> >    将整个堆划分为多个大小相等的独立区域（Region）；
> >
> >    新生代和老年代不再是物理隔离，它们都是一部分Region（不需要连续）的集合；
> >
> >    更多G1内存布局信息请参考：
> >
> > >    《垃圾收集调优指南》 9节：[http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/g1_gc.html#garbage_first_garbage_collection](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/g1_gc.html%23garbage_first_garbage_collection)

> > （C）、结合多种垃圾收集算法，空间整合，不产生碎片
> >
> >    从整体看，是基于标记-整理算法；
> >
> >    从局部（两个Region间）看，是基于复制算法；
> >
> >    这是一种类似火车算法的实现；
> >
> >  
> >
> >    都不会产生内存碎片，有利于长时间运行；
> >
> > （D）、可预测的停顿：低停顿的同时实现高吞吐量
> >
> >    G1除了追求低停顿处，还能建立可预测的停顿时间模型；
> >
> >    可以明确指定M毫秒时间片内，垃圾收集消耗的时间不超过N毫秒；

> 2、应用场景
>
>    面向服务端应用，针对具有大内存、多处理器的机器；
>
>    最主要的应用是为需要低GC延迟，并具有大堆的应用程序提供解决方案；
>
>    如：在堆大小约6GB或更大时，可预测的暂停时间可以低于0.5秒；
>
> ​      
>
>    用来替换掉JDK1.5中的CMS收集器；
>
>    在下面的情况时，使用G1可能比CMS好：
>
> >    （1）、超过50％的Java堆被活动数据占用；
> >
> >    （2）、对象分配频率或年代提升频率变化很大；
> >
> >    （3）、GC停顿时间过长（长于0.5至1秒）。

>    是否一定采用G1呢？也未必：
>
> >    如果现在采用的收集器没有出现问题，不用急着去选择G1；
> >
> >    如果应用程序追求低停顿，可以尝试选择G1；
> >
> >    是否代替CMS需要实际场景测试才知道。

> 3、设置参数
>
>    "-XX:+UseG1GC"：指定使用G1收集器；
>
>    "-XX:InitiatingHeapOccupancyPercent"：当整个Java堆的占用率达到参数值时，开始并发标记阶段；默认为45；
>
>    "-XX:MaxGCPauseMillis"：为G1设置暂停时间目标，默认值为200毫秒；
>
>    "-XX:G1HeapRegionSize"：设置每个Region大小，范围1MB到32MB；目标是在最小Java堆时可以拥有约2048个Region；
>
>    更多关于G1参数设置请参考：
>
> >    《垃圾收集调优指南》 10.5节：[http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/g1_gc_tuning.html#important_defaults](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/g1_gc_tuning.html%23important_defaults)
>
> 4、为什么G1收集器可以实现可预测的停顿
>
>    G1可以建立可预测的停顿时间模型，是因为：

> >    可以有计划地避免在Java堆的进行全区域的垃圾收集；
> >
> >    G1跟踪各个Region获得其收集价值大小，在后台维护一个优先列表；
> >
> >    每次根据允许的收集时间，优先回收价值最大的Region（名称Garbage-First的由来）；
> >
> >    这就保证了在有限的时间内可以获取尽可能高的收集效率；

> 5、一个对象被不同区域引用的问题
>
>    一个Region不可能是孤立的，一个Region中的对象可能被其他任意Region中对象引用，判断对象存活时，是否需要扫描整个Java堆才能保证准确？
>
>    在其他的分代收集器，也存在这样的问题（而G1更突出）：
>
> >    回收新生代也不得不同时扫描老年代？
>
>    这样的话会降低Minor GC的效率；
>
>    解决方法：

> >    无论G1还是其他分代收集器，JVM都是使用Remembered Set来避免全局扫描：
> >
> > >    每个Region都有一个对应的Remembered Set；
> > >
> > >    每次Reference类型数据写操作时，都会产生一个Write Barrier暂时中断操作；
> > >
> > >    然后检查将要写入的引用指向的对象是否和该Reference类型数据在不同的Region（其他收集器：检查老年代对象是否引用了新生代对象）；
> > >
> > >    如果不同，通过CardTable把相关引用信息记录到引用指向对象的所在Region对应的Remembered Set中；
> > >
> > > ​          
> >
> > >    当进行垃圾收集时，在GC根节点的枚举范围加入Remembered Set；
> > >
> > >    就可以保证不进行全局扫描，也不会有遗漏。

> 6、G1收集器运作过程
>
>    不计算维护Remembered Set的操作，可以分为4个步骤（与CMS较为相似）。

> > （A）、初始标记（Initial Marking）
> >
> >    仅标记一下GC Roots能直接关联到的对象；
> >
> >    且修改TAMS（Next Top at Mark Start）,让下一阶段并发运行时，用户程序能在正确可用的Region中创建新对象；
> >
> >    需要"Stop The World"，但速度很快；
> >
> > （B）、并发标记（Concurrent Marking）
> >
> >    进行GC Roots Tracing的过程；
> >
> >    刚才产生的集合中标记出存活对象；
> >
> >    耗时较长，但应用程序也在运行；
> >
> >    并不能保证可以标记出所有的存活对象；
> >
> > （C）、最终标记（Final Marking）
> >
> >    为了修正并发标记期间因用户程序继续运作而导致标记变动的那一部分对象的标记记录；
> >
> >    上一阶段对象的变化记录在线程的Remembered Set Log；
> >
> >    这里把Remembered Set Log合并到Remembered Set中；
> >
> > ​          
> >
> >    需要"Stop The World"，且停顿时间比初始标记稍长，但远比并发标记短；
> >
> >    采用多线程并行执行来提升效率；
> >
> > （D）、筛选回收（Live Data Counting and Evacuation）
> >
> >    首先排序各个Region的回收价值和成本；
> >
> >    然后根据用户期望的GC停顿时间来制定回收计划；
> >
> >    最后按计划回收一些价值高的Region中垃圾对象；
> >
> > ​          
> >
> >    回收时采用"复制"算法，从一个或多个Region复制存活对象到堆上的另一个空的Region，并且在此过程中压缩和释放内存；
> >
> >    可以并发进行，降低停顿时间，并增加吞吐量；

>    G1收集器运行示意图如下：

![img](jvm垃圾收集器.assets/20170102225017799.png)

> 更多G1收集器信息请参考：
>
>    《垃圾收集调优指南》 9节 Garbage-First Garbage Collector：[http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/g1_gc.html#garbage_first_garbage_collection](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/g1_gc.html%23garbage_first_garbage_collection)
>
>    《垃圾收集调优指南》 10节 Garbage-First Garbage Collector Tuning：[http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/g1_gc_tuning.html#g1_gc_tuning](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/g1_gc_tuning.html%23g1_gc_tuning)

​    

>    到这里，我们大体了解HotSpot虚拟机中的所有垃圾收集器，后面我们将去了解JVM的一些内存分配与回收策略、JVM垃圾收集相关调优方法……

 

【参考资料】

1、《编译原理》第二版 第7章

2、《深入理解Java虚拟机：JVM高级特性与最佳实践》第二版 第3章

3、《The Java Virtual Machine Specification》Java SE 8 Edition：https://docs.oracle.com/javase/specs/jvms/se8/html/index.html

4、《Java Platform, Standard Edition HotSpot Virtual Machine Garbage Collection Tuning Guide》：http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/index.html

5、《Memory Management in the Java HotSpot™ Virtual Machine》：http://www.oracle.com/technetwork/java/javase/tech/memorymanagement-whitepaper-1-150020.pdf

6、HotSpot虚拟机参数官方说明：http://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html

7、《Thinking in Java》第四版 5.5 清理：终结处理和垃圾回收；

分类: [JAVA-JVM](https://www.cnblogs.com/cxxjohnson/category/1218845.html)

[一步步讲解g1](https://blog.csdn.net/j3T9Z7H/article/details/80074460)

[cms和g1的比较](https://www.jianshu.com/p/bdd6f03923d1)