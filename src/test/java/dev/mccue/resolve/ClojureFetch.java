package dev.mccue.resolve;

import dev.mccue.resolve.maven.MavenRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

public class ClojureFetch {
    @Test
    public void aaa(){
        var libraries = new Fetch()
                .addDependency(Dependency.maven(
                        "ring:ring:1.10.0",
                        List.of(
                                MavenRepository.remote("https://repo.clojars.org"),
                                MavenRepository.central()
                        )
                ))
                .withCache(Cache.standard(Path.of("./local")))
                .run()
                .libraries();

        libraries.forEach((library, path) ->
                System.out.println(library + " = " + path)
        );
    }
}


