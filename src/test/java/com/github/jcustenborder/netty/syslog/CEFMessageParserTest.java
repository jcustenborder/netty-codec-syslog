package com.github.jcustenborder.netty.syslog;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class CEFMessageParserTest extends MessageParserTest<CEFSyslogMessage, CEFMessageParser> {


  @Override
  protected void assertMessage(CEFSyslogMessage expected, CEFSyslogMessage actual) {
    if (null != expected) {
      assertNotNull(actual, "actual should not be null.");
    } else {
      assertNull(actual, "actual should be null.");
      return;
    }

    assertEquals(expected.facility(), actual.facility(), "facility should match.");
//    assertEquals(expected.message(), actual.message(), "message should match.");
    assertEquals(expected.level(), actual.level(), "level should match.");
    assertEquals(expected.remoteAddress(), actual.remoteAddress(), "remoteAddress should match.");
    assertEquals(expected.date(), actual.date(), "date should match.");
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

  //  @Disabled
  @Test
  public void foo() throws IOException {

    List<String> lines = new ArrayList<>();
    try (Reader streamReader = new InputStreamReader(this.getClass().getResourceAsStream("/syslog.txt"))) {
      try (BufferedReader reader = new BufferedReader(streamReader)) {
        String line;
        while (null != (line = reader.readLine())) {
          lines.add(line);
        }
      }
    }

    int index = 0;

    File outputRoot = new File("src/test/resources/com/github/jcustenborder/netty/syslog/cef");

    for (String s : lines) {
      TestCase testCase = new TestCase();
      testCase.input = s;

      List<Object> output = new ArrayList<>(1);
      if (parse(output, testCase.input)) {
        testCase.expected = (CEFSyslogMessage) output.get(0);
        String filename = String.format("cef%03d.json", index);
        File outputFile = new File(outputRoot, filename);
        this.mapper.writeValue(outputFile, testCase);
        index++;
      }
    }
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
