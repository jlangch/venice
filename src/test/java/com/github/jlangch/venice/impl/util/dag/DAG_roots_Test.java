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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;


public class DAG_roots_Test {

	@Test
	public void test_roots_1() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B"); 
		dag.update();
		
		final List<String> sorted = dag.roots();

		assertEquals("A", String.join(" ", sorted));
	}

	@Test
	public void test_roots_2() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");     //     A
		dag.addEdge("B", "C");     //     |
		dag.update();              //     B
		                           //     |
		                           //     C
		
		final List<String> sorted = dag.roots();

		assertEquals("A", String.join(" ", sorted));
	}

	@Test
	public void test_roots_3() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //       A
		dag.addEdge("B", "D");      //      / \
		dag.addEdge("A", "C");      //     B   C
		dag.addEdge("A", "D");      //      \ /
		dag.update();               //       D
		
		final List<String> sorted = dag.roots();

		assertEquals("A", String.join(" ", sorted));
	}

	@Test
	public void test_roots_4() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //     A   C
		dag.addEdge("B", "E");      //     |   |
		dag.addEdge("C", "D");      //     B   D
		dag.addEdge("D", "E");      //      \ /
		dag.update();               //       E
		
		final List<String> sorted = dag.roots();

		assertEquals("A C", String.join(" ", sorted));
	}

	@Test
	public void test_roots_5() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //     A  C
		dag.addEdge("B", "E");      //     | /
		dag.addEdge("C", "B");      //     B   D
		dag.addEdge("D", "E");      //      \ /
		dag.update();               //       E
		
		final List<String> sorted = dag.roots();

		assertEquals("A C D", String.join(" ", sorted));
	}
	
	@Test
	public void test_roots_6() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //	   A  E
		dag.addEdge("B", "C");      //	   |  |
		dag.addEdge("C", "D");      //     B  F
		dag.addEdge("E", "F");      //     | / \
		dag.addEdge("F", "C");      //     C   G
		dag.addEdge("F", "G");      //      \ /
		dag.addEdge("G", "D");      //       D
		dag.update();
		
		final List<String> sorted = dag.roots();

		assertEquals("A E", String.join(" ", sorted));
	}
	
	@Test
	public void test_roots_7() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //	     A
		dag.addEdge("A", "C");      //	    / \ 
		dag.addEdge("B", "D");      //     B   C
		dag.addEdge("C", "D");      //      \ /
		dag.addEdge("D", "E");      //       D 
		dag.addEdge("D", "F");      //      / \
		dag.update();               //     E   F
		
		final List<String> sorted = dag.roots();

		assertEquals("A", String.join(" ", sorted));
	}

	@Test
	public void test_roots_node_1() {
		final DAG<String> dag = new DAG<>();
		
		dag.addNode("A"); 
		dag.update();
		
		final List<String> roots = dag.roots();

		assertEquals("A", String.join(" ", roots));
	}


	@Test
	public void test_roots_node_2() {
		final DAG<String> dag = new DAG<>();
		
		dag.addNode("A"); 
		dag.addNode("B"); 
		dag.update();
		
		final List<String> roots = dag.roots();

		assertEquals("A B", String.join(" ", roots));
	}

}
