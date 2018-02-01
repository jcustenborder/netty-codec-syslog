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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public abstract class AbstractMessageParser<T extends Message> {
  static final Charset CHARSET = Charset.forName("UTF-8");

  protected final ThreadLocal<List<Format>> dateFormats;

  public AbstractMessageParser(TimeZone timeZone) {
    this.dateFormats = new InheritableThreadLocal<List<Format>>() {
      @Override
      protected List<Format> initialValue() {
        List<Format> formats = Arrays.asList(
            new Format("MMM dd hh:mm:ss", timeZone)
        );
        return formats;
      }
    };
  }

  public AbstractMessageParser(String timeZoneId) {
    this(TimeZone.getTimeZone(timeZoneId));
  }


  protected abstract T parse(InetAddress remoteAddress, String message);

  public T parse(ChannelHandlerContext context, ByteBuf buf) {
    final SocketAddress socketAddress = context.channel().remoteAddress();
    final String message = buf.toString(CHARSET);
    InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
    return parse(inetSocketAddress.getAddress(), message);
  }

  static class Format {
    public final boolean hasYear;
    public final SimpleDateFormat format;
    public final TimeZone timeZone;

    Format(String format, TimeZone timeZone) {
      this.timeZone = timeZone;
      this.format = new SimpleDateFormat(format);
      this.format.setTimeZone(this.timeZone);
      this.hasYear = this.format.toPattern().contains("y");
    }

    public Date parse(String s) {
      Date date;

      try {
        date = this.format.parse(s);

        if (!this.hasYear) {
          final Calendar calendar = Calendar.getInstance(this.timeZone);
          final int year = calendar.get(Calendar.YEAR);
          calendar.setTime(date);
          calendar.set(Calendar.YEAR, year);
          date = calendar.getTime();
        }
      } catch (ParseException e) {

        date = null;
      }

      return date;
    }
  }
}
