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
package org.venice.impl.javainterop;

import org.venice.impl.types.VncFunction;
import org.venice.impl.types.VncVal;
import org.venice.impl.types.collections.VncList;


public class JavaInteropFn extends VncFunction {

	private JavaInteropFn(final JavaImports javaImports) {
		super(".");
		
		this.javaImports = javaImports;
		
		setArgLists("(. classname :new args)", "(. object method args)", "(. classname :class)");
		
		setDescription(
				"Java interop. Calls a constructor or an object method. " +
				"The function is sandboxed");
	}
	
	public static JavaInteropFn create(final JavaImports javaImports) {
		return new JavaInteropFn(javaImports);
	}

	public VncVal apply(final VncList args) {
		JavaInterop.getInterceptor().checkBlackListedVeniceFunction(".", args);
		
		return JavaInteropUtil.applyJavaAccess(args, javaImports);
	}

	
	private final JavaImports javaImports;
}
