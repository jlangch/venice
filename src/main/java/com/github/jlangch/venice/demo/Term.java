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
package com.github.jlangch.venice.demo;

import java.util.Arrays;

import org.jline.builtins.Tmux;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;


public class Term {

    public static void main(final String[] args){
        try {
            final TerminalBuilder builder = TerminalBuilder
                                                .builder()
                                                .streams(System.in, System.out)
                                                .system(true)
                                                .jna(false);

            final Terminal terminal = builder.build();

            Tmux tm = new Tmux(terminal, System.err, Term::open);
            tm.run();
            tm.execute(System.out, System.err, Arrays.asList(args));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void open(Terminal terminal) {
        terminal.writer().println("Terminal opened");
    }

}
