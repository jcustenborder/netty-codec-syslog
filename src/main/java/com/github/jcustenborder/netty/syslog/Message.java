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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.avro.reflect.Nullable;
import org.immutables.value.Value;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a standard syslog message.
 */
public interface Message {
  /**
   * Date of the message. This is the parsed date from the client.
   *
   * @return Date of the message.
   */
  @JsonProperty(required = true)
  LocalDateTime date();

  /**
   * IP Address for the sender of the message.
   *
   * @return Sender IP Address.
   */
  @JsonProperty(required = true)
  InetAddress remoteAddress();

  /**
   * Unprocessed copy of the message.
   *
   * @return Unprocessed message.
   */
  @JsonProperty(required = true)
  String rawMessage();

  /**
   * @return
   */
  @JsonProperty(required = true)
  MessageType type();


  /**
   * Level for the message. Parsed from the message.
   *
   * @return Message Level
   */
  @Nullable
  Integer level();

  /**
   * Version of the message.
   *
   * @return Message version
   */
  @Nullable
  Integer version();

  /**
   * Facility of the message.
   *
   * @return Message facility.
   */
  @Nullable
  Integer facility();

  /**
   * Host of the message. This is the value from the message.
   *
   * @return Message host.
   */
  @Nullable
  String host();

  /**
   * Message part of the overall syslog message.
   *
   * @return Message part of the overall syslog message.
   */
  @Nullable
  String message();

  @Nullable
  String processId();

  /*
  rfc 3164
   */
  @Nullable
  String tag();

  /*
  rfc 3164
   */

  /*
rfc 5424
 */
  @Nullable
  String messageId();

  @Nullable
  String appName();

  @Nullable
  List<StructuredData> structuredData();

  /*
  CEF
   */
  @Nullable
  String deviceVendor();

      /*
  rfc 5424
   */

  @Nullable
  String deviceProduct();

  @Nullable
  String deviceVersion();

  @Nullable
  String deviceEventClassId();

  @Nullable
  String name();

  @Nullable
  String severity();

  @Nullable
  Map<String, String> extension();

  @Value.Immutable
  @Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
  @JsonSerialize(as = ImmutableStructuredData.class)
  @JsonDeserialize(as = ImmutableStructuredData.class)
  interface StructuredData {
    String id();

    Map<String, String> structuredDataElements();
  }
  /*
  CEF
   */


}
