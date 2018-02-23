[![Maven Central](https://img.shields.io/maven-central/v/com.github.jcustenborder.netty/netty-codec-syslog.svg)]()

# Introduction

This project provides a [Netty](http://netty.io) based solution for receiving syslog messages. The 
following formats are currently supported. The mechanism for parsing log messages is plugable. You 
can add support for additional formats by implementing a 
[MessageParser](src/main/java/com/github/jcustenborder/netty/syslog/MessageParser.java) for the 
format you wish to support.

* [RFC 3164 - The BSD Syslog Protocol](https://tools.ietf.org/html/rfc3164)
* [RFC 5424 - The Syslog Protocol](https://tools.ietf.org/html/rfc5424)
* [CEF - ArcSight Common Event Format](https://community.softwaregrp.com/t5/ArcSight-Connectors/ArcSight-Common-Event-Format-CEF-Guide/ta-p/1589306)

## RFC 3164 - The BSD Syslog Protocol

```java

```

## RFC 5424 - The Syslog Protocol

```java

```

## CEF - ArcSight Common Event Format

```java

```

