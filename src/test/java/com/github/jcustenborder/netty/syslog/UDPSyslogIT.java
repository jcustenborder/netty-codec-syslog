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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class UDPSyslogIT extends SyslogIT {
  private static final Logger log = LoggerFactory.getLogger(UDPSyslogIT.class);

  @Override
  protected int port() {
    return 20001;
  }

  @Override
  protected SyslogIF syslogIF() {
    SyslogIF syslogIF = Syslog.getInstance("UDP");
    syslogIF.getConfig().setHost("127.0.0.1");
    syslogIF.getConfig().setPort(port());
    return syslogIF;
  }

  @Override
  protected ChannelFuture setupServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, TestSyslogMessageHandler handler) throws InterruptedException {
    Bootstrap b = new Bootstrap();
    b.group(workerGroup)
        .channel(NioDatagramChannel.class)
        .handler(new ChannelInitializer<DatagramChannel>() {
          @Override
          protected void initChannel(DatagramChannel datagramChannel) throws Exception {
            ChannelPipeline channelPipeline = datagramChannel.pipeline();
            channelPipeline.addLast(
                new LoggingHandler("Syslog", LogLevel.TRACE),
                new UDPSyslogMessageDecoder(),
                new SyslogMessageHandler(),
                handler
            );
          }
        });

    return b.bind(InetAddress.getLoopbackAddress(), port());
  }
}
