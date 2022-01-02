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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;


public class DAG_topologicalSort_Test {

	@Test
	public void test_topologicalSort_1() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B"); 
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("A B", String.join(" ", sorted));
	}

	@Test
	public void test_topologicalSort_2() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")     //     A
						.addEdge("B", "C");    //     |
						                       //     B
						                       //     |
						                       //     C
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("A B C", String.join(" ", sorted));
	}

	@Test
	public void test_topologicalSort_3() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //       A
						.addEdge("B", "D")      //      / \
						.addEdge("A", "C")      //     B   C
						.addEdge("A", "D");     //      \ /
						                        //       D
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("A C B D", String.join(" ", sorted));
	}

	@Test
	public void test_topologicalSort_4() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //     A   C
						.addEdge("B", "E")      //     |   |
						.addEdge("C", "D")      //     B   D
						.addEdge("D", "E");     //      \ /
						                        //       E
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("C D A B E", String.join(" ", sorted));
	}

	@Test
	public void test_topologicalSort_5() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //     A  C
						.addEdge("B", "E")      //     | /
						.addEdge("C", "B")      //     B   D
						.addEdge("D", "E");     //      \ /
						                        //       E
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("D C A B E", String.join(" ", sorted));
	}
	
	@Test
	public void test_topologicalSort_6a() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //     A  E
						.addEdge("B", "C")      //     |  |
						.addEdge("C", "D")      //     B  F
						.addEdge("E", "F")      //     | / \
						.addEdge("F", "C")      //     C   G
						.addEdge("F", "G")      //      \ /
						.addEdge("G", "D");     //       D
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("E F G A B C D", String.join(" ", sorted));
	}
	
	@Test
	public void test_topologicalSort_6b() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("B", "A")      //       D
						.addEdge("C", "B")      //      / \
						.addEdge("D", "C")      //     C   G
						.addEdge("F", "E")      //     | \ /
						.addEdge("C", "F")      //     B  F
						.addEdge("G", "F")      //     |  |
						.addEdge("D", "G");     //     A  E
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("D G C F E B A", String.join(" ", sorted));
	}
	
	@Test
	public void test_topologicalSort_7a() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //       A
						.addEdge("A", "C")      //      / \ 
						.addEdge("B", "D")      //     B   C
						.addEdge("C", "D")      //      \ /
						.addEdge("D", "E")      //       D 
						.addEdge("D", "F");     //      / \
						                        //     E   F
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("A C B D F E", String.join(" ", sorted));
	}
	
	@Test
	public void test_topologicalSort_7b() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("B", "A")      //     E   F
						.addEdge("C", "A")      //      \ /
						.addEdge("D", "B")      //       D 
						.addEdge("D", "C")      //      / \
						.addEdge("E", "D")      //     B   C 
						.addEdge("F", "D");     //      \ /
						                        //       A
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("F E D C B A", String.join(" ", sorted));
	}

	
	@Test
	public void test_topologicalSort_8() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //       A       Z
						.addEdge("A", "C")      //      / \ 
						.addEdge("B", "D")      //     B   C
						.addEdge("C", "D")      //      \ /
						.addEdge("D", "E")      //       D 
						.addEdge("D", "F")      //      / \
						.addNode("Z");          //     E   F
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("Z A C B D F E", String.join(" ", sorted));
	}


	@Test
	public void test_topologicalSort_9() {
		final DAG<String> dag = 
				new DAG<String>()
						.addNode("A") 
						.addNode("B"); 
		
		final List<String> sorted = dag.topologicalSort();

		assertEquals("B A", String.join(" ", sorted));
	}

	@Test
	public void test_comparator_1() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //     A  E
						.addEdge("B", "C")      //     |  |
						.addEdge("C", "D")      //     B  F
						.addEdge("E", "F")      //     | / \
						.addEdge("F", "C")      //     C   G
						.addEdge("F", "G")      //      \ /
						.addEdge("G", "D");      //       D
		
		final List<String> sorted = Arrays.asList("D", "F", "A", "Z")
										  .stream()
										  .sorted(dag.comparator())
										  .collect(Collectors.toList());

		assertEquals("F A D Z", String.join(" ", sorted));
	}
}
