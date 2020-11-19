# system IO学习笔记

[toc]

本文内容是linux 系统io的知识，结合java的知识，让我对java的io知识更加清楚



## linux 中一切皆文件

在linux 中一切都可以归纳为文件，无论是文件、字符设备、或者网络请求，对于linux来说都是文件

## linux 中文件类型

通过命令可以看到当前目录下的所有文件

```shell
ls -al                                                                                                                                                          
total 1991456
drwxr-xr-x+  79 lihongli24  staff        2528 11 17 23:07 .
drwxr-xr-x    5 root        admin         160 11 28  2019 ..
-r--------    1 lihongli24  staff           9 12 11  2019 .CFUserTextEncoding
-rw-r--r--@   1 lihongli24  staff       14340 11 17 18:32 .DS_Store
drwxr-xr-x    5 lihongli24  staff         160 11 28  2019 .ShadowsocksX-NG
drwx------  198 lihongli24  staff        6336 11 10 15:05 .Trash
drwxr-xr-x    3 lihongli24  staff          96 11 28  2019 .android
```

* d: 目录
* l: 连接
* b: 块设备，可以随机访问的设备，如磁盘等
* c: 字符设备（CHR）， 如果只能按照字符流的方式访问，不能随机访问的话，就属于字符设备
* s: socket,
* p: pipeline   管道 |





## linux 中的文件描述符

```shell
lsof -op $$   
COMMAND  PID       USER   FD   TYPE DEVICE    OFFSET        NODE NAME
......
.....
zsh     1306 lihongli24  txt    REG    1,4    1100896 12885600033 /usr/lib/dyld
zsh     1306 lihongli24    0u   CHR   16,2 0t22824432         649 /dev/ttys002
zsh     1306 lihongli24    1u   CHR   16,2 0t22824432         649 /dev/ttys002
zsh     1306 lihongli24    2u   CHR   16,2 0t22824432         649 /dev/ttys002
zsh     1306 lihongli24   10u   CHR   16,2    0t57306         649 /dev/ttys002
zsh     1306 lihongli24   11u   REG    1,4          0 12902898052 /private/var/folders/_f/7k3572m14y50tcf_4t9r43s00000gp/T/gitstatus.1306.lock.mbk30hhm09
zsh     1306 lihongli24   12w  FIFO            0t3517 12902898054 /private/var/folders/_f/7k3572m14y50tcf_4t9r43s00000gp/T/gitstatus.1306.pipe.req.ZneR03yJUK
zsh     1306 lihongli24   13r  FIFO            0t3461 12902898056 /private/var/folders/_f/7k3572m14y50tcf_4t9r43s00000gp/T/gitstatus.1306.pipe.resp.PbBc24IH9V
```



* $$ 当前执行bash 的进程

* FD： 当前进程使用的文件描述符

* 所有程序都有文件描述符

  * 0： 标准输入
  * 1：标准输出
  * 2：错误输出

* OFFSET： 当前进程对文件描述符的操作偏移量

  

### 文件描述符的使用

1. 对标准输出文件描述符的使用

   ```shell
   lsof -op $$ 1> aa.txt
   ```

   原本 lsof -op $$ 的命令执行完了之后，内容会打印到终端，加上 1> aa.txt,就是说将标准输出指向了文件aa.txt文件。

   ![image-20201117234249078](https://tva1.sinaimg.cn/large/0081Kckwgy1gkslm5jwglj31o60j80zc.jpg)



2. 对标准输入文件描述符的使用

   ![image-20201117234438676](https://tva1.sinaimg.cn/large/0081Kckwgy1gkslu61iu3j311s060js8.jpg)

   * read是一个方法，他会读取直到输入端进行回车操作。

   >  上面 ```read cc```命令会等到终端输入asd之后，回车将会讲asd的值存入cc中
   >
   > 如果使用标准输入的文件描述符0，重定向，使用aa.txt来作为输入，把值赋给aa
   >
   > aa的内容为aa.txt中的第一行数据，因为read是以换行为结束的。

   

## 管道 |

管道的概念就是，将前面命令的输出作为后面命令的输入

![image-20201117235031297](https://tva1.sinaimg.cn/large/0081Kckwgy1gkslu3w3vuj31ty0ngn62.jpg)

还是刚才的aa.txt文件，如果我想查看第10行，怎么查询

![image-20201117235154019](https://tva1.sinaimg.cn/large/0081Kckwgy1gkslvjqwe9j313201sq3a.jpg)



可以使用命令```head -10 aa.txt | tail -1```

1. head -10 获取aa.txt的前10行
2. tail -1 获取前面命令输出中的最后一行





## page cache

page cache 是内核维护的，用来提高进程读写效率的一种机制。

1. 进程调用write，数据会被写入page cache, 如果刷盘策略这个时候如果没有达到的情况下，数据只会存在于linux 内核 kernal维护的这个page cache 中，如果服务器断电了，page cache中的数据会丢失。
2. 程序被加载入内存，也是按照page为单位来加载的。不会一整个程序全都加载到内存中。
3. 一个page的大小为 4KB
4. 内核来维护pagecache的好处
   1. 提高了读写的消息,数据不用每次都写入磁盘，减少了磁盘的调用。
   2. 多个进程访问同一份数据，只要同一个pagecache就行了
5. 可能存在的问题：刷盘策略设置的不对，丢的数据会比较多。

![](https://tva1.sinaimg.cn/large/0081Kckwgy1gksm3w8m32j31960l6dlw.jpg)



## 脏页和内存淘汰

对于一个pagecache, 如果app对其进行了操作(create、update)，对应的页会被标记为dirty。

* 刷盘策略： 可以配置间隔时间、可以配置脏页占用内存大小。达到某个条件时，操作系统会把脏页中的数据，刷新到磁盘上去。
* 内存淘汰：对于被缓存在page cache中的数据达到程度之后，新载入的数据没有空间存放，会通过LRU算法将最近使用最少使用的数据淘汰出内存。如果这个需要被淘汰的page cache是dirty的话，必须进行同步到磁盘的操作。



![](https://tva1.sinaimg.cn/large/0081Kckwgy1gksm6oh04xj31d20mkdjt.jpg)

## 缺页中断

![image-20201118000337471](https://tva1.sinaimg.cn/large/0081Kckwgy1gksm7prbrqj31o20fkq8t.jpg)

当进程读取数据到当前不在page cache中的数据，就会发出缺页中断，由dma（协处理器)将page内容加载的page cache中缓存起来，再由dma发起中断，通知cpu文件加载完成，让cpu执行原先缺页的进程。

可以认为是一种通知cpu处理某种事情的机制，否则cpu无法处理多件事情。

## java的nio

nio其实由两种

1. 一种是操作系统的io方式，非阻塞io
2. 另外一种是java层面的，new io,新的io处理方式

这里先讲一下关于java nio的概念。

![image-20201118233816812](https://tva1.sinaimg.cn/large/0081Kckwgy1gktr3oqfhbj31iy0u0n9u.jpg)

在上面的图中，描述了一个jvm进程和内核占用的内存情况。

* 图中的heap有两个，一个是整个jvm进程的heap,还有一个是jvm进程内，我们熟悉的 jvm堆

  



java中使用文件io的方式主要有下面几种

1. 直接获取输出流

```java
File file = new File(path);
FileOutputStream out = new FileOutputStream(file);
while(true){
  Thread.sleep(10);
  out.write(data);

}
```

> 每次write都会触发系统调用syscall，往内核维护的page cache 中写如。
>
> 因为不断的进行syscall，在用户态和内核态中切换，所以效率会低

2. 使用BufferedOutputStream

```java
 File file = new File(path);
BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
while(true){
  Thread.sleep(10);
  out.write(data);
}
```

> ```
> jvm 会维护一个 8kB数组，到了8k在进行系统调用   syscall  write(8KBbyte[])
> 
> 这个时候写入的也是page cache
> 
> 但是会把数据攒一会，再去调用 系统调用，所以频率没有上面那个那么高，效率会高一些。
> ```



3. 使用java nio中的ByteBuffer.allocate来操作

```java
RandomAccessFile raf = new RandomAccessFile(path, "rw");
FileChannel rafchannel = raf.getChannel();
ByteBuffer buffer = ByteBuffer.allocate(8192);
int read = rafchannel.read(buffer);
```

4. 使用java nio中的ByteBuffer.allocateDirect来操作

```java
RandomAccessFile raf = new RandomAccessFile(path, "rw");
FileChannel rafchannel = raf.getChannel();
ByteBuffer buffer = ByteBuffer.allocate(8192);
int read = rafchannel.read(buffer);
```

5. 使用mmap

```java
RandomAccessFile raf = new RandomAccessFile(path, "rw");
FileChannel rafchannel = raf.getChannel();
//mmap  堆外  和文件映射的   byte  not  objtect
MappedByteBuffer map = rafchannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
map.put("@@@".getBytes());
```





### java nio的总结：

前面三种：直接操作的是jvm heap内的空间，要写入page cache中，还需要进行堆外的拷贝后再进行***系统调用***。效率最低

第4种: 使用了jvm进程的直接地址空间，省去了一次jvm heap到堆外的拷贝，最终还是需要***系统调用***写入page cache中。

第5种，使用了mmap的方式，直接将在linux 进程的堆外和page cache 进行了映射，往map里面写入数据直接进入page cache ,不需要进行系统调用，效率最高，但是这种方式仅限于file.



### 使用扩展

1. netty: (on heap/off heap),
2. kafka log文件 ： mmap



## 网络io socketio

上面讲的差不多是文件io.这里下面讲一下网络io

![image-20201119224824109](https://tva1.sinaimg.cn/large/0081Kckwgy1gkuva2jbovj31dv0u0n9y.jpg)



上面的图片介绍的是tcp网络连接时候的内容

### 建立tcp连接会发生什么

1. 服务端监听某个端口号，调用系统调用accept,进行阻塞等待
2. 客户端连接对应的端口号
   1. 会在内核中完成三次握手操作。
   2. 在服务端和客户端都会分配一份资源给这次的连接，这个时候这个连接还不属于某一个进程
   3. 即使不调用服务端的accept,三次握手和资源分配都会完成，这个时候客户端如果往连接中发送数据，数据也会在连接的receive queue中
3. 服务端的accept系统调用有返回，本次连接的文件描述符fd(对于java代码来说会封装成一个socket)

### tcp连接中的四元组

tcp的四元组：服务端.ip+服务端.port+客户端.ip+客户端.port

上面四个属性能唯一确定一个条目，当对应的连接关联到某一个进程中，那么到这个四元组的数据就能被对应的进程处理。

* 端口使用一个16位的数字表示，它的范围是0~65535，0到1023之间的端口号保留给预定义的服务。

### 相关的linux 命令

* lsof -op 进程Id

可以看到打开的文件



* netstat -natp

  可以看到网络信息，但是mac不太一样

  ![image-20201119233904937](https://tva1.sinaimg.cn/large/0081Kckwgy1gkuwqwzbv7j315y0a0q4x.jpg)

可以看到

* Recv-Q/Send-Q
* 状态
* 该连接关联的进程等



### 连接分配相关资源

![image-20201119234243000](https://tva1.sinaimg.cn/large/0081Kckwgy1gkuwujr6lxj31ja0u0n5a.jpg)



* 问题：[10KC问题](https://blog.csdn.net/chenrui310/article/details/101685827)

select/poll/epoll知识

