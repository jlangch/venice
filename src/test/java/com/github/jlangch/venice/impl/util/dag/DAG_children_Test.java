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


public class DAG_children_Test {


	@Test
	public void test_children_1() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //	     A
		dag.addEdge("A", "C");      //	    / \ 
		dag.addEdge("B", "D");      //     B   C
		dag.addEdge("C", "D");      //      \ /
		dag.addEdge("D", "E");      //       D 
		dag.addEdge("D", "F");      //      / \
		dag.update();               //     E   F
		
		final List<String> sorted = dag.children("A");

		assertEquals("B C D E F", String.join(" ", sorted));
	}

	@Test
	public void test_children_2() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //	     A
		dag.addEdge("A", "C");      //	    / \ 
		dag.addEdge("B", "D");      //     B   C
		dag.addEdge("C", "D");      //      \ /
		dag.addEdge("D", "E");      //       D 
		dag.addEdge("D", "F");      //      / \
		dag.update();               //     E   F
		
		final List<String> sorted = dag.children("B");

		assertEquals("D E F", String.join(" ", sorted));
	}

	@Test
	public void test_children_3() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //	     A
		dag.addEdge("A", "C");      //	    / \ 
		dag.addEdge("B", "D");      //     B   C
		dag.addEdge("C", "D");      //      \ /
		dag.addEdge("D", "E");      //       D 
		dag.addEdge("D", "F");      //      / \
		dag.update();               //     E   F
		
		final List<String> sorted = dag.children("C");

		assertEquals("D E F", String.join(" ", sorted));
	}

	@Test
	public void test_children_4() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //	     A
		dag.addEdge("A", "C");      //	    / \ 
		dag.addEdge("B", "D");      //     B   C
		dag.addEdge("C", "D");      //      \ /
		dag.addEdge("D", "E");      //       D 
		dag.addEdge("D", "F");      //      / \
		dag.update();               //     E   F
		
		final List<String> sorted = dag.children("D");

		assertEquals("E F", String.join(" ", sorted));
	}

	@Test
	public void test_children_5() {
		final DAG<String> dag = new DAG<>();
		
		dag.addEdge("A", "B");      //	     A
		dag.addEdge("A", "C");      //	    / \ 
		dag.addEdge("B", "D");      //     B   C
		dag.addEdge("C", "D");      //      \ /
		dag.addEdge("D", "E");      //       D 
		dag.addEdge("D", "F");      //      / \
		dag.update();               //     E   F
		
		final List<String> sorted = dag.children("E");

		assertEquals("", String.join(" ", sorted));
	}

}