# Semantic Versioning

[See Semantic Versioning](http://semver.org)


```clojure
(do
   (load-module :semver)
   
   (semver/parse "1.2.3") 
   ; {:major 1, :minor 2, :patch 3, :pre-release nil, :meta-data nil}

   (semver/parse "1.2.3-SNAPSHOT")
   ; {:major 1, :minor 2, :patch 3, :pre-release "SNAPSHOT", :meta-data nil}

   (semver/parse "1.2.3-SNAPSHOT+build.143")
   ; {:major 1, :minor 2, :patch 3, :pre-release "SNAPSHOT", :meta-data "build.143"}
```

