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

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.graylog2.syslog4j.SyslogIF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class SyslogIT {
  protected abstract int port();

  protected abstract SyslogIF syslogIF();

  protected abstract ChannelFuture setupServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, TestSyslogMessageHandler handler) throws InterruptedException;

  private EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
  private EventLoopGroup workerGroup = new NioEventLoopGroup(4);
  protected ChannelFuture channelFuture;
  protected TestSyslogMessageHandler handler;

  @BeforeEach
  public void setup() throws InterruptedException {
    this.bossGroup = new NioEventLoopGroup();
    this.workerGroup = new NioEventLoopGroup();
    this.handler = new TestSyslogMessageHandler();
    this.channelFuture = setupServer(this.bossGroup, this.workerGroup, this.handler);
    Thread.sleep(500);
  }

  @Test
  public void roundtrip() throws InterruptedException {
    final int count = 100;
    SyslogIF syslogIF = syslogIF();
    for (int i = 0; i < count; i++) {
      syslogIF.info("foo");
    }
    syslogIF.flush();

    final long start = System.currentTimeMillis();
    while ((System.currentTimeMillis() - start) < 5000 && this.handler.messages.size() < count) {
      Thread.sleep(100);
    }
    assertEquals(count, this.handler.messages.size());
  }


  @AfterEach
  public void close() throws InterruptedException {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
    this.channelFuture.channel().closeFuture().sync();
  }

}
