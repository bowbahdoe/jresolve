# jresolve

## Differences from other resolvers

* Open `Coordinate` system

[maven-resolver](https://github.com/apache/maven-resolver) is understandably very maven focused.
Dependencies have to come from maven repositories, resolution is affected by global 
configuration like `settings.xml`

This is also true for [Shrinkwrap Resolver](https://github.com/shrinkwrap/resolver) which is built on
[maven-resolver](https://github.com/apache/maven-resolver). 

Coursier takes configuration from a different place and supports [ivy](https://ant.apache.org/ivy/) repositories in addition to maven
style ones, 

* Slightly better resolution algorithm



## Inspiration

I tried my best to document where different concepts and parts of the code came from,
but the most notable influences have been from [Coursier](https://get-coursier.io/)
and [tools.deps](https://clojure.org/guides/deps_and_cli).


## Rough Edges


* Maven Profiles are not yet supported.
* Snapshot repositories are not yet supported.
* Version ranges are not yet supported.
* File checksums are not yet checked
* There is some information about resolved dependency trees that isn't externally introspectable.
* No built-in mechanism to track downloads of artifacts
* `MavenCoordinate` is the only `Coordinate` implementation provided
* The global cache is likely not in its most ideal state
* No CLI is provided, only a programmatic API
* No support for `Fetch` without using a cache
* Performance has not been profiled or optimized

## Example Usage

### Resolve Dependencies

```java
var resolution = new Resolve()
        .addDependency(Dependency.mavenCentral("com.fasterxml.jackson.core:jackson-databind:2.15.2"))
        .run();

resolution.printTree();
```

```
com.fasterxml.jackson.core/jackson-databind 2.15.2
  . com.fasterxml.jackson.core/jackson-annotations 2.15.2
  . com.fasterxml.jackson.core/jackson-core 2.15.2
```

### Fetch Dependencies

```java
var resolution = new Resolve()
        .addDependency(Dependency.mavenCentral("com.fasterxml.jackson.core:jackson-databind:2.15.2"))
        .run();
        
var fetchResult = new Fetch(resolution)
        .run();

System.out.println(fetchResult.path());
```

```
/Users/emccue/.jresolve/https/repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar:/Users/emccue/.jresolve/https/repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar:/Users/emccue/.jresolve/https/repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar
```

### Use Fetched Dependencies to start a JVM

```java
var fetchResult = new Resolve()
        .addDependency(Dependency.mavenCentral("com.fasterxml.jackson.core:jackson-databind:2.15.2"))
        .fetch()
        .run();

int exitStatus = new ProcessBuilder(List.of(
    "jshell", "--class-path", fetchResult.path()        
))
        .inheritIO()
        .start()
        .waitFor()
```

```java
|  Welcome to JShell -- Version 17.0.4.1
|  For an introduction type: /help intro

jshell> import com.fasterxml.jackson.databind.ObjectMapper;

jshell> var mapper = new ObjectMapper();
mapper ==> com.fasterxml.jackson.databind.ObjectMapper@59ec2012
```

### Resolve Dependencies with exclusions

