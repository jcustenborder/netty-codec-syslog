package com.github.jcustenborder.netty.syslog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;

import java.net.InetAddress;
import java.util.List;

public abstract class MessageParserTest<M extends Message, T extends MessageParser> {
  protected abstract void assertMessage(M expected, M actual);

  protected abstract T createParser();

  protected ObjectMapper mapper;
  protected T parser;

  protected boolean parse(List<Object> output, String message) {
    return this.parser.parse(output, InetAddress.getLoopbackAddress(), message);
  }


  @BeforeEach
  public void setup() {
    this.mapper = new ObjectMapper();
    this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    this.parser = createParser();
  }

}
