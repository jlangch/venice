/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util.ipc.impl.conn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.util.ipc.impl.Message;


public class Subscriptions {

    public Subscriptions() {
    }


    public void addSubscription(
            final String topicName,
            final IPublisher publisher
    ) {
        final Set<String> t = subscriptions.getOrDefault(publisher, new HashSet<>());
        t.add(topicName);
        subscriptions.put(publisher, t);
    }

    public void removeSubscription(
            final String topicName,
            final IPublisher publisher
    ) {
        final Set<String> t = subscriptions.getOrDefault(publisher, new HashSet<>());
        t.remove(topicName);
        if (t.isEmpty()) {
            subscriptions.remove(publisher);
        }
        else {
            subscriptions.put(publisher, t);
        }
    }

    public void removeSubscriptions(
            final IPublisher publisher
    ) {
        subscriptions.remove(publisher);
    }

    public void publish(final Message msg) {
        // mark the message as a subscription reply
        final Message m = msg.withSubscriptionReply(true);

        final String topic = m.getTopicName();

        final List<IPublisher> publishers = new ArrayList<>(subscriptions.keySet());

        publishers.forEach(p -> {
            final Set<String> topics = subscriptions.get(p);
            if (topics != null && topics.contains(topic)) {
                p.publish(m);
            }
        });
    }

    public int getClientSubscriptionCount() {
        return subscriptions.size();
    }

    public int getTopicSubscriptionCount() {
        return subscriptions.values().stream().mapToInt(t -> t.size()).sum();
    }


    // subscriptions: publisher -> topics
    private final Map<IPublisher, Set<String>> subscriptions = new ConcurrentHashMap<>();
}
