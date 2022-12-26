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
package com.github.jlangch.venice.jsr223;

import java.io.Reader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;


public class VeniceScriptEngine
    extends AbstractScriptEngine
    implements Compilable, Invocable {

    public VeniceScriptEngine() {
        this.factory = null;
    }

    public VeniceScriptEngine(final ScriptEngineFactory factory) {
        this.factory = factory;
    }

    public VeniceScriptEngine(final Bindings bindings) {
        super(bindings);
        this.factory = null;
    }

    public VeniceScriptEngine(
             final ScriptEngineFactory factory,
            final Bindings bindings
    ) {
        super(bindings);
        this.factory = factory;
    }

    @Override
    public Object invokeMethod(
            final Object thiz,
            final String name,
            final Object... args
    ) throws ScriptException, NoSuchMethodException {
        return null;
    }

    @Override
    public Object invokeFunction(
            final String name,
            final Object... args
    ) throws ScriptException, NoSuchMethodException {
        return null;
    }

    @Override
    public <T> T getInterface(final Class<T> clasz) {
        return null;
    }

    @Override
    public <T> T getInterface(final Object thiz, final Class<T> clasz) {
        return null;
    }

    @Override
    public CompiledScript compile(final String script) throws ScriptException {
        return null;
    }

    @Override
    public CompiledScript compile(final Reader script) throws ScriptException {
        return null;
    }

    @Override
    public Object eval(
            final String script,
            final ScriptContext context
    ) throws ScriptException {
        return null;
    }

    @Override
    public Object eval(
            final Reader reader,
            final ScriptContext context
    ) throws ScriptException {
        return null;
    }

    @Override
    public Bindings createBindings() {
        return null;
    }

    @Override
    public ScriptEngineFactory getFactory() {
         return factory;
    }


    private final ScriptEngineFactory factory;
}
