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

import io.kubernetes.client.openapi.models.V1ListMeta;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.exception.ObjectMetaReflectException;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ListAccessorTest {

  @Test
  void accessListMetadataAndItems() throws ObjectMetaReflectException {
    V1ListMeta listMeta = new V1ListMeta().resourceVersion("42");
    V1Pod pod = new V1Pod().metadata(new V1ObjectMeta().name("foo"));
    V1PodList podList = new V1PodList().metadata(listMeta).items(Collections.singletonList(pod));

    assertThat(ListAccessor.listMetadata(podList)).isSameAs(listMeta);
    assertThat(ListAccessor.<V1Pod>getItems(podList)).containsExactly(pod);
  }

  @Test
  void objectWithoutListMethods() {
    assertThatThrownBy(() -> ListAccessor.listMetadata("not-a-list"))
        .isInstanceOf(ObjectMetaReflectException.class);
    assertThatThrownBy(() -> ListAccessor.getItems("not-a-list"))
        .isInstanceOf(ObjectMetaReflectException.class);
  }
}
