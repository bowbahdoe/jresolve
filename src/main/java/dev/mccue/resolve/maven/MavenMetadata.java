package dev.mccue.resolve.maven;

import dev.mccue.resolve.Artifact;
import dev.mccue.resolve.Group;
import dev.mccue.resolve.Version;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

record MavenMetadata(
        Group group,
        Artifact artifact,
        Version latest,
        Version release,
        List<Version> versions,
        LocalDateTime lastUpdated
) {
    public MavenMetadata(
            Group group,
            Artifact artifact,
            Version latest,
            Version release,
            List<Version> versions,
            LocalDateTime lastUpdated
    ) {
        this.group = Objects.requireNonNull(group);
        this.artifact = Objects.requireNonNull(artifact);
        this.latest = Objects.requireNonNull(latest);
        this.release = Objects.requireNonNull(release);
        this.versions = List.copyOf(versions);
        this.lastUpdated = Objects.requireNonNull(lastUpdated);
    }

    static MavenMetadata parseXml(String content) {
        var handler = new DefaultHandler() {
            Group group;
            Artifact artifact;
            Version latest;
            Version release;
            final List<Version> versions = new ArrayList<>();
            LocalDateTime lastUpdated;

            final StringBuilder characterBuffer = new StringBuilder();
            Consumer<String> next = (__) -> {};
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                switch (qName) {
                    case "groupId" -> next = groupId ->
                            this.group = new Group(groupId);
                    case "artifactId" -> next = artifactId ->
                            this.artifact = new Artifact(artifactId);
                    case "latest" -> next = latest ->
                            this.latest = new Version(latest);
                    case "release" -> next = release ->
                            this.release = new Version(release);
                    case "version" -> next = version ->
                            this.versions.add(new Version(version));
                    case "lastUpdated" -> next = lastUpdated ->
                            this.lastUpdated = LocalDateTime.parse(
                                    lastUpdated,
                                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                            );
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                next.accept(characterBuffer.toString().trim());
                characterBuffer.setLength(0);
                next = (__) -> {};
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                characterBuffer.append(ch, start, length);
            }
        };

        var factory = SAXParserFactory.newDefaultInstance();
        try {
            var saxParser = factory.newSAXParser();
            saxParser.parse(
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                    handler
            );
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return new MavenMetadata(
                handler.group,
                handler.artifact,
                handler.latest,
                handler.release,
                handler.versions,
                handler.lastUpdated
        );
    }

    Optional<Version> resolveVersionRange(VersionRange range) {
        return this.versions.stream()
                .sorted(Comparator.reverseOrder())
                .filter(range::includes)
                .findFirst();

    }
}
