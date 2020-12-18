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
package com.github.jlangch.venice.impl.javainterop;

import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;


public class JavaInterop {

	public static boolean isSandboxed() {
		return !(getInterceptor() instanceof AcceptAllInterceptor);
	}
	
	public static Integer getMaxExecutionTimeSeconds() {
		return getInterceptor().getMaxExecutionTimeSeconds();
	}
	
	
	public static void register(final IInterceptor interceptor) {
		threadInterceptor.set(interceptor == null ? new AcceptAllInterceptor() : interceptor);
	}

	public static void unregister() {
		try {
			threadInterceptor.remove();
		}
		catch(Exception ex) {
			// do not care
		}
	}

	
	private static final ThreadLocal<IInterceptor> threadInterceptor = 
			ThreadLocal.withInitial(() -> new RejectAllInterceptor());
	
	public static IInterceptor getInterceptor() {
		return threadInterceptor.get();
	}
}
