package com.github.jcustenborder.netty.syslog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateTimeFormatterTest {
  public static class Dummy extends MessageParser {

    @Override
    public boolean parse(SyslogRequest request, List<Object> output) {
      return false;
    }
  }

  MessageParser parser;

  @BeforeEach
  public void before() {
    this.parser = new Dummy();
  }

  @Test
  public void foo() {

    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
        .appendPattern("MMM dd")
        .optionalStart()
        .appendPattern("[ yyyy]")
        .parseDefaulting(ChronoField.YEAR_OF_ERA, 1)
        .optionalEnd()
        .appendPattern(" HH:mm:ss")
        .toFormatter();
    System.out.println(Instant.now().atOffset(ZoneOffset.UTC).format(formatter));


    LocalDateTime localDateTime = (LocalDateTime) formatter.parseBest("Mar 12 12:00:08", OffsetDateTime::from, LocalDateTime::from);
    System.out.println(localDateTime.format(formatter));

    localDateTime = (LocalDateTime) formatter.parseBest("Mar 12 2017 12:00:08", OffsetDateTime::from, LocalDateTime::from);


    final OffsetDateTime expected = Instant.now().atOffset(ZoneOffset.UTC);
    String temp = expected.format(formatter);

    System.out.println(temp);
    final OffsetDateTime actual = LocalDateTime.parse(temp, formatter).atOffset(ZoneOffset.UTC);
    assertEquals(expected, actual);

//    OffsetDateTime dateTime = this.parser.parseDate("Mar 12 12:00:08");
//    assertNotNull(dateTime);
  }



}
