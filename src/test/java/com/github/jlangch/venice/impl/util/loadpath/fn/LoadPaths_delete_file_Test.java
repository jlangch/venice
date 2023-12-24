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
package com.github.jlangch.venice.impl.util.loadpath.fn;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.loadpath.TempFS;


public class LoadPaths_delete_file_Test {

    @Test
    public void test_no_loadpaths() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice();

            // existing files
            venice.eval("(io/delete-file src)", param(root, "res1.txt"));
            venice.eval("(io/delete-file src)", param(root, "dir1/11/res4.txt"));

            // non existing file -> silently ignored
            venice.eval("(io/delete-file src)", param(root, "unknown/res2.txt"));
        });
    }

    @Test
    public void test_relative_limited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(false));

            // inside
            venice.eval("(io/delete-file src)", param("11/res4.txt"));

            // non existing file -> silently ignored
            venice.eval("(io/delete-file src)", param("unknown/res2.txt"));
      });
    }

    @Test
    public void test_relative_unlimited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(true));

            // inside
            venice.eval("(io/delete-file src)", param("11/res4.txt"));

            // non existing file -> silently ignored
            venice.eval("(io/delete-file src)", param("unknown/res2.txt"));
        });
    }

    @Test
    public void test_absolute_limited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(false));

            // inside
            venice.eval("(io/delete-file src)", param(root, "dir1/11/res4.txt"));

            // outside
            assertThrows(
            		VncException.class,
            		() -> venice.eval("(io/delete-file src)", param(root, "dir2/res5.txt")));
      });
    }

    @Test
    public void test_absolute_unlimited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(true));


            // inside
            venice.eval("(io/delete-file src)", param(root, "dir1/11/res4.txt"));

            // outside
            venice.eval("(io/delete-file src)", param(root, "dir2/res5.txt"));
        });
    }


    private static Map<String,Object> param(final String file) {
        return Parameters.of("src", new File(file));
    }

    private static Map<String,Object> param(final File root, final String file) {
        return Parameters.of("src", new File(root, file));
    }
}

