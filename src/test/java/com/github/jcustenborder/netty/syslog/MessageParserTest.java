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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class MessageParserTest<M extends Message, T extends MessageParser> {
  private static final Logger log = LoggerFactory.getLogger(MessageParserTest.class);
  protected abstract void assertMessage(M expected, M actual);

  protected abstract T createParser();

  protected T parser;

  protected boolean parse(List<Object> output, String message) {
    log.trace("parse() - message = '{}'", message);
    SyslogRequest request = mock(SyslogRequest.class);
    when(request.rawMessage()).thenReturn(message);
    when(request.remoteAddress()).thenReturn(InetAddress.getLoopbackAddress());
    boolean result = this.parser.parse(request, output);

    if(result && !output.isEmpty()) {
      log.trace("parse() - output = '{}'", output.get(0));
    }

    return result;
  }


  @BeforeEach
  public void setup() {
    this.parser = createParser();
  }

}
