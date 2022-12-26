/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

public class ReaderPos {

    public ReaderPos(
            final int filePos,
            final int lineNr,
            final int columnNr
    ) {
        this.filePos = filePos;
        this.lineNr = lineNr;
        this.columnNr = columnNr;
    }


    public int getFilePos() {
        return filePos;
    }

    public int getLineNr() {
        return lineNr;
    }

    public int getColumnNr() {
        return columnNr;
    }


    private final int filePos;    // zero based file position
    private final int lineNr;     // one based line number
    private final int columnNr;   // one based column number
}
