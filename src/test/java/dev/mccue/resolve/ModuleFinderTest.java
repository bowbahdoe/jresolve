package dev.mccue.resolve;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleFinderTest {
    @Test
    public void testGettingDepDynamically() throws IOException, ClassNotFoundException {
        var tempDir = Files.createTempDirectory("temp");
        var result = new Resolve()
                .withCache(Cache.standard(tempDir))
                .addDependency(Dependency.mavenCentral("dev.mccue:json:0.2.3"))
                .fetch()
                .run();

        var finder = result.moduleFinder();

        ModuleLayer parent = ModuleLayer.boot();

        Configuration cf = parent.configuration()
                .resolve(finder, ModuleFinder.of(), Set.of("dev.mccue.json"));

        ClassLoader scl = ClassLoader.getSystemClassLoader();

        ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);

        Class<?> c = layer.findLoader("dev.mccue.json")
                .loadClass("dev.mccue.json.Json");

        assertEquals(c.getName(), "dev.mccue.json.Json");
        assertTrue(c.isInterface());
    }

    @Test
    public void testGettingDepDynamically2() throws IOException, ClassNotFoundException {
        var tempDir = Files.createTempDirectory("temp");
        var result = new Resolve()
                .withCache(Cache.standard(tempDir))
                .addDependency(Dependency.mavenCentral("com.fasterxml.jackson.core:jackson-core:2.15.0"))
                .fetch()
                .run();

        var finder = result.moduleFinder();

        ModuleLayer parent = ModuleLayer.boot();

        Configuration cf = parent.configuration()
                .resolve(finder, ModuleFinder.of(), Set.of("com.fasterxml.jackson.core"));

        ClassLoader scl = ClassLoader.getSystemClassLoader();

        ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);

        Class<?> c = layer.findLoader("com.fasterxml.jackson.core")
                .loadClass("com.fasterxml.jackson.core.JsonParser");

        assertEquals(c.getName(), "com.fasterxml.jackson.core.JsonParser");
    }

    @Test
    public void testResolvingDependentModules() throws IOException, ClassNotFoundException {
        /*
        com.fasterxml.jackson.core/jackson-databind 2.15.0
          . com.fasterxml.jackson.core/jackson-annotations 2.15.0
          . com.fasterxml.jackson.core/jackson-core 2.15.0
         */
        var tempDir = Files.createTempDirectory("temp");
        var resolution = new Resolve()
                .withCache(Cache.standard(tempDir))
                .addDependency(Dependency.mavenCentral("com.fasterxml.jackson.core:jackson-databind:2.15.0"))
                .run();

        var result = new Fetch(resolution)
                .withCache(Cache.standard(tempDir))
                .run();

        var finder = result
                .moduleFinder();

        ModuleLayer parent = ModuleLayer.boot();

        Configuration cf = parent.configuration()
                .resolve(finder, ModuleFinder.of(), Set.of("com.fasterxml.jackson.databind"));

        ClassLoader scl = ClassLoader.getSystemClassLoader();

        ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);

        Class<?> c = layer.findLoader("com.fasterxml.jackson.core")
                .loadClass("com.fasterxml.jackson.core.JsonParser");

        assertEquals(c.getName(), "com.fasterxml.jackson.core.JsonParser");

        Class<?> c2 = layer.findLoader("com.fasterxml.jackson.databind")
                .loadClass("com.fasterxml.jackson.databind.JsonDeserializer");

        assertEquals(c2.getName(),"com.fasterxml.jackson.databind.JsonDeserializer");

        Class<?> c3 = layer.findLoader("com.fasterxml.jackson.annotation")
                .loadClass("com.fasterxml.jackson.annotation.JsonGetter");

        assertEquals(c3.getName(), "com.fasterxml.jackson.annotation.JsonGetter");
    }
}
