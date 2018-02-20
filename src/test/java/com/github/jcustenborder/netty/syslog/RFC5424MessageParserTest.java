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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class RFC5424MessageParserTest extends MessageParserTest<StructuredSyslogMessage, RFC5424MessageParser> {


  @Override
  protected void assertMessage(StructuredSyslogMessage expected, StructuredSyslogMessage actual) {
    MessageAssertions.assertMessage(expected, actual);
  }

  @TestFactory
  public Stream<DynamicTest> parse() {
    final File testsPath = new File("src/test/resources/com/github/jcustenborder/netty/syslog/rfc5424");
    return Arrays.stream(testsPath.listFiles()).map(file -> dynamicTest(file.getName(), () -> {
      final TestCase testCase = this.mapper.readValue(file, TestCase.class);
      List<Object> output = new ArrayList<>();
      parse(output, testCase.input);
      assertFalse(output.isEmpty());
      StructuredSyslogMessage actual = (StructuredSyslogMessage) output.get(0);
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

    File outputRoot = new File("src/test/resources/com/github/jcustenborder/netty/syslog/rfc5424");

    for (String s : lines) {
      TestCase testCase = new TestCase();
      testCase.input = s;

      List<Object> output = new ArrayList<>(1);
      if(parse(output, testCase.input)) {
        testCase.expected = (StructuredSyslogMessage) output.get(0);
        String filename = String.format("structured%03d.json", index);
        File outputFile = new File(outputRoot, filename);
        this.mapper.writeValue(outputFile, testCase);
        index++;
      }
    }
  }

  @Override
  protected RFC5424MessageParser createParser() {
    return new RFC5424MessageParser();
  }

  public static class TestCase {
    public String input;
    public StructuredSyslogMessage expected;
  }
}
