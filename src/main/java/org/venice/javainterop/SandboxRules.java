/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2014-2018 Venice
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
package org.venice.javainterop;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Keeps white listed classes and tuples of white listed classes/accessors.
 * 
 * <p>If a class is white listed all its accessors are implicitly white listed.
 * Accessors are static and instance methods as well as static and instance 
 * fields.
 * 
 * 
 * <p>A class/accessor follows the rule 
 * '{path}.{className}:{methodName | fieldName}'.
 * The asterix may be used as a wildcard
 * 
 * <p>
 * E.g:
 * <ul>
 *   <li>java.lang.Long</li>
 *   <li>java.lang.Long:new</li>
 *   <li>java.lang.Math</li>
 *   <li>java.lang.Math:abs</li>
 *   <li>java.lang.Math:*</li>
 *   <li>java.lang.*</li>
 *   <li>java.lang.*:*</li>
 *   <li>blacklist:venice:slurp</li>
 *   <li>blacklist:venice:. (disables java interop)</li>
 * </ul>
 */
public class SandboxRules {
	
	public SandboxRules() {
	}
	
	
	public SandboxRules add(final List<String> rules) {
		if (rules != null) {
			this.rules.addAll(rules);
		}
		return this;
	}
	
	public SandboxRules add(final String... rules) {
		if (rules != null) {
			this.rules.addAll(Arrays.asList(rules));
		}
		return this;
	}
	
	public SandboxRules rejectAllVeniceIoFunctions() {
		if (rules != null) {
			this.rules.add("blacklist:venice:*io*");
		}
		return this;
	}
	
	public Set<String> getRules() {
		return Collections.unmodifiableSet(rules);
	}
	
	
	private final Set<String> rules = new HashSet<>();
}
