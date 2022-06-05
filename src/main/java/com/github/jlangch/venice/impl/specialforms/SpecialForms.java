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
					"binding",
					"bound?",
					"catch",
					"do",
					"dobench",
					"doc",
					"dorun",
					"def",
					"defmacro",
					"defmethod",
					"defmulti",
					"defonce",
					"defprotocol",
					"deftype",
					"deftype?",
					"deftype-describe",
					"deftype-of",
					"deftype-or",
					"def-dynamic",
					"dobench",
					"doc",
					"dorun",
					"eval",
					"extend",
					"extends?",
					"finally",
					"fn",
					"if",
					"import",
					"imports",
					"inspect",
					"let",
					"locking",
					"loop",
					"macroexpand",
					"macroexpand-all*",
					"modules",
					"ns",
					"ns-list",
					"ns-remove",
					"ns-unmap",		
					"print-highlight",
					"prof",
					"quasiquote",
					"quote",
					"recur",
					"resolve",
					"set!",
					"tail-pos",
					"try",
					"try-with",
					"var-get",
					"var-global?",
					"var-local?",
					"var-name",
					"var-ns",
					"var-thread-local?",
					".:"));

}
