# netty的处理流程--源码分析

>  首先通过**/src/main/java/netty/EchoServer**和**/src/main/java/netty/EchoClient**两个实例，先粗略的运行一次netty.然后重头到位的查看一遍netty是怎么处理请求的


## server启动
```java
private void start() throws InterruptedException {
  //定义4个handler,前3个实现channelRead
  //最后一个handler实现的是channelReadComplete
  final EchoServerHandler handler01 = new EchoServerHandler(1);
  final EchoServerHandler handler02 = new EchoServerHandler(2);
  final EchoServerHandler handler03 = new EchoServerHandler(3);
  final EchoEndHandler endHandler = new EchoEndHandler();
  //定义一个eventLoopGroup,并且将他和ServerBootStrap绑定
  EventLoopGroup group = new NioEventLoopGroup();
  ServerBootstrap serverBootstrap = new ServerBootstrap();
  try {
    serverBootstrap.group(group)
      //使用nio的方式取处理事件
      .channel(NioServerSocketChannel.class)
      .localAddress(new InetSocketAddress(8080))
      //通过这个口子取注册handler
      .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
          ch.pipeline().addLast(handler01, handler02, handler03);
          ch.pipeline().addLast(endHandler);
        }
      });
    //绑定成功返回channelFuture
    ChannelFuture channelFuture = serverBootstrap.bind().sync();
    //阻塞到channel close
    channelFuture.channel().closeFuture().sync();
  } finally {
    //优雅下线。不知道什么概念，之后细看
    group.shutdownGracefully().sync();
  }
}
```

### group
```java
ServerBootstrap:

ServerBootstrap extends AbstractBootstrap<ServerBootstrap, ServerChannel>

@Override
public ServerBootstrap group(EventLoopGroup group) {
  return group(group, group);
}

public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
  super.group(parentGroup);
  ObjectUtil.checkNotNull(childGroup, "childGroup");
  if (this.childGroup != null) {
    throw new IllegalStateException("childGroup set already");
  }
  this.childGroup = childGroup;
  return this;
}

AbstractBootstrap： 
public B group(EventLoopGroup group) {
  ObjectUtil.checkNotNull(group, "group");
  if (this.group != null) {
    throw new IllegalStateException("group set already");
  }
  this.group = group;
  return self();
}
```

ServerBootstrap中维护着两个EventLoopGroup，

* group:处理连接，
* childGroup:处理读写的

#### NioEventLoopGroup();

我们在代码里，往serverBootstrap里面注入了一个NioEventLoopGroup，下面分析下这个NioEventLoopGroup.

从下面的类图可以看出来，NioEventLoopGroup继承自MultithreadEventExecutorGroup，我们在初始化NioEventLoopGroup的时候会调用到MultithreadEventExecutorGroup的构造函数

```java
protected MultithreadEventExecutorGroup(int nThreads, Executor executor,
                                        EventExecutorChooserFactory chooserFactory, Object... args) {
  //判断线程数 DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
//                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
  if (nThreads <= 0) {
    throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
  }

  //如果没有执行器，生成一个执行器
  //后面创建eventloop的时候，会把这个执行器传递进去，同一个eventLoopGroup使用一个执行器
  //虽然eventLoop也是一个Executor，但是真正执行任务时使用到的执行器时这个，这个Executor会被包装进eventLoop
  if (executor == null) {
    executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
  }

  //按照线程数，初始化evetloop的线程数组
  children = new EventExecutor[nThreads];

  //遍历构造这个EventExecutor数组中的数据，本实例中时NioEventLoop
  for (int i = 0; i < nThreads; i ++) {
    boolean success = false;
    try {
      children[i] = newChild(executor, args);
      success = true;
    } catch (Exception e) {
      // TODO: Think about if this is a good exception type
      throw new IllegalStateException("failed to create a child event loop", e);
    } finally {
      //如果时失败了，清理资源
      if (!success) {
        for (int j = 0; j < i; j ++) {
          children[j].shutdownGracefully();
        }

        for (int j = 0; j < i; j ++) {
          EventExecutor e = children[j];
          try {
            while (!e.isTerminated()) {
              e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            }
          } catch (InterruptedException interrupted) {
            // Let the caller handle the interruption.
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    }
  }

  //生成一个chooser,这个chooser有两种策略，当childre是2的幂次和不是2的幂次两种，应该是因为2的幂次使用位运算的方式效率会高点，其实也就是一个轮询的策略方式。
  chooser = chooserFactory.newChooser(children);

  final FutureListener<Object> terminationListener = new FutureListener<Object>() {
    @Override
    public void operationComplete(Future<Object> future) throws Exception {
      if (terminatedChildren.incrementAndGet() == children.length) {
        terminationFuture.setSuccess(null);
      }
    }
  };

  //设置中断的监听
  for (EventExecutor e: children) {
    e.terminationFuture().addListener(terminationListener);
  }

  Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(children.length);
  Collections.addAll(childrenSet, children);
  readonlyChildren = Collections.unmodifiableSet(childrenSet);
}

```





<img src="netty%E7%9A%84%E5%A4%84%E7%90%86%E6%B5%81%E7%A8%8B.assets/image-20200101224242889.png" alt="image-20200101224242889" style="zoom: 33%;" />



#### newChild

```java
NioEventLoopGroup:
@Override
protected EventLoop newChild(Executor executor, Object... args) throws Exception {
  EventLoopTaskQueueFactory queueFactory = args.length == 4 ? (EventLoopTaskQueueFactory) args[3] : null;
  return new NioEventLoop(this, executor, (SelectorProvider) args[0],
                          ((SelectStrategyFactory) args[1]).newSelectStrategy(), (RejectedExecutionHandler) args[2], queueFactory);
}
```

NioEventLoopGroup中堆newChild的实现是返回一个NioEventLoop,所以上面的children列表中放入的是一堆的NioEventLoop下面看一下这堆的NioEventLoop具体是做什么的。



<img src="netty%E7%9A%84%E5%A4%84%E7%90%86%E6%B5%81%E7%A8%8B.assets/image-20200101230436651.png" alt="image-20200101230436651" style="zoom:30%;" />



NioEventLoop继承了SingleThreadEventExecutor，和Eventloop接口

调用到SingleThreadEventExecutor的构造函数

```java
SingleThreadEventExecutor:

protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor,
                                    boolean addTaskWakesUp, Queue<Runnable> taskQueue,
                                    RejectedExecutionHandler rejectedHandler) {
  super(parent);
  this.addTaskWakesUp = addTaskWakesUp;
  this.maxPendingTasks = DEFAULT_MAX_PENDING_EXECUTOR_TASKS;
  //传入上面生成的ThreadPerTaskExecutor,将他伪装成当前的执行器
  this.executor = ThreadExecutorMap.apply(executor, this);
  //线程池的queue
  this.taskQueue = ObjectUtil.checkNotNull(taskQueue, "taskQueue");
  //线程池的拒绝策略
  rejectedExecutionHandler = ObjectUtil.checkNotNull(rejectedHandler, "rejectedHandler");
}



ThreadExecutorMap:

public static Executor apply(final Executor executor, final EventExecutor eventExecutor) {
  ObjectUtil.checkNotNull(executor, "executor");
  ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
  //封装出一个executor，当执行任务的时候全都使用group统一的executor
  return new Executor() {
    @Override
    public void execute(final Runnable command) {
      executor.execute(apply(command, eventExecutor));
    }
  };
}


//对Runnable进行封装，主要是为了执行setCurrentEventExecutor命令
public static Runnable apply(final Runnable command, final EventExecutor eventExecutor) {
  ObjectUtil.checkNotNull(command, "command");
  ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
  return new Runnable() {
    @Override
    public void run() {
      setCurrentEventExecutor(eventExecutor);
      try {
        command.run();
      } finally {
        setCurrentEventExecutor(null);
      }
    }
  };
}
```

到这里，我们得到了几点概念

1. eventLoopGroup中公用一个线程池
2. 每个eventloop执行命令的时候都会使用封装在里面的统一的线程池处理
3. 每个eventLoop---SingleThreadEventExecutor会维护一个taskQueue,后续应该是会从这里面取任务执行



### channel(NioServerSocketChannel.class)

```java
//使用的时候执行的代码
.channel(NioServerSocketChannel.class)

//设置bootstrap中的channelFactory，constructor设置成传入class的NioServerSocketChannel的构造函数
//这个channelFactory会在后续的bind方法中使用
AbstractBootstrap:
public B channel(Class<? extends C> channelClass) {
  return channelFactory(new ReflectiveChannelFactory<C>(
    ObjectUtil.checkNotNull(channelClass, "channelClass")
  ));
}

ReflectiveChannelFactory:
public ReflectiveChannelFactory(Class<? extends T> clazz) {
  ObjectUtil.checkNotNull(clazz, "clazz");
  try {
    this.constructor = clazz.getConstructor();
  } catch (NoSuchMethodException e) {
    throw new IllegalArgumentException("Class " + StringUtil.simpleClassName(clazz) +
                                       " does not have a public non-arg constructor", e);
  }
}
```

### localAddress(new InetSocketAddress(8080))

```java
AbstractBootstrap:
public B localAddress(SocketAddress localAddress) {
  //设置bootstrap的localAddress
  this.localAddress = localAddress;
  return self();
}
```

### ChannelInitializer

```java
//通过这个口子取注册handler
.childHandler(new ChannelInitializer<SocketChannel>() {
  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline().addLast(handler01, handler02, handler03);
    ch.pipeline().addLast(endHandler);
  }
});
```

上面的方法调用的是childHandler,所以设置的是

```java
ServerBootstrap:
private volatile ChannelHandler childHandler;
```

这个childHandler是用来监听读写事件的。

#### ServerBootstrap的handler和childHandler

```java
ServerBootstrap：
@Override
void init(Channel channel) {
  setChannelOptions(channel, options0().entrySet().toArray(newOptionArray(0)), logger);
  setAttributes(channel, attrs0().entrySet().toArray(newAttrArray(0)));

  ChannelPipeline p = channel.pipeline();

  //这个操作意思就好像设置的时候把很多东西放在了ServerBootstrap中，这里当我们初始化真正的server channel的时候，可以把那些东西放回来了
  final EventLoopGroup currentChildGroup = childGroup;
  final ChannelHandler currentChildHandler = childHandler;
  final Entry<ChannelOption<?>, Object>[] currentChildOptions =
    childOptions.entrySet().toArray(newOptionArray(0));
  final Entry<AttributeKey<?>, Object>[] currentChildAttrs = childAttrs.entrySet().toArray(newAttrArray(0));

  //创建增加监听器，当channel被初始化的时候执行下面的方法
  p.addLast(new ChannelInitializer<Channel>() {
    @Override
    public void initChannel(final Channel ch) {
      final ChannelPipeline pipeline = ch.pipeline();
      //将handler关联上server channel的pipeline中
      ChannelHandler handler = config.handler();
      if (handler != null) {
        pipeline.addLast(handler);
      }

      ch.eventLoop().execute(new Runnable() {
        @Override
        public void run() {
          //将childHandler和childOptions等封装到ServerBootstrapAcceptor中，后续这个ServerBootstrapAcceptor被触发的时候会把这些信息放入子channel中
          pipeline.addLast(new ServerBootstrapAcceptor(
            ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
        }
      });
    }
  });
}

```

ServerBootstrap在bind的时候会调用上面的init()方法，进行一些操作。

```java
ServerBootstrapAcceptor:
@Override
@SuppressWarnings("unchecked")
public void channelRead(ChannelHandlerContext ctx, Object msg) {
  final Channel child = (Channel) msg;

  //将原先定义的childHandler放入到了新产生的channel连接的pipeline里面了，也就是在echoserver中定义的ChannelInitializer
  child.pipeline().addLast(childHandler);

  //channelOptitions也是一样
  setChannelOptions(child, childOptions, logger);
  //还有attributes
  setAttributes(child, childAttrs);

  //将这个channel注册到evetloop中
  try {
    childGroup.register(child).addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
          forceClose(child, future.cause());
        }
      }
    });
  } catch (Throwable t) {
    forceClose(child, t);
  }
}
```

ServerBootstrapAcceptor被加入到server channel的pipleline之后，当出现read操作的时候，本方法会被触发

> 当有新的连接进来的时候，netty会执行unsafe.read()方法，上面的channelRead方法就会被触发。

### serverBootstrap.bind()

```java
 final ChannelFuture initAndRegister() {
   Channel channel = null;
   try {
     //使用到channel那一节设置进来的class的构造函数，创建一个channel实例
     channel = channelFactory.newChannel();
     //init方法在上面一节已经介绍了功能，将定义的handler等信息关联到channel上
     init(channel);
   } catch (Throwable t) {
     。。。
   }

   //将channel注册到group中，注意：是group不是childGroup哦
   ChannelFuture regFuture = config().group().register(channel);
   if (regFuture.cause() != null) {
     if (channel.isRegistered()) {
       channel.close();
     } else {
       channel.unsafe().closeForcibly();
     }
   }
   return regFuture;
 }
```



完成上面的步骤，

1. 我们初始化了一个channel,
2. 将handler和这个channel关联上了
3. 使用ServerBootstrapAcceptor的方式，在发成server channel的read的时候(处理和client的连接的时候)，将childHandler等和新简历的channel关联起来。---不是创建的过程中，是一个异步的操作。
4. 最后将group和server channel关联起来。我们需要一个eventloop能定期的取判断server channel上的事件。





## NioEventLoopGroup

上面的代码里

### group().register(channel)

上面执行initAndRegister的时候，发现有一行代码`config().group().register(channel);`,

config().group()是从当前的bootsrap中获取到它的group,由上面的代码就是我们设置进去的NioEventLoopGroup，

<img src="netty%E7%9A%84%E5%A4%84%E7%90%86%E6%B5%81%E7%A8%8B.assets/image-20191231230311474.png" alt="image-20191231230311474" style="zoom: 33%;" />



### eventloop的启动

执行`config().group().register(channel);`的时候，其实就是从当前的group中使用它里面的chooser来获取一个EventLoop来把这个channel注册进去。

```java
MultithreadEventLoopGroup:

@Override
public ChannelFuture register(Channel channel) {
  return next().register(channel);
}

@Override
public EventLoop next() {
  return (EventLoop) super.next();
}


SingleThreadEventLoop:

@Override
public ChannelFuture register(Channel channel) {
  return register(new DefaultChannelPromise(channel, this));
}

@Override
public ChannelFuture register(final ChannelPromise promise) {
  ObjectUtil.checkNotNull(promise, "promise");
  promise.channel().unsafe().register(this, promise);
  return promise;
}

AbstractChannel:
@Override
public final void register(EventLoop eventLoop, final ChannelPromise promise) {

  //相关的一些检测
  ...

  AbstractChannel.this.eventLoop = eventLoop;

  //如果当前线程和eventLoop中的线程一致，直接同步执行
  //第一次运行的时候，因为eventLoop还没有和thread关联，所以肯定是false
  if (eventLoop.inEventLoop()) {
    register0(promise);
  } else {
    try {
      //使用eventLoop的execute线程池的方式去执行这个register0方法
      eventLoop.execute(new Runnable() {
        @Override
        public void run() {
          register0(promise);
        }
      });
    } catch (Throwable t) {
      //异常情况的处理
      ...
    }
  }
}

SingleThreadEventExecutor:
@Override
public void execute(Runnable task) {
  if (task == null) {
    throw new NullPointerException("task");
  }

  //和上面一致，判断eventLoop中保存的是不是当前线程
  boolean inEventLoop = inEventLoop();
  //把任务添加到eventLoop的taskQueue中
  addTask(task);
  //thread和eventLoop中的不一致，说明是第一次启动，调用statThread()方法，和线程关联并启动它
  if (!inEventLoop) {
    startThread();
    //入股是关闭状态了，把刚刚加进去的任务移出，并且进行reject操作。
    if (isShutdown()) {
      boolean reject = false;
      try {
        if (removeTask(task)) {
          reject = true;
        }
      } catch (UnsupportedOperationException e) {
        // The task queue does not support removal so the best thing we can do is to just move on and
        // hope we will be able to pick-up the task before its completely terminated.
        // In worst case we will log on termination.
      }
      if (reject) {
        reject();
      }
    }
  }

  //这两行还没看懂啥意思
  if (!addTaskWakesUp && wakesUpForTask(task)) {
    wakeup(inEventLoop);
  }
}




private void doStartThread() {
  assert thread == null;
  executor.execute(new Runnable() {
    @Override
    public void run() {
      //首先将eventLoop中的thread和当前请求的thread关联上
      //后续的inEventLoop()方法就会return true了
      thread = Thread.currentThread();
      if (interrupted) {
        thread.interrupt();
      }

      boolean success = false;
      updateLastExecutionTime();
      try {
        //因为已经在异步代码块里面了，所以直接同步调用run,也就是我们的NioEventLoop的run方法
        //
        SingleThreadEventExecutor.this.run();
        success = true;
      } catch (Throwable t) {
        logger.warn("Unexpected exception from an event executor: ", t);
      } finally {
        //其他代码
        ...
      }
        
  });
}


```

> SingleThreadEventExecutor.this.run(); 至于为什么用了类名.this这种方式，查了下网上有两种这么用的场景
>
> 1. 当在一个类的内部类中，如果需要访问外部类的方法或者成员域的时候，如果使用 this.成员域(与 内部类.this.成员域 没有分别) 调用的显然是内部类的域 ， 如果我们想要访问外部类的域的时候，就要必须使用 外部类.this.成员域
> 2. 还有一个使用情况，那就是在是使用意图更加的清楚，在Android开发中我们经常要在一些地方使用 Context 类型的参数， 而这个参数我们往往使用this
>
> 感觉这里可能就是第二种情况，具体还没参透，如果后续有什么想法再来修改
>
> 



**到这里我们的eventLoop已经运行起来了**

```java
@Override
protected void run() {
  for (;;) {
    try {
      try {
        switch (selectStrategy.calculateStrategy(selectNowSupplier, hasTasks())) {
          case SelectStrategy.CONTINUE:
            continue;

          case SelectStrategy.BUSY_WAIT:
            // fall-through to SELECT since the busy-wait is not supported with NIO

          case SelectStrategy.SELECT:
            select(wakenUp.getAndSet(false));
            if (wakenUp.get()) {
              selector.wakeup();
            }
            // fall through
          default:
        }
      } catch (IOException e) {
        // If we receive an IOException here its because the Selector is messed up. Let's rebuild
        // the selector and retry. https://github.com/netty/netty/issues/8566
        rebuildSelector0();
        handleLoopException(e);
        continue;
      }

      cancelledKeys = 0;
      needsToSelectAgain = false;
      final int ioRatio = this.ioRatio;
      if (ioRatio == 100) {
        try {
          processSelectedKeys();
        } finally {
          // Ensure we always run tasks.
          runAllTasks();
        }
      } else {
        final long ioStartTime = System.nanoTime();
        try {
          processSelectedKeys();
        } finally {
          // Ensure we always run tasks.
          final long ioTime = System.nanoTime() - ioStartTime;
          runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
        }
      }
    } catch (Throwable t) {
      handleLoopException(t);
    }
    // Always handle shutdown even if the loop processing threw an exception.
    try {
      if (isShuttingDown()) {
        closeAll();
        if (confirmShutdown()) {
          return;
        }
      }
    } catch (Throwable t) {
      handleLoopException(t);
    }
  }
}

```





## 客户端连接



![img](netty%E7%9A%84%E5%A4%84%E7%90%86%E6%B5%81%E7%A8%8B.assets/linkedkeeper0_e1335c47-09d2-4f78-8082-3d7b34701fdf.jpg)



## 请求的处理

## 其他资料

[源码讲解](https://segmentfault.com/a/1190000007403873)

[netty的线程模型](https://blog.csdn.net/u010853261/article/details/62043709)

