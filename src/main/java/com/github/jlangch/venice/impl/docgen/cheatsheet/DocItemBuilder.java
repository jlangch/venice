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
package com.github.jlangch.venice.impl.docgen.cheatsheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.docgen.util.CodeHighlighter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.specialforms.SpecialFormsDoc;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.custom.VncProtocol;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class DocItemBuilder {

    public DocItemBuilder(
            final Env env,
            final CodeHighlighter codeHighlighter,
            final List<String> preloadedModules,
            final boolean runExamples
    ) {
        this.env = env;
        this.codeHighlighter = codeHighlighter;
        this.runExamples = runExamples;
        this.preloadedModules.addAll(preloadedModules);
    }


    public DocItem getDocItem(final String name) {
        return getDocItem(name, true, false);
    }

    public DocItem getDocItem(final String name, final boolean runExamples) {
        return getDocItem(name, runExamples, false);
    }

    public DocItem getDocItem(final String name, final boolean runExamples, final boolean catchEx) {
        final DocItem item = docItems.get(name);
        if (item != null) {
            return item;
        }
        else {
            final DocItem item_ = getDocItem_(name, runExamples, catchEx);
            if (item_ != null) {
                docItems.put(name, item_);
            }
            return item_;
        }
    }

    public String id() {
        return idgen.id();
    }

    public String id(final String name) {
        return idgen.id(name);
    }


    private DocItem getDocItem_(final String name, final boolean runExamples, final boolean catchEx) {
        final VncProtocol crossRefProtocol = findProtocol(name);
        if (crossRefProtocol != null) {
            final String fnDescr = crossRefProtocol.getDoc() == Constants.Nil
                            ? ""
                            : ((VncString)crossRefProtocol.getDoc()).getValue();

            final String descr = MARKDOWN_FN_DESCR ? null : fnDescr;

            final String descrXmlStyled = MARKDOWN_FN_DESCR
                                    ? Markdown.parse(fnDescr).renderToHtml()
                                    : null;

            return new DocItem(
                        name,
                        new ArrayList<>(),
                        descr,
                        descrXmlStyled,
                        runExamples(
                                name,
                                toStringList(crossRefProtocol.getExamples(), name, ":examples"),
                                runExamples,
                                catchEx),
                        createCrossRefs(name, crossRefProtocol.getSeeAlso()),
                        id(name));

        }
        else {
            final VncFunction fn = findFunction(name);
            if (fn != null) {
                final String fnDescr = fn.getDoc() == Constants.Nil
                                            ? ""
                                            : ((VncString)fn.getDoc()).getValue();

                final String descr = MARKDOWN_FN_DESCR ? null : fnDescr;

                final String descrXmlStyled = MARKDOWN_FN_DESCR
                                                ? Markdown.parse(fnDescr).renderToHtml()
                                                : null;

                return new DocItem(
                            name,
                            toStringList(fn.getArgLists(), name, ":arglists"),
                            descr,
                            descrXmlStyled,
                            runExamples(
                                    name,
                                    toStringList(fn.getExamples(), name, ":examples"),
                                    runExamples,
                                    catchEx),
                            createCrossRefs(name, fn.getSeeAlso()),
                            id(name));
            }
            else {
                final VncSpecialForm sf = findSpecialForm(name);
                if (sf != null) {
                    final String fnDescr = sf.getDoc() == Constants.Nil
                                                ? ""
                                                : ((VncString)sf.getDoc()).getValue();

                    final String descr = MARKDOWN_FN_DESCR ? null : fnDescr;

                    final String descrXmlStyled = MARKDOWN_FN_DESCR
                                                    ? Markdown.parse(fnDescr).renderToHtml()
                                                    : null;

                    return new DocItem(
                                name,
                                toStringList(sf.getArgLists(), name, ":arglists"),
                                descr,
                                descrXmlStyled,
                                runExamples(
                                        name,
                                        toStringList(sf.getExamples(), name, ":examples"),
                                        runExamples,
                                        catchEx),
                                createCrossRefs(name, sf.getSeeAlso()),
                                id(name));
                }
                else {
                    throw new RuntimeException(String.format("Unknown doc function %s", name));
                }
            }
        }
    }

    private List<ExampleOutput> runExamples(
            final String name,
            final List<String> examples,
            final boolean run,
            final boolean catchEx
    ) {
        if (runExamples) {
            final Venice runner = new Venice();

            final AtomicLong exampleNr = new AtomicLong(0);
            try {
                return examples
                            .stream()
                            .filter(e -> !StringUtil.isEmpty(e))
                            .map(e -> runExample(
                                        runner,
                                        exampleNr.incrementAndGet(),
                                        name,
                                        e,
                                        run,
                                        catchEx))
                            .collect(Collectors.toList());
            }
            catch(RuntimeException ex) {
                throw new RuntimeException(
                        String.format(
                                "Failed to run examples #%d (of %d) for %s",
                                exampleNr.get(), examples.size(), name),
                        ex);
            }
        }
        else {
            return EMPTY_EXAMPLES;
        }
    }

    private ExampleOutput runExample(
            final Venice runner,
            final long exampleNr,
            final String name,
            final String example,
            final boolean run,
            final boolean catchEx
    ) {
        final String exampleHighlighted = codeHighlighter.highlight(example);

        if (run) {
            final CapturingPrintStream ps_out = new CapturingPrintStream();
            final CapturingPrintStream ps_err = new CapturingPrintStream();

            try {
                final String modules = preloadedModules
                                            .stream()
                                            .map(m -> "  (load-module :" + m + ")")
                                            .collect(Collectors.joining("\n"));

                final String script = "(do \n" + modules + "\n\n  (pr-str " + example + "\n))";

                final String result = (String)runner.eval(
                                            "example",
                                            script,
                                            Parameters.of(
                                                "*out*", ps_out,
                                                "*err*", ps_err));

                return new ExampleOutput(
                        name, example, exampleHighlighted,
                        ps_out.getOutput(), ps_err.getOutput(), result);
            }
            catch(RuntimeException ex) {
                if (catchEx) {
                    return new ExampleOutput(
                            name, example, exampleHighlighted,
                            ps_out.getOutput(), ps_err.getOutput(), ex);
                }
                else {
                    throw new RuntimeException(
                            String.format("Failed to run example #%d for '%s'", exampleNr, name),
                            ex);
                }
            }
        }
        else {
            return new ExampleOutput(name, example, exampleHighlighted);
        }
    }

    private VncFunction findFunction(final String name) {
        // Special forms
        final VncFunction fn = (VncFunction)SpecialFormsDoc.ns.get(new VncSymbol(name));
        if (fn != null) {
            return fn;
        }

        // functions & macros
        final VncVal val = env.getOrNil(new VncSymbol(name));
        return Types.isVncFunction(val) ? (VncFunction)val : null;
    }

    private VncSpecialForm findSpecialForm(final String name) {
        final VncVal val = env.getOrNil(new VncSymbol(name));
        return Types.isVncSpecialForm(val) ? (VncSpecialForm)val : null;
    }

    private VncProtocol findProtocol(final String name) {
        final VncVal val = env.getOrNil(new VncSymbol(name));
        return val instanceof VncProtocol ? (VncProtocol)val : null;
    }

    private List<CrossRef> createCrossRefs(final String parentName, final VncList seeAlso) {
        final List<CrossRef> crossRefs = new ArrayList<>();

        seeAlso.forEach(v -> {
            final String crossRefName = ((VncString)v).getValue();

            final VncProtocol crossRefProtocol = findProtocol(crossRefName);
            if (crossRefProtocol != null) {
                String doc = crossRefProtocol.getDoc() == Constants.Nil
                                ? null
                                : ((VncString)crossRefProtocol.getDoc()).getValue();

                if (doc != null) {
                    crossRefs.add(
                        createCrossRef(crossRefName, getCrossRefDescr(doc)));
                }
            }
            else {
                final VncFunction crossRefFn = findFunction(crossRefName);
                if (crossRefFn != null) {
                    String doc = crossRefFn.getDoc() == Constants.Nil
                                    ? null
                                    : ((VncString)crossRefFn.getDoc()).getValue();

                    if (doc != null) {
                        crossRefs.add(
                            createCrossRef(crossRefName, getCrossRefDescr(doc)));
                    }
                }
                else {
                    final VncSpecialForm crossRefSf = findSpecialForm(crossRefName);
                    if (crossRefSf != null) {
                        String doc = crossRefSf.getDoc() == Constants.Nil
                                        ? null
                                        : ((VncString)crossRefSf.getDoc()).getValue();

                        if (doc != null) {
                            crossRefs.add(
                                createCrossRef(crossRefName, getCrossRefDescr(doc)));
                        }
                    }
                    else {
                        throw new RuntimeException(String.format(
                                "Missing cross reference function %s -> %s",
                                parentName,
                                crossRefName));
                    }
                }
            }
        });

        return crossRefs;
    }

    private String getCrossRefDescr(final String descr) {
        String crossRefDescr = Markdown.parse(descr).renderToText(CROSSREF_MAX_LEN * 2);

        int posLF = crossRefDescr.indexOf('\n');

        // the crossref description text is built from the first line only
        String s = (posLF == -1) ? crossRefDescr.trim() : crossRefDescr.substring(0, posLF).trim();

        // limit to at most CROSSREF_MAX_LEN chars
        if (s.length() > CROSSREF_MAX_LEN) {
            // do not cut in the middle of a word, cut at the first space in the last 15
            // characters of the description, if no space is found remove the last 5 chars
            // to get space for "..." marker
            final int spacePos = s.indexOf(' ', CROSSREF_MAX_LEN - 15);
            s = (spacePos != -1)
                  ? s.substring(0, spacePos)
                  : s.substring(0, CROSSREF_MAX_LEN - 5).trim();

            if (!s.endsWith(".")) {
                s = s + " ...";
            }
        }

        return s;
    }

    private CrossRef createCrossRef(final String name, final String descr) {
        return new CrossRef(name, id(name), descr);
    }

    private List<String> toStringList(final VncList list, final String name, final String helpType) {
        try {
            return list.stream()
                       .map(s -> ((VncString)s).getValue())
                       .collect(Collectors.toList());
        }
        catch(Exception ex) {
            throw new RuntimeException(String.format("Failed on item '%s' processing %s", name, helpType), ex);
        }
    }



    private static final boolean MARKDOWN_FN_DESCR = true;

    private static final List<ExampleOutput> EMPTY_EXAMPLES =
            Collections.unmodifiableList(new ArrayList<>());

    private static final int CROSSREF_MAX_LEN = 145;

    private final Env env;
    private final boolean runExamples;
    private final List<String> preloadedModules = new ArrayList<>();

    private final Map<String, DocItem> docItems = new HashMap<>();
    private final CodeHighlighter codeHighlighter;

    private final IdGen idgen = new IdGen();
}
