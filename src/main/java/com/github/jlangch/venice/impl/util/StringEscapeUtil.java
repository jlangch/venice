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
package com.github.jlangch.venice.impl.util;

import java.util.Arrays;
import java.util.List;


public class StringEscapeUtil {

    public static String escapeXml(final String s) {
        return replace(s, XML_ESCAPES);
    }

    public static String escapeHtml(final String s) {
        return replace(s, HTML_ESCAPES);
    }

    private static String replace(final String str, final List<Tuple2<String,String>> replacements) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String s = str;
        for(Tuple2<String,String> r : replacements) {
            s = s.replace(r._1, r._2);
        }
        return s;
    }


    private static final List<Tuple2<String,String>> XML_ESCAPES =
            Arrays.asList(
                    Tuple2.of("&", "&amp;"),
                    Tuple2.of("<", "&lt;"),
                    Tuple2.of(">", "&gt;"),
                    Tuple2.of("\"", "&quot;"),
                    Tuple2.of("'", "&apos;"));

    private static final List<Tuple2<String,String>> HTML_ESCAPES =
            Arrays.asList(
                    Tuple2.of("&", "&amp;"),
                    Tuple2.of("<", "&lt;"),
                    Tuple2.of(">", "&gt;"),
                    Tuple2.of("\"", "&quot;"),
                    Tuple2.of("'", "&apos;"));
}
