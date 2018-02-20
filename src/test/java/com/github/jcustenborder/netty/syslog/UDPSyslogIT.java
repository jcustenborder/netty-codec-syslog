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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogIF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UDPSyslogIT {
  private static final Logger log = LoggerFactory.getLogger(UDPSyslogIT.class);

  EventLoopGroup bossGroup;
  Bootstrap b;
  ChannelFuture channelFuture;
  SyslogMessageHandler handler;

  class SyslogMessageHandler extends SimpleChannelInboundHandler<SyslogMessage> {
    List<SyslogMessage> messages = new ArrayList<>(1024);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SyslogMessage message) throws Exception {
      messages.add(message);
    }
  }

  @BeforeEach
  public void setup() throws InterruptedException {
    this.bossGroup = new NioEventLoopGroup(1);

    this.b = new Bootstrap();
    this.b.group(bossGroup)
        .channel(NioDatagramChannel.class)
        .handler(new ChannelInitializer<DatagramChannel>() {
          @Override
          protected void initChannel(DatagramChannel datagramChannel) throws Exception {
            ChannelPipeline channelPipeline = datagramChannel.pipeline();
            channelPipeline.addLast(
                new LoggingHandler("Syslog", LogLevel.TRACE),
                new UDPSyslogMessageDecoder(),
                handler = new SyslogMessageHandler()
            );
          }
        });

    this.channelFuture = b.bind(InetAddress.getLoopbackAddress(), 12345);
    this.channelFuture.await();
  }

  @Test
  public void roundtrip() throws InterruptedException {
    SyslogIF syslogIF = Syslog.getInstance("UDP");
    syslogIF.getConfig().setHost("127.0.0.1");
    syslogIF.getConfig().setPort(12345);
    for (int i = 0; i < 100; i++)
      syslogIF.info("foo");
    Thread.sleep(100);
    assertEquals(100, this.handler.messages.size());
  }

  @AfterEach
  public void teardown() throws InterruptedException, ExecutionException, TimeoutException {
    Future<?> shutdown = this.bossGroup.shutdownGracefully(1, 10, TimeUnit.SECONDS);
    shutdown.get(30, TimeUnit.SECONDS);
  }

}
