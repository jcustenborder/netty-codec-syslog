/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractMessageParserTest<M extends Message, T extends AbstractMessageParser> {
  protected abstract void assertMessage(M expected, M actual);

  protected abstract T createParser();

  protected ObjectMapper mapper;
  protected T parser;


  protected void parse(List<Object> output, String message) {
    final Charset charset = Charset.forName("UTF-8");
    final byte[] messageBytes = message.getBytes(charset);
    final ByteBuf byteBuf = Unpooled.wrappedBuffer(messageBytes);
    this.parser.parse(output, new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345), byteBuf);
  }


  @BeforeEach
  public void setup() {
    this.mapper = new ObjectMapper();
    this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    this.parser = createParser();
  }


  @TestFactory
  public Stream<DynamicTest> parseStructuredData() {
    Map<String, List<StructuredData>> tests = new LinkedHashMap<>();
    tests.put(
        "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"]",
        Arrays.asList(
            ImmutableStructuredData.builder()
                .id("exampleSDID@32473")
                .putStructuredDataElements("iut", "3")
                .putStructuredDataElements("eventSource", "Application")
                .putStructuredDataElements("eventID", "1011")
                .build()
        )
    );
    tests.put(
        "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"]",
        Arrays.asList(
            ImmutableStructuredData.builder()
                .id("exampleSDID@32473")
                .putStructuredDataElements("iut", "3")
                .putStructuredDataElements("eventSource", "Application")
                .putStructuredDataElements("eventID", "1011")
                .build(),
            ImmutableStructuredData.builder()
                .id("examplePriority@32473")
                .putStructuredDataElements("class", "high")
                .build()
        )
    );

    return tests.entrySet().stream().map(test -> DynamicTest.dynamicTest(test.getKey(), () -> {
      final List<StructuredData> expected = test.getValue();
      final List<StructuredData> actual = AbstractMessageParser.parseStructuredData(test.getKey());
      assertEquals(expected, actual);
    }));
  }
}
