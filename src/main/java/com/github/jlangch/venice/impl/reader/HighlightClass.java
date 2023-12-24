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

public enum HighlightClass {
    // whitespaces
    COMMENT,               // "; ...."
    WHITESPACES,           // " \t", "\n", "  \n"

    // atoms
    STRING,                // "lorem", """lorem"""
    NUMBER,                // 100, 100I, 100.0, 100.23M
    CONSTANT,              // nil, true, false
    KEYWORD,               // :alpha
    SYMBOL,                // alpha
    SYMBOL_SPECIAL_FORM,   // def, loop, ...
    SYMBOL_FUNCTION_NAME,  // +, println, ...
    SYMBOL_MACRO_NAME,     // and, case ...
    SYMBOL_EAR_MUFFS,      // *out*, *err*

    // quotes
    QUOTE,                 // '
    QUASI_QUOTE,           // `
    UNQUOTE,               // ~
    UNQUOTE_SPLICING,      // ~@

    META,                  // ^private, ^{:arglist '() :doc "...."}
    AT,                    // @
    HASH,                  // #

    // braces
    BRACE_BEGIN,           // {
    BRACE_END,             // {
    BRACKET_BEGIN,         // [
    BRACKET_END,           // ]
    PARENTHESIS_BEGIN,     // (
    PARENTHESIS_END,       // )

    UNKNOWN,               // anything that could not be classified

    UNPROCESSED;           // unprocessed input
}
