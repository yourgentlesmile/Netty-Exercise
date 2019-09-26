package group.collaborators.csf.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author orangeC
 * @date 2019/9/17 19:26
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<ChannelId, String> channelMapAll = new HashMap<ChannelId, String>();
    private static Map<ChannelId, String> channelMapExp = new HashMap<ChannelId, String>();
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("EchoServer received:" + ctx.channel().remoteAddress().toString().substring(1) + " message -> " + in.toString(CharsetUtil.UTF_8));
        if(atomicInteger.intValue() <= 1){
            atomicInteger.incrementAndGet();
            return;
        }
        in.skipBytes(4);
        int version = in.readInt();
        String ver = String.valueOf(in.readCharSequence(version, CharsetUtil.UTF_8 ));
        int userNameLen = in.readInt();
        String userName = String.valueOf(in.readCharSequence(userNameLen, CharsetUtil.UTF_8 ));
        long messageLen = in.readLong();
//        String msgs = String.valueOf(in.readCharSequence(messageLen,CharsetUtil.UTF_8));
        System.out.println("version length: " + version + " version：" + ver);
        System.out.println("userName length: " + userNameLen + " userName: " + userName);
        System.out.println("message length: " + messageLen );
        channelGroup.forEach(channel -> {
            // 此副本用于write操作
            ByteBuf byteBuf = in.copy();
            channel.writeAndFlush(byteBuf);
        });
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.writeAndFlush(" server -> " +channel.remoteAddress() +" join\n");
        channelGroup.add(channel);
    }

    //连接处于活动状态
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelMapAll.put(channel.id(),channel.remoteAddress().toString());
        if(channelGroup.size() == 1){
            atomicInteger.incrementAndGet();
            System.out.println(String.format("welcome %s  online,you are currently %s client" , channel.remoteAddress(), channelGroup.size()));
        }else if(channelGroup.size() > 1){
            atomicInteger.decrementAndGet();
            Set<ChannelId> channelIds = channelMapAll.keySet();
            for(ChannelId channelId: channelIds){
                if(channelId != channel.id()){
                    channelMapExp.put(channelId,channelMapAll.get(channelId));
                }
            }
            Iterator<Map.Entry<ChannelId, String>> iterators = channelMapExp.entrySet().iterator();
            System.out.println(String.format("welcome %s  online,you are currently %s client,others:" , channel.remoteAddress(), channelGroup.size()));
            while(iterators.hasNext()){
                Map.Entry<ChannelId, String> entry = iterators.next();
                System.out.println("client -> " + entry.getValue());
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.writeAndFlush(" server -> " +channel.remoteAddress() +" leave\n");
        // 客户端断开连接，删除
        channelMapAll.remove(channel.id());
        System.out.println("current connection：" + channelGroup.size());
        //channelGroup.remove(channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println(channel.remoteAddress() +" Offline");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
