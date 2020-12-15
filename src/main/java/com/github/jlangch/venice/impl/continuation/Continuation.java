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
package com.github.jlangch.venice.impl.continuation;

import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;

// https://en.wikipedia.org/wiki/Continuation
// https://en.wikipedia.org/wiki/Call-with-current-continuation

//
// [1] (e1 (call-cc f))
// [2] ((call-cc f) e2)
//
//     (do 
//       (println 1)
//       (println (call-cc (fn [cont]
//                           (println 2)
//                           (cont 3)
//                           (println "?"))))
//       (println 4))
//
//     (do
//       (def counter (atom 0))
//		 (def saved-cont (atom nil))
//
//       (println (call-cc (fn [cont]
//                           (reset! saved-cont cont)
//                           (cont 100)
//                           (println "?")))))
//       (when (< @counter 3)
//          (swap! counter inc)
//          (println "$")
//          (saved-cont @counter)))

public class Continuation {
		
	public Continuation(final Env env, final VncList ast) {
		this.env = env;
		this.ast = ast;
	}

	public Env getEnv() { 
		return env; 
	}

	public VncVal getAst() { 
		return ast; 
	}
	

	private final Env env;
	private final VncList ast;
}