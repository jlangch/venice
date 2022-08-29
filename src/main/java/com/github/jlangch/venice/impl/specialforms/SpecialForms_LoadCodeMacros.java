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
package com.github.jlangch.venice.impl.specialforms;

import static com.github.jlangch.venice.impl.specialforms.util.SpecialFormsUtil.specialFormCallValidation;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.functions.ModuleFunctions;
import com.github.jlangch.venice.impl.namespaces.Namespace;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


// ****************************************************************************
// *  To activate the special form macros:
// *
// *    - enable in VeniceInterpreter::doMacroexpand(), line 905
// *    - remove from core.venice, line 1090
// *
// *  Problems
// *    - Precompilation with module loading and alias -> alias not working
// *      see PrecompiledTest::test_ns_alias_2()
// ****************************************************************************

/**
 * Special form code loading functions
 *
 * Special forms have evaluation rules that differ from standard Venice
 * evaluation rules and are understood directly by the Venice interpreter.
 */
public class SpecialForms_LoadCodeMacros {

    public static VncSpecialForm load_string =
        new VncSpecialForm(
                "load-string",
                VncSpecialForm
                    .meta()
                    .arglists(
                        "(load-string s)")
                    .doc(
                        "Sequentially read and evaluate the set of forms contained in the string.")
                    .examples(
                        "(do                             \n" +
                        "  (load-string \"(def x 1)\")   \n" +
                        "  (+ x 2))                      ")
                    .seeAlso(
                        "load-file", "load-classpath-file", "loaded-modules")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation("load-string");
                assertArity("load-string", FnType.SpecialForm, args, 1);

                final Namespace currNS = Namespaces.getCurrentNamespace();
                try {
                    synchronized (this) {
                        final VncString s = Coerce.toVncString(args.first());

                        @SuppressWarnings("unused")
                        VncVal ast = ctx.getInterpreter().RE(
                                            "(do " + s.getValue() + ")",
                                            "string",
                                            env);

                        return VncVector
                                    .empty()
                                    .addAtEnd(new VncKeyword("string"))
                                    .addAtEnd(new VncKeyword("loaded"));
                    }
                }
                finally {
                     Namespaces.setCurrentNamespace(currNS);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncSpecialForm load_module =
        new VncSpecialForm(
                "load-module2",
                VncSpecialForm
                    .meta()
                    .arglists(
                        "(load-module m)",
                        "(load-module m force)",
                        "(load-module m nsalias)",
                        "(load-module m force nsalias)")
                    .doc(
                        "Loads a Venice predefined extension module.\n\n" +
                        "Returns a tuple with the module's name and the keyword `:loaded` " +
                        "if the module has been successfully loaded or `:already-loaded` " +
                        "if the module has been already loaded. Throws an exception on any "  +
                        "loading error.\n\n" +
                        "With 'force' set to `false` (the default) the module is only loaded " +
                        "once and interpreted once. Subsequent load attempts will be skipped. " +
                        "With 'force' set to `true` it is always loaded and interpreted.\n\n" +
                        "Loaded modules are cached by Venice and subsequent loads are just " +
                        "skipped. To enforce a reload call the module load with the force " +
                        "flag set to true: `(load-module :hexdump true)`\n\n" +
                        "An optional namespace alias can passed:\n" +
                        "`(load-module :hexdump ['hexdump :as 'h])`")
                    .examples(
                        "(load-module :trace) ",

                        ";; loading the :trace modul and define a ns alias 't for namespace \n" +
                        ";; 'trace used in the module                                       \n" +
                        "(load-module :trace ['trace :as 't])                               ",

                        ";; reloading a module          \n" +
                        "(do                            \n" +
                        "  (load-module :trace)         \n" +
                        "  ; reload the module          \n" +
                        "  (ns-remove 'trace)           \n" +
                        "  (load-module :trace true))   ",

                        ";; namespace aliases                       \n" +
                        "(do                                        \n" +
                        "  (load-module :hexdump ['hexdump :as 'h]) \n" +
                        "  (h/dump (range 32 64)))                  ")
                    .seeAlso(
                        "load-file", "load-classpath-file", "load-string", "loaded-modules")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation("load-module");
                assertArity("load-module", FnType.SpecialForm, args, 1, 2, 3);


                final Namespace currNS = Namespaces.getCurrentNamespace();
                try {
                    synchronized (this) {
                        final VncKeyword moduleName = Coerce.toVncKeyword(args.first());
                        final Options options = parseOptions(args, "load-module");
                        final VncBoolean force = options.force;
                        final VncVector alias = options.alias;

                        final VncSet loadedModules = getLoadedModules(env);

                        final boolean load = VncBoolean.isTrue(force) || !loadedModules.contains(moduleName);

                        if (load) {
                            final VncString code = (VncString)ModuleFunctions.loadModule.applyOf(moduleName);

                            @SuppressWarnings("unused")
                            VncVal ast = ctx.getInterpreter().RE(
                                                "(do " + code.getValue() + ")",
                                                moduleName.getValue(),
                                                env);

                            loadedModules.add(moduleName);
                        }

                        if (alias != null) {
                                validateNsAlias(
                                    String.format("load-module '%s'", moduleName.getValue()),
                                    alias);

                                final VncSymbol aliasName = unquoteSymbol(alias.third());
                                final VncSymbol nsName = unquoteSymbol(alias.first());
                                currNS.addAlias(aliasName.getName(), nsName.getName());
                        }

                        return VncVector
                                    .empty()
                                    .addAtEnd(moduleName)
                                    .addAtEnd(new VncKeyword(load ? "loaded" : "already-loaded"));
                    }
                }
                finally {
                     Namespaces.setCurrentNamespace(currNS);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncSpecialForm load_file =
        new VncSpecialForm(
                "load-file",
                VncSpecialForm
                    .meta()
                    .arglists(
                        "(load-file f)",
                        "(load-file f force)",
                        "(load-file f nsalias)",
                        "(load-file f force nsalias)")
                    .doc(
                        "Sequentially read and evaluate the set of forms contained in the file.\n\n" +
                        "If the file is found on one of the defined load paths it is read and " +
                        "the forms it contains are evaluated. If the file is not found an " +
                        "exception is raised.\n\n" +
                        "Returns a tuple with the file's name and the keyword `:loaded` " +
                        "if the file has been successfully loaded or `:already-loaded` " +
                        "if the file has been already loaded. Throws an exception on any " +
                        "loading error.\n\n" +
                        "With 'force' set to `false` (the default) the file is only loaded " +
                        "once and interpreted once. Subsequent load attempts will be skipped. " +
                        "With 'force' set to `true` it is always loaded and interpreted.\n\n" +
                        "The function is restricted to load files with the extension '.venice'. " +
                        "If the file extension is missing '.venice' will be implicitely added.\n\n" +
                        "An optional namespace alias can passed:\n" +
                        "`(load-file \"coffee.venice\" ['coffee :as 'c])`\n\n" +
                        "See the `load-paths` doc for a description of the *load path* feature.")
                    .examples(
                        ";; With load-paths: [/users/foo/scripts]                              \n" +
                        ";;        -> loads: /users/foo/scripts/coffee.venice                  \n" +
                        "(load-file \"coffee\")                                                ",

                        ";; With load-paths: [/users/foo/scripts]                              \n" +
                        ";;        -> loads: /users/foo/scripts/coffee.venice                  \n" +
                        "(load-file \"coffee.venice\")                                         ",

                        ";; With load-paths: [/users/foo/scripts]                              \n" +
                        ";;        -> loads: /users/foo/scripts/beverages/coffee.venice        \n" +
                        "(load-file \"beverages/coffee\")                                      ",

                        ";; With load-paths: [/users/foo/resources.zip]                        \n" +
                        ";;        -> loads: /users/foo/resources.zip!beverages/coffee.venice  \n" +
                        "(load-file \"beverages/coffee\")                                      ")
                    .seeAlso(
                        "load-classpath-file", "load-string", "load-module", "load-paths")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation("load-file");
                assertArity("load-file", FnType.SpecialForm, args, 1, 3);

                final Namespace currNS = Namespaces.getCurrentNamespace();
                try {
                    synchronized (this) {
                        final VncString fileName = getVeniceFile(env, args.first(), "load-file");
                        final Options options = parseOptions(args, "load-file");
                        final VncBoolean force = options.force;
                        final VncVector alias = options.alias;

                        final VncSet loadedFunction = getLoadedFunctions(env);

                        final boolean load = VncBoolean.isTrue(force) || !loadedFunction.contains(fileName);

                        if (load) {
                            final VncString code = (VncString)ModuleFunctions.loadFile.applyOf(fileName);

                            @SuppressWarnings("unused")
                            VncVal ast = ctx.getInterpreter().RE(
                                                "(do " + code.getValue() + ")",
                                                fileName.getValue(),
                                                env);

                            loadedFunction.add(fileName);
                        }

                        if (alias != null) {
                            validateNsAlias(
                                    String.format("load-file '%s'", fileName.getValue()),
                                    alias);

                                final VncSymbol aliasName = unquoteSymbol(alias.third());
                                final VncSymbol nsName = unquoteSymbol(alias.first());
                                currNS.addAlias(aliasName.getName(), nsName.getName());
                        }

                        return VncVector
                                    .empty()
                                    .addAtEnd(fileName)
                                    .addAtEnd(new VncKeyword(load ? "loaded" : "already-loaded"));
                    }
                }
                finally {
                     Namespaces.setCurrentNamespace(currNS);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncSpecialForm load_classpath_file =
        new VncSpecialForm(
                "load-classpath-file",
                VncSpecialForm
                    .meta()
                    .arglists(
                        "(load-classpath-file f)",
                        "(load-classpath-file f force)",
                        "(load-classpath-file f nsalias)",
                        "(load-classpath-file f force nsalias)")
                    .doc(
                        "Sequentially read and evaluate the set of forms contained in the " +
                        "classpath file. The function is restricted to classpath files with " +
                        "the extension '.venice'.\n\n" +
                        "Returns a tuple with the file's name and the keyword `:loaded` " +
                        "if the file has been successfully loaded or `:already-loaded` " +
                        "if the file has been already loaded. Throws an exception on any " +
                        "loading error.\n\n" +
                        "With 'force' set to `false` (the default) the file is only loaded " +
                        "once and interpreted once. Subsequent load attempts will be skipped. " +
                        "With 'force' set to `true` it is always loaded and interpreted.\n\n" +
                        "Loaded files are cached by Venice and subsequent loads are just " +
                        "skipped. To enforce a reload call the file load with the force " +
                        "flag set to true:\n" +
                        "`(load-classpath-file \"com/github/jlangch/venice/test.venice\" true)`\n\n" +
                        "An optional namespace alias can passed:\n" +
                        "`(load-classpath-file \"com/github/jlangch/venice/test.venice\" ['test :as 't])`")
                    .examples(
                        "(do                                                                              \n" +
                        "  (load-classpath-file \"com/github/jlangch/venice/test.venice\")                \n" +
                        "  (test/test-fn \"hello\"))                                                      ",

                        "(do                                                                              \n" +
                        "  (load-classpath-file \"com/github/jlangch/venice/test.venice\")                \n" +
                        "  (test/test-fn \"hello\")                                                       \n" +
                        "  ; reload the classpath file                                                    \n" +
                        "  (ns-remove 'test)                                                              \n" +
                        "  (load-classpath-file \"com/github/jlangch/venice/test.venice\" true)           \n" +
                        "  (test/test-fn \"hello\"))                                                      ",

                        ";; namespace aliases                                                             \n" +
                        "(do                                                                              \n" +
                        "  (load-classpath-file \"com/github/jlangch/venice/test.venice\" ['test :as 't]) \n" +
                        "  (t/test-fn \"hello\"))                                                         ")
                    .seeAlso(
                        "load-file", "load-string", "load-module")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation("load-classpath-file");
                assertArity("load-classpath-file", FnType.SpecialForm, args, 1, 3);

                final Namespace currNS = Namespaces.getCurrentNamespace();
                try {
                    synchronized (this) {
                        final VncString fileName = getVeniceFile(env, args.first(), "load-classpath-file");
                        final Options options = parseOptions(args, "load-classpath-file");
                        final VncBoolean force = options.force;
                        final VncVector alias = options.alias;

                        final VncSet loadedFunction = getLoadedFunctions(env);

                        final boolean load = VncBoolean.isTrue(force) || !loadedFunction.contains(fileName);

                        if (load) {
                            final VncString code = (VncString)ModuleFunctions.loadClasspathFile.applyOf(fileName);

                            @SuppressWarnings("unused")
                            VncVal ast = ctx.getInterpreter().RE(
                                                "(do " + code.getValue() + ")",
                                                fileName.getValue(),
                                                env);

                            loadedFunction.add(fileName);
                        }

                        if (alias != null) {
                            validateNsAlias(
                                    String.format("load-classpath-file '%s'", fileName.getValue()),
                                    alias);

                                final VncSymbol aliasName = unquoteSymbol(alias.third());
                                final VncSymbol nsName = unquoteSymbol(alias.first());
                                currNS.addAlias(aliasName.getName(), nsName.getName());
                        }

                        return VncVector
                                    .empty()
                                    .addAtEnd(fileName)
                                    .addAtEnd(new VncKeyword(load ? "loaded" : "already-loaded"));
                    }
                }
                finally {
                     Namespaces.setCurrentNamespace(currNS);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    private static void validateNsAlias(
            final String caller,
            final VncVector alias_
    ) {

        final boolean ok = alias_.size() == 3
                            && (Types.isVncSymbol(alias_.first()) || isQuotedSymbol(alias_.first()))
                            && Types.isVncKeyword(alias_.second())  && "as".equals(Coerce.toVncKeyword(alias_.second()).getValue())
                            && (Types.isVncSymbol(alias_.third()) || isQuotedSymbol(alias_.third()));

        if (!ok) {
            throw new VncException(
                        String.format(
                                "Invalid ns alias definition '%s' for %s!",
                                Printer.pr_str(alias_, true),
                                caller));
        }
    }

    private static Options parseOptions(final VncList args, final String fnName) {
        VncBoolean force =  VncBoolean.False;
        VncVector alias = null;

        if (args.size() == 1) {
            // ok, defaults
        }
        else if (args.size() == 2) {
            if (Types.isVncBoolean(args.second())) {
                force = Coerce.toVncBoolean(args.second());
            }
            else if (Types.isVncVector(args.second())) {
                alias = Coerce.toVncVector(args.second());
            }
            else {
                throw new VncException(String.format(
                        "Function '%s' does not allow %s as 2nd arg! Expected a force flag or a ns alias.",
                        fnName,
                        Types.getType(args.second())));
            }
        }
        else {
            if (Types.isVncBoolean(args.second())) {
                force = Coerce.toVncBoolean(args.second());
            }
            else {
                throw new VncException(String.format(
                        "Function '%s' does not allow %s as 2nd arg! Expected a force flag.",
                        fnName,
                        Types.getType(args.second())));
            }
            if (Types.isVncVector(args.third())) {
                alias = Coerce.toVncVector(args.third());
            }
            else {
                throw new VncException(String.format(
                        "Function '%s' does not allow %s as 3rd arg! Expected a ns alias.",
                        fnName,
                        Types.getType(args.third())));
            }
        }

        return new Options(force, alias);
    }

    private static VncSet getLoadedModules(final Env env) {
        return Coerce.toVncSet(env.getGlobalOrNull(new VncSymbol("*loaded-modules*")));
    }

    private static VncSet getLoadedFunctions(final Env env) {
        return Coerce.toVncSet(env.getGlobalOrNull(new VncSymbol("*loaded-functions*")));
    }

    private static VncString getVeniceFile(final Env env, final VncVal arg, final String fnName) {
        if (Types.isVncSymbol(arg)) {
        	// lookup symbol
        	final VncVal v = env.get((VncSymbol)arg);
            return getVeniceFile(v, fnName);
        }
        else {
        	return getVeniceFile(arg, fnName);
        }
    }

    private static VncString getVeniceFile(final VncVal arg, final String fnName) {
        if (Types.isVncString(arg)) {
            return addVeniceFileExte((VncString)arg);
        }
        else if (Types.isVncJavaObject(arg, File.class)) {
            return addVeniceFileExte(
                    new VncString(Coerce.toVncJavaObject(arg, File.class).getPath()));
        }
        else if (Types.isVncJavaObject(arg, Path.class)) {
            return addVeniceFileExte(
                        new VncString(Coerce.toVncJavaObject(arg, Path.class).toFile().getPath()));
        }
        else {
            throw new VncException(String.format(
                    "Function '%s' does not allow %s as 1st arg! Expected a string or file.",
                    fnName,
                    Types.getType(arg)));
        }
    }

    private static VncString addVeniceFileExte(final VncString f) {
        String s = f.getValue();
        return s.endsWith(".venice") ? f : new VncString(s + ".venice");
    }

    private static boolean isQuotedSymbol(final VncVal v) {
       if (Types.isVncSequence(v)) {
            VncSequence seq = (VncSequence)v;
            if (seq.size() == 2) {
                   VncVal v1 = seq.first();
                   VncVal v2 = seq.second();

                   return Types.isVncSymbol(v1)
                           && "quote".equals(((VncSymbol)v1).getValue())
                           && Types.isVncSymbol(v2);
            }
       }

        return false;
    }

    private static VncSymbol unquoteSymbol(final VncVal v) {
        if (Types.isVncSymbol(v)) {
            return (VncSymbol)v;
        }
        else if (isQuotedSymbol(v)) {
             VncSequence seq = (VncSequence)v;
             return (VncSymbol)seq.second();
        }
        else {
            throw new VncException("Invalid namespace alias.");
        }
     }

    private static class Options {
        public Options(final VncBoolean force, final VncVector alias) {
            this.force = force;
            this.alias = alias;
        }

        final VncBoolean force;
        final VncVector alias;
    }



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static boolean ENABLED = false;

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
//                    .add(load_string)
//                    .add(load_module)
//                    .add(load_file)
//                    .add(load_classpath_file)
                    .toMap();
}
