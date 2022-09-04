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
package com.github.jlangch.venice.impl.util.loadpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class LoadPaths_normalize_Test {

	// ------------------------------------------------------------------------
	// Without Sandbox
	// ------------------------------------------------------------------------

    @Test
    public void test_normalize_absolute() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice();

            assertEquals(new File(root, "res1.txt"), venice.eval("(loadpath/normalize f)", param(root, "res1.txt")));

            assertEquals(new File(root, "dir1/res2.txt"), venice.eval("(loadpath/normalize f)", param(root, "dir1/res2.txt")));

            assertEquals(new File(root, "dir1/11/res4.txt"), venice.eval("(loadpath/normalize f)", param(root, "dir1/11/res4.txt")));

            assertEquals(new File(root, "dir2/res5.txt"), venice.eval("(loadpath/normalize f)", param(root, "dir2/res5.txt")));
        });
    }

    @Test
    public void test_normalize_relative() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice();

            assertEquals(new File("res1.txt"), venice.eval("(loadpath/normalize f)", param("res1.txt")));

            assertEquals(new File("res2.txt"), venice.eval("(loadpath/normalize f)", param("res2.txt")));

            assertEquals(new File("11/res4.txt"), venice.eval("(loadpath/normalize f)", param("11/res4.txt")));

            assertEquals(new File("dir2/res5.txt"), venice.eval("(loadpath/normalize f)", param("dir2/res5.txt")));
        });
    }



	// ------------------------------------------------------------------------
	// With Sandbox
	// ------------------------------------------------------------------------

    @Test
    public void test_normalize_absolute_unlimited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(true));

            // on loadpath
            assertEquals(canonical(root, "res1.txt"), venice.eval("(loadpath/normalize f)", param(root, "res1.txt")));

            // on loadpath
            assertEquals(canonical(root, "dir1/res2.txt"), venice.eval("(loadpath/normalize f)", param(root, "dir1/res2.txt")));

            // on loadpath
            assertEquals(canonical(root, "dir1/11/res4.txt"), venice.eval("(loadpath/normalize f)", param(root, "dir1/11/res4.txt")));

            // not on loadpath
            assertEquals(new File(root, "dir2/res5.txt"), venice.eval("(loadpath/normalize f)", param(root, "dir2/res5.txt")));
        });
    }

    @Test
    public void test_normalize_absolute_limited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(false));

            // on loadpath
            assertEquals(canonical(root, "res1.txt"), venice.eval("(loadpath/normalize f)", param(root, "res1.txt")));

            // on loadpath
            assertEquals(canonical(root, "dir1/res2.txt"), venice.eval("(loadpath/normalize f)", param(root, "dir1/res2.txt")));

            // on loadpath
            assertEquals(canonical(root, "dir1/11/res4.txt"), venice.eval("(loadpath/normalize f)", param(root, "dir1/11/res4.txt")));

            // not on loadpath
            assertThrows(
                    VncException.class,
                    () -> venice.eval("(loadpath/normalize f)", param(root, "dir2/res5.txt")));
        });
    }

    @Test
    public void test_normalize_relative_unlimited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(true));

            // on loadpath
            assertEquals(canonical(root, "res1.txt"), venice.eval("(loadpath/normalize f)", param("res1.txt")));

            // on loadpath
            assertEquals(canonical(root, "dir1/res2.txt"), venice.eval("(loadpath/normalize f)", param("res2.txt")));

            // on loadpath
            assertEquals(canonical(root, "dir1/11/res4.txt"), venice.eval("(loadpath/normalize f)", param("11/res4.txt")));

            // Venice is unable to see the intention -> so "dir2/res5.txt" is on loadpath as "dir/dir2/res5.txt"
            assertEquals(canonical(root, "dir1/dir2/res5.txt"), venice.eval("(loadpath/normalize f)", param("dir2/res5.txt")));
        });
    }

    @Test
    public void test_normalize_relative_limited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(false));

            // on loadpath
            assertEquals(canonical(root, "res1.txt"), venice.eval("(loadpath/normalize f)", param("res1.txt")));

            // on loadpath
            assertEquals(canonical(root, "dir1/res2.txt"), venice.eval("(loadpath/normalize f)", param("res2.txt")));

            // on loadpath
            assertEquals(canonical(root, "dir1/11/res4.txt"), venice.eval("(loadpath/normalize f)", param("11/res4.txt")));

            // Venice is unable to see the intention -> so "dir2/res5.txt" is on loadpath as "dir/dir2/res5.txt"
            assertEquals(canonical(root, "dir1/dir2/res5.txt"), venice.eval("(loadpath/normalize f)", param("dir2/res5.txt")));
        });
    }


    private static File canonical(final File file, final String name) {
    	try {
            return new File(file, name).getCanonicalFile();
    	}
    	catch(IOException ex) {
    		throw new RuntimeException(ex);
    	}
    }

    private static Map<String,Object> param(final String file) {
        return Parameters.of("f", new File(file));
    }

    private static Map<String,Object> param(final File root, final String file) {
        return Parameters.of("f", new File(root, file));
    }
}

