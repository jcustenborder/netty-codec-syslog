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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class MessageParserTest<P extends MessageParser> {
  private static final Logger log = LoggerFactory.getLogger(MessageParserTest.class);

  protected P parser;

  @BeforeEach
  public void setup() {
    this.parser = createParser();
  }

  void assertMessage(Message expected, Message actual) {
    if (null != expected) {
      assertNotNull(actual, "actual should not be null.");
    } else {
      assertNull(actual, "actual should be null.");
      return;
    }

    assertEquals(expected.facility(), actual.facility(), "facility should match.");
    assertEquals(expected.level(), actual.level(), "level should match.");
    assertEquals(expected.remoteAddress(), actual.remoteAddress(), "remoteAddress should match.");
    assertEquals(expected.date(), actual.date(), "date should match.");
    assertEquals(expected.rawMessage(), actual.rawMessage(), "rawMessage should match.");

    assertEquals(expected.deviceEventClassId(), actual.deviceEventClassId(), "deviceEventClassId does not match.");
    assertEquals(expected.deviceProduct(), actual.deviceProduct(), "deviceProduct does not match.");
    assertEquals(expected.deviceVendor(), actual.deviceVendor(), "deviceVendor does not match.");
    assertEquals(expected.deviceVersion(), actual.deviceVersion(), "deviceVersion does not match.");
    assertEquals(expected.name(), actual.name(), "name does not match.");
    assertEquals(expected.severity(), actual.severity(), "severity does not match.");
    assertEquals(expected.extension(), actual.extension(), "extension does not match.");
  }

  protected abstract P createParser();

  protected abstract File testsPath();


  @TestFactory
  public Stream<DynamicTest> parse() {
    final File testsPath = testsPath();

    return Arrays.stream(testsPath.listFiles(p -> p.getName().endsWith(".json"))).map(file -> dynamicTest(file.getName(), () -> {
      final TestCase testCase = ObjectMapperFactory.INSTANCE.readValue(file, TestCase.class);
      SyslogRequest request = mock(SyslogRequest.class);
      when(request.rawMessage()).thenReturn(testCase.input);
      when(request.remoteAddress()).thenReturn(InetAddress.getLoopbackAddress());
      Message actual = this.parser.parse(request);
      ObjectMapperFactory.INSTANCE.writeValue(file, testCase);
      assertNotNull(actual, "actual should not be null.");
      assertMessage(testCase.expected, actual);
    }));
  }
}
