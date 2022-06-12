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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * Topological Sort using Kahn's algorithm.
 *
 * @see <a href="https://www.baeldung.com/cs/dag-topological-sort">Topological Sorting 1</a>
 * @see <a href="https://www.baeldung.com/java-depth-first-search">Topological Sorting 2</a>
 * @see <a href="https://en.wikipedia.org/wiki/Topological_sorting">Topological Sorting 3</a>
 * @see <a href="https://www.geeksforgeeks.org/topological-sorting-indegree-based-solution/">Topological Sorting 4</a>
 * @see <a href="https://de.wikipedia.org/wiki/Topologische_Sortierung">Topological Sorting 5</a>
 */
public class TopologicalSort<T> {

    public TopologicalSort(
            final Collection<Edge<Node<T>>> edges,
            final List<Node<T>> isolatedNodes
    ) {
        this.edges.addAll(edges);

        // all nodes in the graph (isolated and edge nodes)
        final Set<Node<T>> nodes = new HashSet<>(isolatedNodes);
        for(Edge<Node<T>> e : edges) {
            nodes.add(e.getParent());
            nodes.add(e.getChild());
        }
        this.nodes = new ArrayList<>(nodes);
    }


    public List<T> sort() throws DagCycleException {
        if (nodes.isEmpty()) {
            throw new RuntimeException("The graph is empty!");
        }

        // --- Init Data ------------------------------------------------------

        // A list of lists to represent an adjacency list
        final Map<Node<T>,List<Node<T>>> adjList = new HashMap<>();
        nodes.forEach(n -> adjList.put(n, new ArrayList<Node<T>>()));

        // stores in-degree of a node, defaults to 0
        final Map<Node<T>,Integer> indegree = new HashMap<>();


        // --- Prepare adjacent list and in-degree counts ---------------------

        for(Edge<Node<T>> e : edges) {
            // add edge parent/child to the adjacency list
            adjList.get(e.getParent()).add(e.getChild());

            // increment in-degree of destination vertex by 1
            indegree.put(e.getChild(), indegree.getOrDefault(e.getChild(), 0) + 1);
        }


        // --- Sort ----------------------------------------------------------

        final List<Node<T>> sorted = new ArrayList<>();

        final Stack<Node<T>> stack = new Stack<>();

        // Put all nodes with no incoming edges (in-degree = 0) onto the stack
        nodes.stream()
             .filter(n -> indegree.getOrDefault(n, 0) == 0)
             .forEach(n -> stack.push(n));

        while (!stack.isEmpty()) {
            final Node<T> n = stack.pop();

            // add `n` at the tail of `sorted`
            sorted.add(n);

            for (Node<T> m : adjList.get(n)) {
                // remove an edge from `n` to `m` from the graph
                indegree.put(m, indegree.getOrDefault(m, 0) - 1);

                // if `m` has no other incoming edges, put `m` onto the `stack`
                if (indegree.getOrDefault(m, 0) == 0) {
                    stack.push(m);
                }
            }
        }

        // if there is a node left, then the graph has at least one cycle
        for (Node<T> node : nodes) {
            if (indegree.getOrDefault(node, 0) != 0) {
                throw new DagCycleException("The graph has at least one cycle!");
            }
        }

        return Node.toValues(sorted);
    }


    private final List<Edge<Node<T>>> edges = new ArrayList<>();
    private final List<Node<T>> nodes;
}
