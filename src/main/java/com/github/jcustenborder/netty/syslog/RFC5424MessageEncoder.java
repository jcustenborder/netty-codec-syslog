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

public class RFC5424MessageEncoder extends MessageToMessageEncoder<RFC5424Message> {
  private final static Logger log = LoggerFactory.getLogger(RFC5424MessageEncoder.class);

  final Charset charset;
  final DateTimeFormatter dateFormat;

  RFC5424MessageEncoder(DateTimeFormatter dateFormat) {
    this.dateFormat = dateFormat;
    this.charset = Charset.forName("UTF-8");
  }

  public RFC5424MessageEncoder() {
    this(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
  }

  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext, RFC5424Message message, List<Object> output) throws Exception {
    log.trace("encode() - message = {}", message);
    final ByteBuf buffer = channelHandlerContext.alloc().buffer();


    EncoderHelper.appendPriority(buffer, message);
    if (null != message.version()) {
      buffer.writeCharSequence(message.version().toString(), this.charset);
    }
    buffer.writeBytes(EncoderHelper.SPACE);
    buffer.writeCharSequence(message.date().format(this.dateFormat), this.charset);
    buffer.writeCharSequence(" ", this.charset);
    buffer.writeCharSequence(message.host(), this.charset);
    buffer.writeCharSequence(" ", this.charset);

    if (null != message.appName()) {
      buffer.writeCharSequence(message.appName(), this.charset);
    }
    if (null != message.procId()) {
      buffer.writeCharSequence(message.procId(), this.charset);
    } else {
      buffer.writeCharSequence(" -", this.charset);
    }
    if (null != message.messageId()) {
      buffer.writeCharSequence(message.messageId(), this.charset);
    } else {
      buffer.writeCharSequence(" -", this.charset);
    }

    buffer.writeCharSequence(" - ", this.charset);


//    buffer.writeCharSequence(message.tag(), this.charset);
//
//    if (null != message.processId()) {
//      buffer.writeCharSequence("[", this.charset);
//      buffer.writeCharSequence(message.processId().toString(), this.charset);
//      buffer.writeCharSequence("]", this.charset);
//    }
    buffer.writeCharSequence(message.message(), this.charset);
    output.add(buffer);
  }
}
