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
package com.github.jlangch.venice.impl.javainterop;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.reflect.ReflectionUtil;


public class JavaInteropFunctions {

	public static List<VncFunction> create(final JavaImports javaImports) {
		return Arrays.asList(
				new JavaFn(javaImports),
				new ProxifyFn(javaImports),
				new SupersFn(javaImports),
				new BasesFn(javaImports));
	}

	
	public static class JavaFn extends VncFunction {

		private JavaFn(final JavaImports javaImports) {
			super(
				".", 
				VncFunction
					.meta()
					.module("core")
					.arglists(
						"(. classname :new args)", 
						"(. classname method-name args)",
						"(. classname field-name)",
						"(. classname :class)",
						"(. object method-name args)", 
						"(. object field-name)",
						"(. object :class)")		
					.doc(
						"Java interop. Calls a constructor or an class/object method or accesses a " +
						"class/instance field. The function is sandboxed.")
					.examples(
						";; invoke constructor \n(. :java.lang.Long :new 10)", 
						";; invoke static method \n(. :java.time.ZonedDateTime :now)",
						";; invoke static method \n(. :java.lang.Math :min 10 20)", 
						";; access static field \n(. :java.lang.Math :PI)",
						";; invoke method \n(. (. :java.lang.Long :new 10) :toString)", 
						";; get class name \n(. :java.lang.Math :class)", 
						";; get class name \n(. (. :java.io.File :new \"/temp\") :class)")
					.build());
			
			this.javaImports = javaImports;
		}

		
		@Override
		public VncVal apply(final VncList args) {
			return JavaInteropUtil.applyJavaAccess(args, javaImports);
		}

		@Override
		public boolean isRedefinable() { 
			return false;  // don't allow redefinition for security reasons
		}
		
	    private static final long serialVersionUID = -1848883965231344442L;

	    private final JavaImports javaImports;
	}

	
	public static class ProxifyFn extends VncFunction {

		public ProxifyFn(final JavaImports javaImports) {
			super(
				"proxify", 
				VncFunction
					.meta()
					.arglists("(proxify classname method-map)")		
					.doc(
						"Proxifies a Java interface to be passed as a Callback object to " +
						"Java functions. The interface's methods are implemented by Venice " +
						"functions.")
					.examples(
						"(do \n" +
						"   (import :java.io.File :java.io.FilenameFilter) \n" +
						"\n" +
						"   (def file-filter \n" +
						"        (fn [dir name] (str/ends-with? name \".xxx\"))) \n" +
						"\n" +
						"   (let [dir (io/tmp-dir )] \n" +
						"        ;; create a dynamic proxy for the interface FilenameFilter\n" +
						"        ;; and implement its function 'accept' by 'file-filter'\n" +
						"        (. dir :list (proxify :FilenameFilter {:accept file-filter}))) \n" +
						")")
					.build());
			
			this.javaImports = javaImports;
		}

		@Override
		public VncVal apply(final VncList args) {
			if (args.size() != 2) {
				throw new ArityException(2, "proxify");
			}

			final Class<?> clazz = JavaInteropUtil.toClass(args.first(), javaImports);

			return new VncJavaObject(
						DynamicInvocationHandler.proxify(
								CallFrame.fromVal("proxify(:" + clazz.getName() +")", args),
								clazz, 
								Coerce.toVncMap(args.second())));
		}
		
		@Override
		public boolean isRedefinable() { 
			return false;  // don't allow redefinition for security reasons
		}
		
		
	    private static final long serialVersionUID = -1848883965231344442L;

		private final JavaImports javaImports;
	}

	
	public static class SupersFn extends VncFunction {

		public SupersFn(final JavaImports javaImports) {
			super(
				"supers", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(supers class)")
					.doc("Returns the immediate and indirect superclasses and interfaces of class, if any.")
					.examples("(supers :java.util.ArrayList)")
					.build());
			
			this.javaImports = javaImports;
		}
	
		@Override
		public VncVal apply(final VncList args) {
			assertArity("supers", args, 1);
					
			final Class<?> clazz = JavaInteropUtil.toClass(args.first(), javaImports);

			final List<Class<?>> classes = new ArrayList<>();

			final List<Class<?>> superclasses = ReflectionUtil.getAllSuperclasses(clazz);
			final List<Class<?>> interfaces = ReflectionUtil.getAllInterfaces(superclasses);
			
			classes.addAll(superclasses);
			classes.addAll(interfaces);
	
			return new VncList(JavaInteropUtil.toVncKeywords(ReflectionUtil.distinct(classes)));
		}

		@Override
		public boolean isRedefinable() { 
			return false;  // don't allow redefinition for security reasons
		}
		
		
	    private static final long serialVersionUID = -1848883965231344442L;

		private final JavaImports javaImports;
	}

	
	public static class BasesFn extends VncFunction {

		public BasesFn(final JavaImports javaImports) {
			super(
				"bases", 
				VncFunction
					.meta()
					.module("core")
					.arglists("(bases class)")
					.doc("Returns the immediate superclass and interfaces of class, if any.")
					.examples("(bases :java.util.ArrayList)")
					.build());
			
			this.javaImports = javaImports;
		}
	
		@Override
		public VncVal apply(final VncList args) {
			assertArity("bases", args, 1);
			
			final Class<?> clazz = JavaInteropUtil.toClass(args.first(), javaImports);
						
			final List<Class<?>> classes = new ArrayList<>();
			final Class<?> superclass = ReflectionUtil.getSuperclass(clazz);
			if (superclass != null) {
				classes.add(superclass);
			}
			classes.addAll(ReflectionUtil.getAllDirectInterfaces(clazz));
	
			return new VncList(JavaInteropUtil.toVncKeywords(ReflectionUtil.distinct(classes)));
		}

		@Override
		public boolean isRedefinable() { 
			return false;  // don't allow redefinition for security reasons
		}
		
		
	    private static final long serialVersionUID = -1848883965231344442L;

		private final JavaImports javaImports;
	}
}
