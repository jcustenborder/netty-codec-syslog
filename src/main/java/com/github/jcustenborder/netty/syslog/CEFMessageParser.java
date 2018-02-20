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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;

public class CEFMessageParser extends MessageParser {
  private static final Logger log = LoggerFactory.getLogger(CEFMessageParser.class);
  private static final String PATTERN = "^(<(?<priority>\\d+)>)?(?<date>([a-zA-Z]{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)|([0-9T:.Z-]+))\\s+(?<host>\\S+)\\s+CEF:(?<version>\\d+)\\|(?<deviceVendor>[^|]+)\\|(?<deviceProduct>[^|]+)\\|(?<deviceVersion>[^|]+)\\|(?<deviceEventClassId>[^|]+)\\|(?<name>[^|]+)\\|(?<severity>[^|]+)\\|(?<extension>.*)$";
  private static final String CEF_PREFIX_PATTERN = "^(<(?<priority>\\d+)>)?(?<date>([a-zA-Z]{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)|([0-9T:.Z-]+))\\s+(?<host>\\S+)\\s+CEF:(?<version>\\d+)\\|(?<data>.*)$";
  private static final String CEF_MAIN_PATTERN = "(?<!\\\\)\\|";

  private final ThreadLocal<Matcher> matcherCEFPrefix;
  private final ThreadLocal<Matcher> matcherCEFMain;


  public CEFMessageParser() {
    this("UTC");
  }

  public CEFMessageParser(TimeZone timeZone) {
    super(timeZone);
    this.matcherCEFPrefix = initMatcher(CEF_PREFIX_PATTERN);
    this.matcherCEFMain = initMatcher(CEF_MAIN_PATTERN);
  }

  public CEFMessageParser(String timeZoneId) {
    super(timeZoneId);
    this.matcherCEFPrefix = initMatcher(CEF_PREFIX_PATTERN);
    this.matcherCEFMain = initMatcher(CEF_MAIN_PATTERN);
  }

  List<String> splitToList(String data) {
    List<String> result = new ArrayList<>(10);
    final Matcher matcherData = this.matcherCEFMain.get().reset(data);

    int start = 0;
    int end = 0;
    while (matcherData.find()) {
      end = matcherData.end();
      String part = data.substring(start, end - 1);
      start = end;
      result.add(part);
    }

    if (data.length() > end + 1) {
      result.add(data.substring(end + 1));
    }

    return result;
  }

  @Override
  public boolean parse(List<Object> output, InetAddress remoteAddress, String rawMessage) {
    log.trace("parse() - remoteAddress='{}' rawMessage='{}'", remoteAddress, rawMessage);
    final Matcher matcherPrefix = this.matcherCEFPrefix.get().reset(rawMessage);

    if (!matcherPrefix.find()) {
      log.trace("parse() - Could not match message. rawMessage='{}'", rawMessage);
      return false;
    }

    log.trace("parse() - Parsed message as CEF.");
    final String groupPriority = matcherPrefix.group("priority");
    final String groupDate = matcherPrefix.group("date");
    final String groupHost = matcherPrefix.group("host");
    final String groupCEFVersion = matcherPrefix.group("version");
    final String groupData = matcherPrefix.group("data");

    final Integer priority = (groupPriority == null || groupPriority.isEmpty()) ? null : Integer.parseInt(groupPriority);
    final Integer facility = null == priority ? null : facility(priority);
    final Integer level = null == priority ? null : level(priority, facility);
    final Date date = parseDate(groupDate);
    final Integer cefVersion = Integer.parseInt(groupCEFVersion);

    final List<String> parts = splitToList(groupData);

    ImmutableCEFSyslogMessage.Builder builder = ImmutableCEFSyslogMessage.builder();
    builder.rawMessage(rawMessage);
    builder.remoteAddress(remoteAddress);
    builder.date(date);
    builder.version(cefVersion);
    builder.host(groupHost);
    builder.level(level);
    builder.facility(facility);

    int index = 0;
    for (String token : parts) {
      token = token.replace("\\|", "|");
      log.trace("parse() - index={}, token='{}'", index, token);

      switch (index) {
        case 0:
          builder.deviceVendor(token);
          break;
        case 1:
          builder.deviceProduct(token);
          break;
        case 2:
          builder.deviceVersion(token);
          break;
        case 3:
          builder.deviceEventClassId(token);
          break;
        case 4:
          builder.name(token);
          break;
        case 5:
          builder.severity(token);
          break;
        case 6:

          break;
        default:
          break;
      }

      index++;
    }

//
//    final String groupdeviceVendor = matcherPrefix.group("deviceVendor");
//    final String groupdeviceProduct = matcherPrefix.group("deviceProduct");
//    final String groupdeviceVersion = matcherPrefix.group("deviceVersion");
//    final String groupdeviceEventClassId = matcherPrefix.group("deviceEventClassId");
//    final String groupname = matcherPrefix.group("name");
//    final String groupseverity = matcherPrefix.group("severity");
//    final String groupextension = matcherPrefix.group("extension");


    final Map<String, String> extension = new LinkedHashMap<>();

    output.add(builder.build());

    return true;
  }
}
