/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;
import com.github.jlangch.venice.util.CallStack;


public class SystemFunctions {

	///////////////////////////////////////////////////////////////////////////
	// System
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction version = 
		new VncFunction(
				"version", 
				VncFunction
					.meta()
					.arglists("(version)")		
					.doc("Returns the version.")
					.examples("(version )")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("version", args, 0);
				
				return new VncString(Version.VERSION);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};


	public static VncFunction uuid = 
		new VncFunction(
				"uuid", 
				VncFunction
					.meta()
					.arglists("(uuid)")		
					.doc("Generates a UUID.")
					.examples("(uuid )")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("uuid", args, 0);
				return new VncString(UUID.randomUUID().toString());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};


	public static VncFunction objid = 
		new VncFunction(
				"oobjid", 
				VncFunction
					.meta()
					.arglists("(objid)")		
					.doc("Returns the original unique hash code for the given object.")
					.examples("(objid x)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("objid", args, 1);
				return new VncLong(System.identityHashCode(args.first()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	
	public static VncFunction current_time_millis = 
		new VncFunction(
				"current-time-millis", 
				VncFunction
					.meta()
					.arglists("(current-time-millis)")		
					.doc("Returns the current time in milliseconds.")
					.examples("(current-time-millis)")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("current-time-millis", args, 0);
				
				return new VncLong(System.currentTimeMillis());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction nano_time = 
		new VncFunction(
				"nano-time", 
				VncFunction
					.meta()
					.arglists("(nano-time)")		
					.doc(
						"Returns the current value of the running Java Virtual Machine's " +
						"high-resolution time source, in nanoseconds.")
					.examples("(nano-time)")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("nano-time", args, 0);
				
				return new VncLong(System.nanoTime());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction sleep = 
		new VncFunction(
				"sleep", 
				VncFunction
					.meta()
					.arglists("(sleep n)")		
					.doc("Sleep for n milliseconds.")
					.examples("(sleep 30)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("sleep");
	
				assertArity("sleep", args, 1);
				
				try {
					Thread.sleep(Math.max(0, Coerce.toVncLong(args.first()).getValue()));
				} 
				catch(InterruptedException ex) {
					throw new com.github.jlangch.venice.InterruptedException("(sleep n) interrupted", ex);
				} 
				
				return Nil;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction gc = 
		new VncFunction(
				"gc", 
				VncFunction
					.meta()
					.arglists("(gc)")		
					.doc("Run the Java garbage collector. Runs the finalization methods of any objects pending finalization prior to the GC.")
					.examples("(gc)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				JavaInterop.getInterceptor().validateBlackListedVeniceFunction("gc");
	
				assertArity("gc", args, 0);
				
				Runtime.getRuntime().runFinalization();
				Runtime.getRuntime().gc();
				
				return Nil;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction callstack = 
		new VncFunction(
				"callstack", 
				VncFunction
					.meta()
					.arglists("(callstack )")		
					.doc("Returns the current callstack.")
					.examples(
						"(do                             \n" +
						"   (defn f1 [x] (f2 x))         \n" +
						"   (defn f2 [x] (f3 x))         \n" +
						"   (defn f3 [x] (f4 x))         \n" +
						"   (defn f4 [x] (callstack))    \n" +
						"   (f1 100))                      ")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("callstack", args, 0);
				
				final CallStack stack = ThreadLocalMap.getCallStack();
				
				return new VncVector(
						stack
							.callstack()
							.stream()
							.map(f -> VncOrderedMap.of(
											CALLSTACK_KEY_FN_NAME, f.getFnName() == null 
															? Constants.Nil 
															: new VncString(f.getFnName()),
											CALLSTACK_KEY_FILE, new VncString(f.getFile()),
											CALLSTACK_KEY_LINE, new VncLong(f.getLine()),
											CALLSTACK_KEY_COL, new VncLong(f.getCol())))
							.collect(Collectors.toList()));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction os_type = 
		new VncFunction(
				"os-type", 
				VncFunction
					.meta()
					.arglists("(os-type)")		
					.doc("Returns the OS type")
					.examples("(os-type)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("os", args, 0);
				
				final String osName = System.getProperty("os.name");
				if (osName.startsWith("Windows")) {
					return new VncKeyword("windows");
				}
				else if (osName.startsWith("Mac OS X")) {
					return new VncKeyword("mac-osx");
				}
				else if (osName.startsWith("LINUX")) {
					return new VncKeyword("linux");
				}
				else {
					return new VncKeyword("unknown");
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction os_type_Q = 
		new VncFunction(
				"os-type?", 
				VncFunction
					.meta()
					.arglists("(os-type? type)")		
					.doc(
						"Returns true if the OS id of the type otherwise false. Type is one " +
						"of :windows, :mac-osx, or :linux")
					.examples("(os-type? :mac-osx)", "(os-type? :windows)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("os-type?", args, 1);
				
				final String type = Coerce.toVncKeyword(args.first()).getValue();
				final String osName = System.getProperty("os.name");
				switch(type) {
					case "windows": return  osName.startsWith("Windows") ? True : False;
					case "mac-osx": return  osName.startsWith("Mac OS X") ? True : False;
					case "linux":   return  osName.startsWith("LINUX") ? True : False;
					default:        return False;
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction sandboxed_Q = 
		new VncFunction(
				"sandboxed?", 
				VncFunction
					.meta()
					.arglists("(sandboxed? )")		
					.doc("Returns true if there is a sandbox otherwise false")
					.examples("(sandboxed? )")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("sandboxed?", args, 0);
				
				return JavaInterop.isSandboxed() ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction system_prop = 
		new VncFunction(
				"system-prop", 
				VncFunction
					.meta()
					.arglists("(system-prop name default-val)")		
					.doc(
						"Returns the system property with the given name. Returns " +
						"the default-val if the property does not exist or it's value is nil")
					.examples(
						"(system-prop :os.name)", 
						"(system-prop :foo.org \"abc\")", 
						"(system-prop \"os.name\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("system-prop", args, 1, 2);
				
				final VncString key = Coerce.toVncString(
										CoreFunctions.name.apply(
											VncList.of(args.first())));
				final VncVal defaultVal = args.size() == 2 ? args.second() : Nil;
				
				final String val = JavaInterop.getInterceptor().onReadSystemProperty(key.getValue());
	
				return val == null ? defaultVal : new VncString(val);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction java_version = 
			new VncFunction(
					"java-version", 
					VncFunction
						.meta()
						.arglists("(java-version)")		
						.doc("Returns the Jvav VM version.")
						.examples("(java-version)")
						.build()
			) {		
				public VncVal apply(final VncList args) {
					assertArity("java-version", args, 0);
					
					return new VncString(System.getProperty("java.version"));
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("uuid",				uuid)
					.put("objid",				objid)
					.put("current-time-millis",	current_time_millis)
					.put("nano-time",			nano_time)
					.put("gc",					gc)
					.put("sandboxed?",			sandboxed_Q)
					.put("sleep",				sleep)
					.put("callstack",			callstack)
					.put("os-type",				os_type)
					.put("os-type?",			os_type_Q)
					.put("version",				version)
					.put("system-prop",			system_prop)
					.put("java-version",		java_version)
					.toMap();	
	
	
	
	public static final VncKeyword CALLSTACK_KEY_FN_NAME = new VncKeyword(":fn-name");
	public static final VncKeyword CALLSTACK_KEY_FILE = new VncKeyword(":file");
	public static final VncKeyword CALLSTACK_KEY_LINE = new VncKeyword(":line");
	public static final VncKeyword CALLSTACK_KEY_COL = new VncKeyword(":col");
}
