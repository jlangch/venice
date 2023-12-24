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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class DAG_childOf_Test {

    @Test
    public void test_childOf_1() {
        final DAG<String> dag =
                new DAG<String>()
                        .addEdge("B", "A")      //       D
                        .addEdge("C", "B")      //      / \
                        .addEdge("D", "C")      //     C   G
                        .addEdge("F", "E")      //     | \ /
                        .addEdge("C", "F")      //     B  F
                        .addEdge("G", "F")      //     |  |
                        .addEdge("D", "G");     //     A  E

        assertTrue(dag.isChildOf("C", "D"));
        assertTrue(dag.isChildOf("B", "D"));
        assertTrue(dag.isChildOf("A", "D"));
        assertTrue(dag.isChildOf("G", "D"));
        assertTrue(dag.isChildOf("F", "D"));
        assertTrue(dag.isChildOf("E", "D"));

        assertTrue(dag.isChildOf("E", "C"));
        assertTrue(dag.isChildOf("E", "G"));

        assertFalse(dag.isChildOf("B", "G"));
        assertFalse(dag.isChildOf("A", "G"));
        assertFalse(dag.isChildOf("A", "F"));
        assertFalse(dag.isChildOf("A", "E"));
    }

}
