package com.github.jcustenborder.netty.syslog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessageAssertions {
  public static void assertMessage(Message expected, Message actual) {
    if (null != expected) {
      assertNotNull(actual, "actual should not be null.");
    } else {
      assertNull(actual, "actual should be null.");
      return;
    }

    assertEquals(expected.facility(), actual.facility(), "facility should match.");
    assertEquals(expected.level(), actual.level(), "level should match.");
    assertEquals(expected.remoteAddress(), actual.remoteAddress(), "remoteAddress should match.");
    assertEquals(expected.date(), actual.date(), "date should match.");
  }

}
