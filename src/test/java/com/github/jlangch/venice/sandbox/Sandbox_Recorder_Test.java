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
package com.github.jlangch.venice.sandbox;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.SandboxRecorder;


public class Sandbox_Recorder_Test {

    @Test
    @Disabled
    public void testSandboxRecorder() {
        final Venice venice = new Venice(new SandboxRecorder());

        venice.eval("(. :java.lang.Math :min 20 30)");
        venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5)");

        final String lisp =
                "(do                                                  " +
                "   (def fmt (. :java.time.format.DateTimeFormatter   " +
                "               :ofPattern                            " +
                "               \"YYYY-MM-dd'T'HH:mm:ss.SSS\"))       " +
                "                                                     " +
                "   (let [now (. :java.time.ZonedDateTime :now)]      " +
                "        (. fmt :parse (. fmt :format now))           " +
                "        (. fmt :format now))                         " +
                ")                                                    ";

        System.out.println(venice.eval(lisp));
    }

}
