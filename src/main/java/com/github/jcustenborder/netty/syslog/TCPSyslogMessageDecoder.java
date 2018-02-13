/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
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
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class TCPSyslogMessageDecoder extends MessageToMessageDecoder<ByteBuf> {
  final AbstractMessageParser parser;

  public TCPSyslogMessageDecoder(AbstractMessageParser parser) {
    this.parser = parser;
  }

  public TCPSyslogMessageDecoder() {
    this(new SyslogMessageParser("UTC"));
  }
  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> output) throws Exception {
    this.parser.parse(output, channelHandlerContext.channel().remoteAddress(), byteBuf);
  }
}
