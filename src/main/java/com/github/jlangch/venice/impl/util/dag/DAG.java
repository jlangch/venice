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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;


public class DAG<T> {

	/**
	 * Directed Acylic Graph
	 * 
	 * <pre>
	 * DAG<String> dag = new DAG<>();
	 * 
	 * dag.addEdge("A", "B");
	 * dag.addEdge("B", "C");
	 * dag.update();
	 * 		
	 * List<String> sorted = dag.topologicalSort();
	 * String path = String.join(" -> ", sorted); // "A -> B -> C"
	 * </pre>
	 */
	public DAG() {
	}

	/**
	 * Adds a node
	 *
	 * @param value the node's value
	 * @return the created node
	 */
	public synchronized Node<T> addNode(final T value) {
		return getNodeOrCreate(value);
	}

	public synchronized void addEdge(final T parent, final T child) {
		final Node<T> parentNode = getNodeOrCreate(parent);
		final Node<T> childNode = getNodeOrCreate(child);
		parentNode.addChild(childNode);
		
		edges.add(new Edge<>(parentNode, childNode));
	}

	/**
	 * Finds root nodes and checks for cycles
	 * 
	 * @throws DagCycleException if cycle is found
	 */
	public synchronized void update() throws DagCycleException {
		roots.clear();
		findRoots();
		checkForCycles();
	}

	public synchronized Node<T> getNode(final T value) {
		return nodes.get(value);
	}

	public synchronized Collection<Node<T>> getNodes() {
		return Collections.unmodifiableCollection(nodes.values());
	}
	
	public synchronized List<Edge<Node<T>>> getEdges() {
		return Collections.unmodifiableList(edges);
	}

	public synchronized Collection<T> getValues() {
		return Collections.unmodifiableCollection(
				nodes.values()
					 .stream()
					 .map(n -> n.getValue())
					 .collect(Collectors.toList()));
	}

	public synchronized int size() {
		return nodes.size();
	}

	public synchronized boolean isEmpty() {
		return nodes.isEmpty();
	}

	/**
	 * Topological Sort using Kahn's algorithm.
	 * 
	 * @return the sorted values
	 * 
	 * @throws DagCycleException if cycle is found
	 * 
	 * @see <a href="https://en.wikipedia.org/wiki/Topological_sorting">Topological Sorting</a>
	 * @see <a href="https://www.geeksforgeeks.org/topological-sorting-indegree-based-solution/">Topological Sorting</a>
	 * @see <a href="https://de.wikipedia.org/wiki/Topologische_Sortierung">Topological Sorting</a>
	 */
	public synchronized List<T> topologicalSort() throws DagCycleException {
		// --- Prepare Data ---------------------------------------------------

		final List<Node<T>> nodes = new ArrayList<>(this.nodes.values());

		// A list of lists to represent an adjacency list
		final Map<Node<T>,List<Node<T>>> adjList = new HashMap<>();

		// stores indegree of a vertex, defaults to 0
		final Map<Node<T>,Integer> indegree = new HashMap<>();

		for(Edge<Node<T>> e : edges) {
			// add an edge from source to destination
			if (!adjList.containsKey(e.getSrc())) {
				adjList.put(e.getSrc(), new ArrayList<Node<T>>());
			}			
			adjList.get(e.getSrc()).add(e.getDst());

			// increment in-degree of destination vertex by 1
			indegree.put(e.getDst(), indegree.getOrDefault(e.getDst(), 0) + 1);
		}
		
		for(Node<T> n : nodes) {
			if (!adjList.containsKey(n)) {
				adjList.put(n, new ArrayList<Node<T>>());
			}			
		}
		
			
		// --- Topological Sort -----------------------------------------------
		
		// list to store the sorted elements
		final List<Node<T>> sorted = new ArrayList<>();

		// Set of all nodes with no incoming edges
		final Stack<Node<T>> stack = new Stack<>();
		for (Node<T> node : nodes) {
			if (indegree.getOrDefault(node, 0) == 0) {
				stack.add(node);
			}
		}

		while (!stack.isEmpty()) {
			// remove node `n` from `stack`
			final Node<T> n = stack.pop();

			// add `n` at the tail of `sorted`
			sorted.add(n);

			for (Node<T> m : adjList.get(n)) {
				// remove an edge from `n` to `m` from the graph
				indegree.put(m, indegree.getOrDefault(m, 0) - 1);

				// if `m` has no other incoming edges, insert `m` into `stack`
				if (indegree.getOrDefault(m, 0) == 0) {
					stack.add(m);
				}
			}
		}

		// if a graph has edges, then the graph has at least one cycle
		for (Node<T> node : nodes) {
			if (indegree.getOrDefault(node, 0) != 0) {
				throw new DagCycleException("The graph has at least one cycle");
			}
		}

		return sorted.stream()
				.map(n -> n.getValue())
				.collect(Collectors.toList());
	}

	@Override
	public synchronized String toString() {
		return String.format("DAG{nodes=%d}", nodes.size());
	}


	private void findRoots() {
		for (Node<T> n : nodes.values()) {
			if (n.getParents().isEmpty())
				roots.add(n);
		}
	}

	private void checkForCycles() throws DagCycleException {
		if (roots.isEmpty() && nodes.size() > 1) {
			throw new DagCycleException("No childless node found to be selected as root");
		}
		
		final List<Node<T>> cycleCrawlerPath = new ArrayList<>();
		for (Node<T> n : roots) {
			checkForCycles(n, cycleCrawlerPath);
		}
	}

	private void checkForCycles(final Node<T> n, final List<Node<T>> path) {
		if (path.contains(n)) {
			path.add(n);
			throw new DagCycleException(
						getPath(path.subList(path.indexOf(n), path.size())));
		}
		path.add(n);
		n.getParents().forEach(node -> checkForCycles(node, path));
		path.remove(path.size() - 1);
	}
	
	private Node<T> getNodeOrCreate(final T value) {
		final Node<T> node = getNode(value);
		if (node != null) {
			return node;
		}
		else {
			final Node<T> n = new Node<>(value);
			nodes.put(value, n);
			return n;
		}
	}

	private String getPath(final List<Node<T>> path) {
		return path.stream()
				   .map(n -> String.valueOf(n.getValue()))
				   .collect(Collectors.joining(" -> "));
	}

	
	private final Map<T, Node<T>> nodes = new LinkedHashMap<>();
	private final List<Node<T>> roots = new ArrayList<>();
	private final List<Edge<Node<T>>> edges = new ArrayList<>();
}