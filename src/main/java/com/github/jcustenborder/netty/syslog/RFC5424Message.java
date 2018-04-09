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

import java.util.List;
import java.util.Map;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
@JsonSerialize(as = ImmutableRFC5424Message.class)
@JsonDeserialize(as = ImmutableRFC5424Message.class)
public interface RFC5424Message extends Message {
  @Nullable
  String messageId();

  @Nullable
  String procId();

  @Nullable
  String appName();

  List<StructuredData> structuredData();

  @Value.Immutable
  @Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
  @JsonSerialize(as = ImmutableStructuredData.class)
  @JsonDeserialize(as = ImmutableStructuredData.class)
  interface StructuredData {
    String id();

    Map<String, String> structuredDataElements();
  }
}
