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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@ChannelHandler.Sharable
public class SyslogMessageHandler extends SimpleChannelInboundHandler<SyslogRequest> {
  private static final Logger log = LoggerFactory.getLogger(SyslogMessageHandler.class);
  final List<MessageParser> parsers;

  public SyslogMessageHandler(List<MessageParser> parsers) {
    this.parsers = parsers;
  }

  public SyslogMessageHandler() {
    this(
        Arrays.asList(
            new CEFMessageParser(),
            new RFC5424MessageParser(),
            new RFC3164MessageParser()
        )
    );
  }

  @Override
  protected void channelRead0(ChannelHandlerContext context, SyslogRequest request) throws Exception {
    log.trace("channelRead0() - request = '{}'", request);
    context.executor().submit(() -> {
      for (MessageParser parser : parsers) {
        Object result = parser.parse(request);

        if (null != result) {
          log.trace("channelRead0() - add result = '{}'", result);
          context.fireChannelRead(result);
          return;
        }
      }

      log.warn("decode() - Could not parse message. request = '{}'", request);
      Message unparseableMessage = ImmutableSyslogMessage.builder()
          .type(MessageType.UNKNOWN)
          .date(LocalDateTime.now())
          .rawMessage(request.rawMessage())
          .remoteAddress(request.remoteAddress())
          .build();
      context.write(unparseableMessage);
    });
  }
}
