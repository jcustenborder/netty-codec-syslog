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
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.nio.charset.Charset;
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

public class RFC5424MessageEncoderTest {

  RFC5424MessageEncoder encoder;

  @BeforeEach
  public void setup() {
    this.encoder = new RFC5424MessageEncoder();
  }

  @TestFactory
  public Stream<DynamicTest> encode() {
    final File testsPath = new File("src/test/resources/com/github/jcustenborder/netty/syslog/rfc5424");
    return Arrays.stream(testsPath.listFiles()).map(file -> dynamicTest(file.getName(), () -> {
      final RFC5424TestCase testCase = ObjectMapperFactory.INSTANCE.readValue(file, RFC5424TestCase.class);
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
