/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;


public class LoadPathFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // LodPath load functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction paths =
        new VncFunction(
                "loadpath/paths",
                VncFunction
                    .meta()
                    .arglists("(loadpath/paths)")
                    .doc(
                        "Returns the list of the defined load paths. A load path is either " +
                        "a file, a ZIP file, or a directory. Load paths are defined at the " +
                        "application level. They are passed as part of the sandbox to the" +
                        "Venice evaluator.\n\n" +
                        "The functions that support load paths try sequentially every " +
                        "load path to access files. If a load path is a ZIP file, files can be " +
                        "read from within that ZIP file.\n\n" +
                        "Example:\n\n" +
                        "```                                \n" +
                        "/Users/foo/demo                    \n" +
                        "  |                                \n" +
                        "  +--- resources.zip               \n" +
                        "  |                                \n" +
                        "  +--- /data                       \n" +
                        "        |                          \n" +
                        "        +--- config.json           \n" +
                        "        |                          \n" +
                        "        +--- /scripts              \n" +
                        "              |                    \n" +
                        "              +--- script1.venice  \n" +
                        "```                                \n" +
                        "                                   \n" +
                        "With a load path configuration of `[\"/Users/foo/demo/resources.zip\", \"/Users/foo/demo/data\"]` \n\n" +
                        " * `(io/slurp \"config.json\")` -> slurps /Users/foo/demo/data/config.json \n" +
                        " * `(io/slurp \"scripts/script1.venice\")` -> slurps /Users/foo/demo/data/scripts/script1.venice \n" +
                        " * `(io/slurp \"img1.png\")` -> slurps /Users/foo/demo/resources.zip!img1.png" +
                        "\n\n" +
                        "I/O functions with support for load paths:" +
                        "\n\n" +
                        " * `load-file`\n" +
                        " * `io/slurp`\n" +
                        " * `io/slurp-lines`\n" +
                        " * `io/spit`\n" +
                        " * `io/file-in-stream`\n" +
                        " * `io/file-out-stream`\n" +
                        " * `io/delete-file`" +
                        "\n\n" +
                        "To enforce a Venice script to read/write files on the load paths only:" +
                        "\n\n" +
                        " * Define a custom sandbox\n" +
                        " * Disable all I/O functions\n" +
                        " * Enable the I/O functions that support load paths\n")
                    .seeAlso(
                        "loadpath/unrestricted?",
                        "loadpath/normalize",
                        "load-file")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                sandboxFunctionCallValidation();

                final IInterceptor interceptor = ThreadContext.getInterceptor();
                final ILoadPaths paths = interceptor.getLoadPaths();

                return VncList.ofColl(
                        paths.getPaths()
                             .stream()
                             .map(f -> new VncJavaObject(f))
                             .collect(Collectors.toList()));
            }

            @Override
            public boolean isRedefinable() {
                return false;  // security
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction normalize =
        new VncFunction(
                "loadpath/normalize",
                VncFunction
                    .meta()
                    .arglists("(loadpath/normalize f)")
                    .doc(
                        "Normalize a relative file regarding the load paths.\n\n" +
                        "With the load paths: `[\"/Users/foo/img.png\", \"/Users/foo/resources\"]`\n\n" +
                        "  * `(loadpath/normalize \"img.png\")` -> \"/Users/foo/img.png\"\n" +
                        "  * `(loadpath/normalize \"test.json\")` -> \"/Users/foo/resources/test.json\"\n" +
                        "  * `(loadpath/normalize \"/tmp/data.json\")` -> \"/tmp/data.json\"")
                    .seeAlso(
                        "loadpath/paths",
                        "loadpath/unrestricted?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final IInterceptor interceptor = ThreadContext.getInterceptor();
                final ILoadPaths paths = interceptor.getLoadPaths();

                final VncVal f = args.first();

                if (Types.isVncJavaObject(f, File.class)) {
                    return new VncJavaObject(
                                paths.normalize(
                                    Coerce.toVncJavaObject(f, File.class)));
                }
                else if (Types.isVncJavaObject(f, Path.class)) {
                    return new VncJavaObject(
                                paths.normalize(
                                        Coerce.toVncJavaObject(f, Path.class).toFile())
                                              .toPath());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'loadpath/normalize' does not allow %s as file arg",
                            Types.getType(f)));
                }
            }

            @Override
            public boolean isRedefinable() {
                return false;  // security
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction unrestricted_Q =
        new VncFunction(
                "loadpath/unrestricted?",
                VncFunction
                    .meta()
                    .arglists("(loadpath/unrestricted?)")
                    .doc("Returns true if the load paths are unrestricted.")
                    .seeAlso(
                        "loadpath/paths",
                        "loadpath/normalize")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                sandboxFunctionCallValidation();

                final IInterceptor interceptor = ThreadContext.getInterceptor();
                final ILoadPaths paths = interceptor.getLoadPaths();

                return VncBoolean.of(paths.isUnlimitedAccess());
            }

            @Override
            public boolean isRedefinable() {
                return false;  // security
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(paths)
                    .add(unrestricted_Q)
                    .add(normalize)
                    .toMap();
}
