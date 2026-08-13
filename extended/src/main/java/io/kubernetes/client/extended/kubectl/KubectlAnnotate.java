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
package io.kubernetes.client.extended.kubectl;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.extended.kubectl.exception.KubectlException;
import io.kubernetes.client.util.annotations.Annotations;
import java.util.HashMap;
import java.util.Map;

public class KubectlAnnotate<ApiType extends KubernetesObject>
    extends Kubectl.ResourceBuilder<ApiType, KubectlAnnotate<ApiType>>
    implements Kubectl.Executable<ApiType> {

  private final Map<String, String> addingAnnotations;

  KubectlAnnotate(Class<ApiType> apiTypeClass) {
    super(apiTypeClass);
    this.addingAnnotations = new HashMap<>();
  }

  public KubectlAnnotate<ApiType> addAnnotation(String key, String value) {
    this.addingAnnotations.put(key, value);
    return this;
  }

  @Override
  public ApiType execute() throws KubectlException {
    verifyName();
    refreshDiscovery();

    ApiType currentObj = getCurrentObject();
    Annotations.addAnnotations(currentObj, addingAnnotations);
    return updateObject(currentObj);
  }
}
