package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import dev.mccue.resolve.doc.Coursier;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class PomParserTest {

    @Test
    public void parseBasicPOM() throws ParserConfigurationException, SAXException, IOException {
        var pomParser = new PomParser();
        var basicPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                                
                    <groupId>dev.mccue</groupId>
                    <artifactId>resolve</artifactId>
                    <version>0.0.1</version>
                    <packaging>jar</packaging>
                                
                    <properties>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    </properties>
                                
                    <dependencies>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-api</artifactId>
                            <version>5.9.0</version>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-params</artifactId>
                            <version>5.9.0</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                                
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.8.1</version>
                                <scope>
                                    <source>19</source>
                                    <target>19</target>
                                    <compilerArgs>--enable-preview</compilerArgs>
                                </scope>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-source-plugin</artifactId>
                                <version>3.0.1</version>
                                <executions>
                                    <execution>
                                        <id>attach-sources</id>
                                        <goals>
                                            <goal>jar</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-javadoc-plugin</artifactId>
                                <version>3.2.0</version>
                                <executions>
                                    <execution>
                                        <id>attach-javadocs</id>
                                        <goals>
                                            <goal>jar</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-surefire-plugin</artifactId>
                                <version>3.0.0-M7</version>
                                <scope>
                                    <argLine>@{argLine} --enable-preview</argLine>
                                </scope>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.jacoco</groupId>
                                <artifactId>jacoco-maven-plugin</artifactId>
                                <version>0.8.8</version>
                                <executions>
                                    <execution>
                                        <id>jacoco-initialize</id>
                                        <goals>
                                            <goal>prepare-agent</goal>
                                        </goals>
                                    </execution>
                                    <execution>
                                        <id>jacoco-site</id>
                                        <phase>test</phase>
                                        <goals>
                                            <goal>report</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """;
        var factory = SAXParserFactory.newDefaultInstance();
        var saxParser = factory.newSAXParser();
        saxParser.parse(
                new ByteArrayInputStream(basicPom.getBytes(StandardCharsets.UTF_8)),
                pomParser
        );

        var project = pomParser.pomInfo();

        assertEquals(new PomGroupId.Declared("dev.mccue"), project.groupId());
        assertEquals(new PomArtifactId.Declared("resolve"), project.artifactId());
        assertEquals(new PomVersion.Declared("0.0.1"), project.version());
        assertEquals(List.of(new PomProperty("project.build.sourceEncoding", "UTF-8")), project.properties());
        assertEquals(new PomPackaging.Declared("jar"), project.packaging());

        assertEquals(List.of(
                new PomDependency(
                        new PomGroupId.Declared("org.junit.jupiter"),
                        new PomArtifactId.Declared("junit-jupiter-api"),
                        new PomVersion.Declared("5.9.0"),
                        Set.of(),
                        PomType.Undeclared.INSTANCE,
                        PomClassifier.Undeclared.INSTANCE,
                        PomOptionality.Undeclared.INSTANCE,
                        new PomScope.Declared("test")
                ),
                new PomDependency(
                        new PomGroupId.Declared("org.junit.jupiter"),
                        new PomArtifactId.Declared("junit-jupiter-params"),
                        new PomVersion.Declared("5.9.0"),
                        Set.of(),
                        PomType.Undeclared.INSTANCE,
                        PomClassifier.Undeclared.INSTANCE,
                        PomOptionality.Undeclared.INSTANCE,
                        new PomScope.Declared("test")
                )
        ), project.dependencies());
    }

    @Test
    @Coursier("https://github.com/coursier/coursier/blob/main/modules/core/jvm/src/test/scala/coursier/maven/PomParserTests.scala#L73-L91")
    public void propertiesAreParsed() {
        var pom = """
          <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>awesome-project</artifactId>
              <version>1.0-SNAPSHOT</version>
          
              <properties>
                  <info.versionScheme>semver-spec</info.versionScheme>
              </properties>
          </project>""";

        assertEquals(
                PomParser.parse(pom).properties(),
                List.of(new PomProperty("info.versionScheme", "semver-spec"))
        );
    }

    @Test
    public void parseTopLevelGroup() {
        var pom = """
          <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>awesome-project</artifactId>
              <version>1.0-SNAPSHOT</version>
          </project>""";

        assertEquals(
                PomParser.parse(pom).groupId(),
                new PomGroupId.Declared("com.example")
        );
    }

    @Test
    public void parseTopLevelArtifact() {
        var pom = """
          <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>awesome-project</artifactId>
              <version>1.0-SNAPSHOT</version>
          </project>""";

        assertEquals(
                PomParser.parse(pom).artifactId(),
                new PomArtifactId.Declared("awesome-project")
        );
    }

    @Test
    public void parseTopLevelVersion() {
        var pom = """
          <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>awesome-project</artifactId>
              <version>1.0-SNAPSHOT</version>
          </project>""";

        assertEquals(
                PomParser.parse(pom).version(),
                new PomVersion.Declared("1.0-SNAPSHOT")
        );
    }

    @Test
    public void parseParent() {
        var pom = """
          <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>awesome-project</artifactId>
              <version>1.0-SNAPSHOT</version>
              
              <parent>
                <groupId>com.example2</groupId>
                <artifactId>awesome-project-parent</artifactId>
                <version>0.9.9</version>
              </parent>
          </project>""";

        assertEquals(
                PomParser.parse(pom).parent(),
                new PomParent.Declared(
                        new PomGroupId.Declared("com.example2"),
                        new PomArtifactId.Declared("awesome-project-parent"),
                        new PomVersion.Declared("0.9.9")
                )
        );

        pom = """
          <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>awesome-project</artifactId>
              <version>1.0-SNAPSHOT</version>
          </project>""";

        assertEquals(
                PomParser.parse(pom).parent(),
                PomParent.Undeclared.INSTANCE
        );
    }

    @Test
    public void parseNd4jPom() {
        var pom = """
                <!--
                  ~ /*
                  ~  * Copyright 2015 Skymind,Inc.
                  ~  *
                  ~  *    Licensed under the Apache License, Version 2.0 (the "License");
                  ~  *    you may not use this file except in compliance with the License.
                  ~  *    You may obtain a copy of the License at
                  ~  *
                  ~  *        http://www.apache.org/licenses/LICENSE-2.0
                  ~  *
                  ~  *    Unless required by applicable law or agreed to in writing, software
                  ~  *    distributed under the Licese is distributed on an "AS IS" BASIS,
                  ~  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                  ~  *    See the License for the specific language governing permissions and
                  ~  *    limitations under the License.
                  ~  */
                  ~
                  -->
                                
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                                
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.nd4j</groupId>
                    <artifactId>nd4j</artifactId>
                    <version> 0.5.0</version>
                                
                    <parent>
                        <groupId>org.sonatype.oss</groupId>
                        <artifactId>oss-parent</artifactId>
                        <version>7</version>
                    </parent>
                                
                    <distributionManagement>
                        <snapshotRepository>
                            <id>sonatype-nexus-snapshots</id>
                            <name>Sonatype Nexus snapshot repository</name>
                            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
                        </snapshotRepository>
                        <repository>
                            <id>nexus-releases</id>
                            <name>Nexus Release Repository</name>
                            <url>http://oss.sonatype.org/service/local/staging/deploy/maven2/</url>
                        </repository>
                    </distributionManagement>
                                
                    <profiles>
                                
                        <!-- If someone knows a better way to do this, please do let me know! -->
                        <profile>
                            <id>linux</id>
                            <activation>
                                <os><name>linux</name></os>
                            </activation>
                            <properties>
                                <os.name>linux</os.name>
                            </properties>
                        </profile>
                        <profile>
                            <id>macosx</id>
                            <activation>
                                <os><name>mac os x</name></os>
                            </activation>
                            <properties>
                                <os.name>macosx</os.name>
                            </properties>
                        </profile>
                        <profile>
                            <id>windows</id>
                            <activation>
                                <os><family>windows</family></os>
                            </activation>
                            <properties>
                                <os.name>windows</os.name>
                            </properties>
                        </profile>
                        <profile>
                            <id>i386</id>
                            <activation>
                                <os><arch>i386</arch></os>
                            </activation>
                            <properties>
                                <os.arch>x86_64</os.arch>
                            </properties>
                        </profile>
                        <profile>
                            <id>i486</id>
                            <activation>
                                <os><arch>i486</arch></os>
                            </activation>
                            <properties>
                                <os.arch>x86_64</os.arch>
                            </properties>
                        </profile>
                        <profile>
                            <id>i586</id>
                            <activation>
                                <os><arch>i586</arch></os>
                            </activation>
                            <properties>
                                <os.arch>x86_64</os.arch>
                            </properties>
                        </profile>
                        <profile>
                            <id>i686</id>
                            <activation>
                                <os><arch>i686</arch></os>
                            </activation>
                            <properties>
                                <os.arch>x86_64</os.arch>
                            </properties>
                        </profile>
                        <profile>
                            <id>x86</id>
                            <activation>
                                <os><arch>x86</arch></os>
                            </activation>
                            <properties>
                                <os.arch>x86_64</os.arch>
                            </properties>
                        </profile>
                        <profile>
                            <id>amd64</id>
                            <activation>
                                <os><arch>amd64</arch></os>
                            </activation>
                            <properties>
                                <os.arch>x86_64</os.arch>
                            </properties>
                        </profile>
                        <profile>
                            <id>x86-64</id>
                            <activation>
                                <os><arch>x86-64</arch></os>
                            </activation>
                            <properties>
                                <os.arch>x86_64</os.arch>
                            </properties>
                        </profile>
                                
                        <profile>
                            <id>release-sign-artifacts</id>
                            <activation>
                                <property>
                                    <name>performRelease</name>
                                    <value>true</value>
                                </property>
                            </activation>
                            <build>
                                <plugins>
                                    <plugin>
                                        <artifactId>maven-source-plugin</artifactId>
                                        <version>2.4</version>
                                        <executions>
                                            <execution>
                                                <id>attach-sources</id>
                                                <goals>
                                                    <goal>jar</goal>
                                                </goals>
                                            </execution>
                                        </executions>
                                    </plugin>
                                    <plugin>
                                        <artifactId>maven-javadoc-plugin</artifactId>
                                        <executions>
                                            <execution>
                                                <id>attach-javadocs</id>
                                                <goals>
                                                    <goal>jar</goal>
                                                </goals>
                                            </execution>
                                        </executions>
                                    </plugin>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-gpg-plugin</artifactId>
                                        <version>1.5</version>
                                        <configuration>
                                            <passphrase>${gpg.passphrase}</passphrase>
                                        </configuration>
                                        <executions>
                                            <execution>
                                                <id>sign-artifacts</id>
                                                <phase>verify</phase>
                                                <goals>
                                                    <goal>sign</goal>
                                                </goals>
                                            </execution>
                                        </executions>
                                    </plugin>
                                </plugins>
                            </build>
                        </profile>
                        <profile>
                            <id>release</id>
                            <build>
                                <plugins>
                                    <plugin>
                                        <artifactId>maven-source-plugin</artifactId>
                                        <executions>
                                            <execution>
                                                <id>attach-sources</id>
                                                <goals>
                                                    <goal>jar</goal>
                                                </goals>
                                            </execution>
                                        </executions>
                                    </plugin>
                                    <plugin>
                                        <artifactId>maven-javadoc-plugin</artifactId>
                                        <configuration>
                                            <additionalparam>-Xdoclint:none</additionalparam>
                                        </configuration>
                                        <executions>
                                            <execution>
                                                <id>attach-javadocs</id>
                                                <goals>
                                                    <goal>jar</goal>
                                                </goals>
                                            </execution>
                                        </executions>
                                    </plugin>
                                </plugins>
                            </build>
                        </profile>
                    </profiles>
                    <scm>
                        <connection>scm:git@github.com:deeplearning4j/nd4j.git</connection>
                        <developerConnection>scm:git:git@github.com:deeplearning4j/nd4j.git</developerConnection>
                        <url>git@github.com:deeplearning4j/nd4j.git</url>
                        <tag> nd4j-0.5.0</tag>
                    </scm>
                                
                                
                    <packaging>pom</packaging>
                                
                    <name>nd4j</name>
                    <url>http://nd4j.org/</url>
                    <modules>
                        <module>nd4j-jdbc</module>
                        <module>nd4j-instrumentation</module>
                        <module>nd4j-perf</module>
                        <module>nd4j-serde</module>
                        <module>nd4j-bytebuddy</module>
                        <module>nd4j-common</module>
                        <module>nd4j-buffer</module>
                        <module>nd4j-context</module>
                        <module>nd4j-backends</module>
                    </modules>
                                
                    <properties>
                        <jackson.version>2.5.1</jackson.version>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <junit.version>4.12</junit.version>
                        <slf4j.version>1.7.10</slf4j.version>
                        <logback.version>1.1.2</logback.version>
                        <javacpp.version>1.2.3</javacpp.version>
                        <process-classes.skip> false</process-classes.skip>  <!-- To skip native compilation phase: -Dprocess-classes.skip=true    -->
                        <javacpp.platform>${os.name}-${os.arch}</javacpp.platform> <!-- For Android: -Dplatform=android-arm                                        -->
                        <javacpp.platform.root />            <!--              -Dplatform.root=/path/to/android-ndk/                         -->
                        <javacpp.platform.compiler />    <!--              -Dplatform.compiler=/path/to/arm-linux-androideabi-g++        -->
                        <javacpp.platform.properties>${javacpp.platform}</javacpp.platform.properties>
                        <spark.version>1.5.2</spark.version>
                    </properties>
                                
                    <developers>
                        <developer>
                            <id>agibsonccc</id>
                            <name>Adam Gibson</name>
                            <email>adam@skymind.io</email>
                        </developer>
                    </developers>
                                
                    <licenses>
                        <license>
                            <name>Apache License, Version 2.0</name>
                            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
                            <distribution>repo</distribution>
                        </license>
                    </licenses>
                                
                                
                    <build>
                                
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-jar-plugin</artifactId>
                                <version>2.4</version>
                                <configuration>
                                    <includes>
                                        <include>**/*.so</include>
                                        <include>**/*.so.*</include>
                                        <include>META-INF/*</include>
                                        <include>META-INF/services/**</include>
                                        <include>org.*</include>
                                        <include>**/*.properties</include>
                                        <include>**/*.class</include>
                                        <include>**/*.dll</include>
                                        <include>**/*.dylib</include>
                                        <include>lib/*</include>
                                
                                    </includes>
                                </configuration>
                            </plugin>
                            <plugin>
                                <artifactId>maven-source-plugin</artifactId>
                                <version>2.4</version>
                                <executions>
                                    <execution>
                                        <id>attach-sources</id>
                                        <goals>
                                            <goal>jar</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                            <plugin>
                                <artifactId>maven-javadoc-plugin</artifactId>
                                <executions>
                                    <execution>
                                        <id>attach-javadocs</id>
                                        <goals>
                                            <goal>jar</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                            <plugin>
                                <artifactId>maven-deploy-plugin</artifactId>
                                <configuration>
                                    <skip>true</skip>
                                </configuration>
                            </plugin>
                            <plugin>
                                <groupId>org.sonatype.plugins</groupId>
                                <artifactId>nexus-staging-maven-plugin</artifactId>
                                <version>1.6.6</version>
                                <executions>
                                    <execution>
                                        <id>default-deploy</id>
                                        <phase>deploy</phase>
                                        <goals>
                                            <goal>deploy</goal>
                                        </goals>
                                    </execution>
                                </executions>
                                <extensions>true</extensions>
                                <configuration>
                                    <serverId>nexus-releases</serverId>
                                    <nexusUrl>https://oss.sonatype.org/</nexusUrl>
                                    <skipStagingRepositoryClose>true</skipStagingRepositoryClose>
                                </configuration>
                            </plugin>
                        </plugins>
                                
                        <pluginManagement>
                            <plugins>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-javadoc-plugin</artifactId>
                                    <version>2.10.1</version>
                                    <configuration>
                                        <additionalparam>-Xdoclint:none</additionalparam>
                                    </configuration>
                                    <executions>
                                        <execution>
                                            <id>attach-javadocs</id>
                                            <goals>
                                                <goal>jar</goal>
                                            </goals>
                                        </execution>
                                    </executions>
                                </plugin>
                                
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-source-plugin</artifactId>
                                    <executions>
                                        <execution>
                                            <id>attach-sources</id>
                                            <goals>
                                                <goal>jar</goal>
                                            </goals>
                                        </execution>
                                    </executions>
                                </plugin>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-release-plugin</artifactId>
                                    <version>2.5</version>
                                    <configuration>
                                        <mavenExecutorId>forked-path</mavenExecutorId>
                                        <!-- To deploy to an open staging repository: -Darguments=-DstagingRepositoryId=orgnd4j-xxxx -->
                                        <arguments>-Psonatype-oss-release -DskipTests ${arguments}</arguments>
                                        <localCheckout>true</localCheckout>
                                        <pushChanges>false</pushChanges>
                                    </configuration>
                                </plugin>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-gpg-plugin</artifactId>
                                    <configuration>
                                        <passphrase>${gpg.passphrase}</passphrase>
                                    </configuration>
                                    <executions>
                                        <execution>
                                            <id>sign-artifacts</id>
                                            <phase>verify</phase>
                                            <goals>
                                                <goal>sign</goal>
                                            </goals>
                                        </execution>
                                    </executions>
                                </plugin>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-compiler-plugin</artifactId>
                                    <version>3.1</version>
                                    <configuration>
                                        <source>1.7</source>
                                        <target>1.7</target>
                                    </configuration>
                                </plugin>
                            </plugins>
                        </pluginManagement>
                    </build>
                                
                                
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.slf4j</groupId>
                                <artifactId>slf4j-log4j12</artifactId>
                                <version>${slf4j.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>org.slf4j</groupId>
                                <artifactId>slf4j-api</artifactId>
                                <version>${slf4j.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>com.google.guava</groupId>
                                <artifactId>guava</artifactId>
                                <version>18.0</version>
                            </dependency>
                            <dependency>
                                <groupId>junit</groupId>
                                <artifactId>junit</artifactId>
                                <version>${junit.version}</version>
                                <scope>test</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                                
                </project>
                                
                """;

        var parsed = PomParser.parse(pom);
        /*
                            <groupId>org.nd4j</groupId>
                    <artifactId>nd4j</artifactId>
                    <version> 0.5.0</version>

                    <parent>
                        <groupId>org.sonatype.oss</groupId>
                        <artifactId>oss-parent</artifactId>
                        <version>7</version>
                    </parent>
         */

        assertEquals(
                parsed.groupId(),
                new PomGroupId.Declared("org.nd4j")
        );


        assertEquals(
                parsed.artifactId(),
                new PomArtifactId.Declared("nd4j")
        );

        assertEquals(
                parsed.version(),
                new PomVersion.Declared("0.5.0")
        );

        assertEquals(
                parsed.parent(),
                new PomParent.Declared(
                        new PomGroupId.Declared("org.sonatype.oss"),
                        new PomArtifactId.Declared("oss-parent"),
                        new PomVersion.Declared("7")
                )
        );

        /*
                            <properties>
                        <jackson.version>2.5.1</jackson.version>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <junit.version>4.12</junit.version>
                        <slf4j.version>1.7.10</slf4j.version>
                        <logback.version>1.1.2</logback.version>
                        <javacpp.version>1.2.3</javacpp.version>
                        <process-classes.skip> false</process-classes.skip>  <!-- To skip native compilation phase: -Dprocess-classes.skip=true    -->
                        <javacpp.platform>${os.name}-${os.arch}</javacpp.platform> <!-- For Android: -Dplatform=android-arm                                        -->
                        <javacpp.platform.root />            <!--              -Dplatform.root=/path/to/android-ndk/                         -->
                        <javacpp.platform.compiler />    <!--              -Dplatform.compiler=/path/to/arm-linux-androideabi-g++        -->
                        <javacpp.platform.properties>${javacpp.platform}</javacpp.platform.properties>
                        <spark.version>1.5.2</spark.version>
                    </properties>
         */

        assertEquals(
                List.of(
                        new PomProperty("jackson.version", "2.5.1"),
                        new PomProperty("project.build.sourceEncoding", "UTF-8"),
                        new PomProperty("junit.version", "4.12"),
                        new PomProperty("slf4j.version", "1.7.10"),
                        new PomProperty("logback.version", "1.1.2"),
                        new PomProperty("javacpp.version", "1.2.3"),
                        new PomProperty("process-classes.skip", "false"),
                        new PomProperty("javacpp.platform", "${os.name}-${os.arch}"),
                        new PomProperty("javacpp.platform.root", ""),
                        new PomProperty("javacpp.platform.compiler", ""),
                        new PomProperty("javacpp.platform.properties", "${javacpp.platform}"),
                        new PomProperty("spark.version", "1.5.2")
                ),
                parsed.properties()
        );

        assertEquals(
                List.of(),
                parsed.dependencies()
        );

        /*


                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.slf4j</groupId>
                                <artifactId>slf4j-log4j12</artifactId>
                                <version>${slf4j.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>org.slf4j</groupId>
                                <artifactId>slf4j-api</artifactId>
                                <version>${slf4j.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>com.google.guava</groupId>
                                <artifactId>guava</artifactId>
                                <version>18.0</version>
                            </dependency>
                            <dependency>
                                <groupId>junit</groupId>
                                <artifactId>junit</artifactId>
                                <version>${junit.version}</version>
                                <scope>test</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>

         */
        assertEquals(
                List.of(
                        new PomDependency(
                                new PomGroupId.Declared("org.slf4j"),
                                new PomArtifactId.Declared("slf4j-log4j12"),
                                new PomVersion.Declared("${slf4j.version}")
                        ),
                        new PomDependency(
                                new PomGroupId.Declared("org.slf4j"),
                                new PomArtifactId.Declared("slf4j-api"),
                                new PomVersion.Declared("${slf4j.version}")
                        ),
                        new PomDependency(
                                new PomGroupId.Declared("com.google.guava"),
                                new PomArtifactId.Declared("guava"),
                                new PomVersion.Declared("18.0")
                        ),
                        new PomDependency(
                                new PomGroupId.Declared("junit"),
                                new PomArtifactId.Declared("junit"),
                                new PomVersion.Declared("${junit.version}"),
                                Set.of(),
                                PomType.Undeclared.INSTANCE,
                                PomClassifier.Undeclared.INSTANCE,
                                PomOptionality.Undeclared.INSTANCE,
                                new PomScope.Declared("test")
                        )
                ),
                parsed.dependencyManagement()
        );

        System.out.println(parsed.profiles());
    }
}
