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
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class CEFMessageEncoder extends MessageToMessageEncoder<CEFMessage> {
  private final static Logger log = LoggerFactory.getLogger(CEFMessageEncoder.class);
  final DateTimeFormatter dateFormat;
  final Charset charset;
  final byte[] cef;
  final byte[] pipe;

  CEFMessageEncoder(DateTimeFormatter dateFormat) {
    this.dateFormat = dateFormat;
    this.charset = Charset.forName("UTF-8");
    this.cef = "CEF:0".getBytes(this.charset);
    this.pipe = "|".getBytes(this.charset);
  }

  public CEFMessageEncoder() {
    this(DateTimeFormatter.ofPattern("MMM d HH:mm:ss"));
  }

  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext, CEFMessage message, List<Object> output) throws Exception {
    log.trace("encode() - message = {}", message);
    final ByteBuf buffer = channelHandlerContext.alloc().buffer();
    EncoderHelper.appendPriority(buffer, message);

    buffer.writeCharSequence(dateFormat.format(message.date()), this.charset);
    buffer.writeBytes(EncoderHelper.SPACE);
    buffer.writeCharSequence(message.host(), this.charset);
    buffer.writeBytes(EncoderHelper.SPACE);
    buffer.writeBytes(this.cef);
    buffer.writeBytes(this.pipe);
    buffer.writeCharSequence(message.deviceVendor(), this.charset);
    buffer.writeBytes(this.pipe);
    buffer.writeCharSequence(message.deviceProduct(), this.charset);
    buffer.writeBytes(this.pipe);
    buffer.writeCharSequence(message.deviceVersion(), this.charset);
    buffer.writeBytes(this.pipe);
    buffer.writeCharSequence(message.deviceEventClassId(), this.charset);
    buffer.writeBytes(this.pipe);
    buffer.writeCharSequence(message.name(), this.charset);
    buffer.writeBytes(this.pipe);
    buffer.writeCharSequence(message.severity(), this.charset);
    buffer.writeBytes(this.pipe);

    int index = 0;
    for (Map.Entry<String, String> kvp : message.extension().entrySet()) {
      if (index > 0) {
        buffer.writeBytes(EncoderHelper.SPACE);
      }
      buffer.writeCharSequence(kvp.getKey(), this.charset);
      buffer.writeBytes(EncoderHelper.EQUALS);
      buffer.writeCharSequence(kvp.getValue(), this.charset);
      index++;
    }

    output.add(buffer);
  }
}
