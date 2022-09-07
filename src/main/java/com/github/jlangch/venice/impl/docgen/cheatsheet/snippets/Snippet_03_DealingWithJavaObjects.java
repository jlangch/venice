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
package com.github.jlangch.venice.impl.docgen.cheatsheet.snippets;

import java.awt.Point;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class Snippet_03_DealingWithJavaObjects {
   public static void main(String[] args) {
      Venice venice = new Venice();

      // returns a string: "Point=(x: 100.0, y: 200.0)"
      String ret = (String)venice.eval(
                            "(let [x (:x point)                            \n" +
                            "      y (:y point)]                           \n" +
                            "  (str \"Point=(x: \" x \", y: \" y \")\"))   ",
                            Parameters.of("point", new Point(100, 200)));

      // returns a java.awt.Point: [x=110,y=220]
      Point point = (Point)venice.eval(
                            "(. :java.awt.Point :new (+ x 10) (+ y 20))",
                            Parameters.of("x", 100, "y", 200));
   }
}
