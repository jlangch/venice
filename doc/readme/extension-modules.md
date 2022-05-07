# Extension Modules

Through extension modules Venice provides specific functionality
that not all application require thus keeping load time and 
resource usage low if the modules are not used.

Extension Modules are plain Venice scripts and must be loaded 
explicitly `(load-module :name)`. Venice tracks the modules loaded
and loads a module only once and skips subsequent load attempts.

* [Kira Template Engine](ext-kira.md)
* [Shell](ext-shell.md)
* [Charts](ext-charts.md)
* [Configuration](ext-configuration.md)
* [Components](ext-components.md)
* [Hexdump](ext-hexdump.md)
* [Parsifal Parser Combinator](ext-parsifal.md)
* [GEO IP](ext-geoip.md)
* [Mercator Maps](ext-mercator.md)
* [CIDR (Classless Inter-Domain Routing)](ext-cidr.md)
* [Semantic Versioning](ext-semver.md)
* [XML](ext-xml.md)
* [Cryptographic Functions](ext-crypt.md)
* [Apache Tomcat WEB Server](ext-tomcat.md)
* [Ring WEB App library](ext-ring.md)
* [WebDAV](ext-webdav.md)
* [Maven](ext-maven.md)
* [Jackson JSON](ext-jackson.md)


### Explicitly forcing a module reload

Venice can be forced to reload an already loaded module

```clojure
(load-module :maven)

; use the module
(maven/download "org.knowm.xchart:xchart:3.5.4")
             
; reload the module
(ns-remove maven)
(load-module :maven true)
```

