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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class DAG_cycles_Test {

	@Test
	public void test_cyles_1() {
		final DAG<String> dag = new DAG<>();
		
		assertThrows(DagCycleException.class, () -> dag.addEdge("A", "A"));
	}

	@Test
	public void test_cyles_2() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B"); 
		dag.addEdge("B", "A");
		
		assertThrows(DagCycleException.class, () -> dag.update());
	}

	@Test
	public void test_cycles_3() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //	   A  E
		dag.addEdge("B", "C");      //	   |  |
		dag.addEdge("C", "D");      //     B  F <--+
		dag.addEdge("E", "F");      //     | / \   |
		dag.addEdge("F", "C");      //     C   G   |
		dag.addEdge("F", "G");      //      \ /    |
		dag.addEdge("G", "D");      //       D-----+
		dag.addEdge("D", "F");
		
		dag.update();  // !! does not throw DagCycleException
		
		assertThrows(DagCycleException.class, () -> dag.topologicalSort());
	}

	@Test
	public void test_cycles_4() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //     A  C <--+
		dag.addEdge("B", "E");      //     | /     |
		dag.addEdge("C", "B");      //     B   D   |
		dag.addEdge("D", "E");      //      \ /    |
		dag.addEdge("E", "C");      //       E-----+
		
		dag.update();  // !! does not throw DagCycleException
		
		assertThrows(DagCycleException.class, () -> dag.topologicalSort());
	}

}
