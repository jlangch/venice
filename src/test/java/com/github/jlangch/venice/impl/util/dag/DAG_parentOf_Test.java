/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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


public class DAG_parentOf_Test {

	@Test
	public void test_parentOf_1() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("B", "A");      //       D
		dag.addEdge("C", "B");      //      / \
		dag.addEdge("D", "C");      //     C   G
		dag.addEdge("F", "E");      //     | \ /
		dag.addEdge("C", "F");      //     B  F
		dag.addEdge("G", "F");      //	   |  |
		dag.addEdge("D", "G");      //	   A  E
		dag.update();
		
		assertTrue(dag.parentOf("D", "A"));
		assertTrue(dag.parentOf("D", "E"));
		
		assertTrue(dag.parentOf("C", "A"));
		assertTrue(dag.parentOf("C", "E"));
		
		assertTrue(dag.parentOf("B", "A"));
		assertFalse(dag.parentOf("B", "E"));
		
		assertTrue(dag.parentOf("G", "E"));
		assertFalse(dag.parentOf("G", "A"));
	}

}
