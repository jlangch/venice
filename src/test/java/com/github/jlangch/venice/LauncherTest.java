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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.FileUtil;


public class LauncherTest {

    @Test
    public void test_run_script() {
        final PrintStream orgStdOut = System.out;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(baos));

            final int exitCode = Launcher.run(new String[] {"-script", "(println (+ 1 100))"});

            assertEquals(0, exitCode);

            // Must run on *nix and Windows
            assertEquals("101\nnil\n", StringUtil.crlf_to_lf(baos.toString()));
        }
        finally {
            System.setOut(orgStdOut);
        }
    }

    @Test
    public void test_run_file() throws Exception {
        final File tmp = Files.createTempDirectory("launcher").toFile();

        final PrintStream orgStdOut = System.out;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(baos));

            final File script = new File(tmp, "script.venice");
            FileUtil.save("(println (+ 1 200))", script, true);

            final int exitCode = Launcher.run(new String[] {"-file", script.getPath()});

            assertEquals(0, exitCode);

            // Must run on *nix and Windows
            assertEquals("201\nnil\n", StringUtil.crlf_to_lf(baos.toString()));
        }
        finally {
            System.setOut(orgStdOut);
            deleteSetupDir(tmp);
        }
    }

    @Test
    public void test_run_classpath_file() {
        final PrintStream orgStdOut = System.out;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(baos));

            final int exitCode = Launcher.run(new String[] {"-cp-file", "com/github/jlangch/venice/launcher-classpath-script-test.venice"});

            assertEquals(0, exitCode);

            // Must run on *nix and Windows
            assertEquals("301\nnil\n", StringUtil.crlf_to_lf(baos.toString()));
        }
        finally {
            System.setOut(orgStdOut);
        }
    }

    private static void deleteSetupDir(final File dir) throws IOException {
        Files.walk(dir.toPath())
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);
    }

}
