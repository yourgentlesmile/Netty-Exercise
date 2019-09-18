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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.net.InetAddress;

/**
 * 客户端入站处理类
 */
public class NettyClientRequestHandler extends SimpleChannelInboundHandler<ByteBuf> {

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    InetAddress address = InetAddress.getLocalHost();
    //告诉服务器本机的主机名
    ctx.writeAndFlush(Unpooled.copiedBuffer("Hey server, I'm " + address.getHostName(),CharsetUtil.UTF_8));
  }

  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
    System.out.println("Client received: " + msg.toString(CharsetUtil.UTF_8));
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}
