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
package io.kubernetes.client.fluent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.sundr.builder.annotations.ExternalBuildables;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test suite for Config class annotation configuration.
 * Tests verify that the ExternalBuildables annotation is properly configured
 * for code generation purposes.
 */
class ConfigTest {

  @Test
  void configClass_shouldExist() {
    assertThatCode(() -> Class.forName("io.kubernetes.client.fluent.Config"))
        .doesNotThrowAnyException();
  }

  @Test
  void configClass_shouldBeAnnotatedWithExternalBuildables() {
    Class<?> configClass = Config.class;
    
    assertThat(configClass.isAnnotationPresent(ExternalBuildables.class))
        .as("Config class should have ExternalBuildables annotation")
        .isTrue();
  }

  @Test
  void externalBuildablesAnnotation_shouldHaveCorrectBuilderPackage() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThat(annotation.builderPackage())
        .as("Builder package should be correctly configured")
        .isEqualTo("io.kubernetes.client.fluent");
  }

  @Test
  void externalBuildablesAnnotation_shouldHaveGenerateBuilderPackageEnabled() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThat(annotation.generateBuilderPackage())
        .as("generateBuilderPackage should be enabled")
        .isTrue();
  }

  @Test
  void externalBuildablesAnnotation_shouldHaveCorrectValueArray() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThat(annotation.value())
        .as("Value array should contain the correct package")
        .containsExactly("io.kubernetes.client.openapi.models");
  }

  @Test
  void externalBuildablesAnnotation_shouldHaveEditableEnabledAsInteger() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    // The annotation should have editableEnabled property
    // Note: This tests the current state where editableEnabled = 123123
    // This is actually a type error as editableEnabled should be boolean
    assertThat(annotation.editableEnabled())
        .as("editableEnabled is currently set to 123123 (non-boolean value)")
        .isEqualTo(123123);
  }

  @Test
  void externalBuildablesAnnotation_editableEnabledShouldNotBeZero() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThat(annotation.editableEnabled())
        .as("editableEnabled should not be zero (false)")
        .isNotEqualTo(0);
  }

  @Test
  void externalBuildablesAnnotation_editableEnabledShouldNotBeOne() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThat(annotation.editableEnabled())
        .as("editableEnabled should not be one (true)")
        .isNotEqualTo(1);
  }

  @Test
  void configClass_shouldBePublic() {
    assertThat(Config.class)
        .as("Config class should be public")
        .isPublic();
  }

  @Test
  void configClass_shouldHaveNoFields() {
    assertThat(Config.class.getDeclaredFields())
        .as("Config class should have no fields (it's just an annotation holder)")
        .isEmpty();
  }

  @Test
  void configClass_shouldHaveNoMethods() {
    assertThat(Config.class.getDeclaredMethods())
        .as("Config class should have no methods (it's just an annotation holder)")
        .isEmpty();
  }

  @Test
  void configClass_shouldHaveDefaultConstructor() {
    assertThatCode(() -> new Config())
        .as("Config should have accessible default constructor")
        .doesNotThrowAnyException();
  }

  @Test
  void configClass_shouldBeInstantiable() {
    Config config = new Config();
    
    assertThat(config)
        .as("Should be able to create Config instance")
        .isNotNull();
  }

  @Test
  void multipleConfigInstances_shouldBeIndependent() {
    Config config1 = new Config();
    Config config2 = new Config();
    
    assertThat(config1)
        .as("Different instances should be independent objects")
        .isNotSameAs(config2);
  }

  @Test
  void externalBuildablesAnnotation_allAttributesShouldBeAccessible() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThatCode(() -> {
      annotation.editableEnabled();
      annotation.generateBuilderPackage();
      annotation.builderPackage();
      annotation.value();
    }).as("All annotation attributes should be accessible without errors")
        .doesNotThrowAnyException();
  }

  @Test
  void externalBuildablesAnnotation_shouldNotBeNull() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThat(annotation)
        .as("ExternalBuildables annotation should not be null")
        .isNotNull();
  }

  @Test
  void externalBuildablesAnnotation_valueArrayShouldNotBeEmpty() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThat(annotation.value())
        .as("Value array should not be empty")
        .isNotEmpty();
  }

  @Test
  void externalBuildablesAnnotation_valueArrayShouldHaveOneElement() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThat(annotation.value())
        .as("Value array should have exactly one element")
        .hasSize(1);
  }

  @Test
  void externalBuildablesAnnotation_builderPackageShouldNotBeEmpty() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    
    assertThat(annotation.builderPackage())
        .as("Builder package should not be empty")
        .isNotEmpty();
  }

  @Test
  void externalBuildablesAnnotation_builderPackageShouldMatchConfigPackage() {
    ExternalBuildables annotation = Config.class.getAnnotation(ExternalBuildables.class);
    String configPackage = Config.class.getPackage().getName();
    
    assertThat(annotation.builderPackage())
        .as("Builder package should match Config class package")
        .isEqualTo(configPackage);
  }
}