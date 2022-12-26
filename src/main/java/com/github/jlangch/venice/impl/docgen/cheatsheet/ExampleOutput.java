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
package com.github.jlangch.venice.impl.docgen.cheatsheet;

import com.github.jlangch.venice.impl.docgen.util.CodeHighlighter;
import com.github.jlangch.venice.impl.util.StringEscapeUtil;
import com.github.jlangch.venice.impl.util.StringUtil;

public class ExampleOutput {

    public ExampleOutput(
            final String name,
            final String example,
            final String exampleXmlStyled
    ) {
        this(name, example, exampleXmlStyled, null, null, null, null);
    }

    public ExampleOutput(
            final String name,
            final String example,
            final String exampleXmlStyled,
            final String stdout,
            final String stderr,
            final String result
    ) {
        this(name, example, exampleXmlStyled, stdout, stderr, result, null);
    }

    public ExampleOutput(
            final String name,
            final String example,
            final String exampleXmlStyled,
            final String stdout,
            final String stderr,
            final RuntimeException ex
    ) {
        this(name, example, exampleXmlStyled, stdout, stderr, null, ex);
    }

    private ExampleOutput(
            final String name,
            final String example,
            final String exampleXmlStyled,
            final String stdout,
            final String stderr,
            final String result,
            final RuntimeException ex
    ) {
        this.name = name;
        this.example = StringUtil.emptyToNull(StringUtil.trimRight(example));
        this.exampleXmlStyled = exampleXmlStyled;
        this.stdout = StringUtil.emptyToNull(StringUtil.trimRight(stdout));
        this.stderr = StringUtil.emptyToNull(StringUtil.trimRight(stderr));
        this.result = StringUtil.emptyToNull(StringUtil.trimRight(result));
        this.ex = ex;
    }



    public String getName() {
        return name;
    }

    public String getExample() {
        return example;
    }

    public String getExampleXmlStyled() {
        return exampleXmlStyled;
    }

    public String getResult() {
        return result;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public RuntimeException getEx() {
        return ex;
    }

    public String getExString() {
        return ex != null
                ? String.format(
                        "%s: %s",
                        ex.getClass().getSimpleName(),
                        ex.getMessage())
                : null;
    }

    public String render() {
        final StringBuilder sb = new StringBuilder();

        sb.append(example);

        if (stdout != null) {
            sb.append("\n");
            sb.append(stdout);
        }

        if (stderr != null) {
            sb.append("\n");
            sb.append(stderr);
        }

        if (result != null) {
            sb.append("\n");
            sb.append("=> ").append(result);
        }

        if (ex != null) {
            sb.append("\n");
            sb.append("=> ").append(getExString());
        }

        return sb.toString();
    }

    public String renderXmlStyled() {
        final StringBuilder sb = new StringBuilder();

        sb.append(exampleXmlStyled);

        if (stdout != null) {
            sb.append("\n");
            sb.append(CodeHighlighter.style(stdout, "#808080"));
        }

        if (stderr != null) {
            sb.append("\n");
            sb.append(CodeHighlighter.style(stderr, "#808080"));
        }

        if (result != null) {
            sb.append("\n");
            sb.append(StringEscapeUtil.escapeXml("=> "));
            sb.append(StringEscapeUtil.escapeXml(result));
        }

        if (ex != null) {
            sb.append("\n");
            sb.append(StringEscapeUtil.escapeXml("=> "));
            sb.append(CodeHighlighter.style(getExString(), "#808080"));
        }

        return sb.toString();
    }



    private final String name;
    private final String example;
    private final String exampleXmlStyled;
    private final String result;
    private final String stdout;
    private final String stderr;
    private final RuntimeException ex;
}
