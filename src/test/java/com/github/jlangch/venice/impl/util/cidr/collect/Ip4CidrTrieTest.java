/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.util.cidr.collect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.cidr.CIDR;


public class Ip4CidrTrieTest {

    @Test
    public void test() {
        final CidrTrie<String> trie = new CidrTrie<>();

        assertEquals(0, trie.size());

        final CIDR cidr = CIDR.parse("192.16.10.0/24");

        trie.insert(cidr, cidr.getNotation());

        assertEquals(1, trie.size());

        assertEquals("192.16.10.0/24", trie.getValue(CIDR.parse("192.16.10.0")));
        assertEquals("192.16.10.0/24", trie.getValue(CIDR.parse("192.16.10.100")));
        assertEquals("192.16.10.0/24", trie.getValue(CIDR.parse("192.16.10.255")));

        assertEquals("192.16.10.0/24", trie.getCIDR("192.16.10.100").getNotation());

        assertNull(trie.getValue(CIDR.parse("0.0.0.0")));
        assertNull(trie.getValue(CIDR.parse("20.10.0.0")));
        assertNull(trie.getValue(CIDR.parse("192.16.9.255")));
        assertNull(trie.getValue(CIDR.parse("192.16.11.0")));
        assertNull(trie.getValue(CIDR.parse("192.16.12.0")));
        assertNull(trie.getValue(CIDR.parse("200.16.0.9")));
        assertNull(trie.getValue(CIDR.parse("255.255.255.255")));
    }

}
