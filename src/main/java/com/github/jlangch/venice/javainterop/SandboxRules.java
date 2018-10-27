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
package com.github.jlangch.venice.javainterop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Defines the Venice's sandbox rules.
 * 
 * <p>The sandbox keeps whitelist rules for the Java Interop and blacklist rules 
 * for the Venice functions.
 */
public class SandboxRules {
	
	public SandboxRules() {
		this(true);
	}

	private SandboxRules(boolean withDefaults) {
		if (withDefaults) {
			withDefaultClasses();
		}
	}

	/**
	 * Creates new SandboxRules starting without any defaults.
	 * 
	 * @return <code>SandboxRules</code>
	 */
	public static SandboxRules noDefaults() {
		return new SandboxRules(false);
	}

	/**
	 * Add whitelisted class rules to the sandbox.
	 * 
	 * <p>Java whitelist rules for class/instance accessor follow the schema: 
	 * '{package}.{className}:{methodName | fieldName}'. The asterix may be used as a wildcard
	 * 
	 * <p>
	 * E.g:
	 * <ul>
	 *   <li>java.lang.Boolean (allow calling Java methods with arguments or return values of type Boolean)</li>
	 *   <li>java.lang.* (allow calling Java methods with arguments or return values of any type in the package 'java.lang')</li>
	 *   <li>java.lang.Long:new (allow calling Long constructor)</li>
	 *   <li>java.lang.Math:abs (allow calling Math::abs method)</li>
	 *   <li>java.lang.Math:* (allow calling all Math constructors/methods/fields)</li>
	 *   <li>java.lang.*:*  (allow calling all constructors/methods/fields for classes in the package 'java.lang')</li>
	 * </ul>
	 * 
	 * @param rules rules
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withClasses(final String... rules) {
		if (rules != null) {
			withClasses(Arrays.asList(rules));
		}
		return this;
	}
	
	/**
	 * Add whitelisted class rules to the sandbox.
	 * 
	 * <p>Java whitelist rules for class/instance accessor follow the schema: 
	 * '{package}.{className}:{methodName | fieldName}'. The asterix may be used as a wildcard
	 * 
	 * <p>
	 * E.g:
	 * <ul>
	 *   <li>java.lang.Boolean (allow calling Java methods with arguments or return values of type Boolean)</li>
	 *   <li>java.lang.* (allow calling Java methods with arguments or return values of any type in the package 'java.lang')</li>
	 *   <li>java.lang.Long:new (allow calling Long constructor)</li>
	 *   <li>java.lang.Math:abs (allow calling Math::abs method)</li>
	 *   <li>java.lang.Math:* (allow calling all Math constructors/methods/fields)</li>
	 *   <li>java.lang.*:*  (allow calling all constructors/methods/fields for classes in the package 'java.lang')</li>
	 *   <li>ava.awt.**:*  (allow calling all constructors/methods/fields for classes in the package 'java.awt' and all its subpackages)
	 * </ul>
	 * 
	 * @param rules rules
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withClasses(final List<String> rules) {
		if (rules != null) {
			this.rules.addAll(
				rules.stream().map(r -> "class:" + r).collect(Collectors.toList()));
		}
		return this;
	}
	
	/**
	 * Add a rule for classes to the sandbox, whitelisting the class and all its 
	 * methods and fields 
	 * 
	 * <p>Adds a class rule "x.y.classname:*" for each class
	 * 
	 * @param classes classes
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withClasses(final Class<?>... classes) {
		if (classes != null) {
			withClasses(Arrays.asList(classes));
		}
		return this;
	}
	
	/**
	 * Add a rule for classes to the sandbox, whitelisting the class and all its 
	 * methods and fields 
	 * 
	 * <p>Adds a class rule "x.y.classname:*" for each class
	 * 
	 * @param classes classes
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withClasses(final Collection<Class<?>> classes) {
		if (classes != null) {
			withClasses(classes.stream().map(c -> c.getName() + ":*").collect(Collectors.toList()));
		}
		return this;
	}
	
	public SandboxRules withDefaultClasses() {
		withClasses(DEFAULT_CLASS_RULES);
		return this;
	}
	
	/**
	 * Add whitelisted classpath resource rules to the sandbox.
	 * 
	 * E.g: 
	 * <ul>
	 *   <li>/foo/org/image.png</li>
	 *   <li>/foo/org/*.png</li>
	 *   <li>/foo/org/*.jpg</li>
	 * </ul>
	 * 
	 * @param rules rules
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withClasspathResources(final String... rules) {
		if (rules != null) {
			withClasspathResources(Arrays.asList(rules));
		}
		return this;
	}
	
	/**
	 * Add whitelisted classpath resource rules to the sandbox.
	 * 
	 * E.g: 
	 * <ul>
	 *   <li>/foo/org/image.png</li>
	 *   <li>/foo/org/*.png</li>
	 *   <li>/foo/org/*.jpg</li>
	 * </ul>
	 * 
	 * @param rules rules
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withClasspathResources(final Collection<String> rules) {
		if (rules != null) {
			this.rules.addAll(
				rules.stream().map(r -> "classpath:" + r).collect(Collectors.toList()));
		}
		return this;
	}
	
	/**
	 * Add whitelisted system property rules to the sandbox.
	 * 
	 * <p>
	 * E.g: white listing Java system properties
	 * <ul>
	 *   <li>file.separator</li>
	 *   <li>java.home</li>
	 * </ul>
	 * 
	 * @param rules rules
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withSystemProperties(final String... rules) {
		if (rules != null) {
			withSystemProperties(Arrays.asList(rules));
		}
		return this;
	}
	
	/**
	 * Add whitelisted system property rules to the sandbox.
	 * 
	 * <p>
	 * E.g: white listing Java system properties
	 * <ul>
	 *   <li>file.separator</li>
	 *   <li>java.home</li>
	 * </ul>
	 * 
	 * @param rules rules
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withSystemProperties(final Collection<String> rules) {
		if (rules != null) {
			this.rules.addAll(
				rules.stream().map(r -> "system.property:" + r).collect(Collectors.toList()));
		}
		return this;
	}
	
	/**
	 * Add blacklisted Venice IO function rules to the sandbox.
	 * 
	 * <p>
	 * E.g:
	 * <ul>
	 *   <li>io/slurp (reject calls to 'io/slurp')</li>
	 *   <li>*io* (reject all Venice I/O calls like 'io/slurp', 'create-file', ...)</li>
	 *   <li>. (reject java interop completely)</li>
	 * </ul>
	 * 
	 * @param rules rules
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withBlacklistedVeniceFn(final String... rules) {
		if (rules != null) {
			withBlacklistedVeniceFn(Arrays.asList(rules));
		}
		return this;
	}
	
	/**
	 * Add blacklisted Venice IO function rules to the sandbox.
	 * 
	 * <p>
	 * E.g:
	 * <ul>
	 *   <li>io/slurp (reject calls to 'io/slurp')</li>
	 *   <li>*io* (reject all Venice I/O calls like 'io/slurp', 'create-file', ...)</li>
	 *   <li>. (reject java interop completely)</li>
	 * </ul>
	 * 
	 * @param rules rules
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules withBlacklistedVeniceFn(final Collection<String> rules) {
		if (rules != null) {
			this.rules.addAll(
				rules.stream().map(r -> "blacklist:venice:" + r).collect(Collectors.toList()));
		}
		return this;
	}

	
	/**
	 * Reject access to all Venice I/O related functions
	 * 
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules rejectAllVeniceIoFunctions() {
		if (rules != null) {
			withBlacklistedVeniceFn("*io*");
		}
		return this;
	}
	
	/**
	 * Allow access to all standard Java system properties
	 * 
	 * <p>Standard system properties:
	 * <ul>
	 *   <li>file.separator</li>
	 *   <li>java.home</li>
	 *   <li>java.vendor</li>
	 *   <li>java.vendor.url</li>
	 *   <li>java.version</li>
	 *   <li>line.separator</li>
	 *   <li>os.arch</li>
	 *   <li>os.name</li>
	 *   <li>os.version</li>
	 *   <li>path.separator</li>
	 *   <li>user.dir</li>
	 *   <li>user.home</li>
	 *   <li>user.name</li>
	 * </ul>
	 * 
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules allowAccessToStandardSystemProperties() {
		withSystemProperties(DEFAULT_SYSTEM_PROPERTIES);
		return this;
	}
	
	/**
	 * Allow access to all Java system properties
	 * 
	 * @return this <code>SandboxRules</code>
	 */
	public SandboxRules allowAccessToAllSystemProperties() {
		withSystemProperties("*");
		return this;
	}
		
	/**
	 * Merges this <code>SandboxRules</code> with the passed other 
	 * <code>SandboxRules</code> 
	 * 
	 * @param other the other SandboxRules to merge with
	 * @return the new merged <code>SandboxRules</code>
	 */
	public SandboxRules merge(final SandboxRules other) {
		final SandboxRules merged = new SandboxRules();
		merged.rules.addAll(this.rules);
		merged.rules.addAll(other.rules);
		return merged;
	}
	
	/**
	 * @return the rules of this <code>SandboxRules</code>
	 */
	public Set<String> getRules() {
		return Collections.unmodifiableSet(rules);
	}

	@Override
	public String toString() {
		return new ArrayList<String>(rules)
					.stream()
					.sorted()
					.collect(Collectors.joining("\n"));
	}
		
	private static final List<String> DEFAULT_CLASS_RULES = 
			Arrays.asList(
				// Dynamic proxies based on venice' DynamicInvocationHandler
				"com.github.jlangch.venice.javainterop.DynamicInvocationHandler*:*",
				
				// ValueException
				"com.github.jlangch.venice.ValueException:*",
				"com.github.jlangch.venice.impl.types.collections.VncVector",
				
				// Venice dynamic proxies
				"com.sun.proxy.$Proxy*:*",
				

				"java.lang.IllegalArgumentException:*",
				"java.lang.RuntimeException:*",
				"java.lang.Exception:*",
				"java.lang.SecurityException:*",
				"java.io.IOException:*",
				
				"java.io.PrintStream",
				"java.io.InputStream",
				"java.io.OutputStream",
				
				"java.nio.ByteBuffer",
				"java.nio.HeapByteBuffer:*",

				"java.lang.Object",
				"java.lang.Object:class",
	
				java.lang.Character.class.getName(),
				java.lang.String.class.getName(),
				java.lang.Boolean.class.getName(),
				java.lang.Integer.class.getName(),
				java.lang.Long.class.getName(),
				java.lang.Float.class.getName(),
				java.lang.Double.class.getName(),
				java.lang.Byte.class.getName(),
				java.lang.StringBuffer.class.getName(),
				java.lang.StringBuilder.class.getName(),
				
				java.math.BigInteger.class.getName(),
				java.math.BigDecimal.class.getName(),
				
				java.util.Date.class.getName(),						
				java.util.ArrayList.class.getName(),
				java.util.HashSet.class.getName(),
				java.util.HashMap.class.getName(),
				java.util.LinkedHashMap.class.getName(),
				java.util.Locale.class.getName(),

				java.util.ArrayList.class.getName(),
				java.util.HashSet.class.getName(),
				java.util.HashMap.class.getName(),
				java.util.LinkedHashMap.class.getName());

	public static final Set<String> DEFAULT_SYSTEM_PROPERTIES = 
			Collections.unmodifiableSet(
				new HashSet<>(
					Arrays.asList(
							"file.separator",
							"java.home",
							"java.vendor",
							"java.vendor.url",
							"java.version",
							"line.separator",
							"os.arch",
							"os.name",
							"os.version",
							"path.separator",
							"user.dir",
							"user.home",
							"user.name")));


	private final Set<String> rules = new HashSet<>();
}
