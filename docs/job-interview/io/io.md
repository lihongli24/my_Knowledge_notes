
## io模型# io

       ## 同步/异步 + 阻塞/非阻塞
       
       **同步与异步**
       
       - **同步：** 同步就是发起一个调用后，被调用者未处理完请求之前，调用不返回。
       - **异步：** 异步就是发起一个调用后，立刻得到被调用者的回应表示已接收到请求，但是被调用者并没有返回结果，此时我们可以处理其他的请求，被调用者通常依靠事件，回调等机制来通知调用者其返回结果。
       
       同步和异步的区别最大在于异步的话调用者不需要等待处理结果，被调用者会通过回调等机制来通知调用者其返回结果。
       
       **阻塞和非阻塞**
       
       - **阻塞：** 阻塞就是发起一个请求，调用者一直等待请求结果返回，也就是当前线程会被挂起，无法从事其他任务，只有当条件就绪才能继续。
       - **非阻塞：** 非阻塞就是发起一个请求，调用者不用一直等着结果返回，可以先去干其他事情。
       
       举个生活中简单的例子，你妈妈让你烧水，小时候你比较笨啊，在那里傻等着水开（**同步阻塞**）。等你稍微再长大一点，你知道每次烧水的空隙可以去干点其他事，然后只需要时不时来看看水开了没有（**同步非阻塞**）。后来，你们家用上了水开了会发出声音的壶，这样你就只需要听到响声后就知道水开了，在这期间你可以随便干自己的事情，你需要去倒水了（**异步非阻塞**）。


​       

[linux中的io模型](https://mp.weixin.qq.com/s?__biz=Mzg3MjA4MTExMw==&mid=2247484746&idx=1&sn=c0a7f9129d780786cabfcac0a8aa6bb7&source=41#wechat_redirect)



## java nio的优化点

* 零拷贝
* 同步非阻塞
* 



## netty 

#### Netty 的零拷贝(用户层面的零拷贝，偏向于数据操作优化)

具体讲解在零拷贝.md中

* 在 OS 层面上的 `Zero-copy` 通常指避免在 `用户态(User-space)` 与 `内核态(Kernel-space)` 之间来回拷贝数据.
* Netty 中的 `Zero-copy` 与上面我们所提到到 OS 层面上的 `Zero-copy` 不太一样, Netty的 `Zero-coyp` 完全是在用户态(Java 层面)的, 它的 `Zero-copy` 的更多的是偏向于 `优化数据操作` 这样的概念.

#### netty的零拷贝

* [讲的很详细的文档](https://segmentfault.com/a/1190000007560884)

- Netty 提供了 CompositeByteBuf 类, 它可以将多个 ByteBuf 合并为一个逻辑上的 ByteBuf, 避免了各个 ByteBuf 之间的拷贝.
- 通过 wrap 操作, 我们可以将 byte[] 数组、ByteBuf、ByteBuffer等包装成一个 Netty ByteBuf 对象, 进而避免了拷贝操作.
- ByteBuf 支持 slice 操作, 因此可以将 ByteBuf 分解为多个共享同一个存储区域的 ByteBuf, 避免了内存的拷贝.
- 通过 FileRegion 包装的FileChannel.tranferTo 实现文件传输, 可以直接将文件缓冲区的数据发送到目标 Channel, 避免了传统通过循环 write 方式导致的内存拷贝问题.

## 参考文档

[linux中的io讲得很好](https://mp.weixin.qq.com/s?__biz=Mzg3MjA4MTExMw==&mid=2247484746&idx=1&sn=c0a7f9129d780786cabfcac0a8aa6bb7&source=41#wechat_redirect)

[tcp的buffer](https://www.cnblogs.com/promise6522/archive/2012/03/03/2377935.html)

[netty buffer](http://ifeve.com/netty-2-buffer/)

[netty处理流程](https://www.yuque.com/page/luan.ma/netty-flow)

[netty的零拷贝](https://segmentfault.com/a/1190000007560884)