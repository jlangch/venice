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
package com.github.jlangch.venice.jsr223;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.Modules;


public class VeniceScriptEngineFactory implements ScriptEngineFactory {

    public VeniceScriptEngineFactory() {
        properties.setProperty(ScriptEngine.NAME, getEngineName());
        properties.setProperty(ScriptEngine.LANGUAGE, getLanguageName());
        properties.setProperty(ScriptEngine.LANGUAGE_VERSION, getLanguageVersion());

        this.scriptEngine = new VeniceScriptEngine(this, new VeniceBindings());
    }


    @Override
    public String getEngineName() {
        return  "venice";
    }

    @Override
    public String getEngineVersion() {
        return Venice.getVersion();
    }

    @Override
    public List<String> getExtensions() {
        return new ArrayList<>(Modules.VALID_MODULES);
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList("application/venice");
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("venice");
    }

    @Override
    public String getLanguageName() {
        return "venice";
    }

    @Override
    public String getLanguageVersion() {
        return "1.0";
    }

    @Override
    public Object getParameter(final String key) {
         if (ScriptEngine.ENGINE.equals(key)) {
             return getScriptEngine();
         }

         return properties.getProperty(key);
    }

    @Override
    public String getMethodCallSyntax(
            final String obj,
            final String method,
            final String... args
    ) {
        final StringBuilder sb = new StringBuilder();

        sb.append("(. ");
        sb.append(obj);
        sb.append(" :");
        sb.append(method);
        for (String arg : args) {
            sb.append(" ");
            sb.append(arg);
        }
        sb.append(" )");

        return sb.toString();
    }

    @Override
    public String getOutputStatement(final String toDisplay) {
        return "(println " + toDisplay + ")";
    }

    @Override
    public String getProgram(final String... statements) {
        final StringBuilder sb = new StringBuilder();

        sb.append("(do \n");
        for (String statement : statements) {
            sb.append("    ");
            sb.append(statement);
            sb.append("\n");
        }
        sb.append(" )");

        return sb.toString();
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }


    private final ScriptEngine scriptEngine;
    private final Properties properties = new Properties();
}
