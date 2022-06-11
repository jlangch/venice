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
package com.github.jlangch.venice.impl;

import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.JsonFunctions;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.zip.ZipFileSystemUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;
import com.github.jlangch.venice.javainterop.LoadPathsFactory;
import com.github.jlangch.venice.util.NullInputStream;
import com.github.jlangch.venice.util.NullOutputStream;


public class AppRunner {

    public static String run(
            final File appArchive,
            final VncList cmdLineArgs,
            final ILoadPaths loadPaths,
            final PrintStream stdout,
            final PrintStream stderr,
            final Reader stdin
    ) {
        // Merge the load paths from the command line with the application archive
        final List<File> mergedLoadPaths = new ArrayList<>();
        mergedLoadPaths.add(appArchive.getAbsoluteFile());
        mergedLoadPaths.addAll(loadPaths.getPaths());
        final ILoadPaths appLoadPaths = LoadPathsFactory.of(
                                            mergedLoadPaths,
                                            loadPaths.isUnlimitedAccess());

        final IInterceptor interceptor = new AcceptAllInterceptor(appLoadPaths);

        final VncMap manifest = getManifest(appArchive);

        final String appName = Coerce.toVncString(manifest.get(new VncString("app-name"))).getValue();
        final String mainFile = Coerce.toVncString(manifest.get(new VncString("main-file"))).getValue();

        final IVeniceInterpreter venice = new VeniceInterpreter(interceptor);

        final Env env = createEnv(
                            venice,
                            Arrays.asList(
                                convertCliArgsToVar(cmdLineArgs == null ? VncList.empty() : cmdLineArgs),
                                convertAppNameToVar(appName),
                                convertAppArchiveToVar(appArchive)),
                            stdout,
                            stderr,
                            stdin);

        final String script = String.format("(do (load-file \"%s\") nil)", stripVeniceFileExt(mainFile));

        return venice.PRINT(venice.RE(script, appName, env));
    }


    private static VncMap getManifest(final File app) {
        if (app.exists()) {
            try {
                final VncVal manifest = ZipFileSystemUtil.loadTextFileFromZip(app, new File("MANIFEST.MF"), "utf-8");
                return Coerce.toVncMap(JsonFunctions.read_str.apply(VncList.of(manifest)));
            }
            catch (Exception ex) {
                throw new VncException(String.format(
                        "Failed to load manifest from Venice application archive '%s'.",
                        app.getPath()));
            }
        }
        else {
            throw new VncException(String.format(
                    "The Venice application archive '%s' does not exist",
                    app.getPath()));
        }
    }

    private static Env createEnv(
            final IVeniceInterpreter venice,
            final List<Var> vars,
            final PrintStream stdout,
            final PrintStream stderr,
            final Reader stdin
    ) {
        final PrintStream stdout_ = stdout == null ? new PrintStream(new NullOutputStream(), true) : stdout;
        final PrintStream stderr_ = stderr == null ? new PrintStream(new NullOutputStream(), true) : stderr;
        final Reader stdin_ = stdin == null ? new InputStreamReader(new NullInputStream()) : stdin;

        return venice.createEnv(true, false, RunMode.APP)
                     .addGlobalVars(vars)
                     .setStdoutPrintStream(stdout_)
                     .setStderrPrintStream(stderr_)
                     .setStdinReader(stdin_);
    }

    private static Var convertAppNameToVar(final String appName) {
        return new Var(new VncSymbol("*app-name*"), new VncString(appName), false);
    }

    private static Var convertAppArchiveToVar(final File appArchive) {
        return new Var(new VncSymbol("*app-archive*"), new VncJavaObject(appArchive), false);
    }

    private static Var convertCliArgsToVar(final VncList cmdLineArgs) {
        return new Var(new VncSymbol("*ARGV*"), cmdLineArgs, false);
    }

    private static String stripVeniceFileExt(final String s) {
        return StringUtil.removeEnd(s, ".venice");
    }

}
