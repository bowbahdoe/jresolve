module dev.mccue.resolve {
    requires static org.jspecify;
    requires dev.mccue.purl;
    requires dev.mccue.guava.graph;

    requires java.xml;
    requires transitive java.net.http;

    exports dev.mccue.resolve;
    exports dev.mccue.resolve.maven;
}