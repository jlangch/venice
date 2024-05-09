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
package com.github.jlangch.venice.impl.repl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp.Capability;
import org.jline.utils.NonBlockingReader;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.IRepl;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.javainterop.DynamicInvocationHandler;
import com.github.jlangch.venice.impl.repl.ReplConfig.ColorMode;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.Tuple2;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.util.OS;


public class ReplFunctions {

    public static Env register(
            final Env env,
            final IRepl repl,
            final Terminal terminal,
            final ReplConfig config,
            final boolean macroExpandOnLoad,
            final ReplDirs replDirs
    ) {
        Env e = env;
        for(VncFunction fn : createFunctions(env, repl, terminal, config, macroExpandOnLoad, replDirs)) {
            e = registerFn(e,fn);
        }
        return e;
    };


    private static Env registerFn(final Env env, final VncFunction fn) {
        return env.setGlobal(new Var(new VncSymbol(fn.getQualifiedName()), fn, false, Var.Scope.Global));
    }

    private static List<VncFunction> createFunctions(
            final Env env,
            final IRepl repl,
            final Terminal terminal,
            final ReplConfig config,
            final boolean macroExpandOnLoad,
            final ReplDirs replDirs
    ) {
        final List<VncFunction> fns = new ArrayList<>();

        fns.add(createReplInfoFn(terminal, config));
        fns.add(createReplRestartFn(config, macroExpandOnLoad));
        fns.add(createTermRowsFn(terminal));
        fns.add(createTermColsFn(terminal));
        fns.add(createReplHomeDirFn(replDirs));
        fns.add(createReplLibsDirFn(replDirs));
        fns.add(createPromptFn(repl));
        fns.add(setHandlerFn(repl));
        fns.add(getColorTheme(config));
        fns.add(setColorTheme(env, config));
        fns.add(catReplEnv(replDirs));
        fns.add(getReplEnv(replDirs));
        fns.add(addReplEnv(replDirs));
        fns.add(waitAnyKeyPressed(terminal));
        fns.add(exit(repl));

        return fns;
    }

    private static VncFunction createReplInfoFn(
            final Terminal terminal,
            final ReplConfig config
    ) {
        return
            new VncFunction(
                    "repl/info",
                    VncFunction
                        .meta()
                        .arglists("(repl/info)")
                        .doc(
                            "Returns information on the REPL.\n\n" +
                            "Note: This function is only available when called from " +
                            "within a REPL!\n\n" +
                            "E.g.: \n\n" +
                            "```\n" +
                            "{ :term-name \"JLine terminal\" \n" +
                            "  :term-type \"xterm-256color\" \n" +
                            "  :term-cols 80 \n" +
                            "  :term-rows 24 \n" +
                            "  :term-colors 256 \n" +
                            "  :term-class :org.repackage.org.jline.terminal.impl.PosixSysTerminal \n" +
                            "  :color-mode :light }")
                        .seeAlso(
                            "repl?", "repl/term-rows", "repl/term-cols")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    if (terminal != null) {
                        try {
                            return VncOrderedMap.of(
                                    new VncKeyword("term-name"),   new VncString(terminal.getName()),
                                    new VncKeyword("term-type"),   new VncString(terminal.getType()),
                                    new VncKeyword("term-cols"),   new VncLong(terminal.getSize().getColumns()),
                                    new VncKeyword("term-rows"),   new VncLong(terminal.getSize().getRows()),
                                    new VncKeyword("term-colors"), new VncLong(terminal.getNumericCapability(Capability.max_colors)),
                                    new VncKeyword("term-class"),  new VncKeyword(terminal.getClass().getName()),
                                    new VncKeyword("color-mode"),  new VncKeyword(config.getColorMode().toString().toLowerCase()));
                        }
                        catch(Exception ex) {
                            throw new VncException("Failed to get the REPL terminal info", ex);
                        }
                    }
                    else {
                        return VncOrderedMap.of(
                                new VncKeyword("term-name"),   new VncString("unknown"),
                                new VncKeyword("term-type"),   new VncString("unknown"),
                                new VncKeyword("term-cols"),   new VncLong(0),
                                new VncKeyword("term-rows"),   new VncLong(0),
                                new VncKeyword("term-colors"), new VncLong(0),
                                new VncKeyword("term-class"),  new VncKeyword("unknown"),
                                new VncKeyword("color-mode"),  new VncKeyword("unknown"));
                    }
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction createReplRestartFn(
            final ReplConfig config,
            final boolean macroExpandOnLoad
    ) {
        return
            new VncFunction(
                    "repl/restart",
                    VncFunction
                        .meta()
                        .arglists("(repl/restart)")
                        .doc(
                            "Restarts the REPL.\n\n" +
                            "Note: This function is only available when called from " +
                            "within a REPL!")
                        .seeAlso(
                            "repl?")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    ReplRestart.restart(macroExpandOnLoad, config.getColorMode());

                    return Constants.Nil;
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction createTermRowsFn(final Terminal terminal) {
        return
            new VncFunction(
                    "repl/term-rows",
                    VncFunction
                        .meta()
                        .arglists("(repl/term-rows)")
                        .doc(
                            "Returns number of rows in the REPL terminal.\n\n" +
                            "Note: This function is only available when called from " +
                            "within a REPL!")
                        .seeAlso(
                            "repl?", "repl/term-cols", "repl/info")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    return new VncLong(terminal == null ? 0 : terminal.getSize().getRows());
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction createTermColsFn(final Terminal terminal) {
        return
            new VncFunction(
                    "repl/term-cols",
                    VncFunction
                        .meta()
                        .arglists("(repl/term-cols)")
                        .doc(
                            "Returns number of columns in the REPL terminal.\n\n" +
                            "Note: This function is only available when called from " +
                            "within a REPL!")
                        .seeAlso(
                            "repl?", "repl/term-rows", "repl/info")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    return new VncLong(terminal == null ? 0 : terminal.getSize().getColumns());
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction createReplHomeDirFn(final ReplDirs replDirs) {
        return
            new VncFunction(
                    "repl/home-dir",
                    VncFunction
                        .meta()
                        .arglists("(repl/home-dir)")
                        .doc(
                            "Returns the REPL home directory.\n\n" +
                            "Note: This function is only available when called from " +
                            "within a REPL!")
                        .seeAlso(
                            "repl?", "repl/libs-dir")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    return replDirs.getHomeDir() == null
                            ? Constants.Nil
                            : new VncJavaObject(replDirs.getHomeDir());
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction createReplLibsDirFn(final ReplDirs replDirs) {
        return
            new VncFunction(
                    "repl/libs-dir",
                    VncFunction
                        .meta()
                        .arglists("(repl/libs-dir)")
                        .doc(
                            "Returns the REPL libs directory\n\n" +
                            "Note: This function is only available when called from " +
                            "within a REPL!")
                        .seeAlso(
                            "repl?", "repl/home-dir")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    return replDirs.getLibsDir() == null
                            ? Constants.Nil
                            : new VncJavaObject(replDirs.getLibsDir());
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction getColorTheme(final ReplConfig config) {
        return
            new VncFunction(
                    "repl/color-theme",
                    VncFunction
                        .meta()
                        .arglists("(repl/color-theme)")
                        .doc(
                            "Returns REPL's color theme (:light, :dark, :none) ")
                        .examples(
                            "(repl/color-theme)")
                        .seeAlso(
                            "repl?", "repl/color-theme!", "repl/prompt!", "repl/handler!",
                            "repl/info")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    final ColorMode theme = config.getColorMode();

                    return new VncKeyword(theme.name().toLowerCase());
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction setColorTheme(final Env env, final ReplConfig config) {
        return
            new VncFunction(
                    "repl/color-theme!",
                    VncFunction
                        .meta()
                        .arglists("(repl/color-theme! theme)")
                        .doc(
                            "Set the REPL's color theme (:light, :dark) ")
                        .examples(
                            "(repl/color-theme!)")
                        .seeAlso(
                            "repl?", "repl/color-theme", "repl/prompt!", "repl/handler!",
                            "repl/info")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    final VncKeyword theme = Coerce.toVncKeyword(args.first());

                    final ColorMode mode;

                    switch(theme.getSimpleName()) {
                        case "light": mode = ColorMode.Light; break;
                        case "dark":  mode = ColorMode.Dark;  break;
                        case "none":  mode = ColorMode.None;  break;
                        default:      mode = ColorMode.Light; break;
                    }

                    config.switchColorMode(mode);

                    return new VncKeyword(mode.name().toLowerCase());
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction createPromptFn(final IRepl repl) {
        return
            new VncFunction(
                    "repl/prompt!",
                    VncFunction
                        .meta()
                        .arglists("(repl/prompt! s)")
                        .doc(
                            "Sets the REPL prompt string")
                        .examples(
                            "(repl/prompt! \"venice> \")")
                        .seeAlso(
                            "repl?", "repl/handler!", "repl/color-theme", "repl/info")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    final String prompt = Coerce.toVncString(args.first()).getValue();

                    if (repl != null) {
                        repl.setPrompt(prompt);
                    }

                    return Constants.Nil;
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction catReplEnv(final ReplDirs replDirs) {
        return
            new VncFunction(
                    "repl/cat-env",
                    VncFunction
                        .meta()
                        .arglists("(repl/cat-env)")
                        .doc(
                            "Returns the content of the REPL's local env file.\n\n" +
                            "Note: This function is only available when called from " +
                            "within a REPL!")
                        .examples(
                            "(printl (repl/cat-env))")
                        .seeAlso(
                            "repl?",
                            "repl/home-dir",
                            "repl/get-env",
                            "repl/add-env")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                   final List<String> lines = loadReplEnv(replDirs);

                   return new VncString(String.join("\n", lines));
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction getReplEnv(final ReplDirs replDirs) {
        return
            new VncFunction(
                    "repl/get-env",
                    VncFunction
                        .meta()
                        .arglists("(repl/get-env name)")
                        .doc(
                            "Returns the value of a REPL local env var.\n\n" +
                            "Note: This function is only available when called from " +
                            "within a REPL!")
                        .examples(
                            "(repl/get-env \"DEMO\")")
                        .seeAlso(
                            "repl?",
                            "repl/home-dir",
                            "repl/cat-env",
                            "repl/add-env")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    final String name = Coerce.toVncString(args.first()).getValue();

                    final String value = parseReplEnvVars(replDirs)
                                            .stream()
                                            .filter(v -> name.equals(v.getFirst()))
                                            .map(v -> v.getSecond())
                                            .findFirst()
                                            .orElse(null);

                    return value == null ? Constants.Nil : new VncString(value);
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction addReplEnv(final ReplDirs replDirs) {
        return
            new VncFunction(
                    "repl/add-env",
                    VncFunction
                        .meta()
                        .arglists("(repl/add-env! name value)")
                        .doc(
                            "Add (or replace) an env var to REPL local env file.\n\n" +
                            "DO NO FORGET to restart the REPL after adding an env var!\n\n" +
                            "Note: This function is only available when called from " +
                            "within a REPL!\n\n\n" +
                            "**Example**                      \n\n" +
                            "*1. Add env var:*                \n\n" +
                            "```                              \n" +
                            "(repl/add-env \"DEMO\" \"100\")  \n" +
                            "```                              \n" +
                            "*2. Restart the REPL:*           \n\n" +
                            "```                              \n" +
                            "venice> !restart                 \n" +
                            "```                              \n" +
                            "*3. Test:*                       \n\n" +
                            "```                              \n" +
                            "(system-env \"DEMO\")            \n" +
                            "```                              ")
                        .examples(
                            "(repl/add-env \"DEMO\")")
                        .seeAlso(
                            "repl?",
                            "repl/home-dir",
                            "repl/get-env",
                            "repl/cat-env")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 2);

                    final String name = Coerce.toVncString(args.first()).getValue().trim();
                    final String value = Coerce.toVncString(args.second()).getValue().trim();

                    final List<String> out = new ArrayList<>();

                    boolean replaced = false;

                    for(String line : loadReplEnv(replDirs)) {
                        if (isEnvVarLine(line)) {
                            final String[] nv = splitEnvVarLine(line);
                            if (nv.length == 2 && name.equals(nv[0].trim())) {
                                // replace
                                out.add(renderEnvVarLine(name, value));
                                replaced = true;
                                continue;
                            }
                        }

                        out.add(line);
                    }

                    if (!replaced) {
                        // add the new env var at the end of the file
                        out.add(renderEnvVarLine(name, value));
                    }

                    try {
                        Files.write(
                            replEnvFile(replDirs).toPath(),
                            out,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to updated the REPL's env file!");
                    }

                    return Constants.Nil;
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction setHandlerFn(final IRepl repl) {
        return
            new VncFunction(
                    "repl/handler!",
                    VncFunction
                        .meta()
                        .arglists("(repl/handler! f)")
                        .doc(
                            "Sets the REPL command handler")
                        .examples(
                            "(do                                \n" +
                            "  (defn handle-command [cmd]       \n" +
                            "     ;; run the command 'cmd'      \n" +
                            "     (println \"Demo:\" cmd))      \n" +
                            "                                   \n" +
                            "  (repl/handler! handle-command))   ")
                        .seeAlso(
                            "repl?", "repl/prompt!", "repl/color-theme", "repl/info")
                        .build()
            ) {
                @Override
                @SuppressWarnings("unchecked")
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    final VncFunction handlerFn = Coerce.toVncFunction(args.first());

                    if (repl != null) {
                        final Object handler =
                                DynamicInvocationHandler.proxify(
                                    new CallFrame("proxify(:" + Consumer.class.getName() +")", args.getMeta()),
                                    Consumer.class,
                                    VncHashMap.of(new VncString("accept"), handlerFn));

                        repl.setHandler((Consumer<String>)handler);
                    }

                    return Constants.Nil;
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction waitAnyKeyPressed(final Terminal terminal) {
        return
            new VncFunction(
                    "repl/wait-any-key-pressed",
                    VncFunction
                        .meta()
                        .arglists("(repl/wait-any-key-pressed)")
                        .doc(
                            "Returns REPL's color theme (:light, :dark, :none) ")
                        .examples(
                            "(repl/color-theme)")
                        .seeAlso(
                            "repl?", "repl/color-theme!", "repl/prompt!", "repl/handler!",
                            "repl/info")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    terminal.enterRawMode();
                    NonBlockingReader reader = terminal.reader();

                    try {
                        final int c = reader.read();

                        return new VncChar((char)c);
                    }
                    catch(IOException ex) {
                        return Constants.Nil;
                    }
                }

                private static final long serialVersionUID = -1L;
            };
    }

    private static VncFunction exit(final IRepl repl) {
        return
            new VncFunction(
                    "repl/exit!",
                    VncFunction
                        .meta()
                        .arglists("(repl/exit!)")
                        .doc(
                            "Exit from the REPL")
                        .examples(
                            "(repl/exit!)")
                        .seeAlso(
                            "repl?", "repl/prompt!", "repl/color-theme", "repl/info")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 0);

                    throw new EofException("exit");
                }

                private static final long serialVersionUID = -1L;
            };
    }


    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    private static File replEnvFile(final ReplDirs replDirs) {
        return new File(
                replDirs.getHomeDir(),
                OS.isWindows() ? "repl.env.bat" : "repl.env");
    }

    private static List<Tuple2<String,String>> parseReplEnvVars(final ReplDirs replDirs) {
        return loadReplEnv(replDirs)
                   .stream()
                   .filter(l -> isEnvVarLine(l))
                   .map(l -> splitEnvVarLine(l))
                   .filter(nv -> nv.length == 2)
                   .map(nv -> new Tuple2<String,String>(nv[0].trim(), nv[1].trim()))
                   .collect(Collectors.toList());
    }

    private static List<String> loadReplEnv(final ReplDirs replDirs) {
        final File envFile = replEnvFile(replDirs);

        if (envFile.canRead()) {
            try {
                return Files.readAllLines(envFile.toPath());
            }
            catch(Exception ex) {
                throw new VncException("Failed to load the REPL's env file!", ex);
            }
        }
        else {
            throw new VncException("The REPL's env file does not exist");
        }
    }


    private static boolean isEnvVarLine(final String line) {
        return line.matches("^(export|set) *[^=] *.*$");
    }

    private static String removeEnvVarPrefix(final String line) {
        String l = line;
        l = StringUtil.removeStart(l, "export ");
        l = StringUtil.removeStart(l, "set ");
        return l;
    }

    private static final String[] splitEnvVarLine(final String line) {
        return removeEnvVarPrefix(line).split("=");
    }

    private static final String renderEnvVarLine(final String name, final String value) {
        return String.format(
                "%s %s=%s",
                OS.isWindows() ? "set" : "export",
                name.trim(),
                value.trim());
    }


}
