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

### group().register(channel)

上面执行initAndRegister的时候，发现有一行代码`config().group().register(channel);`,

config().group()是从当前的bootsrap中获取到它的group,由上面的代码就是我们设置进去的NioEventLoopGroup，

<img src="netty%E7%9A%84%E5%A4%84%E7%90%86%E6%B5%81%E7%A8%8B.assets/image-20191231230311474.png" alt="image-20191231230311474" style="zoom: 33%;" />







## 客户端连接



![img](netty%E7%9A%84%E5%A4%84%E7%90%86%E6%B5%81%E7%A8%8B.assets/linkedkeeper0_e1335c47-09d2-4f78-8082-3d7b34701fdf.jpg)



## 请求的处理

## 其他资料

[源码讲解](https://segmentfault.com/a/1190000007403873)

