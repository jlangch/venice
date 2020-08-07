/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.removeNilValues;
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMutableList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.transducer.Reduced;


public class TransducerFunctions {

	public static VncFunction transduce =
		new VncFunction(
				"transduce",
				VncFunction
					.meta()
					.arglists(
						"(transduce xform f coll)",
						"(transduce xform f init coll)")
					.doc(
						"Reduce with a transformation of a reduction function f (xf). If init is not " +
						"supplied, (f) will be called to produce it. f should be a reducing " +
						"step function that accepts both 1 and 2 arguments. Returns the result " +
						"of applying (the transformed) xf to init and the first item in coll, " +
						"then applying xf to that result and the 2nd item, etc. If coll " +
						"contains no items, returns init and f is not called.")
					.examples(
						"(do                                       \n" +
						"  (def xform (map #(+ % 1)))              \n" +
						"  (transduce xform + [1 2 3 4]))            ",
						
						"(do                                       \n" +
						"  (def xform (map #(+ % 1)))              \n" +
						"  (transduce xform conj [1 2 3 4]))         ",
						
						"(do                                       \n" +
						"  (def xform (comp (drop 2) (take 3)))    \n" +
						"  (transduce xform conj [1 2 3 4 5 6]))     ",
						
						"(do                                       \n" +
						"  (def xform (comp                        \n" +
						"              (map #(* % 10))             \n" +
						"              (map #(- % 5))              \n" +
						"              (sorted compare)            \n" +
						"              (drop 3)                    \n" +
						"              (take 2)                    \n" +
						"              (reverse)))                 \n" +
						"  (def coll [5 2 1 6 4 3])                \n" +
						"  (str (transduce xform conj coll)))        ")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("transduce", args, 3, 4);

				// (def xform (drop 2))
				// (transduce xform + [1 2 3 4])  ;; => 7
				// (transduce xform conj [1 2 3 4])  ;; => [3 4]

				final VncFunction xform = Coerce.toVncFunction(args.first());
				final VncFunction reduction_fn = Coerce.toVncFunction(args.second());
				final VncSequence coll = coerceToSequence(args.last());

				final VncVal init = args.size() == 4
										? args.third()
										: reduction_fn.apply(VncList.empty());


				final VncFunction xf = (VncFunction)xform.apply(VncList.of(reduction_fn));

				// reduce the collection
				final VncVal ret = CoreFunctions.reduce.apply(VncList.of(xf, init, coll));

				// cleanup
				return Reduced.unreduced(xf.apply(VncList.of(ret)));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction reduced =
		new VncFunction(
				"reduced",
				VncFunction
					.meta()
					.arglists("(reduced x)")
					.doc("Wraps x in a way such that a reduce will terminate with the value x.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("reduced", args, 1);

				return Reduced.reduced(args.first());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction reduced_Q =
		new VncFunction(
				"reduced?",
				VncFunction
					.meta()
					.arglists("(reduced? x)")
					.doc("Returns true if x is the result of a call to reduced.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("reduced?", args, 1);

				return VncBoolean.of(Reduced.isReduced(args.first()));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction map =
		new VncFunction(
				"map",
				VncFunction
					.meta()
					.arglists("(map f coll colls*)")
					.doc(
						"Applys f to the set of first items of each coll, followed by applying " +
						"f to the set of second items in each coll, until any one of the colls " +
						"is exhausted. Any remaining items in other colls are ignored. " +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(map inc [1 2 3 4])",
						"(map + [1 2 3 4] [10 20 30 40])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				final MeterRegistry meterRegistry = JavaInterop.getInterceptor().getMeterRegistry();

				if (args.size() == 0) {
					return Nil;
				}
				else if (args.size() == 1) {
					final IVncFunction fn = Coerce.toIVncFunction(args.first());

					// return a transducer
					return new VncFunction(createAnonymousFuncName("map:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final IVncFunction rf = Coerce.toIVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("map:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										return rf.apply(VncList.of(result, fn.apply(VncList.of(input))));
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					final IVncFunction fn = Coerce.toIVncFunction(args.first());
					final VncList lists = removeNilValues((VncList)args.rest());
					final List<VncVal> result = new ArrayList<>();

					if (lists.isEmpty()) {
						return Nil;
					}
					else if (lists.size() == 1) {
						// optimized mapper for a single collection
						return coerceToSequence(lists.first())
								.map(v -> VncFunction.applyWithMeter(
															fn, 
															VncList.of(v),
															meterRegistry))
								.toVncList();
					}
					else {
						// mapper with multiple collections
						int index = 0;
						boolean hasMore = true;
						while(hasMore) {
							final List<VncVal> fnArgs = new ArrayList<>();
	
							for(int ii=0; ii<lists.size(); ii++) {
								VncVal seq = lists.nth(ii);							
								final VncSequence nthList = coerceToSequence(seq);
								if (nthList.size() > index) {
									fnArgs.add(nthList.nth(index));
								}
								else {
									hasMore = false;
									break;
								}
							}
	
							if (hasMore) {
								final VncVal val = VncFunction.applyWithMeter(fn, VncList.ofList(fnArgs), meterRegistry);
								result.add(val);
								index += 1;
							}
						}
	
						return VncList.ofList(result);
					}
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction map_indexed =
		new VncFunction(
				"map-indexed",
				VncFunction
					.meta()
					.arglists("(map-indexed f coll)")
					.doc(
						"Retruns a collection of applying f to 0 and the first item of " +
						"coll, followed by applying f to 1 and the second item of coll, etc. " +
						"until coll is exhausted. " +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(map-indexed (fn [idx val] [idx val]) [:a :b :c])",
						"(map-indexed vector [:a :b :c])",
						"(map-indexed vector \"abcdef\")",
						"(map-indexed hash-map [:a :b :c])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				final MeterRegistry meterRegistry = JavaInterop.getInterceptor().getMeterRegistry();

				if (args.size() == 0) {
					return Nil;
				}
				else if (args.size() == 1) {
					final IVncFunction fn = Coerce.toIVncFunction(args.first());

					// return a transducer
					return new VncFunction(createAnonymousFuncName("map-indexed:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicLong idx = new AtomicLong(0);

							return new VncFunction(createAnonymousFuncName("map-indexed:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();
			
										return rf.apply(VncList.of(
															result, 
															fn.apply(VncList.of(
																		new VncLong(idx.getAndIncrement()), 
																		input))));
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					final VncFunction fn = Coerce.toVncFunction(args.first());
					final VncVal coll = args.second();

					List<VncVal> items;
					
					if (Types.isVncSequence(coll)) {
						items = ((VncSequence)coll).getList();
					}
					else if (Types.isVncSet(coll)) {
						items = ((VncSet)coll).getList();
					}
					else if (Types.isVncMap(coll)) {
						items = ((VncMap)coll).toVncList().getList();
					}
					else if (Types.isVncString(coll)) {
						items = ((VncString)coll).toVncList().getList();
					}
					else {
						throw new VncException(
								"Function 'map-indexed' requires a list, vector, set, " +
								"map, or string as coll argument.");
					}
					

					final List<VncVal> list = new ArrayList<>();
					int index = 0;
					
					for(VncVal v : items) {
						list.add(VncFunction.applyWithMeter(fn, VncList.of(new VncLong(index++), v), meterRegistry));
					}

					return VncList.ofList(list);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction filter =
		new VncFunction(
				"filter",
				VncFunction
					.meta()
					.arglists("(filter predicate coll)")
					.doc(
						"Returns a collection of the items in coll for which " +
						"(predicate item) returns logical true. " +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(filter even? [1 2 3 4 5 6 7])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("filter", args, 1, 2);

				final MeterRegistry meterRegistry = JavaInterop.getInterceptor().getMeterRegistry();

				final IVncFunction predicate = Coerce.toIVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("filter:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("filter:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										final VncVal cond = predicate.apply(VncList.of(input));
										return (!VncBoolean.isFalse(cond) && cond != Nil)
													? rf.apply(VncList.of(result, input))
													: result;
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					return coerceToSequence(args.second())
							.filter(v -> {
									final VncVal keep = VncFunction.applyWithMeter(
															predicate, 
															VncList.of(v),
															meterRegistry);
									return !VncBoolean.isFalse(keep) && keep != Nil;
								});
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction drop =
		new VncFunction(
				"drop",
				VncFunction
					.meta()
					.arglists("(drop n coll)")
					.doc(
						"Returns a collection of all but the first n items in coll. " +
						"Returns a stateful transducer when no collection is provided.")
					.examples("(drop 3 [1 2 3 4 5])", "(drop 10 [1 2 3 4 5])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("drop", args, 1, 2);

				if (args.size() == 1) {
					final long n = Coerce.toVncLong(args.first()).getValue();

					// return a transducer
					return new VncFunction(createAnonymousFuncName("drop:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicLong nn = new AtomicLong(n);

							return new VncFunction(createAnonymousFuncName("drop:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										if (nn.getAndDecrement() > 0) {
											return result;
										}
										else {
											return rf.apply(VncList.of(result, input));
										}
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					final VncLong n = Coerce.toVncLong(args.first());
					final VncSequence coll = coerceToSequence(args.second());

					return coll.slice((int)Math.min(n.getValue(), coll.size()));
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction drop_while =
		new VncFunction(
				"drop-while",
				VncFunction
					.meta()
					.arglists("(drop-while predicate coll)")
					.doc(
						"Returns a list of the items in coll starting from the " +
						"first item for which (predicate item) returns logical false. " +
						"Returns a stateful transducer when no collection is provided.")
					.examples("(drop-while neg? [-2 -1 0 1 2 3])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("drop-while", args, 1, 2);

				final MeterRegistry meterRegistry = JavaInterop.getInterceptor().getMeterRegistry();

				final IVncFunction predicate = Coerce.toIVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("drop-while:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicBoolean take = new AtomicBoolean(false);

							return new VncFunction(createAnonymousFuncName("drop-while:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										if (take.get()) {
											return rf.apply(VncList.of(result, input));
										}
										else {
											final VncVal drop = predicate.apply(VncList.of(input));
											if (VncBoolean.isTrue(drop)) {
												return result;
											}
											else {
												take.set(true);
												return rf.apply(VncList.of(result, input));
											}
										}
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					final VncSequence coll = coerceToSequence(args.second());

					for(int i=0; i<coll.size(); i++) {
						final VncVal take = VncFunction.applyWithMeter(predicate, VncList.of(coll.nth(i)), meterRegistry);
						if (VncBoolean.isFalse(take)) {
							return coll.slice(i);
						}
					}
					return coll.emptyWithMeta();
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction take =
		new VncFunction(
				"take",
				VncFunction
					.meta()
					.arglists("(take n coll)")
					.doc(
						"Returns a collection of the first n items in coll, or all items if " +
						"there are fewer than n. " +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(take 3 [1 2 3 4 5])",
						"(take 10 [1 2 3 4 5])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("take", args, 1, 2);

				if (args.size() == 1) {
					final long n = Coerce.toVncLong(args.first()).getValue();

					// return a transducer
					return new VncFunction(createAnonymousFuncName("take:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicLong nn = new AtomicLong(n);

							return new VncFunction(createAnonymousFuncName("take:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										if (nn.getAndDecrement() > 0) {
											return rf.apply(VncList.of(result, input));
										}
										else {
											return Reduced.reduced(result);
										}
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};

				}
				else {
					final VncLong n = Coerce.toVncLong(args.first());
					final VncSequence coll = coerceToSequence(args.second());

					return coll.slice(0, n.getValue().intValue());
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction take_while =
		new VncFunction(
				"take-while",
				VncFunction
					.meta()
					.arglists("(take-while predicate coll)")
					.doc(
						"Returns a list of successive items from coll while " +
						"(predicate item) returns logical true. " +
						"Returns a transducer when no collection is provided.")
					.examples("(take-while neg? [-2 -1 0 1 2 3])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("take-while", args, 1, 2);

				final MeterRegistry meterRegistry = JavaInterop.getInterceptor().getMeterRegistry();

				final IVncFunction predicate = Coerce.toIVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("take-while:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("take-while:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										final VncVal take = predicate.apply(VncList.of(input));
										if (VncBoolean.isTrue(take)) {
											return rf.apply(VncList.of(result, input));
										}
										else {
											return Reduced.reduced(result);
										}
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};

				}
				else {
					final VncSequence coll = coerceToSequence(args.second());

					for(int i=0; i<coll.size(); i++) {
						final VncVal take = VncFunction.applyWithMeter(predicate, VncList.of(coll.nth(i)), meterRegistry);
						if (VncBoolean.isFalse(take)) {
							return coll.slice(0, i);
						}
					}
					return coll;
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction keep =
		new VncFunction(
				"keep",
				VncFunction
					.meta()
					.arglists("(keep f coll)")
					.doc(
						"Returns a sequence of the non-nil results of (f item). Note, " +
						"this means false return values will be included. f must be " +
						"free of side-effects. " +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(keep even? (range 1 4))",
						"(keep (fn [x] (if (odd? x) x)) (range 4))",
						"(keep #{3 5 7} '(1 3 5 7 9))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("keep", args, 1, 2);

				if (args.size() == 1) {
					final IVncFunction fn = Coerce.toIVncFunction(args.first());

					// return a transducer
					return new VncFunction(createAnonymousFuncName("keep:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("keep:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										final VncVal val = fn.apply(VncList.of(input));
										return val == Nil || VncBoolean.isFalse(val) ? result : rf.apply(VncList.of(result, input));
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};

				}
				else {
					// use 'map' to apply the mapping function
					final VncVal result = TransducerFunctions.map.apply(args);

					return result == Nil ? Nil : removeNilValues(Coerce.toVncList(result));
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction dedupe =
		new VncFunction(
				"dedupe",
				VncFunction
					.meta()
					.arglists("(dedupe coll)")
					.doc(
						"Returns a collection with all consecutive duplicates removed. " +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(dedupe [1 2 2 2 3 4 4 2 3])",
						"(dedupe '(1 2 2 2 3 4 4 2 3))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("dedupe", args, 0, 1);

				if (args.isEmpty()) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("dedupe:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicReference<VncVal> seen = new AtomicReference<>(NONE);

							return new VncFunction(createAnonymousFuncName("dedupe:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										if (!input.equals(seen.get())) {
											seen.set(input);
											return rf.apply(VncList.of(result, input));
										}
										else {
											return result;
										}
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					if (args.first() == Nil) {
						return VncList.empty();
					}

					VncVal seen = NONE;

					final List<VncVal> items = new ArrayList<>();

					for(VncVal val : coerceToSequence(args.first()).getList()) {
						if (!val.equals(seen)) {
							items.add(val);
							seen = val;
						}
					}

					return ((VncSequence)args.first()).withValues(items);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction remove =
		new VncFunction(
				"remove",
				VncFunction
					.meta()
					.arglists("(remove predicate coll)")
					.doc(
						"Returns a collection of the items in coll for which " +
						"(predicate item) returns logical false. " +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(remove even? [1 2 3 4 5 6 7])",
						"(remove #{3 5} '(1 3 5 7 9))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("remove", args, 1, 2);

				final MeterRegistry meterRegistry = JavaInterop.getInterceptor().getMeterRegistry();

				final IVncFunction predicate = Coerce.toIVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					final VncFunction fn =
							new VncFunction(createAnonymousFuncName("remove:transducer")) {
								public VncVal apply(final VncList args) {
									return VncBoolean.of(!VncBoolean.isTrue(predicate.apply(args)));
								}
								private static final long serialVersionUID = -1;
							};
					return filter.apply(VncList.of(fn));
				}
				else {
					final VncSequence coll = coerceToSequence(args.second());
					final List<VncVal> items = new ArrayList<>();
					
					for(VncVal val : coll.getList()) {
						final VncVal keep = VncFunction.applyWithMeter(predicate, VncList.of(val), meterRegistry);
						if (keep == Nil || VncBoolean.isFalse(keep)) {
							items.add(val);
						}
					}

					return coll.withValues(items);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction distinct =
		new VncFunction(
				"distinct",
				VncFunction
					.meta()
					.arglists("(distinct coll)")
					.doc(
						"Returns a collection with all duplicates removed. " +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(distinct [1 2 3 4 2 3 4])",
						"(distinct '(1 2 3 4 2 3 4))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("distinct", args, 0, 1);

				if (args.isEmpty()) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("distinct:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final Set<VncVal> seen = new HashSet<>();

							return new VncFunction(createAnonymousFuncName("distinct:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										if (seen.contains(input)) {
											return result;
										}
										else {
											seen.add(input);
											return rf.apply(VncList.of(result, input));
										}
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					if (args.first() == Nil) {
						return VncList.empty();
					}

					return ((VncSequence)args.first()).withValues(
														Coerce
															.toVncSequence(args.first())
															.getList()
															.stream()
															.distinct()
															.collect(Collectors.toList()));
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction sorted =
		new VncFunction(
				"sorted",
				VncFunction
					.meta()
					.arglists("(sorted cmp coll)")
					.doc(
						"Returns a sorted collection using the compare function cmp. " +
						"The compare function takes two arguments and returns -1, 0, or 1. " +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(sorted compare [4 2 1 5 6 3])",
						"(sorted (comp (partial * -1) compare) [4 2 1 5 6 3])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("sorted", args, 1, 2);

				final VncFunction compfn = Coerce.toVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("sorted:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final List<VncVal> list = new ArrayList<>();

							return new VncFunction(createAnonymousFuncName("sorted:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										VncVal result = args.first();
										final VncVal sortedList = CoreFunctions.sort.apply(VncList.of(compfn, VncList.ofList(list)));

										result = CoreFunctions.reduce.apply(VncList.of(rf, result, sortedList));
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										list.add(input);
										return result;
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					// delegate to core functions sort
					return CoreFunctions.sort.apply(args);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction reverse =
		new VncFunction(
				"reverse",
				VncFunction
					.meta()
					.arglists("(reverse coll)")
					.doc(
						"Returns a collection of the items in coll in reverse order. " +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(reverse [1 2 3 4 5 6])",
						"(reverse \"abcdef\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("reverse", args, 0, 1);

				if (args.size() == 0) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("reverse:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final List<VncVal> list = new ArrayList<>();

							return new VncFunction(createAnonymousFuncName("reverse:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										VncVal result = args.first();
										Collections.reverse(list);

										result = CoreFunctions.reduce.apply(VncList.of(rf, result, VncList.ofList(list)));
										return rf.apply(VncList.of(result));
									}
									else {
										final VncVal result = args.first();
										final VncVal input = args.second();

										list.add(input);
										return result;
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					final VncVal coll = args.first();

					if (coll == Nil) {
						return Nil;
					}
					else if (Types.isVncList(coll)) {
						return reverseList(((VncList)coll).getList());
					}
					else if (Types.isVncMutableList(coll)) {
						return reverseMutableList(((VncMutableList)coll).getList());
					}
					else if (Types.isVncVector(coll)) {
						return reverseVector(((VncVector)coll).getList());
					}
					else if (Types.isVncSet(coll)) {
						return reverseList(((VncSet)coll).getList());
					}
					else if (Types.isVncMap(coll)) {
						return reverseList(((VncMap)coll).toVncList().getList());
					}
					else if (Types.isVncString(coll)) {
						return reverseList(((VncString)coll).toVncList().getList());
					}
					else {
						throw new VncException(
								"Function 'reverse' requires a list, vector, set, " +
								"map, or string as coll argument.");
					}
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction flatten =
		new VncFunction(
				"flatten",
				VncFunction
					.meta()
					.arglists("(flatten coll)")
					.doc(
						"Takes any nested combination of collections (lists, vectors, " +
						"etc.) and returns their contents as a single, flat sequence. " +
						"(flatten nil) returns an empty list." +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(flatten [])",
						"(flatten [[1 2 3] [4 [5 6]] [7 [8 [9]]]])",
						"(flatten [1 2 {:a 3 :b [4 5 6]}])",
						"(flatten (seq {:a 1 :b 2}))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("flatten", args, 0, 1);

				if (args.size() == 0) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("flatten:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							assertArity(this.getQualifiedName(), args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("flatten:transducer")) {
								public VncVal apply(final VncList args) {
									assertArity(this.getQualifiedName(), args, 1, 2, 3);

									if (args.size() == 0) {
										return rf.apply(VncList.empty());
									}
									else if (args.size() == 1) {
										final VncVal result = args.first();
										return rf.apply(VncList.of(result));
									}
									else {
										VncVal result = args.first();
										final VncVal input = args.second();

										if (Types.isVncCollection(input)) {
											for(VncVal v : flatten(Coerce.toVncCollection(input))) {
												result = rf.apply(VncList.of(result, v));
											}
											return result;
										}
										else {
											return rf.apply(VncList.of(result, input));
										}
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					final VncCollection coll = Coerce.toVncCollection(args.first());
					final List<VncVal> result = flatten(coll);
					return Types.isVncVector(coll) ? VncVector.ofList(result) : VncList.ofList(result);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction halt_when =
		new VncFunction(
				"halt-when",
				VncFunction
					.meta()
					.arglists(
						"(halt-when pred)",
						"(halt-when pred retf)")
					.doc(
						"Returns a transducer that ends transduction when pred returns true " +
						"for an input. When retf is supplied it must be a fn of 2 arguments - " +
						"it will be passed the (completed) result so far and the input that " +
						"triggered the predicate, and its return value (if it does not throw " +
						"an exception) will be the return value of the  If retf " +
						"is not supplied, the input that triggered the predicate will be " +
						"returned. If the predicate never returns true the transduction is " +
						"unaffected.")
					.examples(
						"(do                                                     \n" +
						"  (def xf (comp (halt-when #(== % 10)) (filter odd?)))  \n" +
						"  (transduce xf conj [1 2 3 4 5 6 7 8 9]))                ",
						"(do                                                     \n" +
						"  (def xf (comp (halt-when #(> % 5)) (filter odd?)))    \n" +
						"  (transduce xf conj [1 2 3 4 5 6 7 8 9]))                ")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("halt-when", args, 1, 2, 3);

				final VncFunction predicate = Coerce.toVncFunction(args.first());
				final VncFunction halt_return_fn = args.size() > 1 ? Coerce.toVncFunction(args.second()) : null;
				final VncFunction no_halt_return_fn = args.size() > 2 ? Coerce.toVncFunction(args.third()) : null;

				// return a transducer
				return new VncFunction(createAnonymousFuncName("halt-when:transducer:wrapped")) {
					public VncVal apply(final VncList args) {
						assertArity(this.getQualifiedName(), args, 1);

						final VncFunction rf = Coerce.toVncFunction(args.first());

						return new VncFunction(createAnonymousFuncName("halt-when:transducer")) {
							public VncVal apply(final VncList args) {
								assertArity(this.getQualifiedName(), args, 1, 2, 3);

								if (args.size() == 0) {
									return rf.apply(VncList.empty());
								}
								else if (args.size() == 1) {
									final VncVal result = args.first();

									if (Types.isVncMap(result) && VncBoolean.isTrue(((VncMap)result).containsKey(HALT))) {
										return ((VncMap)result).get(HALT);
									}
									else if (no_halt_return_fn != null) {
										return no_halt_return_fn.apply(VncList.of(result));
									}
									else {
										return rf.apply(VncList.of(result));
									}
								}
								else {
									final VncVal result = args.first();
									final VncVal input = args.second();

									final VncVal cond = predicate.apply(VncList.of(input));
									if (!VncBoolean.isFalse(cond) && cond != Nil) {
										final VncVal haltVal = halt_return_fn != null
																? halt_return_fn.apply(
																		VncList.of(
																			rf.apply(VncList.of(result)),
																			input))
																: input;
										return Reduced.reduced(VncHashMap.of(HALT, haltVal));
									}
									else {
										return rf.apply(VncList.of(result, input));
									}
								}
							}

							private static final long serialVersionUID = -1L;
						};
					}

					private static final long serialVersionUID = -1L;
				};
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};



	private static List<VncVal> flatten(final VncVal value) {
		final List<VncVal> list = new ArrayList<>();
		flatten(value, list);
		return list;
	}

	private static void flatten(final VncVal value, final List<VncVal> result) {
		if (Types.isVncSequence(value)) {
			final Iterator<VncVal> iter = ((VncSequence)value).iterator();
			while(iter.hasNext()) flatten(iter.next(), result);
		}
		else {
			result.add(value);
		}
	}

	private static VncList reverseList(final List<VncVal> list) {
		final List<VncVal> copy = new ArrayList<>(list);
		Collections.reverse(copy);
		return VncList.ofList(copy);
	}

	private static VncMutableList reverseMutableList(final List<VncVal> list) {
		final List<VncVal> copy = new ArrayList<>(list);
		Collections.reverse(copy);
		return new VncMutableList(copy);
	}

	private static VncVector reverseVector(final List<VncVal> list) {
		final List<VncVal> copy = new ArrayList<>(list);
		Collections.reverse(copy);
		return VncVector.ofList(copy);
	}

	private static VncSequence coerceToSequence(final VncVal val) {
		if (Types.isVncMap(val)) {
			return VncList.ofList(((VncMap)val).entries());
		}
		else if (Types.isVncSet(val)) {
			return ((VncSet)val).toVncList();
		}
		else if (Types.isVncString(val)) {
			return ((VncString)val).toVncList();
		}
		else {
			return Coerce.toVncSequence(val);
		}
	}
	
	
	
	public static final VncKeyword HALT = new VncKeyword("@halt");
	private static final VncKeyword NONE = new VncKeyword("@none");


	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns =
			new VncHashMap
					.Builder()
					.add(transduce)
					.add(reduced)
					.add(reduced_Q)
					.add(map)
					.add(map_indexed)
					.add(filter)
					.add(drop)
					.add(drop_while)
					.add(take)
					.add(take_while)
					.add(keep)
					.add(dedupe)
					.add(remove)
					.add(distinct)
					.add(sorted)
					.add(reverse)
					.add(flatten)
					.add(halt_when)
					.toMap();
}
