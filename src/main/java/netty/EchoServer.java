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
        final EchoServerHandler handler01 = new EchoServerHandler(1);
        final EchoServerHandler handler02 = new EchoServerHandler(2);
        final EchoServerHandler handler03 = new EchoServerHandler(3);
        final EchoEndHandler endHandler = new EchoEndHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            serverBootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(8080))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(handler01, handler02, handler03);
                            ch.pipeline().addLast(endHandler);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
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

