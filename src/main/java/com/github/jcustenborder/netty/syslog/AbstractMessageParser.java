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

import io.netty.buffer.ByteBuf;
import io.netty.channel.unix.DatagramSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractMessageParser {
  private static final Logger log = LoggerFactory.getLogger(AbstractMessageParser.class);
  static final Charset CHARSET = Charset.forName("UTF-8");

  protected final ThreadLocal<List<Format>> dateFormats;

  public AbstractMessageParser(TimeZone timeZone) {
    this.dateFormats = new InheritableThreadLocal<List<Format>>() {
      @Override
      protected List<Format> initialValue() {
        List<Format> formats = Arrays.asList(
            new Format("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", timeZone),
            new Format("yyyy-MM-dd'T'HH:mm:ssZ", timeZone),
            new Format("MMM dd hh:mm:ss", timeZone),
            new Format("MMM dd yyyy hh:mm:ss", timeZone)
        );
        return formats;
      }
    };
  }

  public AbstractMessageParser(String timeZoneId) {
    this(TimeZone.getTimeZone(timeZoneId));
  }


  private static final String NULL_TOKEN = "-";

  private static final Pattern MESSAGE_PATTERN_RFC5424 = Pattern.compile("^<(?<priority>\\d+)>(?<version>\\d{1,3})\\s*(?<date>[0-9:+-TZ]+)\\s*(?<host>\\S+)\\s*(?<appname>\\S+)\\s*(?<procid>\\S+)\\s*(?<msgid>\\S+)\\s*(?<structureddata>(-|\\[.+\\]))\\s*(?<message>.+)$");
  private static final Pattern MESSAGE_PATTERN_RFC3164 = Pattern.compile("^<(?<priority>\\d+)>(?<date>([a-zA-Z]{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)|([0-9T:.Z-]+))\\s+(?<host>\\S+)\\s+((?<appname>\\S+):)*\\s*(?<message>.+)$");
  private static final Pattern MESSAGE_PATTERN_CISCO = Pattern.compile("^(?<date>([a-zA-Z]{3}\\s+\\d+\\s+\\d+\\s+\\d+:\\d+:\\d+)):\\s+%(?<host>\\S+):\\s*(?<message>.+)$");
  private static final Pattern MESSAGE_PATTERN_FALLBACK = Pattern.compile("^(?<date>([a-zA-Z]{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)) (?<host>\\S+) (?<appname>\\S+)\\[(?<procid>\\d+)\\]:\\s(?<message>.*)$");
  private static final Pattern STRUCTURED_DATA_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");
  private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("(?<key>\\S+)=\"(?<value>[^\"]+)\"|(?<id>\\S+)");

  private static final ThreadLocal<Matcher> MATCHER_RFC_5424 = new MatcherInheritableThreadLocal(MESSAGE_PATTERN_RFC5424);
  private static final ThreadLocal<Matcher> MATCHER_RFC_3164 = new MatcherInheritableThreadLocal(MESSAGE_PATTERN_RFC3164);
  private static final ThreadLocal<Matcher> MATCHER_CISCO = new MatcherInheritableThreadLocal(MESSAGE_PATTERN_CISCO);
  private static final ThreadLocal<Matcher> MATCHER_FALLBACK = new MatcherInheritableThreadLocal(MESSAGE_PATTERN_FALLBACK);
  private static final ThreadLocal<Matcher> MATCHER_STRUCTURED_DATA = new MatcherInheritableThreadLocal(STRUCTURED_DATA_PATTERN);
  private static final ThreadLocal<Matcher> MATCHER_KEY_VALUE = new MatcherInheritableThreadLocal(KEY_VALUE_PATTERN);


  private Date parseDate(String date) {
    List<Format> dateFormatList = dateFormats.get();
    Date result = null;

    for (Format format : dateFormatList) {
      result = format.parse(date);

      if (null != result) {
        break;
      }
    }
    if (null == result) {
      log.error("Could not parse date '{}'", date);
    }
    return result;
  }

  protected abstract void parse(
      List<Object> output,
      String rawMessage,
      Date date,
      InetAddress remoteAddress,
      String host,
      Integer facility,
      Integer level,
      Integer version,
      String appName,
      String procID,
      String messageID,
      List<StructuredSyslogMessage.StructuredData> structuredData,
      String message);


  static String nullableString(String groupText) {
    return NULL_TOKEN.equals(groupText) ? null : groupText;
  }

  boolean parseFallback(List<Object> output, InetAddress remoteAddress, String message) {
    final Matcher matcher = MATCHER_FALLBACK.get().reset(message);

    if (!matcher.find()) {
      log.trace("parseFallback() - Could not match message. message='{}'", message);
      return false;
    }

    final String groupDate = matcher.group("date");
    final String groupHost = matcher.group("host");
    final String groupMessage = matcher.group("message");
    final String groupAppName = matcher.group("appname");
    final String groupProcID = matcher.group("procid");
    final Date date = parseDate(groupDate);

    parse(
        output,
        message,
        date,
        remoteAddress,
        groupHost,
        null,
        null,
        null,
        groupAppName,
        groupProcID,
        null,
        null,
        groupMessage);

    return true;
  }

  boolean parseCisco(List<Object> output, InetAddress remoteAddress, String message) {
    final Matcher matcher = MATCHER_CISCO.get().reset(message);

    if (!matcher.find()) {
      log.trace("parseCisco() - Could not match message. message='{}'", message);
      return false;
    }

    final String groupDate = matcher.group("date");
    final String groupHost = matcher.group("host");
    final String groupMessage = matcher.group("message");
    final Date date = parseDate(groupDate);

    parse(
        output,
        message,
        date,
        remoteAddress,
        groupHost,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        groupMessage);

    return true;
  }

  boolean parseRFC5424(List<Object> output, InetAddress remoteAddress, String message) {
    final Matcher matcher = MATCHER_RFC_5424.get().reset(message);

    if (!matcher.find()) {
      log.trace("parseRFC5424() - Could not match message. message='{}'", message);
      return false;
    }
    log.trace("parseRFC5424() - Parsed message as RFC 5424");
    final String groupPriority = matcher.group("priority");
    final String groupVersion = matcher.group("version");
    final String groupDate = matcher.group("date");
    final String groupHost = matcher.group("host");
    final String groupAppName = matcher.group("appname");
    final String groupProcID = matcher.group("procid");
    final String groupMessageID = matcher.group("msgid");
    final String groupStructuredData = matcher.group("structureddata");
    final String groupMessage = matcher.group("message");

    final int priority = Integer.parseInt(groupPriority);
    final int facility = facility(priority);
    final Date date = parseDate(groupDate);
    final int level = level(priority, facility);
    final Integer version = Integer.parseInt(groupVersion);
    final String appName = nullableString(groupAppName);
    final String procID = nullableString(groupProcID);
    final String messageID = nullableString(groupMessageID);

    final List<StructuredSyslogMessage.StructuredData> structuredData = parseStructuredData(groupStructuredData);

    parse(
        output,
        message,
        date,
        remoteAddress,
        groupHost,
        facility,
        level,
        version,
        appName,
        procID,
        messageID,
        structuredData, groupMessage);
    return true;
  }

  boolean parseRFC3164(List<Object> output, InetAddress remoteAddress, String message) {
    final Matcher matcher = MATCHER_RFC_3164.get().reset(message);

    if (!matcher.find()) {
      log.trace("parseRFC3164() - Could not match message. message='{}'", message);
      return false;
    }
    log.trace("parseRFC3164() - Parsed message as RFC 3164");
    final String groupPriority = matcher.group("priority");
    final String groupDate = matcher.group("date");
    final String groupHost = matcher.group("host");
    final String groupMessage = matcher.group("message");
    final String groupAppName = matcher.group("appname");
    final int priority = Integer.parseInt(groupPriority);
    final int facility = facility(priority);
    final Date date = parseDate(groupDate);
    final int level = level(priority, facility);

    parse(
        output,
        message,
        date,
        remoteAddress,
        groupHost,
        facility,
        level,
        null,
        groupAppName,
        null,
        null,
        null,
        groupMessage);

    return true;
  }

  static int facility(int priority) {
    return priority >> 3;
  }

  static int level(int priority, int facility) {
    return priority - (facility << 3);
  }


  static List<StructuredSyslogMessage.StructuredData> parseStructuredData(String structuredData) {
    log.trace("parseStructuredData() - structuredData = '{}'", structuredData);
    final Matcher matcher = MATCHER_STRUCTURED_DATA.get().reset(structuredData);
    final List<StructuredSyslogMessage.StructuredData> result = new ArrayList<>();
    while (matcher.find()) {
      final String input = matcher.group(1);
      log.trace("parseStructuredData() - input = '{}'", input);
      ImmutableStructuredData.Builder builder = ImmutableStructuredData.builder();

      final Matcher kvpMatcher = MATCHER_KEY_VALUE.get().reset(input);
      while (kvpMatcher.find()) {
        final String key = kvpMatcher.group("key");
        final String value = kvpMatcher.group("value");
        final String id = kvpMatcher.group("id");

        if (null != id && !id.isEmpty()) {
          log.trace("parseStructuredData() - id='{}'", id);
          builder.id(id);
        } else {
          log.trace("parseStructuredData() - key='{}' value='{}'", key, value);
          builder.putStructuredDataElements(key, value);
        }
      }
      result.add(builder.build());
    }
    return result;
  }


  public void parse(List<Object> output, SocketAddress socketAddress, ByteBuf buf) {
    final InetAddress remoteAddress;

    if (socketAddress instanceof DatagramSocketAddress) {
      remoteAddress = ((DatagramSocketAddress) socketAddress).getAddress();
    } else if (socketAddress instanceof InetSocketAddress) {
      remoteAddress = ((InetSocketAddress) socketAddress).getAddress();
    } else {
      remoteAddress = null;
    }

    final String message = buf.toString(CHARSET);
    log.trace("parse() - message = '{}'", message);

    boolean result = parseRFC5424(output, remoteAddress, message);

    if (result) {
      return;
    }

    result = parseRFC3164(output, remoteAddress, message);

    if (result) {
      return;
    }

    result = parseFallback(output, remoteAddress, message);

    if (result) {
      return;
    }

    result = parseCisco(output, remoteAddress, message);

    if (result) {
      return;
    }

    log.error("Could not parse message. message='{}'", message);
    output.add(
        ImmutableUnparseableMessage.builder()
            .date(new Date())
            .rawMessage(message)
            .remoteAddress(remoteAddress)
            .build()
    );
  }

  static class Format {
    public final boolean hasYear;
    public final SimpleDateFormat format;
    public final TimeZone timeZone;

    Format(String format, TimeZone timeZone) {
      this.timeZone = timeZone;
      this.format = new SimpleDateFormat(format);
      this.format.setTimeZone(this.timeZone);
      this.hasYear = this.format.toPattern().contains("y");
    }

    public Date parse(String s) {
      Date date;

      try {
        date = this.format.parse(s);

        if (!this.hasYear) {
          final Calendar calendar = Calendar.getInstance(this.timeZone);
          final int year = calendar.get(Calendar.YEAR);
          calendar.setTime(date);
          calendar.set(Calendar.YEAR, year);
          date = calendar.getTime();
        }
      } catch (ParseException e) {

        date = null;
      }

      return date;
    }
  }

  static class MatcherInheritableThreadLocal extends InheritableThreadLocal<Matcher> {
    private final Pattern pattern;

    MatcherInheritableThreadLocal(Pattern pattern) {
      this.pattern = pattern;
    }

    @Override
    protected Matcher initialValue() {
      return this.pattern.matcher("");
    }
  }
}
