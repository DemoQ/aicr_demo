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
package io.kubernetes.client.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.prometheus.client.CollectorRegistry;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

class PrometheusInterceptorTest {

  private static Response intercept(String url, int code) throws IOException {
    Request request = new Request.Builder().url(url).get().build();
    Response response =
        new Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("message")
            .body(ResponseBody.create("", MediaType.parse("text/plain")))
            .build();
    Interceptor.Chain chain = mock(Interceptor.Chain.class);
    when(chain.request()).thenReturn(request);
    when(chain.proceed(any(Request.class))).thenReturn(response);
    return new PrometheusInterceptor().intercept(chain);
  }

  private static Double resourceLatencyCount(String[] labelValues) {
    return CollectorRegistry.defaultRegistry.getSampleValue(
        "k8s_java_resource_request_latency_seconds_count",
        new String[] {
          "http_response_code", "group", "version", "resource", "subresource", "api_verb",
          "namespace"
        },
        labelValues);
  }

  private static Double nonResourceLatencyCount(String[] labelValues) {
    return CollectorRegistry.defaultRegistry.getSampleValue(
        "k8s_java_non_resource_request_latency_seconds_count",
        new String[] {"http_response_code", "non_resource_url_path"},
        labelValues);
  }

  @Test
  void recordsResourceRequestLatency() throws IOException {
    String[] labelValues = {"200", "apps", "v1", "deployments", "status", "get", "default"};
    double before = orZero(resourceLatencyCount(labelValues));

    Response response =
        intercept("https://localhost/apis/apps/v1/namespaces/default/deployments/foo/status", 200);

    assertThat(response.code()).isEqualTo(200);
    assertThat(resourceLatencyCount(labelValues)).isEqualTo(before + 1);
  }

  @Test
  void recordsNonResourceRequestLatency() throws IOException {
    String[] labelValues = {"500", "/healthz"};
    double before = orZero(nonResourceLatencyCount(labelValues));

    intercept("https://localhost/healthz", 500);

    assertThat(nonResourceLatencyCount(labelValues)).isEqualTo(before + 1);
  }

  @Test
  void recordsTotalRequestsAndResponseCodes() throws IOException {
    double requestsBefore =
        orZero(
            CollectorRegistry.defaultRegistry.getSampleValue("k8s_java_requests_total"));
    double codesBefore =
        orZero(
            CollectorRegistry.defaultRegistry.getSampleValue(
                "k8s_java_response_code_total", new String[] {"code"}, new String[] {"404"}));

    intercept("https://localhost/api/v1/namespaces/default/pods", 404);

    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("k8s_java_requests_total"))
        .isEqualTo(requestsBefore + 1);
    assertThat(
            CollectorRegistry.defaultRegistry.getSampleValue(
                "k8s_java_response_code_total", new String[] {"code"}, new String[] {"404"}))
        .isEqualTo(codesBefore + 1);
  }

  private static double orZero(Double value) {
    return value == null ? 0d : value;
  }
}
