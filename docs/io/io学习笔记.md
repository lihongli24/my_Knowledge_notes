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
   1. 提高了读写的消息
   2. 多个进程访问同一份数据，只要同一个pagecache就行了
5. 可能存在的问题：刷盘策略设置的不对，丢的数据会比较多。

![](https://tva1.sinaimg.cn/large/0081Kckwgy1gksm3w8m32j31960l6dlw.jpg)



## 脏页和内存淘汰

对于一个pagecache, 如果app对其进行了操作(create、update)，对应的页会被标记为dirty。

* 刷盘策略： 可以配置间隔时间、可以配置脏页占用内存大小。达到某个条件时，操作系统会把脏页中的数据，刷新到磁盘上去。



![](https://tva1.sinaimg.cn/large/0081Kckwgy1gksm6oh04xj31d20mkdjt.jpg)

## 缺页

![image-20201118000337471](https://tva1.sinaimg.cn/large/0081Kckwgy1gksm7prbrqj31o20fkq8t.jpg)





* 问题：[10KC问题](https://blog.csdn.net/chenrui310/article/details/101685827)

select/poll/epoll知识

