# Development Tools


## Atom Editor

The [Atom](https://atom.io/) editor is highly configurable. Clojure is a pretty good f
it for Venice, the Clojure syntax highlighting can be used for editing Venice files.

To map the Venice filetype to the Clojure language, use the file-types option in your 
config.json (via the Atom -> Config... menu). Specify a pattern to match for the key 
(in bash-like glob format) and the new scope name for the value.

```yaml
"*":
  "file-types":
    "*.venice": "source.clojure"
```

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/atom-editor.png">
