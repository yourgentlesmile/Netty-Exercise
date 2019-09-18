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
package group.collaborators.xc.ipc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    /**todo 此处，由于客户端在建立连接的时候会发送一条无格式的消息，所以在解析的时候会出错。
     * todo 因此，进阶处理的时候要首先处理首次请求的数据包
     */
    ByteBuf in = (ByteBuf) msg;
    in.skipBytes(4);
    int version = in.readByte();
    int usernameLength = in.readInt();
    String username = String.valueOf(in.readCharSequence(usernameLength, CharsetUtil.UTF_8 ));
    int msgLength = in.readInt();
    String msgs = String.valueOf(in.readCharSequence(msgLength,CharsetUtil.UTF_8));
    System.out.println("version : " + version + "  usernameLength : " + usernameLength);
    System.out.println("username : " + username + "  msgLength : " + msgLength);
    System.out.println("msgs : " + username);
//    ctx.write(in);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    //添加一个listener(ChannelFutureListener.CLOSE) 当writeAndFlush完成后，将channel关闭
//    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//    System.out.println("read complete");
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
//    ctx.close();
  }
}
