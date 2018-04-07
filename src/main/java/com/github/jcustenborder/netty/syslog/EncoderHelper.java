package com.github.jcustenborder.netty.syslog;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

class EncoderHelper {
  public static final Charset CHARSET = Charset.forName("UTF-8");
  public static final byte[] LESS_THAN = "<".getBytes(CHARSET);
  public static final byte[] GREATER_THAN = ">".getBytes(CHARSET);
  public static final byte[] LEFT_SQUARE = "[".getBytes(CHARSET);
  public static final byte[] RIGHT_SQUARE = "]".getBytes(CHARSET);
  public static final byte[] SPACE = " ".getBytes(CHARSET);
  public static final byte[] EQUALS = "=".getBytes(CHARSET);


  public static void appendPriority(ByteBuf buffer, Message message) {
    if (null != message.facility() && null != message.level()) {
      Integer priority = Priority.priority(message.level(), message.facility());
      buffer.writeBytes(LESS_THAN);
      buffer.writeCharSequence(priority.toString(), CHARSET);
      buffer.writeBytes(GREATER_THAN);
    }
  }
}
