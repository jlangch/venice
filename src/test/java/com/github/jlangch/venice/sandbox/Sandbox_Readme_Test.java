/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


/**
 * Test the code from 'sandbox.md'
 */
public class Sandbox_Readme_Test {

    @Test
    public void test() {
        final Venice venice = new Venice(createSandbox());

        // rule: "java.lang.Math:PI"
        // => OK (whitelisted static field)
        venice.eval("(. :java.lang.Math :PI)");

        // rule: "java.lang.Math:min"
        // => OK (whitelisted static method)
        venice.eval("(. :java.lang.Math :min 20 30)");

        // rule: "java.time.ZonedDateTime:*
        // => OK (whitelisted constructor & instance method)
        venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5))");

        // rule: "java.util.ArrayList:new" and "java.util.ArrayList:add"
        // => OK (whitelisted constructor & instance method)
        venice.eval(
            "(doto (. :java.util.ArrayList :new)  " +
            "      (. :add 1)                     " +
            "      (. :add 2))                    ");

        // rule: "java.awt.**:*"
        // => OK (whitelisted)
        venice.eval(
            "(-<> (. :java.awt.color.ColorSpace :CS_LINEAR_RGB)      " +
            "     (. :java.awt.color.ICC_ColorSpace :getInstance <>) " +
            "     (. <> :getMaxValue 0))                             ");

        // => FAIL (invoking non whitelisted static method)
        assertThrows(SecurityException.class, () ->
            venice.eval("(. :java.lang.System :exit 0)"));

        // => FAIL (invoking blacklisted Venice I/O function)
        assertThrows(SecurityException.class, () ->
            venice.eval("(io/slurp \"/tmp/file\")"));

        // => OK (invoking whitelisted Venice I/O function 'println')
        venice.eval("(println 100)");

        // => FAIL exceeded max exec time of 3s
        assertThrows(SecurityException.class, () ->
            venice.eval("(sleep 10_000)"));

        // => FAIL (accessing non whitelisted system property)
        assertThrows(SecurityException.class, () ->
            venice.eval("(system-prop \"db.password\")"));

        // => FAIL (accessing non whitelisted system environment variable)
        assertThrows(SecurityException.class, () ->
            venice.eval("(system-env \"USER\")"));

        // => FAIL (accessing non whitelisted classpath resources)
        assertThrows(SecurityException.class, () ->
            venice.eval("(io/load-classpath-resource \"resources/images/img.tiff\")"));
    }


    private SandboxInterceptor createSandbox() {
        return new SandboxInterceptor(
                    new SandboxRules()
                          // Java interop: whitelist rules
                          .withStandardSystemProperties()
                          .withSystemProperties("db.name", "db.port")
                          .withSystemEnvs("SHELL", "HOME")
                          .withClasspathResources("resources/images/*.png")
                          .withClasses(
                            "java.lang.Math:PI",
                            "java.lang.Math:min",
                            "java.time.ZonedDateTime:*",
                            "java.awt.**:*",
                            "java.util.ArrayList:new",
                            "java.util.ArrayList:add")

                          // Venice extension modules: whitelist rules
                          .withVeniceModules(
                            "crypt",
                            "kira",
                            "math")

                          // Venice functions: blacklist rules
                          .rejectAllIoFunctions()
                          .rejectAllConcurrencyFunctions()
                          .rejectAllSystemFunctions()
                          .rejectAllSenstiveSpecialForms()
                          .rejectVeniceFunctions(
                            "time/date",
                            "time/zone-ids")

                          // Venice functions: whitelist rules for print functions to offset
                          // blacklist rules by individual functions
                          .whitelistVeniceFunctions("*print*")

                          // Generic rules
                          .withMaxFutureThreadPoolSize(20)
                          .withMaxExecTimeSeconds(3));
    }
}
