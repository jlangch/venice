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
package com.github.jlangch.venice.impl.reader;


public class Token {

    public Token(
            final TokenType type,
            final String token,
            final String file,
            final ReaderPos pos
    ) {
        this.type = type;
        this.token = token;
        this.file = file == null || file.isEmpty() ? "unknown" : file;
        this.filePos = pos.getFilePos();
        this.line = pos.getLineNr();
        this.col = pos.getColumnNr();
    }


    public TokenType getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public String getFile() {
        return file;
    }

    // zero based start position
    public int getFileStartPos() {
        return filePos;
    }

    // zero based end position
    public int getFileEndPos() {
        return filePos + token.length() - 1;
    }

    // one based line number
    public int getLine() {
        return line;
    }

    // one based column number
    public int getColumn() {
        return col;
    }

    public char charAt(int index) {
        return token.charAt(index);
    }

    public boolean equals(String str) {
        return token.equals(str);
    }


    public boolean isWhitespaces() {
        return type == TokenType.WHITESPACES;
    }

    public boolean isComment() {
        return type == TokenType.COMMENT;
    }

    public boolean isWhitespacesOrComment() {
        return isWhitespaces() || isComment();
    }

    public boolean isString() {
        return type == TokenType.STRING;
    }

    public boolean isStringBlock() {
        return type == TokenType.STRING_BLOCK;
    }

    public boolean isAny() {
        return type == TokenType.ANY;
    }


    @Override
    public String toString() {
        return String.format("%s (file: %s, line %d, column %d)", token, file, line, col);
    }


    private final TokenType type;
    private final String token;
    private final String file;
    private final int filePos;
    private final int line;
    private final int col;
}
