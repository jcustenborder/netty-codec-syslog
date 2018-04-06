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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class PriorityTest {

  static List<TestCase> testCases;

  @BeforeAll
  public static void before() {
    testCases = Arrays.asList(
        priority(13, 1, 5),
        priority(46, 5, 6),
        priority(30, 3, 6),
        priority(86, 10, 6),
        priority(0, 0, 0)
    );
  }

  @TestFactory
  public Stream<DynamicTest> level() {
    return testCases.stream()
        .map(t -> dynamicTest(t.toString(), () -> {
          final int level = Priority.level(t.priority, t.facility);
          assertEquals(t.level, level);
        }));
  }

  @TestFactory
  public Stream<DynamicTest> facility() {
    return testCases.stream()
        .map(t -> dynamicTest(t.toString(), () -> {
          final int facility = Priority.facility(t.priority);
          assertEquals(t.facility, facility);
        }));
  }

  @TestFactory
  public Stream<DynamicTest> priority() {
    return testCases.stream()
        .map(t -> dynamicTest(t.toString(), () -> {
          final int actual = Priority.priority(t.level, t.facility);
          assertEquals(t.priority, actual);
        }));
  }

  static TestCase priority(int expected, int facility, int level) {
    return new TestCase(expected, facility, level);
  }

  static class TestCase {
    public final int priority;
    public final int facility;
    public final int level;


    TestCase(int priority, int facility, int level) {
      this.priority = priority;
      this.facility = facility;
      this.level = level;
    }


    @Override
    public String toString() {
      return String.format("priority=%s facility=%s level=%s", priority, facility, level);
    }
  }
}
