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

import java.util.List;

import com.github.jlangch.venice.impl.util.StringUtil;


public class ShellResult {

    public ShellResult(
            final String stdout,
            final String stderr,
            final int exitCode
    ) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }


    public String getStdout() {
        return stdout;
    }

    public List<String> getStdoutLines() {
        return StringUtil.splitIntoLines(stdout);
    }

    public String getStderr() {
        return stderr;
    }

    public List<String> getStderrLines() {
        return StringUtil.splitIntoLines(stderr);
    }

    public int getExitCode() {
        return exitCode;
    }

    public boolean isZeroExitCode() {
        return exitCode == 0;
    }


    @Override
    public String toString() {
        final String err = StringUtil.trimToNull(stderr);
        final String out = StringUtil.trimToNull(stdout);

        final StringBuilder sb = new StringBuilder();

        sb.append("Exit code: " + exitCode);

        if (out == null) {
            sb.append("\n[stdout]  empty\n");
        }
        else {
            sb.append("\n[stdout]\n");
            sb.append(out);
        }

        if (err == null) {
            sb.append("\n[stderr]  empty\n");
        }
        else {
            sb.append("\n[stderr]\n");
            sb.append(err);
        }

        return sb.toString();
    }


    private final String stdout;
    private final String stderr;
    private final int exitCode;
}
