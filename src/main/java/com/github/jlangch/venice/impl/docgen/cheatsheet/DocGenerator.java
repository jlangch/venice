/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleAnsiSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleAppSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleAsciiTableSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleAvironSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleBenchmarkSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleCargoArangoDBSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleCargoPostgresqlDBSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleCargoQdrantDBSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleCargoSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleChinookPostgreSQLSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleComponentSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleConfigSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleCryptographySection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleDockerSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleExcelSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleFontsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleGeoipSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleGradleSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleGradleWrapperSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleGrepSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleHexdumpSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleHttpClientJ8Section;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleImagesSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleInstallerSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleJTokkitSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleJavaSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleJdbcCoreSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleJdbcPostgreSQLSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleJsonlSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleKeystoresSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleKiraSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleMatrixSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleMavenSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleMimetypesSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleMultipartSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleOpenAiSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleParsifalSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleQrBillSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleQrCodeSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleQrRefSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleRingSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleSemverSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleShellSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleSseSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleStopWatchSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleTestSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleTimingSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleTomcatSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleTracingSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleXmlSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.modules.ModuleZipVaultSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ArraySection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ByteBufSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.CidrSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.CollectionsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ConcurrencySection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.CsvSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ExceptionsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.FunctionsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.InetSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.IoFileSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.IoFileWatchSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.IoSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.IoZipSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.JavaInteropSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.JsonSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.LazySequencesSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.LicenseSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.LoadPathSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.MBeanSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.MacrosSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.MathSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.NamespaceSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.PdfSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.PrimitivesSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ProtocolsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.RegexSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ReplSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.SandboxSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.ShellCoreSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.SpecialFormsSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.SystemSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.SystemVarSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.TapSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.TimeSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.TransducersSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.section.TypesSection;
import com.github.jlangch.venice.impl.docgen.util.CodeHighlighter;
import com.github.jlangch.venice.impl.docgen.util.ColorTheme;
import com.github.jlangch.venice.impl.docgen.util.MarkdownDoc;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.repl.ReplDirs;
import com.github.jlangch.venice.impl.repl.ReplFunctions;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.impl.util.markdown.renderer.html.HtmlRenderer;
import com.github.jlangch.venice.impl.util.markdown.renderer.text.TextRenderer;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.lowagie.text.pdf.PdfReader;


public class DocGenerator {

    public DocGenerator(final boolean runExamples) {
        final List<String> preloadedModules = new ArrayList<>();

        preloadedModules
            .addAll(Arrays.asList(
                        "app",           "xml",                 "crypt",            "gradle",
                        "trace",         "ansi",                "maven",            "kira",
                        "java",          "semver",              "excel",            "hexdump",
                        "shell",         "geoip",               "benchmark",        "component",
                        "config",        "parsifal",            "grep",             "test",
                        "fonts",         "jsonl",               "timing",           "stopwatch",
                        "zipvault",      "gradlew",             "matrix",
                        "docker",        "cargo",               "cargo-arangodb",   "cargo-qdrant",
                        "cargo-postgresql",
                        "installer",     "mimetypes",           "multipart",        "images",
                        "tomcat",        "jetty",               "http-client",       "http-client-j8",
                        "openai",        "jtokkit",             "keystores",
                        "jdbc-core",     "jdbc-postgresql",     "chinook-postgresql",
                        "ring",          "ring-multipart",      "ring-session",     "ring-mw",
                        "ring-util",     "server-side-events",  "pretty-print",
                        "qrcode",        "qrref",               "qrbill",
                        "ascii-canvas",  "ascii-charts",        "ascii-table",      "aviron"));

        final IVeniceInterpreter venice = new VeniceInterpreter(new AcceptAllInterceptor());

        final Env docEnv = venice.createEnv(
                                preloadedModules,
                                false,
                                false,
                                RunMode.DOCGEN,
                                IOStreamUtil.nullPrintStream(),
                                IOStreamUtil.nullPrintStream(),
                                null);

        // make REPL specific functions available (e.g: 'repl/info')
        final Env env = ReplFunctions.register(docEnv, null, null, null, false, ReplDirs.notavail());

        this.diBuilder = new DocItemBuilder(
                                env,
                                new CodeHighlighter(ColorTheme.getLightTheme()),
                                preloadedModules,
                                runExamples);
    }

    public static List<DocSection> docInfo() {
        return new DocGenerator(false).buildDocInfo();
    }

    public void run(final String version) {
        try {
            System.out.println("Creating cheatsheet V" + version);
            System.out.println("Collecting data...");

            final List<DocSection> left = getLeftSections();
            final List<DocSection> right = getRightSections();
            final List<DocSection> leftModules = getModulesLeftSections();
            final List<DocSection> rightModules = getModulesRightSections();
            final List<MarkdownDoc> topics = getMarkdownTopics();

            validateUniqueSectionsId(left, right);

            final List<DocSection> toc = getTOC();
            final List<DocItem> details = getDocItems(concat(left, right, leftModules, rightModules));
            final List<CodeSnippet> codeSnippets = new CodeSnippetReader().readSnippets();

            final Map<String,Object> data = new HashMap<>();
            data.put("meta-author", "Venice");
            data.put("version", version);
            data.put("toc", toc);
            data.put("left", left);
            data.put("right", right);
            data.put("left-modules", leftModules);
            data.put("right-modules", rightModules);
            data.put("details", details);
            data.put("snippets", codeSnippets);
            data.put("topics", topics);

            // [1] create a ASCII
            System.out.println("Creating cheatsheet (ascii)...");
            final String ascii = CheatsheetRenderer.renderASCII(data);
            save(new File(getUserDir(), "cheatsheet.ascii"), ascii);

            // [2] create a HTML
            System.out.println("Creating cheatsheet (html)...");
            data.put("pdfmode", false);
            final String html = CheatsheetRenderer.renderXHTML(data);
            save(new File(getUserDir(), "cheatsheet.html"), html);

            // [3] create a PDF
            System.out.println("Creating cheatsheet (pdf)...");
            data.put("pdfmode", true);
            final String xhtml = CheatsheetRenderer.renderXHTML(data);
            final ByteBuffer pdf = CheatsheetRenderer.renderPDF(xhtml);
            final byte[] pdfArr =  pdf.array();
            save(new File(getUserDir(), "cheatsheet.pdf"), pdfArr);

            // some PDF statistics
            final PdfReader reader = new PdfReader(pdfArr);
            final int pages = reader.getNumberOfPages();
            reader.close();

            System.out.println(String.format(
                    "Generated cheatsheet: %s",
                    getUserDir()));

            System.out.println(String.format(
                    "Generated cheatsheet: ASCII: %dKB, XHTML: %dKB, PDF: %dKB / %d pages",
                    ascii.length() / 1024,
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
        primitives.addSection(new DocSection("Number", "primitives.numbers"));
        primitives.addSection(new DocSection("String", "primitives.strings"));
        primitives.addSection(new DocSection("Char", "primitives.chars"));
        primitives.addSection(new DocSection("Boolean", "primitives.booleans"));
        primitives.addSection(new DocSection("Keyword", "primitives.keywords"));
        primitives.addSection(new DocSection("Symbol", "primitives.symbols"));
        primitives.addSection(new DocSection("Nil", "primitives.mil"));
        primitives.addSection(new DocSection("Just", "primitives.just"));
        content.add(primitives);

        final DocSection collections = new DocSection("Collections", "collections");
        collections.addSection(new DocSection("List", "collections.lists"));
        collections.addSection(new DocSection("Vector", "collections.vectors"));
        collections.addSection(new DocSection("Set", "collections.sets"));
        collections.addSection(new DocSection("Map", "collections.maps"));
        collections.addSection(new DocSection("LazySeq", "lazyseq"));
        collections.addSection(new DocSection("Stack", "collections.stack"));
        collections.addSection(new DocSection("Queue", "collections.queue"));
        collections.addSection(new DocSection("DelayQueue", "collections.delayqueue"));
        collections.addSection(new DocSection("DAG", "collections.dag"));
        collections.addSection(new DocSection("Array", "arrays"));
        collections.addSection(new DocSection("ByteBuf", "bytebuf"));
        content.add(collections);

        final DocSection datatypes = new DocSection("Custom\u00A0Types", "datatypes");
        datatypes.addSection(new DocSection("Types", "types"));
        datatypes.addSection(new DocSection("Protocols", "protocols"));
        content.add(datatypes);

        final DocSection concepts = new DocSection("Concepts", "concepts");
        concepts.addSection(new DocSection("Recursion", "concepts.recursion"));
        concepts.addSection(new DocSection("Destructuring", "concepts.destructuring"));
        content.add(concepts);

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
        concurrency.addSection(new DocSection("Locks", "concurrency.locks"));
        concurrency.addSection(new DocSection("Locking", "concurrency.locking"));
        concurrency.addSection(new DocSection("Futures", "concurrency.futures"));
        concurrency.addSection(new DocSection("Promises", "concurrency.promises"));
        concurrency.addSection(new DocSection("Delay", "concurrency.delay"));
        concurrency.addSection(new DocSection("Agents", "concurrency.agents"));
        concurrency.addSection(new DocSection("Scheduler", "concurrency.scheduler"));
        concurrency.addSection(new DocSection("Volatiles", "concurrency.volatiles"));
        concurrency.addSection(new DocSection("Parallel", "concurrency.parallel"));
        content.add(concurrency);

        final DocSection threads = new DocSection("Threads", "concurrency.threads");
        threads.addSection(new DocSection("ThreadLocal", "concurrency.threadlocal"));
        threads.addSection(new DocSection("Threads", "concurrency.threads"));
        content.add(threads);

        final DocSection system = new DocSection("System", "system");
        system.addSection(new DocSection("System", "system"));
        system.addSection(new DocSection("System\u00A0Vars", "sysvars"));
        system.addSection(new DocSection("REPL", "repl"));
        system.addSection(new DocSection("Sandbox", "sandbox"));
        system.addSection(new DocSection("Load\u00A0Paths", "loadpaths"));
        content.add(system);

        final DocSection shell = new DocSection("Shell", "shell");
        shell.addSection(new DocSection("Shell\u00A0System", "shell-system"));
        shell.addSection(new DocSection("Shell", "modules.shell"));
        shell.addSection(new DocSection("Shebang", "shebang"));
        content.add(shell);

        final DocSection java = new DocSection("Java", "java");
        java.addSection(new DocSection("Java\u00A0Interop", "javainterop"));
        java.addSection(new DocSection("Java", "modules.java"));
        java.addSection(new DocSection("MBeans", "mbean"));
        content.add(java);

        final DocSection util = new DocSection("Util", "util");
        util.addSection(new DocSection("Math", "math"));
        util.addSection(new DocSection("Time", "time"));
        util.addSection(new DocSection("StopWatch", "modules.stopwatch"));
        util.addSection(new DocSection("Regex", "regex"));
        util.addSection(new DocSection("INET", "inet"));
        util.addSection(new DocSection("CIDR", "cidr"));

        content.add(util);

        final DocSection io = new DocSection("I/O", "io");
        io.addSection(new DocSection("I/O", "io.util"));
        io.addSection(new DocSection("File", "io.file"));
        io.addSection(new DocSection("File Watcher", "io.file-watcher"));
        io.addSection(new DocSection("Zip/GZip", "io.zip"));
        content.add(io);

        final DocSection documents = new DocSection("Documents", "miscellaneous");
        documents.addSection(new DocSection("JSON", "json"));
        documents.addSection(new DocSection("JSON Lines", "modules.jsonl"));
        documents.addSection(new DocSection("PDF", "pdf"));
        documents.addSection(new DocSection("PDF Tools", "pdf.pdftools"));
        documents.addSection(new DocSection("CSV", "csv"));
        documents.addSection(new DocSection("XML", "modules.xml"));
        documents.addSection(new DocSection("Excel", "modules.excel"));
        documents.addSection(new DocSection("Images", "modules.images"));
        content.add(documents);

        final DocSection extmod = new DocSection("Modules", "modules");
        extmod.addSection(new DocSection("Kira\u00A0Templates", "modules.kira"));
        extmod.addSection(new DocSection("Parsifal", "modules.parsifal"));
        extmod.addSection(new DocSection("Grep", "modules.grep"));
        extmod.addSection(new DocSection("Configuration", "modules.config"));
        extmod.addSection(new DocSection("Component", "modules.component"));
        extmod.addSection(new DocSection("ZipVault", "modules.zipvault"));
        extmod.addSection(new DocSection("Fonts", "modules.fonts"));
        extmod.addSection(new DocSection("Cryptography", "modules.cryptography"));
        extmod.addSection(new DocSection("Keystores", "modules.keystores"));
        extmod.addSection(new DocSection("AsciiTable", "modules.asciitable"));
        extmod.addSection(new DocSection("Matrix", "modules.matrix"));
        extmod.addSection(new DocSection("Shell", "modules.shell"));
        extmod.addSection(new DocSection("Geo IP", "modules.geoip"));
        extmod.addSection(new DocSection("Mimetypes", "modules.mimetypes"));
        extmod.addSection(new DocSection("Ansi", "modules.ansi"));
        extmod.addSection(new DocSection("Aviron", "modules.aviron"));
        extmod.addSection(new DocSection("App", "modules.app"));
        extmod.addSection(new DocSection("QR\u00A0Ref", "modules.qrref"));
        extmod.addSection(new DocSection("QR\u00A0Bill", "modules.qrbill"));
        extmod.addSection(new DocSection("QR\u00A0Code", "modules.qrcode"));
        extmod.addSection(new DocSection("Semver", "modules.semver"));
        content.add(extmod);

        final DocSection build = new DocSection("Build\u00A0Tools", "build");
        build.addSection(new DocSection("Gradle\u00A0Wrapper", "modules.gradlew"));
        build.addSection(new DocSection("Gradle", "modules.gradle"));
        build.addSection(new DocSection("Maven", "modules.maven"));
        build.addSection(new DocSection("Installer", "modules.installer"));
        content.add(build);

        final DocSection debug = new DocSection("Test\u00A0&\u00A0Debug", "test");
        debug.addSection(new DocSection("Test", "modules.test"));
        debug.addSection(new DocSection("Tracing", "modules.tracing"));
        debug.addSection(new DocSection("Tap", "tap"));
        debug.addSection(new DocSection("Hexdump", "modules.hexdump"));
        debug.addSection(new DocSection("Timing", "modules.timing"));
        debug.addSection(new DocSection("Benchmark", "modules.benchmark"));
        content.add(debug);

        final DocSection db = new DocSection("Database", "database");
        db.addSection(new DocSection("JDBC\u00A0Core", "modules.jdbc-core"));
        db.addSection(new DocSection("JDBC\u00A0PostgreSQL", "modules.jdbc-postgresql"));
        db.addSection(new DocSection("Chinook\u00A0Dataset", "modules.chinook-postgresql"));
        content.add(db);

        final DocSection web = new DocSection("Web", "web");
        //web.addSection(new DocSection("Http\u00A0Client", "modules.http-client"));
        web.addSection(new DocSection("Http\u00A0Client\u00A0J8", "modules.http-client-j8"));
        web.addSection(new DocSection("Tomcat\u00A0WebApp\u00A0Server", "modules.tomcat"));
        web.addSection(new DocSection("Ring", "modules.ring"));
        web.addSection(new DocSection("Multipart", "modules.multipart"));
        web.addSection(new DocSection("SSE", "modules.sse"));
        content.add(web);

        final DocSection llm = new DocSection("LLM", "llm");
        llm.addSection(new DocSection("OpenAI", "modules.openai"));
        llm.addSection(new DocSection("JTokkit", "modules.jtokkit"));
        content.add(llm);

        final DocSection docker = new DocSection("Docker", "docker");
        docker.addSection(new DocSection("Docker", "modules.docker"));
        docker.addSection(new DocSection("Cargo", "modules.cargo"));
        docker.addSection(new DocSection("Cargo/ArangoDB", "modules.cargo-arangodb"));
        docker.addSection(new DocSection("Cargo/Qdrant", "modules.cargo-qdrant"));
        docker.addSection(new DocSection("Cargo/PostgreSQL", "modules.cargo-postgresql"));
        content.add(docker);

        final DocSection license = new DocSection("License", "license");
        license.addSection(new DocSection("License", "license"));
        content.add(license);

        final DocSection others = new DocSection("Others", "others");
        others.addSection(new DocSection("Embedding in Java", "embedding"));
        others.addSection(new DocSection("Venice Doc", "venicedoc"));
        others.addSection(new DocSection("Markdown", "markdown"));
        content.add(others);

        return content;
    }

    private List<MarkdownDoc> getMarkdownTopics() {
        final List<MarkdownDoc> topics = new ArrayList<>();

        topics.add(new MarkdownDoc(
                        "Recursion",
                        new TextRenderer().softWrap(120).render(loadVeniceDocMarkdown("recursion-doc.md")),
                        new HtmlRenderer().render(loadVeniceDocMarkdown("recursion-doc.md")),
                        "concepts.recursion"));

        topics.add(new MarkdownDoc(
                        "Destructuring",
                        new TextRenderer().softWrap(120).render(loadVeniceDocMarkdown("destructuring-doc.md")),
                        new HtmlRenderer().render(loadVeniceDocMarkdown("destructuring-doc.md")),
                        "concepts.destructuring"));

        topics.add(new MarkdownDoc(
                        "Shebang",
                        new TextRenderer().softWrap(120).render(loadVeniceDocMarkdown("shebang-doc.md")),
                        new HtmlRenderer().render(loadVeniceDocMarkdown("shebang-doc.md")),
                        "shebang"));

        topics.add(new MarkdownDoc(
                        "VeniceDoc",
                        new TextRenderer().softWrap(120).render(loadVeniceDocMarkdown("venice-doc.md")),
                        new HtmlRenderer().render(loadVeniceDocMarkdown("venice-doc.md")),
                        "venicedoc"));

        topics.add(new MarkdownDoc(
                        "Markdown",
                        new TextRenderer().softWrap(120).render(loadVeniceDocMarkdown("markdown-doc.md")),
                        new HtmlRenderer().render(loadVeniceDocMarkdown("markdown-doc.md")),
                        "markdown"));

        return topics;
    }

    private List<DocSection> getLeftSections() {
        return Arrays.asList(
                new PrimitivesSection(diBuilder).section(),
                new ByteBufSection(diBuilder).section(),
                new RegexSection(diBuilder).section(),
                new MathSection(diBuilder).section(),
                new TransducersSection(diBuilder).section(),
                new FunctionsSection(diBuilder).section(),
                new MacrosSection(diBuilder).section(),
                new SpecialFormsSection(diBuilder).section(),
                new ExceptionsSection(diBuilder).section(),
                new TypesSection(diBuilder).section(),
                new ProtocolsSection(diBuilder).section(),
                new NamespaceSection(diBuilder).section(),
                new JavaInteropSection(diBuilder).section(),
                new ReplSection(diBuilder).section(),
                new SandboxSection(diBuilder).section(),
                new LoadPathSection(diBuilder).section(),
                new PdfSection(diBuilder).section(),
                new IoZipSection(diBuilder).section(),
                new LicenseSection(diBuilder).section(),
                new MBeanSection(diBuilder).section());
    }

    private List<DocSection> getRightSections() {
        return Arrays.asList(
                new CollectionsSection(diBuilder).section(),
                new LazySequencesSection(diBuilder).section(),
                new ArraySection(diBuilder).section(),
                new ConcurrencySection(diBuilder).section(),
                new SystemSection(diBuilder).section(),
                new ShellCoreSection(diBuilder).section(),
                new SystemVarSection(diBuilder).section(),
                new TapSection(diBuilder).section(),
                new TimeSection(diBuilder).section(),
                new ModuleStopWatchSection(diBuilder).section(),
                new IoSection(diBuilder).section(),
                new IoFileSection(diBuilder).section(),
                new IoFileWatchSection(diBuilder).section(),
                new JsonSection(diBuilder).section(),
                new InetSection(diBuilder).section(),
                new CidrSection(diBuilder).section(),
                new CsvSection(diBuilder).section());
    }

    private List<DocSection> getModulesLeftSections() {
        return Arrays.asList(
                new ModuleKiraSection(diBuilder).section(),
                new ModuleCryptographySection(diBuilder).section(),
                new ModuleKeystoresSection(diBuilder).section(),
                new ModuleJsonlSection(diBuilder).section(),
                new ModuleZipVaultSection(diBuilder).section(),
                new ModuleXmlSection(diBuilder).section(),
                new ModuleJavaSection(diBuilder).section(),
                new ModuleParsifalSection(diBuilder).section(),
                new ModuleGradleWrapperSection(diBuilder).section(),
                new ModuleGradleSection(diBuilder).section(),
                new ModuleMavenSection(diBuilder).section(),
                new ModuleDockerSection(diBuilder).section(),
                new ModuleCargoSection(diBuilder).section(),
                new ModuleCargoArangoDBSection(diBuilder).section(),
                new ModuleCargoQdrantDBSection(diBuilder).section(),
                new ModuleCargoPostgresqlDBSection(diBuilder).section(),
                new ModuleTomcatSection(diBuilder).section(),
                new ModuleRingSection(diBuilder).section(),
                new ModuleTracingSection(diBuilder).section(),
                new ModuleShellSection(diBuilder).section(),
                new ModuleJdbcCoreSection(diBuilder).section(),
                new ModuleInstallerSection(diBuilder).section(),
                new ModuleJdbcPostgreSQLSection(diBuilder).section(),
                new ModuleChinookPostgreSQLSection(diBuilder).section(),
                new ModuleAvironSection(diBuilder).section()
                // new ModuleHttpClientSection(diBuilder).section(),
         );
    }

    private List<DocSection> getModulesRightSections() {
        return Arrays.asList(
                new ModuleHexdumpSection(diBuilder).section(),
                new ModuleSemverSection(diBuilder).section(),
                new ModuleGeoipSection(diBuilder).section(),
                new ModuleExcelSection(diBuilder).section(),
                new ModuleFontsSection(diBuilder).section(),
                new ModuleTestSection(diBuilder).section(),
                new ModuleConfigSection(diBuilder).section(),
                new ModuleComponentSection(diBuilder).section(),
                new ModuleAppSection(diBuilder).section(),
                new ModuleBenchmarkSection(diBuilder).section(),
                new ModuleTimingSection(diBuilder).section(),
                new ModuleGrepSection(diBuilder).section(),
                new ModuleQrRefSection(diBuilder).section(),
                new ModuleQrBillSection(diBuilder).section(),
                new ModuleQrCodeSection(diBuilder).section(),
                new ModuleAsciiTableSection(diBuilder).section(),
                new ModuleMatrixSection(diBuilder).section(),
                new ModuleAnsiSection(diBuilder).section(),
                new ModuleMimetypesSection(diBuilder).section(),
                new ModuleMultipartSection(diBuilder).section(),
                new ModuleSseSection(diBuilder).section(),
                new ModuleHttpClientJ8Section(diBuilder).section(),
                new ModuleOpenAiSection(diBuilder).section(),
                new ModuleJTokkitSection(diBuilder).section(),
                new ModuleImagesSection(diBuilder).section());
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

    private Markdown loadVeniceDocMarkdown(final String resource) {
        try {
            return Markdown.parse(
                        new ClassPathResource(Venice.class.getPackage(), "docgen/" + resource)
                            .getResourceAsString("UTF-8"));
        }
        catch(RuntimeException ex) {
            throw new RuntimeException("Failed to read '" + resource+ "'!", ex);
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


    private final DocItemBuilder diBuilder;
}
