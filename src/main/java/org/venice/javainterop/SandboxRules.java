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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Keeps the sandbox rules.
 * 
 * <p>The sandbox keeps whitelist rules for the Java Interop and blacklist rules for the
 * Venice functions.
 * 
 * <p>Java whitelist rules for class/instance accessor follow the schema: 
 * '{package}.{className}:{methodName | fieldName}'. The asterix may be used as a wildcard
 * 
 * <p>
 * E.g: white listing Java Interop
 * <ul>
 *   <li>java.lang.Boolean (allow calling Java methods with arguments or return values of type Boolean)</li>
 *   <li>java.lang.* (allow calling Java methods with arguments or return values of any type in the package 'java.lang')</li>
 *   <li>java.lang.Long:new (allow calling Long constructor)</li>
 *   <li>java.lang.Math:abs (allow calling Math::abs method)</li>
 *   <li>java.lang.Math:* (allow calling all Math constructors/methods/fields)</li>
 *   <li>java.lang.*:*  (allow calling all constructors/methods/fields for classes in the package 'java.lang')</li>
 * </ul>
 * 
 * <p>
 * E.g: black listing Venice I/O functions
 * <ul>
 *   <li>blacklist:venice:slurp (reject calls to 'slurp')</li>
 *   <li>blacklist:venice:*io* (reject all Venice I/O calls like 'slurp', 'create-file', ...)</li>
 *   <li>blacklist:venice:. (reject java interop)</li>
 * </ul>
 */
public class SandboxRules {
	
	public SandboxRules() {
	}
	
	public SandboxRules add(final Collection<String> rules) {
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
	
	public SandboxRules merge(final SandboxRules other) {
		final SandboxRules merged = new SandboxRules();
		merged.add(this.rules);
		merged.add(other.rules);
		return merged;
	}
	
	public Set<String> getRules() {
		return Collections.unmodifiableSet(rules);
	}
	
	
	private final Set<String> rules = new HashSet<>();
}
