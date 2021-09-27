# Development Tools


## Gitpod

### Start a Venice Gitpod workspace in your browser

Fire up a Venice a [Gitpod](https://gitpod.io/) workspace in your browser by clicking this button: [![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/jlangch/venice). It opens the URL _https://gitpod.io/#https://github.com/jlangch/venice_. Gitpod will ask you to login to GitHub and will launch a Venice workspace container for you in the cloud, containing a full Linux environment. It will also clone the Venice repository and build it. If you don't have a GitHub login yet, please sign up for [GitHub](https://github.com/).

[Gitpod](https://gitpod.io/) gives you a full development environment and a [REPL](doc/readme/repl.md) within your browser without needing to install anything on your local machine.

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/gitpod/gitpod-repl.png">


### Useful VSCode extensions

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/gitpod/gitpod-VsCodeExtensions.png" width="300">


### VSCode settings.json

```json
{
    "files.associations": {
        "*.venice": "clojure"
    },
    
    "workbench.colorTheme": "Tomorrow Night Blue",
    "redhat.telemetry.enabled": false    
}
```


## REPL with an Editor

The [REPL](doc/readme/repl.md) and an editor with syntax highlighting is all
you need to start with Venice.


### Atom Editor

[Atom](https://atom.io/) is a friendly cross-platform Open Source text editor. With 
Clojure being a pretty good fit for Venice, the Clojure syntax highlighting can be 
used for editing Venice files.

To map the Venice filetype (.venice) to the Clojure language, use the file-types option in your 
_config.json_ (via the Atom -> Config... menu). Specify a pattern to match for the key 
(in bash-like glob format) and the new scope name for the value.

```yaml
"*":
  "file-types":
    "*.venice": "source.clojure"
```

**Editing a Venice file and verifying parenthesis**

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/atom-editor.png" width="700">

