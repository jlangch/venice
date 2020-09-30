# Shell

... work in progress ...



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

Return `true` if the process represented by a pid or a process handle
is alive otherwise `false`.

```
(shell/alive? pid) 
```


## pid

Without argument returns the pid (type long) of cureent process. With
a process-handle (:java.lang.ProcessHandle) it returns the pid for the 
process represented by the handle.

```
(shell/pid)
```


## process-handle

Returns the process handle (:java.lang.ProcessHandle) for a pid or
nil if there is no process.

```
(shell/process-handle p)
```


## process-handle?

Returns true if p is a process handle (:java.lang.ProcessHandle).

```
(shell/process-handle? p)
```


## process-info

Returns true the process info for a process represented by a pid or a 
process handle.

Returns for each process a map the keys ':arguments', ':command', 
':command-line', ':start-time', ':total-cpu-millis', and ':user'

```
(shell/process-info p)
```


## processes

Returns the process handle (:java.lang.ProcessHandle) for all
running processes.

```
(shell/processes)
```


## kill

Requests the process to be killed. Returns true if the process is 
killed and false if the process stays alive. Returns nil if the 
process does not exist. Accepts a process PID or a process handle 
(:java.lang.ProcessHandle).

```
(shell/kill pid)
```


## kill-forcibly

Requests the process to be killed forcibly. Returns true if the process 
is killed and false if the process stays alive. Returns nil if the 
process does not exist. Accepts a process PID or a process handle 
(:java.lang.ProcessHandle).
        

```
(shell/kill-forcibly pid)
```


## wait-for-process-exit

Waits until the process with the PID exits. Waits max timeout 
seconds. Returns nil if the process exits before reaching the 
timeout, else the PID is returned. Accepts a process PID or a 
 process handle (:java.lang.ProcessHandle).

```
(shell/wait-for-process-exit pid timeout) 
```
