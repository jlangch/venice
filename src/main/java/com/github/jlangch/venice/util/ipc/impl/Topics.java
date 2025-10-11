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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.jlangch.venice.impl.util.StringUtil;


public class Topics {

    private Topics(final String topic) {
        this.topics.add(topic);
    }

    private Topics(final Collection<String> topics) {
        if (topics instanceof Set) {
            this.topics.addAll(topics);
        }
        else {
            // remove duplicates
            final Set<String> distinct = new HashSet<>(topics);
            this.topics.addAll(distinct.size() == topics.size() ? topics : distinct);
        }
    }

    private Topics(final String[] topics) {
        this(Arrays.asList(topics));
    }


    public static Topics of(final String topic) {
        validate(topic);
        return new Topics(topic);
    }

    public static Topics of(final Set<String> topics) {
        if (topics == null || topics.isEmpty()) {
            throw new IllegalArgumentException("A least one topic is required!");
        }
        topics.forEach(t -> validate(t));
        if (topics.size() > TOPICS_MAX) {
            throw new IllegalArgumentException("More than " + TOPICS_MAX + "specified!");
        }

        return new Topics(topics);
    }

    public static Topics of(final String[] topics) {
        if (topics == null || topics.length == 0) {
            throw new IllegalArgumentException("A least one topic is required!");
        }
        for(int ii=0; ii<topics.length; ii++) validate(topics[ii]);
        if (topics.length > TOPICS_MAX) {
            throw new IllegalArgumentException("More than " + TOPICS_MAX + "specified!");
        }

        return new Topics(topics);
    }


    public String getTopic() {
        return topics.get(0);
   }

    public List<String> getTopics() {
        return Collections.unmodifiableList(topics);
     }

    public Set<String> getTopicsSet() {
        return Collections.unmodifiableSet(new HashSet<>(topics));
     }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((topics == null) ? 0 : topics.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Topics other = (Topics) obj;
        if (topics == null) {
            if (other.topics != null)
                return false;
        } else if (!topics.equals(other.topics))
            return false;
        return true;
    }


    public static String encode(final Topics topics) {
        Objects.requireNonNull(topics);
        return String.join(",", topics.topics);
    }

    public static Topics decode(final String topics) {
        Objects.requireNonNull(topics);
       return Topics.of(topics.split(" *, *"));
    }


    public static void validate(final String topic) {
        if (StringUtil.isBlank(topic)) {
            throw new IllegalArgumentException("A topic must not be empty or blank!");
        }

        if (topic.length() > TOPIC_MAX_LEN) {
            throw new IllegalArgumentException(
                    "A topic is limit to " + TOPIC_MAX_LEN + "characters!");
        }

        if (topic.contains(",")) {
            throw new IllegalArgumentException("A topic must not contain commas!");
        }

       for(char c : topic.toCharArray()) {
           if (Character.isWhitespace(c)) {
               throw new IllegalArgumentException("A topic must not contain white spaces!");
           }
        }
    }


    public static final long TOPIC_MAX_LEN = 100;
    public static final long TOPICS_MAX = 20;

    private final List<String> topics = new ArrayList<>();
}
