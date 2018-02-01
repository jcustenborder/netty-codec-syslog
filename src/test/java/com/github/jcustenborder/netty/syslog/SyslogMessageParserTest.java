package com.github.jcustenborder.netty.syslog;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
      final SyslogMessage actual = parse(testCase.input);
      assertMessage(testCase.expected, actual);
    }));
  }


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
