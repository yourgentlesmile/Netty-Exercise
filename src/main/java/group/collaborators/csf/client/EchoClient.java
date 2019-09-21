package group.collaborators.csf.client;

import group.collaborators.csf.message.MessageDto;
import group.collaborators.xc.issue.constant.PFConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * @author orangeC
 * @date 2019/9/17 20:21
 */
public class EchoClient {
    private final String host;
    private final int port;
    public static final String EXIT = "exit";

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("client start");
        String host = "127.0.0.1";
        int port = 8888;
        new EchoClient(host,port).start();
    }

    public void start() throws Exception {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(host,port)).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(new EchoClientHandler());
                }
            });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            sendMessage(channelFuture.channel());
//            channelFuture.channel().closeFuture().sync();
        } finally {
//            eventLoopGroup.shutdownGracefully().sync();
        }
    }

    public void sendMessage(Channel channel){
        System.out.println("start sendMessage! what's your name: ");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            ByteBuf magicBlock = Unpooled.copiedBuffer(MessageDto.MAGICBLOCK);
            String userName = bufferedReader.readLine();
            ByteBuf uName = Unpooled.buffer(userName.length() + 4).writeInt(userName.length());
            uName.writeCharSequence(userName, CharsetUtil.UTF_8);
            System.out.println("Please enter the message you want to send: ");
            while (true){
                String message = bufferedReader.readLine();
                ByteBuf msg = Unpooled.buffer(message.length() + 8).writeInt(message.length());
                msg.writeCharSequence(message, CharsetUtil.UTF_8);
                if(StringUtils.equals(message,EXIT)){
                    channel.writeAndFlush("exit! welcome back next time");
                    System.out.println("exit! welcome back next time");
                    channel.closeFuture().sync();
                    break;
                }
                CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer();
                compositeByteBuf.addComponents(true,magicBlock);
                compositeByteBuf.addComponents(true,msg);
                compositeByteBuf.addComponents(true,uName);
                channel.writeAndFlush(compositeByteBuf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
