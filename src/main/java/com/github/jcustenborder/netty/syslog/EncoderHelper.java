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
