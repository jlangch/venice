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

/**
 * Continuation
 * 
 * <p>See <a href="https://courses.cs.washington.edu/courses/cse341/04wi/lectures/15-scheme-continuations.html">Scheme continuations</a>.
 * 
 * <p>An expression's continuation is "the computation that will receive the 
 * result of that expression". For example, in the expression
 * 
 * <pre>(+ 4 (+ 1 2))</pre>
 * 
 * <p>the result of <code>(+ 1 2)</code> will be added to <code>4</code>. The 
 * addition to <code>4</code> is that expression's continuation. If we wanted 
 * to represent the continuation of <code>(+ 1 2)</code>, we might write:
 * 
 * <pre>(fn [v] (+ 4 v))</pre>
 * 
 * <p>That is, the continuation of <code>(+ 1 2)</code> takes a value, and adds 
 * four to that value.
 * 
 * <p>Every expression has an implicit continuation. In Venice the current 
 * continuation can be reified as a function by using the 
 * built-in function <code>call-cc</code> (call-with-current-continuation).
 * 
 * <p><code>(call-cc expr)</code> does the following:
 * <ol>
 *   <li>Captures the current continuation.
 *       </li>
 *   <li>Constructs a function C that takes one argument, and applies the 
 *       current continuation with that argument value.
 *       </li>
 *   <li>Passes this function as an argument to expr --- i.e., it invokes 
 *       (expr C).
 *       </li>
 *   <li>Returns the result of evaluating (expr C), unless expr calls C, 
 *       in which case the value that is passed to C is returned.
 *       </li>
 * </ol>
 * 
 * <p>Here is an example:
 * 
 * <pre>
 * (+ 4 (call-cc (fn [cont] (cont (+ 1 2)))))
 * </pre>
 * 
 * <p>This performs exactly the same computation as <code>(+ 4 (+ 1 2))</code>. 
 * However, it uses  <code>call-cc</code> to capture the current continuation, 
 * and then passes the result of evaluating <code>(+ 1 2)</code> directly to 
 * that continuation. Another, roughly equivalent way of writing the above 
 * is as follows:
 * 
 * <pre>
 * (let [f (fn [cont] (cont (+ 1 2)))]
 *   (f (fn [v] (+ 4 v))))
 * </pre>
 */
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