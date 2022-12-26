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
package com.github.jlangch.venice.impl.env;

import com.github.jlangch.venice.impl.types.VncSymbol;


public class GlobalSymbols {

    public static final VncSymbol VERSION = new VncSymbol("*version*");
    public static final VncSymbol NEWLINE = new VncSymbol("*newline*");
    public static final VncSymbol NAMESPACE = new VncSymbol("*ns*");

    public static final VncSymbol LOADED_MODULES = new VncSymbol("*loaded-modules*");
    public static final VncSymbol LOADED_FILES = new VncSymbol("*loaded-files*");

    public static final VncSymbol RUN_MODE = new VncSymbol("*run-mode*");
    public static final VncSymbol ANSI_TERM = new VncSymbol("*ansi-term*");

    public static final VncSymbol ARGV = new VncSymbol("*ARGV*");
    public static final VncSymbol REPL = new VncSymbol("*REPL*");
    public static final VncSymbol REPL_COLOR_THEME = new VncSymbol("*repl-color-theme*");

    public static final VncSymbol APP_NAME = new VncSymbol("*app-name*");
    public static final VncSymbol APP_ARCHIVE = new VncSymbol("*app-archive*");

    public static final VncSymbol STDOUT = new VncSymbol("*out*");
    public static final VncSymbol STDERR = new VncSymbol("*err*");
    public static final VncSymbol STDIN = new VncSymbol("*in*");

}
