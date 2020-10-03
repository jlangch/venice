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
package com.github.jlangch.venice.impl.types;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.collections.VncList;


public interface IJavaBridgeFunction 
	extends IVncFunction,
	        Function<Object,Object>, 
            Predicate<Object>,
            Consumer<Object>,
            Supplier<Object>,
            Runnable,
            Callable<Object> {


	@Override // Function<>::apply()
	default Object apply(Object t) { 
		return apply(
				VncList.of(JavaInteropUtil.convertToVncVal(t))
					   .convertToJavaObject()); 
	}
	
	@Override // Predicate<>::test()
	default boolean test(Object t) {
		final VncVal ret = apply(
							VncList.of(JavaInteropUtil.convertToVncVal(t))); 
		if (ret instanceof VncBoolean) {
			return (((VncBoolean)ret).getValue() == Boolean.TRUE);
		}
		else {
			return ret != Constants.Nil;
		}
	}

	@Override // Consumer<>::accept()
	default void accept(Object t) { 
		apply(VncList.of(JavaInteropUtil.convertToVncVal(t))
					 .convertToJavaObject()); 
	}
	
	@Override // Supplier<>::get()
	default Object get() { 
		return apply(VncList.empty()).convertToJavaObject(); 
	}
	
	@Override // Runnable::run()
	default void run() { 
		apply(VncList.empty()); 
	}
	
	@Override // Callable<>::call()
	default Object call() throws Exception { 
		return apply(VncList.empty()).convertToJavaObject(); 
	}
}