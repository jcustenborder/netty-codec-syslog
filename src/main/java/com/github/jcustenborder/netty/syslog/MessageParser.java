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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
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

public abstract class MessageParser {
  private static final Logger log = LoggerFactory.getLogger(MessageParser.class);
  private static final String NULL_TOKEN = "-";
  protected final ThreadLocal<List<Format>> dateFormats;
  private final ThreadLocal<Matcher> matcherStructuredData;
  private final ThreadLocal<Matcher> matcherKeyValue;

  public MessageParser(TimeZone timeZone) {
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
    this.matcherStructuredData = initMatcher("\\[([^\\]]+)\\]");
    this.matcherKeyValue = initMatcher("(?<key>\\S+)=\"(?<value>[^\"]+)\"|(?<id>\\S+)");
  }

  public MessageParser(String timeZoneId) {
    this(TimeZone.getTimeZone(timeZoneId));
  }

  /**
   * Method is used to parse an incoming syslog message.
   *
   * @param output        Output to write the message to.
   * @param remoteAddress Remote address for the sender.
   * @param rawMessage    Raw Message to parse.
   * @return true if was parsed successfully. False if not.
   */
  public abstract boolean parse(
      final List<Object> output,
      final InetAddress remoteAddress,
      final String rawMessage
  );

  protected final ThreadLocal<Matcher> initMatcher(String pattern) {
    return initMatcher(pattern, 0);
  }

  protected final ThreadLocal<Matcher> initMatcher(String pattern, int flags) {
    final Pattern p = Pattern.compile(pattern, flags);
    return new MatcherInheritableThreadLocal(p);
  }

  protected String nullableString(String groupText) {
    return NULL_TOKEN.equals(groupText) ? null : groupText;
  }

  protected Date parseDate(String date) {
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

  protected List<StructuredSyslogMessage.StructuredData> parseStructuredData(String structuredData) {
    log.trace("parseStructuredData() - structuredData = '{}'", structuredData);
    final Matcher matcher = matcherStructuredData.get().reset(structuredData);
    final List<StructuredSyslogMessage.StructuredData> result = new ArrayList<>();
    while (matcher.find()) {
      final String input = matcher.group(1);
      log.trace("parseStructuredData() - input = '{}'", input);
      ImmutableStructuredData.Builder builder = ImmutableStructuredData.builder();

      final Matcher kvpMatcher = matcherKeyValue.get().reset(input);
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

  protected int facility(int priority) {
    return priority >> 3;
  }

  protected int level(int priority, int facility) {
    return priority - (facility << 3);
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
}
