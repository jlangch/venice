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
package com.github.jlangch.venice.jsr223;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;


public class VeniceScriptContext implements ScriptContext {

    @Override
    public void setBindings(final Bindings bindings, final int scope) {
    }

    @Override
    public Bindings getBindings(final int scope) {
        return null;
    }

    @Override
    public void setAttribute(final String name, final Object value, final int scope) {
    }

    @Override
    public Object getAttribute(final String name, final int scope) {
        return null;
    }

    @Override
    public Object removeAttribute(final String name, final int scope) {
        return null;
    }

    @Override
    public Object getAttribute(final String name) {
        return null;
    }

    @Override
    public int getAttributesScope(final String name) {
        return 0;
    }

    @Override
    public Writer getWriter() {
        return null;
    }

    @Override
    public Writer getErrorWriter() {
        return null;
    }

    @Override
    public void setWriter(final Writer writer) {
    }

    @Override
    public void setErrorWriter(final Writer writer) {
    }

    @Override
    public Reader getReader() {
        return null;
    }

    @Override
    public void setReader(final Reader reader) {
    }

    @Override
    public List<Integer> getScopes() {
        return null;
    }

}
