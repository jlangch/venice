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
package com.github.jlangch.venice.sandbox;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.sandbox.CompiledSandboxRules;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class SandboxRulesTest {

    @Test
    public void classesTest() {
        final CompiledSandboxRules wl = CompiledSandboxRules.compile(
                                            SandboxRules
                                                .noDefaults()
                                                .withClasses(
                                                    "java.lang.Math",
                                                    "java.math.BigDecimal"
                                                ));

        assertTrue(wl.isWhiteListed(java.lang.Math.class));
        assertTrue(wl.isWhiteListed(java.math.BigDecimal.class));
        assertFalse(wl.isWhiteListed(java.math.BigInteger.class));
    }

    @Test
    public void classesWildcardTest() {
        final CompiledSandboxRules wl = CompiledSandboxRules.compile(
                                            SandboxRules
                                                .noDefaults()
                                                .withClasses(
                                                    "java.lang.Math",
                                                    "java.lang.String:*",
                                                    "java.math.*",
                                                    "java.awt.**"
                                                ));

        assertTrue(wl.isWhiteListed(java.lang.Math.class));
        assertTrue(wl.isWhiteListed(java.lang.String.class));

        assertTrue(wl.isWhiteListed(java.math.BigDecimal.class));
        assertTrue(wl.isWhiteListed(java.math.BigInteger.class));
        assertTrue(wl.isWhiteListed(java.math.MathContext.class));

        assertTrue(wl.isWhiteListed(java.awt.Button.class));
        assertTrue(wl.isWhiteListed(java.awt.color.ColorSpace.class));
        assertTrue(wl.isWhiteListed(java.awt.image.renderable.RenderContext.class));
    }

    @Test
    public void methodsTest() {
        final CompiledSandboxRules wl = CompiledSandboxRules.compile(
                                            new SandboxRules().withClasses(
                                                "java.lang.Math:min",
                                                "java.lang.Math:max"
                                            ));

        assertTrue(wl.isWhiteListed(java.lang.Math.class));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "min"));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "max"));
        assertFalse(wl.isWhiteListed(java.lang.Math.class, "abs"));
    }

    @Test
    public void methodsWildcardTest_1() {
        final CompiledSandboxRules wl = CompiledSandboxRules.compile(
                                            new SandboxRules().withClasses(
                                                "java.lang.Math:*"
                                            ));

        assertTrue(wl.isWhiteListed(java.lang.Math.class));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "min"));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "max"));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "abs"));
    }

    @Test
    public void methodsWildcardTest_2() {
        final CompiledSandboxRules wl = CompiledSandboxRules.compile(
                                            new SandboxRules().withClasses(
                                                "java.lang.*:*"
                                            ));

        assertTrue(wl.isWhiteListed(java.lang.Math.class));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "min"));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "max"));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "abs"));

        assertFalse(wl.isWhiteListed(java.util.BitSet.class, "isEmpty"));
    }

    @Test
    public void methodsWildcardTest_3() {
        final CompiledSandboxRules wl = CompiledSandboxRules.compile(
                                            new SandboxRules().withClasses(
                                                "java.**:*"
                                            ));

        assertTrue(wl.isWhiteListed(java.lang.Math.class));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "min"));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "max"));
        assertTrue(wl.isWhiteListed(java.lang.Math.class, "abs"));

        assertTrue(wl.isWhiteListed(java.util.BitSet.class, "isEmpty"));
    }

    @Test
    public void classpathWildcardTest() {
        final CompiledSandboxRules wl = CompiledSandboxRules.compile(
                                            new SandboxRules().withClasspathResources(
                                                "/foo/org/image.png",
                                                "/foo/org/*.jpg",
                                                "/abc/**/*.png",
                                                "/xyz/**"
                                            ));

        assertFalse(wl.isWhiteListedClasspathResource("/foo/org/image.tiff"));
        assertTrue(wl.isWhiteListedClasspathResource("/foo/org/image.png"));
        assertTrue(wl.isWhiteListedClasspathResource("/foo/org/image.jpg"));

        assertFalse(wl.isWhiteListedClasspathResource("/abc/image.png"));
        assertTrue(wl.isWhiteListedClasspathResource("/abc/x/image.png"));
        assertTrue(wl.isWhiteListedClasspathResource("/abc/x/y/image.png"));
        assertFalse(wl.isWhiteListedClasspathResource("/abc/x/y/image.jpg"));

        assertTrue(wl.isWhiteListedClasspathResource("/xyz/image.png"));
        assertTrue(wl.isWhiteListedClasspathResource("/xyz/x/image.png"));
        assertTrue(wl.isWhiteListedClasspathResource("/xyz/x/y/image.png"));
    }

    @Test
    public void systemPropertyTest() {
        final CompiledSandboxRules wl = CompiledSandboxRules.compile(
                                            new SandboxRules().withSystemProperties("foo.org"));

        assertTrue(wl.isWhiteListedSystemProperty("foo.org"));
        assertFalse(wl.isWhiteListedSystemProperty("foo.com"));
    }

    @Test
    public void systemPropertyAllTest() {
        final CompiledSandboxRules wl = CompiledSandboxRules.compile(
                                            new SandboxRules().withSystemProperties("*"));

        assertTrue(wl.isWhiteListedSystemProperty("foo.org"));
        assertTrue(wl.isWhiteListedSystemProperty("foo.com"));
    }

}
