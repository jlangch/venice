/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.types.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.dag.DAG;
import com.github.jlangch.venice.impl.util.dag.DagCycleException;
import com.github.jlangch.venice.impl.util.dag.Edge;


/**
 * DAG (directed acyclic graph)
 */
public class VncDAG extends VncCollection {

    public VncDAG(final VncVal meta) {
        super(meta);
        dag = new DAG<>();
    }

    private VncDAG(final DAG<VncVal> dag, final VncVal meta) {
        super(meta);
        this.dag = dag;
    }


    @Override
    public VncDAG withMeta(final VncVal meta) {
        return new VncDAG(dag, meta);
    }

    @Override
    public VncKeyword getType() {
        return new VncKeyword(
                        TYPE,
                        MetaUtil.typeMeta(
                            new VncKeyword(VncVal.TYPE)));
    }


    @Override
    public VncDAG emptyWithMeta() {
        return new VncDAG(getMeta());
    }

    public VncDAG addNode(final VncVal node) {
        try {
            return new VncDAG(dag.addNode(node), getMeta());
        }
        catch(DagCycleException ex) {
            throw new VncException("The edge is a cycle", ex);
        }
    }

    public VncDAG addEdge(final VncVal parent, final VncVal child) {
        try {
            return new VncDAG(dag.addEdge(parent, child), getMeta());
        }
        catch(DagCycleException ex) {
            throw new VncException("The edge is a cycle", ex);
        }
    }

    public VncDAG addNodes(final VncSequence nodes) {
        try {
            final List<VncVal> list = new ArrayList<>();
            nodes.forEach(n -> list.add(n));
            return new VncDAG(dag.addNodes(list), getMeta());
        }
        catch(DagCycleException ex) {
            throw new VncException("The edge is a cycle", ex);
        }
    }

    public VncDAG addEdges(final VncSequence edges) {
        try {
            final List<Edge<VncVal>> list = new ArrayList<>();

            edges.forEach(e -> {
                if (Types.isVncSequence(e)) {
                    final VncSequence nodes = (VncSequence)e;
                    if (nodes.size() == 2) {
                        list.add(new Edge<>(nodes.first(), nodes.second()));
                    }
                    else {
                        throw new VncException(String.format(
                                "Invalid DAG (directed acyclic graph) edge sequence with "
                                + "%d elements! Two sequence elements are required to "
                                + "define an edge, e.g.: [\"A\" \"B\"].",
                                nodes.size()));
                    }
                }
                else {
                    throw new VncException(String.format(
                            "%s is not allowed to pass a DAG (directed acyclic graph) edge! "
                            + "A sequence with two values (e.g.: [\"A\" \"B\"]) is required.",
                            Types.getType(e)));
                }
            });

            return new VncDAG(dag.addEdges(list), getMeta());
        }
        catch(DagCycleException ex) {
            throw new VncException("The edge is a cycle", ex);
        }
    }

    public VncList nodes() {
        return VncList.ofColl(
                dag.getNodes()
                   .stream()
                   .map(n -> n.getValue())
                   .collect(Collectors.toList()));
    }

    public VncList edges() {
        return VncList.ofColl(
                dag.getEdges()
                   .stream()
                   .map(e -> VncVector.of(e.getParent().getValue(), e.getChild().getValue()))
                   .collect(Collectors.toList()));
    }

    public VncList children(final VncVal val) {
        try {
            return VncList.ofColl(dag.children(val));
        }
        catch(NoSuchElementException ex) {
            throw new VncException("Node not found: " + val.toString(true));
        }
    }

    public VncList directChildren(final VncVal val) {
        try {
            return VncList.ofColl(dag.directChildren(val));
        }
        catch(NoSuchElementException ex) {
            throw new VncException("Node not found: " + val.toString(true));
        }
    }

    public VncList parents(final VncVal val) {
        try {
            return VncList.ofColl(dag.parents(val));
        }
        catch(NoSuchElementException ex) {
            throw new VncException("Node not found: " + val.toString(true));
        }
    }

    public VncList directParents(final VncVal val) {
        try {
            return VncList.ofColl(dag.directParents(val));
        }
        catch(NoSuchElementException ex) {
            throw new VncException("Node not found: " + val.toString(true));
        }
    }

    public VncList roots() {
        return VncList.ofColl(dag.roots());
    }

    public VncVector topologicalSort() {
        try {
            return VncVector.ofColl(dag.topologicalSort());
        }
        catch(DagCycleException ex) {
            throw new VncException("The graph has cycles!", ex);
        }
    }

    public VncFunction compareFn() {
        final Comparator<VncVal> c = dag.comparator();

        return new VncFunction(VncFunction.createAnonymousFuncName("toposort-compare")) {
            @Override
            public VncVal apply(final VncList args) {
                return new VncLong(c.compare(args.first(), args.second()));
            }
            private static final long serialVersionUID = 1L;
        };
    }

    public VncBoolean isParentOf(final VncVal parent, final VncVal value) {
        try {
            return VncBoolean.of(dag.isParentOf(parent, value));
        }
        catch(NoSuchElementException ex) {
            throw new VncException("Node not found!");
        }
    }

    public VncBoolean isChildOf(final VncVal child, final VncVal value) {
        try {
            return VncBoolean.of(dag.isChildOf(child, value));
        }
        catch(NoSuchElementException ex) {
            throw new VncException("Node not found!");
        }
    }

    public VncBoolean isNode(final VncVal value) {
        return VncBoolean.of(dag.isNode(value));
    }

    public VncBoolean isEdge(final VncVal parent, final VncVal child) {
        return VncBoolean.of(dag.isEdge(parent, child));
    }

    @Override
    public VncList toVncList() {
        return VncList.ofColl(dag.getValues());
    }

    @Override
    public VncVector toVncVector() {
        return VncVector.ofColl(dag.getValues());
    }

    @Override
    public int size() {
        return dag.size();
    }

    @Override
    public boolean isEmpty() {
        return dag.isEmpty();
    }


    @Override
    public TypeRank typeRank() {
        return TypeRank.DAG;
    }

    @Override
    public Object convertToJavaObject() {
        // return a list of the edges. an edge is list of the parent and client
        // value
        return dag.getEdges()
                  .stream()
                  .map(e -> Arrays.asList(
                                  e.getParent()
                                   .getValue()
                                   .convertToJavaObject(),
                                  e.getChild()
                                   .getValue()
                                   .convertToJavaObject()))
                  .collect(Collectors.toList());
    }

    @Override
    public int compareTo(final VncVal o) {
        return dag == ((VncDAG)o).dag ? 0 : -1; // limited compare!
    }

    @Override
    public int hashCode() {
        return dag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        VncDAG other = (VncDAG) obj;
        return dag.equals(other.dag);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        final VncList elements = getIsolatedNodes().addAllAtEnd(edges());

        return "(" + Printer.join(elements, " ", print_machine_readably) + ")";
    }

    private VncList getIsolatedNodes() {
        return VncList.ofColl(
                dag.getIsolatedNodes()
                   .stream()
                   .map(n -> n.getValue())
                   .collect(Collectors.toList()));
    }


    public static final String TYPE = ":dag/dag";

    private static final long serialVersionUID = -1848883965231344442L;

    private final DAG<VncVal> dag;
}
