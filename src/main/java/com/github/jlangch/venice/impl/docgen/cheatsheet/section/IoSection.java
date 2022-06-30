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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class IoSection implements ISectionBuilder {

    public IoSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("I/O", "io.util");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection to = new DocSection("to", "io.to");
        all.addSection(to);
        to.addItem(diBuilder.getDocItem("print"));
        to.addItem(diBuilder.getDocItem("println"));
        to.addItem(diBuilder.getDocItem("printf"));
        to.addItem(diBuilder.getDocItem("flush"));
        to.addItem(diBuilder.getDocItem("newline"));
        to.addItem(diBuilder.getDocItem("pr"));
        to.addItem(diBuilder.getDocItem("prn"));

        final DocSection to_str = new DocSection("to-str", "io.tostr");
        all.addSection(to_str);
        to_str.addItem(diBuilder.getDocItem("pr-str"));
        to_str.addItem(diBuilder.getDocItem("with-out-str"));

        final DocSection from = new DocSection("from", "io.from");
        all.addSection(from);
        from.addItem(diBuilder.getDocItem("read-line"));
        from.addItem(diBuilder.getDocItem("read-char"));

        final DocSection classpath = new DocSection("classpath", "io.classpath");
        all.addSection(classpath);
        classpath.addItem(diBuilder.getDocItem("io/load-classpath-resource"));
        classpath.addItem(diBuilder.getDocItem("io/classpath-resource?"));

        final DocSection stream = new DocSection("stream", "io.stream");
        all.addSection(stream);
        stream.addItem(diBuilder.getDocItem("io/slurp"));
        stream.addItem(diBuilder.getDocItem("io/slurp-lines"));
        stream.addItem(diBuilder.getDocItem("io/copy-stream"));
        stream.addItem(diBuilder.getDocItem("io/slurp-stream"));
        stream.addItem(diBuilder.getDocItem("io/spit-stream"));
        stream.addItem(diBuilder.getDocItem("io/uri-stream", false));
        stream.addItem(diBuilder.getDocItem("io/file-in-stream", false));
        stream.addItem(diBuilder.getDocItem("io/string-in-stream", false));
        stream.addItem(diBuilder.getDocItem("io/bytebuf-in-stream", false));
        stream.addItem(diBuilder.getDocItem("io/wrap-os-with-buffered-writer"));
        stream.addItem(diBuilder.getDocItem("io/wrap-os-with-print-writer"));
        stream.addItem(diBuilder.getDocItem("io/wrap-is-with-buffered-reader"));

        final DocSection rd_wr = new DocSection("reader/writer", "io.readerwriter");
        all.addSection(rd_wr);
        rd_wr.addItem(diBuilder.getDocItem("io/buffered-reader"));
        rd_wr.addItem(diBuilder.getDocItem("io/buffered-writer"));

        final DocSection http = new DocSection("http", "io.http");
        all.addSection(http);
        http.addItem(diBuilder.getDocItem("io/download", false));
        http.addItem(diBuilder.getDocItem("io/internet-avail?", false));

        final DocSection other = new DocSection("other", "io.other");
        all.addSection(other);
        other.addItem(diBuilder.getDocItem("with-out-str"));
        other.addItem(diBuilder.getDocItem("with-err-str"));
        other.addItem(diBuilder.getDocItem("io/mime-type"));
        other.addItem(diBuilder.getDocItem("io/default-charset"));

        final DocSection vars = new DocSection("vars", "io.vars");
        all.addSection(vars);
        vars.addItem(diBuilder.getDocItem("*out*"));
        vars.addItem(diBuilder.getDocItem("*err*"));
        vars.addItem(diBuilder.getDocItem("*in*"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
