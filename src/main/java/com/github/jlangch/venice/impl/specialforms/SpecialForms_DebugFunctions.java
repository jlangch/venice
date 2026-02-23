/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.util.ArityExceptions.assertArity;

import java.io.PrintStream;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


/**
 * The special form pseudo functions
 *
 * Special forms have evaluation rules that differ from standard Venice
 * evaluation rules and are understood directly by the Venice interpreter.
 */
public class SpecialForms_DebugFunctions {

    public static VncSpecialForm debug =
        new VncSpecialForm(
                "debug",
                VncSpecialForm
                    .meta()
                    .arglists("(debug cmd)")
                    .doc("Debugger")
                    .examples(
                            "(debug :attach)",
                            "(debug :detach)")
                    .build()
        ) {
            @Override
            public VncVal apply(
                    final VncVal specialFormMeta,
                    final VncList args,
                    final Env env,
                    final SpecialFormsContext ctx
            ) {
                specialFormCallValidation(ctx, "debug");
                assertArity("debug", FnType.SpecialForm, args, 1);

                final VncKeyword cmd = Coerce.toVncKeyword(args.first());

                switch(cmd.getSimpleName()) {
                    case "attach":     return attach(env);
                    case "detach":     return detach(env);
                    case "terminate":  return terminate(env);
                    case "info":       return info(env, ctx);
                    default:
                        throw new VncException("Invalid debugger command '" + cmd + "'");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };





        ///////////////////////////////////////////////////////////////////////////
        // Commands
        ///////////////////////////////////////////////////////////////////////////

        private static VncVal attach(final Env env) {
            final PrintStream out = getStdOutPrintStream(env);
            final PrintStream err = getStdErrPrintStream(env);

            if (DebugAgent.isAttached()) {
                err.println("Debugger already attached");
            }
            else {
                final DebugAgent agent = new DebugAgent();
                DebugAgent.register(agent);
                agent.restoreBreakpoints();
                out.println("Debugger attached");
            }
            return Nil;
        }

        private static VncVal detach(final Env env) {
            final PrintStream out = getStdOutPrintStream(env);
            final PrintStream err = getStdErrPrintStream(env);

            final DebugAgent agent = DebugAgent.current();
            if (agent != null) {
                agent.storeBreakpoints();
                agent.detach();
                DebugAgent.unregister();
                out.println("Debugger detached");
            }
            else {
                err.println("Debugger not attached");
            }
            return Nil;
        }

        private static VncVal terminate(final Env env) {
            final DebugAgent agent = DebugAgent.current();
            if (agent != null) {
                agent.clearBreaks();
            }
            return Nil;
        }

        private static VncVal info(
                final Env env,
                final SpecialFormsContext ctx
        ) {
            final PrintStream out = getStdOutPrintStream(env);
            final PrintStream err = getStdErrPrintStream(env);

            if (DebugAgent.isAttached()) {
                final DebugAgent agent = getDebugAgent(ctx);
                out.println(agent.toString());
                out.println("Current CallFrame:  "
                                            + (agent.hasCurrCallFrame()
                                                    ? agent.getCurrCallFrame()
                                                    : "-"));
            }
            else {
                err.println("Debugger not attached");
            }
            return Nil;
        }


        ///////////////////////////////////////////////////////////////////////////
        // Utils
        ///////////////////////////////////////////////////////////////////////////

        private static PrintStream getStdOutPrintStream(final Env env) {
            final VncVal out = env.get(new VncSymbol("*out*"));
            return Types.isVncJavaObject(out, PrintStream.class)
                                ? Coerce.toVncJavaObject(out, PrintStream.class)
                                : System.out;
        }

        private static PrintStream getStdErrPrintStream(final Env env) {
            final VncVal out = env.get(new VncSymbol("*err*"));
            return Types.isVncJavaObject(out, PrintStream.class)
                                ? Coerce.toVncJavaObject(out, PrintStream.class)
                                : System.err;
        }

        private static DebugAgent getDebugAgent(final SpecialFormsContext ctx) {
            return ((VeniceInterpreter)ctx.getInterpreter()).getDebugAgent();
        }


        ///////////////////////////////////////////////////////////////////////////
        // types_ns is namespace of type functions
        ///////////////////////////////////////////////////////////////////////////

        public static final Map<VncVal, VncVal> ns =
                new SymbolMapBuilder()
                        .add(debug)
                        .toMap();
}
