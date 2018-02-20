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
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class SyslogMessageDecoder {
  private static final Logger log = LoggerFactory.getLogger(SyslogMessageDecoder.class);
  private static final Charset CHARSET = Charset.forName("UTF-8");

  final List<MessageParser> parsers;

  public SyslogMessageDecoder(List<MessageParser> parsers) {
    this.parsers = parsers;
  }

  public SyslogMessageDecoder() {
    this(
        Arrays.asList(
            new CEFMessageParser(),
            new RFC5424MessageParser(),
            new RFC3164MessageParser()
        )
    );
  }


  public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> output) throws Exception {
    final String rawMessage = byteBuf.toString(CHARSET);
    final SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
    if (socketAddress instanceof InetSocketAddress) {
      decode(output, ((InetSocketAddress) socketAddress).getAddress(), rawMessage);
    } else {
      throw new UnsupportedOperationException(
          String.format(
              "%s is not supported.",
              socketAddress.getClass().getName()
          )
      );
    }
  }

  public void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> output) throws Exception {
    final ByteBuf content = datagramPacket.content();
    final String rawMessage = content.toString(CHARSET);
    decode(output, datagramPacket.recipient().getAddress(), rawMessage);
  }

  void decode(final List<Object> output,
              final InetAddress remoteAddress,
              final String rawMessage) {

    for (MessageParser parser : this.parsers) {
      if (parser.parse(output, remoteAddress, rawMessage)) {
        return;
      }
    }

    log.warn("decode() - Could not parse message. rawMessage = '{}'", rawMessage);

    output.add(
        ImmutableUnparseableMessage.builder()
            .date(new Date())
            .rawMessage(rawMessage)
            .remoteAddress(remoteAddress)
            .build()
    );
  }
}
