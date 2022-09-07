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
package com.github.jlangch.venice.examples;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Embed_10_CustomSandbox {

    public static void main(final String[] args) {
        try {
            run();
            System.exit(0);
        }
        catch(VncException ex) {
            ex.printVeniceStackTrace();
            System.exit(1);
        }
        catch(RuntimeException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void run() {
        final Venice venice = new Venice(createSandbox());

        // rule: "java.lang.Math:PI"
        // => OK (whitelisted static field)
        venice.eval("(. :java.lang.Math :PI)");
        System.out.println("OK      : (. :java.lang.Math :PI)");

        // rule: "java.lang.Math:min"
        // => OK (whitelisted static method)
        venice.eval("(. :java.lang.Math :min 20 30)");
        System.out.println("OK      : (. :java.lang.Math :min 20 30)");

        // rule: "java.time.ZonedDateTime:*
        // => OK (whitelisted constructor & instance method)
        venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5))");
        System.out.println("OK      : (. (. :java.time.ZonedDateTime :now) :plusDays 5))");

        // rule: "java.util.ArrayList:new" and "java.util.ArrayList:add"
        // => OK (whitelisted constructor & instance method)
        venice.eval(
            "(doto (. :java.util.ArrayList :new)  " +
            "      (. :add 1)                     " +
            "      (. :add 2))                    ");
        System.out.println("OK      : java.util.ArrayList::new()");

        // rule: "java.awt.**:*"
        // => OK (whitelisted)
        venice.eval(
            "(-<> (. :java.awt.color.ColorSpace :CS_LINEAR_RGB)      " +
            "     (. :java.awt.color.ICC_ColorSpace :getInstance <>) " +
            "     (. <> :getMaxValue 0))                             ");
        System.out.println("OK      : use of java.awt.** classes");

        // => FAIL (invoking non whitelisted static method)
        try {
            venice.eval("(. :java.lang.System :exit 0)");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (. :java.lang.System :exit 0)");
        }

        // => FAIL (invoking blacklisted Venice I/O function)
        try {
            venice.eval("(io/slurp \"/tmp/file\")");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (io/slurp ...)");
        }

        // => OK (invoking whitelisted Venice I/O function 'println')
        venice.eval("(println 100)");
        System.out.println("OK:  (println 100)");

        // => FAIL exceeded max exec time of 3s
        try {
            venice.eval("(sleep 10_000)");
        }
        catch(SecurityException ex) {
            System.out.println("EXCEEDED: max exec time on (sleep ...)");
        }

        // => FAIL (accessing non whitelisted system property)
        try {
            venice.eval("(system-prop \"db.password\")");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (system-prop ...)");
        }

        // => FAIL (accessing non whitelisted system environment variable)
        try {
            venice.eval("(system-env \"USER\")");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (system-env ...)");
        }

        // => FAIL (accessing non whitelisted classpath resources)
        try {
             venice.eval("(io/load-classpath-resource \"resources/images/img.tiff\")");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (io/load-classpath-resource ...)");
        }
    }


    private static SandboxInterceptor createSandbox() {
        return new SandboxInterceptor(
                    new SandboxRules()
                        // Venice functions: blacklist all unsafe functions
                        .rejectAllUnsafeFunctions()

                        // Venice functions:  blacklist additional functions
                        .rejectVeniceFunctions(
                            "time/date",
                            "time/zone-ids")

                        // Venice functions: whitelist rules for print functions to offset
                        // blacklist rules by individual functions
                        .whitelistVeniceFunctions("*print*")

                        // Venice functions: whitelist Java calls offsets the black list
                        // rule for java interop from SandboxRules::rejectAllUnsafeFunctions()
                        .whitelistVeniceFunctions(".")

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

                        // Generic rules
                        .withMaxFutureThreadPoolSize(20)
                        .withMaxExecTimeSeconds(3));
    }
}
