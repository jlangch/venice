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

import java.time.ZonedDateTime;

import com.github.jlangch.venice.Venice;


public class Embed_08_JavaInterop {
    public static void main(final String[] args) {
        final Venice venice = new Venice();
        
        // qualified classes
        final Long val = (Long)venice.eval("(. :java.lang.Math :min 20 30)");
        
        // class import
        final ZonedDateTime ts = (ZonedDateTime)venice.eval(
                                    "(do " +
                                    "   (import :java.time.ZonedDateTime) " +
                                    "   (. (. :ZonedDateTime :now) :plusDays 5))");

        System.out.println(val);
        System.out.println(ts);
    }
}