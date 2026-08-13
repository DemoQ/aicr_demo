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

class KubernetesResourceTest {

  @Test
  void parseClusterScopedCoreResource() throws ParseKubernetesResourceException {
    KubernetesResource resource = KubernetesResource.parseCoreResource("/api/v1/nodes");
    assertThat(resource.getGroupVersionResource().getGroup()).isEmpty();
    assertThat(resource.getGroupVersionResource().getVersion()).isEqualTo("v1");
    assertThat(resource.getGroupVersionResource().getResource()).isEqualTo("nodes");
    assertThat(resource.getNamespace()).isNull();
    assertThat(resource.getName()).isNull();
    assertThat(resource.getSubResource()).isNull();
  }

  @Test
  void parseNamespacedCoreResourceWithSubResource() throws ParseKubernetesResourceException {
    KubernetesResource resource =
        KubernetesResource.parseCoreResource("/api/v1/namespaces/default/pods/foo/status");
    assertThat(resource.getGroupVersionResource().getResource()).isEqualTo("pods");
    assertThat(resource.getNamespace()).isEqualTo("default");
    assertThat(resource.getName()).isEqualTo("foo");
    assertThat(resource.getSubResource()).isEqualTo("status");
  }

  @Test
  void parseInvalidCoreResource() {
    assertThatThrownBy(() -> KubernetesResource.parseCoreResource("/healthz"))
        .isInstanceOf(ParseKubernetesResourceException.class);
  }

  @Test
  void parseRegularResource() throws ParseKubernetesResourceException {
    KubernetesResource resource =
        KubernetesResource.parseRegularResource(
            "/apis/apps/v1/namespaces/default/deployments/foo/scale");
    assertThat(resource.getGroupVersionResource().getGroup()).isEqualTo("apps");
    assertThat(resource.getGroupVersionResource().getVersion()).isEqualTo("v1");
    assertThat(resource.getGroupVersionResource().getResource()).isEqualTo("deployments");
    assertThat(resource.getNamespace()).isEqualTo("default");
    assertThat(resource.getName()).isEqualTo("foo");
    assertThat(resource.getSubResource()).isEqualTo("scale");
  }

  @Test
  void parseClusterScopedRegularResource() throws ParseKubernetesResourceException {
    KubernetesResource resource =
        KubernetesResource.parseRegularResource(
            "/apis/rbac.authorization.k8s.io/v1/clusterroles/foo");
    assertThat(resource.getGroupVersionResource().getGroup())
        .isEqualTo("rbac.authorization.k8s.io");
    assertThat(resource.getGroupVersionResource().getResource()).isEqualTo("clusterroles");
    assertThat(resource.getNamespace()).isNull();
    assertThat(resource.getName()).isEqualTo("foo");
  }

  @Test
  void parseInvalidRegularResource() {
    assertThatThrownBy(() -> KubernetesResource.parseRegularResource("/api/v1/pods"))
        .isInstanceOf(ParseKubernetesResourceException.class);
  }
}
