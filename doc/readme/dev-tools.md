# Development Tools

The [REPL](doc/readme/repl.md) and an editor with syntax highlighting is all
you need to start with Venice.


## Atom Editor

[Atom](https://atom.io/) is a friendly cross-platform Open Source text editor. With 
Clojure being a pretty good fit for Venice, the Clojure syntax highlighting can be 
used for editing Venice files.

To map the Venice filetype (.venice) to the Clojure language, use the file-types option in your 
config.json (via the Atom -> Config... menu). Specify a pattern to match for the key 
(in bash-like glob format) and the new scope name for the value.

```yaml
"*":
  "file-types":
    "*.venice": "source.clojure"
```

**Editing a Venice file and verifying parenthesis**

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/atom-editor.png">
