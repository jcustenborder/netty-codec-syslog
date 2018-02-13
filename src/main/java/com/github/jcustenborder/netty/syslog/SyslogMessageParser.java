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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SyslogMessageParser extends AbstractMessageParser {
  private static final Logger log = LoggerFactory.getLogger(SyslogMessageParser.class);


  public SyslogMessageParser(TimeZone timeZone) {
    super(timeZone);
  }

  public SyslogMessageParser(String timeZoneId) {
    super(timeZoneId);
  }

  @Override
  protected void parse(
      List<Object> output,
      String rawMessage, Date date,
      InetAddress remoteAddress,
      String host,
      Integer facility,
      Integer level,
      Integer version,
      String appName,
      String procID,
      String messageID,
      List<StructuredData> structuredData,
      String message) {
    output.add(
        ImmutableSyslogMessage.builder()
            .rawMessage(message)
            .remoteAddress(remoteAddress)
            .version(version)
            .date(date)
            .host(host)
            .level(level)
            .facility(facility)
            .message(message)
            .build()
    );
  }
}
