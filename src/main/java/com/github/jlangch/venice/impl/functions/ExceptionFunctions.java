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

import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;


public class ExceptionFunctions {
	
	public static VncFunction throw_ =
		new VncFunction(
				"throw",
				VncFunction
					.meta()
					.arglists("(throw)", "(throw val)", "(throw ex)")
					.doc(
						"Throws an exception.\n\n" +
						"`(throw)`¶\n" +
						"Throws a :ValueException with `nil` as its value.\n" +
						"\n" +
						"`(throw val)`¶\n" +
						"With *val* as a Venice value throws a :ValueException with *val* " +
						"as its value.¶\n" +
						"E.g: `(throw [1 2 3])`\n" +
						"\n" +
						"`(throw ex)`¶\n" +
						"With a *ex* as an exception type throws the exception.¶\n" +
						"E.g: `(throw (ex :VncException \"invalid data\"))`")
					.examples(
						"(do                                                      \n" +
						"   (try                                                  \n" +
						"      (+ 100 200)                                        \n" +
						"      (catch :Exception e                                \n" +
						"             \"caught ~(ex-message e)\")))                 ",

						"(do                                                      \n" +
						"   (try                                                  \n" +
						"      (+ 100 200)                                        \n" +
						"      (throw)                                            \n" +
						"      (catch :ValueException e                           \n" +
						"             \"caught ~(pr-str (ex-value e))\")))          ",

						"(do                                                      \n" +
						"   (try                                                  \n" +
						"      (+ 100 200)                                        \n" +
						"      (throw 100)                                        \n" +
						"      (catch :ValueException e                           \n" +
						"             \"caught ~(ex-value e)\")))                   ",

						";; The finally block is just for side effects, like     \n" +
						";; closing resources. It never returns a value!         \n" +
						"(do                                                     \n" +
						"   (try                                                 \n" +
						"      (+ 100 200)                                       \n" +
						"      (throw [100 {:a 3}])                              \n" +
						"      (catch :ValueException e                          \n" +
						"             \"caught ~(ex-value e)\")                  \n" +
						"      (finally (println \"#finally\")                   \n" +
						"               :finally)))                                ",

						"(do                                                     \n" +
						"   (try                                                 \n" +
						"      (throw (ex :RuntimeException \"#test\"))          \n" +
						"      (catch :RuntimeException e                        \n" +
						"             \"caught ~(ex-message e)\")))                ",

						";; Venice wraps thrown checked exceptions with a RuntimeException! \n" +
						"(do                                                                \n" +
						"   (import :java.io.IOException)                                   \n" +
						"   (try                                                            \n" +
						"      (throw (ex :IOException \"#test\"))                          \n" +
						"      (catch :RuntimeException e                                   \n" +
					    "             \"caught ~(ex-message (ex-cause e))\")))                ")
					.seeAlso("ex", "try", "try-with")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0, 1);

				if (args.isEmpty()) {
					throw new ValueException(Nil);
				}
				else if (Types.isVncJavaObject(args.first(), Exception.class)) {
					final Exception ex = (Exception)((VncJavaObject)args.first()).getDelegate();
					if (ex instanceof RuntimeException) {
						throw (RuntimeException)ex;
					}
					else {
						// wrap it with a RuntimeException
						throw new RuntimeException(ex);
					}
				}
				else {
					throw new ValueException(args.first());
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction ex =
		new VncFunction(
				"ex",
				VncFunction
					.meta()
					.arglists(
						"(ex class)", 
						"(ex class message)", 
						"(ex class message cause)")
					.doc(
						"Creates an exception of type *class* with an optional *message* and " +
						"an optional *cause* exception. \n\n" +
						"The *class* must be a subclass of :java.lang.Exception¶\n" +
						"The *cause* must be an instance of :java.lang.Throwable\n\n" +
						"The exception types \n\n" +
						"  * :java.lang.Exception \n" +
						"  * :java.lang.RuntimeException \n" +
						"  * :com.github.jlangch.venice.VncException \n\n" +
						"  * :com.github.jlangch.venice.ValueException \n\n" +
						"are imported implicitly so its alias :Exception, :RuntimeException, " +
						":VncException, and :ValueException can be used.")
					.examples(
						"(do                                                      \n" +
						"   (try                                                  \n" +
						"      (throw (ex :VncException))                         \n" +
						"      (catch :VncException e \"caught :VncException\")))   ",

						"(do                                                      \n" +
						"   (try                                                  \n" +
						"      (throw (ex :RuntimeException \"#test\"))           \n" +
						"      (catch :Exception e                                \n" +
						"             \"msg: ~(ex-message e)\")))                   ",

						"(do                                                      \n" +
						"   (try                                                  \n" +
						"      (throw (ex :ValueException 100))                   \n" +
						"      (catch :ValueException e                           \n" +
						"             \"value: ~(ex-value e)\")))                     ",

						"(do                                                                         \n" +
						"   (defn throw-ex-with-cause []                                             \n" +
						"     (try                                                                   \n" +
						"        (throw (ex :java.io.IOException \"I/O failure\"))                    \n" +
						"        (catch :Exception e                                                 \n" +
						"               (throw (ex :VncException \"failure\" (ex-cause e))))))       \n" +
						"   (try                                                                     \n" +
						"      (throw-ex-with-cause)                                                 \n" +
						"      (catch :Exception e                                                   \n" +
						"             \"msg: ~(ex-message e), cause: ~(ex-message (ex-cause e))\")))   ")
					.seeAlso("throw", "try", "try-with", "ex?", "ex-venice?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2, 3);

				final JavaImports javaImports = Namespaces.getCurrentNamespace().getJavaImports();
				
				final Class<?> excClass = JavaInteropUtil.toClass(args.first(), javaImports);			
				if (!Exception.class.isAssignableFrom(excClass)) {
					throw new VncException(
							"Function 'ex' expects a 'class' arg as a subtype of :java.lang.Exception!");
				}

				// No sandbox checking for creating an exception!
				
				if (args.size() == 1) {
					return JavaInteropUtil.applyJavaAccess(
							VncList.of(args.first(), new VncKeyword("new")), 
							javaImports);	
				}
				else {
					final VncVal msg = args.second();
					final VncVal cause = args.third();

					// the cause must be nil or an exception
					if (cause != Nil && !Types.isVncJavaObject(cause, Exception.class)) {
						throw new VncException(
								"Function 'ex' expects a 'cause' arg of type :java.lang.Exception!");
					}

					if (ValueException.class.isAssignableFrom(excClass)) {
						return new VncJavaObject(new ValueException(msg));
					}
					else {
						// the message must be a nil or a string
						if (msg != Nil && !Types.isVncString(msg)) {
							throw new VncException(
									"Function 'ex' expects a 'message' arg of type string!");
						}
	
						return JavaInteropUtil.applyJavaAccess(
									VncList.of(args.first(), new VncKeyword("new"), msg, cause), 
									javaImports);
					}
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction ex_Q =
		new VncFunction(
				"ex?",
				VncFunction
					.meta()
					.arglists("(ex? x)")
					.doc("Returns true if x is a an instance of :java.lang.Throwable")
					.examples(
						"(ex? (ex :RuntimeException))")
					.seeAlso("ex", "ex-venice?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				if (Types.isVncJavaObject(args.first(), Throwable.class)) {
					final Throwable ex = (Throwable)((VncJavaObject)args.first()).getDelegate();
					return VncBoolean.of(ex instanceof Throwable);
				}
				
				return VncBoolean.False;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction ex_venice_Q =
		new VncFunction(
				"ex-venice?",
				VncFunction
					.meta()
					.arglists("(ex-venice? x)")
					.doc("Returns true if x is a an instance of :VncException")
					.examples(
						"(ex-venice? (ex :VncException))",
						"(ex-venice? (ex :RuntimeException))")
					.seeAlso("ex", "ex?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				if (Types.isVncJavaObject(args.first(), Throwable.class)) {
					final Throwable ex = (Throwable)((VncJavaObject)args.first()).getDelegate();
					return VncBoolean.of(ex instanceof VncException);
				}
				
				return VncBoolean.False;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction ex_message =
		new VncFunction(
				"ex-message",
				VncFunction
					.meta()
					.arglists("(ex-message x)")
					.doc("Returns the message of the exception")
					.examples(
						"(ex-message (ex :VncException \"a message\"))",
						"(ex-message (ex :RuntimeException))")
					.seeAlso("ex", "ex-cause", "ex-value")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				if (Types.isVncJavaObject(args.first(), Throwable.class)) {
					final Throwable ex = (Throwable)((VncJavaObject)args.first()).getDelegate();
					final String msg = ex.getMessage();
					return msg == null ? Nil : new VncString(ex.getMessage());
				}
				else {
					throw new VncException(
							"Function 'ex-message' expects a :java.lang.Throwable as arg!");
				}			
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction ex_cause =
		new VncFunction(
				"ex-cause",
				VncFunction
					.meta()
					.arglists("(ex-cause x)")
					.doc("Returns the exception cause or nil")
					.examples(
						"(ex-cause (ex :VncException \"a message\" (ex :RuntimeException \"..cause..\")))",
						"(ex-cause (ex :VncException \"a message\"))")
					.seeAlso("ex", "ex-message", "ex-value")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				if (Types.isVncJavaObject(args.first(), Throwable.class)) {
					final Throwable ex = (Throwable)((VncJavaObject)args.first()).getDelegate();
					final Throwable cause = ex.getCause();
					return cause == null ? Nil : new VncJavaObject(cause);
				}
				else {
					throw new VncException(
							"Function 'ex-cause' expects a :java.lang.Throwable as arg!");
				}			
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction ex_value =
		new VncFunction(
				"ex-value",
				VncFunction
					.meta()
					.arglists("(ex-value x)")
					.doc(
						"Returns the value associated with a :ValueException or nil if the " +
						"exception is not a :ValueException")
					.examples(
						"(ex-value (ex :ValueException [10 20]))",
						"(ex-value (ex :RuntimeException))")
					.seeAlso("ex", "ex-message", "ex-cause")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				if (Types.isVncJavaObject(args.first(), Throwable.class)) {
					final Throwable ex = (Throwable)((VncJavaObject)args.first()).getDelegate();
					final Object value = ex instanceof ValueException 
											? ((ValueException)ex).getValue() 
											: Nil;
					if (value == null || value == Nil) {
						return Nil;
					}
					else {
						return value instanceof VncVal ? (VncVal)value : JavaInteropUtil.convertToVncVal(value);
					}
				}
				else {
					throw new VncException(
							"Function 'ex-value' expects a :java.lang.Throwable as arg!");
				}			
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction ex_venice_stacktrace =
		new VncFunction(
				"ex-venice-stacktrace",
				VncFunction
					.meta()
					.arglists("(ex-venice-stacktrace x)")
					.doc(
						"Returns the Venice stacktrace for an exception or nil if the " +
						"exception is not a venice exception")
					.examples(
						"(ex-venice-stacktrace (ex :ValueException [10 20]))",
						"(ex-venice-stacktrace (ex :RuntimeException))")
					.seeAlso("ex", "ex-java-stacktrace")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				if (Types.isVncJavaObject(args.first(), Throwable.class)) {
					final Throwable ex = (Throwable)((VncJavaObject)args.first()).getDelegate();
					
					return Nil;
				}
				else {
					throw new VncException(
							"Function 'ex-venice-stacktrace' expects a :java.lang.Throwable as arg!");
				}			
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction ex_java_stacktrace =
			new VncFunction(
					"ex-java-stacktrace",
					VncFunction
						.meta()
						.arglists("(ex-java-stacktrace x)")
						.doc(
							"Returns the Java stacktrace for an exception")
						.examples(
							"(ex-java-stacktrace (ex :RuntimeException))")
						.seeAlso("ex", "ex-venice-stacktrace")
						.build()
			) {
				public VncVal apply(final VncList args) {
					ArityExceptions.assertArity(this, args, 1);

					if (Types.isVncJavaObject(args.first(), Throwable.class)) {
						final Throwable ex = (Throwable)((VncJavaObject)args.first()).getDelegate();
						
						return Nil;
					}
					else {
						throw new VncException(
								"Function 'ex-java-stacktrace' expects a :java.lang.Throwable as arg!");
					}			
				}

				private static final long serialVersionUID = -1848883965231344442L;
			};
	
			
			
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(throw_)
					.add(ex)
					.add(ex_Q)
					.add(ex_venice_Q)
					.add(ex_message)
					.add(ex_cause)
					.add(ex_value)
					.add(ex_venice_stacktrace)
					.add(ex_java_stacktrace)
					.toMap();	
}
