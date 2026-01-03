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
package com.github.jlangch.venice.impl.util.shell;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import com.github.jlangch.venice.impl.util.StringUtil;


public class ShellBackgroundResult {

    public ShellBackgroundResult(
            final ShellResult startResult,
            final File nohupFile
    ) {
        this.startResult = startResult;
        this.nohupFile = nohupFile;
    }


    public ShellResult getStartResult() {
        return startResult;
    }

    public File getNohupFile() {
        return nohupFile;
    }

    public String getNohupFileText() {
        if (nohupFile.isFile()) {
            try {
                return new String(
                            Files.readAllBytes(nohupFile.toPath()),
                            Charset.forName("UTF-8"));
            }
            catch(IOException ex) {
                throw new RuntimeException("Faile to read nohup file", ex);
            }
        }
        else {
            return null;
        }
    }


    @Override
    public String toString() {
        if (nohupFile.isFile()) {
            final StringBuilder sb = new StringBuilder(startResult.toString());

            final String nohup = StringUtil.trimToNull(getNohupFileText());
            if (nohup == null) {
                sb.append("\n[nohup]   empty\n");
            }
            else {
                sb.append("\n[nohup]\n");
                sb.append(nohup);
            }
            return sb.toString();
        }
        else {
            return startResult.toString();
        }
    }


    private final ShellResult startResult;
    private final File nohupFile;
}
