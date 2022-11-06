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


public class IoFileSection implements ISectionBuilder {

    public IoFileSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("File I/O", "io.file");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection file = new DocSection("file", "io.file_");
        all.addSection(file);
        file.addItem(diBuilder.getDocItem("io/file"));
        file.addItem(diBuilder.getDocItem("io/file-parent"));
        file.addItem(diBuilder.getDocItem("io/file-name"));
        file.addItem(diBuilder.getDocItem("io/file-path"));
        file.addItem(diBuilder.getDocItem("io/file-absolute"));
        file.addItem(diBuilder.getDocItem("io/file-canonical"));
        file.addItem(diBuilder.getDocItem("io/file-ext"));
        file.addItem(diBuilder.getDocItem("io/file-ext?"));
        file.addItem(diBuilder.getDocItem("io/file-size", false));
        file.addItem(diBuilder.getDocItem("io/file-last-modified", false));

        final DocSection file_dir = new DocSection("file dir", "io.filedir");
        all.addSection(file_dir);
        file_dir.addItem(diBuilder.getDocItem("io/mkdir"));
        file_dir.addItem(diBuilder.getDocItem("io/mkdirs"));

        final DocSection file_io = new DocSection("file i/o", "io.fileio");
        all.addSection(file_io);
        file_io.addItem(diBuilder.getDocItem("io/slurp"));
        file_io.addItem(diBuilder.getDocItem("io/slurp-lines"));
        file_io.addItem(diBuilder.getDocItem("io/spit"));
        file_io.addItem(diBuilder.getDocItem("io/copy-file"));
        file_io.addItem(diBuilder.getDocItem("io/move-file"));
        file_io.addItem(diBuilder.getDocItem("io/touch-file"));

        final DocSection file_del = new DocSection("file delete", "io.filedelete");
        all.addSection(file_del);
        file_del.addItem(diBuilder.getDocItem("io/delete-file"));
        file_del.addItem(diBuilder.getDocItem("io/delete-files-glob"));
        file_del.addItem(diBuilder.getDocItem("io/delete-file-tree"));
        file_del.addItem(diBuilder.getDocItem("io/delete-file-on-exit"));

        final DocSection file_list = new DocSection("file list", "io.filelist");
        all.addSection(file_list);
        file_list.addItem(diBuilder.getDocItem("io/list-files", false));
        file_list.addItem(diBuilder.getDocItem("io/list-files-glob", false));
        file_list.addItem(diBuilder.getDocItem("io/list-file-tree", false));

        final DocSection file_test = new DocSection("file test", "io.filetest");
        all.addSection(file_test);
        file_test.addItem(diBuilder.getDocItem("io/file?"));
        file_test.addItem(diBuilder.getDocItem("io/file-absolute?"));
        file_test.addItem(diBuilder.getDocItem("io/exists-file?"));
        file_test.addItem(diBuilder.getDocItem("io/exists-dir?"));
        file_test.addItem(diBuilder.getDocItem("io/file-can-read?", false));
        file_test.addItem(diBuilder.getDocItem("io/file-can-write?", false));
        file_test.addItem(diBuilder.getDocItem("io/file-can-execute?", false));
        file_test.addItem(diBuilder.getDocItem("io/file-hidden?", false));
        file_test.addItem(diBuilder.getDocItem("io/file-symbolic-link?", false));
        file_test.addItem(diBuilder.getDocItem("io/file-within-dir?"));

        final DocSection file_glob = new DocSection("file glob", "io.fileglob");
        all.addSection(file_glob);
        file_glob.addItem(diBuilder.getDocItem("io/glob-path-matcher", false));
        file_glob.addItem(diBuilder.getDocItem("io/file-matches-glob?"));
        file_glob.addItem(diBuilder.getDocItem("io/list-files-glob", false));
        file_glob.addItem(diBuilder.getDocItem("io/delete-files-glob", false));

        final DocSection file_uri = new DocSection("URL/URI", "io.url_uri");
        all.addSection(file_uri);
        file_uri.addItem(diBuilder.getDocItem("io/->url"));
        file_uri.addItem(diBuilder.getDocItem("io/->uri"));

        final DocSection file_watch = new DocSection("file watch", "io.filewatch");
        all.addSection(file_watch);
        file_watch.addItem(diBuilder.getDocItem("io/await-for", false));
        file_watch.addItem(diBuilder.getDocItem("io/watch-dir", false));
        file_watch.addItem(diBuilder.getDocItem("io/close-watcher", false));

        final DocSection file_tmp = new DocSection("file tmp", "io.filetmp");
        all.addSection(file_tmp);
        file_tmp.addItem(diBuilder.getDocItem("io/temp-file"));
        file_tmp.addItem(diBuilder.getDocItem("io/temp-dir"));
        file_tmp.addItem(diBuilder.getDocItem("io/tmp-dir"));

        final DocSection file_other = new DocSection("file user", "io.fileuser");
        all.addSection(file_other);
        file_other.addItem(diBuilder.getDocItem("io/user-dir"));
        file_other.addItem(diBuilder.getDocItem("io/user-home-dir"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
