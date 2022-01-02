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
package com.github.jlangch.venice.impl.util.dag;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class DAG_cycles_Test {

	@Test
	public void test_cyles_1() {
		assertThrows(DagCycleException.class, () -> 
				new DAG<String>()
						.addEdge("A", "A"));
	}

	@Test
	public void test_cyles_2() {
		assertThrows(DagCycleException.class, () -> 
				new DAG<String>()
							.addEdge("A", "B")
							.addEdge("B", "A"));
	}

	@Test
	public void test_cycles_3() {
		assertThrows(DagCycleException.class, () -> 
				new DAG<String>()
						.addEdge("A", "B")      //     A  E
						.addEdge("B", "C")      //     |  |
						.addEdge("C", "D")      //     B  F <--+
						.addEdge("E", "F")      //     | / \   |
						.addEdge("F", "C")      //     C   G   |
						.addEdge("F", "G")      //      \ /    |
						.addEdge("G", "D")      //       D-----+
						.addEdge("D", "F")
						.topologicalSort());
	}

	@Test
	public void test_cycles_4() {
		assertThrows(DagCycleException.class, () ->
				new DAG<String>()
						.addEdge("A", "B")      //     A  C <--+
						.addEdge("B", "E")      //     | /     |
						.addEdge("C", "B")      //     B   D   |
						.addEdge("D", "E")      //      \ /    |
						.addEdge("E", "C")      //       E-----+
						.topologicalSort());
	}

}
