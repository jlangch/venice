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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.removeNilValues;
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncLazySeq;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
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
						"Reduce with a transformation of a reduction function f (xf). " +
						"If init is not supplied, `(f)` will be called to produce it. " +
						"f should be a reducing step function that accepts both 1 and " +
						"2 arguments. Returns the result of applying (the transformed) " +
						"xf to init and the first item in coll, then applying xf to " +
						"that result and the 2nd item, etc. If coll contains no items, " +
						"returns init and f is not called.\n\n"+
						"```\n" +
						"Transformations            Reductions            Control   \n" +
						"----------------------     ------------------    --------- \n" +
						"map        map-indexed     rf-first              halt-when \n" +
						"filter     flatten         rf-last                         \n" +
						"drop       drop-while      rf-any?                         \n" +
						"drop-last  remove          rf-every?                       \n" +
						"take       take-while                                      \n" +
						"take-last  keep            conj                            \n" +
						"dedupe     distinct        +, *                            \n" +
						"sorted     reverse         max, min                        \n" +
						"```")
					.examples(
						"(transduce identity + [1 2 3 4])",
						
						"(transduce (map #(+ % 3)) + [1 2 3 4])",
						
						"(transduce identity max [1 2 3])",

						"(transduce identity rf-last [1 2 3])",

						"(transduce identity (rf-every? pos?) [1 2 3])",

						"(transduce (map inc) conj [1 2 3])",

						"(do                                       \n" +
						"  (def xform (comp (drop 2) (take 3)))    \n" +
						"  (transduce xform conj [1 2 3 4 5 6]))     ",

						"(do                                       \n" +
						"  (def xform (comp                        \n" +
						"               (map #(* % 10))            \n" +
						"               (map #(+ % 1))             \n" +
						"               (sorted compare)           \n" +
						"               (drop 3)                   \n" +
						"               (take 2)                   \n" +
						"               (reverse)))                \n" +
						"  (transduce xform conj [1 2 3 4 5 6]))     ")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 3, 4);

				// (def xform (drop 2))
				// (transduce xform + [1 2 3 4])  ;; => 7
				// (transduce xform conj [1 2 3 4])  ;; => [3 4]

				final VncFunction xform = Coerce.toVncFunction(args.first());
				final VncFunction reduction_fn = Coerce.toVncFunction(args.second());
				final VncSequence coll = VncSequence.coerceToSequence(args.last());

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
				ArityExceptions.assertArity(this, args, 1);

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
				ArityExceptions.assertArity(this, args, 1);

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
						"is exhausted. Any remaining items in other colls are ignored.¶" +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(map inc [1 2 3 4])",
						"(map + [1 2 3 4] [10 20 30 40])",
						"(map list '(1 2 3 4) '(10 20 30 40))",
						"(map (fn [e] [(key e) (inc (val e))]) {:a 1 :b 2})",
						"(map inc #{1 2 3})")
					.seeAlso("filter", "reduce")
					.build()
		) {
			public VncVal apply(final VncList args) {
				final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

				if (args.size() == 0) {
					return Nil;
				}
				else if (args.size() == 1) {
					final IVncFunction fn = Coerce.toIVncFunction(args.first());

					// return a transducer
					return new VncFunction(createAnonymousFuncName("map:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final IVncFunction rf = Coerce.toIVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("map:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();
											return rf.apply(VncList.of(result, fn.apply(VncList.of(input))));
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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
						VncSequence seq = VncSequence.coerceToSequence(lists.first());
						seq = meterRegistry.enabled 
								? seq.map(v -> VncFunction.applyWithMeter(fn, VncList.of(v), meterRegistry))
								: seq.map(v -> fn.apply(VncList.of(v)));
						return (seq instanceof VncLazySeq) ? seq : seq.toVncList();
					}
					else {
						final VncSequence[] seqs = new VncSequence[lists.size()];
						for(int ii=0; ii<lists.size(); ii++) {
							seqs[ii] = VncSequence.coerceToSequence(lists.nth(ii));
						}
						
						// mapper with multiple collections
						while(!isOneEmpty(seqs)) {
							final List<VncVal> fnArgs = new ArrayList<>();

							for(int ii=0; ii<seqs.length; ii++) {
								fnArgs.add(seqs[ii].first());
								seqs[ii] = seqs[ii].rest();
							}

							final VncVal val = VncFunction.applyWithMeter(
													fn, 
													VncList.ofList(fnArgs), 
													meterRegistry);
							result.add(val);
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
						"until coll is exhausted.¶" +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(map-indexed (fn [idx val] [idx val]) [:a :b :c])",
						"(map-indexed vector [:a :b :c])",
						"(map-indexed vector \"abcdef\")",
						"(map-indexed hash-map [:a :b :c])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

				if (args.size() == 0) {
					return Nil;
				}
				else if (args.size() == 1) {
					final IVncFunction fn = Coerce.toIVncFunction(args.first());

					// return a transducer
					return new VncFunction(createAnonymousFuncName("map-indexed:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicLong idx = new AtomicLong(0);

							return new VncFunction(createAnonymousFuncName("map-indexed:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();
	
											return rf.apply(VncList.of(
																result,
																fn.apply(VncList.of(
																			new VncLong(idx.getAndIncrement()),
																			input))));
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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

					Iterable<VncVal> items;

					if (Types.isVncSequence(coll)) {
						items = ((VncSequence)coll);
					}
					else if (Types.isVncSet(coll)) {
						items = ((VncSet)coll);
					}
					else if (Types.isVncMap(coll)) {
						items = ((VncMap)coll).toVncList();
					}
					else if (Types.isVncString(coll)) {
						items = ((VncString)coll).toVncList();
					}
					else {
						throw new VncException(
								"Function 'map-indexed' requires a list, vector, set, " +
								"map, or string as coll argument.");
					}


					final List<VncVal> list = new ArrayList<>();
					int index = 0;

					for(VncVal v : items) {
						list.add(
							VncFunction.applyWithMeter(
								fn, 
								VncList.of(new VncLong(index++), v), 
								meterRegistry));
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
						"`(predicate item)` returns logical true.¶" +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(filter even? [1 2 3 4 5 6 7])",
						"(filter #(even? (val %)) {:a 1 :b 2})",
						"(filter even? #{1 2 3})")
					.seeAlso("map", "reduce")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

				final IVncFunction predicate = Coerce.toIVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("filter:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("filter:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();
											final VncVal cond = predicate.apply(VncList.of(input));
											return !VncBoolean.isFalseOrNil(cond)
														? rf.apply(VncList.of(result, input))
														: result;
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					VncSequence seq = VncSequence.coerceToSequence(args.second());
					if (meterRegistry.enabled) {
						seq = seq.filter(v -> !VncBoolean.isFalseOrNil(
													VncFunction.applyWithMeter(
														predicate,
														VncList.of(v),
														meterRegistry)));
					}
					else {
						seq = seq.filter(v -> !VncBoolean.isFalseOrNil(
													predicate.apply(VncList.of(v))));
					}
					return (seq instanceof VncLazySeq) ? seq : seq.toVncList();
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
						"Returns a collection of all but the first n items in coll.¶" +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(drop 3 [1 2 3 4 5])", 
						"(drop 10 [1 2 3 4 5])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				if (args.size() == 1) {
					final long n = Coerce.toVncLong(args.first()).getValue();

					// return a transducer
					return new VncFunction(createAnonymousFuncName("drop:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicLong nn = new AtomicLong(n);

							return new VncFunction(createAnonymousFuncName("drop:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();
	
											return nn.getAndDecrement() > 0 
													? result
													: rf.apply(VncList.of(result, input));
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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
					final VncSequence coll = VncSequence.coerceToSequence(args.second());

					return coll.drop(n.getIntValue());
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
						"first item for which `(predicate item)` returns logical false.¶" +
						"Returns a stateful transducer when no collection is provided.")
					.examples("(drop-while neg? [-2 -1 0 1 2 3])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

				final IVncFunction predicate = Coerce.toIVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("drop-while:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicBoolean take = new AtomicBoolean(false);

							return new VncFunction(createAnonymousFuncName("drop-while:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();

											if (take.get()) {
												return rf.apply(VncList.of(result, input));
											}
											else {
												final VncVal drop = predicate.apply(VncList.of(input));
												if (VncBoolean.isFalseOrNil(drop)) {
													take.set(true);
													return rf.apply(VncList.of(result, input));
												}
												else {
													return result;
												}
											}
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					final VncSequence coll = VncSequence.coerceToSequence(args.second());

					return coll.dropWhile(v -> !VncBoolean.isFalseOrNil(
													VncFunction.applyWithMeter(
															predicate,
															VncList.of(v),
															meterRegistry)));
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction drop_last =
		new VncFunction(
				"drop-last",
				VncFunction
					.meta()
					.arglists("(drop-last n coll)")
					.doc(
						"Return a sequence of all but the last n items in coll.¶" +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(drop-last 3 [1 2 3 4 5])", 
						"(drop-last 10 [1 2 3 4 5])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				if (args.size() == 1) {
					final int n = Math.max(0, Coerce.toVncLong(args.first()).getValue().intValue());

					// return a transducer
					return new VncFunction(createAnonymousFuncName("drop-last:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final List<VncVal> list = new ArrayList<>();

							return new VncFunction(createAnonymousFuncName("drop-last:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1: {
											VncVal result = args.first();
											final VncVal dropList = list.size() > n 
																		? VncList.ofList(list.subList(0, list.size()-n)) 
																		: VncList.empty();
	
											result = CoreFunctions.reduce.apply(VncList.of(rf, result, dropList));
											return rf.apply(VncList.of(result));
										}
										case 2: {
											final VncVal result = args.first();
											final VncVal input = args.second();
	
											list.add(input);
											return result;
										}
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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
					final VncSequence coll = VncSequence.coerceToSequence(args.second());

					return coll.dropRight(n.getIntValue());
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
						"there are fewer than n.¶" +
						"Returns a stateful transducer when no collection is provided. \n" +
						"Returns a lazy sequence if coll is a lazy sequence.")
					.examples(
						"(take 3 [1 2 3 4 5])",
						"(take 10 [1 2 3 4 5])",
						"(doall (take 4 (repeat 3)))",
						"(doall (take 10 (cycle (range 0 3))))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				if (args.size() == 1) {
					final long n = Coerce.toVncLong(args.first()).getValue();

					// return a transducer
					return new VncFunction(createAnonymousFuncName("take:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicLong nn = new AtomicLong(n);

							return new VncFunction(createAnonymousFuncName("take:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();
	
											return nn.getAndDecrement() > 0
													? rf.apply(VncList.of(result, input))
													: Reduced.reduced(result);
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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
					final VncSequence coll = VncSequence.coerceToSequence(args.second());

					return coll.take(n.getIntValue());
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
						"`(predicate item)` returns logical true.¶" +
						"Returns a transducer when no collection is provided.")
					.examples("(take-while neg? [-2 -1 0 1 2 3])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

				final IVncFunction predicate = Coerce.toIVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("take-while:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("take-while:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();
	
											final VncVal take = predicate.apply(VncList.of(input));
											if (VncBoolean.isFalseOrNil(take)) {
												return Reduced.reduced(result);
											}
											else {
												return rf.apply(VncList.of(result, input));
											}
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};

				}
				else {
					final VncSequence coll = VncSequence.coerceToSequence(args.second());

					return coll.takeWhile(v -> !VncBoolean.isFalseOrNil(
													VncFunction.applyWithMeter(
															predicate,
															VncList.of(v),
															meterRegistry)));
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction take_last =
		new VncFunction(
				"take-last",
				VncFunction
					.meta()
					.arglists("(take-last n coll)")
					.doc(
						"Return a sequence of the last n items in coll.¶" +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(take-last 3 [1 2 3 4 5])", 
						"(take-last 10 [1 2 3 4 5])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				if (args.size() == 1) {
					final int n = Math.max(0, Coerce.toVncLong(args.first()).getValue().intValue());

					// return a transducer
					return new VncFunction(createAnonymousFuncName("take-last:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final LinkedList<VncVal> list = new LinkedList<>();

							return new VncFunction(createAnonymousFuncName("take-last:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1: {
											VncVal result = args.first();
	
											result = CoreFunctions.reduce.apply(VncList.of(rf, result, VncList.ofList(list)));
											return rf.apply(VncList.of(result));
										}
										case 2: {
											final VncVal result = args.first();
											final VncVal input = args.second();
	
											list.add(input);
											if (list.size() > n) {
												list.removeFirst();
											}
											return result;
										}
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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
					final VncSequence coll = VncSequence.coerceToSequence(args.second());

					return coll.takeRight(n.getIntValue());
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
						"Returns a sequence of the non-nil results of `(f item)`. Note, " +
						"this means false return values will be included. f must be " +
						"free of side-effects.¶" +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(keep even? (range 1 4))",
						"(keep (fn [x] (if (odd? x) x)) (range 4))",
						"(keep #{3 5 7} '(1 3 5 7 9))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				if (args.size() == 1) {
					final IVncFunction fn = Coerce.toIVncFunction(args.first());

					// return a transducer
					return new VncFunction(createAnonymousFuncName("keep:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("keep:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();
											final VncVal val = fn.apply(VncList.of(input));
											return VncBoolean.isFalseOrNil(val) 
														? result 
														: rf.apply(VncList.of(result, input));
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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
						"Returns a collection with all consecutive duplicates removed.¶" +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(dedupe [1 2 2 2 3 4 4 2 3])",
						"(dedupe '(1 2 2 2 3 4 4 2 3))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0, 1);

				if (args.isEmpty()) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("dedupe:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final AtomicReference<VncVal> seen = new AtomicReference<>(NONE);

							return new VncFunction(createAnonymousFuncName("dedupe:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();

											if (!input.equals(seen.get())) {
												seen.set(input);
												return rf.apply(VncList.of(result, input));
											}
											else {
												return result;
											}
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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

					for(VncVal val : VncSequence.coerceToSequence(args.first())) {
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
						"`(predicate item)` returns logical false.¶" +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(remove even? [1 2 3 4 5 6 7])",
						"(remove #{3 5} '(1 3 5 7 9))",
						"(remove #(= 3 %) '(1 2 3 4 5 6))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				final MeterRegistry meterRegistry = ThreadContext.getMeterRegistry();

				final IVncFunction predicate = Coerce.toIVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					final VncFunction fn =
							new VncFunction(createAnonymousFuncName("remove:transducer")) {
								public VncVal apply(final VncList args) {
									return VncBoolean.of(VncBoolean.isFalseOrNil(predicate.apply(args)));
								}
								private static final long serialVersionUID = -1;
							};
					return filter.apply(VncList.of(fn));
				}
				else {
					final VncSequence seq = VncSequence.coerceToSequence(args.second())
												.filter(v -> VncBoolean.isFalseOrNil(
																VncFunction.applyWithMeter(
																				predicate,
																				VncList.of(v),
																				meterRegistry)));
					return (seq instanceof VncLazySeq) ? seq : seq.toVncList();
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
						"Returns a collection with all duplicates removed.¶" +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(distinct [1 2 3 4 2 3 4])",
						"(distinct '(1 2 3 4 2 3 4))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0, 1);

				if (args.isEmpty()) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("distinct:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final Set<VncVal> seen = new HashSet<>();

							return new VncFunction(createAnonymousFuncName("distinct:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1:
											return rf.apply(VncList.of(args.first()));
										case 2:
											final VncVal result = args.first();
											final VncVal input = args.second();
	
											if (seen.contains(input)) {
												return result;
											}
											else {
												seen.add(input);
												return rf.apply(VncList.of(result, input));
											}
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
									}
								}

								private static final long serialVersionUID = -1L;
							};
						}

						private static final long serialVersionUID = -1L;
					};
				}
				else {
					return args.first() == Nil
							? VncList.empty()
							: Coerce.toVncSequence(args.first()).distinct();
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
						"The compare function takes two arguments and returns -1, 0, or 1.¶" +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(sorted compare [4 2 1 5 6 3])",
						"(sorted (comp (partial * -1) compare) [4 2 1 5 6 3])")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				final VncFunction compfn = Coerce.toVncFunction(args.first());

				if (args.size() == 1) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("sorted:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final List<VncVal> list = new ArrayList<>();

							return new VncFunction(createAnonymousFuncName("sorted:transducer")) {
								public VncVal apply(final VncList args) {
										switch (args.size()) {
											case 0:
												return rf.apply(VncList.empty());
											case 1: {
												VncVal result = args.first();
												final VncVal sortedList = CoreFunctions.sort.apply(VncList.of(compfn, VncList.ofList(list)));
		
												result = CoreFunctions.reduce.apply(VncList.of(rf, result, sortedList));
												return rf.apply(VncList.of(result));
											}
											case 2: {
												final VncVal result = args.first();
												final VncVal input = args.second();
		
												list.add(input);
												return result;
											}
											default:
												ArityExceptions.assertArity(this, args, 0, 1, 2);
												return Nil;
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
						"Returns a collection of the items in coll in reverse order.¶" +
						"Returns a stateful transducer when no collection is provided.")
					.examples(
						"(reverse [1 2 3 4 5 6])",
						"(reverse \"abcdef\")")
					.seeAlso("str/reverse")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0, 1);

				if (args.size() == 0) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("reverse:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());
						    final List<VncVal> list = new ArrayList<>();

							return new VncFunction(createAnonymousFuncName("reverse:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1: {
											VncVal result = args.first();
											Collections.reverse(list);

											result = CoreFunctions.reduce.apply(VncList.of(rf, result, VncList.ofList(list)));
											return rf.apply(VncList.of(result));
										}
										case 2: {
											final VncVal result = args.first();
											final VncVal input = args.second();

											list.add(input);
											return result;
										}
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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
					else if (Types.isVncSequence(coll)) {
						return ((VncSequence)coll).reverse();
					}
					else if (Types.isVncCollection(coll)) {
						return ((VncCollection)coll).toVncList().reverse();
					}
					else if (Types.isVncString(coll)) {
						return ((VncString)coll).toVncList().reverse();
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
						"`(flatten nil)` returns an empty list.¶" +
						"Returns a transducer when no collection is provided.")
					.examples(
						"(flatten [])",
						"(flatten [[1 2 3] [4 [5 6]] [7 [8 [9]]]])",
						"(flatten [1 2 {:a 3 :b [4 5 6]}])",
						"(flatten (seq {:a 1 :b 2}))")
					.seeAlso("mapcat")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0, 1);

				if (args.size() == 0) {
					// return a transducer
					return new VncFunction(createAnonymousFuncName("flatten:transducer:wrapped")) {
						public VncVal apply(final VncList args) {
							ArityExceptions.assertArity(this, args, 1);

							final VncFunction rf = Coerce.toVncFunction(args.first());

							return new VncFunction(createAnonymousFuncName("flatten:transducer")) {
								public VncVal apply(final VncList args) {
									switch (args.size()) {
										case 0:
											return rf.apply(VncList.empty());
										case 1: {
											final VncVal result = args.first();
											return rf.apply(VncList.of(result));
										}
										case 2: {
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
										default:
											ArityExceptions.assertArity(this, args, 0, 1, 2);
											return Nil;
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
						"an exception) will be the return value of the transducer. If retf " +
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
				ArityExceptions.assertArity(this, args, 1, 2, 3);

				final VncFunction predicate = Coerce.toVncFunction(args.first());
				final VncFunction halt_return_fn = args.size() > 1 ? Coerce.toVncFunction(args.second()) : null;
				final VncFunction no_halt_return_fn = args.size() > 2 ? Coerce.toVncFunction(args.third()) : null;

				// return a transducer
				return new VncFunction(createAnonymousFuncName("halt-when:transducer:wrapped")) {
					public VncVal apply(final VncList args) {
						ArityExceptions.assertArity(this, args, 1);

						final VncFunction rf = Coerce.toVncFunction(args.first());

						return new VncFunction(createAnonymousFuncName("halt-when:transducer")) {
							public VncVal apply(final VncList args) {
								switch (args.size()) {
									case 0:
										return rf.apply(VncList.empty());
									case 1: {
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
									case 2: {
										final VncVal result = args.first();
										final VncVal input = args.second();
	
										final VncVal cond = predicate.apply(VncList.of(input));
										if (!VncBoolean.isFalseOrNil(cond)) {
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
									default:
										ArityExceptions.assertArity(this, args, 0, 1, 2);
										return Nil;
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
			for(VncVal v : ((VncSequence)value)) {
				flatten(v, result);
			}
		}
		else {
			result.add(value);
		}
	}
	
	private static boolean isOneEmpty(final VncSequence[] seqs) {
		for(int ii=0; ii<seqs.length; ii++) {
			if (seqs[ii].isEmpty()) {
				return true;
			}
		}
		return false;
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
					.add(drop_last)
					.add(take)
					.add(take_while)
					.add(take_last)
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
