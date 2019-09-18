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
package group.collaborators.xc.issue.util;

import group.collaborators.xc.issue.constant.PFConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * 数据包生成类
 */
public class UserPacketGenerator {
  private String username;
  public ByteBuf chatMsg(String msg) {
    return getPacket(msg,PFConstants.METHOD_CHAT);
  }
  public ByteBuf getPacket(String msg,int method) {
    ByteBuf magic = Unpooled.copiedBuffer(PFConstants.MEGIC_HEADER);
    ByteBuf version = Unpooled.buffer(1).writeByte(PFConstants.VERSION);
    ByteBuf usernameSizeAndBody = Unpooled.buffer(username.length() + 4);
    usernameSizeAndBody.writeInt(username.length()).writeCharSequence(username, CharsetUtil.UTF_8);
    ByteBuf msgSizeAndBody = Unpooled.buffer(msg.length() + 4);
    msgSizeAndBody.writeInt(msg.length()).writeCharSequence(msg, CharsetUtil.UTF_8);
    ByteBuf operate = Unpooled.buffer(1).writeByte(method);
    CompositeByteBuf packet = Unpooled.compositeBuffer();
    packet.addComponents(true,magic);
    packet.addComponents(true,version);
    packet.addComponents(true,usernameSizeAndBody);
    packet.addComponents(true,msgSizeAndBody);
    packet.addComponents(true,operate);
    return packet;
  }
  public ByteBuf leave() {
    return getPacket(PFConstants.ZERO_MSG,PFConstants.METHOD_GOODBYE);
  }
  public UserPacketGenerator(String username) {
    this.username = username;
  }
}
