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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
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
import com.github.jlangch.venice.javainterop.ILoadPaths;


public class IOFunctionsSpitSlurp {


    ///////////////////////////////////////////////////////////////////////////
    // I/O functions
    ///////////////////////////////////////////////////////////////////////////


    public static VncFunction io_slurp_lines =
        new VncFunction(
                "io/slurp-lines",
                VncFunction
                    .meta()
                    .arglists("(io/slurp-lines f & options)")
                    .doc(
                        "Read all lines from f.                                            \n\n" +
                        "f may be a:                                                       \n\n" +
                        " * string file path, e.g: \"/temp/foo.json\"                      \n" +
                        " * bytebuffer                                        `            \n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")`            \n" +
                        " * `java.io.InputStream`                                          \n" +
                        " * `java.io.Reader`                                               \n" +
                        " * `java.nio.file.Path`                                           \n" +
                        " * `java.net.URL`                                                 \n" +
                        " * `java.net.URI`                                                 \n\n" +
                        "Options:                                                          \n\n" +
                        "| :encoding enc | e.g.: `:encoding :utf-8`, defaults to :utf-8 |  \n\n" +
                        "`io/slurp-lines` supports load paths. See the `loadpath/paths` " +
                        "doc for a description of the *load path* feature.")
                    .examples(
                        "(->> \"1\\n2\\n3\"        \n" +
                        "     io/string-in-stream  \n" +
                        "     io/slurp-lines)      ")
                    .seeAlso(
                        "str/split-lines",
                        "io/slurp", "io/slurp-stream",
                        "io/spit", "io/string-in-stream",
                        "loadpath/paths")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncVal arg = args.first();

                final VncHashMap options = VncHashMap.ofAll(args.rest());

                final ILoadPaths loadpaths = ThreadContext.getInterceptor().getLoadPaths();

                final File file = convertToFile(arg);
                if (file != null) {
                    try {
                        final InputStream is = loadpaths.getInputStream(file);
                        if (is == null) {
                            if (file.exists()) {
                                throw new VncException(
                                        "Failed to slurp text lines from the file " + file.getPath() +
                                        ". The file does not exists!");
                            }
                            else {
                                throw new SecurityException(
                                        "Failed to slurp text lines from the file " + file.getPath() +
                                        ". The load paths configuration prevented this action!");
                            }
                        }
                        return slurpLines(options, is);
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to slurp text lines from the file " + file.getPath(),
                                ex);
                    }
                }
                else if (Types.isVncByteBuffer(arg)) {
                    try {
                        final VncByteBuffer buf = (VncByteBuffer)arg;
                        final InputStream is = new ByteArrayInputStream(buf.getBytes());
                        return slurpLines(options, is);
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to slurp text lines from a bytebuffer", ex);
                    }
                }
                else if (Types.isVncJavaObject(arg, InputStream.class)) {
                    try {
                        final InputStream is = Coerce.toVncJavaObject(args.first(), InputStream.class);
                        return slurpLines(options, is);
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to slurp text lines from a :java.io.InputStream", ex);
                    }
                }
                else if (Types.isVncJavaObject(arg, Reader.class)) {
                    try {
                        final Reader rd = Coerce.toVncJavaObject(args.first(), Reader.class);
                        return slurpLines(options, rd);
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to slurp text lines from a :java.io.Reader", ex);
                    }
                }
                else if (Types.isVncJavaObject(arg, URL.class)) {
                    if (loadpaths.isUnlimitedAccess()) {
                        try {
                            final URL url = Coerce.toVncJavaObject(args.first(), URL.class);
                            return slurpLines(options, url.openStream());
                        }
                        catch (Exception ex) {
                            throw new VncException("Failed to slurp text lines from a :java.net.URL", ex);
                        }
                    }
                    else {
                        throw new SecurityException(
                                "Rejected to slurp text lines from a :java.net.URL. " +
                                "The load paths configuration (unlimited access is disabled) prevented this action!");
                    }
                }
                else if (Types.isVncJavaObject(arg, URI.class)) {
                    if (loadpaths.isUnlimitedAccess()) {
                        try {

                            final URI uri = Coerce.toVncJavaObject(args.first(), URI.class);
                            return slurpLines(options, uri.toURL().openStream());
                        }
                        catch (Exception ex) {
                            throw new VncException("Failed to slurp text lines from a :java.net.URI", ex);
                        }
                    }
                    else {
                        throw new SecurityException(
                                "Rejected to slurp text lines from a :java.net.URI. " +
                                "The load paths configuration (unlimited access is disabled) prevented this action!");
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/slurp-lines' does not allow %s as f",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_slurp =
        new VncFunction(
                "io/slurp",
                VncFunction
                    .meta()
                    .arglists("(io/slurp f & options)")
                    .doc(
                        "Reads the content of file f as text (string) or binary (bytebuf). \n\n" +
                        "f may be a:                                                       \n\n" +
                        " * string file path, e.g: \"/temp/foo.json\"                      \n" +
                        " * bytebuffer                                        `            \n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")`            \n" +
                        " * `java.io.InputStream`                                          \n" +
                        " * `java.io.Reader`                                               \n" +
                        " * `java.nio.file.Path`                                           \n" +
                        " * `java.net.URL`                                                 \n" +
                        " * `java.net.URI`                                                 \n\n" +
                        "Options:                                                          \n\n" +
                        "| :binary true/false | e.g.: `:binary true`, defaults to false |     \n" +
                        "| :encoding enc      | e.g.: `:encoding :utf-8`, defaults to :utf-8 |\n\n" +
                        "`io/slurp` supports load paths. See the `loadpath/paths` " +
                        "doc for a description of the *load path* feature.\n\n" +
                        "Note: For HTTP and HTTPS downloads prefer to use `io/download`. ")
                    .seeAlso(
                    	"io/slurp-lines", "io/slurp-stream", "io/spit", "io/download",
                    	"loadpath/paths")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncVal arg = args.first();

                final VncHashMap options = VncHashMap.ofAll(args.rest());

                final ILoadPaths loadpaths = ThreadContext.getInterceptor().getLoadPaths();

                final File file = convertToFile(arg);
                if (file != null) {
                    try {
                        final InputStream is = loadpaths.getInputStream(file);
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
                        return slurp(options, is);
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to slurp data from the file " + file.getPath(), ex);
                    }
                }
                else if (Types.isVncByteBuffer(arg)) {
                    try {
                        final VncByteBuffer buf = (VncByteBuffer)arg;
                        final InputStream is = new ByteArrayInputStream(buf.getBytes());
                        return slurp(options, is);
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to slurp text lines from a bytebuffer", ex);
                    }
                }
                else if (Types.isVncJavaObject(arg, InputStream.class)) {
                    try {
                        final InputStream is = Coerce.toVncJavaObject(args.first(), InputStream.class);
                        return slurp(options, is);
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to slurp data from a :java.io.InputStream", ex);
                    }
                }
                else if (Types.isVncJavaObject(arg, Reader.class)) {
                    try {
                        final Reader rd = Coerce.toVncJavaObject(args.first(), Reader.class);
                        return slurp(options, rd);
                    }
                    catch (Exception ex) {
                        throw new VncException("Failed to slurp data from a :java.io.Reader", ex);
                    }
                }
                else if (Types.isVncJavaObject(arg, URL.class)) {
                    if (loadpaths.isUnlimitedAccess()) {
                        try {
                            final URL url = Coerce.toVncJavaObject(args.first(), URL.class);
                            return slurp(options, url.openStream());
                        }
                        catch (Exception ex) {
                            throw new VncException("Failed to slurp data from a :java.net.URL", ex);
                        }
                    }
                    else {
                        throw new SecurityException(
                                "Rejected to slurp data from a :java.net.URL. " +
                                "The load paths configuration (unlimited access is disabled) " +
                                "prevented this action! You can use 'io/download' instead, " +
                                "given this not blacklisted by the sandbox either.)");
                    }
                }
                else if (Types.isVncJavaObject(arg, URI.class)) {
                    if (loadpaths.isUnlimitedAccess()) {
                        try {
                            final URI uri = Coerce.toVncJavaObject(args.first(), URI.class);
                            return slurp(options, uri.toURL().openStream());
                        }
                        catch (Exception ex) {
                            throw new VncException("Failed to slurp data from a :java.net.URI", ex);
                        }
                    }
                    else {
                        throw new SecurityException(
                                "Rejected to slurp data from a :java.net.URI. " +
                                "The load paths configuration (unlimited access is disabled) " +
                                "prevented this action! You can use 'io/download' instead, " +
                                "given this not blacklisted by the sandbox either.)");
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/slurp' does not allow %s as f",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_spit =
        new VncFunction(
                "io/spit",
                VncFunction
                    .meta()
                    .arglists("(io/spit f content & options)")
                    .doc(
                        "Opens file f, writes content, and then closes f. " +
                        "f may be a file or a string (file path). " +
                        "The content may be a string or a bytebuf.\n\n" +
                        "Options: \n\n" +
                        "| :append true/false | e.g.: `:append true`, defaults to false |\n" +
                        "| :encoding enc      | e.g.: `:encoding :utf-8`, defaults to :utf-8 |\n\n" +
                        "`io/spit` supports load paths. See the `loadpath/paths` " +
                        "doc for a description of the *load path* feature.")
                    .seeAlso(
                    	"io/spit-stream", "io/slurp", "io/slurp-lines",
                    	"loadpath/paths")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                final ILoadPaths loadpaths = ThreadContext.getInterceptor().getLoadPaths();

                final VncVal content = args.second();

                final VncHashMap options = VncHashMap.ofAll(args.slice(2));
                final VncVal append = options.get(new VncKeyword("append"));
                final VncVal encVal = options.get(new VncKeyword("encoding"));
                final Charset charset = CharsetUtil.charset(encVal);

                byte[] binaryData = null;
                String stringData = null;
                if (Types.isVncString(content)) {
                    stringData = ((VncString)content).getValue();
                }
                else if (Types.isVncByteBuffer(content)) {
                    binaryData = ((VncByteBuffer)content).getBytes();
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/spit' does not allow %s as content",
                            Types.getType(content)));
                }

                final File file = convertToFile(args.first());
                if (file != null) {
                    try {
                        final OutputStream outStream = loadpaths.getOutputStream(
                                                            file,
                                                            StandardOpenOption.CREATE,
                                                            StandardOpenOption.WRITE,
                                                            VncBoolean.isTrue(append)
                                                                ? StandardOpenOption.APPEND
                                                                : StandardOpenOption.TRUNCATE_EXISTING);
                        if (outStream != null) {
                            try (OutputStream os = outStream) {
                                os.write(stringData != null ? stringData.getBytes(charset) : binaryData);
                                os.flush();
                            }
                        }
                        else {
                            throw new SecurityException(
                                    String.format(
                                            "Failed to spit data to the file %s. " +
                                            "The load paths configuration prevented this action!\n" +
                                            "Load-Paths:  unlimited-access=%b, paths=%s",
                                            file.getPath(),
                                            loadpaths.isUnlimitedAccess(),
                                            loadpaths.getPaths().toString()));
                        }
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to spit data to the file " + file.getPath(),
                                ex);
                    }
                }
                else if (Types.isVncJavaObject(args.first(), OutputStream.class)) {
                    try {
                        final OutputStream os = Coerce.toVncJavaObject(args.first(), OutputStream.class);
                        os.write(stringData != null ? stringData.getBytes(charset) : binaryData);
                        os.flush();
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to spit data to the OutputStream!",
                                ex);
                    }
                }
                else if (Types.isVncJavaObject(args.first(), Writer.class)) {
                    try {
                        final Writer wr =  Coerce.toVncJavaObject(args.first(), Writer.class);
                        wr.write(binaryData != null ? new String(binaryData, charset) : stringData);
                        wr.flush();
                    }
                    catch (Exception ex) {
                        throw new VncException(
                                "Failed to spit data to the Writer!",
                                ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/spit' does not allow %s as f",
                            Types.getType(args.first())));
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_slurp_stream =
        new VncFunction(
                "io/slurp-stream",
                VncFunction
                    .meta()
                    .arglists("(io/slurp-stream is & options)")
                    .doc(
                        "Slurps binary or string data from a `java.io.InputStream` is. " +
                        "Supports the option :binary to either slurp binary or string data. " +
                        "For string data an optional encoding can be specified.\n\n" +
                        "Options: \n\n" +
                        "| :binary true/false | e.g.: `:binary true`, defaults to false |\n" +
                        "| :encoding enc      | e.g.: `:encoding :utf-8`, defaults to :utf-8 |\n\n" +
                        "Note: \n\n" +
                        "`io/slurp-stream` offers the same functionality as `io/slurp` but it " +
                        "opens more flexibility with sandbox configuration. `io/slurp` can be " +
                        "blacklisted to prevent reading data from the filesystem and still having " +
                        "`io/slurp-stream` for stream input available!")
                    .examples(
                        "(do \n" +
                        "   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
                        "      (io/delete-file-on-exit file) \n" +
                        "      (io/spit file \"123456789\" :append true) \n" +
                        "      (try-with [is (io/file-in-stream file)] \n" +
                        "         (io/slurp-stream is :binary false))) \n" +
                        ")")
                    .seeAlso(
                        "io/slurp", "io/slurp-lines", "io/spit",
                        "io/uri-stream",
                        "io/file-in-stream", "io/string-in-stream", "io/bytebuf-in-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                try {
                    final InputStream is = Coerce.toVncJavaObject(args.first(), InputStream.class);

                    final VncHashMap options = VncHashMap.ofAll(args.rest());
                    final VncVal binary = options.get(new VncKeyword("binary"));

                    if (VncBoolean.isTrue(binary)) {
                        final byte[] data = IOStreamUtil.copyIStoByteArray(is);
                        return data == null ? Nil : new VncByteBuffer(ByteBuffer.wrap(data));
                    }
                    else {
                        final VncVal encVal = options.get(new VncKeyword("encoding"));
                        final Charset charset = CharsetUtil.charset(encVal);

                        return new VncString(IOStreamUtil.copyIStoString(is, charset));
                    }
                }
                catch (VncException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to slurp data from a :java.io.InputStream",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction io_slurp_reader =
        new VncFunction(
                "io/slurp-reader",
                VncFunction
                    .meta()
                    .arglists("(io/slurp-reader rd)")
                    .doc(
                        "Slurps string data from a `java.io.Reader` rd." +
                        "Note: \n\n" +
                        "`io/slurp-reader` offers the same functionality as `io/slurp` but it " +
                        "opens more flexibility with sandbox configuration. `io/slurp` can be " +
                        "blacklisted to prevent reading data from the filesystem and still having " +
                        "`io/slurp-reader` for readers input available!")
                    .examples(
                        "(do \n" +
                        "   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
                        "      (io/delete-file-on-exit file) \n" +
                        "      (io/spit file \"123456789\" :append true) \n" +
                        "      (try-with [rd (io/buffered-reader (io/file-in-stream file) :utf-8)] \n" +
                        "         (io/slurp-reader rd))) \n" +
                        ")")
                    .seeAlso(
                        "io/slurp", "io/slurp-lines", "io/spit",
                        "io/uri-stream",
                        "io/file-in-stream", "io/string-in-stream", "io/bytebuf-in-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                try {
                    final Reader rd = Coerce.toVncJavaObject(args.first(), Reader.class);

                    char[] buffer = new char[4096];
                    StringBuilder builder = new StringBuilder();
                    int numChars;

                    while ((numChars = rd.read(buffer)) >= 0) {
                        builder.append(buffer, 0, numChars);
                    }

                    return new VncString(builder.toString());
                }
                catch (VncException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to slurp data from a :java.io.Reader",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_spit_stream =
        new VncFunction(
                "io/spit-stream",
                VncFunction
                    .meta()
                    .arglists("(io/spit-stream os content & options)")
                    .doc(
                        "Writes content (string or bytebuf) to the `java.io.OutputStream` os. " +
                        "If content is of type string an optional encoding (defaults to " +
                        "UTF-8) is supported. The stream can optionally be flushed after " +
                        "the operation.\n\n" +
                        "Options: \n\n" +
                        "| :flush true/false | e.g.: :flush true, defaults to false |\n" +
                        "| :encoding enc     | e.g.: :encoding :utf-8, defaults to :utf-8 |\n\n" +
                        "Note: \n\n" +
                        "`io/spit-stream` offers the same functionality as `io/spit` but it " +
                        "opens more flexibility with sandbox configuration. `io/spit` can be " +
                        "blacklisted to prevent writing data to the filesystem and still having " +
                        "`io/spit-stream` for stream output available!")
                    .examples(
                        "(do \n" +
                        "   (let [file (io/temp-file \"test-\", \".txt\")]         \n" +
                        "      (io/delete-file-on-exit file)                       \n" +
                        "      (try-with [os (io/file-out-stream file)]            \n" +
                        "         (io/spit-stream os \"123456789\" :flush true)))) ")
                    .seeAlso("io/spit")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                try {
                    final OutputStream os = Coerce.toVncJavaObject(args.first(), OutputStream.class);

                    final VncVal content = args.second();

                    final VncHashMap options = VncHashMap.ofAll(args.slice(2));
                    final VncVal encVal = options.get(new VncKeyword("encoding"));
                    final Charset charset = CharsetUtil.charset(encVal);
                    final VncVal flushVal = options.get(new VncKeyword("flush"));
                    final boolean flush = VncBoolean.isTrue(flushVal);

                    byte[] data;

                    if (Types.isVncString(content)) {
                        data = ((VncString)content).getValue().getBytes(charset);
                    }
                    else if (Types.isVncByteBuffer(content)) {
                        data = ((VncByteBuffer)content).getBytes();
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'spit-stream' does not allow %s as content",
                                Types.getType(content)));
                    }

                    os.write(data);

                    if (flush) {
                        os.flush();
                    }

                    return Nil;
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to spit data to a :java.io.OutputStream",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_spit_writer =
        new VncFunction(
                "io/spit-writer",
                VncFunction
                    .meta()
                    .arglists("(io/spit-writer wr text)")
                    .doc(
                        "Writes text to the `java.io.Writer` wr. The writer can optionally be " +
                        "flushed after the operation.\n\n" +
                        "Options: \n\n" +
                        "| :flush true/false | e.g.: :flush true, defaults to false |\n" +
                        "Note: \n\n" +
                        "`io/spit-writer` offers the same functionality as `io/spit` but it " +
                        "opens more flexibility with sandbox configuration. `io/spit` can be " +
                        "blacklisted to prevent writing data to the filesystem and still having " +
                        "`io/spit-writer` for stream output available!")
                    .examples(
                        "(do \n" +
                        "   (let [file (io/temp-file \"test-\", \".txt\")          \n" +
                        "         os   (io/file-out-stream file)]                  \n" +
                        "      (io/delete-file-on-exit file)                       \n" +
                        "      (try-with [wr (io/buffered-writer os :utf-8)]       \n" +
                        "         (io/spit-writer wr \"123456789\" :flush true)))) ")
                    .seeAlso("io/spit")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                try {
                    final Writer wr = Coerce.toVncJavaObject(args.first(), Writer.class);

                    final String text = Coerce.toVncString(args.second()).getValue();

                    final VncHashMap options = VncHashMap.ofAll(args.slice(2));
                    final VncVal flushVal = options.get(new VncKeyword("flush"));
                    final boolean flush = VncBoolean.isTrue(flushVal);

                    wr.write(text);

                    if (flush) {
                        wr.flush();
                    }

                    return Nil;
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to write text to a :java.io.Writer",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    private static VncVal slurp(
            final VncHashMap options,
            final InputStream inStream
    ) throws Exception {
        final VncVal binary = options.get(new VncKeyword("binary"));

        try (InputStream is = inStream) {
            if (VncBoolean.isTrue(binary)) {
                final byte[] data = IOStreamUtil.copyIStoByteArray(is);
                return data == null ? Nil : new VncByteBuffer(ByteBuffer.wrap(data));
            }
            else {
                final VncVal encVal = options.get(new VncKeyword("encoding"));
                final Charset charset = CharsetUtil.charset(encVal);

                return new VncString(IOStreamUtil.copyIStoString(is, charset));
            }
        }
    }

    private static VncVal slurp(
            final VncHashMap options,
            final Reader rd
    ) throws Exception {
        final VncVal binary = options.get(new VncKeyword("binary"));

        try (BufferedReader brd = new BufferedReader(rd)) {
            final String s = brd.lines().collect(Collectors.joining(System.lineSeparator()));

            if (VncBoolean.isTrue(binary)) {
                final VncVal encVal = options.get(new VncKeyword("encoding"));
                final Charset charset = CharsetUtil.charset(encVal);

                return new VncByteBuffer(s.getBytes(charset));
            }
            else {
                return new VncString(s);
            }
        }
    }

    private static VncVal slurpLines(
            final VncHashMap options,
            final InputStream inStream
    ) throws Exception {
        final VncVal encVal = options.get(new VncKeyword("encoding"));
        final Charset charset = CharsetUtil.charset(encVal);

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(inStream, charset))) {
            return VncList.ofList(rd.lines().map(s -> new VncString(s)).collect(Collectors.toList()));
        }
    }

    private static VncVal slurpLines(
            final VncHashMap options,
            final Reader rd
    ) throws Exception {
        try (BufferedReader brd = new BufferedReader(rd)) {
            return VncList.ofList(brd.lines().map(s -> new VncString(s)).collect(Collectors.toList()));
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


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(io_slurp)
                    .add(io_slurp_lines)
                    .add(io_slurp_stream)
                    .add(io_slurp_reader)
                    .add(io_spit)
                    .add(io_spit_stream)
                    .add(io_spit_writer)
                    .toMap();
}
