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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncChar;
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
import com.github.jlangch.venice.impl.util.csv.CSVReader;
import com.github.jlangch.venice.impl.util.csv.CSVWriter;
import com.github.jlangch.venice.impl.util.io.CharsetUtil;


public class CsvFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // CSV
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction read =
        new VncFunction(
                "csv/read",
                VncFunction
                    .meta()
                    .arglists("(csv/read source & options)")
                    .doc(
                        "Reads CSV-data from a source.                                     \n\n" +
                        "The source may be a:                                              \n\n" +
                        " * `string`                                                       \n" +
                        " * `bytebuf`                                                      \n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")`            \n" +
                        " * `java.nio.Path`,                                  `            \n" +
                        " * `java.io.InputStream`                                          \n" +
                        " * `java.io.Reader`                                               \n\n" +
                        "Options:\n\n" +
                        "| :encoding enc  | used when reading from a binary data source " +
                        "                   e.g :encoding :utf-8, defaults to :utf-8 |\n" +
                        "| :separator val | e.g. \",\", defaults to a comma |\n" +
                        "| :quote val     | e.g. \"'\", defaults to a double quote |")
                    .examples(
                        "(csv/read \"1,\\\"ab\\\",false\")",
                        "(csv/read \"1:::'ab':false\" :separator \":\" :quote \"'\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                try {
                    final VncVal source = args.first();
                    final VncHashMap options = VncHashMap.ofAll(args.rest());
                    final char separator = toChar(options.get(new VncKeyword("separator")), ',');
                    final char quote = toChar(options.get(new VncKeyword("quote")), '"');

                    final CSVReader parser = new CSVReader(separator, quote);

                    if (Types.isVncString(source)) {
                        return map(parser.parse(((VncString)source).getValue()));
                    }
                    else if (Types.isVncJavaObject(source, File.class) || Types.isVncJavaObject(source, Path.class)) {
                    	// Delegate to 'io/file-in-stream' for sandbox validation
                        final InputStream fileIS = Coerce.toVncJavaObject(
                        							IOFunctionsStreams.io_file_in_stream.applyOf(source),
                        							InputStream.class);

                        final VncVal encVal = options.get(new VncKeyword("encoding"));

                        try(InputStream is = fileIS) {
                            return map(parser.parse(is, CharsetUtil.charset(encVal)));
                        }
                    }
                    else if (Types.isVncJavaObject(source, InputStream.class)) {
                        final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());

                        final VncVal encVal = options.get(new VncKeyword("encoding"));

                        try(InputStream is_ = is) {
                            return map(parser.parse(is_, CharsetUtil.charset(encVal)));
                        }
                    }
                    else if (Types.isVncJavaObject(source, Reader.class)) {
                        final Reader rd = (Reader)(Coerce.toVncJavaObject(args.first()).getDelegate());
                        try(Reader rd_ = rd) {
                            return map(parser.parse(rd_));
                        }
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'csv/read' does not allow %s as source",
                                Types.getType(args.first())));
                    }
                }
                catch (VncException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new VncException(ex.getMessage(), ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction write =
        new VncFunction(
                "csv/write",
                VncFunction
                    .meta()
                    .arglists("(csv/write sink records & options)")
                    .doc(
                        "Spits data to a sink in CSV format.\n\n" +
                        "The sink may be a:                                            \n\n" +
                        " * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")`       \n" +
                        " * `java.nio.Path`                                           \n" +
                        " * `java.io.OutputStream`                                    \n" +
                        " * `java.io.Writer`                                          \n\n" +
                        "Options:\n\n" +
                        "| :separator val | e.g. \",\", defaults to a comma |\n" +
                        "| :quote val     | e.g. \"'\", defaults to a double quote |\n" +
                        "| :newline val   | :lf (default) or :cr+lf |\n" +
                        "| :encoding enc  | used when writing to a binary data sink. " +
                        "                   e.g :encoding :utf-8, defaults to :utf-8 |")
                    .examples(
                        "(csv/write (io/file \"test.csv\") [[1 \"AC\" false] [2 \"WS\" true]])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                try {
                    final Object out = Coerce.toVncJavaObject(args.first()).getDelegate();

                    final VncHashMap options = VncHashMap.ofAll(args.rest().rest());
                    final char separator = toChar(options.get(new VncKeyword("separator")), ',');
                    final char quote = toChar(options.get(new VncKeyword("quote")), '"');
                    final String newline = toNewLine(options.get(new VncKeyword("newline")));
                    final String encoding = encoding(options.get(new VncKeyword("encoding")));

                    final CSVWriter csvWriter = new CSVWriter(separator, quote, newline);

                    if (out instanceof File || out instanceof Path) {
                    	// Delegate to 'io/file-out-stream' for sandbox validation
                        final OutputStream os = Coerce.toVncJavaObject(
                    							    IOFunctionsStreams.io_file_out_stream.applyOf(args.first()),
                    							    OutputStream.class);

                        try (Writer wr = new OutputStreamWriter(os, encoding)) {
                            csvWriter.write(wr, Coerce.toVncSequence(args.second()));
                            return Constants.Nil;
                        }
                    }
                    else if (out instanceof OutputStream) {
                        try (Writer wr = new OutputStreamWriter((OutputStream)out, encoding)) {
                            csvWriter.write(wr, Coerce.toVncSequence(args.second()));
                            return Constants.Nil;
                        }
                    }
                    else if (out instanceof Writer) {
                        try(Writer wr = (Writer)out) {
                            csvWriter.write(wr, Coerce.toVncSequence(args.second()));
                            return Constants.Nil;
                        }
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'csv/write' does not allow %s as sink",
                                Types.getType(args.first())));
                    }
                }
                catch (VncException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new VncException(ex.getMessage(), ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction write_str =
            new VncFunction(
                    "csv/write-str",
                    VncFunction
                        .meta()
                        .arglists("(csv/write-str records & options)")
                        .doc(
                            "Writes data to a string in CSV format.\n" +
                            "\n" +
                            "Options:\n\n" +
                            "| :separator val | e.g. \",\", defaults to a comma |\n" +
                            "| :quote val     | e.g. \"'\", defaults to a double quote |\n" +
                            "| :newline val   | :lf (default) or :cr+lf |")
                        .examples(
                            "(csv/write-str [[1 \"AC\" false] [2 \"WS\" true]])",
                            "(csv/write-str [[1 \"AC\" false] [2 \"WS, '-1'\" true]]\n" +
                            "               :quote \"'\"\n" +
                            "               :separator \",\"\n" +
                            "               :newline :cr+lf)")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertMinArity(this, args, 1);

                    try {
                        final VncHashMap options = VncHashMap.ofAll(args.rest());
                        final char separator = toChar(options.get(new VncKeyword("separator")), ',');
                        final char quote = toChar(options.get(new VncKeyword("quote")), '"');
                        final String newline = toNewLine(options.get(new VncKeyword("newline")));

                        final CSVWriter csvWriter = new CSVWriter(separator, quote, newline);

                        final StringWriter sw = new StringWriter();
                        csvWriter.write(sw, Coerce.toVncSequence(args.first()));
                        return new VncString(sw.toString());
                    }
                    catch (VncException ex) {
                        throw ex;
                    }
                    catch (Exception ex) {
                        throw new VncException(ex.getMessage(), ex);
                    }
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };


    private static VncList map(final List<List<String>> data) {
        final List<VncVal> vncRecords = new ArrayList<>();
        for(List<String> record : data) {
            final List<VncVal> vncRecord = new ArrayList<>();
            for(String s : record) {
                vncRecord.add(s == null ? Constants.Nil : new VncString(s));
            }
            vncRecords.add(VncList.ofList(vncRecord));
        }
        return VncList.ofList(vncRecords);
    }

    private static char toChar(final VncVal v, final char defaultChar) {
        if (v == Constants.Nil) {
            return defaultChar;
        }
        else if (Types.isVncChar(v)) {
            return ((VncChar)v).getValue();
        }
        else if (Types.isVncString(v)) {
            final String s = ((VncString)v).getValue();
            return s.isEmpty() ? defaultChar : s.charAt(0);
        }
        else {
            return defaultChar;
        }
    }

    private static String toNewLine(final VncVal v) {
        if (Types.isVncKeyword(v)) {
            final String s = ((VncKeyword)v).getValue();
            if (s.equals("lf")) return "\n";
            else if (s.equals("cr+lf")) return "\r\n";
            else return "\n";
        }
        else if (Types.isVncString(v)) {
            return ((VncString)v).getValue();
        }
        else {
            return "\n";
        }
    }

    private static String encoding(final VncVal enc) {
        return enc == Nil
                ? "UTF-8"
                : Types.isVncKeyword(enc)
                    ? Coerce.toVncKeyword(enc).getValue()
                    : Coerce.toVncString(enc).getValue();
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(read)
                    .add(write)
                    .add(write_str)
                    .toMap();
}
