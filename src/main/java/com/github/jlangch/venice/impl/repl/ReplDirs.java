/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.repl;

import java.io.File;

import com.github.jlangch.venice.impl.util.StringUtil;


public class ReplDirs {

    private ReplDirs() {
        this.homeDir = null;
        this.libsDir = null;
        this.fontsDir = null;
        this.scriptsDir = null;
    }

    private ReplDirs(
            final File homeDir,
            final File libsDir,
            final File fontsDir,
            final File scriptsDir
    ) {
        this.homeDir = homeDir;
        this.libsDir = libsDir;
        this.fontsDir = fontsDir;
        this.scriptsDir = scriptsDir;
    }

    public static ReplDirs create() {
        final File homeDir = getReplHomeDir();

        if (homeDir != null) {
            final File libsDir = new File(homeDir, "libs");
            final File fontsDir = new File(homeDir, "fonts");
            final File scriptsDir = new File(homeDir, "scripts");

            return new ReplDirs(
                        homeDir.isDirectory() ? homeDir : null,
                        libsDir.isDirectory() ? libsDir : null,
                        fontsDir.isDirectory() ? fontsDir : null,
                        scriptsDir.isDirectory() ? scriptsDir : null);
        }
        else {
            return new ReplDirs();
        }
    }

    public static ReplDirs notavail() {
        return new ReplDirs();
    }

    public File getHomeDir() {
        return homeDir;
    }

    public File getLibsDir() {
        return libsDir;
    }

    public File getFontsDir() {
        return fontsDir;
    }

    public File getScriptsDir() {
        return scriptsDir;
    }

    public boolean valid() {
        return homeDir != null;
    }


    private static File getReplHomeDir() {
        final String home = StringUtil.trimToNull(System.getProperty("venice.repl.home"));
        return home == null ? null : new File(home);
    }


    private final File homeDir;
    private final File libsDir;
    private final File fontsDir;
    private final File scriptsDir;
}
