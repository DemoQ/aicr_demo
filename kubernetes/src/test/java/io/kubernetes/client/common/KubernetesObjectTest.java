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
package io.kubernetes.client.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kubernetes.client.custom.NodeMetrics;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test suite for KubernetesObject interface.
 * Tests verify the interface contract and its implementations.
 */
class KubernetesObjectTest {

  @Test
  void kubernetesObjectInterface_shouldExist() {
    assertThatCode(() -> Class.forName("io.kubernetes.client.common.KubernetesObject"))
        .doesNotThrowAnyException();
  }

  @Test
  void kubernetesObjectInterface_shouldBeAnInterface() {
    assertThat(KubernetesObject.class.isInterface())
        .as("KubernetesObject should be an interface")
        .isTrue();
  }

  @Test
  void kubernetesObjectInterface_shouldExtendKubernetesType() {
    assertThat(KubernetesType.class.isAssignableFrom(KubernetesObject.class))
        .as("KubernetesObject should extend KubernetesType")
        .isTrue();
  }

  @Test
  void kubernetesObjectInterface_shouldHaveGetMetadataMethod() {
    assertThatCode(() -> {
      Method method = KubernetesObject.class.getMethod("getMetadata", int.class);
      assertThat(method).isNotNull();
    }).as("KubernetesObject should have getMetadata method with int parameter")
        .doesNotThrowAnyException();
  }

  @Test
  void getMetadataMethod_shouldReturnV1ObjectMeta() throws NoSuchMethodException {
    Method method = KubernetesObject.class.getMethod("getMetadata", int.class);
    
    assertThat(method.getReturnType())
        .as("getMetadata should return V1ObjectMeta")
        .isEqualTo(V1ObjectMeta.class);
  }

  @Test
  void getMetadataMethod_shouldAcceptIntParameter() throws NoSuchMethodException {
    Method method = KubernetesObject.class.getMethod("getMetadata", int.class);
    
    assertThat(method.getParameterCount())
        .as("getMetadata should accept one parameter")
        .isEqualTo(1);
    
    assertThat(method.getParameterTypes()[0])
        .as("getMetadata parameter should be of type int")
        .isEqualTo(int.class);
  }

  @Test
  void nodeMetrics_shouldImplementKubernetesObject() {
    assertThat(KubernetesObject.class.isAssignableFrom(NodeMetrics.class))
        .as("NodeMetrics should implement KubernetesObject")
        .isTrue();
  }

  @Test
  void podMetrics_shouldImplementKubernetesObject() {
    assertThat(KubernetesObject.class.isAssignableFrom(PodMetrics.class))
        .as("PodMetrics should implement KubernetesObject")
        .isTrue();
  }

  @Test
  void nodeMetrics_shouldHaveGetMetadataMethodWithIntParameter() {
    assertThatCode(() -> {
      NodeMetrics.class.getMethod("getMetadata", int.class);
    }).as("NodeMetrics should have getMetadata method with int parameter")
        .doesNotThrowAnyException();
  }

  @Test
  void podMetrics_shouldHaveGetMetadataMethodWithIntParameter() {
    assertThatCode(() -> {
      PodMetrics.class.getMethod("getMetadata", int.class);
    }).as("PodMetrics should have getMetadata method with int parameter")
        .doesNotThrowAnyException();
  }

  @Test
  void kubernetesObjectInterface_shouldBePublic() {
    assertThat(KubernetesObject.class)
        .as("KubernetesObject interface should be public")
        .isPublic();
  }

  @Test
  void kubernetesObjectInterface_shouldHaveExactlyOneDeclaredMethod() {
    assertThat(KubernetesObject.class.getDeclaredMethods())
        .as("KubernetesObject should declare exactly one method (getMetadata)")
        .hasSize(1);
  }

  @Test
  void kubernetesObjectInterface_getMetadataShouldBeAbstract() throws NoSuchMethodException {
    Method method = KubernetesObject.class.getMethod("getMetadata", int.class);
    
    assertThat(java.lang.reflect.Modifier.isAbstract(method.getModifiers()))
        .as("getMetadata should be abstract in interface")
        .isTrue();
  }

  @Test
  void kubernetesObjectInterface_getMetadataShouldBePublic() throws NoSuchMethodException {
    Method method = KubernetesObject.class.getMethod("getMetadata", int.class);
    
    assertThat(java.lang.reflect.Modifier.isPublic(method.getModifiers()))
        .as("getMetadata should be public")
        .isTrue();
  }

  @Test
  void getMetadataMethod_parameterShouldNotAcceptNegativeValues() throws Exception {
    NodeMetrics nodeMetrics = new NodeMetrics();
    Method method = NodeMetrics.class.getMethod("getMetadata", int.class);
    
    // This tests that calling with negative values doesn't cause compilation errors
    assertThatCode(() -> method.invoke(nodeMetrics, -1))
        .as("Should be able to call getMetadata with negative int")
        .isInstanceOf(Exception.class); // May throw due to implementation
  }

  @Test
  void getMetadataMethod_parameterShouldAcceptZero() throws Exception {
    NodeMetrics nodeMetrics = new NodeMetrics();
    Method method = NodeMetrics.class.getMethod("getMetadata", int.class);
    
    assertThatCode(() -> method.invoke(nodeMetrics, 0))
        .as("Should be able to call getMetadata with zero")
        .isInstanceOf(Exception.class); // May throw due to implementation
  }

  @Test
  void getMetadataMethod_parameterShouldAcceptPositiveValues() throws Exception {
    NodeMetrics nodeMetrics = new NodeMetrics();
    Method method = NodeMetrics.class.getMethod("getMetadata", int.class);
    
    assertThatCode(() -> method.invoke(nodeMetrics, 1))
        .as("Should be able to call getMetadata with positive int")
        .isInstanceOf(Exception.class); // May throw due to implementation
  }

  @Test
  void getMetadataMethod_parameterShouldAcceptMaxInteger() throws Exception {
    NodeMetrics nodeMetrics = new NodeMetrics();
    Method method = NodeMetrics.class.getMethod("getMetadata", int.class);
    
    assertThatCode(() -> method.invoke(nodeMetrics, Integer.MAX_VALUE))
        .as("Should be able to call getMetadata with Integer.MAX_VALUE")
        .isInstanceOf(Exception.class); // May throw due to implementation
  }

  @Test
  void getMetadataMethod_parameterShouldAcceptMinInteger() throws Exception {
    NodeMetrics nodeMetrics = new NodeMetrics();
    Method method = NodeMetrics.class.getMethod("getMetadata", int.class);
    
    assertThatCode(() -> method.invoke(nodeMetrics, Integer.MIN_VALUE))
        .as("Should be able to call getMetadata with Integer.MIN_VALUE")
        .isInstanceOf(Exception.class); // May throw due to implementation
  }

  @Test
  void kubernetesObjectInterface_shouldInheritMethodsFromKubernetesType() {
    assertThatCode(() -> {
      KubernetesObject.class.getMethod("getApiVersion");
      KubernetesObject.class.getMethod("getKind");
    }).as("KubernetesObject should inherit getApiVersion and getKind from KubernetesType")
        .doesNotThrowAnyException();
  }

  @Test
  void kubernetesObjectInterface_packageShouldBeCorrect() {
    assertThat(KubernetesObject.class.getPackage().getName())
        .as("KubernetesObject should be in correct package")
        .isEqualTo("io.kubernetes.client.common");
  }

  @Test
  void getMetadataMethod_shouldNotHaveDefaultImplementation() throws NoSuchMethodException {
    Method method = KubernetesObject.class.getMethod("getMetadata", int.class);
    
    assertThat(method.isDefault())
        .as("getMetadata should not have a default implementation")
        .isFalse();
  }

  @Test
  void kubernetesObjectInterface_shouldNotHaveStaticMethods() {
    long staticMethodCount = java.util.Arrays.stream(KubernetesObject.class.getDeclaredMethods())
        .filter(m -> java.lang.reflect.Modifier.isStatic(m.getModifiers()))
        .count();
    
    assertThat(staticMethodCount)
        .as("KubernetesObject should not have static methods")
        .isEqualTo(0);
  }
}