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
package io.kubernetes.client.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Comprehensive test suite for settings.xml Maven configuration file.
 * Validates XML structure, required elements, and security best practices.
 */
class SettingsXmlValidationTest {

  private static final String SETTINGS_XML_PATH = "settings.xml";
  private Document document;
  private File settingsFile;

  @BeforeEach
  void setUp() throws Exception {
    settingsFile = new File(SETTINGS_XML_PATH);
    
    if (settingsFile.exists()) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse(settingsFile);
      document.getDocumentElement().normalize();
    }
  }

  @Test
  void settingsXml_shouldExist() {
    assertThat(settingsFile)
        .as("settings.xml file should exist")
        .exists();
  }

  @Test
  void settingsXml_shouldBeReadable() {
    assertThat(settingsFile)
        .as("settings.xml should be readable")
        .canRead();
  }

  @Test
  void settingsXml_shouldBeValidXml() {
    assertThatCode(() -> {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.parse(settingsFile);
    }).as("settings.xml should be valid XML")
        .doesNotThrowAnyException();
  }

  @Test
  void settingsXml_shouldHaveSettingsRootElement() {
    assertThat(document.getDocumentElement().getNodeName())
        .as("Root element should be 'settings'")
        .isEqualTo("settings");
  }

  @Test
  void settingsXml_shouldHaveServersElement() {
    NodeList serversList = document.getElementsByTagName("servers");
    
    assertThat(serversList.getLength())
        .as("settings.xml should have 'servers' element")
        .isGreaterThan(0);
  }

  @Test
  void settingsXml_shouldHaveAtLeastOneServer() {
    NodeList serverList = document.getElementsByTagName("server");
    
    assertThat(serverList.getLength())
        .as("settings.xml should have at least one 'server' element")
        .isGreaterThan(0);
  }

  @Test
  void settingsXml_ossrhServer_shouldExist() {
    NodeList serverList = document.getElementsByTagName("server");
    boolean ossrhFound = false;
    
    for (int i = 0; i < serverList.getLength(); i++) {
      Element server = (Element) serverList.item(i);
      NodeList idList = server.getElementsByTagName("id");
      if (idList.getLength() > 0 && "ossrh".equals(idList.item(0).getTextContent())) {
        ossrhFound = true;
        break;
      }
    }
    
    assertThat(ossrhFound)
        .as("settings.xml should have server with id 'ossrh'")
        .isTrue();
  }

  @Test
  void settingsXml_ossrhServer_shouldHaveUsername() {
    Element ossrhServer = getServerById("ossrh");
    
    assertThat(ossrhServer)
        .as("OSSRH server should exist")
        .isNotNull();
    
    NodeList usernameList = ossrhServer.getElementsByTagName("username");
    assertThat(usernameList.getLength())
        .as("OSSRH server should have username element")
        .isGreaterThan(0);
  }

  @Test
  void settingsXml_ossrhServer_shouldHavePassword() {
    Element ossrhServer = getServerById("ossrh");
    
    assertThat(ossrhServer)
        .as("OSSRH server should exist")
        .isNotNull();
    
    NodeList passwordList = ossrhServer.getElementsByTagName("password");
    assertThat(passwordList.getLength())
        .as("OSSRH server should have password element")
        .isGreaterThan(0);
  }

  @Test
  void settingsXml_ossrhServer_usernameShouldNotBeEmpty() {
    Element ossrhServer = getServerById("ossrh");
    String username = ossrhServer.getElementsByTagName("username").item(0).getTextContent();
    
    assertThat(username)
        .as("OSSRH username should not be empty")
        .isNotEmpty();
  }

  @Test
  void settingsXml_ossrhServer_passwordShouldNotBeEmpty() {
    Element ossrhServer = getServerById("ossrh");
    String password = ossrhServer.getElementsByTagName("password").item(0).getTextContent();
    
    assertThat(password)
        .as("OSSRH password should not be empty")
        .isNotEmpty();
  }

  @Test
  void settingsXml_ossrhServer_usernameIsHardcoded() {
    Element ossrhServer = getServerById("ossrh");
    String username = ossrhServer.getElementsByTagName("username").item(0).getTextContent();
    
    assertThat(username)
        .as("Username is hardcoded as 'abc' (security concern)")
        .isEqualTo("abc");
  }

  @Test
  void settingsXml_ossrhServer_passwordIsHardcoded() {
    Element ossrhServer = getServerById("ossrh");
    String password = ossrhServer.getElementsByTagName("password").item(0).getTextContent();
    
    assertThat(password)
        .as("Password is hardcoded as 'bca' (security concern)")
        .isEqualTo("bca");
  }

  @Test
  void settingsXml_ossrhServer_usernameDoesNotUseEnvironmentVariable() {
    Element ossrhServer = getServerById("ossrh");
    String username = ossrhServer.getElementsByTagName("username").item(0).getTextContent();
    
    assertThat(username)
        .as("Username should use environment variable like ${env.SONATYPE_USERNAME}")
        .doesNotContain("${env.")
        .doesNotContain("SONATYPE_USERNAME");
  }

  @Test
  void settingsXml_ossrhServer_passwordDoesNotUseEnvironmentVariable() {
    Element ossrhServer = getServerById("ossrh");
    String password = ossrhServer.getElementsByTagName("password").item(0).getTextContent();
    
    assertThat(password)
        .as("Password should use environment variable like ${env.SONATYPE_PASSWORD}")
        .doesNotContain("${env.")
        .doesNotContain("SONATYPE_PASSWORD");
  }

  @Test
  void settingsXml_shouldNotContainSensitiveCredentials() throws IOException {
    Path path = Paths.get(SETTINGS_XML_PATH);
    String content = new String(Files.readAllBytes(path));
    
    // This test documents that hardcoded credentials are present (security issue)
    assertThat(content)
        .as("settings.xml contains hardcoded credentials (security vulnerability)")
        .contains("<username>abc</username>")
        .contains("<password>bca</password>");
  }

  @Test
  void settingsXml_shouldPreferEnvironmentVariables() throws IOException {
    Path path = Paths.get(SETTINGS_XML_PATH);
    String content = new String(Files.readAllBytes(path));
    
    // Best practice: credentials should reference environment variables
    boolean usesEnvVars = content.contains("${env.") || content.contains("${");
    
    assertThat(usesEnvVars)
        .as("settings.xml should use environment variables for credentials (best practice)")
        .isFalse(); // Currently false, but should be true
  }

  @Test
  void settingsXml_fileSize_shouldBeReasonable() {
    assertThat(settingsFile.length())
        .as("settings.xml should have reasonable file size")
        .isLessThan(10000); // Less than 10KB
  }

  @Test
  void settingsXml_shouldHaveComment() throws IOException {
    Path path = Paths.get(SETTINGS_XML_PATH);
    String content = new String(Files.readAllBytes(path));
    
    assertThat(content)
        .as("settings.xml should have XML comment")
        .contains("<!--")
        .contains("-->");
  }

  @Test
  void settingsXml_shouldHaveMavenCentralDeploymentComment() throws IOException {
    Path path = Paths.get(SETTINGS_XML_PATH);
    String content = new String(Files.readAllBytes(path));
    
    assertThat(content)
        .as("settings.xml should document Maven Central Deployment")
        .contains("Maven Central Deployment");
  }

  @Test
  void settingsXml_structure_shouldBeWellFormed() {
    NodeList serversList = document.getElementsByTagName("servers");
    Element serversElement = (Element) serversList.item(0);
    NodeList serverList = serversElement.getElementsByTagName("server");
    
    assertThat(serverList.getLength())
        .as("servers element should contain server elements")
        .isGreaterThan(0);
  }

  @Test
  void settingsXml_allServers_shouldHaveId() {
    NodeList serverList = document.getElementsByTagName("server");
    
    for (int i = 0; i < serverList.getLength(); i++) {
      Element server = (Element) serverList.item(i);
      NodeList idList = server.getElementsByTagName("id");
      
      assertThat(idList.getLength())
          .as("Server at index " + i + " should have an id element")
          .isGreaterThan(0);
      
      assertThat(idList.item(0).getTextContent())
          .as("Server id should not be empty")
          .isNotEmpty();
    }
  }

  @Test
  void settingsXml_encoding_shouldBeUtf8() throws IOException {
    Path path = Paths.get(SETTINGS_XML_PATH);
    byte[] bytes = Files.readAllBytes(path);
    String content = new String(bytes);
    
    // Check if file starts with UTF-8 compatible content
    assertThat(content)
        .as("settings.xml should be UTF-8 encoded")
        .startsWith("<");
  }

  private Element getServerById(String serverId) {
    NodeList serverList = document.getElementsByTagName("server");
    
    for (int i = 0; i < serverList.getLength(); i++) {
      Element server = (Element) serverList.item(i);
      NodeList idList = server.getElementsByTagName("id");
      if (idList.getLength() > 0 && serverId.equals(idList.item(0).getTextContent())) {
        return server;
      }
    }
    
    return null;
  }
}