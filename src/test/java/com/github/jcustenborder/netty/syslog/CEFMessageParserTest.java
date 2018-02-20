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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class CEFMessageParserTest extends MessageParserTest<CEFSyslogMessage, CEFMessageParser> {


  @Override
  protected void assertMessage(CEFSyslogMessage expected, CEFSyslogMessage actual) {
    MessageAssertions.assertMessage(expected, actual);
    assertEquals(expected.deviceEventClassId(), actual.deviceEventClassId(), "deviceEventClassId does not match.");
    assertEquals(expected.deviceProduct(), actual.deviceProduct(), "deviceProduct does not match.");
    assertEquals(expected.deviceVendor(), actual.deviceVendor(), "deviceVendor does not match.");
    assertEquals(expected.deviceVersion(), actual.deviceVersion(), "deviceVersion does not match.");
    assertEquals(expected.name(), actual.name(), "name does not match.");
    assertEquals(expected.severity(), actual.severity(), "severity does not match.");
    assertEquals(expected.extension(), actual.extension(), "extension does not match.");
  }

  @TestFactory
  public Stream<DynamicTest> parse() {
    final File testsPath = new File("src/test/resources/com/github/jcustenborder/netty/syslog/cef");
    return Arrays.stream(testsPath.listFiles()).map(file -> dynamicTest(file.getName(), () -> {
      final TestCase testCase = this.mapper.readValue(file, TestCase.class);
      List<Object> output = new ArrayList<>();
      parse(output, testCase.input);
      assertFalse(output.isEmpty());
      CEFSyslogMessage actual = (CEFSyslogMessage) output.get(0);
      assertNotNull(actual, "actual should not be null.");
      assertMessage(testCase.expected, actual);
    }));
  }

  @Override
  protected CEFMessageParser createParser() {
    return new CEFMessageParser();
  }

  public static class TestCase {
    public String input;
    public CEFSyslogMessage expected;
  }
}
