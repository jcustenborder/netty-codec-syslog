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

public class RFC3164MessageParser extends MessageParser {
  private static final Logger log = LoggerFactory.getLogger(RFC3164MessageParser.class);
  private static final String PATTERN = "^(<(?<priority>\\d+)>)?(?<date>([a-zA-Z]{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)|([0-9T:.Z-]+))\\s+(?<host>\\S+)\\s+((?<tag>[^\\[\\s\\]]+)(\\[(?<procid>\\d+)\\])?:)*\\s*(?<message>.+)$";
  private final ThreadLocal<Matcher> matcherThreadLocal;

  public RFC3164MessageParser() {
    this("UTC");
  }

  public RFC3164MessageParser(TimeZone timeZone) {
    super(timeZone);
    this.matcherThreadLocal = initMatcher(PATTERN);
  }

  public RFC3164MessageParser(String timeZoneId) {
    super(timeZoneId);
    this.matcherThreadLocal = initMatcher(PATTERN);
  }

  @Override
  public boolean parse(List<Object> output, InetAddress remoteAddress, String rawMessage) {
    log.trace("parse() - remoteAddress='{}' rawMessage='{}'", remoteAddress, rawMessage);
    final Matcher matcher = matcherThreadLocal.get().reset(rawMessage);

    if (!matcher.find()) {
      log.trace("parse() - Could not match message. rawMessage='{}'", rawMessage);
      return false;
    }

    log.trace("parse() - Parsed message as RFC 3164");
    final String groupPriority = matcher.group("priority");
    final String groupDate = matcher.group("date");
    final String groupHost = matcher.group("host");
    final String groupMessage = matcher.group("message");
    final String groupTag = matcher.group("tag");
    final String groupProcId = matcher.group("procid");
    final Integer processId = (groupProcId == null || groupProcId.isEmpty()) ? null : Integer.parseInt(groupProcId);
    final Integer priority = (groupPriority == null || groupPriority.isEmpty()) ? null : Integer.parseInt(groupPriority);
    final Integer facility = null == priority ? null : facility(priority);
    final Integer level = null == priority ? null : level(priority, facility);
    final Date date = parseDate(groupDate);


    output.add(
        ImmutableSyslogMessage.builder()
            .rawMessage(rawMessage)
            .remoteAddress(remoteAddress)
            .date(date)
            .host(groupHost)
            .level(level)
            .facility(facility)
            .message(groupMessage)
            .tag(groupTag)
            .processId(processId)
            .build()
    );

    return true;
  }
}
