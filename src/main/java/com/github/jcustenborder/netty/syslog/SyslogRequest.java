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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.net.InetAddress;
import java.util.Date;

/**
 * Interface represents an incoming syslog request. This interface acts as an intermediary between
 * the TCPSyslogMessageDecoder and UDPSyslogMessageDecoder
 * @see TCPSyslogMessageDecoder
 * @see UDPSyslogMessageDecoder
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonSerialize(as = ImmutableSyslogRequest.class)
@JsonDeserialize(as = ImmutableSyslogRequest.class)
public interface SyslogRequest {
  /**
   * The time the message was received by Netty.
   * @return The time the message was received by Netty.
   */
  Date receivedDate();

  /**
   * IP Address for the sender of the message.
   * @return Sender IP Address
   */
  InetAddress remoteAddress();

  /**
   * The raw message that was delivered
   * @return Raw message.
   */
  String rawMessage();
}
