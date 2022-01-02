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

import java.util.List;

import org.junit.jupiter.api.Test;


public class DAG_roots_Test {

	@Test
	public void test_roots_1() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B"); 
		
		final List<String> sorted = dag.roots();

		assertEquals("A", String.join(" ", sorted));
	}

	@Test
	public void test_roots_2() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")     //     A
						.addEdge("B", "C");    //     |
						                       //     B
						                       //     |
						                       //     C
		
		final List<String> sorted = dag.roots();

		assertEquals("A", String.join(" ", sorted));
	}

	@Test
	public void test_roots_3() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //       A
						.addEdge("B", "D")      //      / \
						.addEdge("A", "C")      //     B   C
						.addEdge("A", "D");     //      \ /
						                        //       D
		
		final List<String> sorted = dag.roots();

		assertEquals("A", String.join(" ", sorted));
	}

	@Test
	public void test_roots_4() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //     A   C
						.addEdge("B", "E")      //     |   |
						.addEdge("C", "D")      //     B   D
						.addEdge("D", "E");     //      \ /
						                        //       E
		
		final List<String> sorted = dag.roots();

		assertEquals("A C", String.join(" ", sorted));
	}

	@Test
	public void test_roots_5() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //     A  C
						.addEdge("B", "E")      //     | /
						.addEdge("C", "B")      //     B   D
						.addEdge("D", "E");     //      \ /
						                        //       E
		
		final List<String> sorted = dag.roots();

		assertEquals("A C D", String.join(" ", sorted));
	}
	
	@Test
	public void test_roots_6() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //     A  E
						.addEdge("B", "C")      //     |  |
						.addEdge("C", "D")      //     B  F
						.addEdge("E", "F")      //     | / \
						.addEdge("F", "C")      //     C   G
						.addEdge("F", "G")      //      \ /
						.addEdge("G", "D");      //       D
		
		final List<String> sorted = dag.roots();

		assertEquals("A E", String.join(" ", sorted));
	}
	
	@Test
	public void test_roots_7() {
		final DAG<String> dag = 
				new DAG<String>()
						.addEdge("A", "B")      //       A
						.addEdge("A", "C")      //      / \ 
						.addEdge("B", "D")      //     B   C
						.addEdge("C", "D")      //      \ /
						.addEdge("D", "E")      //       D 
						.addEdge("D", "F");     //      / \
						                        //     E   F
		
		final List<String> sorted = dag.roots();

		assertEquals("A", String.join(" ", sorted));
	}

	@Test
	public void test_roots_node_1() {
		final DAG<String> dag = 
				new DAG<String>().addNode("A"); 
		
		final List<String> roots = dag.roots();

		assertEquals("A", String.join(" ", roots));
	}


	@Test
	public void test_roots_node_2() {
		final DAG<String> dag = 
				new DAG<String>()
						.addNode("A") 
						.addNode("B"); 
		
		final List<String> roots = dag.roots();

		assertEquals("A B", String.join(" ", roots));
	}

}
