# Shell

The shell extension module offers OS agnostic functions for managing 
processes and opening files.

Starting with Java 9 Java opens the world to managing operating system 
processes in a standard way.

While _Venice_ is running seamlessly on Java 8+, Java 9+ is required
for managing processes.



## open

Opens a file or an url with the associated platform specific application.

```
(shell/open url)
```


## open-macos-app

Opens a Mac OSX app.

```
(shell/open-macos-app name)
```


## diff

Compare two files and print the differences.

```
(shell/diff file1 file2)
```


## alive?

Return `true` if the process represented by a PID or a process handle
is alive otherwise `false`.

_Note: Requires Java 9+_

```
(shell/alive? pid) 
```


## pid

Without argument returns the pid (type long) of cureent process. With
a process-handle (:java.lang.ProcessHandle) it returns the PID for the 
process represented by the handle.

_Note: Requires Java 9+_

```
(shell/pid)
```


## process-handle

Returns the process handle (:java.lang.ProcessHandle) for a PID or
nil if there is no process.

_Note: Requires Java 9+_

```
(shell/process-handle p)
```


## process-handle?

Returns true if p is a process handle (:java.lang.ProcessHandle).

_Note: Requires Java 9+_

```
(shell/process-handle? p)
```


## process-info

Returns the process info for a process represented by a PID or a 
process handle.

Returns for each process a map the keys ':arguments', ':command', 
':command-line', ':start-time', ':total-cpu-millis', and ':user'

_Note: Requires Java 9+_

```
(shell/process-info p)
```


## processes

Returns the process handle (:java.lang.ProcessHandle) for all
running processes.

_Note: Requires Java 9+_

```
(shell/processes)
```


## current-process

Returns the process handle of the current process.
        
_Note: Requires Java 9+_

```
(shell/current-process)
```


## descendant-processes

Returns the descendant processed of a process represented by a PID
or a process handle.
        
_Note: Requires Java 9+_

```
(shell/descendant-processes p)
```


## parent-process

Returns the parent of a process represented by a PID or a process
handle.

_Note: Requires Java 9+_

```
(shell/parent-process p)
```


## kill

Requests the process to be killed. Returns true if the process is 
killed and false if the process stays alive. Returns nil if the 
process does not exist. Accepts a process PID or a process handle 
(:java.lang.ProcessHandle).

_Note: Requires Java 9+_

```
(shell/kill pid)
```


## kill-forcibly

Requests the process to be killed forcibly. Returns true if the process 
is killed and false if the process stays alive. Returns nil if the 
process does not exist. Accepts a process PID or a process handle 
(:java.lang.ProcessHandle).
        
_Note: Requires Java 9+_

```
(shell/kill-forcibly pid)
```


## wait-for-process-exit

Waits until the process with the PID exits. Waits max timeout 
seconds. Returns nil if the process exits before reaching the 
timeout, else the PID is returned. Accepts a process PID or a 
process handle (:java.lang.ProcessHandle).

_Note: Requires Java 9+_

```
(shell/wait-for-process-exit pid timeout) 
```
