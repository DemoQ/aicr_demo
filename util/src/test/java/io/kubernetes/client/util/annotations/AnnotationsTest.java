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
package io.kubernetes.client.util.annotations;

import static org.assertj.core.api.Assertions.assertThat;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AnnotationsTest {

  @Test
  void addSingleAnnotation() {
    V1Pod pod = new V1Pod().metadata(new V1ObjectMeta());

    Annotations.addAnnotations(pod, "foo", "bar");

    assertThat(pod.getMetadata().getAnnotations()).containsEntry("foo", "bar");
  }

  @Test
  void addMultipleAnnotations() {
    V1Pod pod = new V1Pod().metadata(new V1ObjectMeta());
    Map<String, String> annotations = new HashMap<>();
    annotations.put("foo1", "bar1");
    annotations.put("foo2", "bar2");

    Annotations.addAnnotations(pod, annotations);

    assertThat(pod.getMetadata().getAnnotations())
        .containsEntry("foo1", "bar1")
        .containsEntry("foo2", "bar2");
  }

  @Test
  void mergeIntoExistingAnnotations() {
    V1Pod pod =
        new V1Pod()
            .metadata(
                new V1ObjectMeta()
                    .annotations(new HashMap<>(Collections.singletonMap("existing", "value"))));

    Annotations.addAnnotations(pod, "existing", "overridden");
    Annotations.addAnnotations(pod, "added", "value");

    assertThat(pod.getMetadata().getAnnotations())
        .containsEntry("existing", "overridden")
        .containsEntry("added", "value");
  }

  @Test
  void nullAnnotationsIsNoop() {
    V1Pod pod =
        new V1Pod()
            .metadata(
                new V1ObjectMeta()
                    .annotations(new HashMap<>(Collections.singletonMap("existing", "value"))));

    Annotations.addAnnotations(pod, null);

    assertThat(pod.getMetadata().getAnnotations()).containsExactlyEntriesOf(
        Collections.singletonMap("existing", "value"));
  }
}
