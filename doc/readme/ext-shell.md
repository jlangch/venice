# Shell

The shell extension module offers OS agnostic functions for managing 
processes and opening files.

Starting with Java 9, Java opens the world to managing operating system 
processes in a standard way.

While *Venice* is running seamlessly on Java 8+, Java 9+ is required
for managing processes.



## open

Opens a file or an URL with the associated platform specific application.

```
(shell/open url)
```

Example:

```
(do
  (load-module :shell)
  (shell/open "image.png"))
```


## open-macos-app

Opens a Mac OSX app.

```
(shell/open-macos-app name)
```

Example:

```
(do
  (load-module :shell)
  (shell/open-macos-app "Firefox"))
```


## diff

Compare two files and print the differences.

```
(shell/diff file1 file2)
```


## alive?

Return *true* if the process represented by a PID or a process handle
is alive otherwise *false*.

*Note: Requires Java 9+*

```
(shell/alive? p) 
```


## pid

Without argument returns the PID (type long) of current process. With
a process-handle (:java.lang.ProcessHandle) it returns the PID for the 
process represented by the handle.

*Note: Requires Java 9+*

```
(shell/pid)
```


## process-handle

Returns the process handle (:java.lang.ProcessHandle) for a PID or
*nil* if there is no process associated with the PID.

*Note: Requires Java 9+*

```
(shell/process-handle p)
```


## process-handle?

Returns *true* if p is a process handle (:java.lang.ProcessHandle).

*Note: Requires Java 9+*

```
(shell/process-handle? p)
```


## process-info

Returns the process info for a process represented by a PID or a 
process handle.

The process info is a map with the keys :pid, :alive, 
:arguments, :command, :command-line, :start-time, :total-cpu-millis, 
and :user

*Note: Requires Java 9+*

```
(shell/process-info p)
```


## processes

Returns a snapshot of all processes visible to the current process.
Returns a list of :java.lang.ProcessHandle for the processes.

*Note: Requires Java 9+*

```
(shell/processes)
```

Example:

```
;; find the PID of the ArangoDB process
;; like: pgrep -lf ArangoDB3 | cut -d ' ' -f 1
(->> (shell/processes)
     (map shell/process-info)
     (filter #(str/contains? (:command-line %) "ArangoDB3"))
     (map :pid))
```


## processes-info

Returns a snapshot of all processes visible to the current process.
Returns a list of process infos for the processes.

The process info is a map with the keys :pid, :alive, 
:arguments, :command, :command-line, :start-time, :total-cpu-millis, 
and :user

*Note: Requires Java 9+*

```
(shell/processes-info)
```

Example:

```
;; find the PID of the ArangoDB process
;; like: pgrep -lf ArangoDB3 | cut -d ' ' -f 1
(->> (shell/processes-info)
     (filter #(str/contains? (:command-line %) "ArangoDB3"))
     (map :pid))
```


## current-process

Returns the process handle of the current process.
        
*Note: Requires Java 9+*

```
(shell/current-process)
```


## descendant-processes

Returns the descendants (list of :java.lang.ProcessHandle) of a process
represented by a PID or a process handle.

*Note: Requires Java 9+*

```
(shell/descendant-processes p)
```

Example:

```
(->> (shell/current-process)
     (shell/descendant-processes)
     (map shell/process-info))
```


## parent-process

Returns the parent (:java.lang.ProcessHandle) of a process represented 
by a PID or a process handle.

*Note: Requires Java 9+*

```
(shell/parent-process p)
```

Example:

```
(->> (shell/current-process)
     (shell/parent-process)
     (shell/process-info))
 ```


## kill

Requests the process to be killed. Returns *true* if the process is 
killed and *false* if the process stays alive. Returns *nil* if the 
process does not exist. Accepts a PID or a process handle 
(:java.lang.ProcessHandle).

*Note: Requires Java 9+*

```
(shell/kill pid)
```


## kill-forcibly

Requests the process to be killed forcibly. Returns *true* if the process 
is killed and *false* if the process stays alive. Returns *nil* if the 
process does not exist. Accepts a PID or a process handle 
(:java.lang.ProcessHandle).
        
*Note: Requires Java 9+*

```
(shell/kill-forcibly pid)
```


## wait-for-process-exit

Waits until the process with the PID exits. Waits max timeout 
seconds. Returns *nil* if the process exits before reaching the 
timeout, else the PID is returned. Accepts a PID or a 
process handle (:java.lang.ProcessHandle).

*Note: Requires Java 9+*

```
(shell/wait-for-process-exit pid timeout) 
```
