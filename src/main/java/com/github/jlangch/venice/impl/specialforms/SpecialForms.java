/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.specialforms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class SpecialForms {
	
	public static boolean isSpecialForm(final String name) {
		return FORMS.contains(name);
	}

	public static Set<String> FORMS = new HashSet<>(
			Arrays.asList(
					"gensym",
					"doc",
					"modules",
					"fn",
					"eval",
					"resolve",
					"var-get",
					"var-name",
					"var-ns",
					"var-local?",
					"var-thread-local?",
					"var-global?",
					"def",
					"def-",
					"defn",
					"defn-",
					"defonce",
					"defmulti",
					"defmethod",
					"deftype",
					"deftype?",
					"deftype-of",
					"deftype-or",
					".:",
					"deftype-describe",
					"defprotocol",
					"extend",
					"extends?",
					"def-dynamic",
					"binding",
					"bound?",
					"set!",
					"do",
					"if",
					"let",
					"loop",
					"recur",
					"try",
					"try-with",
					"catch",
					"finally",
					"locking",
					"defmacro",
					"macroexpand",
					"ns",
					"ns-unmap",
					"ns-remove",
					"ns-list",
					"quote",
					"quasiquote",
					"tail-pos",
					"inspect",
					"import",
					"imports",
					"dobench",
					"dorun",
					"prof"));

}
