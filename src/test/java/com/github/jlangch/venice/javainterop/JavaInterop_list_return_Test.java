/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.javainterop;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.CollectionUtil;


public class JavaInterop_list_return_Test {

    @Test
    public void test_convert_1() {
        final Venice venice = new Venice();

        final String script =
                "(let [result (->> (. :com.github.jlangch.venice.javainterop.JavaInterop_list_return_Test$Employees :getEmployees) \n" +
                "                  (map #(str (. % :getFirst) \"/\" (. % :getLast))))]   \n" +
                "  (assert (= \"John/Myers\" (first result))))";

       venice.eval(script);
    }

    @Test
    public void test_convert_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "                                                     \n" +
                "  (defn person->map [person]                         \n" +
                "    (ordered-map                                     \n" +
                "        :first (. person :getFirst)                  \n" +
                "        :last  (. person :getLast)))                 \n" +
                "                                                     \n" +
                "  (let [result (->> (. :com.github.jlangch.venice.javainterop.JavaInterop_list_return_Test$Employees :getEmployees) \n" +
                "                    (map person->map)                \n" +
                "                    (tee prn))]                      \n" +
                "    (assert (= {:first \"John\" :last \"Myers\"}     \n" +
                "               (first result)))))     ";

       venice.eval(script);
    }


    public static class Person {
        public Person(final String first, final String last) {
            this.first = first;
            this.last = last;
        }
        public String getFirst() {
            return first;
        }
        public String getLast() {
            return last;
        }
        public String getName() {
            return "Person: " + first + " " + last;
        }
        @Override public String toString() { return getName(); }
        private final String first;
        private final String last;
    }

    public static class Employees {
        public static List<Person> getEmployees() {
            final List<Person> items = CollectionUtil.toList(
                                        new Person("John", "Myers"),
                                        new Person("Mary", "Smith"),
                                        new Person("Peter", "Smith"));
            return items;
        }
    }
}
