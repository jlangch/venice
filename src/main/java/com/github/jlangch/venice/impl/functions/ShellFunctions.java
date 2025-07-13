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

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.shell.ShellResult;
import com.github.jlangch.venice.impl.util.shell.Signal;
import com.github.jlangch.venice.impl.util.shell.SimpleShell;
import com.github.jlangch.venice.impl.util.shell.SmartShell;


public class ShellFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // Shell
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction sh =
        new VncFunction(
                "sh",
                VncFunction
                    .meta()
                    .arglists("(sh & args)")
                    .doc(
                        "Launches a new sub-process.\n\n" +
                        "Options:\n\n" +
                        "| :in       | may be given followed by input source as InputStream, " +
                        "              Reader, File, ByteBuf, or String, to be fed to the " +
                        "              sub-process's stdin. |\n" +
                        "| :in-enc   | option may be given followed by a String, used as a " +
                        "              character encoding name (for example \"UTF-8\" or " +
                        "              \"ISO-8859-1\") to convert the input string specified " +
                        "              by the :in option to the sub-process's stdin. Defaults " +
                        "              to \"UTF-8\". If the :in option provides a byte array, " +
                        "              then the bytes are passed unencoded, and this option " +
                        "              is ignored. |\n" +
                        "| :out-enc  | option may be given followed by :bytes or a String. If " +
                        "              a String is given, it will be used as a character " +
                        "              encoding name (for example \"UTF-8\" or \"ISO-8859-1\") " +
                        "              to convert the sub-process's stdout to a String which is " +
                        "              returned. If :bytes is given, the sub-process's stdout " +
                        "              will be stored in a Bytebuf and returned. Defaults to " +
                        "              UTF-8. |\n" +
                        "| :out-fn   | a function with a single string argument that receives " +
                        "              line by line from the process' stdout. If passed the " +
                        "              :out value in the return map will be empty. |\n" +
                        "| :err-fn   | a function with a single string argument that receives " +
                        "              line by line from the process' stderr. If passed the " +
                        "              :err value in the return map will be empty. |\n" +
                        "| :env      | override the process env with a map. |\n" +
                        "| :dir      | override the process dir with a String or java.io.File. |\n" +
                        "| :throw-ex | If true throw an exception if the exit code is not equal " +
                        "              to zero, if false returns the exit code. Defaults to " +
                        "              false.¶" +
                        "              It's recommended to use¶" +
                        "              &emsp; `(with-sh-throw (sh \"ls\" \"-l\"))` ¶" +
                        "              instead. |\n" +
                        "| :timeout  | A timeout in milliseconds |\n" +
                        "\n" +
                        "```                                                                  \n" +
                        "   +---------------------------------------------------------+       \n" +
                        "   |                       Venice Script                     |       \n" +
                        "   +---------------------------------------------------------+       \n" +
                        "      |                                                   ^          \n" +
                        "      v                                                   |          \n" +
                        "+---------+      +----------------------------+           |          \n" +
                        "| in-data |----->|stdin                       |      +----------+    \n" +
                        "+---------+      |       SHELL PROCESS  stdout|----->|  out-fn  |    \n" +
                        "                 |                      stderr|----->|  err-fn  |    \n" +
                        "                 +----------------------------+      +----------+    \n" +
                        "```                                                                  \n\n" +
                        "You can bind :env, :dir for multiple operations using `with-sh-env` or " +
                        "`with-sh-dir`. `with-sh-throw` is binds *:throw-ex* as *true*.\n" +
                        "\n" +
                        "`sh` returns a map of:\n\n" +
                        "```                                                                  \n" +
                        ":exit => sub-process's exit code                                     \n" +
                        ":out  => sub-process's stdout (as Bytebuf or String)                 \n" +
                        ":err  => sub-process's stderr (String via platform default encoding) \n" +
                        "```\n\n" +
                        "E.g.:\n\n" +
                        "```                                       \n" +
                        "(sh \"uname\" \"-r\")                     \n" +
                        "=> {:err \"\" :out \"20.5.0\\n\" :exit 0} \n" +
                        "```")
                    .examples(
                        "(println (sh \"ls\" \"-l\"))",
                        "(println (sh \"ls\" \"-l\" \"/tmp\"))",
                        "(println (sh \"sed\" \"s/[aeiou]/oo/g\" :in \"hello there\\n\"))",
                        "(println (sh \"cat\" :in \"x\\u25bax\\n\"))",
                        "(println (sh \"echo\" \"x\\u25bax\"))",
                        "(println (sh \"/bin/sh\" \"-c\" \"ls -l\"))",

                        "(sh \"ls\" \"-l\" :out-fn println)",
                        "(sh \"ls\" \"-l\" :out-fn println :err-fn println)",

                        ";; background process\n" +
                        "(println (sh \"/bin/sh\" \"-c\" \"sleep 30 >/dev/null 2>&1 &\"))",
                        "(println (sh \"/bin/sh\" \"-c\" \"nohup sleep 30 >/dev/null 2>&1 &\"))",

                        ";; asynchronously slurping stdout and stderr\n" +
                        "(sh \"/bin/sh\" \n" +
                        "    \"-c\" \"for i in {1..5}; do sleep 1; echo \\\"Hello $i\\\"; done\" \n" +
                        "    :out-fn println \n" +
                        "    :err-fn println)",

                        ";; asynchronously slurping stdout and stderr with a timeout\n" +
                        "(sh \"/bin/sh\" \n" +
                        "    \"-c\" \"for i in {1..5}; do sleep 1; echo \\\"Hello $i\\\"; done\" \n" +
                        "    :out-fn println \n" +
                        "    :err-fn println \n" +
                        "    :timeout 2500)",

                        ";; reads 4 single-byte chars\n" +
                        "(println (sh \"echo\" \"x\\u25bax\" :out-enc \"ISO-8859-1\"))",

                        ";; reads binary file into bytes[]\n" +
                        "(println (sh \"cat\" \"birds.jpg\" :out-enc :bytes))",

                        ";; working directory\n" +
                        "(println (with-sh-dir \"/tmp\" (sh \"ls\" \"-l\") (sh \"pwd\")))",
                        "(println (sh \"pwd\" :dir \"/tmp\"))",

                        ";; throw an exception if the shell's subprocess exit code is not equal to 0\n" +
                        "(println (with-sh-throw (sh \"ls\" \"-l\")))",
                        "(println (sh \"ls\" \"-l\" :throw-ex true))",

                        ";; windows\n" +
                        "(println (sh \"cmd\" \"/c dir 1>&2\"))")
                    .seeAlso("with-sh-throw", "with-sh-dir", "with-sh-env" )
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                sandboxFunctionCallValidation();

                final VncVector v = parseArgs(args);

                final VncList cmd = Coerce.toVncList(v.first()).withMeta(args.getMeta());
                final VncMap opts = Coerce.toVncMap(v.second()).withMeta(args.getMeta());

                return SmartShell.exec(cmd, opts, null);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction open =
        new VncFunction(
                "sh/open",
                VncFunction
                    .meta()
                    .arglists("(sh/open f)")
                    .doc(
                        "Opens a *file* or an *URL* with the associated platform specific " +
                        "application. \n\n" +
                        "Uses the OS commands:\n\n" +
                        "* *MacOS*: `/usr/bin/open f`\n" +
                        "* *Windows*: `cmd /C start f`\n" +
                        "* *Linux*: `/usr/bin/xdg-open f`\n\n" +
                        "Note: `sh/open` can only be run from a REPL!")
                    .examples(
                        "(sh/open \"sample.pdf\")",
                        "(sh/open \"https://github.com/jlangch/venice\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                switch(SystemFunctions.osType()) {
                    case "mac-osx":
                        sh.apply(VncList.of(
                                    new VncString("/usr/bin/open"),
                                    args.first()));
                        break;

                    case "linux":
                        sh.apply(VncList.of(
                                    new VncString("/usr/bin/xdg-open"),
                                    args.first()));
                        break;

                    case "windows":
                        sh.apply(VncList.of(
                                    new VncString("cmd"),
                                    new VncString("/C"),
                                    new VncString("start"),
                                    args.first()));
                        break;

                    default:
                        throw new VncException("Unsupported OS");
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pwd =
        new VncFunction(
                "sh/pwd",
                VncFunction
                    .meta()
                    .arglists("(sh/pwd)")
                    .doc(
                        "Returns the current working directory.\n\n" +
                        "Note: ¶" +
                        "You can't change the current working directory of the Java VM but " +
                        "if you were to launch another process using (sh & args) you can " +
                        "specify the working directory for the new spawned process.")
                    .examples("(sh/pwd)")
                    .seeAlso("sh")
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

    public static VncFunction kill =
        new VncFunction(
                "sh/kill",
                VncFunction
                    .meta()
                    .arglists("(sh/kill pid)", "(sh/kill pid signal)")
                    .doc(
                        "Kills a process.\n\n" +
                        "The signal to be sent is one of {:sighup, :sigint, :sigquit, :sigkill, :sigterm}." +
                        "If no signal is specified, the :sigterm signal is sent.\n\n" +
                        "Throws an exception if the process does not exist or cannot be killed!\n\n" +
                        "Note: This function is available for Linux and MacOS only!")
                    .examples(
                        "(sh/kill \"2345\")",
                        "(sh/kill \"2345\" :sighup)")
                    .seeAlso("sh", "sh/alive?", "sh/pgrep", "sh/pargs")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                sandboxFunctionCallValidation();

                SimpleShell.validateLinuxOrMacOSX("sh/kill");

                final String pid = Coerce.toVncString(args.first()).getValue();

                if (args.size() == 1) {
                    SimpleShell.kill(pid);
                }
                else {
                    SimpleShell.kill(
                        Signal.valueOf(
                            Coerce.toVncKeyword(args.second())
                                  .getSimpleName()
                                  .toUpperCase()),
                        pid);
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction alive_Q =
        new VncFunction(
                "sh/alive?",
                VncFunction
                    .meta()
                    .arglists("(sh/alive? pid)")
                    .doc(
                        "Returns true if the process represented by the PID is alive otherwise false.\n\n"  +
                        "Runs internally the Unix command `ps -p {pid}` to check if there is a process "+
                        "with the given pid.\n\n" +
                        "Note: This function is available for Linux and MacOS only!")
                    .examples("(sh/alive? \"2345\")")
                    .seeAlso("sh", "sh/kill", "sh/pgrep", "sh/pargs", "sh/load-pid")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                SimpleShell.validateLinuxOrMacOSX("sh/alive?");

                final VncVal arg1 = args.first();
                if (arg1 == Nil) {
                    return VncBoolean.False;
                }
                else {
                    final String pid = Coerce.toVncString(arg1).getValue();

                    final ShellResult result = SimpleShell.execCmd("ps", "-p", pid);
                    return VncBoolean.of(result.isZeroExitCode());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction load_pid =
        new VncFunction(
                "sh/load-pid",
                VncFunction
                    .meta()
                    .arglists("(sh/load-pid pid-file)")
                    .doc(
                        "Load a process PID from a PID file.\n\nReturns the PID or `nil` " +
                        "if the file does not exist or is empty")
                    .examples("(sh/load-pid \"/data/scan.pid\")")
                    .seeAlso("sh", "sh/alive?", "sh/kill", "sh/pgrep", "sh/pargs")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                final File f = IOFunctions.convertToFile(
                                    args.first(),
                                    "Function 'sh/load-pid' does not allow %s as f");

                SimpleShell.validateLinuxOrMacOSX("sh/load-pid");

                if (f.isFile()) {
                    final String s = StringUtil.trimToNull(
                                        new String(
                                                FileUtil.load(f),
                                                Charset.forName("UTF-8")));
                    if (s != null && s.matches("[0-9]+")) {
                        return new VncString(s);
                    }
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pgrep =
        new VncFunction(
                "sh/pgrep",
                VncFunction
                    .meta()
                    .arglists("(sh/pgrep name)")
                    .doc(
                        "Returns a list of all pids for a process with the passed name or `nil` " +
                        "if there are no processes matching the name.\n\n" +
                        "Runs the Unix command: `pgrep -x {name}`\n\n" +
                        "Note: This function is available for Linux and MacOS only!")
                    .examples("(sh/pgrep \"clamd\")")
                    .seeAlso("sh", "sh/pargs", "sh/kill", "sh/alive?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                sandboxFunctionCallValidation();

                SimpleShell.validateLinuxOrMacOSX("sh/pgrep");

                final String name = Coerce.toVncString(args.first()).getValue();

                final List<VncVal> pids = SimpleShell
                                            .pgrep(name)
                                            .stream()
                                            .map(p -> new VncString(p))
                                            .collect(Collectors.toList());

                return VncList.ofList(pids);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pargs =
        new VncFunction(
                "sh/pargs",
                VncFunction
                    .meta()
                    .arglists(
                        "(sh/pargs pid)",
                        "(sh/pargs :parse pid)")
                    .doc(
                        "Returns a process' command line.\n\n" +
                        "Without the `:parse` option returns the command line as a string or `nil` if " +
                        "there is no process matching the PID. With the `:parse` option returns a list " +
                        "of the command line arguments or an empty list the process does not exist.\n\n" +
                        "Runs the Unix command: `ps -p {pid} -ww -o args` to get the command line.\n\n" +
                        "Note: This function is available for Linux and MacOS only!")
                    .examples(
                        "(sh/pargs \"1234\")",
                        "(sh/pargs :parse \"1234\")")
                    .seeAlso("sh", "sh/pgrep",  "sh/kill", "sh/alive?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                sandboxFunctionCallValidation();

                SimpleShell.validateLinuxOrMacOSX("sh/pargs");

                final String pid = Coerce.toVncString(args.size() == 1 ? args.first() : args.second()).getValue();
                final String flag = args.size() == 1 ? null : Coerce.toVncKeyword(args.first()).getValue();

                boolean parse = false;

                if (flag != null) {
                    if (!flag.equals("parse")) {
                        throw new VncException("Invalid arguments for function 'sh/pargs'. " +
                                               "Only :parse is supported as flag!");
                    }
                    parse = true;
                }

                final String cmd = SimpleShell.pargs(pid);

                if (parse) {
                       final VncHashMap result = SmartShell.exec(
                                                   VncList.of(
                                                       new VncString("xargs"),
                                                       new VncString("printf"),
                                                       new VncString("%s\n")),
                                                   VncHashMap.of(
                                                       new VncKeyword("in"),
                                                       new VncString(cmd)),
                                                   null);

                       final long exitCode = ((VncLong)result.get(new VncKeyword(":exit"))).toJavaLong();
                       if (exitCode != 0) {
                             final String err = ((VncString)result.get(new VncKeyword(":err"))).getValue();

                             throw new VncException(
                                           "Function 'sh/pargs' failed with exit code " + exitCode + ". " +
                                           "Error: " + err);
                       }
                       else {
                           final String out = ((VncString)result.get(new VncKeyword(":out"))).getValue();
                           return VncList.ofColl(
                                     StringUtil
                                        .splitIntoLines(out)
                                        .stream()
                                        .map(s -> new VncString(s))
                                        .collect(Collectors.toList()));
                       }
                }
                else {
                    return StringUtil.isBlank(cmd) ? Constants.Nil : new VncString(cmd);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Util
    ///////////////////////////////////////////////////////////////////////////


    private static VncVector parseArgs(final VncList args) {
        final VncThreadLocal th = new VncThreadLocal();

        VncHashMap options = new VncHashMap();
        VncList cmd = VncList.empty();
        VncList args_ = args;
        while(!args_.isEmpty()) {
            final VncVal v = args_.first();
            args_ = args_.rest();

            if (Types.isVncKeyword(v) && optionKeywords.contains(v)) {
                final VncVal optVal = args_.first();
                args_ = args_.rest();
                options = options.assoc(v, optVal);
            }
            else {
                cmd = cmd.addAtEnd(new VncString(v.toString()));
            }
        }


        final VncMap defaultOptions = VncHashMap.of(
                                        new VncKeyword(":out-enc"), new VncString("UTF-8"),
                                        new VncKeyword(":in-enc"), new VncString("UTF-8"),
                                        new VncKeyword(":dir"), th.get(":*sh-dir*"),
                                        new VncKeyword(":throw-ex"), th.get(":*sh-throw-ex*"));

        final VncMap defaultEnv = (VncMap)th.get(":*sh-env*", new VncHashMap());


        // merge options
        VncMap opts = (VncMap)CoreFunctions.merge.apply(
                                VncList.of(defaultOptions, options));

        // add merged :env map
        opts = opts.assoc(
                    new VncKeyword(":env"),
                    CoreFunctions.merge.apply(
                            VncList.of(
                                    defaultEnv,
                                    options.get(new VncKeyword(":env")))));

        return VncVector.of(cmd, opts);
    }


    private static final VncHashSet optionKeywords = VncHashSet.of(
                                                        new VncKeyword(":in"),
                                                        new VncKeyword(":in-enc"),
                                                        new VncKeyword(":out-enc"),
                                                        new VncKeyword(":out-fn"),
                                                        new VncKeyword(":err-fn"),
                                                        new VncKeyword(":env"),
                                                        new VncKeyword(":dir"),
                                                        new VncKeyword(":throw-ex"),
                                                        new VncKeyword(":timeout"));


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(sh)
                    .add(open)
                    .add(pwd)
                    .add(kill)
                    .add(pgrep)
                    .add(pargs)
                    .add(alive_Q)
                    .add(load_pid)
                    .toMap();
}
