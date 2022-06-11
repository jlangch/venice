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
package com.github.jlangch.venice.impl.repl;

import java.io.PrintStream;

import org.jline.terminal.Terminal;

import com.github.jlangch.venice.util.NullOutputStream;


public class ReplPrintStream extends PrintStream {

    public ReplPrintStream(
            final Terminal terminal,
            final String colorEscape
    ) {
        super(new NullOutputStream());
        this.terminal = terminal;
        this.colorEscape = colorEscape;
    }


    @Override
    public PrintStream append(final CharSequence csq) {
        print(csq == null ? "null" : csq.toString());
        return this;
    }

    @Override
    public PrintStream append(final CharSequence csq, final int start, final int end) {
        final CharSequence cs = (csq == null ? "null" : csq);
        print(cs.subSequence(start, end).toString());
        return this;
    }

    @Override
    public PrintStream append(final char c) {
        print(c);
        return this;
    }

    @Override
    public void print(final boolean x) {
        print(String.valueOf(x));
    }

    @Override
    public void print(final int x) {
        print(String.valueOf(x));
    }

    @Override
    public void print(final long x) {
        print(String.valueOf(x));
    }

    @Override
    public void print(final float x) {
        print(String.valueOf(x));
    }

    @Override
    public void print(final double x) {
        print(String.valueOf(x));
    }

    @Override
    public void print(final char x) {
        print(String.valueOf(x));
    }

    @Override
    public void print(final char[] x) {
        print(String.valueOf(x));
    }

    @Override
    public void print(final Object x) {
        print(String.valueOf(x));
    }

    @Override
    public void print(final String s) {
        printToTerminal(s);
    }

    @Override
    public void println() {
        println("");
    }

    @Override
    public void println(final boolean x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(final int x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(final long x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(final float x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(final double x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(final char x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(final char[] x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(final Object x) {
        println(String.valueOf(x));
    }

    @Override
    public void println(final String s) {
        printToTerminal(s + "\n");
    }

    @Override
    public void write(final byte buf[], final int off, final int len) {
        throw new RuntimeException(
                "Method write(byte[],int,int) is not supported");
    }

    @Override
    public void write(final int b) {
        throw new RuntimeException(
                "Method write(int) is not supported");
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }


    private void printToTerminal(final String s) {
        synchronized (this) {
            if (colorEscape != null) terminal.writer().print(colorEscape);

            terminal.writer().print(s);

            if (colorEscape != null) terminal.writer().print(ReplConfig.ANSI_RESET);

            terminal.flush();
        }
    }


    private final Terminal terminal;
    private final String colorEscape;
}
