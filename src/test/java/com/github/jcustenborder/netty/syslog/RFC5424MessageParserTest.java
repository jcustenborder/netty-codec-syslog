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

import java.io.File;

public class RFC5424MessageParserTest extends MessageParserTest<RFC5424MessageParser> {
  @Override
  protected RFC5424MessageParser createParser() {
    return new RFC5424MessageParser();
  }


  @Override
  protected File testsPath() {
    return new File("src/test/resources/com/github/jcustenborder/netty/syslog/rfc5424");
  }
}
