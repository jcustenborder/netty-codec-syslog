package com.github.jcustenborder.netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class MessageEncoderTest {
  MessageEncoder encoder;

  @BeforeEach
  public void setup() {
    this.encoder = new MessageEncoder(DateTimeFormatter.ofPattern("MMM d HH:mm:ss"));
  }

  protected abstract File testsPath();

  @TestFactory
  public Stream<DynamicTest> encode() {
    final File testsPath = testsPath();
    return Arrays.stream(testsPath.listFiles()).map(file -> dynamicTest(file.getName(), () -> {
      final TestCase testCase = ObjectMapperFactory.INSTANCE.readValue(file, TestCase.class);
      ChannelHandlerContext context = mock(ChannelHandlerContext.class);
      when(context.alloc()).thenReturn(ByteBufAllocator.DEFAULT);
      List<Object> output = new ArrayList<>();

      this.encoder.encode(context, testCase.expected, output);
      assertFalse(output.isEmpty());
      ByteBuf actual = (ByteBuf) output.get(0);
      assertNotNull(actual, "actual should not be null.");
      String a = actual.toString(Charset.forName("UTF-8")).replaceAll("\\s+", " ");
      assertEquals(testCase.input.replaceAll("\\s+", " "), a);
    }));
  }
}
