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
package com.github.jlangch.venice.impl.sandbox;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.Tuple2;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class CompiledSandboxRules {

    private CompiledSandboxRules(
            final List<Pattern> whiteListClassPatterns,
            final List<Pattern> whiteListMethodPatterns,
            final List<Pattern> whiteListClasspathPatterns,
            final Set<String> blackListVeniceFunctions,
            final Set<String> whiteListVeniceModules,
            final Set<String> whiteListSystemProps,
            final Set<String> whiteListSystemEnvs,
            final Integer maxExecTimeSeconds,
            final Integer maxFutureThreadPoolSize
    ) {
        this.whiteListClassPatterns = whiteListClassPatterns == null
                                            ? Collections.emptyList()
                                            : whiteListClassPatterns;

        this.whiteListMethodPatterns = whiteListMethodPatterns == null
                                            ? Collections.emptyList()
                                            : whiteListMethodPatterns;

        this.whiteListClasspathPatterns = whiteListClasspathPatterns == null
                                            ? Collections.emptyList()
                                            : whiteListClasspathPatterns;

        this.blackListVeniceFunctions = blackListVeniceFunctions == null
                                            ? Collections.emptySet()
                                            : blackListVeniceFunctions;

        this.whiteListVeniceModules = whiteListVeniceModules == null
                                            ? Collections.emptySet()
                                            : whiteListVeniceModules;

        this.whiteListSystemProps = whiteListSystemProps;
        this.whiteListSystemEnvs = whiteListSystemEnvs;

        this.maxExecTimeSeconds = maxExecTimeSeconds;
        this.maxFutureThreadPoolSize = maxFutureThreadPoolSize;
    }

    public static CompiledSandboxRules compile(final SandboxRules sandbox) {
        if (sandbox == null) {
            return new CompiledSandboxRules(null, null, null, null, null, null, null, null, null);
        }

        final List<String> rules = sandbox
                                        .unique()
                                        .getRules()
                                        .stream()
                                        .filter(s -> s != null)
                                        .map(s -> s.trim())
                                        .filter(s -> !s.isEmpty())
                                        .collect(Collectors.toList());

        return new CompiledSandboxRules(
                // whitelisted classes
                rules
                    .stream()
                    .filter(s -> s.startsWith("class:"))
                    .map(s -> s.substring("class:".length()))
                    .map(s -> { int pos = s.indexOf(':'); return pos < 0 ? s : s.substring(0, pos); })
                    .map(s -> SandboxRuleCompiler.compile(s))
                    .collect(Collectors.toList()),

                // whitelisted methods
                rules
                    .stream()
                    .filter(s -> s.startsWith("class:"))
                    .map(s -> s.substring("class:".length()))
                    .filter(s -> s.indexOf(':') >= 0)
                    .map(s -> SandboxRuleCompiler.compile(s))
                    .collect(Collectors.toList()),

                // whitelisted classpath resources
                rules
                    .stream()
                    .filter(s -> s.startsWith("classpath:"))
                    .map(s -> s.substring("classpath:".length()))
                    .map(s -> SandboxRuleCompiler.compile(s))
                    .collect(Collectors.toList()),

                // blacklisted venice functions
                blacklistedVeniceFunctions(rules),

                // whitelisted venice modules
                rules
                    .stream()
                    .filter(s -> s.startsWith("venice:module:"))
                    .map(s -> s.substring("venice:module:".length()))
                    .collect(Collectors.toSet()),

                // whitelisted system properties
                allowAccessToAllSystemProperties(rules)
                    ? null
                    : rules
                        .stream()
                        .filter(s -> s.startsWith("system.property:"))
                        .map(s -> s.substring("system.property:".length()))
                        .collect(Collectors.toSet()),

                // whitelisted system environment variables
                allowAccessToAllSystemEnvs(rules)
                    ? null
                    : rules
                        .stream()
                        .filter(s -> s.startsWith("system.env:"))
                        .map(s -> s.substring("system.env:".length()))
                        .collect(Collectors.toSet()),

                sandbox.getMaxExecTimeSeconds(),
                sandbox.getMaxFutureThreadPoolSize());
    }

    /**
     * Returns <code>true</code> if the class is white listed otherwise
     * <code>false</code>
     *
     * @param clazz A class
     * @return <code>true</code> if the class is white listed otherwise
     *         <code>false</code>
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
     *         <code>false</code>
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

    /**
     * Returns <code>true</code> if the classpath resource is white listed otherwise
     * <code>false</code>
     *
     * @param resource A classpath resource
     * @return <code>true</code> if the classpath resource is white listed otherwise
     *         <code>false</code>
     */
    public boolean isWhiteListedClasspathResource(final String resource) {
        if (resource == null) {
            return false;
        }
        else if (whiteListedClasspathResources.containsKey(resource)) {
            return true;
        }
        else {
            final boolean matches = whiteListClasspathPatterns
                                        .stream()
                                        .anyMatch(p -> p.matcher(resource).matches());
            if (matches) {
                // cache the matched resource to prevent the expensive pattern matching
                // for subsequent checks.
                whiteListedClasspathResources.put(resource, "");
                return true;
            }
            return false;
        }
    }

    public boolean isBlackListedVeniceFunction(final String funcName) {
        return blackListVeniceFunctions.contains(funcName);
    }

    public boolean isWhiteListedVeniceModule(final String moduleName) {
        return whiteListVeniceModules.contains(moduleName);
    }

    public boolean isWhiteListedSystemProperty(final String property) {
        return (whiteListSystemProps == null)
                    || (property != null && whiteListSystemProps.contains(property));
    }

    public boolean isWhiteListedSystemEnv(final String name) {
        return (whiteListSystemEnvs == null)
                    || (name != null && whiteListSystemEnvs.contains(name));
    }

    public Integer getMaxExecTimeSeconds() {
        return maxExecTimeSeconds;
    }

    public Integer getMaxFutureThreadPoolSize() {
        return maxFutureThreadPoolSize;
    }

    private static boolean allowAccessToAllSystemProperties(final List<String> rules) {
        return rules.stream().anyMatch(s -> s.equals("system.property:*"));
    }

    private static boolean allowAccessToAllSystemEnvs(final List<String> rules) {
        return rules.stream().anyMatch(s -> s.equals("system.env:*"));
    }

    private static Set<String> blacklistedVeniceFunctions(final List<String> rules) {
        final Set<String> blacklisted = new HashSet<>();

        for(String rule : rules) {
            if (rule.startsWith("blacklist:venice:func:")) {
                final String r = rule.substring("blacklist:venice:func:".length());
                if (r.equals("*io*")) {
                    blacklisted.addAll(RestrictedBlacklistedFunctions.getIoFunctions());
                }
                else if (r.equals("*special-forms*")) {
                    blacklisted.addAll(RestrictedBlacklistedFunctions.getSpecialForms());
                }
                else if (r.equals("*concurrency*")) {
                    blacklisted.addAll(RestrictedBlacklistedFunctions.getConcurrencyFunctions());
                }
                else if (r.equals("*system*")) {
                    blacklisted.addAll(RestrictedBlacklistedFunctions.getSystemFunctions());
                }
                else if (r.equals("*java-interop*")) {
                    blacklisted.addAll(RestrictedBlacklistedFunctions.getJavaInteropFunctions());
                }
                else if (r.equals("*unsafe*")) {
                    blacklisted.addAll(RestrictedBlacklistedFunctions.getAllFunctions());
                }
                else {
                    blacklisted.add(r);
                }
            }
            else if (rule.startsWith("whitelist:venice:func:")) {
                final String r = rule.substring("whitelist:venice:func:".length());
                if (r.equals("*io*")) {
                    blacklisted.removeAll(RestrictedBlacklistedFunctions.getIoFunctions());
                }
                else if (r.equals("*special-forms*")) {
                    blacklisted.removeAll(RestrictedBlacklistedFunctions.getSpecialForms());
                }
                else if (r.equals("*concurrency*")) {
                    blacklisted.removeAll(RestrictedBlacklistedFunctions.getConcurrencyFunctions());
                }
                else if (r.equals("*system*")) {
                    blacklisted.removeAll(RestrictedBlacklistedFunctions.getSystemFunctions());
                }
                else if (r.equals("*java-interop*")) {
                    blacklisted.removeAll(RestrictedBlacklistedFunctions.getJavaInteropFunctions());
                }
                else if (r.equals("*unsafe*")) {
                    blacklisted.removeAll(RestrictedBlacklistedFunctions.getAllFunctions());
                }
                else {
                    blacklisted.remove(r);
                }
            }
            else {
                // skip
            }
        }

        return blacklisted;
    }


    // cached classes and methods that are proofed to be white listed
    private final ConcurrentHashMap<Class<?>, String> whiteListedClasses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Tuple2<Class<?>,String>, String> whiteListedMethods = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> whiteListedClasspathResources = new ConcurrentHashMap<>();

    private final List<Pattern> whiteListClassPatterns;
    private final List<Pattern> whiteListMethodPatterns;
    private final List<Pattern> whiteListClasspathPatterns;
    private final Set<String> blackListVeniceFunctions;
    private final Set<String> whiteListVeniceModules;
    private final Set<String> whiteListSystemProps;
    private final Set<String> whiteListSystemEnvs;
    private final Integer maxExecTimeSeconds;
    private final Integer maxFutureThreadPoolSize;
}
