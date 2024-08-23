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

```clojure
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

```java
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

## Simple Rule Engine

Example: Discount Coupon Rules

we are defining some rules for applying discounts based on coupon codes. These are the three rules we are defining here:

1. 10% Off Coupon: Applies a 10% discount if the coupon code is "SUMMER10".
2. Free Shipping Coupon: Provides free shipping if the coupon code is "FREESHIP".
3. Buy Many Get 30% Off: Offers a 30% discount on the entire order if the coupon code is "BMGOF" and the cart contains at least two items.

When a customer enters a coupon code during checkout, the Rules Engine can 
evaluate the rules and apply the appropriate discounts.


The rule is stored in the application's configuration database and is 
defined like:

```clojure
(defn discount [cart coupon]
  (case coupon
    ;; "10% Off Coupon"
    "SUMMER10"    10.0
    
    ;; "Free Shipping Coupon"
    "FREESHIP"    0.0
      
    ;; "Buy Many Get 30% Off"
    "BMGOF"       (if (>= (. cart :getCount) 2) 30.0 0.0)
    
    0.0))  
    
(defn freeship? [cart coupon]
  (case coupon
    ;; "10% Off Coupon"
    "SUMMER10"    false
    
    ;; "Free Shipping Coupon"
    "FREESHIP"   true
      
    ;; "Buy Many Get 30% Off"
    "BMGOF"       false
    
    false))  
    
    
;; apply discount rules
{ :discount  (discount cart coupon)
  :freeship  (freeship? cart coupon) }              
```

Computing discounts

```java
public class Discount {
    public Discount(double discounPercentage, boolean freeshipping) {
        this.discount = discount;
        this.freeshipping = freeshipping;
    }
    
    public double getDiscounPercentage() { return discounPercentage; }
    public boolean isFreeshipping() { return freeshipping; }

    private final double discounPercentage;
    private final boolean freeshipping;
}

public class DiscountRules {
    public DiscountRules(Configuration config) {
        this.config = config;
        this.venice = new Venice();
    }

    public Discount calculate(Cart cart, String coupon) {
        Map<String,Object> event = (Map<String,Object>)venice.eval(
                                          rule,
                                          Parameters.of("cart", cart, 
                                                        "coupon", coupon));
                                                        
        return new Discount(event.get("discount"), event.get("freeship"))
    }
    
    private IPreCompiled compileRule(Configuration config) {
        String ruleFn = config.getProperty("rules.discount");
        return venice.precompile("rule", ruleFn, true);
    }
    
    
    private final Configuration config;
    private final Venice venice;
    private final IPreCompiled rule;
}
```



## Scriptable Applications




## Prototyping a HTMX based WebApp



## Zippping Tomcat logs for archiving




