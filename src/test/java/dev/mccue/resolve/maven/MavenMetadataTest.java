package dev.mccue.resolve.maven;

import dev.mccue.resolve.Artifact;
import dev.mccue.resolve.Group;
import dev.mccue.resolve.Version;
import dev.mccue.resolve.VersionRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MavenMetadataTest {
    @Test
    public void testMetadataParsing() {

        assertEquals(
                new MavenMetadata(
                        new Group("dev.mccue"),
                        new Artifact("json"),
                        new Version("0.2.3"),
                        new Version("0.2.3"),
                        List.of(
                                new Version("0.0.1"),
                                new Version("0.0.2"),
                                new Version("0.0.3"),
                                new Version("0.0.4"),
                                new Version("0.0.5"),
                                new Version("0.0.6"),
                                new Version("0.0.7"),
                                new Version("0.0.8"),
                                new Version("0.0.9"),
                                new Version("0.1.0"),
                                new Version("0.1.1"),
                                new Version("0.1.2"),
                                new Version("0.1.3"),
                                new Version("0.2.0"),
                                new Version("0.2.1"),
                                new Version("0.2.2"),
                                new Version("0.2.3")
                        ),
                        LocalDateTime.of(2023, 3,2,1, 47,10)
                ),
                MavenMetadata.parseXml("""
                
                <metadata>
                <groupId>dev.mccue</groupId>
                <artifactId>json</artifactId>
                <versioning>
                <latest>0.2.3</latest>
                <release>0.2.3</release>
                <versions>
                <version>0.0.1</version>
                <version>0.0.2</version>
                <version>0.0.3</version>
                <version>0.0.4</version>
                <version>0.0.5</version>
                <version>0.0.6</version>
                <version>0.0.7</version>
                <version>0.0.8</version>
                <version>0.0.9</version>
                <version>0.1.0</version>
                <version>0.1.1</version>
                <version>0.1.2</version>
                <version>0.1.3</version>
                <version>0.2.0</version>
                <version>0.2.1</version>
                <version>0.2.2</version>
                <version>0.2.3</version>
                </versions>
                <lastUpdated>20230302014710</lastUpdated>
                </versioning>
                </metadata>
                """));
    }

    @Test
    public void testVersionResolution() {
        var metadata = MavenMetadata.parseXml("""
                
                <metadata>
                <groupId>dev.mccue</groupId>
                <artifactId>json</artifactId>
                <versioning>
                <latest>0.2.3</latest>
                <release>0.2.3</release>
                <versions>
                <version>0.0.1</version>
                <version>0.0.2</version>
                <version>0.0.3</version>
                <version>0.0.4</version>
                <version>0.0.5</version>
                <version>0.0.6</version>
                <version>0.0.7</version>
                <version>0.0.8</version>
                <version>0.0.9</version>
                <version>0.1.0</version>
                <version>0.1.1</version>
                <version>0.1.2</version>
                <version>0.1.3</version>
                <version>0.2.0</version>
                <version>0.2.1</version>
                <version>0.2.2</version>
                <version>0.2.3</version>
                </versions>
                <lastUpdated>20230302014710</lastUpdated>
                </versioning>
                </metadata>
                """);

        assertEquals(
                new Version("0.0.9"),
                metadata.resolveVersionRange(new VersionRange(
                        new Version("0.0.8"),
                        new Version("0.0.9"),
                        true,
                        true
                )).orElseThrow()
        );
        assertEquals(
                new Version("0.0.8"),
                metadata.resolveVersionRange(new VersionRange(
                        new Version("0.0.8"),
                        new Version("0.0.9"),
                        true,
                        false
                )).orElseThrow()
        );
        assertNull(metadata.resolveVersionRange(new VersionRange(
                new Version("0.0.8"),
                new Version("0.0.9"),
                false,
                false
        )).orElse(null));
        assertEquals(
                new Version("0.0.9"),
                metadata.resolveVersionRange(new VersionRange(
                        new Version("0.0.8.4"),
                        new Version("0.0.9.6"),
                        false,
                        false
                )).orElse(null)
        );

    }

    @Test
    public void testUnboundedJustNewest() {
        var metadata = MavenMetadata.parseXml("""
                
                <metadata>
                <groupId>dev.mccue</groupId>
                <artifactId>json</artifactId>
                <versioning>
                <latest>0.2.3</latest>
                <release>0.2.3</release>
                <versions>
                <version>0.0.1</version>
                <version>0.0.2</version>
                <version>0.0.3</version>
                <version>0.0.4</version>
                <version>0.0.5</version>
                <version>0.0.6</version>
                <version>0.0.7</version>
                <version>0.0.8</version>
                <version>0.0.9</version>
                <version>0.1.0</version>
                <version>0.1.1</version>
                <version>0.1.2</version>
                <version>0.1.3</version>
                <version>0.2.0</version>
                <version>0.2.1</version>
                <version>0.2.2</version>
                <version>0.2.3</version>
                </versions>
                <lastUpdated>20230302014710</lastUpdated>
                </versioning>
                </metadata>
                """);
        assertEquals(
                new Version("0.2.3"),
                metadata.resolveVersionRange(new VersionRange(
                        null,
                        null,
                        false,
                        false
                )).orElse(null)
        );
    }

}

