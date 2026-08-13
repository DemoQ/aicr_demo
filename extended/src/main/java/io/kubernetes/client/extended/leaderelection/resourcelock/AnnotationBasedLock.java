/*
Copyright 2020 The Kubernetes Authors.
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

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.extended.leaderelection.LeaderElectionRecord;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * Base implementation for locks storing the {@link LeaderElectionRecord} in an annotation of the
 * lock resource.
 *
 * @param <ApiType> the type of the resource holding the lock
 */
abstract class AnnotationBasedLock<ApiType extends KubernetesObject> extends AbstractLock {

  static final String LeaderElectionRecordAnnotationKey =
      "control-plane.alpha.kubernetes.io/leader";

  private final AtomicReference<ApiType> resourceRefer = new AtomicReference<>(null);

  protected AnnotationBasedLock(String namespace, String name, String identity) {
    super(namespace, name, identity);
  }

  /** The api client used to serialize and deserialize the leader election record. */
  protected abstract ApiClient getApiClient();

  /** The logger of the concrete lock, so that log entries keep their original source class. */
  protected abstract Logger getLogger();

  /** The human-readable resource kind used in log messages, e.g. "configmap". */
  protected abstract String getLockKind();

  protected abstract ApiType readResource() throws ApiException;

  protected abstract ApiType createResource(V1ObjectMeta objectMeta) throws ApiException;

  protected abstract ApiType replaceResource(ApiType resource) throws ApiException;

  @Override
  public LeaderElectionRecord get() throws ApiException {
    ApiType resource = readResource();
    resourceRefer.set(resource);

    Map<String, String> annotations = resource.getMetadata().getAnnotations();
    if (annotations == null || annotations.isEmpty()) {
      resource.getMetadata().setAnnotations(new HashMap<>());
    }

    String recordRawStringContent =
        resource.getMetadata().getAnnotations().get(LeaderElectionRecordAnnotationKey);
    if (StringUtils.isEmpty(recordRawStringContent)) {
      return new LeaderElectionRecord();
    }
    return getApiClient()
        .getJSON()
        .deserialize(recordRawStringContent, LeaderElectionRecord.class);
  }

  @Override
  public boolean create(LeaderElectionRecord record) {
    try {
      V1ObjectMeta objectMeta = new V1ObjectMeta();
      objectMeta.setName(name);
      objectMeta.setNamespace(namespace);
      Map<String, String> annotations = new HashMap<>();
      annotations.put(LeaderElectionRecordAnnotationKey, getApiClient().getJSON().serialize(record));
      objectMeta.setAnnotations(annotations);
      if (record.getOwnerReference() != null) {
        objectMeta.setOwnerReferences(Collections.singletonList(record.getOwnerReference()));
      }
      resourceRefer.set(createResource(objectMeta));
      return true;
    } catch (ApiException e) {
      logApiException(getLogger(), "creating", getLockKind(), e);
      return false;
    }
  }

  @Override
  public boolean update(LeaderElectionRecord record) {
    try {
      ApiType resource = resourceRefer.get();
      resource
          .getMetadata()
          .putAnnotationsItem(
              LeaderElectionRecordAnnotationKey, getApiClient().getJSON().serialize(record));
      // TODO consider to retry if receiving a 409 code
      resourceRefer.set(replaceResource(resource));
      return true;
    } catch (ApiException e) {
      logApiException(getLogger(), "updating", getLockKind(), e);
      return false;
    }
  }
}
