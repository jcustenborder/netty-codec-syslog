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
import java.util.regex.Matcher;

public class RFC3164MessageParser extends MessageParser {
  private static final Logger log = LoggerFactory.getLogger(RFC3164MessageParser.class);
  private static final String PATTERN = "^(<(?<priority>\\d+)>)?(?<date>([a-zA-Z]{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)|([0-9T:.Z-]+))\\s+(?<host>\\S+)\\s+((?<tag>[^\\[\\s\\]]+)(\\[(?<procid>\\d+)\\])?:)*\\s*(?<message>.+)$";
  private final ThreadLocal<Matcher> matcherThreadLocal;

  public RFC3164MessageParser() {
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

    log.trace("parse() - Parsed message as RFC 3164");
    final String groupPriority = matcher.group("priority");
    final String groupDate = matcher.group("date");
    final String groupHost = matcher.group("host");
    final String groupMessage = matcher.group("message");
    final String groupTag = matcher.group("tag");
    final String groupProcId = matcher.group("procid");
    final String processId = (groupProcId == null || groupProcId.isEmpty()) ? null : groupProcId;
    final Integer priority = (groupPriority == null || groupPriority.isEmpty()) ? null : Integer.parseInt(groupPriority);
    final Integer facility = null == priority ? null : Priority.facility(priority);
    final Integer level = null == priority ? null : Priority.level(priority, facility);
    final LocalDateTime date = parseDate(groupDate);


    return ImmutableSyslogMessage.builder()
        .type(MessageType.RFC3164)
        .rawMessage(request.rawMessage())
        .remoteAddress(request.remoteAddress())
        .date(date)
        .host(groupHost)
        .level(level)
        .facility(facility)
        .message(groupMessage)
        .tag(groupTag)
        .processId(processId)
        .build();

  }
}
