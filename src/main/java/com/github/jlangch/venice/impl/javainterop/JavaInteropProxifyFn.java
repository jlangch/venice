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

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.reflect.ReflectionUtil;


public class JavaInteropProxifyFn extends VncFunction {

	public JavaInteropProxifyFn(final JavaImports javaImports) {
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

	public VncVal apply(final VncList args) {
		if (args.size() != 2) {
			throw new ArityException(args, 2, "proxify");
		}

		final VncVal clazzVal = args.first();
		final String className = Types.isVncKeyword(clazzVal)
				? Coerce.toVncKeyword(clazzVal).getValue()
				: Coerce.toVncString(clazzVal).getValue();

		final Class<?> clazz = ReflectionUtil.classForName(javaImports.resolveClassName(className));

		return new VncJavaObject(
					DynamicInvocationHandler.proxify(
							clazz, 
							Coerce.toVncMap(args.second())));
	}
	
	
    private static final long serialVersionUID = -1848883965231344442L;

	private final JavaImports javaImports;
}
