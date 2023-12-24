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
package com.github.jlangch.venice.impl.ansi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.reader.HighlightClass;
import com.github.jlangch.venice.impl.types.VncKeyword;


public class AnsiColorTheme {

    public AnsiColorTheme(
            final String name,
            final Map<VncKeyword,String> colors
    ) {
        this.name = name;
        this.colors.putAll(colors);
        this.mapper = Arrays.stream(HighlightClass.values())
                            .collect(Collectors.toMap(
                                        p -> p,
                                        p -> new VncKeyword(p.name()
                                                             .toLowerCase()
                                                             .replace('_', '-'))));
    }

    public String getName() {
        return name;
    }

    public String getColor(final HighlightClass clazz) {
        return getColor(mapper.get(clazz));
    }

    public String getColor(final VncKeyword clazz) {
        return colors.get(clazz);
    }

    public String style(final String text, final HighlightClass clazz) {
        final String style = clazz == null ? null : getColor(clazz);
        return style == null ? text : style + text + AnsiColorTheme.ANSI_RESET;
    }


    public static String ANSI_RESET = "\u001b[0m";


    private final String name;
    private final Map<VncKeyword,String> colors = new HashMap<>();
    private final Map<HighlightClass,VncKeyword> mapper;
}
