/**
 * Copyright © 2018 Jeremy Custenborder (jcustenborder@gmail.com)
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

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CEFMessageParserTest extends MessageParserTest<CEFSyslogMessage, CEFMessageParser, CEFTestCase> {
  @Override
  protected void assertMessage(CEFSyslogMessage expected, CEFSyslogMessage actual) {
    MessageAssertions.assertMessage(expected, actual);
    assertEquals(expected.deviceEventClassId(), actual.deviceEventClassId(), "deviceEventClassId does not match.");
    assertEquals(expected.deviceProduct(), actual.deviceProduct(), "deviceProduct does not match.");
    assertEquals(expected.deviceVendor(), actual.deviceVendor(), "deviceVendor does not match.");
    assertEquals(expected.deviceVersion(), actual.deviceVersion(), "deviceVersion does not match.");
    assertEquals(expected.name(), actual.name(), "name does not match.");
    assertEquals(expected.severity(), actual.severity(), "severity does not match.");
    assertEquals(expected.extension(), actual.extension(), "extension does not match.");
  }

  @Override
  protected CEFMessageParser createParser() {
    return new CEFMessageParser();
  }

  @Override
  protected Class<CEFTestCase> testCaseClass() {
    return CEFTestCase.class;
  }

  @Override
  protected File testsPath() {
    return new File("src/test/resources/com/github/jcustenborder/netty/syslog/cef");
  }
}
