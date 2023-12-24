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

import org.junit.jupiter.api.Test;


public class DAG_parents_Test {

    @Test
    public void test_parents_1() {
        final DAG<String> dag =
                new DAG<String>()
                        .addEdge("A", "B")      //       A
                        .addEdge("A", "C")      //      / \
                        .addEdge("B", "D")      //     B   C
                        .addEdge("C", "D")      //      \ /
                        .addEdge("D", "E")      //       D
                        .addEdge("D", "F");     //      / \
                                                //     E   F

        assertEquals("", String.join(" ", dag.parents("A")));

        assertEquals("A", String.join(" ", dag.parents("B")));

        assertEquals("A", String.join(" ", dag.parents("C")));

        assertEquals("B C A", String.join(" ", dag.parents("D")));

        assertEquals("D B C A", String.join(" ", dag.parents("E")));

        assertEquals("D B C A", String.join(" ", dag.parents("F")));
    }

    @Test
    public void test_parents_2() {
        final DAG<String> dag =
                new DAG<String>()
                        .addEdge("A", "B")      //       A
                        .addEdge("A", "C")      //      / \
                        .addEdge("B", "D")      //     B   C
                        .addEdge("C", "D")      //      \ / \
                        .addEdge("D", "E")      //       D   |
                        .addEdge("D", "F")      //      / \ /
                        .addEdge("C", "F");     //     E   F

        assertEquals("", String.join(" ", dag.parents("A")));

        assertEquals("A", String.join(" ", dag.parents("B")));

        assertEquals("A", String.join(" ", dag.parents("C")));

        assertEquals("B C A", String.join(" ", dag.parents("D")));

        assertEquals("D B C A", String.join(" ", dag.parents("E")));

        assertEquals("D C B A", String.join(" ", dag.parents("F")));
    }

    @Test
    public void test_direct_parents() {
        final DAG<String> dag =
                new DAG<String>()
                        .addEdge("A", "B")      //       A
                        .addEdge("A", "C")      //      / \
                        .addEdge("B", "D")      //     B   C
                        .addEdge("C", "D")      //      \ / \
                        .addEdge("D", "E")      //       D   |
                        .addEdge("D", "F")      //      / \ /
                        .addEdge("C", "F");     //     E   F

        assertEquals("", String.join(" ", dag.directParents("A")));

        assertEquals("A", String.join(" ", dag.directParents("B")));

        assertEquals("A", String.join(" ", dag.directParents("C")));

        assertEquals("B C", String.join(" ", dag.directParents("D")));

        assertEquals("D", String.join(" ", dag.directParents("E")));

        assertEquals("D C", String.join(" ", dag.directParents("F")));
    }

}
