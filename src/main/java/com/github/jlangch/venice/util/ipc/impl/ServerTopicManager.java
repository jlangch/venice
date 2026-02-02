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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.util.ipc.Authenticator;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.ServerConfig;
import com.github.jlangch.venice.util.ipc.impl.dest.topic.IpcTopic;
import com.github.jlangch.venice.util.ipc.impl.dest.topic.Topic;
import com.github.jlangch.venice.util.ipc.impl.util.ServerLogger;


public class ServerTopicManager {

    public ServerTopicManager(
            final ServerConfig config,
            final ServerLogger logger
    ) {
        this.authenticator = config.getAuthenticator();
        this.logger = logger;
        this.maxTopics = config.getMaxTopics();
    }


    public IpcTopic getTopic(final String topicName) {
        Objects.requireNonNull(topicName);

        return topics.get(topicName);
    }

    public void createTopic(final String topicName) {
        TopicValidator.validateTopicName(topicName);

        if (topics.size() >= maxTopics) {
            throw new IpcException(String.format(
                    "Cannot create topic! Reached the limit of %d topics.",
                    maxTopics));
        }

        topics.computeIfAbsent(
                topicName,
                n -> {
                    final Topic t = new Topic(topicName);

                    t.updateAcls(
                            authenticator.getTopicAclsMappedByPrincipal(topicName),
                            authenticator.getTopicDefaultAcl());

                    logger.info(
                        "server", "topic",
                        String.format("Created topic %s.",topicName));

                    return t;
                });
    }

    public void removeTopic(final String topicName) {
        Objects.requireNonNull(topicName);

        topics.remove(topicName);
    }

    public boolean existsTopic(final String topicName) {
        Objects.requireNonNull(topicName);

        return topics.containsKey(topicName);
    }

    /**
     * Get a topic's status.
     *
     * @param topicName a topic name
     * @return the topic or <code>null</code> if the topic does not exist
     */
    public Map<String,Object> getTopicStatus(final String topicName) {
        Objects.requireNonNull(topicName);

        final IpcTopic q = topics.get(topicName);

        final Map<String,Object> status = new HashMap<>();

        status.put("name",      topicName);
        status.put("exists",    q != null);

        return status;
    }


    private final Authenticator authenticator;
    private final ServerLogger logger;
    private final int maxTopics;
    private final Map<String, IpcTopic> topics = new ConcurrentHashMap<>();
}
