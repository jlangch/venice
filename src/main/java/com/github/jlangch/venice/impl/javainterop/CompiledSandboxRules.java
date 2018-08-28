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
package com.github.jlangch.venice.impl.javainterop;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.Tuple2;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class CompiledSandboxRules {
	
	private CompiledSandboxRules(
			final List<Pattern> whiteListClassPatterns,
			final List<Pattern> whiteListMethodPatterns,
			final Set<String> blackListVeniceFunctions
	) {
		this.whiteListClassPatterns = whiteListClassPatterns == null 
											? Collections.emptyList() 
											: whiteListClassPatterns;
											
		this.whiteListMethodPatterns = whiteListMethodPatterns == null 
											? Collections.emptyList() 
											: whiteListMethodPatterns;
											
		this.blackListVeniceFunctions = blackListVeniceFunctions == null 
											? Collections.emptySet() 
											: blackListVeniceFunctions;
	}
	
	public static CompiledSandboxRules compile(final SandboxRules whiteList) {
		if (whiteList == null) {
			return new CompiledSandboxRules(null, null, null);			
		}
		
		final List<String> filtered = whiteList
										.getRules()
										.stream()
										.filter(s -> s != null)
										.map(s -> s.trim())
										.filter(s -> !s.isEmpty())
										.collect(Collectors.toList());
		
		return new CompiledSandboxRules(
				// whitelisted classes
				filtered
					.stream()
					.filter(s -> !s.startsWith("blacklist:venice:"))
					.map(s -> { int pos = s.indexOf(':'); return pos < 0 ? s : s.substring(0, pos); })
					.map(s -> SandboxRuleCompiler.compile(s))
					.collect(Collectors.toList()),
					
				// whitelisted methods
				filtered
					.stream()
					.filter(s -> !s.startsWith("blacklist:venice:"))
					.filter(s -> s.indexOf(':') >= 0)
					.map(s -> SandboxRuleCompiler.compile(s))
					.collect(Collectors.toList()),
					
				// blacklisted venice functions
				filtered
					.stream()
					.filter(s -> s.startsWith("blacklist:venice:"))
					.map(s -> s.substring("blacklist:venice:".length()))
					.map(s -> s.equals("*io*") ? CoreFunctions.getAllIoFunctions() : toSet(s))
					.flatMap(Set::stream)
					.collect(Collectors.toSet()));
	}
	
	/**
	 * Returns <code>true</code> if the class is white listed otherwise 
	 * <code>false</code>
	 * 
	 * @param clazz A class
	 * @return <code>true</code> if the class is white listed otherwise 
	 * 		   <code>false</code>
	 */
	public boolean isWhiteListed(final Class<?> clazz) {
		if (clazz == null) {
			return false;
		}
		else if (clazz.isArray() || clazz.isPrimitive()) {
			// Arrays and primitives are implicitly whitelisted
			return true;
		}
		else if (whiteListedClasses.containsKey(clazz)) {
			return true;
		}
		else {
			
			final String className = clazz.getName();
			final boolean matches = whiteListClassPatterns
										.stream()
										.anyMatch(p -> p.matcher(className).matches());
			if (matches) {
				// cache the matched class to prevent the expensive pattern matching 
				// for subsequent checks.
				whiteListedClasses.put(clazz, "");
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Returns <code>true</code> if the class/accessor is white listed otherwise 
	 * <code>false</code>
	 * 
	 * @param clazz A class
	 * @param accessor An accessor (method or field name)
	 * @return <code>true</code> if the class/accessor is white listed otherwise 
	 * 		   <code>false</code>
	 */
	public boolean isWhiteListed(final Class<?> clazz, final String accessor) {
		if (clazz == null || accessor == null) {
			return false;
		}
	
		// Check class
		if (!isWhiteListed(clazz)) {
			return false;
		}
		if (clazz.isArray()) {
			return isWhiteListed(clazz.getComponentType());
		}
		
		// Check accessor
		final Tuple2<Class<?>,String> tuple = new Tuple2<>(clazz,accessor);
		if (whiteListedMethods.containsKey(tuple)) {
			return true;
		}
		else {
			final String path = clazz.getName() + ":" + accessor;
			final boolean matches = whiteListMethodPatterns
										.stream()
										.anyMatch(p -> p.matcher(path).matches());
			if (matches) {
				whiteListedMethods.put(tuple, "");
				return true;
			}
			return false;
		}
	}

	public boolean isBlackListedVeniceFunction(
			final String funcName, 
			final VncList args
	) {
		return blackListVeniceFunctions.contains(funcName);
	}
	
	private static Set<String> toSet(final String... args) {
		return new HashSet<>(Arrays.asList(args));
	}

	
	// cached classes and methods that are proofed to be white listed
	private final ConcurrentHashMap<Class<?>, String> whiteListedClasses = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Tuple2<Class<?>,String>, String> whiteListedMethods = new ConcurrentHashMap<>();
	
	private final List<Pattern> whiteListClassPatterns;
	private final List<Pattern> whiteListMethodPatterns;
	private final Set<String> blackListVeniceFunctions;
}
