# OpenAI Client

 

* [Installing OpenAI 3rd party libraries](#installing-openai-3rd-party-libraries)
* [How to get an OpenAI api or admin key](#how-to-get-an-openai-api-or-admin-key)
* [Configuring OpenAI api and admin keys](#configuring-openai-api-and-admin-keys)
* [Chat Completion](openai2-chat-completion.md)
* [Files](openai2-files.md)
* [Images](openai2-images.md)
* [Audio](openai2-audio.md)
* [API Costs](openai2-costs.md)
* [Models](openai2-models.md)

 
 

> [!NOTE]
> Venice' OpenAI module requires Venice 1.13.12+
>

 
 
 

## Installing OpenAI 3rd party libraries

*In the REPL run:*
```
(do
  (load-module :openai-java-install)
  (openai-java-install/install :dir (repl/libs-dir) :silent false))
```

and restart the REPL

 

*or install it to any other destination dir:*
```
(do
  (load-module :openai-java-install)
  (openai-java-install/install :dir /data/libs :silent false))
```

 
 

## How to get an OpenAI api or admin key

### Api key

Go to [platform.openai.com](https://platform.openai.com/api-keys) and log-in with your
OpenAI account (register for an account if you don't have one). Finally 
click on "Create new secret key". That's it.

### Admin key

Go to [platform.openai.com](https://platform.openai.com/settings/organization/admin-keys) 
and click on "Create new Admin key"

**Note:** An admin key is required only if you plan to use OpenAI admin functions, e.g. the *cost* functions.


### OpenAI credits

You need to create a payment method and add some credits. Navigate to 
[platform.openai.com](https://platform.openai.com/settings/organization/billing/overview):
1. Click on "Payment methods" to add a payment method
2. Click on "Add to credit balance" to top up your credit 
3. Click on "Credit Grants" to check your current credit grants

### OpenAI credit balance

You can check your credit balance at 
[platform.openai.com](https://platform.openai.com/settings/organization/billing/overview)

 
 

## Configuring OpenAI api and admin keys

**Note:** An admin key is required only if the OpenAI *cost* functions are called.

 

### Option 1

#### API key

Pass the OpenAI API key as an option `:openai-api-key "sk-proj-1234"` for creating
the OpenAI client:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client :openai-api-key "sk-proj-1234")]
    ;; ...
    (openai-java/close client)))
```

#### ADMIN key

Pass the OpenAI ADMIN key as an option `:openai-admin-key "sk-admin-1234"` for creating
the OpenAI client:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client :openai-admin-key "sk-admin-1234")]
    ;; ...
    (openai-java/close client)))
```

> [!NOTE]
> An api and an admin can be passed together to the client. The client knows which key
> to use and throws an exception if a key is missing.
>

 

### Option 2

Define an environment variable `OPENAI_API_KEY` and/or `OPENAI_ADMIN_KEY` at the OS Shell level:

On MacOS update the profile `~/.zprofile`

```
export OPENAI_API_KEY=sk-proj-1234
export OPENAI_ADMIN_KEY=sk-admin-1234
#export OPENAI_ORG_ID=
#export OPENAI_PROJECT_ID=
```

 

Create a client that reads the OpenAI API and/or ADMIN key from the env vars:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client)]
    ;; ...
    (openai-java/close client)))
```

> [!WARNING]
> This approach does not work for MacOS Apps (e.g.: Eclipse, IDEA) not started from a *Terminal* ⇒ see **Option 4**
>

 

### Option 3

Add the keys to the `repl.env` file in the REPL home directory. 

Linux/Mac OS:

```
# OpenAI api key
export OPENAI_API_KEY=sk-proj-1234
export OPENAI_ADMIN_KEY=sk-admin-1234
export OPENAI_ORG_ID=
export OPENAI_PROJECT_ID=
```

Windows:

```
REM OpenAI api key
set OPENAI_API_KEY=sk-proj-1234
set OPENAI_ADMIN_KEY=sk-admin-1234
set OPENAI_ORG_ID=
set OPENAI_PROJECT_ID=
```

 

Create a client that reads the OpenAI API and/or ADMIN key from the env vars:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client)]
    ;; ...
    (openai-java/close client)))
```

 

### Option 4 (MacOS, system wide environment vars)

The above solutions just work for *MacOS* Apps or programs that are started from a *Terminal*.

*Apps* started from the *Finder* or the *Desktop* do not source `.zprofile` and thus do not 
get the environment variables defined there.

To make *OPENAI_API_KEY* as an environment variable available to MacOS *Apps*, the `launchctl` 
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
      <string>sk-proj-1234</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
  </dict>
</plist>
```

Save this file in `~/Library/LaunchAgents/setenv.OPENAI.plist`! On a restart, `launchctl` 
will arrange this to be run for us and provide the *OPENAI_API_KEY* env var to all
Apps.

