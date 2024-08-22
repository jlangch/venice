# Venice Use Cases




## Automation tasks

custom shells 



## AdHoc creation of PDF and Excel reports 





## Scriptable Extension Points

A web application generates audit events for all important business and 
technical actions and stores them to a database. There is a view in the
application to search and display audit events.

Based on business specific filters critical events should be sent immediately,
as they happen, by e-mail to an administrator. The filter to select these 
events must be configurable at runtime.


The filter is stored in the application's configuration database and is 
defined like:

```text
(let [event-name  (. event :getEventName)
      event-type  (. event :getEventType)
      event-key1  (. event :getEventKey1)]
  (or
    (match? event-name "webapp[.](started|stopped)")
    (and (== event-name "login") (== event-key1  "superuser"))
    (== event-type "ALERT")
    (str/starts-with? event-name "login.foreign.country.")))
```

The audit notifier using the filter to send notifications:

```
public class AuditNotifier {
    public AuditNotifier(Configuration config, NotificationService notifSvc) {
        this.config = config;
        this.notifSvc = notifSvc;
        this.venice = new Venice();
    }

    public void process(Event event) {
         String filter = config.getProperty("audit.notification.filter");
    		Boolean match = (Boolean)venice.eval(filter, Parameters.of("event", event));
    		if (Boolean.TRUE.equals(match)) {
    			notifSvc.sendAuditEventEmail(event);
    		}
    }
    
    private final Configuration config;
    private final NotificationService notifSvc;
    private final Venice venice;
}
```


## Scriptable Applications




## Prototyping a HTMX based WebApp



## Zippping Tomcat logs for archiving




