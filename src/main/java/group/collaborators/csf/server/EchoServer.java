package group.collaborators.csf.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author orangeC
 * @date 2019/9/17 19:41
 */
public class EchoServer {
    private final int port;
    private AtomicInteger connectCount = new AtomicInteger(0);
    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("server start");
//        int port = Integer.parseInt(args[0]);
        new EchoServer(8018).start();
    }
    public void start() throws Exception {
        final EchoServerHandler echoServerHandler = new EchoServerHandler();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(8888)).childHandler(new ChannelInitializer<SocketChannel>() {
                // 当一个新的连接被接受时，一个新的channel被创建，将echoServerHandler实例添加至pipeline（实例链）中，此echoServerHandler会收到有关客户端入站消息的通知
                @Override
                protected void initChannel(SocketChannel socketChannel){
                    socketChannel.pipeline().addLast(echoServerHandler);
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }
}
