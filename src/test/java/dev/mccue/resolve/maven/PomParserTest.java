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
}
