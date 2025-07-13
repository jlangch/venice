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
package com.github.jlangch.venice.impl.util.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.OS;


public class SimpleShell {

    public static ShellResult execCmd(final String... command)  {
        final String cmdFormatted = formatCmd(command);

        try {
            final Process proc = Runtime.getRuntime().exec(command);

            return getShellResult(proc);
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to run command: " + cmdFormatted, ex);
        }
    }

    public static ShellBackgroundResult execCmdBackground(final String... command) {
        validateLinuxOrMacOSX("Shell::execCmdBackground");

        final String cmdFormatted = formatCmd(command);

        try {
            final File nohup = File.createTempFile("nohup-", ".out");
            nohup.deleteOnExit();

            final String cmd = cmdFormatted + " 2>&1 >" + nohup.getAbsolutePath() + " &";

            final Process proc = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmd});


            return new ShellBackgroundResult(getShellResult(proc), nohup);
        }
        catch(Exception ex) {
            throw new RuntimeException(
                    "Failed to run nohup command: /bin/sh -c "
                    + cmdFormatted
                    + " 2>&1 >nohup.out &",
                    ex);
        }
    }

    public static ShellBackgroundResult execCmdBackgroundNohup(final String... command) throws IOException {
        validateLinuxOrMacOSX("Shell::execCmdNohup");

        final String cmdFormatted = formatCmd(command);

        try {
            final File nohup = File.createTempFile("nohup-", ".out");
            nohup.deleteOnExit();

            final String cmd = "nohup " + cmdFormatted + " 2>&1 >" + nohup.getAbsolutePath() + " &";

            final Process proc = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmd});

            return new ShellBackgroundResult(getShellResult(proc), nohup);
        }
        catch(Exception ex) {
            throw new RuntimeException(
                    "Failed to run nohup command: /bin/sh -c nohup "
                    + cmdFormatted
                    + " 2>&1 >nohup.out &",
                    ex);
        }
    }

    public static List<String> pgrep(final String process) {
        validateLinuxOrMacOSX("Shell::pgrep");

        final ShellResult r = SimpleShell.execCmd("pgrep", "-x", process);
        return r.isZeroExitCode()
                ? r.getStdoutLines()
                   .stream()
                   .filter(s -> !StringUtil.isBlank(s))
                   .collect(Collectors.toList())
                : new ArrayList<>();
    }

    public static String pargs(final String pid) {
        validateLinuxOrMacOSX("Shell::pargs");

        final ShellResult r = SimpleShell.execCmd("ps", "-p", pid, "-ww", "-o", "args");
        if (r.isZeroExitCode()) {
            final List<String> lines = r.getStdoutLines();
            return lines.size() == 2
                    ? lines.get(1)
                    : null;
        }
        else {
            return null;
        }
    }

    public static void kill(final String pid) {
        validateLinuxOrMacOSX("Shell::kill");

        final ShellResult r = SimpleShell.execCmd("kill", pid);
        if (!r.isZeroExitCode()) {
            throw new RuntimeException(
                    "Failed to kill process (" + pid + ").\n"
                    + "\nExit code: " + r.getExitCode()
                    + "\nError msg: " + r.getStderr());
        }
    }

    public static void kill(final Signal signal, final String pid) {
        validateLinuxOrMacOSX("Shell::kill");

        if (!StringUtil.isBlank(pid)) {
            final ShellResult r = SimpleShell.execCmd("kill", "-" + signal.signal(), pid);
            if (!r.isZeroExitCode()) {
                throw new RuntimeException(
                        "Failed to kill process (" + pid + ").\n"
                        + "\nExit code: " + r.getExitCode()
                        + "\nError msg: " + r.getStderr());
            }
        }
    }

    public static void validateLinuxOrMacOSX(final String fnName) {
         if (!(OS.isLinux() || OS.isMacOSX())) {
             throw new RuntimeException(fnName + " is available for Linux and MacOS only!");
         }
    }

    private static String formatCmd(final String... command) {
        return String.join(" ", Arrays.asList(command));
    }

    private static String slurp(final InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(
                                        new InputStreamReader(
                                                is, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    private static ShellResult getShellResult(final Process proc)
    throws IOException, InterruptedException {
        final int exitCode = proc.waitFor();

        final String stdout = slurp(proc.getInputStream());
        final String stderr = slurp(proc.getErrorStream());

        return new ShellResult(stdout, stderr, exitCode);
    }
}
