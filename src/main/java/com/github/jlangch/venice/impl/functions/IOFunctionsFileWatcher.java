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

import static com.github.jlangch.venice.impl.functions.ConcurrencyFunctions.future;
import static com.github.jlangch.venice.impl.functions.CoreFunctions.partial;
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.filewatcher.FileWatcher_FsWatch;
import com.github.jlangch.venice.impl.util.filewatcher.FileWatcher_JavaWatchService;
import com.github.jlangch.venice.impl.util.filewatcher.IFileWatcher;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchErrorEvent;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchFileEvent;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchRegisterEvent;
import com.github.jlangch.venice.impl.util.filewatcher.events.FileWatchTerminationEvent;
import com.github.jlangch.venice.util.OS;


public class IOFunctionsFileWatcher {

    public static VncFunction io_watch_dir =
        new VncFunction(
                "io/watch-dir",
                VncFunction
                    .meta()
                    .arglists(
                        "(io/watch-dir dir & options)")
                    .doc(
                        "Watch a directory for changes, and call the function `event-fn` when it " +
                        "does. Calls the optional `failure-fn` if errors occur. On closing " +
                        "the watcher `termination-fn` is called." +
                        "\n\n" +
                        "Options: \n\n" +
                        "| [![width: 25%]] | [![width: 75%]] |\n" +
                        "| :include-all-subdirs true/false | If `true` includes in addtion to the main dir recursively all subdirs. If `false` just watches on the main dir. Defaults to `false`.|\n" +
                        "| :event-fn fn | A two argument function that receives the path and mode {:created, :deleted, :modified} of the changed file. Defaults to `nil`.|\n" +
                        "| :error-fn fn | A two argument function called in error case that receives the watch path and the failure exception. Defaults to `nil`.|\n" +
                        "| :termination-fn fn | A one argument function called when the watcher terminates. It receives the watch dir.  Defaults to `nil`.|\n" +
                        "| :register-fn fn | A one argument function that is called when a newly created sub directory is registered. It receives the registered dir. Defaults to `nil`.|\n" +
                        "The *watcher* is a resource that must be closed with `(io/close-watcher w)`." +
                        "\n\n" +
                        "**MacOS** " +
                        "\n\n" +
                        "The Java WatchService doesn't run properly on MacOS due to Java limitations! " +
                        "As a workaround on MacOS the file watcher is impemented on top of the *fswatch* " +
                        "tool. The required *fswatch* tool can be installed from *Homebrew*.\n\n" +
                        "```                        \n" +
                        "   $ brew install fswatch  \n" +
                        "```                        \n" +
                        "\n" +
                        "Documentation:\n" +
                        "* [fswatch Github](https://github.com/emcrisostomo/fswatch/)\n" +
                        "* [fswatch Manual](https://emcrisostomo.github.io/fswatch/doc/1.17.1/fswatch.html)\n" +
                        "* [fswatch Installation](https://formulae.brew.sh/formula/fswatch)\n")
                    .examples(
                        "(try-with [w (io/watch-dir \"/tmp\" #(println %1 %2))]    \n" +
                        "  ;; wait 30s and terminate                               \n" +
                        "  (sleep 30 :seconds))                                    ",
                        "(do                                                                                  \n" +
                        "  (def dir (io/temp-dir \"watchdir-\"))                                              \n" +
                        "  (io/delete-file-on-exit dir)                                                       \n" +
                        "                                                                                     \n" +
                        "  (println \"Watching:  \" dir)                                                      \n" +
                        "                                                                                     \n" +
                        "  (try-with [w (io/watch-dir dir                                                     \n" +
                        "                   dir                                                               \n" +
                        "                   :include-all-subdirs true                                         \n" +
                        "                   :event-fn            #(println \"File Event: \" %1 %2)            \n" +
                        "                   :error-fn            #(println \"Error:      \" %1 (:message %2)) \n" +
                        "                   :termination-fn      #(println \"Terminated: \" %1)               \n" +
                        "                   :register-fn         #(println \"Registered: \" %1))]             \n" +
                        "                                                                                     \n" +
                        "    (let [f (io/file dir \"test1.txt\")]                                             \n" +
                        "      (io/touch-file f)                   ;; created                                 \n" +
                        "      (io/delete-file-on-exit f)                                                     \n" +
                        "      (sleep 1000)                                                                   \n" +
                        "      (io/spit f \"AAA\" :append true)    ;; modifed                                 \n" +
                        "      (sleep 1000)                                                                   \n" +
                        "      (io/delete-file f))                 ;; deleted                                 \n" +
                        "                                                                                     \n" +
                        "    ;; wait that all events can be processed before the watcher is closed            \n" +
                        "    (sleep 3000))                                                                    \n" +
                        "                                                                                     \n" +
                        "  ;; wait to receive the termination event                                           \n" +
                        "  (sleep 1 :seconds))                                                                ")
                    .seeAlso(
                        "io/add-watch-dir",
                        "io/registered-watch-dirs",
                        "io/close-watcher",
                        "io/await-for")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File dir = IOFunctions.convertToFile(
                                    args.first(),
                                    "Function 'io/watch-dir' does not allow %s as file").getAbsoluteFile();

                if (!dir.isDirectory()) {
                    throw new VncException(
                            String.format(
                                    "Function 'io/watch-dir': dir '%s' is not a directory",
                                    dir.toString()));
                }

                // parse options
                final VncHashMap options = VncHashMap.ofAll(args.rest());
                final VncVal registerAllSubDirsOpt = options.get(new VncKeyword("include-all-subdirs"),
                                                                 VncBoolean.False);
                final VncVal eventFnOpt = options.get(new VncKeyword("event-fn"));
                final VncVal errorFnOpt = options.get(new VncKeyword("error-fn"));
                final VncVal terminationFnOpt = options.get(new VncKeyword("termination-fn"));
                final VncVal registerFnOpt = options.get(new VncKeyword("register-fn"));
                final VncVal fswatchBinaryOpt = options.get(new VncKeyword("fswatch-binary"),
                                                            new VncString("/opt/homebrew/bin/fswatch"));

                final VncFunction eventFn = Coerce.toVncFunctionOptional(eventFnOpt);
                final VncFunction errorFn = Coerce.toVncFunctionOptional(errorFnOpt);
                final VncFunction terminationFn = Coerce.toVncFunctionOptional(terminationFnOpt);
                final VncFunction registerFn = Coerce.toVncFunctionOptional(registerFnOpt);

                final boolean registerAllSubDirs = Coerce.toVncBoolean(registerAllSubDirsOpt).getValue();
                final String fswatchBinary = Coerce.toVncString(fswatchBinaryOpt).toString();

                if (OS.isLinux() || OS.isMacOSX()) {
                    try {
                        final IFileWatcher fw;

                        if (OS.isMacOSX()) {
                            fw = new FileWatcher_FsWatch(
                                         dir.toPath(),
                                         registerAllSubDirs,
                                         createFileEventListener(eventFn),
                                         createErrorEventListener(errorFn),
                                         createTerminationEventListener(terminationFn),
                                         createRegisterEventListener(registerFn),
                                         fswatchBinary);
                        }
                        else {
                            fw = new FileWatcher_JavaWatchService(
                                         dir.toPath(),
                                         registerAllSubDirs,
                                         createFileEventListener(eventFn),
                                         createErrorEventListener(errorFn),
                                         createTerminationEventListener(terminationFn),
                                         createRegisterEventListener(registerFn));
                        }

                        fw.start(new CallFrame[] { new CallFrame(this, args) });

                        return new VncJavaObject(fw);
                    }
                    catch(Exception ex) {
                        throw new VncException(
                                String.format(
                                        "Function 'io/watch-dir' failed to watching dir '%s'",
                                        dir.toString()),
                                ex);
                    }
                }
                else {
                    throw new VncException(
                            "Function 'io/watch-dir' is not supported on this operating system!");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_add_watch_dir =
        new VncFunction(
                "io/add-watch-dir",
                VncFunction
                    .meta()
                    .arglists("(io/add-watch-dir watcher file)")
                    .doc("Register another file or directory with a watcher.")
                    .examples(
                        "(do                                                                     \n" +
                        "  (defn log [msg] (locking log (println msg)))                          \n" +
                        "                                                                        \n" +
                        "  (try-with [w (io/watch-dir \"/data/filestore\"                        \n" +
                        "                             #(log (str %1 \" \" %2))                   \n" +
                        "                             #(log (str \"failure \" (:message %2)))    \n" +
                        "                             #(log (str \"terminated watching \" %1)))] \n" +
                        "    (io/add-watch-dir w \"/data/filestore/0000\")                       \n" +
                        "    (io/add-watch-dir w \"/data/filestore/0001\")                       \n" +
                        "    (io/add-watch-dir w \"/data/filestore/0002\")                       \n" +
                        "    (sleep 30 :seconds)))")
                    .seeAlso(
                        "io/watch-dir",
                        "io/registered-watch-dirs",
                        "io/close-watcher")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                sandboxFunctionCallValidation();

                if (!OS.isLinux()) {
                    throw new VncException(
                            "Function 'io/add-watch-dir' is not supported on this operating system!");
                }

                final IFileWatcher fw = Coerce.toVncJavaObject(args.first(), IFileWatcher.class);

                final File dir = IOFunctions.convertToFile(
                                    args.second(),
                                    "Function 'io/add-watch-dir' does not allow %s as file").getAbsoluteFile();

                if (!dir.isDirectory()) {
                    throw new VncException(
                            String.format(
                                    "Function 'io/add-watch-dir': dir '%s' is not a directory",
                                    dir.toString()));
                }

                try {
                    fw.register(dir.toPath());

                    return Nil;
                }
                catch(Exception ex) {
                    throw new VncException(
                            "Function 'io/add-watch-dir' failed to add a new file with the watcher",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_registered_watch_dirs =
        new VncFunction(
                "io/registered-watch-dirs",
                VncFunction
                    .meta()
                    .arglists("(io/registered-watch-dirs watcher)")
                    .doc("Returns the registred watch directories of a watcher.")
                    .seeAlso(
                        "io/watch-dir",
                        "io/add-watch-dir",
                        "io/close-watcher")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final IFileWatcher fw = Coerce.toVncJavaObject(args.first(), IFileWatcher.class);

                return VncList.ofColl(
                        fw.getRegisteredPaths()
                          .stream()
                          .map(p -> new VncJavaObject(p.toFile()))
                          .collect(Collectors.toList()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction io_close_watcher =
        new VncFunction(
                "io/close-watcher",
                VncFunction
                    .meta()
                    .arglists("(io/close-watcher watcher)")
                    .doc("Closes a watcher created from 'io/watch-dir'.")
                    .seeAlso(
                        "io/watch-dir",
                        "io/add-watch-dir",
                        "io/registered-watch-dirs")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final IFileWatcher fw = Coerce.toVncJavaObject(args.first(), IFileWatcher.class);
                try {
                    fw.close();
                    return Nil;
                }
                catch(Exception ex) {
                    throw new VncException(
                            "Function 'io/close-watcher' failed to close watch service",
                            ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Util
    ///////////////////////////////////////////////////////////////////////////

    private static Consumer<FileWatchFileEvent> createFileEventListener(
            final VncFunction fn
    ) {
        return fn == null
                ? null
                : (event) -> { if (event.isFile()) {
                                    future.applyOf(
                                       partial.applyOf(
                                        fn,
                                        new VncString(event.getPath().toString()),
                                        new VncKeyword(event.getType().name().toLowerCase())));
                               }};
    }

    private static Consumer<FileWatchErrorEvent> createErrorEventListener(
            final VncFunction fn
    ) {
        return fn == null
                ? null
                : (event) -> future.applyOf(
                                partial.applyOf(
                                    fn,
                                    new VncString(event.getPath().toString()),
                                    event.getException() == null
                                    	? Nil
                                    	: new VncJavaObject(event.getException())));
    }

    private static Consumer<FileWatchTerminationEvent> createTerminationEventListener(
            final VncFunction fn
    ) {
        return fn == null
                ? null
                : (event) -> future.applyOf(
                                partial.applyOf(
                                    fn,
                                    new VncString(event.getPath().toString())));
    }

    private static Consumer<FileWatchRegisterEvent> createRegisterEventListener(
            final VncFunction fn
    ) {
        return fn == null
                ? null
                : (event) -> future.applyOf(
                                partial.applyOf(
                                    fn,
                                    new VncString(event.getPath().toString())));
    }



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(io_watch_dir)
                    .add(io_add_watch_dir)
                    .add(io_registered_watch_dirs)
                    .add(io_close_watcher)
                    .toMap();
}
