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
package io.kubernetes.client.extended.leaderelection.resourcelock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.kubernetes.client.extended.leaderelection.LeaderElectionRecord;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Endpoints;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.ClientBuilder;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class EndpointsLockTest {

  private static final String NAMESPACE = "default";
  private static final String NAME = "leader-election";
  private static final String IDENTITY = "foo";
  private static final String ANNOTATION_KEY = "control-plane.alpha.kubernetes.io/leader";
  private static final String ENDPOINTS_PATH =
      "/api/v1/namespaces/" + NAMESPACE + "/endpoints";

  private ApiClient apiClient;
  private EndpointsLock lock;

  @RegisterExtension
  static WireMockExtension apiServer =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @BeforeEach
  void setup() {
    apiClient = new ClientBuilder().setBasePath("http://localhost:" + apiServer.getPort()).build();
    lock = new EndpointsLock(NAMESPACE, NAME, IDENTITY, apiClient);
  }

  private V1Endpoints endpoints(LeaderElectionRecord record) {
    V1ObjectMeta metadata = new V1ObjectMeta().namespace(NAMESPACE).name(NAME);
    if (record != null) {
      metadata.annotations(
          Collections.singletonMap(ANNOTATION_KEY, apiClient.getJSON().serialize(record)));
    }
    return new V1Endpoints().metadata(metadata);
  }

  @Test
  void identityAndDescription() {
    assertThat(lock.identity()).isEqualTo(IDENTITY);
    assertThat(lock.describe()).isEqualTo(NAMESPACE + "/" + NAME);
  }

  @Test
  void getRecordFromAnnotation() throws ApiException {
    LeaderElectionRecord expected =
        new LeaderElectionRecord(IDENTITY, 10, new Date(1000L), new Date(2000L), 3);
    apiServer.stubFor(
        get(urlPathEqualTo(ENDPOINTS_PATH + "/" + NAME))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(apiClient.getJSON().serialize(endpoints(expected)))));

    LeaderElectionRecord record = lock.get();

    assertThat(record.getHolderIdentity()).isEqualTo(IDENTITY);
    assertThat(record.getLeaseDurationSeconds()).isEqualTo(10);
    assertThat(record.getLeaderTransitions()).isEqualTo(3);
    assertThat(record.getAcquireTime()).isEqualTo(expected.getAcquireTime());
    assertThat(record.getRenewTime()).isEqualTo(expected.getRenewTime());
  }

  @Test
  void getWithoutAnnotationReturnsEmptyRecord() throws ApiException {
    apiServer.stubFor(
        get(urlPathEqualTo(ENDPOINTS_PATH + "/" + NAME))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(apiClient.getJSON().serialize(endpoints(null)))));

    LeaderElectionRecord record = lock.get();

    assertThat(record.getHolderIdentity()).isNull();
    assertThat(record.getLeaseDurationSeconds()).isZero();
  }

  @Test
  void createEndpoints() {
    LeaderElectionRecord record =
        new LeaderElectionRecord(IDENTITY, 10, new Date(1000L), new Date(2000L), 0);
    apiServer.stubFor(
        post(urlPathEqualTo(ENDPOINTS_PATH))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withBody(apiClient.getJSON().serialize(endpoints(record)))));

    assertThat(lock.create(record)).isTrue();
    apiServer.verify(1, postRequestedFor(urlPathEqualTo(ENDPOINTS_PATH)));
  }

  @Test
  void createEndpointsConflict() {
    apiServer.stubFor(post(urlPathEqualTo(ENDPOINTS_PATH)).willReturn(aResponse().withStatus(409)));

    assertThat(lock.create(new LeaderElectionRecord(IDENTITY, 10, new Date(), new Date(), 0)))
        .isFalse();
  }

  @Test
  void updateEndpointsAfterGet() throws ApiException {
    LeaderElectionRecord record =
        new LeaderElectionRecord(IDENTITY, 10, new Date(1000L), new Date(2000L), 0);
    apiServer.stubFor(
        get(urlPathEqualTo(ENDPOINTS_PATH + "/" + NAME))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(apiClient.getJSON().serialize(endpoints(record)))));
    apiServer.stubFor(
        put(urlPathEqualTo(ENDPOINTS_PATH + "/" + NAME))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(apiClient.getJSON().serialize(endpoints(record)))));
    lock.get();

    assertThat(lock.update(record)).isTrue();
    apiServer.verify(1, putRequestedFor(urlPathEqualTo(ENDPOINTS_PATH + "/" + NAME)));
  }

  @Test
  void updateEndpointsServerError() throws ApiException {
    LeaderElectionRecord record =
        new LeaderElectionRecord(IDENTITY, 10, new Date(1000L), new Date(2000L), 0);
    apiServer.stubFor(
        get(urlPathEqualTo(ENDPOINTS_PATH + "/" + NAME))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(apiClient.getJSON().serialize(endpoints(record)))));
    apiServer.stubFor(
        put(urlPathEqualTo(ENDPOINTS_PATH + "/" + NAME)).willReturn(aResponse().withStatus(500)));
    lock.get();

    assertThat(lock.update(record)).isFalse();
  }
}
