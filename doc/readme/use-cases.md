# Venice Use Cases


## Scriptable Application



## Scriptable Extension Points

A web application generates audit events for all important business and 
technical actions and stores them to a database. There is a view in the
application to search and display audit events.

Based on business specific filters critical events should be sent immediately
as they happen by e-mail to an administrator. The filter to select these 
events must be configurable at runtime.



```text
audit.notification.type=email
audit.notification.enabled=true
audit.notification.filter=\
    (let [event-name  (. event :getEventName) \
          event-type  (. event :getEventType) \
          event-key1  (. event :getEventKey1)] \
      (or \
        (match? event-name "webapp[.](started|stopped)") \
        (and (== event-name "login") (== event-key1  "superuser")) \
        (== event-type "ALERT") \
        (str/starts-with? event-name "login.foreign.country.")))
```

