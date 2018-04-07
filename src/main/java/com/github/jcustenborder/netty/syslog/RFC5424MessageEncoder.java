/**
 * Copyright © 2018 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.netty.syslog;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

public class RFC5424MessageEncoder extends MessageToMessageEncoder<RFC5424Message> {
  private final static Logger log = LoggerFactory.getLogger(RFC5424MessageEncoder.class);

  final Charset charset;
  final DateFormat dateFormat;

  public RFC5424MessageEncoder(TimeZone timeZone) {
    this.charset = Charset.forName("UTF-8");
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    this.dateFormat.setTimeZone(timeZone);

  }

  public RFC5424MessageEncoder() {
    this(TimeZone.getTimeZone("UTC"));
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
    buffer.writeCharSequence(this.dateFormat.format(message.date()), this.charset);
    buffer.writeCharSequence(" ", this.charset);
    buffer.writeCharSequence(message.host(), this.charset);
    buffer.writeCharSequence(" ", this.charset);

    if(null!=message.appName()) {
      buffer.writeCharSequence(message.appName(), this.charset);
    }
    if(null!=message.procId()){
      buffer.writeCharSequence(message.procId(), this.charset);
    } else {
      buffer.writeCharSequence(" -", this.charset);
    }
    if(null!=message.messageId()){
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
