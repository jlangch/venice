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

import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;


public class LoadPathFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // LodPath load functions
    ///////////////////////////////////////////////////////////////////////////


    public static VncFunction loadPaths =
        new VncFunction(
                "load-paths",
                VncFunction
                    .meta()
                    .arglists("(load-paths)")
                    .doc(
                        "Returns the list of the defined load paths. A load path is either " +
                        "a ZIP file, or a directory. \n\n" +
                        "The functions `load-file` and I/O functions try sequentially every " +
                        "load path to read the file. If a load path is a directory the file is " +
                        "read from that directory. If a load path is a ZIP file the file is read " +
                        "from within that ZIP.\n\n" +
                        "Examples:")
                    .seeAlso(
                        "load-paths-unrestricted?",
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

    public static VncFunction loadPathsUnrestricted_Q =
        new VncFunction(
                "load-paths-unrestricted?",
                VncFunction
                    .meta()
                    .arglists("(load-paths-unrestricted?)")
                    .doc("Returns true if the load paths are unrestricted.")
                    .seeAlso(
                        "load-paths",
                        "load-file")
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

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(loadPaths)
                    .add(loadPathsUnrestricted_Q)
                    .toMap();
}
