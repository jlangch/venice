# Models

 

List the available models:

``` clojure
(do
  (load-module :openai-java)
  (->> (openai-java/client)
       (openai-java/models)
       (sort-by :id)
       (docoll println)))
```

Output:

```
  :  (skipped for brevity)
  :
{:id gpt-5.5 :onwned-by system :created 2026-04-22T02:27:27}
{:id gpt-5.5-2026-04-23 :onwned-by system :created 2026-04-22T06:27:21}
{:id gpt-5.5-pro :onwned-by system :created 2026-04-22T21:45:49}
{:id gpt-5.5-pro-2026-04-23 :onwned-by system :created 2026-04-22T21:47:50}
{:id gpt-audio :onwned-by system :created 2025-08-28T00:00:49}
{:id gpt-audio-1.5 :onwned-by system :created 2026-02-20T01:28:05}
{:id gpt-audio-2025-08-28 :onwned-by system :created 2025-08-27T00:55:46}
{:id gpt-audio-mini :onwned-by system :created 2025-10-03T17:20:27}
{:id gpt-audio-mini-2025-10-06 :onwned-by system :created 2025-10-03T17:22:17}
{:id gpt-audio-mini-2025-12-15 :onwned-by system :created 2025-12-15T00:53:28}
{:id gpt-image-1 :onwned-by system :created 2025-04-24T17:50:30}
{:id gpt-image-1-mini :onwned-by system :created 2025-09-26T00:17:01}
{:id gpt-image-1.5 :onwned-by system :created 2025-11-25T00:30:20}
{:id gpt-image-2 :onwned-by system :created 2026-04-17T04:23:15}
{:id gpt-image-2-2026-04-21 :onwned-by system :created 2026-04-17T04:26:34}
{:id gpt-realtime :onwned-by system :created 2025-08-27T05:15:01}
{:id gpt-realtime-1.5 :onwned-by system :created 2026-02-19T00:37:49}
{:id gpt-realtime-2 :onwned-by system :created 2026-05-05T18:33:52}
{:id gpt-realtime-2.1 :onwned-by system :created 2026-06-23T22:44:47}
{:id gpt-realtime-2.1-mini :onwned-by system :created 2026-06-23T22:45:06}
{:id gpt-realtime-2025-08-28 :onwned-by system :created 2025-08-27T05:16:13}
{:id gpt-realtime-mini :onwned-by system :created 2025-10-03T18:45:33}
{:id gpt-realtime-mini-2025-10-06 :onwned-by system :created 2025-10-03T18:46:15}
{:id gpt-realtime-mini-2025-12-15 :onwned-by system :created 2025-12-13T07:46:47}
{:id gpt-realtime-translate :onwned-by system :created 2026-05-05T03:03:36}
{:id gpt-realtime-whisper :onwned-by system :created 2026-05-05T20:14:20}
  :
  :  (skipped for brevity)
```
