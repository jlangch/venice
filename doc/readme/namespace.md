# Namespaces

A namespace is a set of symbols that are used to organize vars, so that these vars may be referred to by name. A namespace ensures that all the symbols within it have unique names.


### Namespaces are defined using the `ns` function

```clojure
(do
   (ns A)

   (def s1 1)
   (def s2 s1)
   (defn f1 [x] (+ x s1 s2))
   (defn f2 [x] (+ x (f1 x)))

   (ns B)

   (println *ns*)        ; => B
   (println A/s2)        ; => 1
   (println (A/f1 100))  ; => 102
   (println (A/f2 100))  ; => 202
)
```

### Namespaces can be reopened

```clojure
(do
   (ns A)
   (def s1 100)
   
   (ns B)
   (def s1 300)
 
   (ns A)
   (def s2 200)
   
   (ns B)

   (println A/s1)        ; => 100
   (println A/s2)        ; => 200
)
```

### Java imports are relative to a namespace

```clojure
(do
  (ns A)
  (import :java.lang.Math)
  (. :Math :max 2 10) ; => ok

  (ns B)
  (. :Math :max 2 10) ; => error

  (ns A)
  (. :Math :max 2 10) ; => ok
)
```

### Namespace aliases

```clojure
(do
  (ns AAAAAAAAAAAAAA)
  (def x 100)
  
  (ns-alias 'a 'AAAAAAAAAAAAAA)
  (println a/x))
```

```clojure
(do
  (ns AAAAAAAAAAAAAA)
  (def x 100)
 
  (ns B)
  (ns-alias 'a 'AAAAAAAAAAAAAA)
  (println a/x))
```

