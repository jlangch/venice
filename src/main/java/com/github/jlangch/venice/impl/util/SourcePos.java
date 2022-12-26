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
package com.github.jlangch.venice.impl.util;

import com.github.jlangch.venice.impl.types.VncVal;


public class SourcePos {

    public SourcePos(
            final String file,
            final int line,
            final int column
    ) {
        this.file = file == null || file.isEmpty() ? "unknown" : file;
        this.line = line <= 0 ? -1 : line;
        this.column = column <= 0 ? -1 : column;
    }


    public static SourcePos fromVal(final VncVal val) {
        return fromMeta(val.getMeta());
    }

    public static SourcePos fromMeta(final VncVal meta) {
        return new SourcePos(
                    MetaUtil.getFile(meta),
                    MetaUtil.getLine(meta),
                    MetaUtil.getCol(meta));
    }


    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public int getLineOrElse(final int elseLine) {
        return line < 0 ? elseLine : line;
    }

    public int getColumn() {
        return column;
    }

    public int getColumnOrElse(final int elseColumn) {
        return column < 0 ? elseColumn : column;
    }


    @Override
    public String toString() {
        return String.format("%s> line: %d, col: %d", file, line, column);
    }


    private final String file;
    private final int line;
    private final int column;
}
