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

import org.immutables.value.Value;

import java.net.InetAddress;
import java.util.Date;

/**
 * Interface that is yielded when none of the configured parsers can parse the current message.
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
public interface UnparseableMessage {
  /**
   * Method is used to return the date that the parser was executed. This is not the date in the
   * message
   * @return Date the message was received.
   */
  Date date();

  /**
   * Method is used to return the remote address that sent the message.
   * @return ip address of the host that sent the message.
   */
  InetAddress remoteAddress();

  /**
   * Method is used to return the raw unprocessed message.
   * @return Raw unprocessed message.
   */
  String rawMessage();
}
