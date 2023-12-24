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
package com.github.jlangch.venice.impl.util.dag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;


public class DAG_node_Test {

    @Test
    public void test_node_1() {
        final DAG<String> dag =
                new DAG<String>()
                        .addEdge("A", "B");

        assertTrue(dag.isNode("A"));
        assertTrue(dag.isNode("B"));

        assertFalse(dag.isNode("C"));
    }

    @Test
    public void test_node_2() {
        final DAG<String> dag =
                new DAG<String>()
                        .addEdge("A", "B")     //     A
                        .addEdge("B", "C");    //     |
                                               //     B
                                               //     |
                                               //     C

        final Collection<Node<String>> nodes = dag.getNodes();

        assertEquals(3, nodes.size());

        assertTrue(nodes.contains(node("A")));
        assertTrue(nodes.contains(node("B")));

        assertTrue(nodes.contains(node("C")));
        assertFalse(nodes.contains(node("D")));
    }


    private static Node<String> node(final String n) {
        return new Node<String>(n);
    }
}
