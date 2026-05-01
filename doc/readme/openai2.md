# OpenAI Client

 

* [Installing OpenAI 3rd party libraries](#installing-openai-3rd-party-libraries)
* [Configuring OpenAI API keys](#configuring-openai-api-keys)
* [Chat Completion](openai2-chat-completion.md)
* [Functions](openai2-functions.md)

 

> [!NOTE]
> Venice' OpenAI module requires Venice 1.13.1+
>

 
 
 

### Installing OpenAI 3rd party libraries

*In the REPL run:*
```
(do
  (load-module :openai-java-install)
  (openai-java-install/install :dir (repl/libs-dir) :silent false))
```

and restart the REPL

 

*or install ot to any other destination dir:*
```
(do
  (load-module :openai-java-install)
  (openai-java-install/install :dir /data/libs :silent false))
```

 
 

### Configuring OpenAI API keys


**Option 1**

Pass the OpenAI API key as an option `:openai-api-key "sk-123456789"` for creating
the OpenAI client:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client :openai-api-key "sk-123456789")]
    ;; ...
    (openai-java/close client)))
```

 

**Option 2**

Define an environment variable at the OS or Shell level:

```
export OPENAI_API_KEY=sk-123456789
# export OPENAI_ORG_ID=
# export OPENAI_PROJECT_ID=
```

Create a client that reads the OpenAI API key from the env var:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client)]
    ;; ...
    (openai-java/close client)))
```

 

**Option 3**

Add the key to the `repl.env` file in the REPL home directory. 

Linux/Mac OS:

```
# OpenAI api key
export OPENAI_API_KEY=sk-123456789
export OPENAI_ORG_ID=
export OPENAI_PROJECT_ID=
```


Windows:

```
REM OpenAI api key
set OPENAI_API_KEY=sk-123456789
set OPENAI_ORG_ID=
set OPENAI_PROJECT_ID=
```

 

Create a client that reads the OpenAI API key from the env var:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client)]
    ;; ...
    (openai-java/close client)))
```


 

**Option 4 (Mac OS, system wide env vars)**

The above solutions just work for *Apps* or programs that are started from a *Terminal*.

*Apps* started from the Finder or the Desktop do not source `.zprofile` and thus do not 
get the environment variables defined there.

To make *OPENAI_API_KEY* as an environment variable available to Apps, the `launchctl` 
program in MacOS allows us to programmatically interact with `launchd`, a 
“System wide and per-user daemon/agent manager” and define environment variables.

We do this by putting `plist` files in the correct places to make things happen. The 
place we want for this task is `~/Library/LaunchAgents`, and the file is pretty simple!

``` xml
<!-- ~/Library/LaunchAgents/setenv.OPENAI.plist -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
  <plist version="1.0">
  <dict>
  <key>Label</key>
  <string>setenv.OPENAI_API_KEY</string>
  <key>ProgramArguments</key>
  <array>
    <string>/bin/launchctl</string>
    <string>setenv</string>
    <string>OPENAI_API_KEY</string>
    <string>sk-12345678</string>
  </array>
  <key>RunAtLoad</key>
  <true/>
</dict>
</plist>
```

Save this file in `~/Library/LaunchAgents/setenv.OPENAI.plist`! On a restart, `launchctl` 
will arrange this to be run for us and OPENAI_API_KEY provide the OpenAI API Key to all
Apps.
