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

import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.functions.ModuleFunctions;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


/**
 * Special form code laoding functions
 *
 * Special forms have evaluation rules that differ from standard Venice
 * evaluation rules and are understood directly by the Venice interpreter.
 */
public class SpecialForms_LoadCodeFunctions {

    public static VncSpecialForm load_module =
        new VncSpecialForm(
                "load-module",
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
                assertArity("load-module", FnType.SpecialForm, args, 1, 3);

                synchronized (this) {
                    final VncKeyword moduleName = Coerce.toVncKeyword(args.first());
                    VncBoolean force = VncBoolean.False;
                    VncVector alias = null;

                    if (args.size() == 2) {
                        if (Types.isVncBoolean(args.second())) {
                            force = Coerce.toVncBoolean(args.second());
                        }
                        else if (Types.isVncVector(args.second())) {
                            alias = Coerce.toVncVector(args.second());
                        }
                        else {
                            throw new VncException(String.format(
                                    "Function 'load-module' does not allow %s as 2nd arg! " +
                                    "Expected a force flag or a ns alias.",
                                    Types.getType(args.second())));
                        }
                    }
                    else {
                        if (Types.isVncBoolean(args.second())) {
                            force = Coerce.toVncBoolean(args.second());
                        }
                        else {
                            throw new VncException(String.format(
                                    "Function 'load-module' does not allow %s as 2nd arg! " +
                                    "Expected a force flag.",
                                    Types.getType(args.second())));
                        }
                        if (Types.isVncVector(args.third())) {
                            alias = Coerce.toVncVector(args.third());
                        }
                        else {
                            throw new VncException(String.format(
                                    "Function 'load-module' does not allow %s as 3rd arg! " +
                                    "Expected a ns alias.",
                                    Types.getType(args.third())));
                        }
                    }

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

                        CoreFunctions.ns_alias.applyOf(alias.first(), alias.third());
                    }

                    return VncVector
                                .empty()
                                .addAtEnd(moduleName)
                                .addAtEnd(new VncKeyword(load ? "loaded" : "already-loaded"));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    private static void validateNsAlias(
            final String caller,
            final VncVector alias_
    ) {
        final boolean ok = (alias_.size() == 3)
                            && (Types.isVncSymbol(alias_.first()))
                            && (Types.isVncKeyword(alias_.second()))
                            && "as".equals(Coerce.toVncKeyword(alias_.second()).getValue())
                            && (Types.isVncSymbol(alias_.third()));

        if (!ok) {
            throw new VncException(
                        String.format(
                                "Invalid ns alias definition '%s' for %s!",
                                Printer.pr_str(alias_, true),
                                caller));
        }
    }

    private static VncSet getLoadedModules(final Env env) {
        return Coerce.toVncSet(env.getGlobalOrNull(new VncSymbol("*loaded-modules*")));
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(load_module)
                    .toMap();
}
