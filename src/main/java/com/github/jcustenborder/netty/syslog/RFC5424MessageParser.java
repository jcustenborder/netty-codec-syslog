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

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;

public class RFC5424MessageParser extends MessageParser {
  private static final Logger log = LoggerFactory.getLogger(RFC5424MessageParser.class);
  private static final String PATTERN = "^<(?<priority>\\d+)>(?<version>\\d{1,3})\\s*(?<date>[0-9:+-TZ]+)\\s*(?<host>\\S+)\\s*(?<appname>\\S+)\\s*(?<procid>\\S+)\\s*(?<msgid>\\S+)\\s*(?<structureddata>(-|\\[.+\\]))\\s*(?<message>.+)$";
  private final ThreadLocal<Matcher> matcherThreadLocal;

  public RFC5424MessageParser() {
    this.matcherThreadLocal = initMatcher(PATTERN);
  }

  @Override
  public Message parse(SyslogRequest request) {
    log.trace("parse() - request = '{}'", request);
    final Matcher matcher = matcherThreadLocal.get().reset(request.rawMessage());

    if (!matcher.find()) {
      log.trace("parse() - Could not match message. request = '{}'", request);
      return null;
    }

    log.trace("parse() - Successfully matched message");
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
    final int facility = Priority.facility(priority);
    final LocalDateTime date = parseDate(groupDate);
    final int level = Priority.level(priority, facility);
    final Integer version = Integer.parseInt(groupVersion);
    final String appName = nullableString(groupAppName);
    final String procID = nullableString(groupProcID);
    final String messageID = nullableString(groupMessageID);

    final List<Message.StructuredData> structuredData = parseStructuredData(groupStructuredData);

    return ImmutableSyslogMessage.builder()
        .type(MessageType.RFC5424)
        .rawMessage(request.rawMessage())
        .remoteAddress(request.remoteAddress())
        .date(date)
        .host(groupHost)
        .level(level)
        .facility(facility)
        .message(groupMessage)
        .version(version)
        .processId(procID)
        .messageId(messageID)
        .structuredData(structuredData)
        .appName(appName)
        .build();
  }
}
