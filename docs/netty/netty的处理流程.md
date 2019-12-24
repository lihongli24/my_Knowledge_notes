# netty的处理流程--源码分析

>  首先通过**/src/main/java/netty/EchoServer**和**/src/main/java/netty/EchoClient**两个实例，先粗略的运行一次netty.然后重头到位的查看一遍netty是怎么处理请求的

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

