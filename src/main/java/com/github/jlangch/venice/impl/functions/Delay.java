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

import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class Delay {

	public Delay(final VncFunction fn) {
		this.fn = fn;
	}
	
	public VncVal deref() {
		if (result.get() == null) {
			synchronized(this) {
				if (result.get() == null) {
					try {
						final VncVal res = fn.apply(new VncList());
						result.set(new Result(res, null));
					}
					catch(RuntimeException ex) {
						result.set(new Result(null, ex));
					}
				}				
			}			
		}
		
		return result.get().result();
	}
	
	
	private static class Result {
		public Result(final VncVal val, final RuntimeException ex) {
			this.val = val;
			this.ex = ex;
		}
		
		public VncVal result() {
			if (val != null) {
				return val;
			}
			else {
				throw ex;
			}
		}
		
		private final VncVal val;
		private final RuntimeException ex;
	}
	
	private final VncFunction fn;
	private final AtomicReference<Result> result = new AtomicReference<>();
}
