/**
 * Copyright © 2018 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.ByteProcessor;
import io.netty.util.CharsetUtil;

public class SyslogFrameDecoder extends LineBasedFrameDecoder {
  final static ByteProcessor INTEGER = b -> b >= ((byte) 48) && b <= ((byte) 57);

  public SyslogFrameDecoder(int maxLength) {
    super(maxLength, true, false);
  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, ByteBuf b) throws Exception {
    ByteBuf buffer = b.retain();
    int lengthIndex = buffer.forEachByte(INTEGER);
    final int digitCount = lengthIndex - buffer.readerIndex();
    if (digitCount > 0) {
      buffer.markReaderIndex();
      final String frameLength = buffer.getCharSequence(buffer.readerIndex(), digitCount, CharsetUtil.UTF_8).toString();
      buffer.skipBytes(digitCount + 1);
      int length = Integer.parseInt(frameLength);

      if (b.readerIndex() + length > b.writerIndex()) {
        buffer.resetReaderIndex();
        return null;
      }
      return buffer.slice(digitCount + 1, length);
    } else {
      return super.decode(ctx, buffer);
    }
  }
}

