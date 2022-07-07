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
package com.github.jlangch.venice.impl.docgen.cheatsheet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;


public class CodeSnippetReader {

    public List<CodeSnippet> readSnippets() {
        try {
            final List<String> lines = load();

            final List<Integer> snippetStartLines = new ArrayList<>();
            for(int ii=0; ii<lines.size(); ii++) {
                if (lines.get(ii).startsWith("**")) {
                    snippetStartLines.add(ii);
                }
            }
            snippetStartLines.add(lines.size());

            final List<CodeSnippet> snippets = new ArrayList<>();
            for(int ii=0; ii<snippetStartLines.size()-1; ii++) {
                snippets.add(
                        new CodeSnippet(
                                lines.get(snippetStartLines.get(ii))
                                     .substring(2),
                                lines.subList(
                                        snippetStartLines.get(ii)+1,
                                        snippetStartLines.get(ii+1))
                                     .stream()
                                     .collect(Collectors.joining("\n"))
                                     .trim()));
            }

            return snippets;
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to load code snippets", ex);
        }
    }

    private List<String> load() {
        return StringUtil.splitIntoLines(
                new ClassPathResource(Venice.class.getPackage(), "docgen/cheatsheet.snippets")
                        .getResourceAsString("UTF-8"));
    }

}
