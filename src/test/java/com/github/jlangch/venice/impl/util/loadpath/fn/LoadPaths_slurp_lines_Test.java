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
package com.github.jlangch.venice.impl.util.loadpath.fn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.io.InternetUtil;
import com.github.jlangch.venice.impl.util.loadpath.TempFS;


public class LoadPaths_slurp_lines_Test {

    @Test
    public void test_no_loadpaths() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice();

            assertEquals("(\"res1\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "res1.txt")));

            assertEquals("(\"res2\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir1/res2.txt")));

            assertEquals("(\"res3\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir1/res3.txt")));

            assertEquals("(\"res4\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir1/11/res4.txt")));

            assertEquals("(\"res5\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir2/res5.txt")));

            assertThrows(
                    VncException.class,
                    () -> venice.eval("(io/slurp-lines src)", param(root, "dir1/res999.txt")));
        });
    }

    @Test
    public void test_relative_limited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(false));

            // from root
            assertEquals("(\"res1\")", venice.eval("(pr-str (io/slurp-lines \"res1.txt\"))"));

            // from dir1
            assertEquals("(\"res2\")", venice.eval("(pr-str (io/slurp-lines \"res2.txt\"))"));

            // from dir1
            assertEquals("(\"res3\")", venice.eval("(pr-str (io/slurp-lines \"res3.txt\"))"));

            // from dir1
            assertEquals("(\"res4\")", venice.eval("(pr-str (io/slurp-lines \"11/res4.txt\"))"));

            // from zip1.zip
            assertEquals("(\"res11\")", venice.eval("(pr-str (io/slurp-lines \"res11.txt\"))"));

            // from zip1.zip
            assertEquals("(\"res12\")", venice.eval("(pr-str (io/slurp-lines \"dir-z1/res12.txt\"))"));

            // from dir1/zip2.zip
            assertEquals("(\"res21\")", venice.eval("(pr-str (io/slurp-lines \"res21.txt\"))"));

            // from dir1/zip2.zip
            assertEquals("(\"res22\")", venice.eval("(pr-str (io/slurp-lines \"dir-z2/res22.txt\"))"));


            // OUTSIDE load paths ---------------------------------------------------------

            // from dir2/res5.txt
            assertThrows(
                    VncException.class,
                    () -> venice.eval("(io/slurp-lines src)", param(root, "dir2/res5.txt")));

            // from dir1/res999.txt  (not existing)
            assertThrows(
                    VncException.class,
                    () -> venice.eval("(io/slurp-lines src)", param(root, "dir1/res999.txt")));


            // URL/URI ----------------------------------------------------------------------

            // URL (not allowed with limited load paths)
            assertThrows(
            		com.github.jlangch.venice.SecurityException.class,
                    () -> venice.eval("(io/slurp-lines (io/->url \"http://www.google.com/robots.txt\"))"));

            // URI (not allowed with limited load paths)
            assertThrows(
            		com.github.jlangch.venice.SecurityException.class,
                    () -> venice.eval("(io/slurp-lines (io/->uri \"http://www.google.com/robots.txt\"))"));
      });
    }

    @Test
    public void test_relative_unlimited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(true));

            // from root
            assertEquals("(\"res1\")", venice.eval("(pr-str (io/slurp-lines \"res1.txt\"))"));

            // from dir1
            assertEquals("(\"res2\")", venice.eval("(pr-str (io/slurp-lines \"res2.txt\"))"));

            // from dir1
            assertEquals("(\"res3\")", venice.eval("(pr-str (io/slurp-lines \"res3.txt\"))"));

            // from dir1
            assertEquals("(\"res4\")", venice.eval("(pr-str (io/slurp-lines \"11/res4.txt\"))"));

            // from zip1.zip
            assertEquals("(\"res11\")", venice.eval("(pr-str (io/slurp-lines \"res11.txt\"))"));

            // from zip1.zip
            assertEquals("(\"res12\")", venice.eval("(pr-str (io/slurp-lines \"dir-z1/res12.txt\"))"));

            // from dir1/zip2.zip
            assertEquals("(\"res21\")", venice.eval("(pr-str (io/slurp-lines \"res21.txt\"))"));

            // from dir1/zip2.zip
            assertEquals("(\"res22\")", venice.eval("(pr-str (io/slurp-lines \"dir-z2/res22.txt\"))"));


            // OUTSIDE load paths ---------------------------------------------------------

            // from dir2/res5.txt (ok, because of 'unlimited' flag)
            assertEquals("(\"res5\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir2/res5.txt")));

            // from dir1/res999.txt  (not existing)
            assertThrows(
                    VncException.class,
                    () -> venice.eval("(io/slurp-lines src)", param(root, "dir1/res999.txt")));


            // URL/URI ----------------------------------------------------------------------

            if (InternetUtil.isInternetAvailable()) {
	            // URL
	            assertNotNull(venice.eval("(io/slurp-lines (io/->url \"http://www.google.com/robots.txt\"))"));

	            // URI
	            assertNotNull(venice.eval("(io/slurp-lines (io/->uri \"http://www.google.com/robots.txt\"))"));
            }
        });
    }


    @Test
    public void test_absolute_limited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(false));

            // from root
            assertEquals("(\"res1\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "res1.txt")));

            // from dir1
            assertEquals("(\"res2\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir1/res2.txt")));

            // from dir1
            assertEquals("(\"res3\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir1/res3.txt")));

            // from dir1
            assertEquals("(\"res4\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir1/11/res4.txt")));


            // OUTSIDE load paths ---------------------------------------------------------

            // from dir2/res5.txt
            assertThrows(
                    VncException.class,
                    () -> venice.eval("(io/slurp-lines src)", param(root, "dir2/res5.txt")));

            // from dir1/res999.txt  (not existing)
            assertThrows(
                    VncException.class,
                    () -> venice.eval("(io/slurp-lines src)", param(root, "dir1/res999.txt")));
        });
    }

    @Test
    public void test_absolute_unlimited() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice(tempFS.createSandbox(true));

            // from root
            assertEquals("(\"res1\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "res1.txt")));

            // from dir1
            assertEquals("(\"res2\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir1/res2.txt")));

            // from dir1
            assertEquals("(\"res3\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir1/res3.txt")));

            // from dir1
            assertEquals("(\"res4\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir1/11/res4.txt")));


            // OUTSIDE load paths ---------------------------------------------------------

            // from dir2/res5.txt
            assertEquals("(\"res5\")", venice.eval("(pr-str (io/slurp-lines src))", param(root, "dir2/res5.txt")));

            // from dir1/res999.txt  (not existing)
            assertThrows(
                    VncException.class,
                    () -> venice.eval("(io/slurp-lines src)", param(root, "dir1/res999.txt")));
      });
    }


    private static Map<String,Object> param(final File root, final String file) {
        return Parameters.of("src", new File(root, file));
    }

}

