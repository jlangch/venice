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
package com.github.jlangch.venice.util;

import com.github.jlangch.venice.impl.util.StringUtil;


public abstract class Ansi {

    public static String bold(final String text) {
        return StringUtil.isEmpty(text) ? "" : "\u001b[1m" + text +  "\u001b[22m";
    }

    public static String italic(final String text) {
        return StringUtil.isEmpty(text) ? "" : "\u001b[3m" + text +  "\u001b[23m";
    }

    public static String boldItalic(final String text) {
        return StringUtil.isEmpty(text) ? "" : "\u001b[1m\u001b[3m" + text +  "\u001b[21m\u001b[23m";
    }

    public static String underline(final String text) {
        return StringUtil.isEmpty(text) ? "" : "\u001b[4m" + text +  "\u001b[24m";
    }

}
