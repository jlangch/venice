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
package com.github.jlangch.venice.impl.util.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * A regular expression filename filter for regular files.
 */
public class RegexFileFilter implements FilenameFilter {

    public RegexFileFilter(final String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("A regex must not be null");
        }

        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean accept(final File dir, final String name) {
        if (dir == null) {
            throw new IllegalArgumentException("A 'dir' must not be null");
        }
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("A 'name' must not be empty");
        }

        File f = new File(dir, name);

        if (!f.isFile()) return false;  // reject non regular files

        return this.pattern.matcher(name).matches();
    }


    private final Pattern pattern;
}

