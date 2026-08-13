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
package io.kubernetes.client.util.conversion;

import static org.assertj.core.api.Assertions.assertThat;

import io.kubernetes.client.openapi.models.V1CronJob;
import io.kubernetes.client.openapi.models.V1CronJobSpec;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobSpec;
import io.kubernetes.client.openapi.models.V1JobTemplateSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class JobsTest {

  private static V1CronJob cronJob(V1JobTemplateSpec jobTemplate) {
    return new V1CronJob()
        .metadata(new V1ObjectMeta().name("my-cronjob").uid("my-uid"))
        .spec(new V1CronJobSpec().schedule("* * * * *").jobTemplate(jobTemplate));
  }

  @Test
  void copiesTemplateMetadataAndSpec() {
    V1JobSpec jobSpec = new V1JobSpec().template(new V1PodTemplateSpec());
    V1JobTemplateSpec jobTemplate =
        new V1JobTemplateSpec()
            .metadata(
                new V1ObjectMeta()
                    .annotations(Collections.singletonMap("anno", "value"))
                    .labels(Collections.singletonMap("label", "value")))
            .spec(jobSpec);

    V1Job job = Jobs.cronJobToJob(cronJob(jobTemplate), "my-job");

    assertThat(job.getKind()).isEqualTo("Job");
    assertThat(job.getApiVersion()).isEqualTo("batch/v1");
    assertThat(job.getSpec()).isSameAs(jobSpec);
    assertThat(job.getMetadata().getName()).isEqualTo("my-job");
    assertThat(job.getMetadata().getLabels()).containsEntry("label", "value");
    assertThat(job.getMetadata().getAnnotations())
        .containsEntry("anno", "value")
        .containsEntry("cronjob.kubernetes.io/instantiate", "manual");
  }

  @Test
  void defaultsJobNameFromCronJob() {
    V1Job job = Jobs.cronJobToJob(cronJob(new V1JobTemplateSpec()), null);

    assertThat(job.getMetadata().getName()).isEqualTo("my-cronjob-manual");
    assertThat(job.getSpec()).isNull();
    assertThat(job.getMetadata().getLabels()).isEmpty();
    assertThat(job.getMetadata().getAnnotations())
        .containsExactly(
            org.assertj.core.data.MapEntry.entry("cronjob.kubernetes.io/instantiate", "manual"));
  }

  @Test
  void setsOwnerReferenceToCronJob() {
    V1Job job = Jobs.cronJobToJob(cronJob(new V1JobTemplateSpec()), "my-job");

    assertThat(job.getMetadata().getOwnerReferences()).hasSize(1);
    V1OwnerReference ownerReference = job.getMetadata().getOwnerReferences().get(0);
    assertThat(ownerReference.getKind()).isEqualTo("CronJob");
    assertThat(ownerReference.getName()).isEqualTo("my-cronjob");
    assertThat(ownerReference.getUid()).isEqualTo("my-uid");
    assertThat(ownerReference.getApiVersion()).isEqualTo("batch/v1beta1");
    assertThat(ownerReference.getController()).isTrue();
    assertThat(ownerReference.getBlockOwnerDeletion()).isTrue();
  }

  @Test
  void toleratesMissingCronJobSpec() {
    V1CronJob cronJob = new V1CronJob().metadata(new V1ObjectMeta().name("my-cronjob"));

    V1Job job = Jobs.cronJobToJob(cronJob, null);

    assertThat(job.getMetadata().getName()).isEqualTo("my-cronjob-manual");
    assertThat(job.getSpec()).isNull();
  }
}
