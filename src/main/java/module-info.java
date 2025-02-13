module dev.mccue.resolve {
    requires static org.jspecify;
    requires static dev.mccue.purl;

    requires java.xml;
    requires transitive java.net.http;

    exports dev.mccue.resolve;
    exports dev.mccue.resolve.maven;
}