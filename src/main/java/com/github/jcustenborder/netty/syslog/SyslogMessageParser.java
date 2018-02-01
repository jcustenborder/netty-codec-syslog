/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyslogMessageParser extends AbstractMessageParser<SyslogMessage> {
  private static final Logger log = LoggerFactory.getLogger(SyslogMessageParser.class);

  private static final Pattern MESSAGE_PATTERN = Pattern.compile("^<(?<priority>\\d+)>(?<date>([a-zA-Z]{3}\\s+\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})|([0-9T:.Z-]+)) (?<host>\\S+) (\\S+):\\s*(?<message>.+)$");

  public SyslogMessageParser(TimeZone timeZone) {
    super(timeZone);
  }

  public SyslogMessageParser(String timeZoneId) {
    super(timeZoneId);
  }

  Date parseDate(String groupDate) {
    List<Format> dateFormatList = dateFormats.get();
    Date result = null;

    for (Format format : dateFormatList) {
      result = format.parse(groupDate);

      if (null != result) {
        break;
      }
    }

    return result;
  }

  @Override
  protected SyslogMessage parse(final InetAddress remoteAddress, final String message) {
    final Matcher matcher = MESSAGE_PATTERN.matcher(message);

    if (matcher.find()) {
      final ImmutableSyslogMessage.Builder builder = ImmutableSyslogMessage.builder()
          .rawMessage(message)
          .remoteAddress(remoteAddress);
      final String groupPriority = matcher.group("priority");
      final String groupDate = matcher.group("date");
      final String groupHost = matcher.group("host");
      final String groupMessage = matcher.group("message");
      final int priority = Integer.parseInt(groupPriority);
      final int facility = priority >> 3;
      final int level = priority - (facility << 3);
      final Date date = parseDate(groupDate);
      builder.date(date);
      builder.host(groupHost);
      builder.level(level);
      builder.facility(facility);
      builder.message(groupMessage);
      return builder.build();
    } else {
      return null;
    }
  }
}
