# Profiling

Venice supports simple code profiling to analyze execution performance.

The functions `perf` and `prof` work hand in hand.
The `perf` macro profiles Venice functions and `prof` controls and prints 
the gathered profile metrics.


## perf

```clojure
(perf expr warmup-iterations test-iterations)
```

Runs a performance test on the given expression. 

Runs the test in 3 phases: 
   1. Runs the expr in a warmup phase to allow the HotSpot compiler to do optimizations. 
   2. Runs the garbage collector 
   3. Runs the expression under profiling.


## prof

```clojure
(prof opts)
```

Controls the code profiling.

- `(prof :on)`   turn profiler on  
- `(prof :off)`   turn profiler off  
- `(prof :status)`   returns the profiler on/off staus  
- `(prof :clear)`   clear profiler data captured so far  
- `(prof :data)`   returns the profiler data as map  
- `(prof :data-formatted)`   returns the profiler data as formatted text  
- `(prof :data-formatted "Metrics test")`   returns the profiler data as formatted text with a title  


## Example profiling a loop

The profiler runs the sum function 100 times as warm-up followed by 100 times to profile it. 

```clojure
(do
   (defn sum [n]
      (loop [i 0]
         (if (< i n)
            (recur (inc i))
            i)))
            
   (perf (sum 100000) 100 100)
   
   (println (prof :data-formatted "Metrics: loop")))
```

The metrics table shows four columns with the function name, the number of calls, the 
total and average time for the function's calls:

```text
-----------------------------------------------
Metrics: loop
-----------------------------------------------
_warmup     [       1]:     9,82 s             
_test       [       1]:     9,40 s             
sum         [     100]:     9,40 s     93,97 ms
<           [10000100]:  2850,62 ms      285 ns
inc         [10000000]:  1588,21 ms      158 ns
_warmup-gc  [       1]:    46,86 ms            
-----------------------------------------------
```
