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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class RFC5424MessageParserTest extends MessageParserTest<RFC5424Message, RFC5424MessageParser> {


  @Override
  protected void assertMessage(RFC5424Message expected, RFC5424Message actual) {
    MessageAssertions.assertMessage(expected, actual);
  }

  @TestFactory
  public Stream<DynamicTest> parse() {
    final File testsPath = new File("src/test/resources/com/github/jcustenborder/netty/syslog/rfc5424");
    return Arrays.stream(testsPath.listFiles()).map(file -> dynamicTest(file.getName(), () -> {
      final RFC5424TestCase testCase = ObjectMapperFactory.INSTANCE.readValue(file, RFC5424TestCase.class);
      List<Object> output = new ArrayList<>();
      parse(output, testCase.input);
      assertFalse(output.isEmpty());
      RFC5424Message actual = (RFC5424Message) output.get(0);
      assertNotNull(actual, "actual should not be null.");
      assertMessage(testCase.expected, actual);
    }));
  }

  @Override
  protected RFC5424MessageParser createParser() {
    return new RFC5424MessageParser();
  }

}
