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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.Map;

import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncDAG;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;


public class DagFunctions {

	///////////////////////////////////////////////////////////////////////////
	// DAG (directed acyclic graph)
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction dag =
		new VncFunction(
				"dag/dag",
				VncFunction
					.meta()
					.arglists("(dag)", "(dag edges*)")
					.doc("Creates a new DAG (directed acyclic graph)")
					.examples(
						"(dag/dag)",
						"(dag/dag [\"A\" \"B\"] [\"B\" \"C\"])",
						"(dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"         [\"B\", \"C\"]  ;    |  |   \n" +
						"         [\"C\", \"D\"]  ;    B  F   \n" +
						"         [\"E\", \"F\"]  ;    | / \\ \n" +
						"         [\"F\", \"C\"]  ;    C    G \n" +
						"         [\"F\", \"G\"]  ;     \\  / \n" +
						"         [\"G\", \"D\"]) ;      D      ")
					.seeAlso(
						"dag/dag?", 
						"dag/add-edges", 
						"dag/topological-sort", 
						"dag/edges", 
						"dag/nodes",
						"empty?", 
						"count")
					.build()
		) {
			public VncVal apply(final VncList args) {
				final VncDAG dag = new VncDAG(Nil);
				
				return dag.addEdges(args);
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction dag_Q =
		new VncFunction(
				"dag/dag?",
				VncFunction
					.meta()
					.arglists("(dag? coll)")
					.doc("Returns true if coll is a DAG")
					.examples("(dag/dag? (dag/dag))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				return VncBoolean.of(Types.isVncDAG(args.first()));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction add_edges =
		new VncFunction(
				"dag/add-edges",
				VncFunction
					.meta()
					.arglists("(add-edges edges*)")
					.doc("Add edges to a DAG")
					.examples(
						"(dag/add-edges (dag/dag) [\"A\" \"B\"] [\"B\" \"C\"])")
					.seeAlso(
						"dag/dag", "dag/topological-sort")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				final VncDAG dag = Coerce.toVncDAG(args.first());

				return dag.addEdges(args.rest());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction add_node =
		new VncFunction(
				"dag/add-node",
				VncFunction
					.meta()
					.arglists("(add-node node)")
					.doc("Add a node to a DAG")
					.examples(
						"(dag/add-node (dag/dag) \"A\")")
					.seeAlso(
						"dag/dag", "dag/topological-sort")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				final VncDAG dag = Coerce.toVncDAG(args.first());

				return dag.addNode(args.second());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction topological_sort =
		new VncFunction(
				"dag/topological-sort",
				VncFunction
					.meta()
					.arglists("(topological-sort dag)")
					.doc("Topological sort of a DAG")
					.examples(
						"(dag/topological-sort (dag/dag [\"A\" \"B\"] [\"B\" \"C\"]))",
						"(-> (dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"             [\"B\", \"C\"]  ;    |  |   \n" +
						"             [\"C\", \"D\"]  ;    B  F   \n" +
						"             [\"E\", \"F\"]  ;    | / \\ \n" +
						"             [\"F\", \"C\"]  ;    C    G \n" +
						"             [\"F\", \"G\"]  ;     \\  / \n" +
						"             [\"G\", \"D\"]) ;      D    \n" +
						"    (dag/topological-sort))                ")
					.seeAlso(
						"dag/dag", "dag/compare-fn", "dag/add-edges")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.topologicalSort();
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction compare_fn =
		new VncFunction(
				"dag/compare-fn",
				VncFunction
					.meta()
					.arglists("(compare-fn dag)")
					.doc(
						"Returns a comparator fn which produces a topological sort " +
						"based on the dependencies in the graph. Nodes not present " +
						"in the graph will sort after nodes in the graph.")
					.examples(
						"(let [g (dag/dag [\"A\", \"B\"]   ;    A  E   \n" +
						"                 [\"B\", \"C\"]   ;    |  |   \n" +
						"                 [\"C\", \"D\"]   ;    B  F   \n" +
						"                 [\"E\", \"F\"]   ;    | / \\ \n" +
						"                 [\"F\", \"C\"]   ;    C    G \n" +
						"                 [\"F\", \"G\"]   ;     \\  / \n" +
						"                 [\"G\", \"D\"])] ;      D    \n" +
						"  (sort (dag/compare-fn g) [\"D\" \"F\" \"A\" \"Z\"])) ")
					.seeAlso(
						"dag/dag", "dag/topological-sort")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.compareFn();
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction edges =
		new VncFunction(
				"dag/edges",
				VncFunction
					.meta()
					.arglists("(edges dag)")
					.doc("Returns the edges of a DAG")
					.examples(
						"(dag/edges (dag/dag [\"A\" \"B\"] [\"B\" \"C\"]))")
					.seeAlso(
						"dag/dag", "dag/add-edges", "dag/nodes")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.edges();
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction nodes =
		new VncFunction(
				"dag/nodes",
				VncFunction
					.meta()
					.arglists("(nodes dag)")
					.doc("Returns the nodes of a DAG")
					.examples(
						"(dag/nodes (dag/dag [\"A\" \"B\"] [\"B\" \"C\"]))")
					.seeAlso(
						"dag/dag", "dag/node?", "dag/add-edges", "dag/edges")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.nodes();
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction children =
		new VncFunction(
				"dag/children",
				VncFunction
					.meta()
					.arglists("(children dag node)")
					.doc("Returns the child nodes")
					.examples(
						"(dag/children (dag/dag [\"A\" \"B\"] [\"B\" \"C\"]) \"A\")",
						"(-> (dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"             [\"B\", \"C\"]  ;    |  |   \n" +
						"             [\"C\", \"D\"]  ;    B  F   \n" +
						"             [\"E\", \"F\"]  ;    | / \\ \n" +
						"             [\"F\", \"C\"]  ;    C    G \n" +
						"             [\"F\", \"G\"]  ;     \\  / \n" +
						"             [\"G\", \"D\"]) ;      D    \n" +
						"    (dag/children \"F\"))                  ")
					.seeAlso(
						"dag/dag", "dag/immediate-children", "dag/parents", "dag/immediate-parents", "dag/roots")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.children(args.second());
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction immediate_children =
		new VncFunction(
				"dag/immediate-children",
				VncFunction
					.meta()
					.arglists("(immediate-children dag node)")
					.doc("Returns the immediate child nodes")
					.examples(
						"(dag/children (dag/dag [\"A\" \"B\"] [\"B\" \"C\"]) \"A\")",
						"(-> (dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"             [\"B\", \"C\"]  ;    |  |   \n" +
						"             [\"C\", \"D\"]  ;    B  F   \n" +
						"             [\"E\", \"F\"]  ;    | / \\ \n" +
						"             [\"F\", \"C\"]  ;    C    G \n" +
						"             [\"F\", \"G\"]  ;     \\  / \n" +
						"             [\"G\", \"D\"]) ;      D    \n" +
						"    (dag/children \"F\"))                  ")
					.seeAlso(
						"dag/dag", 
						"dag/children", 
						"dag/parents", "dag/immediate-parents", 
						"dag/roots")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.immediateChildren(args.second());
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction parents =
		new VncFunction(
				"dag/parents",
				VncFunction
					.meta()
					.arglists("(parents dag node)")
					.doc("Returns the parent nodes")
					.examples(
						"(dag/parents (dag/dag [\"A\" \"B\"] [\"B\" \"C\"]) \"C\")",
						"(-> (dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"             [\"B\", \"C\"]  ;    |  |   \n" +
						"             [\"C\", \"D\"]  ;    B  F   \n" +
						"             [\"E\", \"F\"]  ;    | / \\ \n" +
						"             [\"F\", \"C\"]  ;    C    G \n" +
						"             [\"F\", \"G\"]  ;     \\  / \n" +
						"             [\"G\", \"D\"]) ;      D    \n" +
						"    (dag/parents \"C\"))                   ")
					.seeAlso(
						"dag/dag", 
						"dag/immediate-parents", 
						"dag/children", "dag/immediate-children", 
						"dag/roots")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.parents(args.second());
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction immediate_parents =
		new VncFunction(
				"dag/immediate-parents",
				VncFunction
					.meta()
					.arglists("(immediate-parents dag node)")
					.doc("Returns the immediate parent nodes")
					.examples(
						"(dag/parents (dag/dag [\"A\" \"B\"] [\"B\" \"C\"]) \"C\")",
						"(-> (dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"             [\"B\", \"C\"]  ;    |  |   \n" +
						"             [\"C\", \"D\"]  ;    B  F   \n" +
						"             [\"E\", \"F\"]  ;    | / \\ \n" +
						"             [\"F\", \"C\"]  ;    C    G \n" +
						"             [\"F\", \"G\"]  ;     \\  / \n" +
						"             [\"G\", \"D\"]) ;      D    \n" +
						"    (dag/parents \"C\"))                   ")
					.seeAlso(
							"dag/dag", 
							"dag/parents", 
							"dag/children", "dag/immediate-children", 
							"dag/roots")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.immediateParents(args.second());
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction roots =
		new VncFunction(
				"dag/roots",
				VncFunction
					.meta()
					.arglists("(roots dag)")
					.doc("Returns the root nodes of a DAG")
					.examples(
						"(dag/roots (dag/dag [\"A\" \"B\"] [\"B\" \"C\"]))",
						"(-> (dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"             [\"B\", \"C\"]  ;    |  |   \n" +
						"             [\"C\", \"D\"]  ;    B  F   \n" +
						"             [\"E\", \"F\"]  ;    | / \\ \n" +
						"             [\"F\", \"C\"]  ;    C    G \n" +
						"             [\"F\", \"G\"]  ;     \\  / \n" +
						"             [\"G\", \"D\"]) ;      D    \n" +
						"    (dag/roots))                           ")
					.seeAlso(
						"dag/dag", "dag/parents", "dag/children")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.roots();
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction parent_of_Q =
		new VncFunction(
				"dag/parent-of?",
				VncFunction
					.meta()
					.arglists("(parent-of? dag p v)")
					.doc("Returns `true` if p is a transtive parent of v")
					.examples(
						"(-> (dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"             [\"B\", \"C\"]  ;    |  |   \n" +
						"             [\"C\", \"D\"]  ;    B  F   \n" +
						"             [\"E\", \"F\"]  ;    | / \\ \n" +
						"             [\"F\", \"C\"]  ;    C    G \n" +
						"             [\"F\", \"G\"]  ;     \\  / \n" +
						"             [\"G\", \"D\"]) ;      D    \n" +
						"    (dag/parent-of? \"E\" \"G\"))         ")
					.seeAlso(
						"dag/dag", "dag/parents", "dag/child-of?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 3);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.isParentOf(args.second(), args.third());
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction child_of_Q =
		new VncFunction(
				"dag/child-of?",
				VncFunction
					.meta()
					.arglists("(child-of? dag c v)")
					.doc("Returns `true` if c is a transtive child of v")
					.examples(
						"(-> (dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"             [\"B\", \"C\"]  ;    |  |   \n" +
						"             [\"C\", \"D\"]  ;    B  F   \n" +
						"             [\"E\", \"F\"]  ;    | / \\ \n" +
						"             [\"F\", \"C\"]  ;    C    G \n" +
						"             [\"F\", \"G\"]  ;     \\  / \n" +
						"             [\"G\", \"D\"]) ;      D    \n" +
						"    (dag/child-of? \"G\" \"E\"))         ")
					.seeAlso(
						"dag/dag", "dag/children", "dag/parent-of?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 3);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.isChildOf(args.second(), args.third());
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction node_Q =
		new VncFunction(
				"dag/node?",
				VncFunction
					.meta()
					.arglists("(node? dag v)")
					.doc("Returns `true` if v is a node in the DAG")
					.examples(
						"(-> (dag/dag [\"A\", \"B\"]  ;    A  E   \n" +
						"             [\"B\", \"C\"]  ;    |  |   \n" +
						"             [\"C\", \"D\"]  ;    B  F   \n" +
						"             [\"E\", \"F\"]  ;    | / \\ \n" +
						"             [\"F\", \"C\"]  ;    C    G \n" +
						"             [\"F\", \"G\"]  ;     \\  / \n" +
						"             [\"G\", \"D\"]) ;      D    \n" +
						"    (dag/node? \"G\"))                     ")
					.seeAlso(
						"dag/dag", "dag/nodes")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.isNode(args.second());
			}
		
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
		
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(dag)
					.add(dag_Q)
					.add(topological_sort)
					.add(compare_fn)
					.add(add_edges)
					.add(edges)
					.add(nodes)
					.add(children)
					.add(immediate_children)
					.add(parents)
					.add(immediate_parents)
					.add(roots)
					.add(parent_of_Q)
					.add(child_of_Q)
					.add(node_Q)
					.toMap();	
}
