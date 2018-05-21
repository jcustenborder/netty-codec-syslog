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

public class MessageEncoder extends MessageToMessageEncoder<Message> {
  private final static Logger log = LoggerFactory.getLogger(MessageEncoder.class);
  final DateTimeFormatter cefDateFormat;
  final Charset charset;
  final byte[] cef;
  final byte[] pipe;

  public MessageEncoder(DateTimeFormatter cefDateFormat) {
    this.cefDateFormat = cefDateFormat;
    this.charset = Charset.forName("UTF-8");
    this.cef = "CEF:0".getBytes(this.charset);
    this.pipe = "|".getBytes(this.charset);
  }

  private void encodeCEF(ChannelHandlerContext context, Message message, List<Object> output) throws Exception {
    log.trace("encode() - message = {}", message);
    final ByteBuf buffer = context.alloc().buffer();
    EncoderHelper.appendPriority(buffer, message);

    buffer.writeCharSequence(cefDateFormat.format(message.date()), this.charset);
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

  private void encodeRFC3164(ChannelHandlerContext context, Message message, List<Object> output) {
    log.trace("encode() - message = {}", message);
    final ByteBuf buffer = context.alloc().buffer();
    EncoderHelper.appendPriority(buffer, message);

    buffer.writeCharSequence(message.date().format(this.cefDateFormat), this.charset);
    buffer.writeCharSequence(" ", this.charset);
    buffer.writeCharSequence(message.host(), this.charset);
    buffer.writeCharSequence(" ", this.charset);
    buffer.writeCharSequence(message.tag(), this.charset);

    if (null != message.processId()) {
      buffer.writeCharSequence("[", this.charset);
      buffer.writeCharSequence(message.processId().toString(), this.charset);
      buffer.writeCharSequence("]", this.charset);
    }
    buffer.writeCharSequence(": ", this.charset);
    buffer.writeCharSequence(message.message(), this.charset);
    output.add(buffer);
  }

  private void encodeRFC5424(ChannelHandlerContext context, Message message, List<Object> output) {
    final ByteBuf buffer = context.alloc().buffer();


    EncoderHelper.appendPriority(buffer, message);
    if (null != message.version()) {
      buffer.writeCharSequence(message.version().toString(), this.charset);
    }
    buffer.writeBytes(EncoderHelper.SPACE);
    buffer.writeCharSequence(message.date().format(this.cefDateFormat), this.charset);
    buffer.writeCharSequence(" ", this.charset);
    buffer.writeCharSequence(message.host(), this.charset);
    buffer.writeCharSequence(" ", this.charset);

    if (null != message.appName()) {
      buffer.writeCharSequence(message.appName(), this.charset);
    }
    if (null != message.processId()) {
      buffer.writeCharSequence(message.processId(), this.charset);
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

  @Override
  protected void encode(ChannelHandlerContext context, Message message, List<Object> list) throws Exception {
    switch (message.type()) {
      case CEF:
        encodeCEF(context, message, list);
        break;
      case RFC3164:
        encodeRFC3164(context, message, list);
        break;
      case RFC5424:
        encodeRFC5424(context, message, list);
        break;
      default:
        break;
    }
  }

}
