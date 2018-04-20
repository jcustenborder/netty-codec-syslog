/**
 * Copyright © 2018 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class MessageParserTest<M extends Message, P extends MessageParser<M>, T extends TestCase<M>> {
  private static final Logger log = LoggerFactory.getLogger(MessageParserTest.class);

  protected P parser;

  @BeforeEach
  public void setup() {
    this.parser = createParser();
  }

  protected abstract void assertMessage(M expected, M actual);

  protected abstract P createParser();

  protected abstract Class<T> testCaseClass();

  protected abstract File testsPath();

  @TestFactory
  public Stream<DynamicTest> parse() {
    final File testsPath = testsPath();
    final Class<T> testCaseClass = testCaseClass();

    return Arrays.stream(testsPath.listFiles(p -> p.getName().endsWith(".json"))).map(file -> dynamicTest(file.getName(), () -> {
      final T testCase = ObjectMapperFactory.INSTANCE.readValue(file, testCaseClass);
      SyslogRequest request = mock(SyslogRequest.class);
      when(request.rawMessage()).thenReturn(testCase.input);
      when(request.remoteAddress()).thenReturn(InetAddress.getLoopbackAddress());
      M actual = this.parser.parse(request);
      assertNotNull(actual, "actual should not be null.");
      assertMessage(testCase.expected, actual);
    }));
  }
}
