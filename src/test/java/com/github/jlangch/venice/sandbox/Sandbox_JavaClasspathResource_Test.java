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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Sandbox_JavaClasspathResource_Test {

    @Test
    public void test_load_classpath_resource() {
        final String resource = ClassPathResource.toPath(Venice.class.getPackage(), "test-support.venice");

        final String script =
                "(do                                                                 \n" +
                "   (-<> \""+ resource + "\"                                          \n" +
                "        (io/load-classpath-resource <>)                              \n" +
                "        (bytebuf-to-string <> :UTF-8)                                \n" +
                "        (str/contains? <> \"(defn test-support/test-fn \"))))        ";

        // [1] OK
        assertTrue((Boolean)new Venice().eval(script));
        assertTrue((Boolean)new Venice(new AcceptAllInterceptor()).eval(script));

        // [2] OK
        ModuleLoader.clear();
        Interceptor interceptor = new SandboxInterceptor(
                                        new SandboxRules()
                                                .withClasspathResources("com/github/jlangch/venice/test-support.venice"));
        assertTrue((Boolean)new Venice(interceptor).eval(script));

        // [3] OK
        ModuleLoader.clear();
        interceptor = new SandboxInterceptor(
                                new SandboxRules()
                                        .withClasspathResources("com/github/jlangch/venice/*.venice"));
        assertTrue((Boolean)new Venice(interceptor).eval(script));

        // [4] OK
        ModuleLoader.clear();
        interceptor = new SandboxInterceptor(
                                new SandboxRules()
                                        .withClasspathResources("**/github/jlangch/**/*.venice"));
        assertTrue((Boolean)new Venice(interceptor).eval(script));



        // [5] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            final Venice venice = new Venice(new RejectAllInterceptor());
            venice.eval(script);
        });

        // [6] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            Interceptor i2 = new SandboxInterceptor(
                                new SandboxRules()
                                        .rejectVeniceFunctions("io/load-classpath-resource"));
            new Venice(i2).eval(script);
        });

        // [7] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            Interceptor i2 = new SandboxInterceptor(
                                new SandboxRules()
                                        .withClasspathResources("com/github/jlangch/venice/x*.venice"));
            new Venice(i2).eval(script);
        });

        // [8] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            Interceptor i2 = new SandboxInterceptor(
                                new SandboxRules()
                                        .rejectVeniceFunctions("io/load-classpath-resource") // this rules wins!
                                        .withClasspathResources("com/github/jlangch/venice/test.venice"));
            new Venice(i2).eval(script);
        });

        // [9] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            Interceptor i2 = new SandboxInterceptor(
                                new SandboxRules()
                                        .withClasspathResources("org/**/*.venice"));
            new Venice(i2).eval(script);
        });
    }


    @Test
    public void test_load_classpath_file() {
        final String resource = ClassPathResource.toPath(Venice.class.getPackage(), "test-support.venice");

        final String script =
                "(do                            \n" +
                "  (->> \""+ resource + "\"     \n" +
                "       (load-classpath-file))  \n" +
                "  (test-support/test-fn 1)))   ";

        // [1] OK
        assertEquals("test: 1", new Venice().eval(script));
        assertEquals("test: 1", new Venice(new AcceptAllInterceptor()).eval(script));

        // [2] OK
        ModuleLoader.clear();
        Interceptor interceptor = new SandboxInterceptor(
                                        new SandboxRules()
                                                .withClasspathResources("com/github/jlangch/venice/test-support.venice"));
        assertEquals("test: 1", new Venice(interceptor).eval(script));

        // [3] OK
        ModuleLoader.clear();
        interceptor = new SandboxInterceptor(
                                new SandboxRules()
                                        .withClasspathResources("com/github/jlangch/venice/*.venice"));
        assertEquals("test: 1", new Venice(interceptor).eval(script));

        // [4] OK
        ModuleLoader.clear();
        interceptor = new SandboxInterceptor(
                                new SandboxRules()
                                        .withClasspathResources("**/github/jlangch/**/*.venice"));
        assertEquals("test: 1", new Venice(interceptor).eval(script));



        // [5] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            final Venice venice = new Venice(new RejectAllInterceptor());
            venice.eval(script);
        });

        // [6] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            Interceptor i2 = new SandboxInterceptor(
                                new SandboxRules()
                                        .rejectVeniceFunctions("load-classpath-file"));
            new Venice(i2).eval(script);
        });

        // [7] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            Interceptor i2 = new SandboxInterceptor(
                                new SandboxRules()
                                        .withClasspathResources("com/github/jlangch/venice/x*.venice"));
            new Venice(i2).eval(script);
        });

        // [8] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            Interceptor i2 = new SandboxInterceptor(
                                new SandboxRules()
                                        .rejectVeniceFunctions("load-classpath-file") // this rules wins!
                                        .withClasspathResources("com/github/jlangch/venice/*.venice"));
            new Venice(i2).eval(script);
        });

        // [9] FAIL
        ModuleLoader.clear();
        assertThrows(SecurityException.class, () -> {
            Interceptor i2 = new SandboxInterceptor(
                                new SandboxRules()
                                        .withClasspathResources("org/**/*.venice"));
            new Venice(i2).eval(script);
        });
    }

}
