# Development Tools

## REPL

The [REPL](doc/readme/repl.md) is all you need to do your first steps with Venice.

For more ambitious work choose [VSCodium](#vscodium).


## VSCodium

[VSCodium](https://vscodium.com/) is a community-driven, fully open-source project derived from the VS Code source code.

5 reason to choose VSCodium:

    1. VSCodium is fully open-source
    2. VSCodium looks and works exactly like VS Code
    3. VSCodium doesn’t have proprietary extensions
    4. VSCodium doesn’t track your activity (disabled telemetry by default)
    5. VSCodium supports VS Code extensions
 
VSCodium uses [Open VSX Registry](https://open-vsx.org/) as its marketplace.


### Install & Upgrade

If you are on a Mac and have [Homebrew](https://brew.sh/) installed:

```
brew install --cask vscodium
```

```
brew upgrade --cask vscodium
```



### Tips to start with
   
**Adjust VSCodium "settings.json" to support Venice files**

You can open the *settings.json* file with the `Preferences: Open User Settings (JSON)` command in the Command Palette (Command+Shift+P) on MacOS or (Ctrl+Shift+P) on Windows. Once the file is open in an editor add these lines:

```json
{
    "files.associations": {
        "*.venice": "clojure"
    },
    
    "workbench.colorTheme": "Tomorrow Night Blue",
    
    "editor.insertSpaces": true,
    "editor.tabSize": 2,
    "editor.detectIndentation": false,
    "editor.rulers": [80, {"column": 100, "color": "#ffcc00"}]   
}
```

and save the file.

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/vscodium/vscodium.png">

**MacOS: Set VSCodium as the default app for ".venice" and ".json" files**

1. Select a Venice or Json file in the *Finder* and choose *File > Get Info*
2. In the *Open With* section select *VSCodium* and click *Change All* and confirm.


**Formatting**

You can format a JSON document using **⇧⌥F** or *"Format Document"* from the context menu.




