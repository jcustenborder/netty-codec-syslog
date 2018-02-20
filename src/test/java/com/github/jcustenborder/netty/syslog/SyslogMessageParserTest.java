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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class SyslogMessageParserTest extends AbstractMessageParserTest<SyslogMessage, SyslogMessageParser> {

  @Override
  protected SyslogMessageParser createParser() {
    return new SyslogMessageParser("UTC");
  }

  @TestFactory
  public Stream<DynamicTest> parse() {
    final File testsPath = new File("src/test/resources/com/github/jcustenborder/netty/syslog/syslog");
    return Arrays.stream(testsPath.listFiles()).map(file -> dynamicTest(file.getName(), () -> {
      final TestCase testCase = this.mapper.readValue(file, TestCase.class);
      List<Object> output = new ArrayList<>();
      parse(output, testCase.input);
      assertFalse(output.isEmpty());
      SyslogMessage actual = (SyslogMessage) output.get(0);
      assertNotNull(actual, "actual should not be null.");
      assertMessage(testCase.expected, actual);
    }));
  }

//  @Disabled
//  @Test
//  public void foo() throws IOException {
//
//    List<String> lines = new ArrayList<>();
//    try (Reader streamReader = new InputStreamReader(this.getClass().getResourceAsStream("/syslog.txt"))) {
//      try (BufferedReader reader = new BufferedReader(streamReader)) {
//        String line;
//        while (null != (line = reader.readLine())) {
//          lines.add(line);
//        }
//      }
//    }
//
//    int index = 13;
//
//    File outputRoot = new File("src/test/resources/com/github/jcustenborder/netty/syslog/syslog");
//
//    for (String s : lines) {
//      TestCase testCase = new TestCase();
//      testCase.input = s;
//      testCase.expected = parse(testCase.input);
//      String filename = String.format("syslog%03d.json", index);
//      File outputFile = new File(outputRoot, filename);
//      index++;
//
//      this.mapper.writeValue(outputFile, testCase);
//    }
//  }

  @Override
  protected void assertMessage(SyslogMessage expected, SyslogMessage actual) {
    if (null != expected) {
      assertNotNull(actual, "actual should not be null.");
    } else {
      assertNull(actual, "actual should be null.");
      return;
    }

    assertEquals(expected.facility(), actual.facility(), "facility should match.");
    assertEquals(expected.message(), actual.message(), "message should match.");
    assertEquals(expected.level(), actual.level(), "level should match.");
    assertEquals(expected.remoteAddress(), actual.remoteAddress(), "remoteAddress should match.");
    assertEquals(expected.date(), actual.date(), "date should match.");
  }


  public static class TestCase {
    public String input;
    public SyslogMessage expected;
  }
}
