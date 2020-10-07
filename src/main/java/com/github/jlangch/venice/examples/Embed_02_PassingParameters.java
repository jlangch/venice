/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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

import java.awt.Point;
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class Embed_02_PassingParameters {
    public static void main(final String[] args) {
        final Venice venice = new Venice();

        // returns a long: 10
        System.out.println(
                venice.eval(
                        "(+ x y 1)", 
                        Parameters.of("x", 6, "y", 3L)));

        // up-front macro expansion, returns a long: 10 
        System.out.println(
                venice.eval(
                        "test",
                        "(+ x y 1)", 
                        true, // up-front macro expansion
                        Parameters.of("x", 6, "y", 3L)));

        // returns a string: "Point=(x: 100.0, y: 200.0)"
        System.out.println(
                venice.eval(
                        "(str \"Point=(x: \" (:x point) \", y: \" (:y point) \")\")", 
                        Parameters.of("point", new Point(100, 200))));

        // returns a java.awt.Point: [x=100,y=200]
        System.out.println(
                venice.eval(
                        "(. :java.awt.Point :new x y)", 
                        Parameters.of("x", 100, "y", 200)));
    }
}