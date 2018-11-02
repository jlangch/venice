/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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

import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class SystemFunctions {

	///////////////////////////////////////////////////////////////////////////
	// System
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction version = new VncFunction("version") {
		{
			setArgLists("(version)");
			
			setDoc("Returns the version.");
			
			setExamples("(version )");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("version", args, 0);
			
			return new VncString(Version.VERSION);
		}
	};


	public static VncFunction uuid = new VncFunction("uuid") {
		{
			setArgLists("(uuid)");
			
			setDoc("Generates a UUID.");
			
			setExamples("(uuid )");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("uuid", args, 0);
			return new VncString(UUID.randomUUID().toString());
		}
	};

	public static VncFunction current_time_millis = new VncFunction("current-time-millis") {
		{
			setArgLists("(current-time-millis)");
			
			setDoc("Returns the current time in milliseconds.");
			
			setExamples("(current-time-millis)");
		}
		public VncVal apply(final VncList args) {
			assertArity("current-time-millis", args, 0);
			
			return new VncLong(System.currentTimeMillis());
		}
	};

	public static VncFunction nano_time = new VncFunction("nano-time") {
		{
			setArgLists("(nano-time)");
			
			setDoc( "Returns the current value of the running Java Virtual Machine's " +
					"high-resolution time source, in nanoseconds.");
			
			setExamples("(nano-time)");
		}
		public VncVal apply(final VncList args) {
			assertArity("nano-time", args, 0);
			
			return new VncLong(System.nanoTime());
		}
	};

	public static VncFunction sleep = new VncFunction("sleep") {
		{
			setArgLists("(sleep n)");
			
			setDoc("Sleep for n milliseconds.");
			
			setExamples("(sleep 30)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("sleep", args, 1);
			
			try { 
				Thread.sleep(Coerce.toVncLong(args.first()).getValue());
			} catch(Exception ex) {
			}
			
			return Nil;
		}
	};

	public static VncFunction os = new VncFunction("os") {
		{
			setArgLists("(os)");
			
			setDoc("Returns the OS type");
			
			setExamples("(os)");
		}
		
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
	};

	public static VncFunction os_Q = new VncFunction("os?") {
		{
			setArgLists("(os? type)");
			
			setDoc(
				"Returns true if the OS id of the type otherwise false. Type is one " +
				"of :windows, :mac-osx, or :linux");
			
			setExamples("(os? :mac-osx)", "(os? :windows)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("os?", args, 1);
			
			final String type = Coerce.toVncKeyword(args.first()).getValue();
			final String osName = System.getProperty("os.name");
			switch(type) {
				case "windows":
					return  osName.startsWith("Windows") ? True : False;
				case "mac-osx": 
					return  osName.startsWith("Mac OS X") ? True : False;
				case "linux":
					return  osName.startsWith("LINUX") ? True : False;
				default:
					return False;
					
			}
		}
	};

	public static VncFunction sandboxed_Q = new VncFunction("sandboxed?") {
		{
			setArgLists("(sandboxed? )");
			
			setDoc("Returns true if there is a sandbox otherwise false");
			
			setExamples("(sandboxed? )");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("sandboxed?", args, 0);
			
			return JavaInterop.isSandboxed() ? True : False;
		}
	};

	public static VncFunction system_prop = new VncFunction("system-prop") {
		{
			setArgLists("(system-prop name default-val)");
			
			setDoc("Returns the system property with the given name. Returns " +
				   "the default-val if the property does not exist or it's value is nil");
			
			setExamples(
					"(system-prop :os.name)", 
					"(system-prop :foo.org \"abc\")", 
					"(system-prop \"os.name\")");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("system-prop", args, 1, 2);
			
			final VncString key = Coerce.toVncString(
									CoreFunctions.name.apply(
										new VncList(args.first())));
			final VncVal defaultVal = args.size() == 2 ? args.second() : Nil;
			
			final String val = JavaInterop.getInterceptor().onReadSystemProperty(key.getValue());

			return val == null ? defaultVal : new VncString(val);
		}
	};

	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("uuid",				uuid)
					.put("current-time-millis",	current_time_millis)
					.put("nano-time",			nano_time)
					.put("sandboxed?",			sandboxed_Q)
					.put("sleep",				sleep)
					.put("os",					os)
					.put("os?",					os_Q)
					.put("version",				version)
					.put("system-prop",			system_prop)
					.toMap();	
}
