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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.kubernetes.client.extended.leaderelection.LeaderElectionRecord;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Lease;
import io.kubernetes.client.openapi.models.V1LeaseSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.util.ClientBuilder;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LeaseLockTest {

  private static final String NAMESPACE = "default";
  private static final String NAME = "leader-election";
  private static final String IDENTITY = "foo";
  private static final String LEASE_PATH =
      "/apis/coordination.k8s.io/v1/namespaces/" + NAMESPACE + "/leases";

  private ApiClient apiClient;
  private LeaseLock lock;

  @RegisterExtension
  static WireMockExtension apiServer =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @BeforeEach
  void setup() {
    apiClient = new ClientBuilder().setBasePath("http://localhost:" + apiServer.getPort()).build();
    lock = new LeaseLock(NAMESPACE, NAME, IDENTITY, apiClient);
  }

  private static V1Lease lease(Date acquireTime, Date renewTime) {
    return new V1Lease()
        .metadata(new V1ObjectMeta().namespace(NAMESPACE).name(NAME))
        .spec(
            new V1LeaseSpec()
                .holderIdentity(IDENTITY)
                .leaseDurationSeconds(10)
                .leaseTransitions(3)
                .acquireTime(
                    OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(acquireTime.getTime()), ZoneOffset.UTC))
                .renewTime(
                    OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(renewTime.getTime()), ZoneOffset.UTC)));
  }

  @Test
  void identityAndDescription() {
    assertThat(lock.identity()).isEqualTo(IDENTITY);
    assertThat(lock.describe()).isEqualTo(NAMESPACE + "/" + NAME);
  }

  @Test
  void getExistingLease() throws ApiException {
    Date acquireTime = new Date(1000L);
    Date renewTime = new Date(2000L);
    apiServer.stubFor(
        get(urlPathEqualTo(LEASE_PATH + "/" + NAME))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(apiClient.getJSON().serialize(lease(acquireTime, renewTime)))));

    LeaderElectionRecord record = lock.get();

    assertThat(record.getHolderIdentity()).isEqualTo(IDENTITY);
    assertThat(record.getLeaseDurationSeconds()).isEqualTo(10);
    assertThat(record.getLeaderTransitions()).isEqualTo(3);
    assertThat(record.getAcquireTime()).isEqualTo(acquireTime);
    assertThat(record.getRenewTime()).isEqualTo(renewTime);
  }

  @Test
  void getMissingLease() {
    apiServer.stubFor(
        get(urlPathEqualTo(LEASE_PATH + "/" + NAME)).willReturn(aResponse().withStatus(404)));

    assertThatThrownBy(() -> lock.get()).isInstanceOf(ApiException.class);
  }

  @Test
  void createLease() {
    apiServer.stubFor(
        post(urlPathEqualTo(LEASE_PATH))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withBody(
                        apiClient.getJSON().serialize(lease(new Date(1000L), new Date(2000L))))));

    LeaderElectionRecord record =
        new LeaderElectionRecord(IDENTITY, 10, new Date(1000L), new Date(2000L), 3);
    record.setOwnerReference(new V1OwnerReference().kind("Pod").name("owner").uid("uid"));

    assertThat(lock.create(record)).isTrue();
    apiServer.verify(1, postRequestedFor(urlPathEqualTo(LEASE_PATH)));
  }

  @Test
  void createLeaseConflict() {
    apiServer.stubFor(post(urlPathEqualTo(LEASE_PATH)).willReturn(aResponse().withStatus(409)));

    assertThat(lock.create(new LeaderElectionRecord(IDENTITY, 10, new Date(), new Date(), 0)))
        .isFalse();
  }

  @Test
  void createLeaseServerError() {
    apiServer.stubFor(post(urlPathEqualTo(LEASE_PATH)).willReturn(aResponse().withStatus(500)));

    assertThat(lock.create(new LeaderElectionRecord(IDENTITY, 10, new Date(), new Date(), 0)))
        .isFalse();
  }

  @Test
  void updateLeaseAfterGet() throws ApiException {
    apiServer.stubFor(
        get(urlPathEqualTo(LEASE_PATH + "/" + NAME))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(
                        apiClient.getJSON().serialize(lease(new Date(1000L), new Date(2000L))))));
    apiServer.stubFor(
        put(urlPathEqualTo(LEASE_PATH + "/" + NAME))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(
                        apiClient.getJSON().serialize(lease(new Date(1000L), new Date(3000L))))));
    lock.get();

    assertThat(
            lock.update(new LeaderElectionRecord(IDENTITY, 10, new Date(1000L), new Date(3000L), 3)))
        .isTrue();
    apiServer.verify(1, putRequestedFor(urlPathEqualTo(LEASE_PATH + "/" + NAME)));
  }

  @Test
  void updateLeaseConflict() throws ApiException {
    apiServer.stubFor(
        get(urlPathEqualTo(LEASE_PATH + "/" + NAME))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(
                        apiClient.getJSON().serialize(lease(new Date(1000L), new Date(2000L))))));
    apiServer.stubFor(
        put(urlPathEqualTo(LEASE_PATH + "/" + NAME)).willReturn(aResponse().withStatus(409)));
    lock.get();

    assertThat(lock.update(new LeaderElectionRecord(IDENTITY, 10, new Date(), new Date(), 3)))
        .isFalse();
  }
}
