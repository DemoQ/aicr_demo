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
package io.kubernetes.client.util.wait;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class WaitTest {

  @Test
  @Timeout(30)
  void conditionEventuallySatisfied() {
    AtomicInteger invocations = new AtomicInteger();

    boolean result =
        Wait.poll(
            Duration.ofMillis(10),
            Duration.ofSeconds(10),
            () -> invocations.incrementAndGet() >= 3);

    assertThat(result).isTrue();
    assertThat(invocations.get()).isGreaterThanOrEqualTo(3);
  }

  @Test
  @Timeout(30)
  void conditionNeverSatisfiedTimesOut() {
    boolean result = Wait.poll(Duration.ofMillis(10), Duration.ofMillis(200), () -> false);

    assertThat(result).isFalse();
  }

  @Test
  @Timeout(30)
  void throwingConditionIsTreatedAsUnsatisfied() {
    boolean result =
        Wait.poll(
            Duration.ofMillis(10),
            Duration.ofMillis(200),
            () -> {
              throw new RuntimeException("boom");
            });

    assertThat(result).isFalse();
  }

  @Test
  @Timeout(30)
  void initialDelayLongerThanTimeoutNeverRunsCondition() {
    AtomicInteger invocations = new AtomicInteger();

    boolean result =
        Wait.poll(
            Duration.ofSeconds(10),
            Duration.ofMillis(10),
            Duration.ofMillis(200),
            () -> {
              invocations.incrementAndGet();
              return true;
            });

    assertThat(result).isFalse();
    assertThat(invocations.get()).isZero();
  }
}
