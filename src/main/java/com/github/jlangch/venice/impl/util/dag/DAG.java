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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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

	public synchronized Node<T> node(final T value) {
		return nodes.get(value);
	}

	public synchronized List<T> children(final T value) {
		final Node<T> node = nodes.get(value);
		if (node == null) {
			throw new NoSuchElementException("Node not found: " + value);
		}
		
		final Set<Node<T>> children = new LinkedHashSet<>();
		final List<Node<T>> toVisit = new LinkedList<>(node.getChildren());
				
		while(!toVisit.isEmpty()) {
			final Node<T> n = toVisit.remove(0);
			if (!children.contains(n)) {
				children.add(n);
				toVisit.addAll(n.getChildren());
			}
		}
		
		return Node.toValues(children);
	}

	public synchronized List<T> parents(final T value) {
		final Node<T> node = nodes.get(value);
		if (node == null) {
			throw new NoSuchElementException("Node not found: " + value);
		}
		
		final Set<Node<T>> parents = new LinkedHashSet<>();
		final List<Node<T>> toVisit = new LinkedList<>(node.getParents());
				
		while(!toVisit.isEmpty()) {
			final Node<T> n = toVisit.remove(0);
			if (!parents.contains(n)) {
				parents.add(n);
				toVisit.addAll(n.getParents());
			}
		}
		
		return Node.toValues(parents);
	}

	public synchronized List<T> roots() {
		return Node.toValues(roots);
	}

	/**
	 * Topological Sort using Kahn's algorithm.
	 * 
	 * @return the sorted values
	 * 
	 * @throws DagCycleException if cycle is found
	 */
	public synchronized List<T> topologicalSort() throws DagCycleException {
		return new TopologicalSort<T>(edges, getIsolatedNodes()).sort();
	}

	public synchronized boolean isParentOf(final T parent, final T value)  {
		return parents(value).contains(parent);
	}

	public synchronized boolean isChildOf(final T child, final T value)  {
		return children(value).contains(child);
	}

	public synchronized boolean isNode(final T value)  {
		return nodes.containsKey(value);
	}

	@Override
	public synchronized String toString() {
		return String.format("DAG{nodes=%d}", nodes.size());
	}

	public Comparator<T> comparator() {
		final AtomicInteger idx = new AtomicInteger();
		final Map<T,Integer> map = new ConcurrentHashMap<>();
		topologicalSort().forEach(e -> map.put(e, idx.getAndIncrement()));
		
		return new Comparator<T>() {
			public int compare(final T o1, final T o2) {
				return map.getOrDefault(o1, Integer.MAX_VALUE)
						  .compareTo(map.getOrDefault(o2, Integer.MAX_VALUE));
			}			
		};
	}

	private void findRoots() {
		for (Node<T> n : nodes.values()) {
			if (n.getParents().isEmpty())
				roots.add(n);
		}
	}

	private void checkForCycles() throws DagCycleException {
		if (roots.isEmpty() && nodes.size() > 1) {
			throw new DagCycleException("No childless node found to be selected as root!");
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

	private List<Node<T>> getIsolatedNodes() {
		// nodes without parent and children
		return nodes.values()
					.stream()
					.filter(n -> n.isWithoutRelations())
					.collect(Collectors.toList());
	}
	
	
	private final Map<T, Node<T>> nodes = new LinkedHashMap<>();
	private final List<Node<T>> roots = new ArrayList<>();
	private final List<Edge<Node<T>>> edges = new ArrayList<>();
}