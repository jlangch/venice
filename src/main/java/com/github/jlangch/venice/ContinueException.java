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
package com.github.jlangch.venice;


/**
 * Thrown by the {@code Reader} to signal that it is reading an incomplete form.
 * 
 * <p>The REPL makes use of this to allow the user to continue expressions. 
 * The REPL displays the continue prompt '|' on the next line to request more 
 * input.
 * 
 * <p>E.g.: reading an incomplete vector
 * <pre>
 * venice&gt; [1 2 3
 *       | 
 * </pre>
 */
public class ContinueException extends VncException {

	public ContinueException() {
	}
	

	private static final long serialVersionUID = 7978400430881723919L; 
}