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
						"(dag/dag [\"A\" \"B\"] [\"B\" \"C\"])")
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
				ArityExceptions.assertMinArity(this, args, 1);

				final VncDAG dag = Coerce.toVncDAG(args.first());

				return dag.addEdges(args.rest());
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
						"(dag/topological-sort (dag/dag [\"A\" \"B\"] [\"B\" \"C\"]))")
					.seeAlso(
						"dag/dag", "dag/add-edges")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.topologicalSort();
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
				ArityExceptions.assertMinArity(this, args, 1);
		
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
						"dag/dag", "dag/add-edges", "dag/edges")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);
		
				final VncDAG dag = Coerce.toVncDAG(args.first());
				
				return dag.nodes();
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
					.add(add_edges)
					.add(edges)
					.add(nodes)
					.toMap();	
}
