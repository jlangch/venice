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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.junit.EnableOnMacOrLinux;
import com.github.jlangch.venice.impl.util.junit.EnableOnWindows;


public class ReplSetupModuleTest {

    @Test
    @EnableOnMacOrLinux
    public void test_repl_setup_macos_linux() throws IOException {
        final Venice venice = new Venice();

        final File tmp = Files.createTempDirectory("setup").toFile();

        try {
            final String script =
                    "(do                                             \n" +
                    "   (load-module :repl-setup)                    \n" +
                     "   (repl-setup/setup :install-dir setup-dir))    ";

            final String result = (String)venice.eval(
                                            script,
                                            Parameters.of("setup-dir", tmp));
            if (result.equals("internet-not-available")) {
                assertTrue(true);
            }
            else if (result.equals("success")) {
                assertTrue(new File(tmp, "libs").isDirectory());
                assertTrue(new File(tmp, "scripts").isDirectory());
                assertTrue(new File(tmp, "tmp").isDirectory());
                assertTrue(new File(tmp, "tools").isDirectory());

                assertTrue(new File(tmp, "repl.env").isFile());
                assertTrue(new File(tmp, "repl.sh").isFile());
                assertTrue(new File(tmp, "repl.sh").canExecute());
                assertTrue(new File(tmp, "run-script.sh").isFile());
                assertTrue(new File(tmp, "run-script.sh").canExecute());

                assertTrue(new File(tmp, "libs/repl.json").isFile());
                assertTrue(new File(tmp, "libs/jansi-2.4.1.jar").isFile());

                assertTrue(new File(tmp, "scripts/pdf").isDirectory());
                assertTrue(new File(tmp, "scripts/pdf/pdf-example.venice").isFile());
                assertTrue(new File(tmp, "scripts/webapp").isDirectory());
                assertTrue(new File(tmp, "scripts/webapp/demo-webapp.venice").isFile());
                assertTrue(new File(tmp, "scripts/sudoku.venice").isFile());
                assertTrue(new File(tmp, "scripts/shebang-demo.venice").isFile());

                assertTrue(new File(tmp, "tools/apache-maven-3.9.6").isDirectory());
                assertTrue(new File(tmp, "tools/apache-maven-3.9.6/bin/mvn").isFile());
            }
            else {
                fail("got " + result);
            }
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            deleteSetupDir(tmp);
        }
    }

    @Test
    @EnableOnMacOrLinux
    public void test_repl_setup_macos_linux_unattended() throws IOException {
        // Unattended Venice REPL setup is supported with Venice 1.12.28+

        final String version = "1.12.28";

        final Venice venice = new Venice();

        final File tmp = Files.createTempDirectory("setup").toFile();

        try {
            final String script =
                    "(do                                                                          \n" +
                    "   (load-module :repl-setup)                                                 \n" +
                    "                                                                             \n" +
                    "   (def jar-file (str \"venice-\" v-version \".jar\"))                       \n" +
                    "                                                                             \n" +
                    "   (if (io/internet-avail?)                                                  \n" +
                    "     (do                                                                     \n" +
                    "       (println \"Downloading Venice jar\")                                  \n" +
                    "       (repl-setup/download-venice-jar v-version setup-dir)                  \n" +
                    "                                                                             \n" +
                    "       (sh \"/bin/sh\" \"-c\"                                                \n" +
                    "           \"java -jar ~{jar-file} -setup -unattended -colors\"              \n" +
                    "           :dir setup-dir :throw-ex true :out-fn println :err-fn println)    \n" +
                    "       :installed)                                                           \n" +
                    "     :no-internet))                                                          \n";

            final String result = (String)venice.eval(
                                            script,
                                            Parameters.of("setup-dir", tmp,
                                                          "v-version", version));

            if (result.equals("no-internet")) {
                assertTrue(true);
            }
            else if (result.equals("installed")) {
                assertTrue(new File(tmp, "venice-" + version + ".jar").isFile());

                assertTrue(new File(tmp, "libs").isDirectory());
                assertTrue(new File(tmp, "scripts").isDirectory());
                assertTrue(new File(tmp, "tmp").isDirectory());
                assertTrue(new File(tmp, "tools").isDirectory());

                assertTrue(new File(tmp, "repl.env").isFile());
                assertTrue(new File(tmp, "repl.sh").isFile());
                assertTrue(new File(tmp, "repl.sh").canExecute());
                assertTrue(new File(tmp, "run-script.sh").isFile());
                assertTrue(new File(tmp, "run-script.sh").canExecute());

                assertTrue(new File(tmp, "libs/repl.json").isFile());
                assertTrue(new File(tmp, "libs/jansi-2.4.1.jar").isFile());
                assertTrue(new File(tmp, "libs/venice-" + version + ".jar").isFile());

                assertTrue(new File(tmp, "scripts/pdf").isDirectory());
                assertTrue(new File(tmp, "scripts/pdf/pdf-example.venice").isFile());
                assertTrue(new File(tmp, "scripts/webapp").isDirectory());
                assertTrue(new File(tmp, "scripts/webapp/demo-webapp.venice").isFile());
                assertTrue(new File(tmp, "scripts/sudoku.venice").isFile());
                assertTrue(new File(tmp, "scripts/shebang-demo.venice").isFile());

                assertTrue(new File(tmp, "tools/apache-maven-3.9.6").isDirectory());
                assertTrue(new File(tmp, "tools/apache-maven-3.9.6/bin/mvn").isFile());
            }
            else {
                fail("got " + result);
            }
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            deleteSetupDir(tmp);
        }
    }

    @Test
    @EnableOnWindows
    public void test_repl_setup_windows() throws IOException {
        final Venice venice = new Venice();

        final File tmp = Files.createTempDirectory("setup").toFile();

        try {
            final String script =
                    "(do                                            \n" +
                    "   (load-module :repl-setup)                   \n" +
                    "   (repl-setup/setup :install-dir setup-dir))  ";

            final String result = (String)venice.eval(
                                            script,
                                            Parameters.of("setup-dir", tmp));
            if (result.equals("internet-not-available")) {
                assertTrue(true);
            }
            else if (result.equals("success")) {
                assertTrue(new File(tmp, "libs").isDirectory());
                assertTrue(new File(tmp, "scripts").isDirectory());
                assertTrue(new File(tmp, "tmp").isDirectory());
                assertTrue(new File(tmp, "tools").isDirectory());

                assertTrue(new File(tmp, "repl.env.bat").isFile());
                assertTrue(new File(tmp, "repl.bat").isFile());

                assertTrue(new File(tmp, "libs/repl.json").isFile());
                assertTrue(new File(tmp, "libs/jansi-2.4.1.jar").isFile());

                assertTrue(new File(tmp, "scripts/pdf").isDirectory());
                assertTrue(new File(tmp, "scripts/pdf/pdf-example.venice").isFile());
                assertTrue(new File(tmp, "scripts/webapp").isDirectory());
                assertTrue(new File(tmp, "scripts/webapp/demo-webapp.venice").isFile());
                assertTrue(new File(tmp, "scripts/sudoku.venice").isFile());
                assertFalse(new File(tmp, "scripts/shebang-demo.venice").exists());

                assertTrue(new File(tmp, "tools/apache-maven-3.9.6").isDirectory());
                assertTrue(new File(tmp, "tools/apache-maven-3.9.6/bin/mvn").isFile());
            }
            else {
                fail("got " + result);
            }
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            deleteSetupDir(tmp);
        }
    }

    @Test
    @EnableOnWindows
    public void test_repl_setup_windows_unattended() throws IOException {
        // Unattended Venice REPL setup is supported with Venice 1.12.28+

        final String version = "1.12.28";

        final Venice venice = new Venice();

        final File tmp = Files.createTempDirectory("setup").toFile();

        try {
            final String script =
                    "(do                                                                              \n" +
                    "   (load-module :repl-setup)                                                     \n" +
                    "                                                                                 \n" +
                    "   (def jar-file (str \"venice-\" v-version \".jar\"))                           \n" +
                    "                                                                                 \n" +
                    "   (if (io/internet-avail?)                                                      \n" +
                    "     (do                                                                         \n" +
                    "       (println \"Downloading Venice jar\")                                      \n" +
                    "       (repl-setup/download-venice-jar v-version setup-dir)                      \n" +
                    "                                                                                 \n" +
                    "       (sh \"cmd\" \"/c java.exe -jar ~{jar-file} -setup -unattended -colors\"   \n" +
                    "           :dir setup-dir :throw-ex true :out-fn println :err-fn println)        \n" +
                    "       :installed)                                                               \n" +
                    "     :no-internet))                                                              \n";

            final String result = (String)venice.eval(
                                            script,
                                            Parameters.of("setup-dir", tmp,
                                                          "v-version", version));

            if (result.equals("no-internet")) {
                assertTrue(true);
            }
            else if (result.equals("installed")) {
                assertTrue(new File(tmp, "venice-" + version + ".jar").isFile());

                assertTrue(new File(tmp, "libs").isDirectory());
                assertTrue(new File(tmp, "scripts").isDirectory());
                assertTrue(new File(tmp, "tmp").isDirectory());
                assertTrue(new File(tmp, "tools").isDirectory());

                assertTrue(new File(tmp, "repl.env.bat").isFile());
                assertTrue(new File(tmp, "repl.bat").isFile());

                assertTrue(new File(tmp, "libs/repl.json").isFile());
                assertTrue(new File(tmp, "libs/jansi-2.4.1.jar").isFile());
                assertTrue(new File(tmp, "libs/venice-" + version + ".jar").isFile());

                assertTrue(new File(tmp, "scripts/pdf").isDirectory());
                assertTrue(new File(tmp, "scripts/pdf/pdf-example.venice").isFile());
                assertTrue(new File(tmp, "scripts/webapp").isDirectory());
                assertTrue(new File(tmp, "scripts/webapp/demo-webapp.venice").isFile());
                assertTrue(new File(tmp, "scripts/sudoku.venice").isFile());
                assertFalse(new File(tmp, "scripts/shebang-demo.venice").exists());

                assertTrue(new File(tmp, "tools/apache-maven-3.9.6").isDirectory());
                assertTrue(new File(tmp, "tools/apache-maven-3.9.6/bin/mvn").isFile());
            }
            else {
                fail("got " + result);
            }
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            deleteSetupDir(tmp);
        }
    }


    private static void deleteSetupDir(final File dir) throws IOException {
        Files.walk(dir.toPath())
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);

       System.out.println();
       System.out.println("Deleted setup dir: " + dir);
    }
}

