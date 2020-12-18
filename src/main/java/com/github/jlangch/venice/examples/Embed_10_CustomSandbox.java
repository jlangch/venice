/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Embed_10_CustomSandbox {

    public static void main(final String[] args) {
        final IInterceptor interceptor =
                new SandboxInterceptor(
                        new SandboxRules()
                                .rejectAllVeniceIoFunctions()
                                .withClasses(
                                    "java.lang.Math:PI", 
                                    "java.lang.Math:min", 
                                    "java.lang.Math:max", 
                                    "java.time.ZonedDateTime:*", 
                                    "java.awt.**:*", 
                                    "java.util.ArrayList:new",
                                    "java.util.ArrayList:add"));

        final Venice venice = new Venice(interceptor);

        // rule: "java.lang.Math:PI"
        // => OK (static field)
        venice.eval("(. :java.lang.Math :PI)");

        // rule: "java.lang.Math:min"
        // => OK (static method)
        venice.eval("(. :java.lang.Math :min 20 30)");
        
        // rule: "java.lang.Math:max"
        // => OK (static method)
        venice.eval("(. :java.lang.Math :max 20 30)");
        
        // rule: "java.time.ZonedDateTime:*"
        // => OK (constructor & instance method)
        venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5))");
        
        // rule: "java.awt.**:*"
        // => OK (constructor & instance method)
        venice.eval("(. (. :java.awt.color.ICC_ColorSpace                  \n" +
                    "      :getInstance                                    \n" +
                    "      (. :java.awt.color.ColorSpace :CS_LINEAR_RGB))  \n" +
                    "   :getMaxValue                                       \n" +
                    "   0)                                                 ");
        
        // rule: "java.util.ArrayList:new"
        // => OK (constructor)
        venice.eval("(. :java.util.ArrayList :new)");
    
        // rule: "java.util.ArrayList:add"
        // => OK (constructor & instance method)
        venice.eval(
                "(doto (. :java.util.ArrayList :new)  " +
                "      (. :add 1)                     " +
                "      (. :add 2))                    ");

        // => FAIL (static method) with Sandbox SecurityException
        venice.eval("(. :java.lang.System :exit 0)"); 
    }
}