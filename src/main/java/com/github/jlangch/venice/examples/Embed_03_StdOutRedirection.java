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
package com.github.jlangch.venice.examples;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class Embed_03_StdOutRedirection {

    public static void main(final String[] args) {
        final Venice venice = new Venice();

        Object result;

        // #1: redirect stdout/stderr to the <null> device
        result = venice.eval(
                   "(do               \n" +
                   "  (println [1 2]) \n" +
                   "  10)             ",
                   Parameters.of("*out*", null,
                                 "*err*", null));
        System.out.println("result: " + result);
        System.out.println();
        // result: 10

        // #2: capture stdout within the script and return it as the result
        result = venice.eval(
                    "(with-out-str     \n" +
                    "  (println [1 2]) \n" +
                    "  10)             ");
        System.out.println("result: " + result);
        // result: [1 2]

        // #3: capturing stdout/stderr preserving the script result
        try(CapturingPrintStream ps_out = new CapturingPrintStream();
            CapturingPrintStream ps_err = new CapturingPrintStream()
        ) {
           result = venice.eval(
                         "(do                        \n" +
                         "  (println [3 4])          \n" +
                         "  (println *err* :failure) \n" +
                         "  100)                     ",
                         Parameters.of("*out*", ps_out,
                                       "*err*", ps_err));
           System.out.println("result: " + result);
           System.out.print("stdout: " + ps_out.getOutput());
           System.out.print("stderr: " + ps_err.getOutput());
           // result: 100
           // stdout: [3 4]
           // stderr: :failure
        }
    }
}
