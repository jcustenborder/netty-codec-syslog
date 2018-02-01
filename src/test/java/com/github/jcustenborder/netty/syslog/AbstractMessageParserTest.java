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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;

import java.net.InetAddress;

public abstract class AbstractMessageParserTest<M extends Message, T extends AbstractMessageParser<M>> {
  protected abstract void assertMessage(M expected, M actual);
  protected abstract T createParser();

  protected ObjectMapper mapper;
  protected T parser;


  protected M parse(String message) {
    return this.parser.parse(InetAddress.getLoopbackAddress(), message);
  }


  @BeforeEach
  public void setup() {
    this.mapper = new ObjectMapper();
    this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    this.parser = createParser();
  }

}
