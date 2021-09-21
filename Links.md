# Links


## Release to Maven

[OSSRH Guide](https://central.sonatype.org/pages/ossrh-guide.html)

[OSSRH Manual Deploy](https://central.sonatype.org/pages/manual-staging-bundle-creation-and-deployment.html)

[Deploying to Sonatype OSSRH using Gradle](https://central.sonatype.org/pages/gradle.html)

[Releasing an artifact in Sonatype OSSRH](https://central.sonatype.org/pages/releasing-the-deployment.html)


## Build

[Verifying Gradle Wrappers with GitHub Actions](https://blog.gradle.org/gradle-wrapper-checksum-verification-github-action)

[My task… what’s wrong with your Gradle task?](https://blog.softwaremill.com/my-task-whats-wrong-with-your-gradle-task-82312100c595)


## HTML Validator

[HTML Validator](https://validator.w3.org/#validate_by_upload)


### BFG Repo-Cleaner

[BFG Repo-Cleaner](https://rtyley.github.io/bfg-repo-cleaner/)

```sh
$ cd /Users/foo/venice/
$ java -jar {path-to-bfg}/bfg-1.14.0.jar  --delete-files my-file.txt
```


### Gitpod

[Gitpodifying — The Ultimate Guide](https://www.gitpod.io/blog/gitpodify/)


### Java 9 Modules

[Java 8 with module info (1)](https://dzone.com/articles/building-java-6-8-libraries-for-jpms-in-gradle)

[Java 8 with module info (2)](https://stackoverflow.com/questions/55100737/compile-a-jdk-8-project-a-jdk-9-module-info-java-in-gradle)

[Gradle multi-release JARs](https://blog.gradle.org/mrjars)

[Reflection](https://www.sitepoint.com/reflection-vs-encapsulation-in-the-java-module-system/)

[Gradle Modules Plugin](https://github.com/java9-modularity/gradle-modules-plugin)

```groovy
test {
    moduleOptions {
        runOnClasspath = true
    }
}
```

## Performance

[Clojure / Java Performance](http://www.diva-portal.org/smash/get/diva2:1424342/FULLTEXT01.pdf)


## Debugger

[The Debuggable Interpreter Design Pattern](http://www.bergel.eu/download/papers/Berg07d-debugger.pdf)

[Multi-level Debugging for Interpreter Developers](http://abstraktor.github.io/images/posts/20160223%20Multi-level%20Debugging%20for%20Interpreter%20Developers%20AuthorsVersion.pdf)


## Class Loading

[Dynamic Class Reloading](http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html)

[Class Loader](https://medium.com/@isuru89/java-a-child-first-class-loader-cbd9c3d0305)


## Java Tools

[Java VisualVM](https://visualvm.github.io/download.html)

[Zulu Mission Control](https://www.azul.com/products/zulu-mission-control/)


## Clojure


### Socket REPL

[Socket REPL](https://clojure.org/reference/repl_and_main)

[Socket REPL explore](https://oli.me.uk/exploring-repl-tooling-with-prepl/)


### Configurator

[clj-configurator](https://github.com/unrelentingtech/clj-configurator)


### Debugger

[Cursive Debugger](https://www.youtube.com/watch?v=ql77RwhcCK0)


### STM

[Clojure STM](https://clojure.org/reference/refs)

[STM Implementation](https://soft.vub.ac.be/~tvcutsem/talks/presentations/STM-in-Clojure.pdf)

[STM Implementation](https://github.com/tvcutsem/stm-in-clojure)

[STM Blog](https://sw1nn.com/blog/2012/04/11/clojure-stm-what-why-how/)

[Multiverse](https://github.com/pveentjer/Multiverse)

[Multiverse Examples](https://javacreed.com/software-transactional-memory-example-using-multiverse)

[ByteSTM](http://www.hydravm.org/hydra/chrome/site/pub/ByteSTM_tech.pdf)


## GitHub Wiki

[Wiki content](https://docs.github.com/en/free-pro-team@latest/github/building-a-strong-community/editing-wiki-content)

[Add images](http://mikehadlow.blogspot.com/2014/03/how-to-add-images-to-github-wiki.html?m=1)

[Add images to the Wiki](https://github.com/RWTH-EBC/AixLib/wiki/How-to:-Add-images-to-the-Wiki)


## /etc

[Marty Lobdell - Study Less Study Smart](https://www.youtube.com/watch?v=IlU-zDU6aQ0)

[Font](https://fonts.google.com/specimen/Audiowide)

[Dali SVG](https://github.com/stathissideris/dali)
