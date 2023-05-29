package dev.mccue.resolve.maven;

import dev.mccue.resolve.doc.Maven;

import java.io.InputStream;
import java.util.List;

@Maven("https://github.com/apache/maven-resolver/blob/466f419fc80734252591a34f29a2fc500de8bff2/maven-resolver-spi/src/main/java/org/eclipse/aether/spi/connector/transport/Transporter.java")
public interface Transport {
    List<String> cachePrefix();

    sealed interface GetFileResult {
        record Success(InputStream inputStream)
                implements GetFileResult {}
        record NotFound()
                implements GetFileResult {}
        record Error(Throwable throwable)
                implements GetFileResult  {}
    }

    GetFileResult getFile(List<String> pathElements);
}
