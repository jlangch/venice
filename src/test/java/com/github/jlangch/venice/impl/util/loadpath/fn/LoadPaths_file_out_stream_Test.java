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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.loadpath.TempFS;


public class LoadPaths_file_out_stream_Test {

    @Test
    public void test_no_loadpaths() {
        TempFS.with((tempFS, root) -> {
            final Venice venice = new Venice();

            venice.eval("(io/spit-stream (io/file-out-stream src) \"1234\" :flush true)", param(root, "spit.txt"));
            assertEquals("1234", venice.eval("(io/slurp src)", param(root, "spit.txt")));
        });
    }

    @Test
    public void test_relative_limited() {
        TempFS.with((tempFS, root) -> {
            final Venice veniceRD = new Venice();
            final Venice veniceWR = new Venice(tempFS.createSandbox(false));

            // to dir1/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream \"spit.txt\") \"1234\" :flush true)");
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir1/spit.txt")));

            // to dir1/11/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream \"11/spit.txt\") \"1234\" :flush true)");
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir1/11/spit.txt")));


            // OUTSIDE load paths ---------------------------------------------------------

            // to dir1/99/spit.txt
            assertThrows(
                    VncException.class,
                    () -> veniceWR.eval("(io/spit-stream (io/file-out-stream \"99/spit.txt\") \"1234\" :flush true)"));
       });
    }

    @Test
    public void test_relative_unlimited() {
        TempFS.with((tempFS, root) -> {
            final Venice veniceRD = new Venice();
            final Venice veniceWR = new Venice(tempFS.createSandbox(true));

            // to dir1/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream \"spit.txt\") \"1234\" :flush true)");
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir1/spit.txt")));

            // to dir1/11/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream \"11/spit.txt\") \"1234\" :flush true)");
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir1/11/spit.txt")));


            // OUTSIDE load paths ---------------------------------------------------------

            // to dir1/../dir2/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream \"../dir2/spit.txt\") \"1234\" :flush true)");
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir2/spit.txt")));
        });
    }

    @Test
    public void test_absolute_limited() {
        TempFS.with((tempFS, root) -> {
            final Venice veniceRD = new Venice();
            final Venice veniceWR = new Venice(tempFS.createSandbox(false));

            // to dir1/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream src) \"1234\" :flush true)", param(root, "dir1/spit.txt"));
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir1/spit.txt")));

            // to dir1/11/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream src) \"1234\" :flush true)", param(root, "dir1/11/spit.txt"));
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir1/11/spit.txt")));


            // OUTSIDE load paths ---------------------------------------------------------

            // to dir1/../dir2/spit.txt => dir2/spit.txt
            assertThrows(
                    VncException.class,
                    () -> veniceWR.eval("(io/spit-stream (io/file-out-stream src) \"1234\" :flush true)", param(root, "dir1/../dir2/spit.txt")));

            // to dir1/99/spit.txt
            assertThrows(
                    VncException.class,
                    () -> veniceWR.eval("(io/spit-stream (io/file-out-stream src) \"1234\" :flush true)", param(root, "dir1/99/spit.txt")));
       });
    }

    @Test
    public void test_absolute_unlimited() {
        TempFS.with((tempFS, root) -> {
            final Venice veniceRD = new Venice();
            final Venice veniceWR = new Venice(tempFS.createSandbox(true));

            // to dir1/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream src) \"1234\" :flush true)", param(root, "dir1/spit.txt"));
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir1/spit.txt")));

            // to dir1/11/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream src) \"1234\" :flush true)", param(root, "dir1/11/spit.txt"));
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir1/11/spit.txt")));


            // OUTSIDE load paths ---------------------------------------------------------

            // to dir1/../dir2/spit.txt => dir2/spit.txt
            veniceWR.eval("(io/spit-stream (io/file-out-stream src) \"1234\" :flush true)", param(root, "dir1/../dir2/spit.txt"));
            assertEquals("1234", veniceRD.eval("(io/slurp src)", param(root, "dir2/spit.txt")));

            // to dir1/99/spit.txt
            assertThrows(
                    VncException.class,
                    () -> veniceWR.eval("(io/spit-stream (io/file-out-stream src) \"1234\" :flush true)", param(root, "dir1/99/spit.txt")));
       });
    }


    private static Map<String,Object> param(final File root, final String file) {
        return Parameters.of("src", new File(root, file));
    }
}

