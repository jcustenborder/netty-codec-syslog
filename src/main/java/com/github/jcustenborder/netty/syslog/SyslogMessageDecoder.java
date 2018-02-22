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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SyslogMessageDecoder extends MessageToMessageDecoder<SyslogRequest> {
  private static final Logger log = LoggerFactory.getLogger(SyslogMessageDecoder.class);

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

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, SyslogRequest request, List<Object> output) throws Exception {
    log.trace("decode() - request = '{}'", request);

    for (MessageParser parser : this.parsers) {
      if (parser.parse(request, output)) {
        return;
      }
    }

    log.warn("decode() - Could not parse message. request = '{}'", request);

    output.add(
        ImmutableUnparseableMessage.builder()
            .date(new Date())
            .rawMessage(request.rawMessage())
            .remoteAddress(request.remoteAddress())
            .build()
    );
  }
}
