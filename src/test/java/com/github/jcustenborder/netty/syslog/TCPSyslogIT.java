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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogIF;

public class TCPSyslogIT extends SyslogIT {
  @Override
  protected int port() {
    return 20000;
  }

  @Override
  protected SyslogIF syslogIF() {
    SyslogIF syslogIF = Syslog.getInstance("TCP");
    syslogIF.getConfig().setHost("127.0.0.1");
    syslogIF.getConfig().setPort(port());
    return syslogIF;
  }

  @Override
  protected ChannelFuture setupServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, TestSyslogMessageHandler handler) throws InterruptedException {
    ServerBootstrap b = new ServerBootstrap(); // (2)
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class) // (3)
        .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
          @Override
          public void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(
                new LoggingHandler("Syslog", LogLevel.INFO),
                new DelimiterBasedFrameDecoder(2000, true, Delimiters.lineDelimiter()),
                new TCPSyslogMessageDecoder(),
                new SyslogMessageHandler(),
                handler
            );
          }
        })

        .option(ChannelOption.SO_BACKLOG, 128)          // (5)
        .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
    // Bind and start to accept incoming connections.
    return b.bind(port()).sync(); // (7)
  }
}
