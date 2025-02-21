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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.io.CharsetUtil;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class IOFunctionsStreams {


    ///////////////////////////////////////////////////////////////////////////
    // I/O functions
    ///////////////////////////////////////////////////////////////////////////
    public static VncFunction io_flush =
        new VncFunction(
                "io/flush",
                VncFunction
                    .meta()
                    .arglists("(io/flush s)")
                    .doc(
                        "Flushes a `:java.io.OutputStream` or a `:java.io.Writer`.")
                    .seeAlso("io/close")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncVal arg = args.first();

                if (Types.isVncJavaObject(arg, OutputStream.class)) {
                    final OutputStream os = Coerce.toVncJavaObject(args.first(), OutputStream.class);
                    try {
                        os.flush();
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to close: " + os.getClass().getName());
                    }
                }
                else if (Types.isVncJavaObject(arg, Writer.class)) {
                    final Writer wr = Coerce.toVncJavaObject(args.first(), Writer.class);
                    try {
                        wr.flush();
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to close: " + wr.getClass().getName());
                    }
                }
                else {
                    throw new VncException(
                            String.format(
                                "Function 'io/flush' does not allow %s as argument",
                                Types.getType(args.first())));
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_close =
        new VncFunction(
                "io/close",
                VncFunction
                    .meta()
                    .arglists("(io/close s)")
                    .doc(
                        "Closes a `:java.io.InputStream`, `:java.io.OutputStream`, " +
                        "`:java.io.Reader`, or a `:java.io.Writer`.\n\n" +
                        "Often it is more elegant to use try-with to let Venice implicitly " +
                        "close the stream when its leaves the scope:  \n\n" +
                        "```                                        \n" +
                        "(let [file (io/file \"foo.txt\")]          \n" +
                        "  (try-with [is (io/file-in-stream file)]  \n" +
                        "     (io/slurp-stream is :binary false)))  \n" +
                        "```")
                    .seeAlso("io/flush")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncVal arg = args.first();

                if (Types.isVncJavaObject(arg, InputStream.class)) {
                    final InputStream is = Coerce.toVncJavaObject(args.first(), InputStream.class);
                    try {
                        is.close();
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to close: " + is.getClass().getName());
                    }
                }
                else if (Types.isVncJavaObject(arg, OutputStream.class)) {
                    final OutputStream os = Coerce.toVncJavaObject(args.first(), OutputStream.class);
                    try {
                        os.close();
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to close: " + os.getClass().getName());
                    }
                }
                else if (Types.isVncJavaObject(arg, Reader.class)) {
                    final Reader rd = Coerce.toVncJavaObject(args.first(), Reader.class);
                    try {
                        rd.close();
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to close: " + rd.getClass().getName());
                    }
                }
                else if (Types.isVncJavaObject(arg, Writer.class)) {
                    final Writer wr = Coerce.toVncJavaObject(args.first(), Writer.class);
                    try {
                        wr.close();
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to close: " + wr.getClass().getName());
                    }
                }
                else {
                    throw new VncException(
                            String.format(
                                "Function 'io/close' does not allow %s as argument",
                                Types.getType(args.first())));
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_copy_stream =
        new VncFunction(
                "io/copy-stream",
                VncFunction
                    .meta()
                    .arglists("(io/copy-stream in-stream out-stream)")
                    .doc(
                        "Copies the input stream to the output stream. Returns `nil` on sucess or " +
                        "throws a VncException on failure. " +
                        "Input and output must be a `java.io.InputStream` and `java.io.OutputStream`.")
                    .seeAlso("io/copy-file")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                sandboxFunctionCallValidation();

                final InputStream is = Coerce.toVncJavaObject(args.first(), InputStream.class);
                final OutputStream os = Coerce.toVncJavaObject(args.second(), OutputStream.class);

                try {
                    IOStreamUtil.copy(is, os);
                }
                catch(Exception ex) {
                    throw new VncException(
                            "Failed to copy data from a :java.io.InputStream to a :java.io.OutputStream");
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_in_stream =
        new VncFunction(
                "io/file-in-stream",
                VncFunction
                    .meta()
                    .arglists("(io/file-in-stream f)")
                    .doc(
                        "Returns a `java.io.InputStream` for the file f.           \n\n" +
                        "f may be a:                                               \n\n" +
                        " * string file path, e.g: \"/temp/foo.json\"              \n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")`    \n\n" +
                        "`io/file-in-stream` supports load paths. See the `loadpath/paths` " +
                        "doc for a description of the *load path* feature.\n\n" +
                        "Note: The caller is responsible for closing the stream!")
                    .seeAlso(
                        "io/slurp", "io/slurp-stream",
                        "io/string-in-stream", "io/bytebuf-in-stream",
                        "loadpath/paths")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final File file = convertToFile(args.first());
                if (file != null) {
                    final IInterceptor interceptor = sandboxFunctionCallValidation();

                    try {
                        final InputStream is = interceptor
                                                .getLoadPaths()
                                                .getInputStream(file);
                        if (is == null) {
                            if (file.exists()) {
                                throw new VncException(
                                        "Failed to slurp data from the file " + file.getPath() +
                                        ". The file does not exists!");
                            }
                            else {
                                throw new SecurityException(
                                        "Failed to slurp data from the file " + file.getPath() +
                                        ". The load paths configuration prevented this action!");
                            }
                        }

                        return new VncJavaObject(is);
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to create a a `java.io.InputStream` from the file " + file.getPath(), ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/file-in-stream' does not allow %s as f",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_out_stream =
        new VncFunction(
                "io/file-out-stream",
                VncFunction
                    .meta()
                    .arglists("(io/file-out-stream f options)")
                    .doc(
                        "Returns a `java.io.OutputStream` for the file f. \n\n" +
                        "f may be a:  \n\n" +
                        " * string file path, e.g: \"/temp/foo.json\" \n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")` \n\n" +
                        "Options: \n\n" +
                        "| :append true/false | e.g.: `:append true`, defaults to false |\n" +
                        "| :encoding enc      | e.g.: `:encoding :utf-8`, defaults to :utf-8 |\n\n" +
                        "`io/file-out-stream` supports load paths. See the `loadpath/paths` " +
                        "doc for a description of the *load path* feature.\n\n" +
                       "Note: The caller is responsible for closing the stream!")
                    .seeAlso(
                        "io/slurp", "io/slurp-stream",
                        "io/string-in-stream", "io/bytebuf-in-stream",
                        "loadpath/paths")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final VncHashMap options = VncHashMap.ofAll(args.slice(1));
                final VncVal append = options.get(new VncKeyword("append"));

                final File file = convertToFile(args.first());
                if (file != null) {
                    final IInterceptor interceptor = sandboxFunctionCallValidation();

                    try {
                         final OutputStream outStream =  interceptor
                                                             .getLoadPaths()
                                                             .getOutputStream(
                                                                file,
                                                                StandardOpenOption.CREATE,
                                                                StandardOpenOption.WRITE,
                                                                VncBoolean.isTrue(append)
                                                                    ? StandardOpenOption.APPEND
                                                                    : StandardOpenOption.TRUNCATE_EXISTING);

                        if (outStream != null) {
                            return new VncJavaObject(outStream);
                        }
                        else {
                            throw new SecurityException(
                                    String.format(
                                            "Failed to spit data to the file %s. " +
                                            "The load paths configuration prevented this action!",
                                            file.getPath()));
                        }
                     }
                    catch (Exception ex) {
                        throw new VncException(
                                String.format(
                                        "Failed to create a `java.io.OutputStream` for the file '%s'",
                                        file.getPath()),
                                ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/file-out-stream' does not allow %s as f",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_string_in_stream =
        new VncFunction(
                "io/string-in-stream",
                VncFunction
                    .meta()
                    .arglists("(io/string-in-stream s & options)")
                    .doc(
                        "Returns a `java.io.InputStream` for the string s.                     \n\n" +
                        "Options:                                                              \n\n" +
                        "| :encoding enc      | e.g.: `:encoding :utf-8`, defaults to :utf-8 | \n\n" +
                        "Note: The caller is responsible for closing the stream!")
                    .examples(
                        "(let [text \"The quick brown fox jumped over the lazy dog\"]  \n" +
                        "  (try-with [is (io/string-in-stream text)]                   \n" +
                        "    ; do something with is                                    \n" +
                        "  ))")
                    .seeAlso(
                        "io/slurp-stream", "io/file-in-stream", "io/bytebuf-in-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncString s = Coerce.toVncString(args.first());

                final VncHashMap options = VncHashMap.ofAll(args.rest());

                final VncVal encVal = options.get(new VncKeyword("encoding"));
                final Charset charset = CharsetUtil.charset(encVal);

                try {
                    return new VncJavaObject(new ByteArrayInputStream(s.getValue().getBytes(charset)));
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to create a :java.io.InputStream from a string",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_bytebuf_in_stream =
        new VncFunction(
                "io/bytebuf-in-stream",
                VncFunction
                    .meta()
                    .arglists("(io/bytebuf-in-stream buf)")
                    .doc(
                        "Returns a `java.io.InputStream` from a bytebuf.\n\n" +
                        "Note: The caller is responsible for closing the stream!")
                    .examples(
                        "(try-with [is (io/bytebuf-in-stream (bytebuf [97 98 99]))] \n"+
                        "    ; do something with is                                 \n" +
                        "  )")
                    .seeAlso(
                        "io/slurp-stream", "io/file-in-stream", "io/string-in-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();

                try {
                    return new VncJavaObject(new ByteArrayInputStream(buf.array()));
                }
                catch(Exception ex) {
                    throw new VncException(String.format(
                            "Failed to create a :java.io.InputStream from a bytebuf"));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_bytebuf_out_stream =
        new VncFunction(
                "io/bytebuf-out-stream",
                VncFunction
                    .meta()
                    .arglists("(io/bytebuf-out-stream)")
                    .doc(
                        "Returns a new `java.io.ByteArrayOutputStream`.\n\n" +
                        "Dereferencing a :ByteArrayOutputStream returns the " +
                        "captured bytebuf.\n\n" +
                        "Note: The caller is responsible for closing the stream!")
                    .examples(
                        "(try-with [os (io/bytebuf-out-stream)]                  \n" +
                        "   (io/spit-stream os (bytebuf [97 98 99]) :flush true) \n" +
                        "   (str/format-bytebuf @os \", \" :prefix0x))           ")
                    .seeAlso(
                        "io/slurp-stream", "io/file-in-stream", "io/string-in-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                try {
                    return new VncJavaObject(new ByteArrayOutputStream());
                }
                catch(Exception ex) {
                    throw new VncException(String.format(
                            "Failed to create a :java.io.InputStream from a bytebuf"));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_uri_stream =
        new VncFunction(
                "io/uri-stream",
                VncFunction
                    .meta()
                    .arglists("(io/uri-stream uri)")
                    .doc(
                        "Returns a `java.io.InputStream` from the uri.\n\n" +
                        "Note: The caller is responsible for closing the stream!")
                    .examples(
                        "(let [url \"https://www.w3schools.com/xml/books.xm\"]     \n" +
                        "  (try-with [is (io/uri-stream url)]                      \n" +
                        "    (io/slurp-stream is :binary false :encoding :utf-8))) ")
                    .seeAlso(
                        "io/slurp-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncVal arg = args.first();

                final ILoadPaths loadpaths = ThreadContext.getInterceptor().getLoadPaths();

                URL url = null;
                try {
                    if (Types.isVncString(arg)) {
                        url = new URL(Coerce.toVncString(arg).getValue());
                    }
                    else if (Types.isVncJavaObject(arg, URL.class)) {
                        url = Coerce.toVncJavaObject(arg, URL.class);
                    }
                    else if (Types.isVncJavaObject(arg, URI.class)) {
                        url = Coerce.toVncJavaObject(arg, URI.class).toURL();
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'io/uri-stream' does not allow %s as argument",
                                Types.getType(arg)));
                    }
                }
                catch (VncException ex) {
                    throw ex;
                }
                catch(MalformedURLException ex) {
                    throw new VncException(
                            "Function 'io/uri-stream' got a malformed uri as argument", ex);
                }

                if (!loadpaths.isUnlimitedAccess()) {
                    final String protocol = url.getProtocol();
                    if (!("http".equals(protocol) || "https".equals(protocol))) {
                        throw new VncException(String.format(
                                "io/uri-stream supports only the protocols 'http' and 'https' " +
                                "if load paths 'limited access' is configured! " +
                                "Rejected protocol '%s'.", protocol));
                    }
                }

                try {
                    return new VncJavaObject(url.openStream());
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to create a :java.io.InputStream from an URI",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_wrap_os_with_buffered_writer =
        new VncFunction(
                "io/wrap-os-with-buffered-writer",
                VncFunction
                    .meta()
                    .arglists("(io/wrap-os-with-buffered-writer os encoding?)")
                    .doc(
                        "Wraps a `java.io.OutputStream` os with a `java.io.BufferedWriter` using an optional " +
                        "encoding (defaults to :utf-8).\n\n" +
                        "Note: The caller is responsible for closing the writer!")
                    .examples(
                        "(try-with [os (io/bytebuf-out-stream)                        \n" +
                        "           wr (io/wrap-os-with-buffered-writer os :utf-8)]   \n" +
                        "  (println wr \"100\")                                       \n" +
                        "  (println wr \"200\")                                       \n" +
                        "  (flush wr)                                                 \n" +
                        "  (bytebuf-to-string @os :utf-8))                            ")
                    .seeAlso(
                        "io/wrap-os-with-print-writer")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                try {
                    final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
                    final Charset charset = CharsetUtil.charset(args.second());

                    return new VncJavaObject(
                                new BufferedWriter(
                                        new OutputStreamWriter(os, charset)));
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to wrap a :java.io.OutputStream with a :java.io.BufferedWriter",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_wrap_os_with_print_writer =
        new VncFunction(
                "io/wrap-os-with-print-writer",
                VncFunction
                    .meta()
                    .arglists("(io/wrap-os-with-print-writer os encoding?)")
                    .doc(
                        "Wraps an `java.io.OutputStream` os with a `java.io.PrintWriter` using an optional " +
                        "encoding (defaults to :utf-8).\n\n"  +
                        "Note: The caller is responsible for closing the writer!")
                    .examples(
                        "(let [os (io/bytebuf-out-stream)]                            \n" +
                        "  (try-with [pr (io/wrap-os-with-print-writer os :utf-8)]    \n" +
                        "    (println pr \"line 1\")                                  \n" +
                        "    (println pr \"line 2\")                                  \n" +
                        "    (flush pr)                                               \n" +
                        "    @os))                                                    ")
                    .seeAlso(
                        "io/wrap-os-with-buffered-writer")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                try {
                    final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
                    final Charset charset = CharsetUtil.charset(args.second());

                    return new VncJavaObject(
                                new PrintWriter(
                                        new OutputStreamWriter(os, charset)));
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to wrap a :java.io.OutputStream with a :java.io.PrintWriter",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_wrap_is_with_buffered_reader =
        new VncFunction(
                "io/wrap-is-with-buffered-reader",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/wrap-is-with-buffered-reader is encoding?)")
                    .doc(
                        "Wraps an `java.io.InputStream` is with a `java.io.BufferedReader` using an optional " +
                        "encoding (defaults to :utf-8).\n\n" +
                        "Note: The caller is responsible for closing the reader!")
                    .examples(
                        "(let [data (bytebuf [108 105 110 101 32 49 10 108 105 110 101 32 50])]    \n" +
                        "  (try-with [is   (io/bytebuf-in-stream data)                             \n" +
                        "             rd (io/wrap-is-with-buffered-reader is :utf-8)]              \n" +
                        "    (println (read-line rd))                                              \n" +
                        "    (println (read-line rd))))                                            ")
                    .seeAlso(
                        "io/buffered-reader")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                 if (Types.isVncJavaObject(args.first())) {
                    final Object delegate = ((VncJavaObject)args.first()).getDelegate();
                    if (delegate instanceof InputStream) {
                        try {
                            final InputStream is = (InputStream)delegate;
                            final Charset charset = CharsetUtil.charset(args.second());

                            return new VncJavaObject(
                                        new BufferedReader(
                                                new InputStreamReader(is, charset)));
                        }
                        catch (Exception ex) {
                            throw new VncException(
                                    "Failed to wrap a :java.io.InputStream with a :java.io.BufferReader",
                                    ex);
                        }
                    }
                }

                throw new VncException(String.format(
                        "Function 'io/wrap-is-with-buffered-reader' requires an InputStream " +
                        "or a Reader. %s as is not allowed!",
                        Types.getType(args.first())));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_wrap_is_with_gzip_input_stream =
        new VncFunction(
                "io/wrap-is-with-gzip-input-stream",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/wrap-is-with-gzip-input-stream is)")
                    .doc(
                        "Wraps a `:java.io.InputStream` is with a `:java.io.GZIPInputStream` " +
                        "to read compressed data in the GZIP format.\n\n" +
                        "Note: The caller is responsible for closing the reader!")
                    .examples(
                        "(let [text      \"hello, hello, hello\"                        \n" +
                        "      gzip-buf  (-> (bytebuf-from-string text :utf-8)          \n" +
                        "                    (io/gzip))]                                \n" +
                        "  (try-with [is (-> (io/bytebuf-in-stream gzip-buf)            \n" +
                        "                    (io/wrap-is-with-gzip-input-stream))]      \n" +
                        "    (-> (io/slurp is :binary true)                             \n" +
                        "        (bytebuf-to-string :utf-8))))                          ")
                    .seeAlso(
                        "io/wrap-os-with-gzip-output-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object delegate = ((VncJavaObject)args.first()).getDelegate();
                if (delegate instanceof InputStream) {
                    try {
                         return new VncJavaObject(new GZIPInputStream((InputStream)delegate));
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to wrap a :java.io.InputStream with a :java.util.zip.GZIPInputStream",
                                ex);
                    }
                }

                throw new VncException(String.format(
                        "Function 'io/wrap-is-with-gzip-input-stream' requires an InputStream." +
                        "%s as is not allowed!",
                        Types.getType(args.first())));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_wrap_os_with_gzip_output_stream =
        new VncFunction(
                "io/wrap-os-with-gzip-output-stream",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/wrap-os-with-gzip-output-stream is)")
                    .doc(
                        "Wraps a `:java.io.OutputStream` is with a `:java.io.GZIPOutputStream` " +
                        "to write compressed data in the GZIP format.\n\n" +
                        "Note: The caller is responsible for closing the reader!")
                    .examples(
                        "(let [text  \"hello, hello, hello\"                         \n" +
                        "      bos   (io/bytebuf-out-stream)]                        \n" +
                        "  (try-with [gos (io/wrap-os-with-gzip-output-stream bos)]  \n" +
                        "    (io/spit gos text :encoding :utf-8)                     \n" +
                        "    (io/flush gos)                                          \n" +
                        "    (io/close gos)                                          \n" +
                        "    (-> (io/ungzip @bos)                                    \n" +
                        "        (bytebuf-to-string :utf-8))))                       ")
                    .seeAlso(
                        "io/wrap-is-with-gzip-input-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object delegate = ((VncJavaObject)args.first()).getDelegate();
                if (delegate instanceof OutputStream) {
                    try {
                         return new VncJavaObject(new GZIPOutputStream((OutputStream)delegate));
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to wrap a :java.io.OutputStream with a :java.util.zip.GZIPOutputStream",
                                ex);
                    }
                }

                throw new VncException(String.format(
                        "Function 'io/wrap-os-with-gzip-output-stream' requires an OutputStream." +
                        "%s as is not allowed!",
                        Types.getType(args.first())));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_wrap_is_with_inflater_input_stream =
        new VncFunction(
                "io/wrap-is-with-inflater-input-stream",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/wrap-is-with-inflater-input-stream is)")
                    .doc(
                        "Wraps a `:java.io.InputStream` is with a `:java.io.InflaterInputStream` " +
                        "to read compressed data in the 'zlib' format.\n\n" +
                        "Note: The caller is responsible for closing the reader!")
                    .examples(
                        "(let [text      \"hello, hello, hello\"                        \n" +
                        "      zlib-buf  (-> (bytebuf-from-string text :utf-8)          \n" +
                        "                    (io/deflate))]                             \n" +
                        "  (try-with [is (-> (io/bytebuf-in-stream zlib-buf)            \n" +
                        "                    (io/wrap-is-with-inflater-input-stream))]  \n" +
                        "    (-> (io/slurp is :binary true)                             \n" +
                        "        (bytebuf-to-string :utf-8))))                          ")
                    .seeAlso(
                        "io/wrap-os-with-deflater-output-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object delegate = ((VncJavaObject)args.first()).getDelegate();
                if (delegate instanceof InputStream) {
                    try {
                         return new VncJavaObject(new InflaterInputStream((InputStream)delegate));
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to wrap a :java.io.InputStream with a :java.util.zip.InflaterInputStream",
                                ex);
                    }
                }

                throw new VncException(String.format(
                        "Function 'io/wrap-is-with-inflater-input-stream' requires an InputStream." +
                        "%s as is not allowed!",
                        Types.getType(args.first())));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_wrap_os_with_deflater_output_stream =
        new VncFunction(
                "io/wrap-os-with-deflater-output-stream",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/wrap-os-with-deflater-output-stream is)")
                    .doc(
                        "Wraps a `:java.io.OutputStream` is with a `:java.io.DeflaterOutputStream` " +
                        "to write compressed data in the 'zlib' format.\n\n" +
                        "Note: The caller is responsible for closing the reader!")
                    .examples(
                        "(let [text  \"hello, hello, hello\"                             \n" +
                        "      bos   (io/bytebuf-out-stream)]                            \n" +
                        "  (try-with [gos (io/wrap-os-with-deflater-output-stream bos)]  \n" +
                        "    (io/spit gos text :encoding :utf-8)                         \n" +
                        "    (io/flush gos)                                              \n" +
                        "    (io/close gos)                                              \n" +
                        "    (-> (io/inflate @bos)                                       \n" +
                        "        (bytebuf-to-string :utf-8))))                           ")
                    .seeAlso(
                        "io/wrap-is-with-inflater-input-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object delegate = ((VncJavaObject)args.first()).getDelegate();
                if (delegate instanceof OutputStream) {
                    try {
                         return new VncJavaObject(new DeflaterOutputStream((OutputStream)delegate));
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to wrap a :java.io.OutputStream with a :java.util.zip.DeflaterOutputStream",
                                ex);
                    }
                }

                throw new VncException(String.format(
                        "Function 'io/wrap-os-with-deflater-output-stream' requires an OutputStream." +
                        "%s as is not allowed!",
                        Types.getType(args.first())));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_buffered_writer =
        new VncFunction(
                "io/buffered-writer",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/buffered-writer f & options)" )
                    .doc(
                        "Creates a `java.io.Writer` for f.\n\n" +
                        "f may be a:                                                       \n\n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")`            \n" +
                        " * `java.nio.file.Path`                                           \n" +
                        " * `java.io.OutputStream`                                         \n" +
                        " * `java.io.Writer`                                               \n" +
                    "Options: \n\n" +
                        "| :append true/false | e.g.: `:append true`, defaults to false |\n" +
                        "| :encoding enc      | e.g.: `:encoding :utf-8`, defaults to :utf-8 |\n\n" +
                        "`io/buffered-writer` supports load paths. See the `loadpath/paths` " +
                        "doc for a description of the *load path* feature.")
                    .seeAlso(
                        "println", "io/string-writer", "io/buffered-reader")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final ILoadPaths loadpaths = ThreadContext.getInterceptor().getLoadPaths();

                final VncHashMap options = VncHashMap.ofAll(args.slice(1));
                final VncVal append = options.get(new VncKeyword("append"));
                final VncVal encVal = options.get(new VncKeyword("encoding"));
                final Charset charset = CharsetUtil.charset(encVal);

                final File file = convertToFile(args.first());
                if (file != null) {
                    try {
                        final OutputStream os = loadpaths.getOutputStream(
                                                            file,
                                                            StandardOpenOption.CREATE,
                                                            StandardOpenOption.WRITE,
                                                            VncBoolean.isTrue(append)
                                                                ? StandardOpenOption.APPEND
                                                                : StandardOpenOption.TRUNCATE_EXISTING);
                        if (os != null) {
                            return new VncJavaObject(
                                    new BufferedWriter(
                                            new OutputStreamWriter(os, charset)));
                        }
                        else {
                            throw new SecurityException(
                                    String.format(
                                            "Failed to create writer to the file %s. " +
                                            "The load paths configuration prevented this action!\n" +
                                            "Load-Paths:  unlimited-access=%b, paths=%s",
                                            file.getPath(),
                                            loadpaths.isUnlimitedAccess(),
                                            loadpaths.getPaths().toString()));
                        }
                    }
                    catch (VncException ex) {
                        throw ex;
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to create writer to the file " + file.getPath(),
                                ex);
                    }
                }
                else if (Types.isVncJavaObject(args.first(), OutputStream.class)) {
                    try {
                        final OutputStream os = Coerce.toVncJavaObject(args.first(), OutputStream.class);
                        return new VncJavaObject(
                                new BufferedWriter(
                                        new OutputStreamWriter(os, charset)));
                    }
                    catch (VncException ex) {
                        throw ex;
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to create writer to the OutputStream!",
                                ex);
                    }
                }
                else if (Types.isVncJavaObject(args.first(), BufferedWriter.class)) {
                    return args.first();
                }
                else if (Types.isVncJavaObject(args.first(), Writer.class)) {
                    final Writer wr = Coerce.toVncJavaObject(args.first(), Writer.class);
                    return new VncJavaObject(new BufferedWriter(wr));
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/buffered-writer' does not allow %s as f",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_string_writer =
            new VncFunction(
                    "io/string-writer",
                    VncFunction
                        .meta()
                        .arglists(
                            "(io/string-writer)" )
                        .doc(
                            "Creates a `java.io.StringWriter`.\n\n" +
                            "Dereferencing a string writer returns the " +
                            "captured string.\n\n" +
                            "Note: The caller is responsible for closing the writer!")
                        .examples(
                            "(try-with [sw (io/string-writer)]     \n" +
                            "  (print sw 100)                      \n" +
                            "  (print sw \"-\")                    \n" +
                            "  (print sw 200)                      \n" +
                            "  (flush sw)                          \n" +
                            "  (println @sw))                      ")
                        .seeAlso(
                            "println", "io/buffered-writer", "io/buffered-reader")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    return new VncJavaObject(new StringWriter());
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction io_buffered_reader =
        new VncFunction(
                "io/buffered-reader",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/buffered-reader f & options)" )
                    .doc(
                        "Create a `java.io.Reader` from f.                                 \n\n" +
                        "f may be a:                                                       \n\n" +
                        " * string                                                         \n" +
                        " * bytebuffer                                                     \n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")`            \n" +
                        " * `java.nio.file.Path`                                           \n" +
                        " * `java.io.InputStream`                                          \n" +
                        " * `java.io.Reader`                                               \n" +
                        " * `java.net.URL`                                                 \n" +
                        " * `java.net.URI`                                                 \n\n" +
                        "Options:                                                          \n\n" +
                        "| :encoding enc | e.g.: `:encoding :utf-8`, defaults to :utf-8 |  \n\n" +
                        "`io/buffered-reader` supports load paths. See the `loadpath/paths`" +
                        "doc for a description of the *load path* feature.                 \n\n" +
                        "Note: The caller is responsible for closing the reader!")
                .examples(
                        "(let [data (bytebuf [108 105 110 101 32 49 10 108 105 110 101 32 50])]     \n" +
                        "  (try-with [rd (io/buffered-reader data :encoding :utf-8)]                \n" +
                        "    (println (read-line rd))                                               \n" +
                        "    (println (read-line rd))))                                             ",
                        "(try-with [rd (io/buffered-reader \"1\\n2\\n3\\n4\")]                      \n" +
                        "  (println (read-line rd))                                                 \n" +
                        "  (println (read-line rd)))                                                ")
                .seeAlso(
                        "read-line", "io/string-reader", "io/buffered-writer")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncVal arg = args.first();

                if (Types.isVncString(arg)) {
                    return new VncJavaObject(
                            new BufferedReader(
                                new StringReader(
                                        Coerce.toVncString(arg).getValue())));
                }


                final VncHashMap options = VncHashMap.ofAll(args.rest());
                final VncVal encVal = options.get(new VncKeyword("encoding"));
                final Charset charset = CharsetUtil.charset(encVal);

                final ILoadPaths loadpaths = ThreadContext.getInterceptor().getLoadPaths();


                final File file = convertToFile(arg);
                if (file != null) {
                    try {
                        final Reader rd = loadpaths.getBufferedReader(file, charset);
                        if (rd == null) {
                            if (file.exists()) {
                                throw new VncException(
                                        "Failed to create reader from the file " + file.getPath() +
                                        ". The file does not exists!");
                            }
                            else {
                                throw new SecurityException(
                                        "Failed to create reader from the file " + file.getPath() +
                                        ". The load paths configuration prevented this action!");
                            }
                        }
                        return new VncJavaObject(rd);
                    }
                    catch (VncException ex) {
                        throw ex;
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to create reader from the file " + file.getPath(), ex);
                    }
                }
                else if (Types.isVncByteBuffer(arg)) {
                    try {
                        final VncByteBuffer buf = (VncByteBuffer)arg;
                        final InputStream is = new ByteArrayInputStream(buf.getBytes());
                        return new VncJavaObject(
                                new BufferedReader(
                                        new InputStreamReader(is, charset)));
                    }
                    catch (VncException ex) {
                        throw ex;
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to create reader from a bytebuffer", ex);
                    }
                }
                else if (Types.isVncJavaObject(arg, InputStream.class)) {
                    try {
                        final InputStream is = Coerce.toVncJavaObject(args.first(), InputStream.class);
                        return new VncJavaObject(
                                new BufferedReader(
                                        new InputStreamReader(is, charset)));
                    }
                    catch (VncException ex) {
                        throw ex;
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to create reader from a :java.io.InputStream", ex);
                    }
                }
                else if (Types.isVncJavaObject(arg, BufferedReader.class)) {
                    return arg;
                }
                else if (Types.isVncJavaObject(arg, Reader.class)) {
                    try {
                        final Reader rd = Coerce.toVncJavaObject(args.first(), Reader.class);
                        return new VncJavaObject(new BufferedReader(rd));
                    }
                    catch (VncException ex) {
                        throw ex;
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to create reader from a :java.io.Reader", ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/buffered-reader' does not allow %s as f",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_string_reader =
        new VncFunction(
                "io/string-reader",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/string-reader s)" )
                    .doc(
                        "Creates a `java.io.StringReader` from a string.\n\n" +
                        "Note: The caller is responsible for closing the reader!")
                    .examples(
                        "(try-with [rd (io/string-reader \"1234\")]       \n" +
                        "  (println (read-char rd))                       \n" +
                        "  (println (read-char rd))                       \n" +
                        "  (println (read-char rd)))                      ",
                        "(let [rd (io/string-reader \"1\\n2\\n3\\n4\")]   \n" +
                        "  (try-with [br (io/buffered-reader rd)]         \n" +
                        "    (println (read-line br))                     \n" +
                        "    (println (read-line br))                     \n" +
                        "    (println (read-line br))))                   ")
                    .seeAlso(
                        "read-line", "io/buffered-reader", "io/string-writer")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncJavaObject(
                            new StringReader(
                                    Coerce.toVncString(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_capturing_print_stream =
        new VncFunction(
                "io/capturing-print-stream",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/capturing-print-stream)" )
                    .doc(
                        "Creates a new capturing print stream.\n\n" +
                        "Dereferencing a capturing print stream returns the " +
                        "captured string.\n\n" +
                        "Note: The caller is responsible for closing the stream!")
                    .examples(
                        "(try-with [ps (io/capturing-print-stream)]    \n" +
                        "  (binding [*out* ps]                         \n" +
                        "    (println 100)                             \n" +
                        "    (println 200)                             \n" +
                        "    (flush)                                   \n" +
                        "    @ps))                                     ",
                        "(try-with [ps (io/capturing-print-stream)]    \n" +
                        "  (println ps 100)                            \n" +
                        "  (println ps 200)                            \n" +
                        "  (flush ps)                                  \n" +
                        "  @ps)                                        ")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncJavaObject(new CapturingPrintStream());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_print =
        new VncFunction(
                "io/print",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/print os s)" )
                    .doc(
                        "Prints a string s to an output stream. The output stream " +
                        "may be a `:java.io.Writer` or a `:java.io.PrintStream`!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal v = args.first();
                if (Types.isVncJavaObject(v, PrintStream.class)) {
                    final PrintStream ps = Coerce.toVncJavaObject(v, PrintStream.class);
                    ps.print(Printer.pr_str(args.second(), false));
                }
                else if (Types.isVncJavaObject(v, Writer.class)) {
                    final Writer wr = Coerce.toVncJavaObject(v, Writer.class);
                    try {
                        wr.write(Printer.pr_str(args.second(), false));
                    }
                    catch(IOException ex) {
                        throw new VncException("Failed print string to a :java.io.Writer", ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "io/print does not allow type %s as output stream arg. " +
                            "Expected a :java.io.PrintStream or a :java.io.Writer.",
                            Types.getType(args.first())));
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_print_line =
        new VncFunction(
                "io/print-line",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/print-line os)",
                        "(io/print-line os s)")
                    .doc(
                        "Prints a string s to an output stream. The output stream " +
                        "may be a `:java.io.Writer` or a `:java.io.PrintStream`!")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final VncVal v = args.first();
                if (Types.isVncJavaObject(v, PrintStream.class)) {
                    final PrintStream ps = Coerce.toVncJavaObject(v, PrintStream.class);
                    if (args.size() > 1) {
                        ps.println(Printer.pr_str(args.second(), false));
                    }
                    else {
                        ps.println();
                    }
                }
                else if (Types.isVncJavaObject(v, BufferedWriter.class)) {
                    final BufferedWriter wr = Coerce.toVncJavaObject(v, BufferedWriter.class);
                    try {
                        if (args.size() > 1) {
                            wr.write(Printer.pr_str(args.second(), false));
                        }
                        wr.newLine();
                    }
                    catch(IOException ex) {
                        throw new VncException("Failed print string to a :java.io.BufferedWriter", ex);
                    }
                }
                else if (Types.isVncJavaObject(v, Writer.class)) {
                    final Writer wr = Coerce.toVncJavaObject(v, Writer.class);
                    try {
                        if (args.size() > 1) {
                            wr.write(Printer.pr_str(args.second(), false));
                        }
                        wr.write('\n');
                    }
                    catch(IOException ex) {
                        throw new VncException("Failed print string to a :java.io.Writer", ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "io/print-line does not allow type %s as output stream arg. " +
                            "Expected a :java.io.PrintStream or a :java.io.Writer.",
                            Types.getType(args.first())));
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_read_line =
        new VncFunction(
                "io/read-line",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/read-line is)" )
                    .doc(
                        "Reads the next line from the passed stream " +
                        "that must be a subclass of `:java.io.BufferedReader`.\n\n" +
                        "Returns `nil` if the end of the stream is reached.")
                    .seeAlso("io/read-char")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();
                if (Types.isVncJavaObject(v, BufferedReader.class)) {
                    final BufferedReader br = Coerce.toVncJavaObject(v, BufferedReader.class);
                    try {
                        final String line = br.readLine();
                        return line == null ? Nil : new VncString(line);
                    }
                    catch(IOException ex) {
                        throw new VncException(
                                "Failed read the next line from a :java.io.BufferedReader.",
                                ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "io/read-line does not allow type %s as input stream arg. " +
                            "Expected a :java.io.BufferedReader.",
                            Types.getType(args.first())));

                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_read_char =
        new VncFunction(
                "io/read-char",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/read-char is)" )
                    .doc(
                        "With arg reads the next char from the passed stream " +
                        "that must be a subclass of `:java.io.Reader`.\n\n" +
                        "Returns `nil` if the end of the stream is reached.")
                    .seeAlso("io/read-line")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal v = args.first();
                if (Types.isVncJavaObject(v, Reader.class)) {
                    final Reader rd = Coerce.toVncJavaObject(v, Reader.class);
                    try {
                        final int ch = rd.read();
                        return ch == -1 ? Nil : new VncChar((char)ch);
                    }
                    catch(IOException ex) {
                        throw new VncException(
                                "Failed read the next a char from a :java.io.Reader",
                                ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "io/read-char does not allow type %s as input stream arg. " +
                            "Expected a :java.io.Reader.",
                            Types.getType(args.first())));

                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_in_streamQ =
        new VncFunction(
                "io/in-stream?",
                VncFunction
                    .meta()
                    .arglists("(io/in-stream? is)")
                    .doc(
                        "Returns true if 'is' is a `java.io.InputStream`")
                    .examples(
                        "(try-with [is (io/string-in-stream \"123\")]  \n" +
                        "  (io/in-stream? is))                         ")
                    .seeAlso(
                        "io/out-stream?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                if (args.first() == Nil) {
                    return VncBoolean.False;
                }
                else {
                    final Object o = Coerce.toVncJavaObject(args.first()).getDelegate();
                    return VncBoolean.of(o instanceof InputStream);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_out_streamQ =
        new VncFunction(
                "io/out-stream?",
                VncFunction
                    .meta()
                    .arglists("(io/out-stream? os)")
                    .doc(
                        "Returns true if 'os' is a `java.io.OutputStream`")
                    .examples(
                        "(try-with [os (io/bytebuf-out-stream)]    \n" +
                        "  (io/out-stream? os))                    ")
                    .seeAlso(
                        "io/in-stream?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                if (args.first() == Nil) {
                    return VncBoolean.False;
                }
                else {
                    final Object o = Coerce.toVncJavaObject(args.first()).getDelegate();
                    return VncBoolean.of(o instanceof OutputStream);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_readerQ =
        new VncFunction(
                "io/reader?",
                VncFunction
                    .meta()
                    .arglists("(io/reader? rd)")
                    .doc(
                        "Returns true if 'rd' is a `java.io.Reader`")
                    .examples(
                        "(try-with [rd (io/string-reader \"123\")]  \n" +
                        "  (io/reader? rd))                         ")
                    .seeAlso(
                        "io/writer?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                if (args.first() == Nil) {
                    return VncBoolean.False;
                }
                else {
                    final Object o = Coerce.toVncJavaObject(args.first()).getDelegate();
                    return VncBoolean.of(o instanceof Reader);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_writerQ =
        new VncFunction(
                "io/writer?",
                VncFunction
                    .meta()
                    .arglists("(io/writer? rd)")
                    .doc(
                        "Returns true if 'rd' is a `java.io.Writer`")
                    .examples(
                        "(try-with [wr (io/string-writer)]  \n" +
                        "  (io/writer? wr))                 ")
                    .seeAlso(
                        "io/reader?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                if (args.first() == Nil) {
                    return VncBoolean.False;
                }
                else {
                    final Object o = Coerce.toVncJavaObject(args.first()).getDelegate();
                    return VncBoolean.of(o instanceof Writer);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static File convertToFile(final VncVal f, final String errFormat) {
        final File file = convertToFile(f);
        if (file == null) {
            throw new VncException(String.format(errFormat, Types.getType(f)));
        }
        else {
            return file;
        }
    }

    private static File convertToFile(final VncVal f) {
        if (Types.isVncString(f)) {
            return new File(((VncString)f).getValue());
        }
        else if (Types.isVncJavaObject(f, File.class)) {
            return Coerce.toVncJavaObject(f, File.class);
        }
        else if (Types.isVncJavaObject(f, Path.class)) {
            return Coerce.toVncJavaObject(f, Path.class).toFile();
        }
        else {
            return null;
        }
    }

    public static void validateReadableFile(final File file) {
        if (!file.isFile()) {
            throw new VncException(String.format("'%s' is not a file", file.getPath()));
        }
        if (!file.canRead()) {
            throw new VncException(String.format("The file '%s' has no read permission", file.getPath()));
        }
    }



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(io_copy_stream)
                    .add(io_uri_stream)
                    .add(io_file_in_stream)
                    .add(io_file_out_stream)
                    .add(io_string_in_stream)
                    .add(io_bytebuf_in_stream)
                    .add(io_bytebuf_out_stream)
                    .add(io_wrap_os_with_buffered_writer)
                    .add(io_wrap_os_with_print_writer)
                    .add(io_wrap_is_with_buffered_reader)
                    .add(io_wrap_is_with_gzip_input_stream)
                    .add(io_wrap_os_with_gzip_output_stream)
                    .add(io_wrap_is_with_inflater_input_stream)
                    .add(io_wrap_os_with_deflater_output_stream)
                    .add(io_buffered_writer)
                    .add(io_string_writer)
                    .add(io_buffered_reader)
                    .add(io_string_reader)
                    .add(io_capturing_print_stream)
                    .add(io_print)
                    .add(io_print_line)
                    .add(io_read_line)
                    .add(io_read_char)
                    .add(io_flush)
                    .add(io_close)
                    .add(io_in_streamQ)
                    .add(io_out_streamQ)
                    .add(io_readerQ)
                    .add(io_writerQ)
                    .toMap();
    }
