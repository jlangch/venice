/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class Embed_14_ExtensionPoint {

    public static void main(final String[] args) {
        try {
            run();
            System.exit(0);
        }
        catch(VncException ex) {
            ex.printVeniceStackTrace();
            System.exit(1);
        }
        catch(RuntimeException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void run() {
    	// Setup services
    	final Configuration config = new Configuration();
    	config.setValue(
    			// id
    			"audit.notification.filter",
    			// script
		    	"(let [event-name  (. event :getName)                                \n" +
		    	"      event-type  (. event :getType)                                \n" +
		    	"      event-key   (. event :getKey)]                                \n" +
		    	"  (or (match? event-name \"webapp[.](started|stopped)\")            \n" +
		    	"      (and (== event-name \"login\") (== event-key \"superuser\"))  \n" +
		    	"      (== event-type \"ALERT\")                                     \n" +
		    	"      (str/starts-with? event-name \"login.foreign.country.\")))    ");

    	final NotificationService notficationService = new NotificationService();

    	// Create the not AuditNotifier extension point
    	final AuditNotifier notifier = new AuditNotifier(config, notficationService);

    	// Process events
    	notifier.process(new Event(EventType.INFO, "webapp.started", "system", "WebApp started"));
    	notifier.process(new Event(EventType.INFO, "login", "john", "Login user john"));

    	// Check
        System.out.println("Notified events: " + notficationService.getSentEvents().size());
    }


    public static class Configuration {
    	public Configuration() {
    	}

    	public String getValue(final String key) {
    		return config.get(key);
    	}
    	public void setValue(final String key, final String value) {
    		config.put(key, value);
    	}

    	private final HashMap<String,String> config = new HashMap<>();
    }


    public static class NotificationService {
    	public NotificationService() {
    	}

    	public void sendAuditEventEmail(final Event event) {
    		sentEvents.add(event);
    	}

    	public List<Event> getSentEvents() {
    		return Collections.unmodifiableList(sentEvents);
    	}

    	private List<Event> sentEvents = new ArrayList<>();
    }

    public static class AuditNotifier {
        public AuditNotifier(
        		final Configuration config,
        		final NotificationService notifSvc
        ) {
            this.config = config;
            this.notifSvc = notifSvc;
            this.venice = new Venice();
        }

        public void process(Event event) {
             String filter = config.getValue("audit.notification.filter");
        		Boolean match = (Boolean)venice.eval(filter, Parameters.of("event", event));
        		if (Boolean.TRUE.equals(match)) {
        			notifSvc.sendAuditEventEmail(event);
        		}
        }

        private final Configuration config;
        private final NotificationService notifSvc;
        private final Venice venice;
    }

    public static enum EventType {
    	SYSTEM, INFO, WARN, ALERT;
    }

    public static class Event {
    	public Event(
    	    	final EventType type,
    	    	final String name,
    	    	final String key,
    	    	final String message
    	) {
    		this.type = type;
    		this.name = name;
    		this.key = key;
    		this.message = message;
    	}

    	public EventType getType() {
			return type;
		}
		public String getName() {
			return name;
		}
		public String getKey() {
			return key;
		}
		public String getMessage() {
			return message;
		}

		private final EventType type;
    	private final String name;
    	private final String key;
    	private final String message;
    }

}
