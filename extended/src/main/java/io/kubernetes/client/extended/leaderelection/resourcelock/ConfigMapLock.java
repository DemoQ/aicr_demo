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

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigMapLock extends AnnotationBasedLock<V1ConfigMap> {

  private static final Logger log = LoggerFactory.getLogger(ConfigMapLock.class);

  private CoreV1Api coreV1Client;

  public ConfigMapLock(String namespace, String name, String identity) {
    this(namespace, name, identity, Configuration.getDefaultApiClient());
  }

  public ConfigMapLock(String namespace, String name, String identity, ApiClient apiClient) {
    super(namespace, name, identity);
    this.coreV1Client = new CoreV1Api(apiClient);
  }

  @Override
  protected ApiClient getApiClient() {
    return coreV1Client.getApiClient();
  }

  @Override
  protected Logger getLogger() {
    return log;
  }

  @Override
  protected String getLockKind() {
    return "configmap";
  }

  @Override
  protected V1ConfigMap readResource() throws ApiException {
    return coreV1Client.readNamespacedConfigMap(name, namespace).execute();
  }

  @Override
  protected V1ConfigMap createResource(V1ObjectMeta objectMeta) throws ApiException {
    V1ConfigMap configMap = new V1ConfigMap();
    configMap.setMetadata(objectMeta);
    return coreV1Client.createNamespacedConfigMap(namespace, configMap).execute();
  }

  @Override
  protected V1ConfigMap replaceResource(V1ConfigMap configMap) throws ApiException {
    return coreV1Client.replaceNamespacedConfigMap(name, namespace, configMap).execute();
  }
}
