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

import static com.github.jlangch.venice.impl.VeniceClasspath.getVeniceBasePath;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.AppSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ArraySection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.BufferSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.CidrSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.CollectionsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ConcurrencySection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.CsvSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ExceptionsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.FunctionsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.IoFileSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.IoSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.IoZipSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.JavaInteropSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.JsonSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.LazySequencesSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.MacrosSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.MathSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ModuleKiraSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.NamespaceSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.PdfSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.PrimitivesSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ProtocolsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.RegexSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ReplSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.SpecialFormsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.SystemSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.SystemVarSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.TimeSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.TransducersSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.TypesSection;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.repl.ReplFunctions;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.impl.util.markdown.renderer.html.HtmlRenderer;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.lowagie.text.pdf.PdfReader;


public class DocGenerator {

    public DocGenerator(final boolean runExamples) {
        final List<String> preloadedModules = new ArrayList<>();

        preloadedModules
            .addAll(Arrays.asList(
                        "app",    "xml",      "crypt",     "gradle",
                        "trace",  "ansi",     "maven",     "kira",
                        "java",   "semver",   "excel",     "hexdump",
                        "shell",  "geoip",    "benchmark", "component",
                        "config", "parsifal", "grep"));

        final Env docEnv = new VeniceInterpreter(new AcceptAllInterceptor())
                            .createEnv(
                                preloadedModules,
                                false,
                                false,
                                RunMode.DOCGEN)
                            .setStdoutPrintStream(null)
                            .setStderrPrintStream(null);

        // make REPL specific functions available (e.g: 'repl/info')
        final Env env = ReplFunctions.register(docEnv, null, null);

        this.diBuilder = new DocItemBuilder(
                                env,
                                new DocHighlighter(DocColorTheme.getLightTheme()),
                                preloadedModules,
                                runExamples);
    }

    public static List<DocSection> docInfo() {
        return new DocGenerator(false).buildDocInfo();
    }

    public void run(final String version) {
        try {
            System.out.println("Creating cheatsheet V" + version);

            final List<DocSection> left = getLeftSections();
            final List<DocSection> right = getRightSections();
            final List<DocSection> leftModules = getModulesLeftSections();
            final List<DocSection> rightModules = getModulesRightSections();
            final List<MarkdownDoc> topics = getTopics();

            validateUniqueSectionsId(left, right);

            final Map<String,Object> data = new HashMap<>();
            data.put("meta-author", "Venice");
            data.put("version", version);
            data.put("toc", getTOC());
            data.put("left", left);
            data.put("right", right);
            data.put("left-modules", leftModules);
            data.put("right-modules", rightModules);
            data.put("details", getDocItems(concat(left, right, leftModules, rightModules)));
            data.put("snippets", new CodeSnippetReader().readSnippets());
            data.put("topics", topics);

            // [1] create a HTML
            data.put("pdfmode", false);
            final String html = CheatsheetRenderer.renderXHTML(data);
            save(new File(getUserDir(), "cheatsheet.html"), html);

            // [2] create a PDF
            data.put("pdfmode", true);
            final String xhtml = CheatsheetRenderer.renderXHTML(data);
            final ByteBuffer pdf = CheatsheetRenderer.renderPDF(xhtml);
            final byte[] pdfArr =  pdf.array();
            save(new File(getUserDir(), "cheatsheet.pdf"), pdfArr);

            final PdfReader reader = new PdfReader(pdf.array());
            final int pages = reader.getNumberOfPages();
            reader.close();

            System.out.println(String.format(
                    "Generated Cheat Sheet at: %s. XHTML: %dKB, PDF: %dKB / %d pages",
                    getUserDir(),
                    xhtml.length() / 1024,
                    pdfArr.length / 1024,
                    pages));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<DocSection> buildDocInfo() {
        final List<DocSection> sections = new ArrayList<>();
        sections.addAll(getLeftSections());
        sections.addAll(getRightSections());
        return sections;
    }

    private List<DocSection> getTOC() {
        final List<DocSection> content = new ArrayList<>();

        final DocSection primitives = new DocSection("Primitives", "primitives");
        primitives.addSection(new DocSection("Literals", "primitives.literals"));
        primitives.addSection(new DocSection("Numbers", "primitives.numbers"));
        primitives.addSection(new DocSection("Strings", "primitives.strings"));
        primitives.addSection(new DocSection("Chars", "primitives.chars"));
        primitives.addSection(new DocSection("Other", "primitives.other"));
        content.add(primitives);

        final DocSection collections = new DocSection("Collections", "collections");
        collections.addSection(new DocSection("List", "collections.lists"));
        collections.addSection(new DocSection("Vector", "collections.vectors"));
        collections.addSection(new DocSection("Set", "collections.sets"));
        collections.addSection(new DocSection("Map", "collections.maps"));
        collections.addSection(new DocSection("LazySeq", "lazyseq"));
        collections.addSection(new DocSection("Stack", "collections.stack"));
        collections.addSection(new DocSection("Queue", "collections.queue"));
        collections.addSection(new DocSection("DAG", "collections.dag"));
        collections.addSection(new DocSection("Array", "arrays"));
        collections.addSection(new DocSection("ByteBuf", "bytebuf"));
        content.add(collections);

        final DocSection datatypes = new DocSection("Custom\u00A0Types", "datatypes");
        datatypes.addSection(new DocSection("Types", "types"));
        datatypes.addSection(new DocSection("Protocols", "protocols"));
        content.add(datatypes);

        final DocSection functions = new DocSection("Core\u00A0Functions", "functions");
        functions.addSection(new DocSection("Functions", "functions"));
        functions.addSection(new DocSection("Macros", "macros"));
        functions.addSection(new DocSection("Special\u00A0Forms", "specialforms"));
        functions.addSection(new DocSection("Transducers", "transducers"));
        functions.addSection(new DocSection("Namespaces", "namespace"));
        functions.addSection(new DocSection("Exceptions", "exceptions"));
        content.add(functions);

        final DocSection concurrency = new DocSection("Concurrency", "concurrency");
        concurrency.addSection(new DocSection("Atoms", "concurrency.atoms"));
        concurrency.addSection(new DocSection("Futures", "concurrency.futures"));
        concurrency.addSection(new DocSection("Promises", "concurrency.promises"));
        concurrency.addSection(new DocSection("Delay", "concurrency.delay"));
        concurrency.addSection(new DocSection("Agents", "concurrency.agents"));
        concurrency.addSection(new DocSection("Scheduler", "concurrency.scheduler"));
        concurrency.addSection(new DocSection("Locking", "concurrency.locking"));
        concurrency.addSection(new DocSection("Volatiles", "concurrency.volatiles"));
        concurrency.addSection(new DocSection("Parallel", "concurrency.parallel"));
        content.add(concurrency);

        final DocSection threads = new DocSection("Threads", "concurrency.threads");
        threads.addSection(new DocSection("ThreadLocal", "concurrency.threadlocal"));
        threads.addSection(new DocSection("Threads", "concurrency.threads"));
        content.add(threads);

        final DocSection system = new DocSection("System\u00A0&\u00A0Java", "system");
        system.addSection(new DocSection("System", "system"));
        system.addSection(new DocSection("System\u00A0Vars", "sysvars"));
        system.addSection(new DocSection("Java\u00A0Interop", "javainterop"));
        system.addSection(new DocSection("REPL", "repl"));
        content.add(system);

        final DocSection util = new DocSection("Util", "util");
        util.addSection(new DocSection("Math", "math"));
        util.addSection(new DocSection("Time", "time"));
        util.addSection(new DocSection("Regex", "regex"));
        util.addSection(new DocSection("CIDR", "cidr"));

        content.add(util);

        final DocSection io = new DocSection("I/O", "io");
        io.addSection(new DocSection("I/O", "io.util"));
        io.addSection(new DocSection("File", "io.file"));
        io.addSection(new DocSection("Zip/GZip", "io.zip"));
        content.add(io);

        final DocSection documents = new DocSection("Documents", "miscellaneous");
        documents.addSection(new DocSection("JSON", "json"));
        documents.addSection(new DocSection("PDF", "pdf"));
        documents.addSection(new DocSection("PDF Tools", "pdf.pdftools"));
        documents.addSection(new DocSection("CSV", "csv"));
        documents.addSection(new DocSection("XML", "modules.xml"));
        documents.addSection(new DocSection("Excel", "modules.excel"));
        content.add(documents);

        final DocSection extmod = new DocSection("Modules", "modules");
        extmod.addSection(new DocSection("Kira\u00A0Templates", "modules.kira"));
        extmod.addSection(new DocSection("Parsifal", "modules.parsifal"));
        extmod.addSection(new DocSection("Configuration", "modules.config"));
        extmod.addSection(new DocSection("Component", "modules.component"));
        extmod.addSection(new DocSection("XML", "modules.xml"));
        extmod.addSection(new DocSection("Grep", "modules.grep"));
        extmod.addSection(new DocSection("Cryptography", "modules.cryptography"));
        extmod.addSection(new DocSection("Java", "modules.java"));
        extmod.addSection(new DocSection("Semver", "modules.semver"));
        extmod.addSection(new DocSection("Hexdump", "modules.hexdump"));
        extmod.addSection(new DocSection("Shell", "modules.shell"));
        extmod.addSection(new DocSection("Geo IP", "modules.geoip"));
        extmod.addSection(new DocSection("Ansi", "modules.ansi"));
        extmod.addSection(new DocSection("Gradle", "modules.gradle"));
        extmod.addSection(new DocSection("Maven", "modules.maven"));
        extmod.addSection(new DocSection("Tracing", "modules.tracing"));
        extmod.addSection(new DocSection("Benchmark", "modules.benchmark"));
        extmod.addSection(new DocSection("App", "modules.app"));
        content.add(extmod);

        final DocSection others = new DocSection("Others", "others");
        others.addSection(new DocSection("Embedding in Java", "embedding"));
        others.addSection(new DocSection("Venice Doc", "venicedoc"));
        others.addSection(new DocSection("Markdown", "markdown"));
        content.add(others);

        return content;
    }

    private List<MarkdownDoc> getTopics() {
        final List<MarkdownDoc> topics = new ArrayList<>();

        topics.add(new MarkdownDoc(
                        "VeniceDoc",
                        new HtmlRenderer().render(loadVeniceDocMarkdown()),
                        "venicedoc"));

        topics.add(new MarkdownDoc(
                        "Markdown",
                        new HtmlRenderer().render(loadMarkdownDoc()),
                        "markdown"));

        return topics;
    }

    private List<DocSection> getLeftSections() {
        return Arrays.asList(
                getPrimitivesSection(),
                getByteBufSection(),
                getRegexSection(),
                getMathSection(),
                getTransducersSection(),
                getFunctionsSection(),
                getMacrosSection(),
                getSpecialFormsSection(),
                getExceptionsSection(),
                getTypesSection(),
                getProtocolsSection(),
                getNamespaceSection(),
                getJavaInteropSection(),
                getReplSection(),
                getPdfSection(),
                getIOZipSection());
    }

    private List<DocSection> getRightSections() {
        return Arrays.asList(
                getCollectionsSection(),
                getLazySequencesSection(),
                getArraysSection(),
                getConcurrencySection(),
                getSystemSection(),
                getSystemVarSection(),
                getTimeSection(),
                getIOSection(),
                getIOFileSection(),
                getJsonSection(),
                getCidrSection(),
                getAppSection(),
                getCsvSection());
    }

    private List<DocSection> getModulesLeftSections() {
        return Arrays.asList(
                getModuleKiraSection(),
                getModuleCryptographySection(),
                getModuleXmlSection(),
                getModuleJavaSection(),
                getModuleParsifalSection(),
                getModuleGradleSection(),
                getModuleMavenSection(),
                getModuleTracingSection(),
                getModuleShellSection(),
                getModuleAnsiSection(),
                getModuleGrepSection());
    }

    private List<DocSection> getModulesRightSections() {
        return Arrays.asList(
                getModuleHexdumpSection(),
                getModuleSemverSection(),
                getModuleGeoipSection(),
                getModuleExcelSection(),
                getModuleConfigSection(),
                getModuleComponentSection(),
                getModuleAppSection(),
                getModuleBenchmarkSection());
    }

    private List<DocItem> getDocItems(final List<DocSection> sections) {
        return sections
                .stream()
                .map(s -> s.getSections())
                .flatMap(List::stream)
                .map(s -> s.getSections())
                .flatMap(List::stream)
                .map(s -> s.getItems())
                .flatMap(List::stream)
                .filter(i -> !StringUtil.isBlank(i.getName()))
                .distinct()
                .sorted(Comparator.comparing(DocItem::getName))
                .collect(Collectors.toList());
    }

    private DocSection getPrimitivesSection() {
        return new PrimitivesSection(diBuilder).section();
    }

    private DocSection getCollectionsSection() {
        return new CollectionsSection(diBuilder).section();
    }

    private DocSection getLazySequencesSection() {
        return new LazySequencesSection(diBuilder).section();
    }

    private DocSection getArraysSection() {
        return new ArraySection(diBuilder).section();
    }

    private DocSection getRegexSection() {
        return new RegexSection(diBuilder).section();
    }

    private DocSection getMathSection() {
        return new MathSection(diBuilder).section();
    }

    private DocSection getFunctionsSection() {
        return new FunctionsSection(diBuilder).section();
    }
    private DocSection getMacrosSection() {
        return new MacrosSection(diBuilder).section();
    }

    private DocSection getSpecialFormsSection() {
        return new SpecialFormsSection(diBuilder).section();
    }

    private DocSection getExceptionsSection() {
        return new ExceptionsSection(diBuilder).section();
    }

    private DocSection getSystemSection() {
        return new SystemSection(diBuilder).section();
    }

    private DocSection getTypesSection() {
        return new TypesSection(diBuilder).section();
    }

    private DocSection getProtocolsSection() {
        return new ProtocolsSection(diBuilder).section();
    }

    private DocSection getTransducersSection() {
        return new TransducersSection(diBuilder).section();
    }

    private DocSection getConcurrencySection() {
        return new ConcurrencySection(diBuilder).section();
    }

    private DocSection getIOSection() {
        return new IoSection(diBuilder).section();
    }

    private DocSection getIOFileSection() {
        return new IoFileSection(diBuilder).section();
    }

    private DocSection getIOZipSection() {
        return new IoZipSection(diBuilder).section();
    }

    private DocSection getAppSection() {
        return new AppSection(diBuilder).section();
    }

    private DocSection getNamespaceSection() {
        return new NamespaceSection(diBuilder).section();
    }

    private DocSection getByteBufSection() {
        return new BufferSection(diBuilder).section();
    }

    private DocSection getTimeSection() {
        return new TimeSection(diBuilder).section();
    }

    private DocSection getJavaInteropSection() {
        return new JavaInteropSection(diBuilder).section();
    }

    private DocSection getReplSection() {
        return new ReplSection(diBuilder).section();
    }

    private DocSection getPdfSection() {
        return new PdfSection(diBuilder).section();
    }

    private DocSection getSystemVarSection() {
        return new SystemVarSection(diBuilder).section();
    }

    private DocSection getJsonSection() {
        return new JsonSection(diBuilder).section();
    }

    private DocSection getCsvSection() {
        return new CsvSection(diBuilder).section();
    }

    private DocSection getCidrSection() {
        return new CidrSection(diBuilder).section();
    }

    private DocSection getModuleKiraSection() {
        return new ModuleKiraSection(diBuilder).section();
    }

    private DocSection getModuleTracingSection() {
        final DocSection section = new DocSection(
                                        "Tracing",
                                        "Tracing functions",
                                        "modules.tracing");

        final DocSection all = new DocSection("(load-module :trace)", id());
        section.addSection(all);

        final DocSection trace = new DocSection("Tracing", id());
        all.addSection(trace);
        trace.addItem(diBuilder.getDocItem("trace/trace"));
        trace.addItem(diBuilder.getDocItem("trace/trace-var"));
        trace.addItem(diBuilder.getDocItem("trace/untrace-var"));

        final DocSection test = new DocSection("Test", id());
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("trace/traced?"));
        test.addItem(diBuilder.getDocItem("trace/traceable?"));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("trace/trace-str-limit"));

        final DocSection tee = new DocSection("Tee", id());
        all.addSection(tee);
        tee.addItem(diBuilder.getDocItem("trace/tee->"));
        tee.addItem(diBuilder.getDocItem("trace/tee->>"));
        tee.addItem(diBuilder.getDocItem("trace/tee"));

        return section;
    }

    private DocSection getModuleShellSection() {
        final DocSection section = new DocSection(
                                        "Shell",
                                        "Functions to deal with the operating system",
                                        "modules.shell");

        final DocSection all = new DocSection("(load-module :shell)", id());
        section.addSection(all);

        final DocSection trace = new DocSection("Open", id());
        all.addSection(trace);
        trace.addItem(diBuilder.getDocItem("shell/open", false));
        trace.addItem(diBuilder.getDocItem("shell/open-macos-app", false));

        final DocSection test = new DocSection("Process", id());
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("shell/kill", false));
        test.addItem(diBuilder.getDocItem("shell/kill-forcibly", false));
        test.addItem(diBuilder.getDocItem("shell/wait-for-process-exit", false));
        test.addItem(diBuilder.getDocItem("shell/alive?", false));
        test.addItem(diBuilder.getDocItem("shell/pid", false));
        test.addItem(diBuilder.getDocItem("shell/process-handle", false));
        test.addItem(diBuilder.getDocItem("shell/process-handle?", false));
        test.addItem(diBuilder.getDocItem("shell/process-info", false));
        test.addItem(diBuilder.getDocItem("shell/processes", false));
        test.addItem(diBuilder.getDocItem("shell/processes-info", false));
        test.addItem(diBuilder.getDocItem("shell/descendant-processes", false));
        test.addItem(diBuilder.getDocItem("shell/parent-process", false));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("shell/diff", false));

        return section;
    }

    private DocSection getModuleXmlSection() {
        final DocSection section = new DocSection(
                                        "XML",
                                        "modules.xml");

        final DocSection all = new DocSection("(load-module :xml)", id());
        section.addSection(all);

        final DocSection xml = new DocSection("XML", id());
        all.addSection(xml);
        xml.addItem(diBuilder.getDocItem("xml/parse-str"));
        xml.addItem(diBuilder.getDocItem("xml/parse"));
        xml.addItem(diBuilder.getDocItem("xml/path->"));
        xml.addItem(diBuilder.getDocItem("xml/children"));
        xml.addItem(diBuilder.getDocItem("xml/text"));

        return section;
    }

    private DocSection getModuleCryptographySection() {
        final DocSection section = new DocSection(
                                        "Cryptography",
                                        "modules.cryptography");

        final DocSection all = new DocSection("(load-module :crypt)", id());
        section.addSection(all);

        final DocSection hashes = new DocSection("Hashes", id());
        all.addSection(hashes);
        hashes.addItem(diBuilder.getDocItem("crypt/md5-hash"));
        hashes.addItem(diBuilder.getDocItem("crypt/sha1-hash"));
        hashes.addItem(diBuilder.getDocItem("crypt/sha512-hash"));
        hashes.addItem(diBuilder.getDocItem("crypt/pbkdf2-hash"));

        final DocSection crypt = new DocSection("Encrypt", id());
        all.addSection(crypt);
        crypt.addItem(diBuilder.getDocItem("crypt/encrypt"));
        crypt.addItem(diBuilder.getDocItem("crypt/decrypt"));

        return section;
    }

    private DocSection getModuleGradleSection() {
        final DocSection section = new DocSection(
                                        "Gradle",
                                        "modules.gradle");

        final DocSection all = new DocSection("(load-module :gradle)", id());
        section.addSection(all);

        final DocSection gradle = new DocSection("Gradle", id());
        all.addSection(gradle);
        gradle.addItem(diBuilder.getDocItem("gradle/with-home", false));
        gradle.addItem(diBuilder.getDocItem("gradle/version", false));
        gradle.addItem(diBuilder.getDocItem("gradle/task", false));

        return section;
    }

    private DocSection getModuleMavenSection() {
        final DocSection section = new DocSection(
                                        "Maven",
                                        "modules.maven");

        final DocSection all = new DocSection("(load-module :maven)", id());
        section.addSection(all);

        final DocSection maven = new DocSection("Maven", id());
        all.addSection(maven);
        maven.addItem(diBuilder.getDocItem("maven/download", false));
        maven.addItem(diBuilder.getDocItem("maven/get", false));
        maven.addItem(diBuilder.getDocItem("maven/uri", false));
        maven.addItem(diBuilder.getDocItem("maven/parse-artefact", false));

        return section;
    }

    private DocSection getModuleJavaSection() {
        final DocSection section = new DocSection(
                                        "Java",
                                        "modules.java");

        final DocSection all = new DocSection("(load-module :java)", id());
        section.addSection(all);

        final DocSection java = new DocSection("Java", id());
        all.addSection(java);
        java.addItem(diBuilder.getDocItem("java/javadoc", false));

        return section;
    }

    private DocSection getModuleSemverSection() {
        final DocSection section = new DocSection(
                                        "Semver",
                                        "Semantic versioning",
                                        "modules.semver");

        final DocSection all = new DocSection("(load-module :semver)", id());
        section.addSection(all);

        final DocSection semver = new DocSection("Semver", id());
        all.addSection(semver);
        semver.addItem(diBuilder.getDocItem("semver/parse"));
        semver.addItem(diBuilder.getDocItem("semver/version"));

        final DocSection valid = new DocSection("Validation", id());
        all.addSection(valid);
        valid.addItem(diBuilder.getDocItem("semver/valid?"));
        valid.addItem(diBuilder.getDocItem("semver/valid-format?"));

        final DocSection test = new DocSection("Test", id());
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("semver/newer?"));
        test.addItem(diBuilder.getDocItem("semver/older?"));
        test.addItem(diBuilder.getDocItem("semver/equal?"));
        test.addItem(diBuilder.getDocItem("semver/cmp"));

        return section;
    }

    private DocSection getModuleGeoipSection() {
        final DocSection section = new DocSection(
                                        "Geo IP",
                                        "Geolocation mapping for IP adresses",
                                        "modules.geoip");

        final DocSection all = new DocSection("(load-module :geoip)", id());
        section.addSection(all);

        final DocSection geoip = new DocSection("Lookup", id());
        all.addSection(geoip);
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-country-resolver", false));
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-country-loc-resolver", false));
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-city-loc-resolver", false));
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-city-loc-resolver-mem-optimized", false));

        final DocSection db = new DocSection("Databases", id());
        all.addSection(db);
        db.addItem(diBuilder.getDocItem("geoip/download-google-country-db-to-csvfile", false));
        db.addItem(diBuilder.getDocItem("geoip/download-maxmind-db-to-zipfile", false));
        db.addItem(diBuilder.getDocItem("geoip/download-maxmind-db", false));

        final DocSection dbBuild = new DocSection("DB Parser", id());
        all.addSection(dbBuild);
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-country-ip-db", false));
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-city-ip-db", false));
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-country-db", false));
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-city-db", false));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("geoip/build-maxmind-country-db-url"));
        util.addItem(diBuilder.getDocItem("geoip/build-maxmind-city-db-url"));
        util.addItem(diBuilder.getDocItem("geoip/map-location-to-numerics"));
        util.addItem(diBuilder.getDocItem("geoip/country-to-location-resolver", false));

        return section;
    }

    private DocSection getModuleHexdumpSection() {
        final DocSection section = new DocSection(
                                        "Hexdump",
                                        "modules.hexdump");

        final DocSection all = new DocSection("(load-module :hexdump)", id());
        section.addSection(all);

        final DocSection hexdump = new DocSection("Hexdump", id());
        all.addSection(hexdump);
        hexdump.addItem(diBuilder.getDocItem("hexdump/dump", false));

        return section;
    }

    private DocSection getModuleAnsiSection() {
        final DocSection section = new DocSection(
                                        "Ansi",
                                        "ANSI codes, styles, and colorization helper functions",
                                        "modules.ansi");

        final DocSection all = new DocSection("(load-module :ansi)", id());
        section.addSection(all);

        final DocSection colors = new DocSection("Colors", id());
        all.addSection(colors);
        colors.addItem(diBuilder.getDocItem("ansi/fg-color", false));
        colors.addItem(diBuilder.getDocItem("ansi/bg-color", false));

        final DocSection style = new DocSection("Styles", id());
        all.addSection(style);
        style.addItem(diBuilder.getDocItem("ansi/style", false));
        style.addItem(diBuilder.getDocItem("ansi/ansi", false));
        style.addItem(diBuilder.getDocItem("ansi/with-ansi", false));
        style.addItem(diBuilder.getDocItem("ansi/without-ansi", false));

        final DocSection cursor = new DocSection("Cursor", id());
        all.addSection(cursor);
        cursor.addItem(diBuilder.getDocItem("ansi/without-cursor", false));

        final DocSection progress = new DocSection("Progress", id());
        all.addSection(progress);
        progress.addItem(diBuilder.getDocItem("ansi/progress", false));
        progress.addItem(diBuilder.getDocItem("ansi/progress-bar", false));

        return section;
    }

    private DocSection getModuleGrepSection() {
        final DocSection section = new DocSection(
                                        "Grep",
                                        "Grep like search tool",
                                        "modules.grep");

        final DocSection all = new DocSection("(load-module :grep)", id());
        section.addSection(all);

        final DocSection grep = new DocSection("Grep", id());
        all.addSection(grep);
        grep.addItem(diBuilder.getDocItem("grep/grep", false));
        grep.addItem(diBuilder.getDocItem("grep/grep-zip", false));

        return section;
    }

    private DocSection getModuleParsifalSection() {
        final DocSection section = new DocSection(
                                        "Parsifal",
                                        "A parser combinator",
                                        "modules.parsifal",
                                        "*Parsifal* is a port of Nate Young's Parsatron Clojure " +
                                        "[parser combinators](https://github.com/youngnh/parsatron) "+
                                        "project.",
                                        null);

        final DocSection all = new DocSection("(load-module :parsifal)", id());
        section.addSection(all);

        final DocSection run = new DocSection("Run", id());
        all.addSection(run);
        run.addItem(diBuilder.getDocItem("parsifal/run", false));

        final DocSection define = new DocSection("Define", id());
        all.addSection(define);
        define.addItem(diBuilder.getDocItem("parsifal/defparser", false));

        final DocSection parsers = new DocSection("Parsers", id());
        all.addSection(parsers);
        parsers.addItem(diBuilder.getDocItem("parsifal/any", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/many", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/many1", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/times", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/either", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/choice", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/between", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/>>", false));

        final DocSection special = new DocSection("Special Parsers", id());
        all.addSection(special);
        special.addItem(diBuilder.getDocItem("parsifal/eof", false));
        special.addItem(diBuilder.getDocItem("parsifal/never", false));
        special.addItem(diBuilder.getDocItem("parsifal/always", false));
        special.addItem(diBuilder.getDocItem("parsifal/lookahead", false));
        special.addItem(diBuilder.getDocItem("parsifal/attempt", false));

        final DocSection binding = new DocSection("Binding", id());
        all.addSection(binding);
        binding.addItem(diBuilder.getDocItem("parsifal/let->>", false));

        final DocSection ch = new DocSection("Char Parsers", id());
        all.addSection(ch);
        ch.addItem(diBuilder.getDocItem("parsifal/char", false));
        ch.addItem(diBuilder.getDocItem("parsifal/not-char", false));
        ch.addItem(diBuilder.getDocItem("parsifal/any-char", false));
        ch.addItem(diBuilder.getDocItem("parsifal/digit", false));
        ch.addItem(diBuilder.getDocItem("parsifal/hexdigit", false));
        ch.addItem(diBuilder.getDocItem("parsifal/letter", false));
        ch.addItem(diBuilder.getDocItem("parsifal/letter-or-digit", false));
        ch.addItem(diBuilder.getDocItem("parsifal/any-char-of", false));
        ch.addItem(diBuilder.getDocItem("parsifal/none-char-of", false));
        ch.addItem(diBuilder.getDocItem("parsifal/string", false));

        final DocSection tok = new DocSection("Token Parsers", id());
        all.addSection(tok);
        tok.addItem(diBuilder.getDocItem("parsifal/token", false));

        final DocSection proto = new DocSection("Protocols", id());
        all.addSection(proto);
        proto.addItem(diBuilder.getDocItem("parsifal/SourcePosition", false));

        final DocSection line = new DocSection("Line Info", id());
        all.addSection(line);
        line.addItem(diBuilder.getDocItem("parsifal/lineno", false));
        line.addItem(diBuilder.getDocItem("parsifal/pos", false));

        return section;
    }

    private DocSection getModuleBenchmarkSection() {
        final DocSection section = new DocSection(
                                        "Benchmark",
                                        "modules.benchmark");

        final DocSection all = new DocSection("(load-module :benchmark)", id());
        section.addSection(all);

        final DocSection colors = new DocSection("Utils", id());
        all.addSection(colors);
        colors.addItem(diBuilder.getDocItem("benchmark/benchmark", false));

        return section;
    }

    private DocSection getModuleConfigSection() {
        final DocSection section = new DocSection(
                                        "Configuration",
                                        "Manages configurations with system property & env var support",
                                        "modules.config");

        final DocSection all = new DocSection("(load-module :config)", id());
        section.addSection(all);

        final DocSection build = new DocSection("Build", id());
        all.addSection(build);
        build.addItem(diBuilder.getDocItem("config/build", false));

        final DocSection file = new DocSection("File", id());
        all.addSection(file);
        file.addItem(diBuilder.getDocItem("config/file", false));
        file.addItem(diBuilder.getDocItem("config/resource", true));

        final DocSection env = new DocSection("Env", id());
        all.addSection(env);
        env.addItem(diBuilder.getDocItem("config/env-var", true));
        env.addItem(diBuilder.getDocItem("config/env", false));

        final DocSection prop = new DocSection("Properties", id());
        all.addSection(prop);
        prop.addItem(diBuilder.getDocItem("config/property-var", true));
        prop.addItem(diBuilder.getDocItem("config/properties", false));

        return section;
    }

    private DocSection getModuleComponentSection() {
        final DocSection section = new DocSection(
                                    "Component",
                                    "Managing lifecycle and dependencies of components",
                                    "modules.component");

        final DocSection all = new DocSection("(load-module :component)", id());
        section.addSection(all);

        final DocSection system = new DocSection("Build", id());
        all.addSection(system);
        system.addItem(diBuilder.getDocItem("component/system-map", false));
        system.addItem(diBuilder.getDocItem("component/system-using"));

        final DocSection protocol = new DocSection("Protocol", id());
        all.addSection(protocol);
        protocol.addItem(diBuilder.getDocItem("component/Component", false));


        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("component/deps"));
        util.addItem(diBuilder.getDocItem("component/dep"));
        util.addItem(diBuilder.getDocItem("component/id"));

        return section;
    }

    private DocSection getModuleAppSection() {
        final DocSection section = new DocSection(
                                        "App",
                                        "Venice application archive",
                                        "modules.app");

        final DocSection all = new DocSection("(load-module :app)", id());
        section.addSection(all);

        final DocSection build = new DocSection("Build", id());
        all.addSection(build);
        build.addItem(diBuilder.getDocItem("app/build", false));

        final DocSection manifest = new DocSection("Manifest", id());
        all.addSection(manifest);
        manifest.addItem(diBuilder.getDocItem("app/manifest", false));

        return section;
    }

    private DocSection getModuleExcelSection() {
        final String footer = "Required 3rd party libraries:\n\n" +
                              "* org.apache.poi:poi:4.1.2\n" +
                              "* org.apache.poi:ooxml:4.1.2\n" +
                              "* org.apache.poi:ooxml-schemas:4.1.2\n" +
                              "* commons-codec:commons-codec:1.15\n" +
                              "* org.apache.commons:commons-collections:4.4.4\n" +
                              "* org.apache.commons:commons-compress:1.20\n" +
                              "* org.apache.commons:commons-math3:3.6.1\n" +
                              "* org.apache.xmlbeans:xmlbeans:3.1.0\n";

        final DocSection section = new DocSection("Excel", "Read/Write Excel files", "modules.excel", null, footer);

        final DocSection all = new DocSection("(load-module :excel)", id());
        section.addSection(all);

        final DocSection wr = new DocSection("Writer", id());
        all.addSection(wr);
        wr.addItem(diBuilder.getDocItem("excel/writer", false));
        wr.addItem(diBuilder.getDocItem("excel/add-sheet", false));
        wr.addItem(diBuilder.getDocItem("excel/add-font", false));
        wr.addItem(diBuilder.getDocItem("excel/add-style", false));
        wr.addItem(diBuilder.getDocItem("excel/add-column", false));

        final DocSection wr_data = new DocSection("Writer Data", id());
        all.addSection(wr_data);
        wr_data.addItem(diBuilder.getDocItem("excel/write-data", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-items", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-item", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-value", false));

        final DocSection wr_io = new DocSection("Writer I/O", id());
        all.addSection(wr_io);
        wr_io.addItem(diBuilder.getDocItem("excel/write->file", false));
        wr_io.addItem(diBuilder.getDocItem("excel/write->stream", false));
        wr_io.addItem(diBuilder.getDocItem("excel/write->bytebuf", false));

        final DocSection wr_util = new DocSection("Writer Util", id());
        all.addSection(wr_util);
        wr_util.addItem(diBuilder.getDocItem("excel/cell-formula", false));
        wr_util.addItem(diBuilder.getDocItem("excel/sum-formula", false));
        wr_util.addItem(diBuilder.getDocItem("excel/cell-address", false));
        wr_util.addItem(diBuilder.getDocItem("excel/auto-size-columns", false));
        wr_util.addItem(diBuilder.getDocItem("excel/auto-size-column", false));
        wr_util.addItem(diBuilder.getDocItem("excel/row-height", false));
        wr_util.addItem(diBuilder.getDocItem("excel/evaluate-formulas", false));
        wr_util.addItem(diBuilder.getDocItem("excel/convert->reader", false));

        final DocSection rd = new DocSection("Reader", id());
        all.addSection(rd);
        rd.addItem(diBuilder.getDocItem("excel/open", false));
        rd.addItem(diBuilder.getDocItem("excel/sheet", false));
        rd.addItem(diBuilder.getDocItem("excel/read-string-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-boolean-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-long-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-double-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-date-val", false));

        final DocSection rd_util = new DocSection("Reader Util", id());
        all.addSection(rd_util);
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-count", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-name", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-row-range", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-col-range", false));
        rd_util.addItem(diBuilder.getDocItem("excel/evaluate-formulas", false));
        rd_util.addItem(diBuilder.getDocItem("excel/cell-empty?", false));
        rd_util.addItem(diBuilder.getDocItem("excel/cell-type", false));

        return section;
    }

    private List<DocSection> concat(
            final List<DocSection> s1,
            final List<DocSection> s2,
            final List<DocSection> s3,
            final List<DocSection> s4
    ) {
        final List<DocSection> list = new ArrayList<>();
        list.addAll(s1);
        list.addAll(s2);
        list.addAll(s3);
        list.addAll(s4);
        return list;
    }

    private void save(final File file, final String text) throws Exception {
        save(file, text.getBytes("UTF-8"));
    }

    private void save(final File file, final byte[] data) throws Exception {
        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data, 0, data.length);
            fos.flush();
        }
    }

    private File getUserDir() {
        return new File(System.getProperty("user.dir"));
    }

    private Markdown loadVeniceDocMarkdown() {
        try {
            return Markdown.parse(
                        new ClassPathResource(getVeniceBasePath() + "docgen/venice-doc.md")
                            .getResourceAsString("UTF-8"));
        }
        catch(RuntimeException ex) {
            throw new RuntimeException("Failed to read 'venice-doc.md!", ex);
        }
    }

    private Markdown loadMarkdownDoc() {
        try {
            return Markdown.parse(
                        new ClassPathResource(getVeniceBasePath() + "docgen/markdown-doc.md")
                            .getResourceAsString("UTF-8"));
        }
        catch(RuntimeException ex) {
            throw new RuntimeException("Failed to read 'markdown-doc.md!", ex);
        }
    }

    private final void validateUniqueSectionsId(
            final List<DocSection> left,
            final List<DocSection> right
    ) {
        final Set<String> ids = new HashSet<>();

        left.forEach(s -> validateUniqueSectionId(s, ids));
        right.forEach(s -> validateUniqueSectionId(s, ids));
    }

    private final void validateUniqueSectionId(
            final DocSection section,
            final Set<String> ids
    ) {
        final String id = section.getId();
        if (id != null) {
            if (ids.contains(section.getId())) {
                throw new RuntimeException(
                        String.format(
                                "Non unique section id %s on section %s",
                                id, section.getTitle()));
            }
            ids.add(id);
        }

        // recursively validate children
        section.getSections().forEach(s -> validateUniqueSectionId(s, ids));
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
