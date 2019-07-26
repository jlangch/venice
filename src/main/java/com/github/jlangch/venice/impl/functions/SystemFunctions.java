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
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertMinArity;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
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
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class SystemFunctions {

	///////////////////////////////////////////////////////////////////////////
	// System
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction version = 
		new VncFunction(
				"version", 
				VncFunction
					.meta()
					.module("core")
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
					.module("core")
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
				"objid", 
				VncFunction
					.meta()
					.module("core")
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
					.module("core")
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
					.module("core")
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
		
	public static VncFunction format_nano_time = 
		new VncFunction(
				"format-nano-time", 
				VncFunction
					.meta()
					.module("core")
					.arglists(
						"(format-nano-time time)",
						"(format-nano-time time & options)")		
					.doc(
						"Formats a time given in nanoseconds as long or double. \n\n" +
						"Options: \n" +
						"  :precision p - e.g :precision 4 (defaults to 3)")
					.examples(
						"(format-nano-time 203)",
						"(format-nano-time 20389.0 :precision 2)",
						"(format-nano-time 203898888)",
						"(format-nano-time 20386766988 :precision 6)")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("format-nano-time", args, 1);
				
				final VncVal val = args.first();

				if (Types.isVncLong(val) || Types.isVncInteger(val)) {
					final long time = Numeric.toLong(val).getValue();
					
					if (time < 1_000) {
						return new VncString(String.format("%d ns", time));
					}
				}

				final VncHashMap options = VncHashMap.ofAll(args.rest());
				final int precision = Coerce.toVncLong(options.get(new VncKeyword("precision"), new VncLong(3)))
											.getIntValue(); 					

				final double time = Numeric.toDouble(val).getValue();
				
				String unit = "s";
				double scale = 1_000_000_000.0D;
				
				if (time < 1_000.0D) {
					unit = "ns";
					scale = 1.0D;
				}
				else if (time < 1_000_000.0D) {
					unit = "µs";
					scale = 1_000.0D;
				}
				else if (time < 1_000_000_000.0D) {
					unit = "ms";
					scale = 1_000_000.0D;
				}
				
				return new VncString(String.format("%." + precision + "f " + unit, time / scale));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction sleep = 
		new VncFunction(
				"sleep", 
				VncFunction
					.meta()
					.module("core")
					.arglists(
						"(sleep n)", 
						"(sleep n time-unit)")		
					.doc(
						"Sleep for the time n. The default time unit is milliseconds \n" +
						"Time unit is one of :milliseconds, :seconds, :minutes, :hours, or :days. ")
					.examples(
						"(sleep 30)", 
						"(sleep 30 :milliseconds)", 
						"(sleep 5 :seconds)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("sleep", args, 1, 2);
				
				try {
					final long sleep = Coerce.toVncLong(args.first()).getValue();
					final TimeUnit unit = args.size() == 1 
											? TimeUnit.MILLISECONDS 
											: toTimeUnit(Coerce.toVncKeyword(args.second()));

					Thread.sleep(Math.max(0, unit.toMillis(sleep)));
				} 
				catch(InterruptedException ex) {
					throw new com.github.jlangch.venice.InterruptedException("(sleep n) interrupted", ex);
				} 
				
				return Nil;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction pid = 
		new VncFunction(
				"pid", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(pid)")		
					.doc("Returns the PID of this process.")
					.examples("(pid)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("pid", args, 0);
			     
				return new VncString(
							ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction gc = 
		new VncFunction(
				"gc", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(gc)")		
					.doc(
						"Run the Java garbage collector. Runs the finalization methods " +
						"of any objects pending finalization prior to the GC.")
					.examples("(gc)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("gc", args, 0);
				
				Runtime.getRuntime().runFinalization();
				Runtime.getRuntime().gc();
				
				return Nil;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction shutdown_hook = 
		new VncFunction(
				"shutdown-hook", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(shutdown-hook f)")		
					.doc("Registers the function f as JVM shutdown hook.")
					.examples("(shutdown-hook (fn [] (println \"shutdown\")))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("shutdown-hook", args, 1);
				
				final VncFunction fn = Coerce.toVncFunction(args.first());

				final IInterceptor parentInterceptor = JavaInterop.getInterceptor();
				
				// thread local values from the parent thread
				final AtomicReference<Map<VncKeyword,VncVal>> parentThreadLocals = 
						new AtomicReference<>(ThreadLocalMap.getValues());

				Runtime.getRuntime().addShutdownHook(new Thread() {
				    public void run() {					
						try {
							// inherit thread local values to the child thread
							ThreadLocalMap.setValues(parentThreadLocals.get());
							ThreadLocalMap.clearCallStack();
							JavaInterop.register(parentInterceptor);	
							
							fn.apply(new VncList());
						}
						finally {
							// clean up
							JavaInterop.unregister();
							ThreadLocalMap.remove();
						}
				    }
				});	
				
				return Nil;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		

	public static VncFunction callstack = 
		new VncFunction(
				"callstack", 
				VncFunction
					.meta()
					.module("core")
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
					.module("core")
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
					.module("core")
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
					.module("core")
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
					.module("core")
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
					.module("core")
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
	
	private static TimeUnit toTimeUnit(final VncKeyword unit) {
		switch(unit.getValue()) {
			case "milliseconds": return TimeUnit.MILLISECONDS;
			case "seconds": return TimeUnit.SECONDS;
			case "minutes":  return TimeUnit.MINUTES;
			case "hours": return TimeUnit.HOURS;
			case "days": return TimeUnit.DAYS;
			default: throw new VncException("Invalid scheduler time-unit " + unit.getValue());
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("uuid",				uuid)
					.put("objid",				objid)
					.put("current-time-millis",	current_time_millis)
					.put("nano-time",			nano_time)
					.put("format-nano-time", 	format_nano_time)
					.put("pid",					pid)
					.put("gc",					gc)
					.put("shutdown-hook",		shutdown_hook)					
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
