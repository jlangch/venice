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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncLazySeq;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.MimeTypes;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.VncFileIterator;
import com.github.jlangch.venice.impl.util.VncPathMatcher;
import com.github.jlangch.venice.impl.util.http.BasicAuthentication;
import com.github.jlangch.venice.impl.util.io.CharsetUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.github.jlangch.venice.impl.util.io.InternetUtil;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;
import com.github.jlangch.venice.util.OS;
import com.github.jlangch.venice.util.StopWatch;

import net.lingala.zip4j.util.FileUtils;


public class IOFunctions {


    ///////////////////////////////////////////////////////////////////////////
    // I/O functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction io_file =
        new VncFunction(
                "io/file",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/file path)",
                        "(io/file parent child)",
                        "(io/file parent child & children)")
                    .doc(
                        "Returns a java.io.File from file path, or from a parent path " +
                        "and one or multiple children. The path and parent may be a file or a string " +
                        "(file path), child and children must be strings.")
                    .examples(
                        "(io/file \"/tmp/test.txt\")",
                        "(io/file \"/temp\" \"test.txt\")",
                        "(io/file \"/\" \"temp\" \"test\" \"test.txt\")",
                        "(io/file (io/file \"/\" \"temp\") \"test\" \"test.txt\")",
                        "(io/file (. :java.io.File :new \"/tmp/test.txt\"))",
                        ";; Windows:\n" +
                        ";;   (io/file \"C:\\\\tmp\\\\test.txt\") \n" +
                        ";;   (io/file \"C:/tmp/test.txt\")",
                        ";;   (io/file \"C:\" \"tmp\" \"test.txt\")")
                    .seeAlso(
                        "io/file-name",
                        "io/file-parent",
                        "io/file-path",
                        "io/file-absolute",
                        "io/file-canonical",
                        "str/normalize-utf")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                if (args.size() == 1) {
                    return new VncJavaObject(
                                    convertToFile(
                                        args.first(),
                                        "Function 'io/file' does not allow %s as path"));
                }
                else {
                    final File parent = convertToFile(
                                            args.first(),
                                            "Function 'io/file' does not allow %s as parent");

                    File file = parent;
                    for(VncVal child : args.rest()) {
                        file = new File(file, Coerce.toVncString(child).getValue());
                    }

                    return new VncJavaObject(file);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_size =
        new VncFunction(
                "io/file-size",
                VncFunction
                    .meta()
                    .arglists("(io/file-size f)")
                    .doc("Returns the size of the file f. f must be a file or a string (file path).")
                    .examples("(io/file-size \"/tmp/test.txt\")")
                    .seeAlso("io/file")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-size' does not allow %s as f");

                validateReadableFile(f);

                return new VncLong(f.length());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_path =
        new VncFunction(
                "io/file-path",
                VncFunction
                    .meta()
                    .arglists("(io/file-path f)")
                    .doc("Returns the path of the file f as a string. f must be a file or a string (file path).")
                    .examples("(io/file-path (io/file \"/tmp/test/x.txt\"))")
                    .seeAlso(
                        "io/file-absolute",
                        "io/file-canonical",
                        "io/file",
                        "str/normalize-utf")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-path' does not allow %s as f");

                return new VncString(f.getPath());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_path_slashify =
            new VncFunction(
                    "io/file-path-slashify",
                    VncFunction
                        .meta()
                        .arglists("(io/file-path-slashify f)")
                        .doc("Returns the path of the file f as a string, turns backslashes into slashes. \n\n" +
                             "f must be a file or a string (file path).\n\n" +
                             "C:\\Users\\foo\\image.png -> C:/Users/foo/image.png\n\n" +
                             "Note: Windows only. On other OSs works identical to 'io/file-path'.")
                        .examples("(io/file-path-slashify (io/file \"C:\" \"Users\" \"foo\" \"image.png\"))")
                        .seeAlso("io/file-path")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    final File f = convertToFile(
                            args.first(),
                            "Function 'io/file-path' does not allow %s as f");


                    if (OS.isWindows()) {
                        final String p = f.getPath().replace('\\', '/').replaceAll("//", "/");
                        return new VncString(p);
                    }
                    else {
                        return new VncString(f.getPath());
                    }
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction io_file_canonical =
        new VncFunction(
                "io/file-canonical",
                VncFunction
                    .meta()
                    .arglists("(io/file-canonical f)")
                    .doc("Returns the canonical path of the file f. f must be a file or a string (file path).")
                    .examples("(io/file-canonical (io/file \"/tmp/test/../x.txt\"))")
                    .seeAlso(
                        "io/file-path",
                        "io/file-absolute",
                        "io/file",
                        "str/normalize-utf")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                try {
                    final VncVal f = args.first();

                    if (Types.isVncString(f)) {
                        return new VncString(
                                    new File(((VncString)f).getValue())
                                        .getCanonicalFile()
                                        .getPath());
                    }
                    else if (Types.isVncJavaObject(f, File.class)) {
                        return new VncJavaObject(
                                Coerce.toVncJavaObject(f, File.class)
                                      .getCanonicalFile());
                    }
                    else if (Types.isVncJavaObject(f, Path.class)) {
                        return new VncJavaObject(
                                 Coerce.toVncJavaObject(f, Path.class)
                                       .toFile()
                                       .getCanonicalFile()
                                       .toPath());
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'io/file-canonical' does not allow %s as file arg",
                                Types.getType(f)));
                    }
                }
                catch(IOException ex) {
                    throw new VncException("Failed to get canonical file", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_absolute =
        new VncFunction(
                "io/file-absolute",
                VncFunction
                    .meta()
                    .arglists("(io/file-absolute f)")
                    .doc("Returns the absolute path of the file f. f must be a file or a string (file path).")
                    .examples("(io/file-absolute (io/file \"/tmp/test/x.txt\"))")
                    .seeAlso(
                        "io/file-path",
                        "io/file-canonical",
                        "io/file",
                        "io/file-absolute?",
                        "str/normalize-utf")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal f = args.first();

                if (Types.isVncString(f)) {
                    return new VncString(
                                new File(((VncString)f).getValue())
                                    .getAbsolutePath());
                }
                else if (Types.isVncJavaObject(f, File.class)) {
                    return new VncJavaObject(
                            Coerce.toVncJavaObject(f, File.class)
                                  .getAbsoluteFile());
                }
                else if (Types.isVncJavaObject(f, Path.class)) {
                    return new VncJavaObject(
                             Coerce.toVncJavaObject(f, Path.class)
                                   .toFile()
                                   .getAbsoluteFile()
                                   .toPath());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/file-absolute' does not allow %s as file arg",
                            Types.getType(f)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_absolute_Q =
        new VncFunction(
                "io/file-absolute?",
                VncFunction
                    .meta()
                    .arglists("(io/file-absolute? f)")
                    .doc("Returns true if file f has an absolute path else false. f must be a file or a string (file path).")
                    .examples("(io/file-absolute? (io/file \"/tmp/test/x.txt\"))")
                    .seeAlso("io/file-path", "io/file-canonical", "io/file", "io/file-absolute")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal f = args.first();

                if (Types.isVncString(f)) {
                    return VncBoolean.of(
                                new File(((VncString)f).getValue()).isAbsolute());
                }
                else if (Types.isVncJavaObject(f, File.class)) {
                    return VncBoolean.of(
                            Coerce.toVncJavaObject(f, File.class).isAbsolute());
                }
                else if (Types.isVncJavaObject(f, Path.class)) {
                    return VncBoolean.of(
                             Coerce.toVncJavaObject(f, Path.class)
                                   .isAbsolute());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/file-absolute?' does not allow %s as file arg",
                            Types.getType(f)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_parent =
        new VncFunction(
                "io/file-parent",
                VncFunction
                    .meta()
                    .arglists("(io/file-parent f)")
                    .doc("Returns the parent file of the file f. f must be a file or a string (file path).")
                    .examples("(io/file-path (io/file-parent (io/file \"/tmp/test/x.txt\")))")
                    .seeAlso("io/file-name", "io/file")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-parent' does not allow %s as f");

                final File parent = f.getParentFile();
                return parent == null ? Nil : new VncJavaObject(parent);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_name =
        new VncFunction(
                "io/file-name",
                VncFunction
                    .meta()
                    .arglists("(io/file-name f)")
                    .doc("Returns the name of the file f as a string. f must be a file or a string (file path).")
                    .examples("(io/file-name (io/file \"/tmp/test/x.txt\"))")
                    .seeAlso(
                        "io/file-basename",
                        "io/file-parent",
                        "io/file",
                        "str/normalize-utf")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-name' does not allow %s as f");

                return new VncString(f.getName());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction io_file_basename =
        new VncFunction(
                "io/file-basename",
                VncFunction
                    .meta()
                    .arglists("(io/file-basename f)")
                    .doc(
                        "Returns the base name (file name without file extension) "
                        + "of the file f as a string. f must be a file or a string "
                        + "(file path).")
                    .examples("(io/file-basename (io/file \"/tmp/test/x.txt\"))")
                    .seeAlso(
                        "io/file-name",
                        "io/file-parent",
                        "io/file-ext",
                        "io/file",
                        "str/normalize-utf")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-basename' does not allow %s as f");

                return new VncString(FileUtil.getFileBaseName(f.getName()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_to_path =
        new VncFunction(
                "io/->path",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/->path f)")
                    .doc(
                        "Converts to a :java.nio.Path. " +
                        "f must be a file or a string (file path).")
                    .examples(
                        "(io/->path \"some.txt\")",
                        "(io/->path \"/tmp/test/some.txt\")",
                        "(io/->path (io/file \"/tmp/test/some\"))")
                    .seeAlso("io/path?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Path p = convertToPath(
                                 args.first(),
                                 "Function 'io/->path' does not allow %s as f");

                return new VncJavaObject(p);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

        public static VncFunction io_path_Q =
            new VncFunction(
                    "io/path?",
                    VncFunction
                        .meta()
                        .arglists(
                            "(io/path f)")
                        .doc(
                            "Returns true if f is a :java.nio.Path.")
                        .examples(
                            "(io/path? (io/->path \"some.txt\"))")
                        .seeAlso("io/->path")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    return VncBoolean.of(Types.isVncJavaObject(args.first(), Path.class));
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };

    public static VncFunction io_file_ext_Q =
        new VncFunction(
                "io/file-ext?",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/file-ext? f ext & exts)")
                    .doc(
                        "Returns true if the file f hast the extension ext. " +
                        "f must be a file or a string (file path).")
                    .examples(
                        "(io/file-ext? \"/tmp/test/x.txt\" \"txt\")",
                        "(io/file-ext? (io/file \"/tmp/test/x.txt\") \".txt\")",
                        "(io/file-ext? \"/tmp/test/x.docx\" \"doc\" \"docx\")")
                    .seeAlso("io/file-ext")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-ext?' does not allow %s as f");

                if (args.size() == 2) {
                    final String ext = Coerce.toVncString(args.second()).getValue();
                    return VncBoolean.of(f.getName().endsWith(ext.startsWith(".") ? ext : "." + ext));
                }
                else {
                    final Set<String> exts = args.slice(1)
                                                  .stream()
                                                  .map(v -> Coerce.toVncString(args.second()).getValue())
                                                  .map(s -> s.startsWith(".") ? s.substring(1) : s)
                                                  .collect(Collectors.toSet());

                    final String fileExt = FileUtils.getFileExtension(f);

                    return VncBoolean.of(exts.contains(fileExt));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_ext =
        new VncFunction(
                "io/file-ext",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/file-ext f)")
                    .doc(
                        "Returns the file extension of a file. " +
                        "f must be a file or a string (file path).")
                    .examples(
                        "(io/file-ext \"some.txt\")",
                        "(io/file-ext \"/tmp/test/some.txt\")",
                        "(io/file-ext \"/tmp/test/some\")")
                    .seeAlso("io/file-ext?", "io/file-basename")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-ext' does not allow %s as f");

                final String ext = FileUtil.getFileExt(f.getName());
                return ext == null ? Nil : new VncString(ext);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_normalize_utf =
        new VncFunction(
                "io/file-normalize-utf",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/file-normalize-utf file)",
                        "(io/file-normalize-utf file form)")
                    .doc(
                        "Normalizes the UTF string of a file path.\n\n" +
                        "On MacOS file names with umlauts like ä are just encoded as 'a' " +
                        "plus the combining diaresis character. Therefore an 'ä' (\\u00FC) " +
                        "and an 'ä' (a + \\u0308) from a MacOS file name are different!\n" +
                        "Under normal circumstances this not problem. But as soon as some " +
                        "file name processing is in place (comparing, matching, ...) this " +
                        "can result in strange behaviour due of the two different technical " +
                        "representations of umlaut characters.\n\n" +
                        "The *form* argument defaults to :NFC and is one of:\n"+
                        "* :NFD  Canonical decomposition\n" +
                        "* :NFC  Canonical decomposition, followed by canonical composition\n" +
                        "* :NFKD  Compatibility decomposition\n" +
                        "* :NFKC  Compatibility decomposition, followed by canonical composition\n\n" +
                        "Returns an UTF normalized java.io.File from a file path\n\n" +
                        "See the function `str/normalize-utf` for details on UTF normalization.")
                    .examples(
                        "(io/file-normalize-utf \"/tmp/test_u\\u0308.txt\")",
                        "(io/file-normalize-utf (io/file \"/tmp/test_u\\u0308.txt\"))")
                     .seeAlso(
                        "str/normalize-utf")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);


                final File file = convertToFile(
                                     args.first(),
                                     "Function 'io/file-normalize-utf' does not allow %s as path");

                if (args.size() == 1) {
                    return new VncJavaObject(normalize(file, Form.NFC));
                }
                else {
                    final VncKeyword form = Coerce.toVncKeyword(args.second());

                    switch(form.getValue()) {
                        case "NFD":   return new VncJavaObject(normalize(file, Form.NFD));
                        case "NFC":   return new VncJavaObject(normalize(file, Form.NFC));
                        case "NFKD":  return new VncJavaObject(normalize(file, Form.NFKD));
                        case "NFKC":  return new VncJavaObject(normalize(file, Form.NFKC));
                        default:
                            throw new VncException(
                                        "Function 'io/file-normalize-utf' invalid form argument " + form + ". "
                                        + "Use one of {:NFD, :NFC, :NFKD, :NFKC}!");
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_Q =
        new VncFunction(
                "io/file?",
                VncFunction
                    .meta()
                    .arglists("(io/file? f)")
                    .doc("Returns true if x is a :java.io.File.")
                    .examples("(io/file? (io/file \"/tmp/test.txt\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncJavaObject(args.first(), File.class));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_exists_Q =
        new VncFunction(
                "io/exists?",
                VncFunction
                    .meta()
                    .arglists("(io/exists? f)")
                    .doc("Returns true if the file  or directory f exists. f must be a file or a string (file path).")
                    .examples("(io/exists? \"/tmp/test.txt\")")
                    .seeAlso("io/exists-file?", "io/exists-dir?", "io/symbolic-link?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/exists?' does not allow %s as f");

                return VncBoolean.of(f.exists());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_exists_file_Q =
        new VncFunction(
                "io/exists-file?",
                VncFunction
                    .meta()
                    .arglists("(io/exists-file? f)")
                    .doc("Returns true if the file f exists and is a file. f must be a file or a string (file path).")
                    .examples("(io/exists-file? \"/tmp/test.txt\")")
                    .seeAlso("io/exists-dir?", "io/symbolic-link?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/exists-file?' does not allow %s as f");

                return VncBoolean.of(f.isFile());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_exists_dir_Q =
        new VncFunction(
                "io/exists-dir?",
                VncFunction
                    .meta()
                    .arglists("(io/exists-dir? f)")
                    .doc(
                        "Returns true if the file f exists and is a directory. " +
                        "f must be a file or a string (file path).")
                    .examples(
                        "(io/exists-dir? (io/file \"/temp\"))")
                    .seeAlso(
                        "io/exists-file?", "io/symbolic-link?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/exists-dir?' does not allow %s as f");

                return VncBoolean.of(f.isDirectory());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_can_read_Q =
        new VncFunction(
                "io/file-can-read?",
                VncFunction
                    .meta()
                    .arglists("(io/file-can-read? f)")
                    .doc(
                        "Returns true if the file or directory f exists and can be read. " +
                        "f must be a file or a string (file path).")
                    .examples(
                        "(io/file-can-read? \"/tmp/test.txt\")")
                    .seeAlso(
                        "io/file-set-readable",
                        "io/file-can-write?",
                        "io/file-can-execute?",
                        "io/file-hidden?",
                        "io/symbolic-link?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-can-read?' does not allow %s as f");

                return VncBoolean.of((f.isFile() || f.isDirectory()) && f.canRead());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_can_write_Q =
        new VncFunction(
                "io/file-can-write?",
                VncFunction
                    .meta()
                    .arglists("(io/file-can-write? f)")
                    .doc(
                        "Returns true if the file or directory f exists and can be written. " +
                        "f must be a file or a string (file path).")
                    .examples(
                        "(io/file-can-write? \"/tmp/test.txt\")")
                    .seeAlso(
                        "io/file-set-writable",
                        "io/file-can-read?",
                        "io/file-can-execute?",
                        "io/file-hidden?",
                        "io/symbolic-link?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-can-write?' does not allow %s as f");

                return VncBoolean.of((f.isFile() || f.isDirectory()) && f.canWrite());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_can_execute_Q =
        new VncFunction(
                "io/file-can-execute?",
                VncFunction
                    .meta()
                    .arglists("(io/file-can-execute? f)")
                    .doc(
                        "Returns true if the file or directory f exists and can be executed. " +
                        "f must be a file or a string (file path).")
                    .examples(
                        "(io/file-can-execute? \"/tmp/test.txt\")")
                    .seeAlso(
                        "io/file-set-executable",
                        "io/file-can-read?",
                        "io/file-can-write?",
                        "io/file-hidden?",
                        "io/symbolic-link?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-can-execute?' does not allow %s as f");

                return VncBoolean.of((f.isFile() || f.isDirectory()) && f.canExecute());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_set_readable =
        new VncFunction(
                "io/file-set-readable",
                VncFunction
                    .meta()
                    .arglists("(io/file-set-readable f readable owner-only)")
                    .doc(
                        "Set the owner’s read permission to the file or directory f. " +
                        "f must be a file or a string (file path)." +
                        "\n\n" +
                        "Returns true if and only if the operation succeeded. The " +
                        "operation will fail if the user does not have permission to " +
                        "change the access permissions of this abstract pathname.  If " +
                        "'readable' is false and the underlying file system does not " +
                        "implement a read permission, then the operation will fail." +
                        "\n\n" +
                        "If 'readable' is true sets the access permission to allow read " +
                        "operations; if false to disallow read operations. " +
                        "\n\n" +
                        "If 'owner-only' is true the read permission applies only to the " +
                        "owner's read permission; otherwise, it applies to everybody. If " +
                        "the underlying file system can not distinguish the owner's read " +
                        "permission from that of others, then the permission will apply to " +
                        "everybody, regardless of this value.")
                    .examples(
                        "(io/file-set-readable \"/tmp/test.txt\" true true)")
                    .seeAlso(
                        "io/file-can-read?",
                        "io/file-set-writable",
                        "io/file-set-executable")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-set-readable' does not allow %s as f");

                final boolean on = Coerce.toVncBoolean(args.second()).getValue();
                final boolean ownerOnly = Coerce.toVncBoolean(args.third()).getValue();

                return VncBoolean.of(f.setReadable(on, ownerOnly));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_set_writable =
        new VncFunction(
                "io/file-set-writable",
                VncFunction
                    .meta()
                    .arglists("(io/file-set-writable f writable owner-only)")
                    .doc(
                        "Set the owner’s write permission to the file or directory f. " +
                        "f must be a file or a string (file path)." +
                        "\n\n" +
                        "Returns true if and only if the operation succeeded. The " +
                        "operation will fail if the user does not have permission to " +
                        "change the access permissions of this abstract pathname.  If " +
                        "'writable' is false and the underlying file system does not " +
                        "implement a read permission, then the operation will fail." +
                        "\n\n" +
                        "If 'writable' is true sets the access permission to allow write " +
                        "operations; if false to disallow write operations. " +
                        "\n\n" +
                        "If 'owner-only' is true the write permission applies only to the " +
                        "owner's write permission; otherwise, it applies to everybody. If " +
                        "the underlying file system can not distinguish the owner's write " +
                        "permission from that of others, then the permission will apply to " +
                        "everybody, regardless of this value.")
                    .examples(
                        "(io/file-set-writable \"/tmp/test.txt\" true true)")
                    .seeAlso(
                        "io/file-can-write?",
                        "io/file-set-readable",
                        "io/file-set-executable")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-set-readable' does not allow %s as f");

                final boolean on = Coerce.toVncBoolean(args.second()).getValue();
                final boolean ownerOnly = Coerce.toVncBoolean(args.third()).getValue();

                return VncBoolean.of(f.setWritable(on, ownerOnly));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction io_file_set_executable =
        new VncFunction(
                "io/file-set-executable",
                VncFunction
                    .meta()
                    .arglists("(io/file-set-executable f executable owner-only)")
                    .doc(
                        "Set the owner’s execute permission to the file or directory f. " +
                        "f must be a file or a string (file path)." +
                        "\n\n" +
                        "Returns true if and only if the operation succeeded. The " +
                        "operation will fail if the user does not have permission to " +
                        "change the access permissions of this abstract pathname.  If " +
                        "'readable' is false and the underlying file system does not " +
                        "implement a read permission, then the operation will fail." +
                        "\n\n" +
                        "If 'executable' is true sets the access permission to allow execute " +
                        "operations; if false to disallow execute operations. " +
                        "\n\n" +
                        "If 'owner-only' is true the execute permission applies only to the " +
                        "owner's execute permission; otherwise, it applies to everybody. If " +
                        "the underlying file system can not distinguish the owner's execute " +
                        "permission from that of others, then the permission will apply to " +
                        "everybody, regardless of this value.")
                    .examples(
                        "(io/file-set-executable \"/tmp/test.txt\" true true)")
                    .seeAlso(
                        "io/file-can-execute?",
                        "io/file-set-readable",
                        "io/file-set-writable")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-set-executable' does not allow %s as f");

                final boolean on = Coerce.toVncBoolean(args.second()).getValue();
                final boolean ownerOnly = Coerce.toVncBoolean(args.third()).getValue();

                return VncBoolean.of(f.setExecutable(on, ownerOnly));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_hidden_Q =
        new VncFunction(
                "io/file-hidden?",
                VncFunction
                    .meta()
                    .arglists("(io/file-hidden? f)")
                    .doc(
                        "Returns true if the file or directory f exists and is hidden. " +
                        "f must be a file or a string (file path).")
                    .examples(
                        "(io/file-hidden? \"/tmp/test.txt\")")
                    .seeAlso(
                        "io/file-can-read?",
                        "io/file-can-write?",
                        "io/file-can-execute?",
                        "io/symbolic-link?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-hidden?' does not allow %s as f");

                return VncBoolean.of((f.isFile() || f.isDirectory()) && f.isHidden());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_symbolic_link_Q =
        new VncFunction(
                "io/symbolic-link?",
                VncFunction
                    .meta()
                    .arglists("(io/symbolic-link? f)")
                    .doc(
                        "Returns true if the file f exists and is a symbolic link. " +
                        "f must be a file or a string (file path).")
                    .examples(
                        "(io/symbolic-link? \"/tmp/test.txt\")")
                    .seeAlso(
                        "io/file-hidden?",
                        "io/file-can-read?",
                        "io/file-can-write?",
                        "io/file-can-execute?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/symbolic-link?' does not allow %s as f");

                final Path p = f.toPath();

                return VncBoolean.of(Files.isSymbolicLink(p));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_create_symbolic_link =
        new VncFunction(
                "io/create-symbolic-link",
                VncFunction
                    .meta()
                    .arglists("(io/create-symbolic-link link target)")
                    .doc(
                        "Creates a symbolic link to a target. \n" +
                        "link and target must be a file or a string (file path).")
                    .examples(
                        "(io/create-symbolic-link \"/tmp/sym-link\" \"/tmp/test.txt\")")
                    .seeAlso(
                        "io/create-hard-link",
                        "io/symbolic-link?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final File link = convertToFile(
                                    args.first(),
                                    "Function 'io/create-symbolic-link' does not allow %s as link");

                final File target = convertToFile(
                                        args.second(),
                                        "Function 'io/create-symbolic-link' does not allow %s as target");

                try {
                    Files.createSymbolicLink(link.toPath(), target.toPath());
                    return Nil;
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format("Failed to create symbolic link %s -> %s", link, target),
                            ex);

                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_create_hard_link =
        new VncFunction(
                "io/create-hard-link",
                VncFunction
                    .meta()
                    .arglists("(io/create-hard-link link target)")
                    .doc(
                        "Creates a hard link to a target. \n" +
                        "link and target must be a file or a string (file path).")
                    .examples(
                         "(io/create-hard-link \"/tmp/hard-link\" \"/tmp/test.txt\")")
                    .seeAlso(
                        "io/create-symbolic-link",
                        "io/symbolic-link?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final File link = convertToFile(
                                    args.first(),
                                    "Function 'io/create-hard-link' does not allow %s as link");

                final File target = convertToFile(
                                        args.second(),
                                        "Function 'io/create-hard-link' does not allow %s as target");

                try {
                    Files.createLink(link.toPath(), target.toPath());
                    return Nil;
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format("Failed to create hard link %s -> %s", link, target),
                            ex);

                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_last_modified =
        new VncFunction(
                "io/file-last-modified",
                VncFunction
                    .meta()
                    .arglists("(io/file-last-modified f)")
                    .doc(
                        "Returns the last modification time (a Java LocalDateTime) of f or nil " +
                        "if f does not exist. f must be a file or a string (file path).")
                    .examples(
                        "(io/file-last-modified \"/tmp/test.txt\")")
                    .seeAlso(
                        "io/file-can-read?",
                        "io/file-can-write?",
                        "io/file-can-execute?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final File f = convertToFile(
                                    args.first(),
                                    "Function 'io/file-last-modified' does not allow %s as f");

                if (f.exists()) {
                    final long millis = f.lastModified();
                    return new VncJavaObject(
                                    Instant.ofEpochMilli(millis)
                                           .atZone(ZoneId.systemDefault())
                                           .toLocalDateTime());
                }
                else {
                    return Nil;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_within_dir_Q =
        new VncFunction(
                "io/file-within-dir?",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/file-within-dir? dir file)")
                    .doc(
                        "Returns true if the file is within the dir else false.\n\n" +
                        "The file and dir args must be absolute paths.")
                    .examples(
                        "(io/file-within-dir? (io/file \"/temp/foo\")          \n" +
                        "                     (io/file \"/temp/foo/img.png\")) ",
                        "(io/file-within-dir? (io/file \"/temp/foo\")                 \n" +
                        "                     (io/file \"/temp/foo/../bar/img.png\")) ")
                    .seeAlso(
                        "io/file")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Path dir = convertToPath(args.first());
                final Path file = convertToPath(args.second());

                if (file == null) {
                    throw new VncException(
                            String.format(
                                "Function 'io/file-within-dir?' does not allow %s as file arg",
                                Types.getType(args.second())));
                }

                if (dir == null) {
                    throw new VncException(
                            String.format(
                                "Function 'io/file-within-dir?' does not allow %s as dir arg",
                                Types.getType(args.first())));
                }

                if (!file.isAbsolute()) {
                    throw new VncException(
                            "Function 'io/file-within-dir?' required an absolute path for file");
                }

                if (!dir.isAbsolute()) {
                    throw new VncException(
                            "Function 'io/file-within-dir?' required an absolute path for dir");
                }

                return VncBoolean.of(file.normalize().startsWith(dir.normalize()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_glob_path_matcher =
        new VncFunction(
                "io/glob-path-matcher",
                VncFunction
                    .meta()
                    .arglists("(io/glob-path-matcher pattern)")
                    .doc(
                        "Returns a file matcher for glob file patterns." +
                        "\n\n" +
                        globPatternHelp())
                    .examples(
                        "(io/glob-path-matcher \"*.log\")",
                        "(io/glob-path-matcher \"**/*.log\")")
                    .seeAlso(
                        "io/file-matches-glob?", "io/list-files-glob")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final String searchPattern = Coerce.toVncString(args.first()).getValue();

                final PathMatcher m = FileSystems.getDefault()
                                                 .getPathMatcher("glob:" + searchPattern);

                return new VncJavaObject(new VncPathMatcher(m));
             }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_file_matches_globQ =
        new VncFunction(
                "io/file-matches-glob?",
                VncFunction
                    .meta()
                    .arglists("(io/file-matches-glob? glob f)")
                    .doc(
                        "Returns true if the file f matches the glob pattern. f must be a file or a string (file path)." +
                        "\n\n" +
                        globPatternHelp())
                    .examples(
                        "(io/file-matches-glob? \"*.log\" \"file.log\")",
                        "(io/file-matches-glob? \"**/*.log\" \"x/y/file.log\")",
                        "(io/file-matches-glob? \"**/*.log\" \"file.log\") ; take care, doesn't match!",
                        "(io/file-matches-glob? (io/glob-path-matcher \"*.log\") (io/file \"file.log\"))",
                        "(io/file-matches-glob? (io/glob-path-matcher \"**/*.log\") (io/file \"x/y/file.log\"))")
                    .seeAlso(
                        "io/glob-path-matcher", "io/list-files-glob")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                PathMatcher m = null;

                if (Types.isVncString(args.first())) {
                    final String searchPattern = Coerce.toVncString(args.first()).getValue();
                    m = FileSystems.getDefault()
                                   .getPathMatcher("glob:" + searchPattern);
                }
                else if (Types.isVncJavaObject(args.first(),VncPathMatcher.class)) {
                    m = Coerce.toVncJavaObject(args.first(),VncPathMatcher.class)
                              .getPathMatcher();
                }
                else {
                    throw new VncException(
                            String.format(
                                    "Function 'io/file-matches-glob?' does not allow %s as argument one",
                                    args.first().getType()));
                }

                final File f = convertToFile(
                                    args.second(),
                                    "Function 'io/file-matches-glob?' does not allow %s as f");

                return VncBoolean.of(m.matches(f.toPath()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_to_url =
        new VncFunction(
                "io/->url",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/->url s)",
                        "(io/->url protocol host port file)")
                    .doc(
                        "Converts s to an URL or builds an URL from its spec elements. \n\n" +
                        "s may be:                   \n\n" +
                        "  * a string (a spec string to be parsed as a URL.)    \n" +
                        "  * a `java.io.File`          \n" +
                        "  * a `java.nio.file.Path`    \n" +
                        "  * a `java.net.URI`\n\n" +
                        "Arguments:¶" +
                        "\u2001**protocol**\u2003the name of the protocol to use.¶" +
                        "\u2001**host**\u2003the name of the host.¶" +
                        "\u2001**port**\u2003the port number on the host.¶" +
                        "\u2001**file**\u2003the file on the host")
                    .examples(
                        "(io/->url \"file:/tmp/test.txt\")",
                        "(io/->url (io/file \"/tmp/test.txt\"))",
                        "(io/->url (io/->uri (io/file \"/tmp/test.txt\")))",
                        "(str (io/->url (io/file \"/tmp/test.txt\")))",
                        ";; to create an URL from spec details: \n" +
                        "(io/->url \"http\" \"foo.org\" 8080 \"/info.html\")")
                    .seeAlso(
                        "io/file", "io/->uri")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 4);

                try {
                    if (args.size() == 1) {
                        final VncVal f = args.first();

                        if (Types.isVncString(f)) {
                            return new VncJavaObject(new URL(((VncString)f).getValue()));
                        }
                        else if (Types.isVncJavaObject(f, File.class)) {
                            final File file = (File)((VncJavaObject)f).getDelegate();
                            return new VncJavaObject(file.toURI().toURL());
                        }
                        else if (Types.isVncJavaObject(f, Path.class)) {
                            final Path path = (Path)((VncJavaObject)f).getDelegate();
                            return new VncJavaObject(path.toUri().toURL());
                        }
                        else if (Types.isVncJavaObject(args.first(), URL.class)) {
                            return args.first();
                        }
                        else if (Types.isVncJavaObject(args.first(), URI.class)) {
                            final VncJavaObject obj = (VncJavaObject)args.first();
                            return new VncJavaObject(((URI)obj.getDelegate()).toURL());
                        }
                        else {
                            throw new VncException("Function 'io/->url' does not allow %s as argument");
                        }
                    }
                    else {
                        final VncVal protocol = args.nth(0);
                        final VncVal host = args.nth(1);
                        final VncVal port = args.nth(2);
                        final VncVal file = args.nth(3);

                        return new VncJavaObject(
                                    new URL(protocol == Nil ? null : Coerce.toVncString(protocol).getValue(),
                                            host == Nil     ? null : Coerce.toVncString(host).getValue(),
                                            port == Nil     ? -1   : Coerce.toVncLong(port).getIntValue(),
                                            file == Nil     ? null : Coerce.toVncString(file).getValue()));
                    }
                }
                catch(MalformedURLException ex) {
                    throw new VncException("Malformed URL: " + ex.getMessage(), ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_to_uri =
        new VncFunction(
                "io/->uri",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/->uri s)",
                        "(io/->uri scheme user-info host port path)",
                        "(io/->uri scheme user-info host port path query)",
                        "(io/->uri scheme user-info host port path query fragment)")
                    .doc(
                        "Converts s to an URI or builds an URI from its spec elements.\n\n" +
                        "s may be:                   \n\n" +
                        "  * a string (a spec string to be parsed as a URI.)    \n" +
                        "  * a `java.io.File`          \n" +
                        "  * a `java.nio.file.Path`    \n" +
                        "  * a `java.net.URL`        \n\n" +
                        "Arguments:¶" +
                        "\u2001**scheme**\u2003Scheme name¶" +
                        "\u2001**userInfo**\u2003User name and authorization information¶" +
                        "\u2001**host**\u2003Host name¶" +
                        "\u2001**port**\u2003Port number¶" +
                        "\u2001**path**\u2003Path¶" +
                        "\u2001**query**\u2003Query¶" +
                        "\u2001**fragment**\u2003Fragment")
                    .examples(
                        "(io/->uri \"file:/tmp/test.txt\")",
                        "(io/->uri (io/file \"/tmp/test.txt\"))",
                        "(io/->uri (io/->url (io/file \"/tmp/test.txt\")))",
                        "(str (io/->uri (io/file \"/tmp/test.txt\")))",
                        ";; to create an URL from spec details: \n" +
                        "(io/->uri \"http\" nil \"foo.org\" 8080 \"/info.html\" nil nil)")
                    .seeAlso(
                        "io/file", "io/->url")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 5, 6, 7);


                try {
                    if (args.size() == 1) {
                        final VncVal f = args.first();

                        if (Types.isVncString(f)) {
                            return new VncJavaObject(new URI(((VncString)f).getValue()));
                        }
                        else if (Types.isVncJavaObject(f, File.class)) {
                            final File file = (File)((VncJavaObject)f).getDelegate();
                            return new VncJavaObject(file.toURI());
                        }
                        else if (Types.isVncJavaObject(f, Path.class)) {
                            final Path path = (Path)((VncJavaObject)f).getDelegate();
                            return new VncJavaObject(path.toUri());
                        }
                        else if (Types.isVncJavaObject(args.first(), URI.class)) {
                            return args.first();
                        }
                        else if (Types.isVncJavaObject(args.first(), URL.class)) {
                            final VncJavaObject obj = (VncJavaObject)args.first();
                            return new VncJavaObject(((URL)obj.getDelegate()).toURI());
                        }
                        else {
                            throw new VncException(
                                    String.format(
                                            "Function 'io/->uri' does not allow %s as argument",
                                            f.getType()));
                        }
                    }
                    else {
                        final VncVal scheme = args.nth(0);
                        final VncVal userInfo = args.nth(1);
                        final VncVal host = args.nth(2);
                        final VncVal port = args.nth(3);
                        final VncVal path = args.nth(4);
                        final VncVal query = args.nthOrDefault(5, Nil);
                        final VncVal fragment = args.nthOrDefault(6, Nil);

                        return new VncJavaObject(
                                new URI(scheme == Nil   ? null : Coerce.toVncString(scheme).getValue(),
                                        userInfo == Nil ? null : Coerce.toVncString(userInfo).getValue(),
                                        host == Nil     ? null : Coerce.toVncString(host).getValue(),
                                        port == Nil     ? -1   : Coerce.toVncLong(port).getIntValue(),
                                        path == Nil     ? null : Coerce.toVncString(path).getValue(),
                                        query == Nil    ? null : Coerce.toVncString(query).getValue(),
                                        fragment == Nil ? null : Coerce.toVncString(fragment).getValue()));
                    }
                }
                catch(URISyntaxException ex) {
                    throw new VncException("Malformed URI: " + ex.getMessage(), ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_delete_file =
        new VncFunction(
                "io/delete-file",
                VncFunction
                    .meta()
                    .arglists("(io/delete-file f & files)")
                    .doc(
                        "Deletes one or multiple files. Silently skips delete if the file " +
                        "does not exist. If f is a directory the directory must be empty. " +
                        "f must be a file or a string (file path).")
                    .seeAlso(
                        "io/delete-files-glob",
                        "io/delete-file-tree",
                        "io/delete-file-on-exit",
                        "io/copy-file",
                        "io/move-file")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 0);

                final IInterceptor interceptor = sandboxFunctionCallValidation();

                final ILoadPaths loadpaths = interceptor.getLoadPaths();

                for(VncVal f : args) {
                    try {
                        final File file = convertToFile(
                                            f,
                                            "Function 'io/delete-file' does not allow %s as f");

                        final Path path = loadpaths.normalize(file).toPath();

                        Files.deleteIfExists(path);
                    }
                    catch(Exception ex) {
                        throw new VncException(
                                String.format("Failed to delete file %s", f.toString()),
                                ex);
                    }
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_delete_file_tree =
        new VncFunction(
                "io/delete-file-tree",
                VncFunction
                    .meta()
                    .arglists("(io/delete-file-tree f & files)")
                    .doc(
                        "Deletes a file or a directory with all its content. Silently skips delete if " +
                        "the file or directory does not exist. f must be a file or a string (file path)")
                    .seeAlso(
                        "io/delete-files-glob",
                        "io/delete-file",
                        "io/delete-file-on-exit")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                args.forEach(f -> {
                    final File file = convertToFile(
                                        f,
                                        "Function 'io/delete-file-tree' does not allow %s as f");

                    if (file.isDirectory()) {
                        try {
                            Files.walk(file.toPath())
                                 .sorted(Comparator.reverseOrder())
                                 .map(Path::toFile)
                                 .forEach(File::delete);
                        }
                        catch(Exception ex) {
                            throw new VncException(
                                    String.format("Failed to delete file tree from dir %s", file.toString()),
                                    ex);
                        }

                        // if the directory still exists -> failed to delete
                        if (file.isDirectory()) {
                            throw new VncException(
                                    String.format("Failed to delete file tree from dir %s", file.toString()));
                        }
                    }
                    else if (file.isFile()) {
                        if (!file.delete()) {
                            throw new VncException(
                                    String.format("Failed to delete file %s", file.toString()));
                        }
                    }
                    else {
                        // ignore
                    }
                });

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_delete_file_on_exit =
        new VncFunction(
                "io/delete-file-on-exit",
                VncFunction
                    .meta()
                    .arglists("(io/delete-file-on-exit f & fs)")
                    .doc(
                       "Requests that the files or directories be deleted when the virtual machine " +
                       "terminates. Files (or directories) are deleted in the reverse order that " +
                       "they are registered. Invoking this method to delete a file or directory " +
                       "that is already registered for deletion has no effect. Deletion will be " +
                       "attempted only for normal termination of the virtual machine, as defined " +
                       "by the Java Language Specification.\n\n" +
                       "f must be a file or a string (file path).")
                    .examples(
                        "(let [file1 (io/temp-file \"test-\", \".data\")    \n" +
                        "      file2 (io/temp-file \"test-\", \".data\")]   \n" +
                        "  (io/delete-file-on-exit file1 file2)             \n" +
                        "  (io/spit file1 \"123\")                          \n" +
                        "  (io/spit file2 \"ABC\"))                         ")
                    .seeAlso(
                        "io/delete-file",
                        "io/delete-file-tree",
                        "io/delete-files-glob")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                args.forEach(arg -> {
                    final File file = convertToFile(
                                        arg,
                                        "Function 'io/delete-file-on-exit' does not allow %s as f");

                    validateReadableFileOrDirectory(file);

                    try {
                        file.deleteOnExit();
                    }
                    catch(Exception ex) {
                        throw new VncException(
                                String.format(
                                        "Failed to mark file %s to be deleted on exit",
                                        file.getPath()),
                                ex);
                    }
                });

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_list_files =
        new VncFunction(
                "io/list-files",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/list-files dir)",
                        "(io/list-files dir filter-fn)")
                    .doc(
                        "Lists files in a directory. dir must be a file or a string (file path). " +
                        "`filter-fn` is an optional filter that filters the files found. The filter " +
                        "gets a `java.io.File` as argument. \n\n" +
                        "Returns files as `java.io.File`")
                    .examples(
                        "(io/list-files \"/tmp\")",
                        "(io/list-files \"/tmp\" #(io/file-ext? % \".log\"))")
                    .seeAlso("io/list-files-glob", "io/list-file-tree", "io/list-file-tree-lazy")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                sandboxFunctionCallValidation();

                final File dir = convertToFile(
                                    args.first(),
                                    "Function 'io/list-files' does not allow %s as dir");

                validateReadableDirectory(dir);

                try {
                    final VncFunction filterFn = args.size() == 2
                                                    ? Coerce.toVncFunction(args.second())
                                                    : null;
                    if (filterFn != null) {
                        filterFn.sandboxFunctionCallValidation();
                    }

                    final List<VncVal> files = new ArrayList<>();
                    for(File f : dir.listFiles()) {
                        if (filterFn == null || VncBoolean.isTrue(filterFn.apply(VncList.of(new VncJavaObject(f))))) {
                            files.add(new VncJavaObject(f));
                        }
                    }

                    return VncList.ofList(files);
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format("Failed to list files %s", dir.getPath()),
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_list_file_tree =
        new VncFunction(
                "io/list-file-tree",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/list-file-tree dir)",
                        "(io/list-file-tree dir filter-fn)")
                    .doc(
                        "Lists all files in a directory tree. dir must be a file or a " +
                        "string (file path). `filter-fn` is an optional filter that filters " +
                        "the files found. The filter gets a `java.io.File` as argument. \n\n" +
                        "Returns files as `java.io.File`")
                    .examples(
                        "(io/list-file-tree \"/tmp\")",
                        "(io/list-file-tree \"/tmp\" #(io/file-ext? % \".log\"))")
                    .seeAlso("io/list-file-tree-lazy", "io/list-files", "io/list-files-glob")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                sandboxFunctionCallValidation();

                final File dir = convertToFile(
                                    args.first(),
                                    "Function 'io/list-file-tree' does not allow %s as dir");

                validateReadableDirectory(dir);

                try {
                    final VncFunction filterFn = (args.size() == 2) ? Coerce.toVncFunction(args.second()) : null;
                    if (filterFn != null) {
                        filterFn.sandboxFunctionCallValidation();
                    }

                    final List<VncVal> files = new ArrayList<>();
                    Files.walk(dir.toPath())
                         .map(Path::toFile)
                         .forEach(f -> {
                            if (filterFn == null || VncBoolean.isTrue(filterFn.apply(VncList.of(new VncJavaObject(f))))) {
                                files.add(new VncJavaObject(f));
                            }
                         });

                    return VncList.ofList(files);
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format("Failed to list files from %s", dir.getPath()),
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_list_file_tree_lazy =
        new VncFunction(
                "io/list-file-tree-lazy",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/list-file-tree-lazy dir)",
                        "(io/list-file-tree-lazy dir filter-fn)")
                    .doc(
                        "Returns a lazy sequence of all the files in a directory tree. " +
                        "dir must be a file or a string (file path). `filter-fn` is an optional " +
                        "filter that filters the files found. The filter gets a `java.io.File` " +
                        "as argument. \n\n" +
                        "The lazy sequence returns files as `java.io.File`")
                    .examples(
                        "(->> (io/list-file-tree-lazy \"/tmp\")  \n" +
                        "     (docoll println))                  ",
                        "(->> (io/list-file-tree-lazy \"/tmp\" #(io/file-ext? % \".log\"))  \n" +
                        "     (docoll println))                                             ")
                    .seeAlso("io/list-file-tree", "io/list-files", "io/list-files-glob")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                sandboxFunctionCallValidation();

                final File dir = convertToFile(
                                    args.first(),
                                    "Function 'io/list-file-tree-lazy' does not allow %s as dir");

                validateReadableDirectory(dir);

                try {
                    final VncFunction filterFn = (args.size() == 2) ? Coerce.toVncFunction(args.second()) : null;
                    if (filterFn != null) {
                        filterFn.sandboxFunctionCallValidation();
                    }

                    return VncLazySeq.ofAll(new VncFileIterator(dir, filterFn), Nil);
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format("Failed to list files from %s", dir.getPath()),
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_list_files_glob =
        new VncFunction(
                "io/list-files-glob",
                VncFunction
                    .meta()
                    .arglists("(io/list-files-glob dir glob)")
                    .doc(
                        "Lists all files in a directory that match the glob pattern. " +
                        "dir must be a file or a string (file path). \n" +
                        "Returns files as `java.io.File`" +
                        "\n\n" +
                        globPatternHelp())
                    .examples(
                        "(io/list-files-glob \".\" \"sample*.txt\")")
                    .seeAlso("io/list-files", "io/list-file-tree", "io/list-file-tree-lazy")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                sandboxFunctionCallValidation();

                final File dir = convertToFile(
                                    args.first(),
                                    "Function 'io/list-files-glob' does not allow %s as dir");

                final String glob = Coerce.toVncString(args.second()).getValue();

                validateReadableDirectory(dir);

                final List<VncVal> files = new ArrayList<>();

                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath(), glob)) {
                    dirStream.forEach(path -> files.add(new VncJavaObject(path.toFile())));
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format("Failed to list files %s", dir.getPath()),
                            ex);
                }

                return VncList.ofList(files);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_delete_files_glob =
        new VncFunction(
                "io/delete-files-glob",
                VncFunction
                    .meta()
                    .arglists("(io/delete-files-glob dir glob)")
                    .doc(
                        "Removes all files in a directory that match the glob pattern. " +
                        "dir must be a file or a string (file path)." +
                        "\n\n" +
                        globPatternHelp())
                   .examples(
                        "(io/delete-files-glob \".\" \"*.log\")")
                    .seeAlso(
                        "io/delete-file",
                        "io/delete-file-tree",
                        "io/move-files-glob",
                        "io/copy-files-glob",
                        "io/list-files-glob")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                sandboxFunctionCallValidation();

                final File dir = convertToFile(
                                    args.first(),
                                    "Function 'io/delete-files-glob' does not allow %s as dir");

                final String glob = Coerce.toVncString(args.second()).getValue();

                validateReadableDirectory(dir);

                final List<VncVal> files = new ArrayList<>();

                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath(), glob)) {
                    dirStream.forEach(path -> {
                        files.add(new VncJavaObject(path.toFile()));
                        try {
                            Files.delete(path);
                        }
                        catch(IOException ex) {
                            throw new VncException(
                                    String.format("Failed to delete file %s", path),
                                    ex);
                                }
                    });
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format("Failed to delete files from %s, glob: %s ", dir.getPath(), glob),
                            ex);
                }

                return VncList.ofList(files);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_copy_file =
        new VncFunction(
                "io/copy-file",
                VncFunction
                    .meta()
                    .arglists("(io/copy-file source dest & options)")
                    .doc(
                        "Copies source to dest. Returns nil or throws a VncException. " +
                        "Source must be a file or a string (file path), dest must be a file, " +
                        "a string (file path), or an `java.io.OutputStream`." +
                        "\n\n" +
                        "Options: \n\n" +
                        "| [![width: 25%]] | [![width: 75%]] |\n" +
                        "| :replace true/false | e.g.: if true replace an existing file, defaults to false |\n" +
                        "| :copy-attributes true/false | e.g.: if true copy attributes to the new file, defaults to false |\n" +
                        "| :no-follow-links true/false | e.g.: if true do not follow symbolic links, defaults to false |\n")
                   .seeAlso(
                        "io/copy-files-glob",
                        "io/copy-file-tree",
                        "io/move-file",
                        "io/delete-file",
                        "io/touch-file",
                        "io/copy-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                final VncHashMap options = VncHashMap.ofAll(args.rest().rest());
                final VncVal replaceOpt = options.get(new VncKeyword("replace"));
                final VncVal copyAttrOpt = options.get(new VncKeyword("copy-attributes"));
                final VncVal noFollowLinks = options.get(new VncKeyword("no-follow-links"));

                File sourceFile = convertToFile(
                                            args.first(),
                                            "Function 'io/copy-file' does not allow %s as source");

                final VncVal destVal = args.second();
                File destFile = convertToFile(destVal);

                if (destFile != null) {
                    final List<CopyOption> copyOptions = new ArrayList<>();
                    if (VncBoolean.isTrue(replaceOpt)) {
                        copyOptions.add(StandardCopyOption.REPLACE_EXISTING);
                    }
                    if (VncBoolean.isTrue(copyAttrOpt)) {
                        copyOptions.add(StandardCopyOption.COPY_ATTRIBUTES);
                    }
                    if (VncBoolean.isTrue(noFollowLinks)) {
                        copyOptions.add(LinkOption.NOFOLLOW_LINKS);
                    }

                    try {
                        if (destFile.isDirectory()) {
                            Files.copy(
                                sourceFile.toPath(),
                                destFile.toPath().resolve(sourceFile.getName()),
                                copyOptions.toArray(new CopyOption[0]));
                        }
                        else {
                            Files.copy(
                                sourceFile.toPath(),
                                destFile.toPath(),
                                copyOptions.toArray(new CopyOption[0]));
                        }
                    }
                    catch(Exception ex) {
                        throw new VncException(
                                String.format(
                                        "Failed to copy file %s to %s",
                                        sourceFile.getPath(), destFile.getPath()),
                                ex);
                    }
                }
                else if (Types.isVncJavaObject(destVal, OutputStream.class)) {
                    final OutputStream os = (OutputStream)((VncJavaObject)destVal).getDelegate();

                    try {
                        IOStreamUtil.copyFileToOS(sourceFile, os);
                    }
                    catch(Exception ex) {
                        throw new VncException(
                                String.format(
                                        "Failed to copy file %s to stream",
                                        sourceFile.getPath()),
                                ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/copy-file' does not allow %s as dest",
                            Types.getType(destVal)));
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_copy_files_glob =
        new VncFunction(
                "io/copy-files-glob",
                VncFunction
                    .meta()
                    .arglists("(io/copy-files-glob src-dir dst-dir glob & options)")
                    .doc(
                        "Copies all files that match the glob pattern from a source to a " +
                        "destination directory. \n" +
                        "src-dir and  dst-dir must be a file or a string (file path).\n" +
                        "\n\n" +
                        "Options: \n\n" +
                        "| [![width: 25%]] | [![width: 75%]] |\n" +
                        "| :replace true/false | e.g.: if true replace an existing file, defaults to false |\n" +
                        "| :copy-attributes true/false | e.g.: if true copy attributes to the new file, defaults to false |\n" +
                        "| :no-follow-links true/false | e.g.: if true do not follow symbolic links, defaults to false |" +
                        "\n\n" +
                        globPatternHelp())
                  .examples(
                        "(io/copy-files-glob \"from\" \"to\" \"*.log\")")
                    .seeAlso(
                        "io/copy-file",
                        "io/copy-file-tree",
                        "io/move-files-glob",
                        "io/delete-files-glob",
                        "io/list-files-glob")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);

                sandboxFunctionCallValidation();

                final File srcdir = convertToFile(
                                        args.first(),
                                        "Function 'io/copy-files-glob' does not allow %s as src-dir");


                final File dstdir = convertToFile(
                                        args.second(),
                                        "Function 'io/copy-files-glob' does not allow %s as dst-dir");

                final String glob = Coerce.toVncString(args.third()).getValue();

                final VncHashMap options = VncHashMap.ofAll(args.rest().rest().rest());
                final VncVal replaceOpt = options.get(new VncKeyword("replace"));
                final VncVal copyAttrOpt = options.get(new VncKeyword("copy-attributes"));
                final VncVal noFollowLinks = options.get(new VncKeyword("no-follow-links"));

                final List<CopyOption> copyOptions = new ArrayList<>();
                if (VncBoolean.isTrue(replaceOpt)) {
                    copyOptions.add(StandardCopyOption.REPLACE_EXISTING);
                }
                if (VncBoolean.isTrue(copyAttrOpt)) {
                    copyOptions.add(StandardCopyOption.COPY_ATTRIBUTES);
                }
                if (VncBoolean.isTrue(noFollowLinks)) {
                    copyOptions.add(LinkOption.NOFOLLOW_LINKS);
                }

                validateReadableDirectory(srcdir);
                validateWritableDirectory(dstdir);

                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(srcdir.toPath(), glob)) {
                    dirStream.forEach(path -> {
                        try {
                            final Path d = dstdir.toPath().resolve(srcdir.toPath().relativize(path));

                            Files.copy(
                                path,
                                d,
                                copyOptions.toArray(new CopyOption[0]));
                        }
                        catch(Exception ex) {
                            throw new VncException(
                                    String.format("Failed to copy file %s", path),
                                    ex);
                        }
                    });
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format(
                                    "Failed to copy files from %s to %s, glob: %s ",
                                    srcdir.getPath(),
                                    dstdir.getPath(),
                                    glob),
                            ex);
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_copy_file_tree =
        new VncFunction(
                "io/copy-file-tree",
                VncFunction
                    .meta()
                    .arglists("(io/copy-file-tree source dest & options)")
                    .doc(
                        "Copies a file tree from source to dest. Returns nil or throws a VncException. " +
                        "Source must be a file or a string (file path), dest must be a file, " +
                        "a string (file path), or an `java.io.OutputStream`." +
                        "\n\n" +
                        "Options: \n\n" +
                        "| [![width: 25%]] | [![width: 75%]] |\n" +
                        "| :replace true/false | e.g.: if true replace an existing file, defaults to false |\n" +
                        "| :copy-attributes true/false | e.g.: if true copy attributes to the new file, defaults to false |\n" +
                        "| :no-follow-links true/false | e.g.: if true do not follow symbolic links, defaults to false |\n")
                   .seeAlso(
                        "io/copy-file",
                        "io/copy-files-glob",
                        "io/move-file",
                        "io/delete-file",
                        "io/touch-file",
                        "io/copy-stream")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                final VncHashMap options = VncHashMap.ofAll(args.rest().rest());
                final VncVal replaceOpt = options.get(new VncKeyword("replace"));
                final VncVal copyAttrOpt = options.get(new VncKeyword("copy-attributes"));
                final VncVal noFollowLinks = options.get(new VncKeyword("no-follow-links"));

                File sourceFile = convertToFile(
                                            args.first(),
                                            "Function 'io/copy-file' does not allow %s as source");

                final VncVal destVal = args.second();
                File destFile = convertToFile(destVal);

                if (destFile != null) {
                    final List<CopyOption> copyOptions = new ArrayList<>();
                    if (VncBoolean.isTrue(replaceOpt)) {
                        copyOptions.add(StandardCopyOption.REPLACE_EXISTING);
                    }
                    if (VncBoolean.isTrue(copyAttrOpt)) {
                        copyOptions.add(StandardCopyOption.COPY_ATTRIBUTES);
                    }
                    if (VncBoolean.isTrue(noFollowLinks)) {
                        copyOptions.add(LinkOption.NOFOLLOW_LINKS);
                    }

                    try {
                        Files.walk(sourceFile.toPath()).forEach( s -> {
                            try {
                                Path d = destFile.toPath().resolve(sourceFile.toPath().relativize(s));
                                if (Files.isDirectory(s)) {
                                    if (!Files.exists(d)) {
                                        Files.createDirectory(d);
                                    }
                                    return;
                                }
                                Files.copy(s, d, copyOptions.toArray(new CopyOption[0]));
                            }
                            catch(Exception ex) {
                                throw new VncException(
                                        String.format("Failed to copy file tree at %s", s.toString()),
                                        ex);
                            }
                        });
                    }
                    catch(Exception ex) {
                        throw new VncException(
                                String.format(
                                        "Failed to copy file %s to %s",
                                        sourceFile.getPath(), destFile.getPath()),
                                ex);
                    }
                }
                else if (Types.isVncJavaObject(destVal, OutputStream.class)) {
                    final OutputStream os = (OutputStream)((VncJavaObject)destVal).getDelegate();

                    try {
                        IOStreamUtil.copyFileToOS(sourceFile, os);
                    }
                    catch(Exception ex) {
                        throw new VncException(
                                String.format(
                                        "Failed to copy file %s to stream",
                                        sourceFile.getPath()),
                                ex);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/copy-file' does not allow %s as dest",
                            Types.getType(destVal)));
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_move_file =
        new VncFunction(
                "io/move-file",
                VncFunction
                    .meta()
                    .arglists("(io/move-file source target & options)")
                    .doc(
                        "Moves source to target. Returns nil or throws a VncException. " +
                        "Source and target must be a file or a string (file path)." +
                        "\n\n" +
                        "Options: \n\n" +
                        "| [![width: 20%]] | [![width: 80%]] |\n" +
                        "| :replace true/false | e.g.: if true replace an existing file, defaults to false |\n" +
                        "| :atomic-move true/false | e.g.: if true move the file as an atomic file system operation, defaults to false |\n")
                    .seeAlso("io/copy-file", "io/delete-file", "io/touch-file")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                sandboxFunctionCallValidation();

                final VncHashMap options = VncHashMap.ofAll(args.rest().rest());
                final VncVal replaceOpt = options.get(new VncKeyword("replace"));
                final VncVal atomicMoveOpt = options.get(new VncKeyword("atomic-move"));

                final File from = convertToFile(
                                    args.first(),
                                    "Function 'io/move-file' does not allow %s as source");

                final File to = convertToFile(
                                    args.second(),
                                    "Function 'io/move-file' does not allow %s as target");

                if (!from.isFile()) {
                    throw new VncException(
                            String.format(
                                "Failed to move file %s to %s. The from file does not exists!",
                                from.getPath(), to.getPath()));
                }

                try {
                    final List<CopyOption> moveOptions = new ArrayList<>();
                    if (VncBoolean.isTrue(replaceOpt)) {
                        moveOptions.add(StandardCopyOption.REPLACE_EXISTING);
                    }
                    if (VncBoolean.isTrue(atomicMoveOpt)) {
                        moveOptions.add(StandardCopyOption.ATOMIC_MOVE);
                    }

                    Files.move(
                        from.toPath(),
                        to.toPath(),
                        moveOptions.toArray(new CopyOption[0]));
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format(
                                "Failed to move file %s to %s", from.getPath(), to.getPath()),
                            ex);
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_move_files_glob =
        new VncFunction(
                "io/move-files-glob",
                VncFunction
                    .meta()
                    .arglists("(io/move-files-glob src-dir dst-dir glob & options)")
                    .doc(
                        "Move all files that match the glob pattern from a source to a " +
                        "destination directory. \n" +
                        "src-dir and  dst-dir must be a file or a string (file path)." +
                        "\n\n" +
                        "Options: \n\n" +
                        "| [![width: 20%]] | [![width: 80%]] |\n" +
                        "| :replace true/false | e.g.: if true replace an existing file, defaults to false |\n" +
                        "| :atomic-move true/false | e.g.: if true move the file as an atomic file system operation, defaults to false |" +
                        "\n\n" +
                        globPatternHelp())
                  .examples(
                        "(io/move-files-glob \"from\" \"to\" \"*.log\")")
                    .seeAlso(
                        "io/move-file",
                        "io/move-files-glob",
                        "io/copy-files-glob",
                        "io/delete-files-glob",
                        "io/list-files-glob")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);

                sandboxFunctionCallValidation();

                final File srcdir = convertToFile(
                                        args.first(),
                                        "Function 'io/move-files-glob' does not allow %s as src-dir");


                final File dstdir = convertToFile(
                                        args.second(),
                                        "Function 'io/move-files-glob' does not allow %s as dst-dir");

                final String glob = Coerce.toVncString(args.third()).getValue();

                final VncHashMap options = VncHashMap.ofAll(args.rest().rest().rest());
                final VncVal replaceOpt = options.get(new VncKeyword("replace"));
                final VncVal atomicMoveOpt = options.get(new VncKeyword("atomic-move"));

                validateReadableDirectory(srcdir);
                validateWritableDirectory(dstdir);

                final List<CopyOption> moveOptions = new ArrayList<>();
                if (VncBoolean.isTrue(replaceOpt)) {
                    moveOptions.add(StandardCopyOption.REPLACE_EXISTING);
                }
                if (VncBoolean.isTrue(atomicMoveOpt)) {
                    moveOptions.add(StandardCopyOption.ATOMIC_MOVE);
                }

                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(srcdir.toPath(), glob)) {
                    dirStream.forEach(path -> {
                        try {
                            final Path d = dstdir.toPath().resolve(srcdir.toPath().relativize(path));

                            Files.move(
                                path,
                                d,
                                moveOptions.toArray(new CopyOption[0]));
                        }
                        catch(IOException ex) {
                            throw new VncException(
                                    String.format("Failed to move file %s", path),
                                    ex);
                        }
                    });
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format(
                                    "Failed to move files from %s to %s, glob: %s ",
                                    srcdir.getPath(),
                                    dstdir.getPath(),
                                    glob),
                            ex);
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_touch_file =
        new VncFunction(
                "io/touch-file",
                VncFunction
                    .meta()
                    .arglists("(io/touch-file file)")
                    .doc(
                        "Updates the *lastModifiedTime* of the file to the current time, or " +
                        "creates a new empty file if the file doesn't already exist. " +
                        "File must be a file or a string (file path). \n" +
                        "Returns the file")
                    .seeAlso("io/move-file", "io/copy-file", "io/delete-file")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File file = convertToFile(
                                    args.first(),
                                    "Function 'io/touch-file' does not allow %s as file");

                try {
                    final Path path = file.toPath();

                    if (Files.exists(path)) {
                        Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
                    }
                    else {
                        Files.createFile(path);
                    }

                    return new VncJavaObject(file);
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format(
                                "Failed to touch file %s", file.getPath()),
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_mkdir =
        new VncFunction(
                "io/mkdir",
                VncFunction
                    .meta()
                    .arglists("(io/mkdir dir)")
                    .doc("Creates the directory. dir must be a file or a string (file path).")
                    .seeAlso("io/mkdirs")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File dir = convertToFile(
                                    args.first(),
                                    "Function 'io/mkdir' does not allow %s as dir");

                try {
                    if (!dir.mkdir()) {
                        throw new VncException(
                                String.format("Failed to create dir %s", dir.getPath()));
                    }
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format("Failed to create dir %s", dir.getPath()),
                            ex);
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_mkdirs =
        new VncFunction(
                "io/mkdirs",
                VncFunction
                    .meta()
                    .arglists("(io/mkdirs dir)")
                    .doc(
                        "Creates the directory including any necessary but nonexistent " +
                        "parent directories. dir must be a file or a string (file path).")
                    .seeAlso("io/mkdir")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File dir = convertToFile(
                                    args.first(),
                                    "Function 'io/mkdirs' does not allow %s as dir");

                try {
                    if (!dir.mkdirs()) {
                        throw new VncException(
                                String.format("Failed to create dir %s", dir.getPath()));
                    }
                }
                catch(VncException ex) {
                    throw ex;
                }
                catch(Exception ex) {
                    throw new VncException(
                            String.format("Failed to create dir %s",  dir.getPath()),
                            ex);
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_tmp_dir =
        new VncFunction(
                "io/tmp-dir",
                VncFunction
                    .meta()
                    .arglists("(io/tmp-dir)")
                    .doc("Returns the tmp dir as a `java.io.File`.")
                    .examples("(io/tmp-dir)")
                    .seeAlso("io/user-dir", "io/user-home-dir", "io/temp-dir")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                sandboxFunctionCallValidation();

                return new VncJavaObject(new File(System.getProperty("java.io.tmpdir")));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_user_dir =
        new VncFunction(
                "io/user-dir",
                VncFunction
                    .meta()
                    .arglists("(io/user-dir)")
                    .doc("Returns the user dir (current working dir) as a java.io.File.")
                    .seeAlso("io/tmp-dir", "io/user-home-dir")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                sandboxFunctionCallValidation();

                return new VncJavaObject(new File(System.getProperty("user.dir")));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_user_home_dir =
        new VncFunction(
                "io/user-home-dir",
                VncFunction
                    .meta()
                    .arglists("(io/user-home-dir)")
                    .doc("Returns the user's home dir as a `java.io.File`.")
                    .seeAlso("user-name", "io/user-dir", "io/tmp-dir")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                sandboxFunctionCallValidation();

                return new VncJavaObject(new File(System.getProperty("user.home")));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_download =
        new VncFunction(
                "io/download",
                VncFunction
                    .meta()
                    .arglists("(io/download uri & options)")
                    .doc(
                        "Downloads the content from the uri and reads it as text (string) " +
                        "or binary (bytebuf). Supports http and https protocols!\n\n" +
                        "Options: \n\n" +
                        "| :binary b           | e.g.: `:binary true`, defaults to false |\n" +
                        "| :user-agent agent   | e.g.: `:user-agent \"Mozilla\"`, defaults to nil |\n" +
                        "| :encoding enc       | e.g.: `:encoding :utf-8,` defaults to :utf-8 |\n" +
                        "| :user u             | optional user for basic authentication|\n" +
                        "| :password p         | optional password for basic authentication |\n" +
                        "| :follow-redirects b | e.g.: `:follow-redirects true`, defaults to false |\n" +
                        "| :conn-timeout val   | e.g.: `:conn-timeout 10000`, " +
                        "                        connection timeout in milliseconds. ¶" +
                        "                        0 is interpreted as an infinite timeout. |\n" +
                        "| :read-timeout val   | e.g.: `:read-timeout 10000`, " +
                        "                        read timeout in milliseconds. ¶" +
                        "                        0 is interpreted as an infinite timeout. |\n" +
                        "| :progress-fn fn     | an optional progress function that takes 2 args ¶" +
                        "                        [1] progress (0..100%) ¶" +
                        "                        [2] status {:start :progress :end :failed}|\n" +
                        "| :debug-fn fn        | an optional debug function that takes a message as argument |\n\n" +
                        "Note:¶" +
                        "If the server returns the HTTP response status code 403 (*Access Denied*) " +
                        "sending a user agent like \"Mozilla\" may fool the website and solve the " +
                        "problem.\n\n" +
                        "To debug pass a printing function like: `(io/download https://foo.org/bar :debug-fn println)`")
                    .examples(
                        "(-<> \"https://live.staticflickr.com/65535/51007202541_ea453871d8_o_d.jpg\"\n" +
                        "     (io/download <> :binary true :user-agent \"Mozilla\")\n" +
                        "     (io/spit \"space-x.jpg\" <>))",
                        "(do \n" +
                        "  (load-module :ansi) \n" +
                        "  (-<> \"https://live.staticflickr.com/65535/51007202541_ea453871d8_o_d.jpg\" \n" +
                        "       (io/download <> :binary true \n" +
                        "                       :user-agent \"Mozilla\" \n" +
                        "                       :progress-fn (ansi/progress :caption \"Download:\")) \n" +
                        "       (io/spit \"space-x.jpg\" <>)))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final StopWatch sw = new StopWatch();

                final String uri = Coerce.toVncString(args.first()).getValue();

                VncFunction progressFn = downloadDummyFn();
                VncFunction debugFn = downloadDummyFn();

                try {
                    final VncHashMap options = VncHashMap.ofAll(args.rest());
                    final VncVal user = options.get(new VncKeyword("user"));
                    final VncVal password = options.get(new VncKeyword("password"));
                    final VncVal binary = options.get(new VncKeyword("binary"), VncBoolean.False);
                    final VncVal followRedirects = options.get(new VncKeyword("follow-redirects"), VncBoolean.False);
                    final VncVal useragent = options.get(new VncKeyword("user-agent"));
                    final VncVal encVal = options.get(new VncKeyword("encoding"));
                    final VncVal progressVal = options.get(new VncKeyword("progress-fn"));
                    final VncVal connTimeoutMillisVal = options.get(new VncKeyword("conn-timeout"));
                    final VncVal readTimeoutMillisVal = options.get(new VncKeyword("read-timeout"));
                    final VncVal debugVal = options.get(new VncKeyword("debug-fn"));
                    final Charset charset = CharsetUtil.charset(encVal);

                    if (debugVal != Nil) {
                        debugFn = Coerce.toVncFunction(debugVal);
                        debugFn.sandboxFunctionCallValidation();
                    }
                    if (progressVal != Nil) {
                        progressFn = Coerce.toVncFunction(progressVal);
                        progressFn.sandboxFunctionCallValidation();
                    }

                    // basic authentication
                    String authHeader = null;
                    if (user != Nil && password != Nil) {
                        authHeader = BasicAuthentication.headerValue(
                                        Coerce.toVncString(user).getValue(),
                                        Coerce.toVncString(password).getValue());
                    }
                    else if (user != Nil || password != Nil) {
                       throw new VncException(
                                "io/download needs both the 'user' and the 'password' "
                                + "option for basic authentication!");
                    }


                    debugFn.applyOf(new VncString("URI: " + uri));

                    final URL url = new URL(uri);
                    final String protocol = url.getProtocol();
                    if (!("http".equals(protocol) || "https".equals(protocol))) {
                        throw new VncException(String.format(
                                "io/download does not support the protocol '%s'! " +
                                "Please use 'http' or 'https'.", protocol));
                    }

                    updateDownloadProgress(progressFn, 0L, -1L, new VncKeyword("start"));

                    final HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    debugFn.applyOf(new VncString("Orignal url: " + conn.getURL()));
                    if (Types.isVncString(useragent)) {
                        conn.addRequestProperty("User-Agent", ((VncString)useragent).getValue());
                    }
                    if (Types.isVncLong(connTimeoutMillisVal)) {
                        conn.setConnectTimeout(Math.max(0, ((VncLong)connTimeoutMillisVal).toJavaInteger()));
                    }
                    if (Types.isVncLong(readTimeoutMillisVal)) {
                        conn.setReadTimeout(Math.max(0, ((VncLong)readTimeoutMillisVal).toJavaInteger()));
                    }

                    if (authHeader != null) {
                        conn.setRequestProperty(BasicAuthentication.HEADER, authHeader);
                        debugFn.applyOf(new VncString("Authorization Header: Basic (base64 " + user + ":xxxxxx)"));
                    }

                    // follow redirects
                    if (VncBoolean.isTrue(followRedirects)) {
                        // redirects should be handled by HttpURLConnection if the protocol
                        // does not change (security reasons)
                        conn.setInstanceFollowRedirects(true);
                        debugFn.applyOf(new VncString("Follow redirects: activated"));
                    }

                    conn.connect();
                    debugFn.applyOf(new VncString("Connected url: " + conn.getURL()));

                    try {
                        final int responseCode = conn.getResponseCode();

                        debugFn.applyOf(new VncString("Response code: " + responseCode));
                        for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
                            debugFn.applyOf(new VncString("Response header: key => " + entry.getKey() + ",  value => " + entry.getValue()));
                        }

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            try (BufferedInputStream is = new BufferedInputStream(conn.getInputStream())) {
                                debugFn.applyOf(new VncString("Redirected url: " + conn.getURL()));

                                final byte[] data = slurpData(is, progressFn, conn.getContentLengthLong());

                                final VncVal retVal = VncBoolean.isTrue(binary)
                                                        ? new VncByteBuffer(data)
                                                        : new VncString(new String(data, charset));

                                final long elapsed = sw.stop().elapsed(TimeUnit.MILLISECONDS);
                                updateDownloadProgress(progressFn, 100L, elapsed, new VncKeyword("end"));

                                return retVal;
                            }
                        }
                        else if (responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                            final String location = conn.getHeaderField("Location");
                            throw new VncException(
                                    "Server returned HTTP code: HTTP_MOVED_PERM (301). New location: " + location);
                        }
                        else {
                            throw new VncException(
                                    "Failed to download data. Server returned HTTP code: " + responseCode);
                        }
                    }
                    finally {
                        conn.disconnect();
                    }
                }
                catch(Exception ex) {
                    updateDownloadProgress(progressFn, 0L, -1L, new VncKeyword("failed"));
                    throw new VncException("Failed to download data from the URI: " + uri, ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction io_internet_avail_Q =
        new VncFunction(
                "io/internet-avail?",
                VncFunction
                    .meta()
                    .arglists("(io/internet-avail?)", "(io/internet-avail? url)")
                    .doc(
                        "Checks if an internet connection is present for a given url. " +
                        "Defaults to URL *http://www.google.com*.")
                    .examples(
                        "(io/internet-avail?)",
                        "(io/internet-avail? \"http://www.google.com\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

                final String sURL = args.isEmpty()
                                        ? "http://www.google.com"
                                        : Coerce.toVncString(args.first()).getValue();

                return VncBoolean.of(InternetUtil.isInternetAvailable(sURL));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_mime_type =
        new VncFunction(
                "io/mime-type",
                VncFunction
                    .meta()
                    .arglists("(io/mime-type file)")
                    .doc("Returns the mime-type for the file if available else nil.")
                    .examples(
                        "(io/mime-type \"document.pdf\")",
                        "(io/mime-type (io/file \"document.pdf\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final VncVal file = args.first();

                if (Types.isVncString(file)) {
                    return new VncString(
                                MimeTypes.getMimeTypeFromFileName(
                                        ((VncString)file).getValue()));
                }
                else if (Types.isVncJavaObject(file, File.class)) {
                    return new VncString(
                            MimeTypes.getMimeTypeFromFile(
                                    (File)(Coerce.toVncJavaObject(file).getDelegate())));
                }
                else {
                    throw new VncException(String.format(
                            "Function 'io/mime-type' does not allow %s as fs",
                            Types.getType(file)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // IO log functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction io_log_filehandler =
        new VncFunction(
                "io/log-filehandler",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/log-filehandler logger-name file-name-pattern)",
                        "(io/log-filehandler logger-name file-name-pattern file-size-limit file-count)")
                    .doc(
                        "Creates a file handler for a logger.")
                    .examples(
                        "(do                                                                            \n" +
                        "  (io/log-filehandler \"venice\" \"/var/log/myapp/venice_%g.log\" 1000000 8)   \n" +
                        "  (io/log \"venice\" :info \"message 1\")                                      \n" +
                        "  (io/log \"venice\" :warning \"message 2\")                                   \n" +
                        "  (io/log \"venice\" :severe \"message 3\"))                                   ")
                    .seeAlso(
                        "io/log")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 4);

                sandboxFunctionCallValidation();

                final String loggerName = Coerce.toVncString(args.first()).getValue();
                final String pattern = Coerce.toVncString(args.second()).getValue();
                final int limit = args.size() > 2 ? Coerce.toVncLong(args.third()).getIntValue() : 0;
                final int count = args.size() > 3 ? Coerce.toVncLong(args.fourth()).getIntValue() : 1;

                try {
                	// Format for SimpleFormatter - detailed breakdown of each parameter:
            		// %1$t... = timestamp (various time/date formats)
            		// %2$s = source class and method
            		// %3$s = logger name
            		// %4$s = log level
            		// %5$s = message
                	// %6$s = thrown exception (if any)
            		// %n = platform-specific line separator
                	// "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %3$s %5$s%6$s%n"

                    final FileHandler handler = new FileHandler(pattern, limit, count, true);
                    handler.setEncoding("UTF-8");
                    handler.setFormatter(new SimpleFormatter() {
                        private static final String format = "%1$tF %1$tT|%4$s|%5$s%6$s%n";

                        @Override
                        public synchronized String format(final LogRecord logRecord) {
                            return String.format(
	                                format,
	                                new Date(logRecord.getMillis()),
	                                logRecord.getLevel().getLocalizedName(),
	                                logRecord.getMessage());
                        }
                    });

                    final Logger logger = Logger.getLogger(loggerName);
                    logger.addHandler(handler);

                    return Nil;
                }
                catch(IOException ex) {
                    throw new VncException(
                            "Failed to create file handler for logger '" + loggerName + "'!",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_log =
        new VncFunction(
                "io/log",
                VncFunction
                    .meta()
                    .arglists(
                    	"(io/log logger-name level message)",
                    	"(io/log logger-name level message exception)")
                    .doc(
                        "Creates an empty temp file with the given prefix and " +
                        "suffix. Returns a :java.io.File.")
                    .examples(
	                    "(do                                                                            \n" +
                        "  (io/log-filehandler \"venice\" \"/var/log/myapp/venice_%g.log\" 1000000 8)   \n" +
                        "  (io/log \"venice\" :info \"message 1\")                                      \n" +
                        "  (io/log \"venice\" :warning \"message 2\")                                   \n" +
                        "  (io/log \"venice\" :severe \"message 3\"))                                   ")
                    .seeAlso(
                        "io/log-filehandler")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3, 4);

                sandboxFunctionCallValidation();

                final String loggerName = Coerce.toVncString(args.first()).getValue();
                final String level = Coerce.toVncKeyword(args.second()).getSimpleName().toUpperCase();
                final String message = Coerce.toVncString(args.third()).getValue();
                final Throwable th = args.size() > 3 ? Coerce.toVncJavaObject(args.fourth(), Throwable.class) : null;

                final Logger logger = Logger.getLogger(loggerName);
                try {
                     logger.log(Level.parse(level), message, th);
                }
                catch(IllegalArgumentException ex) {
                    throw new VncException(
                            "Invalid log level '" + level + "'! "
                            + "Use one of { :SEVERE, :WARNING, :INFO, "
                            + ":CONFIG, :FINE, :FINER, :FINEST}.");
                }

                return Nil;
              }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // IO TEMP functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction io_temp_file =
        new VncFunction(
                "io/temp-file",
                VncFunction
                    .meta()
                    .arglists("(io/temp-file prefix suffix)")
                    .doc(
                        "Creates an empty temp file with the given prefix and " +
                        "suffix. Returns a :java.io.File.")
                    .examples(
                        "(do \n" +
                        "  (let [file (io/temp-file \"test-\", \".txt\")] \n" +
                        "    (io/spit file \"123456789\" :append true) \n" +
                        "    (io/slurp file :binary false :remove true)) \n" +
                        ")")
                    .seeAlso(
                        "io/temp-dir",
                        "io/delete-file-on-exit")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                sandboxFunctionCallValidation();

                final String prefix = Coerce.toVncString(args.first()).getValue();
                final String suffix = Coerce.toVncString(args.second()).getValue();
                try {
                    final File file = Files.createTempFile(prefix, suffix).normalize().toFile();
                    return new VncJavaObject(file);
                }
                catch (Exception ex) {
                    throw new VncException(ex.getMessage(), ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_temp_dir =
        new VncFunction(
                "io/temp-dir",
                VncFunction
                    .meta()
                    .arglists("(io/temp-dir prefix)")
                    .doc("Creates a new temp directory with prefix. Returns a :java.io.File.")
                    .examples("(io/temp-dir \"test-\")")
                    .seeAlso("io/tmp-dir", "io/temp-file")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final String prefix = Coerce.toVncString(args.first()).getValue();
                try {
                    return new VncJavaObject(Files.createTempDirectory(prefix).normalize().toFile());
                }
                catch (Exception ex) {
                    throw new VncException("Failed to create a temp directory", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_filesystem_total_space =
        new VncFunction(
                "io/filesystem-total-space",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/filesystem-total-space)",
                        "(io/filesystem-total-space file)")
                    .doc(
                        "Returns the total diskspace in bytes. \n" +
                        "With no args returns the total disk space of the current working " +
                        "directory's file store. With a file argument returns the total " +
                        "disk space of the file store the file is located.")
                    .examples("(io/filesystem-total-space)")
                    .seeAlso("io/filesystem-usable-space")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

                final File file = args.isEmpty()
                                    ? new File(".")
                                    : convertToFile(
                                         args.first(),
                                         "Function 'io/filesystem-total-space' does not allow %s as file");

                try {
                    final FileStore store = Files.getFileStore(file.toPath());
                    return new VncLong(store.getTotalSpace());
                }
                catch(Exception ex) {
                    throw new VncException("Failed to get total disk space", ex);
                }
             }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_filesystem_usable_space =
        new VncFunction(
                "io/filesystem-usable-space",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/filesystem-usable-space)",
                        "(io/filesystem-usable-space file)")
                    .doc(
                        "Returns the usable diskspace in bytes. \n" +
                        "With no args returns the usable disk space of the current working " +
                        "directory's file store. With a file argument returns the usable " +
                        "disk space of the file store the file is located.")
                    .examples("(io/filesystem-usable-space)")
                    .seeAlso("io/filesystem-total-space")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

                final File file = args.isEmpty()
                                    ? new File(".")
                                    : convertToFile(
                                         args.first(),
                                         "Function 'io/filesystem-usable-space' does not allow %s as file");

                try {
                    final FileStore store = Files.getFileStore(file.toPath());
                    return new VncLong(store.getUsableSpace());
                }
                catch(Exception ex) {
                    throw new VncException("Failed to get usable disk space", ex);
                }
             }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_load_classpath_resource =
        new VncFunction(
                "io/load-classpath-resource",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/load-classpath-resource name)")
                    .doc(
                        "Loads a classpath resource. Returns a bytebuf")
                    .examples(
                        "(io/load-classpath-resource \"com/github/jlangch/venice/images/venice.png\")")
                    .seeAlso("io/classpath-resource?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncVal name = args.first();

                try {
                    if (Types.isVncString(name)) {
                        final String res = ((VncString)args.first()).getValue();
                        final byte[] data = ThreadContext.getInterceptor().onLoadClassPathResource(res);
                        return data == null ? Nil : new VncByteBuffer(data);
                    }
                    else if (Types.isVncKeyword(name)) {
                        final String res = ((VncKeyword)args.first()).getValue();
                        final byte[] data = ThreadContext.getInterceptor().onLoadClassPathResource(res);
                        return data == null ? Nil : new VncByteBuffer(data);
                    }
                    else if (Types.isVncSymbol(name)) {
                        final String res = ((VncSymbol)args.first()).getName();
                        final byte[] data = ThreadContext.getInterceptor().onLoadClassPathResource(res);
                        return data == null ? Nil : new VncByteBuffer(data);
                    }
                    else {
                        return Nil;
                    }
                }
                catch (SecurityException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new VncException(
                            "Failed to load classpath resource: " + name.toString(),
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_classpath_resource_Q =
        new VncFunction(
                "io/classpath-resource?",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/classpath-resource? name)")
                    .doc(
                        "Returns true if the classpath resource exists otherwise false.")
                    .examples(
                        "(io/classpath-resource? \"com/github/jlangch/venice/images/venice.png\")")
                    .seeAlso("io/load-classpath-resource")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal name = args.first();

                try {
                    if (Types.isVncString(name)) {
                        final String path = ((VncString)args.first()).getValue();
                        return VncBoolean.of(new ClassPathResource(path).getResource() != null);
                    }
                    else if (Types.isVncKeyword(name)) {
                        final String path = ((VncKeyword)args.first()).getValue();
                        return VncBoolean.of(new ClassPathResource(path).getResource() != null);
                    }
                    else if (Types.isVncSymbol(name)) {
                        final String path = ((VncSymbol)args.first()).getName();
                        return VncBoolean.of(new ClassPathResource(path).getResource() != null);
                    }
                    else {
                        return VncBoolean.False;
                    }
                }
                catch (Exception ex) {
                    return VncBoolean.False;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_default_charset =
        new VncFunction(
                "io/default-charset",
                VncFunction
                    .meta()
                    .arglists("(io/default-charset)")
                    .doc("Returns the default charset.")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncString(Charset.defaultCharset().name());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_make_venice_filename =
            new VncFunction(
                    "io/make-venice-filename",
                    VncFunction
                        .meta()
                        .arglists(
                            "(io/make-venice-filename f)")
                        .doc(
                            "Returns the file f with the extension '.venice'. f must " +
                            "be a file or a string (file path).")
                        .examples(
                            "(io/make-venice-filename \"/tmp/foo\")",
                            "(io/make-venice-filename \"/tmp/foo.venice\")")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    final VncVal f = args.first();

                    if (Types.isVncString(f)) {
                        String s = Coerce.toVncString(f).getValue();
                        return s.endsWith(".venice") ? f : new VncString(s + ".venice");
                    }
                    else if (Types.isVncJavaObject(f, File.class)) {
                        final File file = Coerce.toVncJavaObject(f, File.class);
                        final String name = file.getName();
                        return name.endsWith(".venice")
                                ? f
                                : new VncJavaObject(
                                        new File(file.getParentFile(), name + ".venice"));
                    }
                    else if (Types.isVncJavaObject(f, Path.class)) {
                        final File file = Coerce.toVncJavaObject(f, Path.class).toFile();
                        final String name = file.getName();
                        return name.endsWith(".venice")
                                ? f
                                : new VncJavaObject(
                                        new File(file.getParentFile(), name + ".venice").toPath());
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'io/make-venice-filename' does not allow %s as fs",
                                Types.getType(f)));
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

    private static Path convertToPath(final VncVal f) {
        if (Types.isVncString(f)) {
            return new File(((VncString)f).getValue()).toPath();
        }
        else if (Types.isVncJavaObject(f, File.class)) {
            return Coerce.toVncJavaObject(f, File.class).toPath();
        }
        else if (Types.isVncJavaObject(f, Path.class)) {
            return Coerce.toVncJavaObject(f, Path.class);
        }
        else {
            return null;
        }
    }

    private static Path convertToPath(final VncVal f, final String errFormat) {
        final Path path = convertToPath(f);
        if (path == null) {
            throw new VncException(String.format(errFormat, Types.getType(f)));
        }
        else {
            return path;
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

    public static void validateReadableDirectory(final File file) {
        if (!file.isDirectory()) {
            throw new VncException(String.format("'%s' is not a directory", file.getPath()));
        }
        if (!file.canRead()) {
            throw new VncException(String.format("The directory '%s' has no read permission", file.getPath()));
        }
    }

    public static void validateReadableFileOrDirectory(final File file) {
        if (!(file.isDirectory() || file.isFile())) {
            throw new VncException(String.format("'%s' is not a file or a directory", file.getPath()));
        }
        if (file.isFile() && !file.canRead()) {
            throw new VncException(String.format("The file '%s' has no read permission", file.getPath()));
        }
        if (file.isDirectory() && !file.canRead()) {
            throw new VncException(String.format("The directory '%s' has no read permission", file.getPath()));
        }
    }

    public static void validateWritableDirectory(final File file) {
        if (!file.isDirectory()) {
            throw new VncException(String.format("'%s' is not a directory", file.getPath()));
        }
        if (!file.canWrite()) {
            throw new VncException(String.format("The directory '%s' has no write permission", file.getPath()));
        }
    }

    private static void updateDownloadProgress(
            final VncFunction fn,
            final long percentage,
            final long elapsedMillis,
            final VncKeyword status
    ) {
        if (fn != null) {
            try {
                final int arity = fn.getFixedArgsCount();
                if (arity == 3) {
                    fn.apply(VncList.of(new VncLong(percentage), status, new VncLong(elapsedMillis)));
                }
                else {
                    fn.apply(VncList.of(new VncLong(percentage), status));
                }
            }
            catch(Exception ex) {
                // do nothing
            }
        }
    }

    private static byte[] slurpData(
            final BufferedInputStream is,
            final VncFunction progressFn,
            final long contentLength
    ) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            updateDownloadProgress(progressFn, 0L, -1L, new VncKeyword("progress"));

            final byte[] buffer = new byte[16 * 1024];
            int n;
            long total = 0L;
            long progressLast = 0L;
            while (-1 != (n = is.read(buffer))) {
                output.write(buffer, 0, n);
                total += n;

                // progress: 0..100%
                long progress = Math.max(0, Math.min(100, (total * 100) / contentLength));

                if (progress != progressLast && progress < 100L) {
                    updateDownloadProgress(progressFn, progress, -1L, new VncKeyword("progress"));
                }

                progressLast = progress;
            }

            updateDownloadProgress(progressFn, 100L, -1L, new VncKeyword("progress"));
            Thread.sleep(100); // leave the 100% progress for a blink of an eye

            return output.toByteArray();
        }
        finally {
            output.close();
        }
    }

    private static File normalize(final File file, final Form form) {
        return new File(Normalizer.normalize(file.getPath(), form));
    }

    private static final String globPatternHelp() {
        return
            "**Globbing patterns**\n" +
            "\n" +
            "| [![width: 20%]] | [![width: 80%]] |\n" +
            "| `*.txt`       | Matches a path that represents a file name ending in .txt |\n" +
            "| `*.*`         | Matches file names containing a dot |\n" +
            "| `*.{txt,xml}` | Matches file names ending with .txt or .xml |\n" +
            "| `foo.?[xy]`   | Matches file names starting with foo. and a single character extension followed by a 'x' or 'y' character |\n" +
            "| `/home/*/*`   | Matches `/home/gus/data` on UNIX platforms |\n" +
            "| `/home/**`    | Matches `/home/gus` and `/home/gus/data` on UNIX platforms |\n" +
            "| `C:\\\\*`     | Matches `C:\\\\foo` and `C:\\\\bar` on the Windows platform |\n" +
            "\n" +
            "*Ranges*\n" +
            "\n" +
            "The pattern `[A-E]` would match any character that included ABCDE. " +
            "Ranges can be used in conjunction with each other to make powerful patterns. " +
            "Alphanumerical strings are matched by `[A-Za-z0-9]`. This would match the " +
            "following:\n" +
            "\n" +
            " * `[A-Z]` All uppercase letters from A to Z\n" +
            " * `[a-z]` All lowercase letters from a to z\n" +
            " * `[0-9]` All numbers from 0 to 9\n" +
            "\n" +
            "*Complementation*\n" +
            "\n" +
            "Globs can be used in complement with special characters that can change how the " +
            "pattern works. The two complement characters are exclamation marks `(!)` and " +
            "backslashes `(\\)`.\n" +
            "\n" +
            "The exclamation mark can negate a pattern that it is put in front of. " +
            "As `[CBR]at` matches Cat, Bat, or Rat the negated pattern `[!CBR]at` matches\n" +
            "anything like Kat, Pat, or Vat.\n" +
            "\n" +
            "Backslashes are used to remove the special meaning of single characters " +
            "`'?'`, `'*'`, and `'['`, so that they can be used in patterns.";
    }

    private static VncFunction downloadDummyFn() {
        return new VncFunction(VncFunction.createAnonymousFuncName("download-dummy")) {
                      @Override  public VncVal apply(final VncList args) { return Nil; }
                      private static final long serialVersionUID = 1L;
                   };
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(io_file)
                    .add(io_file_Q)
                    .add(io_file_path)
                    .add(io_file_path_slashify)
                    .add(io_file_canonical)
                    .add(io_file_absolute)
                    .add(io_file_parent)
                    .add(io_file_name)
                    .add(io_file_basename)
                    .add(io_to_path)
                    .add(io_path_Q)
                    .add(io_file_ext_Q)
                    .add(io_file_ext)
                    .add(io_file_normalize_utf)
                    .add(io_file_size)
                    .add(io_file_last_modified)
                    .add(io_exists_Q)
                    .add(io_exists_file_Q)
                    .add(io_exists_dir_Q)
                    .add(io_file_can_read_Q)
                    .add(io_file_can_write_Q)
                    .add(io_file_can_execute_Q)
                    .add(io_file_set_readable)
                    .add(io_file_set_writable)
                    .add(io_file_set_executable)
                    .add(io_file_hidden_Q)
                    .add(io_create_symbolic_link)
                    .add(io_create_hard_link)
                    .add(io_symbolic_link_Q)
                    .add(io_file_absolute_Q)
                    .add(io_glob_path_matcher)
                    .add(io_file_matches_globQ)
                    .add(io_file_within_dir_Q)
                    .add(io_to_url)
                    .add(io_to_uri)
                    .add(io_list_files)
                    .add(io_list_file_tree)
                    .add(io_list_file_tree_lazy)
                    .add(io_list_files_glob)
                    .add(io_delete_file)
                    .add(io_delete_file_on_exit)
                    .add(io_delete_file_tree)
                    .add(io_delete_files_glob)
                    .add(io_copy_file)
                    .add(io_copy_files_glob)
                    .add(io_copy_file_tree)
                    .add(io_move_file)
                    .add(io_move_files_glob)
                    .add(io_touch_file)
                    .add(io_mkdir)
                    .add(io_mkdirs)
                    .add(io_temp_file)
                    .add(io_temp_dir)
                    .add(io_tmp_dir)
                    .add(io_user_dir)
                    .add(io_user_home_dir)
                    .add(io_filesystem_usable_space)
                    .add(io_filesystem_total_space)
                    .add(io_download)
                    .add(io_internet_avail_Q)
                    .add(io_mime_type)
                    .add(io_default_charset)
                    .add(io_load_classpath_resource)
                    .add(io_classpath_resource_Q)
                    .add(io_make_venice_filename)
                    .add(io_log_filehandler)
                    .add(io_log)
                    .toMap();
}
