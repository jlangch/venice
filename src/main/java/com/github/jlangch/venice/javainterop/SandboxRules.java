/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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

import com.github.jlangch.venice.impl.sandbox.SandboxDefaultRules;


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

    private SandboxRules(
            final List<String> rules,
            final Integer maxExecTimeSeconds,
            final Integer maxFutureThreadPoolSize
    ) {
        this.rules.addAll(rules);
        this.maxExecTimeSeconds = maxExecTimeSeconds;
        this.maxFutureThreadPoolSize = maxFutureThreadPoolSize;
    }

    /**
     * Builds a sandbox from this rules.
     *
     * @return a SandboxInterceptor
     */
    public SandboxInterceptor sandbox() {
        return new SandboxInterceptor(this);
    }

    /**
     * Creates new SandboxRules starting without any defaults.
     *
     * @return {@code SandboxRules}
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
     *   <li>java.awt.**:*  (allow calling all constructors/methods/fields for classes in the package 'java.awt' and all its subpackages)
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
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
     *   <li>java.awt.**:*  (allow calling all constructors/methods/fields for classes in the package 'java.awt' and all its subpackages)
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules withClasses(final List<String> rules) {
        if (rules != null) {
            this.rules.addAll(
                rules.stream()
                     .map(r -> r.startsWith("class:") ? r : "class:" + r)
                     .collect(Collectors.toList()));
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
     * @return this {@code SandboxRules}
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
     * @return this {@code SandboxRules}
     */
    public SandboxRules withClasses(final Collection<Class<?>> classes) {
        if (classes != null) {
            withClasses(classes.stream().map(c -> c.getName() + ":*").collect(Collectors.toList()));
        }
        return this;
    }

    public SandboxRules withDefaultClasses() {
        withClasses(SandboxDefaultRules.SYSTEM_CLASS_RULES);
        withClasses(SandboxDefaultRules.DEFAULT_CLASS_RULES);
        return this;
    }

    /**
     * Add whitelisted classpath resource rules to the sandbox.
     *
     * E.g:
     * <ul>
     *   <li>"foo/org/image.png2</li>
     *   <li>"foo/org/*.png"</li>
     *   <li>"foo/org/*.jpg"</li>
     *   <li>"foo/&#42;&#42;/*.jpg"</li>
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
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
     *   <li>"foo/org/image.png"</li>
     *   <li>"foo/org/*.png"</li>
     *   <li>"foo/org/*.jpg"</li>
     *   <li>"foo/&#42;&#42;/*.jpg"</li>
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules withClasspathResources(final Collection<String> rules) {
        if (rules != null) {
            this.rules.addAll(
                rules.stream()
                     .map(r -> r.startsWith("classpath:") ? r : "classpath:" + r)
                     .collect(Collectors.toList()));
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
     * @return this {@code SandboxRules}
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
     * @return this {@code SandboxRules}
     */
    public SandboxRules withSystemProperties(final Collection<String> rules) {
        if (rules != null) {
            this.rules.addAll(
                rules.stream()
                     .map(r -> r.startsWith("system.property:") ? r : "system.property:" + r)
                     .collect(Collectors.toList()));
        }
        return this;
    }


    /**
     * Add whitelisted system environment variable rules to the sandbox.
     *
     * <p>
     * E.g: white listing environment variable
     * <ul>
     *   <li>HOME</li>
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules withSystemEnvs(final String... rules) {
        if (rules != null) {
            withSystemEnvs(Arrays.asList(rules));
        }
        return this;
    }

    /**
     * Add whitelisted system environment variable rules to the sandbox.
     *
     * <p>
     * E.g: white listing environment variable
     * <ul>
     *   <li>"HOME"</li>
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules withSystemEnvs(final Collection<String> rules) {
        if (rules != null) {
            this.rules.addAll(
                rules.stream()
                     .map(r -> r.startsWith("system.env:") ? r : "system.env:" + r)
                     .collect(Collectors.toList()));
        }
        return this;
    }

    /**
     * Reject Venice function rules to the sandbox.
     *
     * <p>
     * E.g:
     * <ul>
     *   <li>"io/slurp" (reject calls to 'io/slurp')</li>
     *   <li>"*io*" (reject all Venice I/O calls like 'io/slurp', 'create-file', ...)</li>
     *   <li>"." (reject java interop completely)</li>
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules rejectVeniceFunctions(final String... rules) {
        if (rules != null) {
            rejectVeniceFunctions(Arrays.asList(rules));
        }
        return this;
    }

    /**
     * Reject Venice function rules to the sandbox.
     *
     * <p>
     * E.g:
     * <ul>
     *   <li>"io/slurp" (reject calls to 'io/slurp')</li>
     *   <li>"*io*" (reject all Venice I/O calls like 'io/slurp', 'create-file', ...)</li>
     *   <li>"." (reject java interop completely)</li>
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules rejectVeniceFunctions(final Collection<String> rules) {
        if (rules != null) {
            final String prefix = "blacklist:venice:func:";
            rules.forEach(r_ -> {
                String r = r_;

                if (r.startsWith(prefix)) {
                    r = r.substring(prefix.length());  // strip prefix
                }

                if (r.startsWith("core/")) {
                    r = r.substring("core/".length());  // strip core
                }

                this.rules.add(prefix + r);
            });
        }
        return this;
    }


    /**
     * Whitelist Venice function rules to the sandbox.
     *
     * <p>
     * E.g:
     * <ul>
     *   <li>"print"</li>
     *   <li>"println"</li>
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules whitelistVeniceFunctions(final String... rules) {
        if (rules != null) {
            whitelistVeniceFunctions(Arrays.asList(rules));
        }
        return this;
    }

    /**
     * Whitelist Venice function rules to the sandbox.
     *
     * <p>
     * E.g:
     * <ul>
     *   <li>"print"</li>
     *   <li>"println"</li>
     * </ul>
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules whitelistVeniceFunctions(final Collection<String> rules) {
        if (rules != null) {
            final String prefix = "whitelist:venice:func:";
            rules.forEach(r_ -> {
                String r = r_;

                if (r.startsWith(prefix)) {
                    r = r.substring(prefix.length());  // strip prefix
                }

                if (r.startsWith("core/")) {
                    r = r.substring("core/".length());  // strip core
                }

                this.rules.add(prefix + r);
            });
        }
        return this;
    }

    /**
     * Add rules for whitelisted Venice modules.
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules withVeniceModules(final String... rules) {
        if (rules != null) {
            withVeniceModules(Arrays.asList(rules));
        }
        return this;
    }

    /**
     * Add rules for whitelisted Venice modules.
     *
     * @param rules rules
     * @return this {@code SandboxRules}
     */
    public SandboxRules withVeniceModules(final Collection<String> rules) {
        if (rules != null) {
            this.rules.addAll(
                rules.stream()
                     .map(r -> r.startsWith("venice:module:") ? r : "venice:module:" + r)
                     .collect(Collectors.toList()));
        }
        return this;
    }

    public SandboxRules withDefaultVeniceModules() {
        withVeniceModules(SandboxDefaultRules.DEFAULT_WHITELISTED_MODULES);
        return this;
    }

    /**
     * Sets the max execution time in seconds a Venice script under this
     * {@code SandboxRules} is allowed to run.
     *
     * @param maxExecTimeSeconds the max exec time in seconds
     * @return this {@code SandboxRules}
     */
    public SandboxRules withMaxExecTimeSeconds(final int maxExecTimeSeconds) {
        this.maxExecTimeSeconds = maxExecTimeSeconds <= 0 ? null : maxExecTimeSeconds;
        return this;
    }

    /**
     * Sets the max thread pool size for futures a Venice script under this
     * {@code SandboxRules} is allowed to use.
     *
     * @param maximumPoolSize the max thread pool size
     * @return this {@code SandboxRules}
     */
    public SandboxRules withMaxFutureThreadPoolSize(final int maximumPoolSize) {
        this.maxFutureThreadPoolSize = maximumPoolSize <= 0 ? null : maximumPoolSize;
        return this;
    }

    /**
     * Reject access to all Venice I/O related functions
     *
     * @return this {@code SandboxRules}
     */
    public SandboxRules rejectAllIoFunctions() {
        rejectVeniceFunctions("*io*");
        return this;
    }

    /**
     * Reject access to all Venice I/O related functions
     *
     * @return this {@code SandboxRules}
     * @deprecated use {@link #rejectAllIoFunctions}
     */
    @Deprecated
    public SandboxRules rejectAllVeniceIoFunctions() {
        return rejectAllIoFunctions();
    }

    /**
     * Reject access to all concurrency related functions
     *
     * <ul>
     *   <li>agents</li>
     *   <li>futures</li>
     *   <li>promises</li>
     *   <li>thread</li>
     *   <li>thread local</li>
     *   <li>watches</li>
     *   <li>parallel processing</li>
     *   <li>scheduler</li>
     * </ul>
     *
     * @return this {@code SandboxRules}
     */
    public SandboxRules rejectAllConcurrencyFunctions() {
        rejectVeniceFunctions("*concurrency*");
        return this;
    }

    /**
     * Reject access to all system related functions
     *
     * @return this {@code SandboxRules}
     */
    public SandboxRules rejectAllSystemFunctions() {
        rejectVeniceFunctions("*system*");
        return this;
    }

    /**
     * Reject access to all Java interop functions
     *
     * @return this {@code SandboxRules}
     */
    public SandboxRules rejectAllJavaInteropFunctions() {
        rejectVeniceFunctions("*java-interop*");
        return this;
    }

    /**
     * Reject access to all sensitive special forms
     *
     * <ul>
     *   <li>dynamic code loading</li>
     *   <li>ns manipulation</li>
     *   <li>var manipulation</li>
     *   <li>benchmark</li>
     *   <li>profiling</li>
     *   <li>...</li>
     * </ul>
     *
     * @return this {@code SandboxRules}
     */
    public SandboxRules rejectAllSensitiveSpecialForms() {
        rejectVeniceFunctions("*special-forms*");
        return this;
    }
    public SandboxRules rejectAllSenstiveSpecialForms() {
        // keep typo in function name for compatibility
        return rejectAllSensitiveSpecialForms();
    }

    /**
     * Reject access to all unsafe functions.
     *
     * @return this {@code SandboxRules}
     */
    public SandboxRules rejectAllUnsafeFunctions() {
        rejectVeniceFunctions("*unsafe*");
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
     * @return this {@code SandboxRules}
     */
    public SandboxRules withStandardSystemProperties() {
        withSystemProperties(SandboxDefaultRules.DEFAULT_SYSTEM_PROPERTIES);
        return this;
    }

    /**
     * Allow access to all Java system properties
     *
     * @return this {@code SandboxRules}
     */
    public SandboxRules withAllSystemProperties() {
        withSystemProperties("*");
        return this;
    }

    /**
     * Allow access to all standard system environment variables
     *
     * <p>Standard system environment variables:
     *
     * @return this {@code SandboxRules}
     */
    public SandboxRules withStandardSystemEnvs() {
        withSystemEnvs(SandboxDefaultRules.DEFAULT_SYSTEM_ENVS);
        return this;
    }

    /**
     * Allow access to all system environment variables
     *
     * @return this {@code SandboxRules}
     */
    public SandboxRules withAllSystemEnvs() {
        withSystemEnvs("*");
        return this;
    }

    /**
     * Merges this {@code SandboxRules} with the passed other
     * {@code SandboxRules}
     *
     * <p>Note: merges only the rules but not 'maxExecTimeSeconds'
     * and 'maxFutureThreadPoolSize' !!
     *
     * <p>Note: you may end up with duplicates rules. For better
     * performance use {@code SandboxRules.unique()} to get a new
     * deduplicated {@code SandboxRules}.
     *
     * @param other the other SandboxRules to merge with
     * @return the new merged {@code SandboxRules}
     */
    public SandboxRules merge(final SandboxRules other) {
        final List<String> merged = new ArrayList<>(rules);
        merged.addAll(other.rules);
        return new SandboxRules(
                    merged,
                    this.maxExecTimeSeconds,
                    this.maxFutureThreadPoolSize);
    }

    /**
     * Remove duplicate rules from these {@code SandboxRules}
     *
     * @return the new unique rules {@code SandboxRules}
     */
    public SandboxRules unique() {
        final List<String> unique = new ArrayList<>();

        // Note: reverse the ordered rules list for deduplicating.
        //       we are interested in the last occurence, because the order
        //       is important. see example below!
        //
        // Access to println denied:
        //   new SandboxInterceptor(
        //         new SandboxRules()
        //                .whitelistVeniceFunctions("println")
        //                .rejectVeniceFunctions("*io*"));        // <- overules
        //
        // Access to println granted:
        //   new SandboxInterceptor(
        //         new SandboxRules()
        //                .whitelistVeniceFunctions("println")
        //                .rejectVeniceFunctions("*io*")          // <- overules
        //                .whitelistVeniceFunctions("println"));  // <- overules again!

        // remove the duplicate rules by keeping the order, keep always the last
        // rules occurrence!
        final List<String> rules = new ArrayList<>(this.rules);
        Collections.reverse(rules);

        final Set<String> check = new HashSet<>();
        for(String rule : rules) {
            if (check.contains(rule)) continue;

            check.add(rule);
            unique.add(rule);
        }

        Collections.reverse(unique);

        return new SandboxRules(
                    unique,
                    this.maxExecTimeSeconds,
                    this.maxFutureThreadPoolSize);
    }

    /**
     * @return the rules of this {@code SandboxRules}
     */
    public List<String> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * @return the max execution time in seconds a Venice script under this
     * {@code SandboxRules} is allowed to run.
     */
    public Integer getMaxExecTimeSeconds() {
        return maxExecTimeSeconds;
    }

    /**
     * @return the max thread pool size for futures a Venice script under this
     * {@code SandboxRules} is allowed to use.
     */
    public Integer getMaxFutureThreadPoolSize() {
        return maxFutureThreadPoolSize;
    }


    /**
     * Returns the default rules used for Venice sandboxes.
     *
     * <p>Note: The default rules can be omitted by calling
     * <pre>
     *    SandboxRules
     *        .noDefaults()
     *        .withClasses(
     *            "java.lang.Math",
     *            "java.math.BigDecimal");
     * </pre>
     *
     * @return the default rules used for the {@code Sandbox}
     */
    public static List<String> getDefaultRules() {
        return new SandboxRules()
                    .getRules()
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(final String prefix) {
        final Set<String> items = new HashSet<>(rules);
        items.removeAll(SandboxDefaultRules.SYSTEM_CLASS_RULES);
        items.add("maxExecTimeSeconds:" + (maxExecTimeSeconds == null ? "no-limit" : maxExecTimeSeconds.toString()));
        return new ArrayList<String>(items)
                    .stream()
                    .sorted()
                    .map(s -> "   " + s)
                    .collect(Collectors.joining("\n"));
    }




    private final List<String> rules = new ArrayList<>();
    private Integer maxExecTimeSeconds = null;
    private Integer maxFutureThreadPoolSize = null;
}
