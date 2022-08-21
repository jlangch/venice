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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.io.CharsetUtil;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;


public class ModuleFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // Module load functions
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction loadModule =
        new VncFunction(
                "load-module*",
                VncFunction
                    .meta()
                    .arglists("(load-module* name)")
                    .doc(
                        "Loads a Venice extension module. Returns the module as an " +
                        "unevaluted string. Throws a `VncException` if the module " +
                        "does not exist.")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final String name = Coerce.toVncString(CoreFunctions.name.apply(args)).getValue();

                try {
                    // sandbox: validate module load
                    final IInterceptor interceptor = ThreadContext.getInterceptor();
                    interceptor.validateLoadModule(name);

                    return new VncString(ModuleLoader.loadModule(name));
                }
                catch (VncException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new VncException(
                                String.format("Failed to load Venice module '%s'", name),
                                ex);
                }
            }

            @Override
            public boolean isRedefinable() {
                return false;  // security
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction loadClasspathFile =
        new VncFunction(
                "load-classpath-file*",
                VncFunction
                    .meta()
                    .arglists("(load-classpath-file* name)")
                    .doc(
                        "Loads a Venice file from the classpath.\n\n" +
                        "Returns the loaded Venice code as an unevaluated `string` if the " +
                        "file exists.\n\n" +
                        "Throws a `VncException` if the name of the passed file does not " +
                        "have the file extension '.venice' or if the file does not exist.")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                try {
                    final String file = name(args.first());

                    if (file != null) {
                        final String res = ModuleLoader.loadClasspathVeniceFile(file);
                        if (res == null ) {
                            throw new VncException("Failed to load Venice classpath file");
                        }
                        else {
                            return new VncString(res);
                        }
                    }
                    else {
                        return Nil;
                    }
                }
                catch (VncException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new VncException("Failed to load Venice classpath file", ex);
                }
            }

            @Override
            public boolean isRedefinable() {
                return false;  // security
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction loadFile =
        new VncFunction(
                "load-file*",
                VncFunction
                    .meta()
                    .arglists("(load-file* file)")
                    .doc(
                        "Loads a venice file from the given load-paths.\n\n" +
                        "Returns the loaded Venice code as as an unevaluated `string` if " +
                        "the file exists.\n\n" +
                        "Throws a `VncException` if the name of the passed file does not " +
                        "have the file extension '.venice' or if the file does not exist.\n\n" +
                        "See the `load-paths` doc for a description of the *load path* feature.")
                    .seeAlso("load-paths", "load-resource*")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final String file = name(args.first());
                try {
                    final String data = ModuleLoader.loadExternalVeniceFile(file);
                    return new VncString(data);
                }
                catch (VncException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new VncException("Failed to load the Venice file '" + file + "'", ex);
                }
            }

            @Override
            public boolean isRedefinable() {
                return false;  // security
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction loadResource =
        new VncFunction(
                "load-resource*",
                VncFunction
                    .meta()
                    .arglists("(load-resource* file & options)")
                    .doc(
                        "Loads a resource from the given load-paths. Returns a string, a bytebuffer " +
                        "or nil if the file does not exist. \n\n" +
                        "Options: \n\n" +
                        "| :binary b   | e.g :binary true, defaults to true |\n" +
                        "| :encoding e | e.g :encoding :utf-8, defaults to :utf-8 |\n\n" +
                        "See the `load-paths` doc for a description of the *load path* feature.")
                    .seeAlso("load-paths", "load-file*")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final String file = name(args.first());
                try {
                    final VncHashMap options = VncHashMap.ofAll(args.rest());
                    final boolean binary = VncBoolean.isTrue(options.get(
                                                                new VncKeyword("binary"),
                                                                VncBoolean.True));
                    final Charset charset = CharsetUtil.charset(options.get(new VncKeyword("encoding")));

                    final IInterceptor interceptor = ThreadContext.getInterceptor();

                    if (binary) {
                        final ByteBuffer data = interceptor.getLoadPaths().loadBinaryResource(new File(file));

                        if (data == null) {
                            throw new VncException(
                                    "Failed to load the resource file '" + file + "'!");
                        }
                        else {
                            return new VncByteBuffer(data);
                        }
                    }
                    else {
                        final String data = interceptor.getLoadPaths().loadTextResource(new File(file), charset);

                        if (data == null) {
                            throw new VncException(
                                    "Failed to load the resource file '" + file + "'!");
                        }
                        else {
                            return new VncString(data);
                        }
                    }
                }
                catch (VncException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new VncException("Failed to load the resource '" + file + "'", ex);
                }
            }

            @Override
            public boolean isRedefinable() {
                return false;  // security
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction loadPaths =
        new VncFunction(
                "load-paths",
                VncFunction
                    .meta()
                    .arglists("(load-paths)")
                    .doc(
                        "Returns the list of the defined load paths. A load path is either " +
                        "a ZIP file, or a directory. \n\n" +
                        "The functions `load-file` and `load-resource` try sequentially every " +
                        "load path to read the file. If a load path is a directory the file is " +
                        "read from that directory. If a load path is a ZIP file the file is read " +
                        "from within that ZIP.\n\n" +
                        "Examples:")
                    .seeAlso(
                        "load-paths-unrestricted?",
                        "load-file", "load-resource")
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
                        "load-file", "load-resource")
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


    private static String name(final VncVal val) {
        if (Types.isVncString(val)) {
            return ((VncString)val).getValue();
        }
        else if (Types.isVncKeyword(val)) {
            return ((VncKeyword)val).getValue();
        }
        else if (Types.isVncSymbol(val)) {
            return ((VncSymbol)val).getName();
        }
        else {
            return null;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(loadModule)
                    .add(loadFile)
                    .add(loadResource)
                    .add(loadClasspathFile)
                    .add(loadPaths)
                    .add(loadPathsUnrestricted_Q)
                    .toMap();
}
