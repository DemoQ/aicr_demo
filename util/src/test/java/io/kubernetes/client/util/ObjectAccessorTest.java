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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.exception.ObjectMetaReflectException;
import org.junit.jupiter.api.Test;

class ObjectAccessorTest {

  @Test
  void accessMetadata() throws ObjectMetaReflectException {
    V1ObjectMeta metadata = new V1ObjectMeta().name("foo").namespace("default");
    V1Pod pod = new V1Pod().metadata(metadata);

    assertThat(ObjectAccessor.objectMetadata(pod)).isSameAs(metadata);
    assertThat(ObjectAccessor.name(pod)).isEqualTo("foo");
    assertThat(ObjectAccessor.namespace(pod)).isEqualTo("default");
  }

  @Test
  void objectWithoutMetadataMethod() {
    assertThatThrownBy(() -> ObjectAccessor.objectMetadata("not-a-kubernetes-object"))
        .isInstanceOf(ObjectMetaReflectException.class);
  }
}
