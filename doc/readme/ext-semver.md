# Semantic Versioning

See [Semantic Versioning](http://semver.org) on the WEB


```clojure
(do
   (load-module :semver)
   
   (semver/parse "1.2.3") 
   ; => {:major 1, :minor 2, :patch 3, :pre-release nil, :meta-data nil}

   (semver/parse "1.2.3-SNAPSHOT")
   ; => {:major 1, :minor 2, :patch 3, :pre-release "SNAPSHOT", :meta-data nil}

   (semver/parse "1.2.3-SNAPSHOT+build.143")
   ; => {:major 1, :minor 2, :patch 3, :pre-release "SNAPSHOT", :meta-data "build.143"}

   ; compare
   (semver/newer? "1.2.3" "1.1.0")
   (semver/newer? (semver/parse "1.2.3") "1.1.0")
   (semver/equal? "1.1.0" "1.1.0")
   (semver/equal? (semver/parse "1.1.0") "1.1.0")
   (semver/older? "1.1.0" "1.2.3")
   (semver/older? "1.1.0" (semver/parse "1.2.3")))
```

