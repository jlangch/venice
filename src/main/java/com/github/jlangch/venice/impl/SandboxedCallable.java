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
package com.github.jlangch.venice.impl;

import java.util.concurrent.Callable;

import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.concurrent.ThreadContext;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class SandboxedCallable<T> implements Callable<T> {
	public SandboxedCallable(
			final IInterceptor interceptor,
			final Callable<T> callable
	) {
		this.interceptor = interceptor;
		this.callable = callable;
	}

	@Override
	public T call() throws Exception {
		try {
			ThreadContext.remove(); // clean thread locals			
			JavaInterop.register(interceptor);
			
			return callable.call();
		}
		finally {
			// clean up
			JavaInterop.unregister();
			ThreadContext.remove();
		}
	}
	
	
	private final IInterceptor interceptor;
	private final Callable<T> callable;
}