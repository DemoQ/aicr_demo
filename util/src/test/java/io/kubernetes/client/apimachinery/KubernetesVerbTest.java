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
package io.kubernetes.client.apimachinery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class KubernetesVerbTest {

  @Test
  void watchParameterTakesPrecedence() {
    assertThat(KubernetesVerb.of("GET", true, true)).isEqualTo(KubernetesVerb.WATCH);
    assertThat(KubernetesVerb.of("DELETE", false, true)).isEqualTo(KubernetesVerb.WATCH);
  }

  @Test
  void getAndList() {
    assertThat(KubernetesVerb.of("GET", true, false)).isEqualTo(KubernetesVerb.GET);
    assertThat(KubernetesVerb.of("GET", false, false)).isEqualTo(KubernetesVerb.LIST);
  }

  @Test
  void deleteAndDeleteCollection() {
    assertThat(KubernetesVerb.of("DELETE", true, false)).isEqualTo(KubernetesVerb.DELETE);
    assertThat(KubernetesVerb.of("DELETE", false, false))
        .isEqualTo(KubernetesVerb.DELETE_COLLECTION);
  }

  @Test
  void writeVerbs() {
    assertThat(KubernetesVerb.of("POST", false, false)).isEqualTo(KubernetesVerb.CREATE);
    assertThat(KubernetesVerb.of("PUT", true, false)).isEqualTo(KubernetesVerb.UPDATE);
    assertThat(KubernetesVerb.of("PATCH", true, false)).isEqualTo(KubernetesVerb.PATCH);
  }

  @Test
  void unknownHttpVerb() {
    assertThatThrownBy(() -> KubernetesVerb.of("HEAD", true, false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid HTTP verb for kubernetes client");
  }

  @Test
  void value() {
    assertThat(KubernetesVerb.DELETE_COLLECTION.value()).isEqualTo("deleteCollection");
    assertThat(KubernetesVerb.LIST.value()).isEqualTo("list");
  }
}
