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

  protected abstract ChannelFuture setupServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, SyslogMessageHandler handler) throws InterruptedException;

  private EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
  private EventLoopGroup workerGroup = new NioEventLoopGroup();
  protected ChannelFuture channelFuture;
  protected SyslogMessageHandler handler;

  @BeforeEach
  public void setup() throws InterruptedException {
    this.bossGroup = new NioEventLoopGroup();
    this.workerGroup = new NioEventLoopGroup();
    this.handler = new SyslogMessageHandler();
    this.channelFuture = setupServer(this.bossGroup, this.workerGroup, this.handler);
    Thread.sleep(500);
  }

  @Test
  public void roundtrip() throws InterruptedException {
    SyslogIF syslogIF = syslogIF();
    for (int i = 0; i < 100; i++) {
      syslogIF.info("foo");
    }
    syslogIF.flush();
    Thread.sleep(100);
    assertEquals(100, this.handler.messages.size());
  }



  @AfterEach
  public void close() throws InterruptedException {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
    this.channelFuture.channel().closeFuture().sync();
  }

}
