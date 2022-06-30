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
     * DAG<String> dag = new DAG<>()
     *                        .addEdge("A", "B")
     *                        .addEdge("B", "C");
     *
     * List<String> sorted = dag.topologicalSort();
     * String path = String.join(" -> ", sorted); // "A -> B -> C"
     * </pre>
     */
    public DAG() {
    }

    private DAG(
            final Map<T, Node<T>> nodes,
            final Set<Edge<Node<T>>> edges
    ) {
        this.nodes.putAll(nodes);
        this.edges.addAll(edges);

        update();
    }

    public DAG<T> addNode(final T value) {
        if (value == null) {
            throw new IllegalArgumentException("A node value must not be null");
        }

        getNodeOrCreate(value);

        return new DAG<>(nodes, edges);
    }

    public DAG<T> addNodes(final List<T> values) {
        if (values != null) {
            for(T v : values) {
                getNodeOrCreate(v);
            }
        }

        return new DAG<>(nodes, edges);
    }

    public DAG<T> addEdge(final T parent, final T child) {
        addEdgeInternal(parent, child);

        return new DAG<>(nodes, edges);
    }

    public DAG<T> addEdges(final List<Edge<T>> edges) {
        if (edges != null) {
            for(Edge<T> e : edges) {
                addEdgeInternal(e.getParent(), e.getChild());
            }
        }

        return new DAG<>(nodes, this.edges);
    }

    public Node<T> getNode(final T value) {
        if (value == null) {
            throw new IllegalArgumentException("A node value must not be null");
        }

        return nodes.get(value);
    }

    public Collection<Node<T>> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public List<Edge<Node<T>>> getEdges() {
        return Collections.unmodifiableList(
                new ArrayList<>(edges));
    }

    public Collection<T> getValues() {
        return Collections.unmodifiableCollection(
                nodes.values()
                     .stream()
                     .map(n -> n.getValue())
                     .collect(Collectors.toList()));
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public Node<T> node(final T value) {
        if (value == null) {
            throw new IllegalArgumentException("A node value must not be null");
        }

        return nodes.get(value);
    }

    public List<T> children(final T value) {
        if (value == null) {
            throw new IllegalArgumentException("A node value must not be null");
        }

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

    public List<T> directChildren(final T value) {
        if (value == null) {
            throw new IllegalArgumentException("A node value must not be null");
        }

        final Node<T> node = nodes.get(value);
        if (node == null) {
            throw new NoSuchElementException("Node not found: " + value);
        }

        return Node.toValues(node.getChildren());
    }

    public List<T> parents(final T value) {
        if (value == null) {
            throw new IllegalArgumentException("A node value must not be null");
        }

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

    public List<T> directParents(final T value) {
        if (value == null) {
            throw new IllegalArgumentException("A node value must not be null");
        }

        final Node<T> node = nodes.get(value);
        if (node == null) {
            throw new NoSuchElementException("Node not found: " + value);
        }
        return Node.toValues(node.getParents());
    }

    public List<T> roots() {
        return Node.toValues(roots);
    }

    /**
     * Topological Sort using Kahn's algorithm.
     *
     * @return the sorted values
     *
     * @throws DagCycleException if cycle is found
     */
    public List<T> topologicalSort() throws DagCycleException {
        return new TopologicalSort<T>(edges, getIsolatedNodes()).sort();
    }

    public boolean isParentOf(final T parent, final T value)  {
        return parents(value).contains(parent);
    }

    public boolean isChildOf(final T child, final T value)  {
        return children(value).contains(child);
    }

    public boolean isNode(final T value)  {
        return nodes.containsKey(value);
    }

    public boolean isEdge(final T parent, final T child)  {
        return edges.contains(new Edge<Node<T>>(new Node<T>(parent), new Node<T>(child)));
    }

    @Override
    public String toString() {
        return String.format("DAG{nodes=%d}", nodes.size());
    }

    public List<Node<T>> getIsolatedNodes() {
        // nodes without parent and children
        return nodes.values()
                    .stream()
                    .filter(n -> n.isWithoutRelations())
                    .collect(Collectors.toList());
    }

    public Comparator<T> comparator() {
        final AtomicInteger idx = new AtomicInteger();
        final Map<T,Integer> map = new ConcurrentHashMap<>();
        topologicalSort().forEach(e -> map.put(e, idx.getAndIncrement()));

        return new Comparator<T>() {
            @Override
            public int compare(final T o1, final T o2) {
                return map.getOrDefault(o1, Integer.MAX_VALUE)
                          .compareTo(map.getOrDefault(o2, Integer.MAX_VALUE));
            }
        };
    }


    private Node<T> getNodeOrCreate(final T value) {
        return nodes.computeIfAbsent(value, v -> new Node<>(v));
    }

    private void update() throws DagCycleException {
        roots.clear();
        findRoots();
        checkForCycles();
    }

    private void addEdgeInternal(final T parent, final T child) {
        if (parent == null) {
            throw new IllegalArgumentException("A parent must not be null");
        }
        if (child == null) {
            throw new IllegalArgumentException("A child must not be null");
        }

        final Node<T> parentNode = getNodeOrCreate(parent);
        final Node<T> childNode = getNodeOrCreate(child);

        final Edge<Node<T>> edge = new Edge<>(parentNode, childNode);
        if (!edges.contains(edge)) {
            parentNode.addChild(childNode);
            edges.add(edge);
        }
    }

    private void findRoots() {
        for (Node<T> n : nodes.values()) {
            if (n.getParents().isEmpty()) {
                roots.add(n);
            }
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

    private String getPath(final List<Node<T>> path) {
        return path.stream()
                   .map(n -> String.valueOf(n.getValue()))
                   .collect(Collectors.joining(" -> "));
    }


    private final Map<T, Node<T>> nodes = new LinkedHashMap<>();
    private final List<Node<T>> roots = new ArrayList<>();
    private final Set<Edge<Node<T>>> edges = new LinkedHashSet<>();
}
