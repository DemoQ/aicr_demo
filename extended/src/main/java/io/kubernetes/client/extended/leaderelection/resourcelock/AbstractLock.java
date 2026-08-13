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

import io.kubernetes.client.extended.leaderelection.Lock;
import io.kubernetes.client.openapi.ApiException;
import java.net.HttpURLConnection;
import org.slf4j.Logger;

/** Holds the state and error handling that is common to all {@link Lock} implementations. */
abstract class AbstractLock implements Lock {

  // Namespace and name describes the lock object
  // that the LeaderElector will attempt to lead.
  protected final String namespace;
  protected final String name;
  protected final String identity;

  protected AbstractLock(String namespace, String name, String identity) {
    this.namespace = namespace;
    this.name = name;
    this.identity = identity;
  }

  @Override
  public String identity() {
    return identity;
  }

  @Override
  public String describe() {
    return namespace + "/" + name;
  }

  /**
   * Logs an api exception raised while mutating the lock. Conflicts are expected during leader
   * election and are logged at a lower level than other failures.
   *
   * @param log the logger of the concrete lock implementation
   * @param operation the attempted operation, e.g. "creating"
   * @param lockKind the kind of the lock resource, e.g. "configmap"
   * @param e the raised exception
   */
  protected static void logApiException(
      Logger log, String operation, String lockKind, ApiException e) {
    String message = "received {} when " + operation + " " + lockKind + " lock";
    if (e.getCode() == HttpURLConnection.HTTP_CONFLICT) {
      log.debug(message, e.getCode(), e);
    } else {
      log.error(message, e.getCode(), e);
    }
  }
}
