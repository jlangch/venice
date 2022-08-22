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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBoolean;
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


public class IOFunctionsStreams {


    ///////////////////////////////////////////////////////////////////////////
    // I/O functions
    ///////////////////////////////////////////////////////////////////////////

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
                            "Failed to copy data from a :java.io.InputStream to an :java.io.OutputStream");
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
                        "Returns a `java.io.InputStream` for the file f.                       \n\n" +
                        "f may be a:                                                           \n\n" +
                        " * string file path, e.g: \"/temp/foo.json\"                          \n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")`                \n")
                    .seeAlso("io/slurp", "io/slurp-stream", "io/string-in-stream", "io/bytebuf-in-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File file = convertToFile(args.first());
                if (file != null) {
                    try {
                        validateReadableFile(file);
                        return new VncJavaObject(new FileInputStream(file));
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
                        "| :encoding enc      | e.g.: `:encoding :utf-8`, defaults to :utf-8 |\n")
                    .seeAlso("io/slurp", "io/slurp-stream", "io/string-in-stream", "io/bytebuf-in-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncHashMap options = VncHashMap.ofAll(args.slice(2));
                final VncVal append = options.get(new VncKeyword("append"));

                final File file = convertToFile(args.first());
                if (file != null) {
                    try {
                        validateReadableFile(file);

                        return new VncJavaObject(
                        		Files.newOutputStream(
                        				file.toPath(),
                        				StandardOpenOption.CREATE,
                                        StandardOpenOption.WRITE,
                                        VncBoolean.isTrue(append)
                                            ? StandardOpenOption.APPEND
                                            : StandardOpenOption.TRUNCATE_EXISTING));
                     }
                    catch (Exception ex) {
                        throw new VncException("Failed to create a a `java.io.OutputStream` for the file " + file.getPath(), ex);
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
                        "| :encoding enc      | e.g.: `:encoding :utf-8`, defaults to :utf-8 | \n")
                    .examples(
                        "(io/string-in-stream \"The quick brown fox jumped over the lazy dog\")")
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
                    .doc("Returns a `java.io.InputStream` from a bytebuf.")
                    .examples(
                        "(io/bytebuf-in-stream (bytebuf [97 98 99]))")
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
                    .doc("Returns a new `java.io.ByteArrayOutputStream`.")
                    .examples(
                        "(let [os (io/bytebuf-out-stream)]                       \n" +
                        "   (io/spit-stream os (bytebuf [97 98 99]) :flush true) \n" +
                        "   (str/format-bytebuf (bytebuf os) \", \" :prefix0x))  ")
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
                    .doc("Returns a `java.io.InputStream` from the uri.")
                    .examples(
                        "(-> (io/uri-stream \"https://www.w3schools.com/xml/books.xml\") \n" +
                        "    (io/slurp-stream :binary false :encoding :utf-8))             ")
                    .seeAlso("io/slurp-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final String uri = Coerce.toVncString(args.first()).getValue();

                try {
                    return new VncJavaObject(new URL(uri).openStream());
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
                        "encoding (defaults to :utf-8).")
                    .examples(
                        "(do                                                         \n" +
                        "   (import :java.io.ByteArrayOutputStream)                  \n" +
                        "   (let [os (. :ByteArrayOutputStream :new)                 \n" +
                        "         wr (io/wrap-os-with-buffered-writer os :utf-8)]    \n" +
                        "      (. wr :write \"line 1\")                              \n" +
                        "      (. wr :newLine)                                       \n" +
                        "      (. wr :write \"line 2\")                              \n" +
                        "      (. wr :flush)                                         \n" +
                        "      (. os :toByteArray)))                                   ")
                    .seeAlso("io/wrap-os-with-print-writer")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                try {
                    final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
                    final Charset charset = CharsetUtil.charset(args.second());

                    return new VncJavaObject(new BufferedWriter(new OutputStreamWriter(os, charset)));
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to wrap an :java.io.OutputStream with a :java.io.BufferedWriter",
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
                        "encoding (defaults to :utf-8).")
                    .examples(
                        "(do                                                      \n" +
                        "   (import :java.io.ByteArrayOutputStream)               \n" +
                        "   (let [os (. :ByteArrayOutputStream :new)              \n" +
                        "         wr (io/wrap-os-with-print-writer os :utf-8)]    \n" +
                        "      (. wr :println \"line 1\")                         \n" +
                        "      (. wr :println \"line 2\")                         \n" +
                        "      (. wr :flush)                                      \n" +
                        "      (. os :toByteArray)))                                ")
                    .seeAlso("io/wrap-os-with-buffered-writer")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                try {
                    final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
                    final Charset charset = CharsetUtil.charset(args.second());

                    return new VncJavaObject(new PrintWriter(new OutputStreamWriter(os, charset)));
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to wrap an :java.io.OutputStream with a :java.io.PrintWriter",
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
                            "encoding (defaults to :utf-8).")
                        .examples(
                            "(do                                                                          \n" +
                            "   (import :java.io.ByteArrayInputStream)                                    \n" +
                            "   (let [data (byte-array [108 105 110 101 32 49 10 108 105 110 101 32 50])  \n" +
                            "         is (. :ByteArrayInputStream :new data)                              \n" +
                            "         rd (io/wrap-is-with-buffered-reader is :utf-8)]                     \n" +
                            "      (println (. rd :readLine))                                             \n" +
                            "      (println (. rd :readLine))))                                             ")
                        .seeAlso("io/buffered-reader")
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

                                return new VncJavaObject(new BufferedReader(new InputStreamReader(is, charset)));
                            }
                            catch (Exception ex) {
                                throw new VncException(
                                        "Failed to wrap an :java.io.InputStream with a :java.io.BufferReader",
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

    public static VncFunction io_buffered_reader =
        new VncFunction(
                "io/buffered-reader",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/buffered-reader is encoding?)",
                        "(io/buffered-reader rdr)")
                    .doc(
                        "Creates a `java.io.BufferedReader` from a `java.io.InputStream` is with optional " +
                        "encoding (defaults to :utf-8), from a Reader or from a string.")
                    .examples(
                        "(do                                                                          \n" +
                        "   (import :java.io.ByteArrayInputStream)                                    \n" +
                        "   (let [data (byte-array [108 105 110 101 32 49 10 108 105 110 101 32 50])  \n" +
                        "         is (. :ByteArrayInputStream :new data)                              \n" +
                        "         rd (io/buffered-reader is :utf-8)]                                  \n" +
                        "      (println (. rd :readLine))                                             \n" +
                        "      (println (. rd :readLine))))                                             ",
                        "(do                                                                          \n" +
                        "   (let [rd (io/buffered-reader \"1\\n2\\n3\\n4\")]                          \n" +
                        "      (println (. rd :readLine))                                             \n" +
                        "      (println (. rd :readLine))))                                             ")
                    .seeAlso("io/buffered-writer")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (Types.isVncString(args.first())) {
                    return new VncJavaObject(
                            new BufferedReader(
                                    new StringReader(((VncString)args.first()).getValue())));
                }
                else if (Types.isVncJavaObject(args.first())) {
                    final Object delegate = ((VncJavaObject)args.first()).getDelegate();
                    if (delegate instanceof InputStream) {
                        try {
                            final InputStream is = (InputStream)delegate;
                            final Charset charset = CharsetUtil.charset(args.second());

                            return new VncJavaObject(new BufferedReader(new InputStreamReader(is, charset)));
                        }
                        catch (Exception ex) {
                            throw new VncException(ex.getMessage(), ex);
                        }
                    }
                    else if (delegate instanceof BufferedReader) {
                        return args.first();
                    }
                    else if (delegate instanceof Reader) {
                        return new VncJavaObject(new BufferedReader((Reader)delegate));
                    }
                }

                throw new VncException(String.format(
                        "Function 'io/buffered-reader' requires a :java.io.InputStream, " +
                        "a :java.io.Reader, or a string. %s as is not allowed!",
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
                        "(io/buffered-writer os encoding?)",
                        "(io/buffered-writer wr)")
                    .doc(
                        "Creates a `java.io.BufferedWriter` from a `java.io.OutputStream` os with optional " +
                        "encoding (defaults to :utf-8) or from a Writer.")
                    .examples()
                    .seeAlso("io/buffered-reader")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if (Types.isVncJavaObject(args.first())) {
                    final Object delegate = ((VncJavaObject)args.first()).getDelegate();
                    if (delegate instanceof OutputStream) {
                        try {
                            final OutputStream os = (OutputStream)delegate;
                            final Charset charset = CharsetUtil.charset(args.second());

                            return new VncJavaObject(new BufferedWriter(new OutputStreamWriter(os, charset)));
                        }
                        catch (Exception ex) {
                            throw new VncException(ex.getMessage(), ex);
                        }
                    }
                    else if (delegate instanceof BufferedWriter) {
                        return args.first();
                    }
                    else if (delegate instanceof Writer) {
                        return new VncJavaObject(new BufferedWriter((Writer)delegate));
                    }
                }

                throw new VncException(String.format(
                        "Function 'io/buffered-writer' requires a :java.io.OutputStream " +
                        "or a :java.io.Writer. %s as is not allowed!",
                        Types.getType(args.first())));
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

    public static Map<VncVal, VncVal> ns =
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
                    .add(io_buffered_reader)
                    .add(io_buffered_writer)
                    .toMap();
}
