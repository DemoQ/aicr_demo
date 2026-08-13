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

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;

class KubernetesRequestDigestTest {

  private static Request request(String method, String url) {
    Request.Builder builder = new Request.Builder().url(url);
    if ("GET".equals(method) || "DELETE".equals(method)) {
      return builder.method(method, null).build();
    }
    return builder
        .method(method, RequestBody.create("{}", MediaType.parse("application/json")))
        .build();
  }

  @Test
  void isResourceRequest() {
    assertThat(KubernetesRequestDigest.isResourceRequest("/api/v1/pods")).isTrue();
    assertThat(KubernetesRequestDigest.isResourceRequest("/apis/apps/v1/deployments")).isTrue();
    assertThat(KubernetesRequestDigest.isResourceRequest("/healthz")).isFalse();
    assertThat(KubernetesRequestDigest.isResourceRequest("/version")).isFalse();
  }

  @Test
  void parseNonResourceRequest() {
    KubernetesRequestDigest digest =
        KubernetesRequestDigest.parse(request("GET", "https://localhost/healthz"));
    assertThat(digest.isNonResourceRequest()).isTrue();
    assertThat(digest.getUrlPath()).isEqualTo("/healthz");
    assertThat(digest.getResourceMeta()).isNull();
    assertThat(digest.getVerb()).isNull();
    assertThat(digest.toString()).isEqualTo("null /healthz");
  }

  @Test
  void parseCoreListRequest() {
    KubernetesRequestDigest digest =
        KubernetesRequestDigest.parse(
            request("GET", "https://localhost/api/v1/namespaces/default/pods"));
    assertThat(digest.isNonResourceRequest()).isFalse();
    assertThat(digest.getVerb()).isEqualTo(KubernetesVerb.LIST);
    assertThat(digest.getResourceMeta().getNamespace()).isEqualTo("default");
    assertThat(digest.toString()).isEqualTo("list  pods");
  }

  @Test
  void parseCoreWatchRequest() {
    KubernetesRequestDigest digest =
        KubernetesRequestDigest.parse(
            request("GET", "https://localhost/api/v1/namespaces/default/pods?watch=true"));
    assertThat(digest.getVerb()).isEqualTo(KubernetesVerb.WATCH);
  }

  @Test
  void parseRegularSubResourceRequest() {
    KubernetesRequestDigest digest =
        KubernetesRequestDigest.parse(
            request(
                "PUT", "https://localhost/apis/apps/v1/namespaces/default/deployments/foo/status"));
    assertThat(digest.getVerb()).isEqualTo(KubernetesVerb.UPDATE);
    assertThat(digest.getResourceMeta().getName()).isEqualTo("foo");
    assertThat(digest.toString()).isEqualTo("update apps/v1 deployments/status");
  }

  @Test
  void parseUnparsableResourceRequestFallsBackToNonResource() {
    KubernetesRequestDigest digest =
        KubernetesRequestDigest.parse(request("GET", "https://localhost/apis/apps/v1/pods%20x"));
    assertThat(digest.isNonResourceRequest()).isTrue();
  }
}
