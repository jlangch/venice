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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.venice.impl.CoreFunctions;
import org.venice.impl.types.collections.VncList;
import org.venice.impl.util.Tuple2;


/**
 * Keeps white listed classes and tuples of white listed classes/accessors.
 * 
 * <p>If a class is white listed all its accessors are implicitly white listed.
 * Accessors are static and instance methods as well as static and instance 
 * fields.
 * 
 * <p>This class is <b>thread safe</b>!
 */
public class WhiteList {
	
	private WhiteList(
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
	
	/**
	 * Creates a new white list
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
	 * 
	 * @param whiteList A mandatory list of class/accessor patterns
	 * 
	 * @return the white list
	 */
	public static WhiteList create(final List<String> whiteList) {
		if (whiteList == null) {
			return new WhiteList(null, null, null);			
		}
		
		final List<String> filtered = whiteList
										.stream()
										.filter(s -> s != null)
										.map(s -> s.trim())
										.filter(s -> !s.isEmpty())
										.collect(Collectors.toList());
		
		return new WhiteList(
				filtered
					.stream()
					.filter(s -> !s.startsWith("blacklist:venice:"))
					.map(s -> { int pos = s.indexOf(':'); return pos < 0 ? s : s.substring(0, pos); })
					.map(s -> s.replaceAll("[*]", "[^.]*"))
					.map(s -> Pattern.compile(s))
					.collect(Collectors.toList()),
				filtered
					.stream()
					.filter(s -> !s.startsWith("blacklist:venice:"))
					.filter(s -> s.indexOf(':') >= 0)
					.map(s -> s.replaceAll("[*]", "[^.]*"))
					.map(s -> Pattern.compile(s))
					.collect(Collectors.toList()),
				filtered
					.stream()
					.filter(s -> s.startsWith("blacklist:venice:"))
					.map(s -> s.substring("blacklist:venice:".length()))
					.map(s -> s.equals("*io*") ? CoreFunctions.getAllIoFunctions() : toSet(s))
					.flatMap(Set::stream)
					.collect(Collectors.toSet()));
	}
	
	public static WhiteList create(final String... whiteList) {
		return create(whiteList == null ? null : Arrays.asList(whiteList));
	}
	
	public static WhiteList create(
			final boolean rejectAllVeniceIoFunctions,
			final List<String> whiteList
	) {
		if (rejectAllVeniceIoFunctions) {
			if (whiteList == null) {
				return create(Arrays.asList("blacklist:venice:*io*"));
			}
			else {
				final List<String> wl = new ArrayList<>(whiteList);
				wl.add("blacklist:venice:*io*");
				return create(wl);
			}
		}
		else {
			return create(whiteList);
		}
	}
	
	public static WhiteList create(
			final boolean rejectAllVeniceIoFunctions,
			final String... whiteList
	) {
		return create(
				rejectAllVeniceIoFunctions, 
				whiteList == null ? null : Arrays.asList(whiteList));
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
		if (!isWhiteListed(clazz)) {
			return false;
		}
		
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
