/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.util.ipc.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Subscriptions {

    public Subscriptions() {
    }

    public void addSubscription(
            final String topic,
            final IPublisher publisher
    ) {
        final HashSet<String> topics = new HashSet<>();
        topics.add(topic);
        subscriptions.put(publisher, topics);
    }

    public void addSubscription(
            final Set<String> topics,
            final IPublisher publisher
    ) {
        subscriptions.put(publisher, topics);
    }

    public void removeSubscriptions(
            final IPublisher publisher
    ) {
        subscriptions.remove(publisher);
    }

    public void publish(final Message msg) {
        final String topic = msg.getTopic();

        final List<IPublisher> publishers = new ArrayList<>(subscriptions.keySet());

        publishers.forEach(p -> {
            final Set<String> topics = subscriptions.get(p);
            if (topics != null && topics.contains(topic)) {
                p.publish(msg);
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
    private final HashMap<IPublisher, Set<String>> subscriptions = new HashMap<>();
}
