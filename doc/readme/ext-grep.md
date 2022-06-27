# GREP

`grep` is a utility for searching plain-text data sets for lines that match a regular expression. 



## Search in text files

### grep

Search for lines that match a regular expression in text files. The search starts from a base
directory and chooses all files that match a globbing pattern.

```
(grep/grep base-dir file-glob line-pattern)
```

Example:

Print the `grep` matches in a human readable form, one line per match in the format "{{filename}}\:{{lineno}}\:{{line}}":

```clojure
(do
  (load-module :grep ['grep :as 'g])
  
  (g/grep "/Users/foo/logs" "**/*.log" ".*Shutdown.*"))
```

Print the `grep` matches in a machine readable form. It returns a list of tuples holding _filename_, _lineno_, and _line_:

```clojure
(do
  (load-module :grep ['grep :as 'g])
  
  (g/grep "/Users/foo/logs" "**/*.log" ".*Shutdown.*" :print false))
```


## Search in ZIP files

### grep-zip

Search for lines that match a regular expression in text files within a ZIP file. The search 
chooses all files in the ZIP that match a globbing pattern.

```
(grep/grep-zip dir zipfile-glob file-glob line-pattern)
```

Example:

Print the `grep` matches in a human readable form, one line per match in the format "{{zipname}}!{{filename}}:{{lineno}}:{{line}}":


```clojure
(do
  (load-module :grep ['grep :as 'g])
  
  (g/grep-zip "/Users/foo/logs/" "logs*.zip" "**/*.log" ".*Shutdown.*"))
```

Print the `grep` matches in a machine readable form. It returns a list of tuples holding _zipname_, _filename_, _lineno_, and _line_:


```clojure
(do
  (load-module :grep ['grep :as 'g])
  
  (g/grep-zip "/Users/foo/logs/" "logs*.zip" "**/*.log" ".*Shutdown.*" :print false))
```
