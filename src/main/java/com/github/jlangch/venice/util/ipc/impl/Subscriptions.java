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

import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.util.ipc.Message;


public class Subscriptions {

    public Subscriptions() {
    }


    public void addSubscription(final String topic, final IPublisher publisher) {
        subscriptions.compute(publisher, (k,v) -> {
            if (v == null) {
                return toSet(topic);
            }
            else {
                v.add(topic);
                return v;
            }});
    }

    public void removeSubscription(final String topic, final IPublisher publisher) {
        subscriptions.compute(publisher, (k,v) -> {
            if (v == null) {
                return toSet();
            }
            else {
                v.remove(topic);
                return v;
            }});
    }

    public void removePublisher(final IPublisher publisher) {
        subscriptions.remove(publisher);
    }

    public void publish(final Message msg) {
        final String topic = msg.getTopic();

        final List<IPublisher> publishers = subscriptions
                                                .entrySet()
                                                .stream()
                                                .filter(e -> e.getValue().contains(topic))
                                                .map(e -> e.getKey())
                                                .collect(Collectors.toList());

        publishers.forEach(p -> p.publish(msg));
    }

    public int getClientSubscriptionCount() {
        return subscriptions.size();
    }

    public int getTopicSubscriptionCount() {
        return subscriptions.values().stream().mapToInt(t -> t.size()).sum();
    }


    private final HashMap<IPublisher, Set<String>> subscriptions = new HashMap<>();
}
