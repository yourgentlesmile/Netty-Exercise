/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package group.collaborators.xc.issue;

import group.collaborators.xc.issue.util.UserMsgPacketFactory;
import group.collaborators.xc.issue.util.UserPacketGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * Netty客户端引导类
 */
public class NettyClient {
  private final String host;
  private final int port;
  private final String LEAVE = "EXIT";

  public NettyClient(String host, int port) {
    this.host = host;
    this.port = port;
  }
  public static void main(String[] args) {
    String host = "127.0.0.1";
    int port = 8090;
    try {
      new NettyClient(host,port).start();
    } catch (InterruptedException e) {
      System.out.println("Occur an exception : " + e.getMessage());
    }
  }
  public void start() throws InterruptedException {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap;
    try {
      bootstrap = new Bootstrap();
      bootstrap.group(group)
      .channel(NioSocketChannel.class)
      .remoteAddress(new InetSocketAddress(host,port))
      .handler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
          ch.pipeline().addLast(new NettyClientRequestHandler());
        }
      });
      ChannelFuture future = bootstrap.connect().sync();
      letsChat(future);
    } finally {
      group.shutdownGracefully().sync();
    }
  }
  public void letsChat(ChannelFuture future) {
    System.out.print("Let us know your name : ");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    UserPacketGenerator maker;
    try {
      String username = reader.readLine();
      maker = UserMsgPacketFactory.getUserPacketGenerator(username);
      System.out.println("ok~ let's chat");
      while (true) {
        String msg = reader.readLine();
        if(msg.toUpperCase().equals(LEAVE)) {
          future.channel().writeAndFlush(maker.leave());
          future.channel().closeFuture().sync();
          break;
        }
        future.channel().writeAndFlush(maker.chatMsg(msg));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("good bye~");
  }
}
