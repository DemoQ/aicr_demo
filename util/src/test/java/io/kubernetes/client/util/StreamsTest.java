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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class StreamsTest {

  private static byte[] payload(int size) {
    byte[] bytes = new byte[size];
    for (int i = 0; i < size; i++) {
      bytes[i] = (byte) (i % 128);
    }
    return bytes;
  }

  @Test
  void copyLargerThanBuffer() throws IOException {
    byte[] input = payload(Streams.BUFFER_SIZE * 2 + 17);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    Streams.copy(new ByteArrayInputStream(input), out);

    assertThat(out.toByteArray()).isEqualTo(input);
  }

  @Test
  void copyEmptyStream() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    Streams.copy(new ByteArrayInputStream(new byte[0]), out);

    assertThat(out.size()).isZero();
  }

  @Test
  void readerToString() throws IOException {
    StringBuilder input = new StringBuilder();
    while (input.length() < Streams.BUFFER_SIZE + 10) {
      input.append("kubernetes");
    }

    assertThat(Streams.toString(new StringReader(input.toString())))
        .isEqualTo(input.toString());
  }

  @Test
  void readFullyFillsBuffer() throws IOException {
    byte[] input = "kubernetes".getBytes(StandardCharsets.UTF_8);
    byte[] target = new byte[input.length];

    Streams.readFully(new ByteArrayInputStream(input), target);

    assertThat(target).isEqualTo(input);
  }

  @Test
  void readFullyStopsAtEndOfStream() throws IOException {
    byte[] input = "abc".getBytes(StandardCharsets.UTF_8);
    byte[] target = new byte[5];

    Streams.readFully(new ByteArrayInputStream(input), target);

    assertThat(Arrays.copyOf(target, 3)).isEqualTo(input);
    assertThat(target[3]).isZero();
    assertThat(target[4]).isZero();
  }
}
