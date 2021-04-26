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

* 图中的heap有两个，

  * 一个是整个jvm进程的heap,
  * 还有一个是jvm进程内，我们熟悉的 jvm堆

  



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
ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
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

完成三次握手之后，客户端和服务端在各自的内核中维护了对上面所说的***四元组***的一个资源，比如Buffer,以fd(文件描述符的方式)提供能用户程序进行调用。

![image-20201119234243000](https://tva1.sinaimg.cn/large/0081Kckwgy1gkuwujr6lxj31ja0u0n5a.jpg)



## 多路复用器

### 老的socket编程方式：

```java
ServerSocket server = new ServerSocket(9090,20);
while (true) {
  Socket client = server.accept();  //阻塞1
  new Thread(() -> {
    in = client.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    while(true){
    	String dataline = reader.readLine(); //阻塞2
      ...
    }
    ....
  }).start();
}
```



因为老的socket编程时，下面的两个操作是会因为内核的系统调用进行阻塞

* server.accept() 如果调用时没有客户端连接进来，这个方法调用就是会一直阻塞直到有客户端请求进来才会有返回
* reader.readLine();  如果连接的客户端没有数据发送过来，这个方法也会进行阻塞。
* 对于读取数据完成后的数据，还有业务逻辑处理，可能会很耗时

基于上面两个原因，为了保证可以服务于多个客户端请求进来的情况，所以需要把reader.readLine的调用单独做一个线程，让主线程只做客户端连接的响应，新增加的线程来处理数据读取和业务逻辑等操作。



![image-20201129222036395](/Users/lihongli24/Library/Application Support/typora-user-images/image-20201129222036395.png)

#### 问题：

1. 因为老的socket模式是基于对每个连接创建新的线程的方式，那么当请求量非常大的情况下，肯定会创建触非常多的线程出来，对于cpu来说，肯定需要花大部分时间在线程的切换上，这样效率会非常低。(cpu内部大致分为 寄存器、指令计数器pc和逻辑计算单元ALU,在多个线程切换时，寄存器和pc中的数据需要频繁的更换，cpu的计算时间会受到影响）
2. 一个线程默认是占用1M的空间，这个也是问题

### 非阻塞式IO

因为上面的原来的io模式，受限于阻塞和多线程的问题，那么怎么解决这两个问题呢？
内核做了优化，实现了NIO，nonblocking IO

于是调用的时候是这样的

```java
LinkedList<SocketChannel> clients = new LinkedList<>();
ServerSocketChannel ss = ServerSocketChannel.open();  //服务端开启监听：接受客户端
ss.bind(new InetSocketAddress(9090));
ss.configureBlocking(false); //重点  OS  NONBLOCKING!!!  //只让接受客户端  不阻塞
while (true) {
  SocketChannel client = ss.accept(); //不会阻塞？  -1 NULL
  if (client == null) {
    System.out.println("null.....");
  } else {
    client.configureBlocking(false); //重点  socket（服务端的listen socket<连接请求三次握手后，往我这里扔，我去通过accept 得到  连接的socket>，连接socket<连接后的数据读写使用的> ）
    int port = client.socket().getPort();
    System.out.println("client..port: " + port);
    clients.add(client);
  }
  
  //遍历已经链接进来的客户端能不能读写数据
  for (SocketChannel c : clients) {   //串行化！！！！  多线程！！
    int num = c.read(buffer);  // >0  -1  0   //不会阻塞
    if (num > 0) {
      buffer.flip();
      byte[] aaa = new byte[buffer.limit()];
      buffer.get(aaa);

      String b = new String(aaa);
      System.out.println(c.socket().getPort() + " : " + b);
      buffer.clear();
    }


  }
```



上面的代码中，ServerSocketChannel和SocketChannel都可以设置.configureBlocking(false);

那么之后的accept和read等操作都是不会阻塞。

这样的话，就不用单独启动新的线程去处理读取操作了。

![image-20201129223111323](https://tva1.sinaimg.cn/large/0081Kckwgy1gl6ez7dlzzj31o20hmtd2.jpg)



#### 问题：

1. 虽然用了非阻塞解决了***系统调用的阻塞***和***启动太多线程后的线程切换的消耗***问题，但是当连接数非常多，也就是我们的clients容器里面存放了好多客户端之后，在遍历这个clients的时候，如果10万个client里面只有一个或者2个真正有数据，那么9万多次的系统调用***recv***也都是白调用的。
2. 如果clients的遍历过程中，数据的处理很耗时间，那么可能会导致很多数据来不及处理。
3. 一次accept好像只能接收一个客户端，客户端接入速度会比较慢（如果下面的for循环耗时的话，那么就会更加的慢了）

### 多路复用器

根据上面的问题，遍历clients和一次只能accept接收一个客户端，就出现了多路复用器。

解决的问题是，多个client，我们想一次系统调用就知道哪些有数据可以读取，哪些新连接需要接入。

所以代码可以写成下面这样

```java
server = ServerSocketChannel.open();
server.configureBlocking(false);
server.bind(new InetSocketAddress(port));
//如果在epoll模型下，open--》  epoll_create -> fd3
selector = Selector.open();  //  select  poll  *epoll  优先选择：epoll  但是可以 -D修正
//server 约等于 listen状态的 fd4
/*
register
 如果：
 select，poll：jvm里开辟一个数组 fd4 放进去
 epoll：  epoll_ctl(fd3,ADD,fd4,EPOLLIN
*/
server.register(selector, SelectionKey.OP_ACCEPT);

while (true) {  //死循环

  Set<SelectionKey> keys = selector.keys();
  System.out.println(keys.size()+"   size");


  //1,调用多路复用器(select,poll  or  epoll  (epoll_wait))
  /*
                select()是啥意思：
                1，select，poll  其实  内核的select（fd4）  poll(fd4)
                2，epoll：  其实 内核的 epoll_wait()
                *, 参数可以带时间：没有时间，0  ：  阻塞，有时间设置一个超时
                selector.wakeup()  结果返回0

                懒加载：
                其实再触碰到selector.select()调用的时候触发了epoll_ctl的调用

                 */
  while (selector.select() > 0) {
    Set<SelectionKey> selectionKeys = selector.selectedKeys();  //返回的有状态的fd集合
    Iterator<SelectionKey> iter = selectionKeys.iterator();
    //so，管你啥多路复用器，你呀只能给我状态，我还得一个一个的去处理他们的R/W。同步好辛苦！！！！！！！！
    //  NIO  自己对着每一个fd调用系统调用，浪费资源，那么你看，这里是不是调用了一次select方法，知道具体的那些可以R/W了？
    //我前边可以强调过，socket：  listen   通信 R/W
    while (iter.hasNext()) {
      SelectionKey key = iter.next();
      iter.remove(); //set  不移除会重复循环处理
      if (key.isAcceptable()) {
        //看代码的时候，这里是重点，如果要去接受一个新的连接
        //语义上，accept接受连接且返回新连接的FD对吧？
        //那新的FD怎么办？
        //select，poll，因为他们内核没有空间，那么在jvm中保存和前边的fd4那个listen的一起
        //epoll： 我们希望通过epoll_ctl把新的客户端fd注册到内核空间
        acceptHandler(key);
      } else if (key.isReadable()) {
        readHandler(key);  //连read 还有 write都处理了
        //在当前线程，这个方法可能会阻塞  ，如果阻塞了十年，其他的IO早就没电了。。。
        //所以，为什么提出了 IO THREADS
        //redis  是不是用了epoll，redis是不是有个io threads的概念 ，redis是不是单线程的
        //tomcat 8,9  异步的处理方式  IO  和   处理上  解耦
      }
    }
  }
```







* 问题：[10KC问题](https://blog.csdn.net/chenrui310/article/details/101685827)

select/poll/epoll知识

