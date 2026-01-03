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
package com.github.jlangch.venice.util.ipc;

import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.impl.Topics;


public class TopicsTest {

    @Test
    public void test_topics_single() {
        assertThrows(IllegalArgumentException.class, () -> Topics.of((String)null));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(""));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(" "));
        assertThrows(IllegalArgumentException.class, () -> Topics.of("a b"));
        assertThrows(IllegalArgumentException.class, () -> Topics.of("a,b"));
        assertThrows(IllegalArgumentException.class, () -> Topics.of("a,b "));

        assertEquals("alpha", Topics.of("alpha").getTopic());

        assertEquals(1, Topics.of("alpha").getTopics().size());
        assertEquals("alpha", Topics.of("alpha").getTopics().get(0));

        assertEquals(1, Topics.of("alpha").getTopicsSet().size());
        assertTrue(Topics.of("alpha").getTopicsSet().contains("alpha"));
    }

    @Test
    public void test_topics_multiple() {
        assertThrows(IllegalArgumentException.class, () -> Topics.of((Set<String>)null));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(new HashSet<>()));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet((String)null)));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet(" ")));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet("a b")));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet("a,b")));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet("a,b ")));

        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet("alpha", (String)null)));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet("alpha", " ")));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet("alpha", "a b")));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet("alpha", "a,b")));
        assertThrows(IllegalArgumentException.class, () -> Topics.of(toSet("alpha", "a,b ")));

        // single set
        assertEquals("alpha", Topics.of(toSet("alpha")).getTopic());

        assertEquals(1, Topics.of(toSet("alpha")).getTopics().size());
        assertEquals("alpha", Topics.of(toSet("alpha")).getTopics().get(0));

        assertEquals(1, Topics.of(toSet("alpha")).getTopicsSet().size());
        assertTrue(Topics.of(toSet("alpha")).getTopicsSet().contains("alpha"));

        // many set
        assertEquals(2, Topics.of(toSet("alpha", "beta")).getTopics().size());
        assertTrue(Topics.of(toSet("alpha", "beta")).getTopics().contains("alpha"));
        assertTrue(Topics.of(toSet("alpha", "beta")).getTopics().contains("beta"));

        assertEquals(2, Topics.of(toSet("alpha", "beta")).getTopicsSet().size());
        assertTrue(Topics.of(toSet("alpha", "beta")).getTopicsSet().contains("alpha"));
        assertTrue(Topics.of(toSet("alpha", "beta")).getTopicsSet().contains("beta"));
    }

    @Test
    public void test_topics_decode() {
        assertThrows(NullPointerException.class, () -> Topics.decode(null));
        assertThrows(IllegalArgumentException.class, () -> Topics.decode(""));
        assertThrows(IllegalArgumentException.class, () -> Topics.decode(" "));
        assertThrows(IllegalArgumentException.class, () -> Topics.decode(","));

        assertThrows(IllegalArgumentException.class, () -> Topics.decode(" alpha"));
        assertThrows(IllegalArgumentException.class, () -> Topics.decode("alpha "));
        assertThrows(IllegalArgumentException.class, () -> Topics.decode("al pha"));

        assertThrows(IllegalArgumentException.class, () -> Topics.decode("alpha beta"));
        assertThrows(IllegalArgumentException.class, () -> Topics.decode("alpha,be ta"));

        final Topics topics1 = Topics.decode("alpha");
        assertEquals(1, topics1.getTopicsSet().size());
        assertTrue(topics1.getTopicsSet().contains("alpha"));

        final Topics topics1a = Topics.decode("alpha,");
        assertEquals(1, topics1a.getTopicsSet().size());
        assertTrue(topics1a.getTopicsSet().contains("alpha"));

        final Topics topics2 = Topics.decode("alpha,beta");
        assertEquals(2, topics2.getTopicsSet().size());
        assertTrue(topics2.getTopicsSet().contains("alpha"));
        assertTrue(topics2.getTopicsSet().contains("beta"));

        final Topics topics3 = Topics.decode("alpha,beta,gamma");
        assertEquals(3, topics3.getTopicsSet().size());
        assertTrue(topics3.getTopicsSet().contains("alpha"));
        assertTrue(topics3.getTopicsSet().contains("beta"));
        assertTrue(topics3.getTopicsSet().contains("gamma"));
    }

    @Test
    public void test_topics_encode_decode() {
       final Topics topics1 = Topics.decode(Topics.encode(Topics.of(toSet("alpha"))));
       assertEquals(1, topics1.getTopicsSet().size());
       assertTrue(topics1.getTopicsSet().contains("alpha"));

       final Topics topics2 = Topics.decode(Topics.encode(Topics.of(toSet("alpha", "beta"))));
       assertEquals(2, topics2.getTopicsSet().size());
       assertTrue(topics2.getTopicsSet().contains("alpha"));
       assertTrue(topics2.getTopicsSet().contains("beta"));

       final Topics topics3 = Topics.decode(Topics.encode(Topics.of(toSet("alpha", "beta", "gamma"))));
       assertEquals(3, topics3.getTopicsSet().size());
       assertTrue(topics3.getTopicsSet().contains("alpha"));
       assertTrue(topics3.getTopicsSet().contains("beta"));
       assertTrue(topics3.getTopicsSet().contains("gamma"));
    }

    @Test
    public void test_topics_distinct_1() {
       final Topics topics1 = Topics.of(new String[] { "alpha" });
       assertEquals(1, topics1.getTopicsSet().size());
       assertTrue(topics1.getTopicsSet().contains("alpha"));

       final Topics topics2 = Topics.of(new String[] { "alpha", "alpha" });
       assertEquals(1, topics2.getTopicsSet().size());
       assertTrue(topics2.getTopicsSet().contains("alpha"));

       final Topics topics3 = Topics.of(new String[] { "alpha", "beta", "alpha" });
       assertEquals(2, topics3.getTopicsSet().size());
       assertTrue(topics3.getTopicsSet().contains("alpha"));
       assertTrue(topics3.getTopicsSet().contains("beta"));
    }

    @Test
    public void test_topics_distinct_2() {
       final Topics topics1 = Topics.decode("alpha");
       assertEquals(1, topics1.getTopicsSet().size());
       assertTrue(topics1.getTopicsSet().contains("alpha"));

       final Topics topics2 = Topics.decode("alpha,alpha");
       assertEquals(1, topics2.getTopicsSet().size());
       assertTrue(topics2.getTopicsSet().contains("alpha"));

       final Topics topics3 = Topics.decode("alpha,beta,alpha");
       assertEquals(2, topics3.getTopicsSet().size());
       assertTrue(topics3.getTopicsSet().contains("alpha"));
       assertTrue(topics3.getTopicsSet().contains("beta"));
    }

    @Test
    public void test_topics_TOPICS_MAX_1() {
        final List<String> topics = new ArrayList<>();
        for(int ii=10; ii<10+Topics.TOPICS_MAX; ii++) topics.add(String.valueOf(ii));

        assertEquals(20, Topics.decode(String.join(",", topics)).getTopics().size());

        // more than TOPICS_MAX
        topics.clear();
        for(int ii=10; ii<10+Topics.TOPICS_MAX+1; ii++) topics.add(String.valueOf(ii));
        assertThrows(IllegalArgumentException.class, () -> Topics.decode(String.join(",", topics)));
    }

    @Test
    public void test_topics_TOPICS_MAX_2() {
       final List<String> topics = new ArrayList<>();
       for(int ii=10; ii<10+Topics.TOPICS_MAX; ii++) topics.add(String.valueOf(ii));

       final Topics topics1 = Topics.of(new HashSet<>(topics));
       assertEquals(20, topics1.getTopicsSet().size());

       // more than TOPICS_MAX
       topics.clear();
       for(int ii=10; ii<10+Topics.TOPICS_MAX+1; ii++) topics.add(String.valueOf(ii));
       assertThrows(IllegalArgumentException.class, () -> Topics.of(new HashSet<>(topics)));
    }

    @Test
    public void test_topics_TOPIC_MAX_LEN() {
       final Topics topics1 = Topics.of(StringUtil.repeat('a', (int)Topics.TOPIC_MAX_LEN));
       assertNotNull(topics1);

       // too many
       assertThrows(IllegalArgumentException.class, () -> Topics.of(StringUtil.repeat('a', (int)Topics.TOPIC_MAX_LEN+1)));
    }

}
