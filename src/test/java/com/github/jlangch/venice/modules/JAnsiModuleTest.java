/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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


public class JAnsiModuleTest {

    @Test
    public void test_install_jansi() throws IOException {
        final Venice venice = new Venice();

        final File tmp = Files.createTempDirectory("jansi").toFile();

        try {
            final String script =
                    "(do                                                          \n" +
                    "   (load-module :jansi-install)                              \n" +
                    "                                                             \n" +
                    "   (if (io/internet-avail?)                                  \n" +
                    "     (do                                                     \n" +
                    "       (jansi-install/install :dir jansi-dir :silent false)  \n" +
                    "       :installed)                                           \n" +
                    "     :no-internet))                                          \n";

            final String result = (String)venice.eval(
                                            script,
                                            Parameters.of("jansi-dir", tmp));
            if (result.equals("no-internet")) {
                assertTrue(true);
            }
            else if (result.equals("installed")) {
                final File jar = new File(tmp, "jansi-2.4.1.jar");
                assertTrue(jar.isFile());
            }
            else {
                fail("got " + result);
            }
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            Files.walk(tmp.toPath())
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);

            System.out.println("Deleted install dir: " + tmp);
        }
    }

}

