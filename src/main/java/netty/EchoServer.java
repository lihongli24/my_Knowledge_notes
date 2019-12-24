package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class EchoServer {


    public static void main(String[] args) throws InterruptedException {
        EchoServer echoServer = new EchoServer();
        echoServer.start();
    }

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

    class EchoServerHandler extends ChannelInboundHandlerAdapter {
        private int index;

        public EchoServerHandler(int index) {
            this.index = index;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuffer = (ByteBuf) msg;
            System.out.println("handler" + index + " received msg" + byteBuffer.toString(StandardCharsets.UTF_8));
        }
    }

    class EchoEndHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.copiedBuffer("bye bye", StandardCharsets.UTF_8));
        }
    }
}

