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
package com.github.jlangch.venice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StringUtil;


public class CallbackPrintStreamTest {

    @Test
    public void test() throws Exception {
        final PrintStream orgStdOut = System.out;
        final List<String> captured = new ArrayList<>();

        try {
            System.setOut(new CallbackPrintStream(true, s -> captured.add(s)));

            System.out.println(100);
            System.out.println("abc");
            System.out.println("d\ne");
            System.out.print(300);
            System.out.print('-');
            System.out.print(400);
            System.out.flush();
            System.out.println(500.0);

            assertEquals(5, captured.size());

            // Must run on *nix and Windows
            assertEquals("100\n",   StringUtil.crlf_to_lf(captured.get(0)));
            assertEquals("abc\n",   StringUtil.crlf_to_lf(captured.get(1)));
            assertEquals("d\ne\n",  StringUtil.crlf_to_lf(captured.get(2)));
            assertEquals("300-400", StringUtil.crlf_to_lf(captured.get(3)));
            assertEquals("500.0\n", StringUtil.crlf_to_lf(captured.get(4)));
        }
        finally {
            System.setOut(orgStdOut);
        }
    }


}
