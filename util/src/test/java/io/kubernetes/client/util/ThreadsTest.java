/*
Copyright 2025 The Kubernetes Authors.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package io.kubernetes.client.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ThreadsTest {

  @AfterEach
  void resetDefaultThreadFactory() {
    Threads.setDefaultThreadFactory(Executors.defaultThreadFactory());
  }

  @Test
  void threadNamesAreNumbered() {
    ThreadFactory factory = Threads.threadFactory("test-thread-%d");
    Runnable noop = () -> {};

    assertThat(factory.newThread(noop).getName()).isEqualTo("test-thread-1");
    assertThat(factory.newThread(noop).getName()).isEqualTo("test-thread-2");
  }

  @Test
  void threadCountersAreIndependentPerFactory() {
    Runnable noop = () -> {};
    Threads.threadFactory("first-%d").newThread(noop);

    assertThat(Threads.threadFactory("second-%d").newThread(noop).getName())
        .isEqualTo("second-1");
  }

  @Test
  void delegatesToInjectedDefaultThreadFactory() {
    AtomicBoolean delegated = new AtomicBoolean(false);
    Threads.setDefaultThreadFactory(
        r -> {
          delegated.set(true);
          Thread thread = new Thread(r);
          thread.setDaemon(true);
          return thread;
        });

    Thread thread = Threads.threadFactory("injected-%d").newThread(() -> {});

    assertThat(delegated).isTrue();
    assertThat(thread.isDaemon()).isTrue();
    assertThat(thread.getName()).isEqualTo("injected-1");
  }
}
