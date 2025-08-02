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
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.jlangch.aviron.filewatcher.FsWatchMonitor;
import com.github.jlangch.aviron.filewatcher.IFileWatcher;
import com.github.jlangch.aviron.filewatcher.events.FileWatchErrorEvent;
import com.github.jlangch.aviron.filewatcher.events.FileWatchFileEvent;
import com.github.jlangch.aviron.filewatcher.events.FileWatchTerminationEvent;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.filewatcher.aviron.Aviron_FileWatcher_FsWatch;
import com.github.jlangch.venice.impl.util.filewatcher.aviron.Aviron_FileWatcher_JavaWatchService;
import com.github.jlangch.venice.impl.util.io.FileUtil;
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
                        "| :include-all-subdirs true/false | " +
                               "If `true` includes in addtion to the main dir recursively all subdirs. " +
                               "If `false` just watches on the main dir. Defaults to `false`.|\n" +
                        "| :file-fn fn | "+
                               "A two argument function that receives the path and action {:created, " +
                               ":deleted, :modified} of the changed file. Defaults to `nil`.|\n" +
                        "| :error-fn fn | " +
                               "A two argument function called in error case that receives the watch " +
                               "path and the failure exception. Defaults to `nil`.|\n" +
                        "| :termination-fn fn | " +
                               "A one argument function called when the watcher terminates. It receives " +
                               "the main watch dir.  Defaults to `nil`.|\n" +
                        "\n\n" +
                        "Specific MacOS options (fswatch): \n\n" +
                        "| [![width: 25%]] | [![width: 75%]] |\n" +
                        "| :fswatch-monitor m | " +
                               "An optional platform dependent monitor {:fsevents_monitor, " +
                               ":kqueue_monitor, :poll_monitor} to use with `fswatch`. Pass `nil` to " +
                               " let`fswatch` choose the optimal platform. Defaults to `nil`.¶" +
                               "Run `fswatch --list-monitors` to get list of the available platform " +
                               "monitors.|\n" +
                        "| :fswatch-program p |" +
                               "An optional path to the `fswatch` program, e.g.: \"fswatch\", " +
                               "\"/opt/homebrew/bin/fswatch\".¶Defaults to `\"/opt/homebrew/bin/fswatch\"`.|" +
                        "\n\n" +
                        "**Important**" +
                        "\n\n" +
                        "The *watcher* is a resource that **must be closed** with either `(io/close-watcher w)` " +
                        " or by using a `(try-with [w (io/watch-dir ...` resource protection statement!"  +
                        "\n\n" +
                        "**Linux** " +
                        "\n\n" +
                        "On Linux the Java WatchService is used to watch dirs for file changes!" +
                        "\n\n" +
                        "**MacOS** " +
                        "\n\n" +
                        "The Java WatchService doesn't run properly on MacOS due to limitations in the " +
                        "Java WatchService implementation! As a workaround on MacOS the file watcher is " +
                        "impemented on top of the *fswatch* tool. The required *fswatch* tool can be " +
                        "installed from *Homebrew*.\n\n" +
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
                        "(do                                                                          \n" +
                        "  (def dir (io/temp-dir \"watchdir-\"))                                      \n" +
                        "  (io/delete-file-on-exit dir)                                               \n" +
                        "                                                                             \n" +
                        "  (def lock 0)                                                               \n" +
                        "                                                                             \n" +
                        "  (defn log [& s]                                                            \n" +
                        "    (locking lock (apply println s)))                                        \n" +
                        "                                                                             \n" +
                        "  (defn file-event [path action]                                             \n" +
                        "    (log \"File Event: %s %s\" path action))                                 \n" +
                        "                                                                             \n" +
                        "  (defn error-event [path exception]                                         \n" +
                        "    (log \"Error:      %s %s\" path (:message exception)))                   \n" +
                        "                                                                             \n" +
                        "  (defn termination-event [path]                                             \n" +
                        "    (log \"Terminated: %s\" path))                                           \n" +
                        "                                                                             \n" +
                        "  (log \"Watching:  %s\" dir)                                                \n" +
                        "                                                                             \n" +
                        "  (try-with [w (io/watch-dir dir                                             \n" +
                        "                   dir                                                       \n" +
                        "                   :include-all-subdirs true                                 \n" +
                        "                   :file-fn             file-event                           \n" +
                        "                   :error-fn            error-event                          \n" +
                        "                   :termination-fn      termination-event)]                  \n" +
                        "                                                                             \n" +
                        "                   ;; on MacOS you might want to add the option              \n" +
                        "                   ;; :fswatch-program  \"/opt/homebrew/bin/fswatch\"        \n" +
                        "                                                                             \n" +
                        "    (let [f (io/file dir \"test1.txt\")]                                     \n" +
                        "      (io/touch-file f)                   ;; created                         \n" +
                        "      (io/delete-file-on-exit f)                                             \n" +
                        "      (sleep 1000)                                                           \n" +
                        "      (io/spit f \"AAA\" :append true)    ;; modifed                         \n" +
                        "      (sleep 1000)                                                           \n" +
                        "      (io/delete-file f))                 ;; deleted                         \n" +
                        "                                                                             \n" +
                        "    ;; wait that all events can be processed before the watcher is closed    \n" +
                        "    (sleep 3000))                                                            \n" +
                        "                                                                             \n" +
                        "  ;; wait to receive the termination event                                   \n" +
                        "  (sleep 1 :seconds))                                                        ")
                    .seeAlso(
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
                final VncVal eventFnOpt = options.get(new VncKeyword("file-fn"));
                final VncVal errorFnOpt = options.get(new VncKeyword("error-fn"));
                final VncVal terminationFnOpt = options.get(new VncKeyword("termination-fn"));
                final VncVal monitorOpt = options.get(new VncKeyword("fswatch-monitor"));
                final VncVal fswatchBinaryOpt = options.get(new VncKeyword("fswatch-program"),
                                                            new VncString("/opt/homebrew/bin/fswatch"));

                final VncFunction eventFn = Coerce.toVncFunctionOptional(eventFnOpt);
                final VncFunction errorFn = Coerce.toVncFunctionOptional(errorFnOpt);
                final VncFunction terminationFn = Coerce.toVncFunctionOptional(terminationFnOpt);

                final boolean registerAllSubDirs = Coerce.toVncBoolean(registerAllSubDirsOpt).getValue();
                final FsWatchMonitor monitor = monitorOpt == Nil
                                                    ? null
                                                    : FsWatchMonitor.valueOf(
                                                            Coerce.toVncKeyword(registerAllSubDirsOpt)
                                                                  .getSimpleName());
                final String fswatchBinary = Coerce.toVncString(fswatchBinaryOpt).toString();

                // TODO:  Migration to Aviron file watcher
                // 1.  Test Venice 1.12.53 with Aviron 1.6.0 (drop-in replacement for
                //     avsan.venice)
                //     => done
                // 2.  Migrate FileWatcherQueue to use the Aviron FileWatcherQueue and
                //     the methods from the :aviron module
                //     avscan.venice must be changed
                //     => done
                // 3.  Use Aviron_FileWatcher_FsWatch and Aviron_FileWatcher_JavaWatchService
                //     instead of the Venice built-in file watchers (drop-in replacement)
                //     => done
                // 4.  Migrate io/watch-dir to callbacks receiving a single event map arg
                //     avsan.venice must be changed, especially for file-event to discard
                //     dir action events.
                // 5.  Remove Venice :file-watcher-queue module and FileWatcherQueue
                if (OS.isLinux() || OS.isMacOSX()) {
                    try {
                        final IFileWatcher fw;

                        if (OS.isMacOSX()) {
                            fw = new Aviron_FileWatcher_FsWatch(
                                         new CallFrame[] { new CallFrame(this, args) },
                                         dir.toPath(),
                                         registerAllSubDirs,
                                         createFileEventListener(eventFn),
                                         createErrorEventListener(errorFn),
                                         createTerminationEventListener(terminationFn),
                                         monitor,
                                         fswatchBinary);
                        }
                        else {
                            fw = new Aviron_FileWatcher_JavaWatchService(
                                         new CallFrame[] { new CallFrame(this, args) },
                                         dir.toPath(),
                                         registerAllSubDirs,
                                         createFileEventListener(eventFn),
                                         createErrorEventListener(errorFn),
                                         createTerminationEventListener(terminationFn));
                        }

                        fw.start();

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

    public static VncFunction io_registered_watch_dirs =
        new VncFunction(
                "io/registered-watch-dirs",
                VncFunction
                    .meta()
                    .arglists("(io/registered-watch-dirs watcher)")
                    .doc("Returns the registred watch directories of a watcher.")
                    .seeAlso(
                        "io/watch-dir",
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

        // https://github.com/juxt/dirwatch/blob/master/src/juxt/dirwatch.clj
        public static VncFunction io_await_for =
            new VncFunction(
                    "io/await-for",
                    VncFunction
                        .meta()
                        .arglists("(io/await-for timeout time-unit file & modes)")
                        .doc(
                            "Blocks the current thread until the file has been created, deleted, or " +
                            "modified according to the passed modes {:created, :deleted, :modified}, " +
                            "or the timeout has elapsed. Returns logical false if returning due to " +
                            "timeout, logical true otherwise. \n\n" +
                            "Supported time units are: {:milliseconds, :seconds, :minutes, :hours, :days}")
                        .examples(
                            "(io/await-for 10 :seconds \"/tmp/data.json\" :created)")
                        .seeAlso(
                            "io/watch-dir")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertMinArity(this, args, 3);

                    sandboxFunctionCallValidation();

                    final long timeout = Coerce.toVncLong(args.first()).getValue();

                    final TimeUnit unit = toTimeUnit(Coerce.toVncKeyword(args.second()));

                    final long timeoutMillis = unit.toMillis(Math.max(0,timeout));

                    final File file = IOFunctions.convertToFile(
                                            args.third(),
                                            "Function 'io/await-for' does not allow %s as file").getAbsoluteFile();


                    final Set<WatchEvent.Kind<?>> events = new HashSet<>();
                    for(VncVal v : args.slice(3)) {
                        final VncKeyword mode = Coerce.toVncKeyword(v);
                        switch(mode.getSimpleName()) {
                            case "created":
                                events.add(StandardWatchEventKinds.ENTRY_CREATE);
                                break;
                            case "deleted":
                                events.add(StandardWatchEventKinds.ENTRY_DELETE);
                                break;
                            case "modified":
                                events.add(StandardWatchEventKinds.ENTRY_MODIFY);
                                break;
                            default:
                                throw new VncException(
                                        String.format(
                                                "Function 'io/await-for' invalid mode '%s'. Use one or " +
                                                "multiple of {:created, :deleted, :modified}",
                                                mode.toString()));
                        }
                    }

                    if (events.isEmpty()) {
                        throw new VncException(
                                "Function 'io/await-for' missing a mode. Pass one or " +
                                "multiple of {:created, :deleted, :modified}");
                    }

                    try {
                        return VncBoolean.of(FileUtil.awaitFile(
                                                file.getCanonicalFile().toPath(),
                                                timeoutMillis,
                                                events));
                    }
                    catch(InterruptedException ex) {
                        throw new com.github.jlangch.venice.InterruptedException(
                                "Interrupted while calling function 'io/await-for'", ex);
                    }
                    catch(IOException ex) {
                        throw new VncException(
                                String.format(
                                        "Function 'io/await-for' failed to await for file '%s'",
                                        file.getPath()),
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

    private VncMap toMap(final FileWatchFileEvent event) {
        return VncHashMap.of(
                new VncKeyword("type"),   new VncKeyword("file-event"),
                new VncKeyword("file"),   new VncJavaObject(event.getPath().toFile()),
                new VncKeyword("dir?"),   VncBoolean.of(event.isDir()),
                new VncKeyword("file?"),  VncBoolean.of(event.isFile()),
                new VncKeyword("action"), new VncKeyword(event.getType().name().toLowerCase()));
    }

    private VncMap toMap(final FileWatchErrorEvent event) {
        return VncHashMap.of(
                new VncKeyword("type"),      new VncKeyword("error-event"),
                new VncKeyword("file"),      new VncJavaObject(event.getPath().toFile()),
                new VncKeyword("exception"), new VncJavaObject(event.getException()));
    }

    private VncMap toMap(final FileWatchTerminationEvent event) {
        return VncHashMap.of(
                new VncKeyword("type"), new VncKeyword("termination-event"),
                new VncKeyword("file"), new VncJavaObject(event.getPath().toFile()));
    }

    private static TimeUnit toTimeUnit(final VncKeyword unit) {
        switch(unit.getValue()) {
            case "milliseconds": return TimeUnit.MILLISECONDS;
            case "seconds": return TimeUnit.SECONDS;
            case "minutes":  return TimeUnit.MINUTES;
            case "hours": return TimeUnit.HOURS;
            case "days": return TimeUnit.DAYS;
            default: throw new VncException(
                            "Invalid time-unit " + unit.getValue() + ". "
                                + "Use one of {:milliseconds, :seconds, :minutes, :hours, :days}");
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(io_watch_dir)
                    .add(io_registered_watch_dirs)
                    .add(io_close_watcher)
                    .add(io_await_for)
                    .toMap();
}
