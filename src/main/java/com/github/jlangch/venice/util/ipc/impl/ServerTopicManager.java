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
package com.github.jlangch.venice.util.ipc.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.util.ipc.impl.topic.IpcTopic;
import com.github.jlangch.venice.util.ipc.impl.topic.Topic;


public class ServerTopicManager {

    public ServerTopicManager() {
    }

    public IpcTopic getTopic(final String topicName) {
        return topics.get(topicName);
    }

    public void createTopic(final String topicName) {
        TopicValidator.validateTopicName(topicName);
        topics.putIfAbsent(topicName, new Topic(topicName));
    }

    public void removeTopic(final String topicName) {
        topics.remove(topicName);
    }

    public boolean existsTopic(final String topicName) {
        return topics.containsKey(topicName);
    }

    private final Map<String, IpcTopic> topics = new ConcurrentHashMap<>();
}
