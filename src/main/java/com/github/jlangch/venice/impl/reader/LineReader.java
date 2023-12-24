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
package com.github.jlangch.venice.impl.reader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class LineReader {

    public LineReader(final String s) {
        lines = s == null ? new ArrayList<>() : lines(s);
        lineNr = 1;
        lnNext = lines.isEmpty() ? EOF : lines.get(0);
    }

    public String peek() {
        return lnNext;
    }

    public void consume() {
        if (lnNext != EOF) {
            lnNext = lineNr >= lines.size() ? EOF : lines.get(lineNr++);
        }
    }

    public int getLineNr() {
        return lineNr;
    }

    public int size() {
        return lines.size();
    }

    public boolean eof() {
        return lnNext == EOF;
    }

    private List<String> lines(final String s) {
        if (s.isEmpty()) {
            final List<String> lines = new ArrayList<>();
            lines.add("");
            return lines;
        }
        else {
            final List<String> lines = new BufferedReader(new StringReader(s))
                                            .lines()
                                            .collect(Collectors.toList());

            if (s.endsWith("\n")) {
                lines.add("");
            }

            return lines;
        }
    }



    private static final String EOF = null;

    private final List<String> lines;

    private String lnNext;
    private int lineNr;
}
