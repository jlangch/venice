# OpenAI API Costs

 

## Costs for the last N days (raw cost buckets)

Example: Return the raw cost buckets for the last 2 days:

Note: OpenAI limits the number of returned cost buckets to 180 per call

``` clojure
(do
  (load-module :openai-java)
  (let [admin-key  "sk-admin-1234"
        client     (openai-java/client :openai-admin-key admin-key)]
     (openai-java/costs client 2)))
```

Output:

```
( {:lineitem "gpt-5.4-2026-03-05, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-07-05T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-07-04T00:00} 
  {:lineitem "gpt-5.4-2026-03-05, input" :project-id "proj_xt...nN" :currency "usd" :value 0.0082375 :bucket-end 2026-07-05T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-07-04T00:00} 
  {:lineitem "gpt-5.4-2026-03-05, output" :project-id "proj_xt...nN" :currency "usd" :value 0.035085 :bucket-end 2026-07-05T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-07-04T00:00} 
  {:lineitem "gpt-5.4-2026-03-05, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-07-06T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-07-05T00:00} 
  {:lineitem "gpt-5.4-2026-03-05, input" :project-id "proj_xt...nN" :currency "usd" :value 0.004225 :bucket-end 2026-07-06T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-07-05T00:00} 
  {:lineitem "gpt-5.4-2026-03-05, output" :project-id "proj_xt...nN" :currency "usd" :value 0.027915 :bucket-end 2026-07-06T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-07-05T00:00} 
  {:lineitem "gpt-5.5-2026-04-23, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-07-06T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-07-05T00:00} 
  {:lineitem "gpt-5.5-2026-04-23, input" :project-id "proj_xt...nN" :currency "usd" :value 0.013765 :bucket-end 2026-07-06T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-07-05T00:00} 
  {:lineitem "gpt-5.5-2026-04-23, output" :project-id "proj_xt...nN" :currency "usd" :value 0.13422 :bucket-end 2026-07-06T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-07-05T00:00})
```

## Costs for a month (raw cost buckets)

Example: Return the raw cost buckets for June 2026:

Note: OpenAI limits the number of returned cost buckets to 180 per call

``` clojure
(do
  (load-module :openai-java)
  (let [admin-key  "sk-admin-1234"
        client     (openai-java/client :openai-admin-key admin-key)]
     (openai-java/costs-by-month client 2026 6)))
```

Output:

```
({:lineitem "gpt-5.4-2026-03-05, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-06-02T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-01T00:00}
 {:lineitem "gpt-5.4-2026-03-05, input" :project-id "proj_xt...nN" :currency "usd" :value 0.017735 :bucket-end 2026-06-02T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-01T00:00}
 {:lineitem "gpt-5.4-2026-03-05, output" :project-id "proj_xt...nN" :currency "usd" :value 0.06879 :bucket-end 2026-06-02T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-01T00:00}
 
 ...
 
 {:lineitem "gpt-5.5-2026-04-23, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-07-01T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-30T00:00}
 {:lineitem "gpt-5.5-2026-04-23, input" :project-id "proj_xt...nN" :currency "usd" :value 0.00741 :bucket-end 2026-07-01T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-30T00:00}
 {:lineitem "gpt-5.5-2026-04-23, output" :project-id "proj_xt...nN" :currency "usd" :value 0.10017 :bucket-end 2026-07-01T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-30T00:00})
```


## Costs for a period (raw cost buckets)

Example: Return the raw cost buckets for June 2026:

Note: OpenAI limits the number of returned cost buckets to 180 per call

``` clojure
(do
  (load-module :openai-java)
  (let [admin-key  "sk-admin-1234"
        start      (time/local-date 2026 6 10) 
        end        (time/local-date 2026 6 12) 
        client     (openai-java/client :openai-admin-key admin-key)]
     (openai-java/costs-by-days client start end)))
```

Output:

```
({:lineitem "gpt-5.4-2026-03-05, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-06-11T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-10T00:00} 
{:lineitem "gpt-5.4-2026-03-05, input" :project-id "proj_xt...nN" :currency "usd" :value 0.0031025 :bucket-end 2026-06-11T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-10T00:00} 
{:lineitem "gpt-5.4-2026-03-05, output" :project-id "proj_xt...nN" :currency "usd" :value 0.00987 :bucket-end 2026-06-11T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-10T00:00} 
{:lineitem "gpt-5-2025-08-07, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5-2025-08-07, input" :project-id "proj_xt...nN" :currency "usd" :value 1.0E-5 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5-2025-08-07, output" :project-id "proj_xt...nN" :currency "usd" :value 7.5E-4 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5.4-2026-03-05, cached input" :project-id "proj_xt...nN" :currency "us
d" :value 0.0 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5.4-2026-03-05, input" :project-id "proj_xt...nN" :currency "usd" :value 0.003625 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5.4-2026-03-05, output" :project-id "proj_xt...nN" :currency "usd" :value 0.00906 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5.5-2026-04-23, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5.5-2026-04-23, input" :project-id "proj_xt...nN" :currency "usd" :value 4.0E-5 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5.5-2026-04-23, output" :project-id "proj_xt...nN" :currency "usd" :value 1.5E-4 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5-mini-2025-08-07, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5-mini-2025-08-07, input" :project-id "proj_xt...nN" :currency "usd" :value 2.0E-6 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5-mini-2025-08-07, output" :project-id "proj_xt...nN" :currency "usd" :value 1.64E-4 :bucket-end 2026-06-12T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-11T00:00} 
{:lineitem "gpt-5.4-2026-03-05, cached input" :project-id "proj_xt...nN" :currency "usd" :value 0.0 :bucket-end 2026-06-13T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-12T00:00} 
{:lineitem "gpt-5.4-2026-03-05, input" :project-id "proj_xt...nN" :currency "usd" :value 0.00129 :bucket-end 2026-06-13T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-12T00:00} 
{:lineitem "gpt-5.4-2026-03-05, output" :project-id "proj_xt...nN" :currency "usd" :value 0.001605 :bucket-end 2026-06-13T00:00 :api-key-id "key_vE...Me" :bucket-start 2026-06-12T00:00})
```


## Costs for a month aggregated by day

Example: Retrieve the costs for June 2026 aggregated by day

``` clojure
(do
  (load-module :openai-java)
  (load-module :ascii-table)

  (let [admin-key  "sk-admin-1234"
        client     (openai-java/client :openai-admin-key admin-key)]
    (openai-java/costs-by-month-daily client 2026 6)))
```

Output:

``` clojure
( [2026-06-01 0.09M] 
  [2026-06-02 0.01M] 
  [2026-06-03 0.04M]
  ...
  [2026-06-28 0.84M] 
  [2026-06-29 0.37M] 
  [2026-06-30 0.16M])
```

Example: Retrieve the costs for 2026 June aggregated by day and format it 

``` clojure
(do
  (load-module :openai-java)
  (load-module :ascii-table)

  (let [admin-key  "sk-admin-1234"
        client     (openai-java/client :openai-admin-key admin-key)
        costs      (openai-java/costs-by-month-daily client 2026 6)
        total      (reduce #(dec/add %1 %2 2 :HALF_UP) (map second costs))]
    ;; format the daily costs
    (ascii-table/print 
      [ { :header {:text "Date" }  
          :body { :align :left } 
          :footer { :text "" }  
          :width 10 }
        { :header { :text "Amount [USD]" :align :right }  
          :body { :align :right } 
          :footer { :text (str total) :align :right }  
          :width 14 } ] 
      costs
      :minimal
      0)))
```

Output:

```
Date        Amount [USD]
────────────────────────
2026-06-01          0.09
2026-06-02          0.01
2026-06-03          0.04
2026-06-04          0.00
2026-06-05          0.01
2026-06-06          0.00
2026-06-07          0.00
2026-06-08          0.00
2026-06-09          0.02
2026-06-10          0.01
2026-06-11          0.01
2026-06-12          0.00
2026-06-13          0.07
2026-06-14          0.00
2026-06-15          0.00
2026-06-16          0.02
2026-06-17          0.00
2026-06-18          0.06
2026-06-19          0.06
2026-06-20          0.09
2026-06-21          0.12
2026-06-22          0.60
2026-06-23          0.21
2026-06-24          0.65
2026-06-25          0.12
2026-06-26          0.10
2026-06-27          0.01
2026-06-28          0.84
2026-06-29          0.37
2026-06-30          0.16
────────────────────────
                    3.67
────────────────────────
```
        