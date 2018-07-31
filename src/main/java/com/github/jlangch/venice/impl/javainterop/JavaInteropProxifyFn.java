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
package com.github.jlangch.venice.impl.javainterop;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.javainterop.DynamicInvocationHandler;


public class JavaInteropProxifyFn extends VncFunction {

	public JavaInteropProxifyFn() {
		super("proxify");
		
		setArgLists("(proxify classname method-map)");
		
		setDoc(
				"Proxifies a Java interface to be passed as a Callback object to " +
				"Java functions. The interface's methods are implemented by Venice " +
				"functions.");
		
		setExamples();
	}

	public VncVal apply(final VncList args) {
		if (args.size() != 2) {
			throw new ArityException(args, 2, "proxify");
		}

		return new VncJavaObject(
					DynamicInvocationHandler.proxify(
							args.first(), 
							Coerce.toVncMap(args.second())));
	}
}
