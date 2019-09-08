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
package group.xc.exp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

public class Experience {
  public static void main(String[] args) {
    //将一个字节数组转换成一个ByteBuf
    byte[] raw = {12,15};
    byte[] raw2 = {20,21};
    ByteBuf s = Unpooled.copiedBuffer(raw);
    ByteBuf j = Unpooled.copiedBuffer(raw2);

    CompositeByteBuf byteBufs = Unpooled.compositeBuffer();
    byteBufs.addComponents(s,j);
    byteBufs.writeByte(0);
    byteBufs.writeByte(12);
    byteBufs.writeByte(13);
    byteBufs.writeByte(14);
    try {
      byteBufs.writeByte(15);
    } catch (Exception e) {
      System.out.println("Error we just can write 4 byte value in this bytebuf");
    }
    System.out.println("byteBufs = " + byteBufs.readableBytes());
    System.out.println("readerIndex = " + byteBufs.readerIndex());
    System.out.println("writerIndex = " + byteBufs.writerIndex());
    byte[] array = new byte[5];
    //readBytes 会更新readerIndex的值,而getBytes不会
    //总的来说get*操作不会移动readerIndex
    //任何名称以read或者skip开头的操作都将检索或者跳过位于当前readerIndex的数据，并且将他它增加的值写入已读字节数(readerIndex)
    byteBufs.readBytes(array,byteBufs.readerIndex(),array.length);
    byteBufs.getBytes(byteBufs.readerIndex(),array);
    System.out.println("readerIndex = " + byteBufs.readerIndex());
    //重置读索引
    byteBufs.resetReaderIndex();
    System.out.println("capacity = " + byteBufs.capacity());
    while (byteBufs.isReadable()) {
      System.out.println(byteBufs.readByte());
    }
    System.out.println(byteBufs.getUnsignedMedium(0));
    UnpooledByteBufAllocator unpooledByteBufAllocator = new UnpooledByteBufAllocator(true);
    unpooledByteBufAllocator.buffer();
  }
}