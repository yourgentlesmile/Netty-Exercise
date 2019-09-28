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
package group.collaborators.xc.exp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.TooLongFrameException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class EmbeddedChannelTest {
  @Test
  public void testFrameDecoded() {
    ByteBuf buf = Unpooled.buffer();
    for (int i = 0; i < 9; i++) {
      buf.writeByte(i);
    }
    ByteBuf input = buf.duplicate();
    EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));
    assertTrue(channel.writeInbound(input.retain()));
    assertTrue(channel.finish());
    ByteBuf read = (ByteBuf) channel.readInbound();
    assertEquals(buf.readSlice(3),read);
    read.release();
    read = channel.readInbound();
    assertEquals(buf.readSlice(3),read);
    read.release();
    read = channel.readInbound();
    assertEquals(buf.readSlice(3),read);
    read.release();
    assertNull(channel.readInbound());
    buf.release();

  }
  public class FrameChunkDecoder extends ByteToMessageDecoder {
    private final int maxFrameSize;

    public FrameChunkDecoder(int maxFrameSize) {
      this.maxFrameSize = maxFrameSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      int readableByte = in.readableBytes();
      if(readableByte > maxFrameSize) {
        in.clear();
        throw new TooLongFrameException();
      }
      ByteBuf buf = in.readBytes(readableByte);
      out.add(buf);
    }
  }
  @Test
  public void frameChunkDecoder() {
    ByteBuf buf = Unpooled.buffer();
    for (int i = 0; i < 9; i++) {
      buf.writeByte(i);
    }
    ByteBuf input = buf.duplicate();
    EmbeddedChannel channel = new EmbeddedChannel(new FrameChunkDecoder(3));
    assertTrue(channel.writeInbound(input.readBytes(2)));
    try {
      channel.writeInbound(input.readBytes(4));
      Assert.fail();
    } catch (Exception e) {
      e.printStackTrace();
    }
    assertTrue(channel.writeInbound(input.readBytes(3)));
    assertTrue(channel.finish()); //标记为写入完成，可以开始读取
    ByteBuf read = channel.readInbound();
    assertEquals(buf.readSlice(2),read);
    read.release();

    read = channel.readInbound();
    assertEquals(buf.skipBytes(4).readSlice(3),read);
    read.release();
    buf.release();
  }
}
