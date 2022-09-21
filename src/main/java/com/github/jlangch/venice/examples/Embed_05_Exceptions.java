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

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class Embed_05_Exceptions {
    public static void main(final String[] args) {
        try {
            final String script =
                    "(do                               \n" +
                    "  (defn speed [distance time]     \n" +
                    "     (/ distance time))           \n" +
                    "                                  \n" +
                    "   (str (speed 20 0) \"km/h\"))   ";

            new Venice().eval("test", script);
        }
        catch(VncException ex) {
            ex.printVeniceStackTrace();
        }
        catch(RuntimeException ex) {
            ex.printStackTrace();
        }
    }
}
