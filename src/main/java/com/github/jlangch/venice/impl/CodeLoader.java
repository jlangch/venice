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
package com.github.jlangch.venice.impl;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.modules.ModuleLoader;
import com.github.jlangch.venice.impl.namespaces.Namespace;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class CodeLoader {

    public CodeLoader() {
    }

    public boolean loadModule(
            final VncKeyword module,
            final IVeniceInterpreter venice,
            final IInterceptor interceptor,
            final Env env,
            final boolean force,
            final VncVector aliasOpt
    ) {
        final VncSet loadedModules = getLoadedModules(env);

        synchronized (loadedModules) {
            final Namespace currNS = Namespaces.getCurrentNamespace();

            try {
                final long nanos = System.nanoTime();
                final String moduleName = module.getValue();


                final boolean load = !loadedModules.contains(module) || force;

                if (load) {
                    // sandbox: validate module load
                    if (interceptor != null) {
                        interceptor.validateLoadModule(moduleName);
                    }

                    // load the module's code
                    final String code = ModuleLoader.loadModule(moduleName);

                    // evaluate the code
                    loadCode(code, moduleName, venice, env);

                    // remember the loaded module
                    loadedModules.add(module);
                }

                processNsAlias(
                    aliasOpt,
                    currNS,
                    String.format("load-module '%s'", moduleName));

                final long elapsed = System.nanoTime() - nanos;

                if (load && venice.getMeterRegistry().enabled) {
                    venice.getMeterRegistry().record("venice.module." + module + ".load", elapsed);
                }

                reportModuleLoad(moduleName, elapsed, load, force);

                return load;
            }
            catch(VncException ex) {
                throw ex;
            }
            catch(RuntimeException ex) {
                throw new VncException("Failed to load module '" + module + "'", ex);
            }
            finally {
                Namespaces.setCurrentNamespace(currNS);
           }
        }
    }

    public boolean loadVeniceFile(
            final VncString file,
            final IVeniceInterpreter venice,
            final IInterceptor interceptor,
            final Env env,
            final boolean force,
            final VncVector aliasOpt
    ) {
        final VncSet loadedFiles = getLoadedFiles(env);

        synchronized (loadedFiles) {
            final Namespace currNS = Namespaces.getCurrentNamespace();

            try {
                final String fileName = file.getValue();

                final boolean load = !loadedFiles.contains(file) || force;

                if (load) {
                    // load the file's code
                    final String code = ModuleLoader.loadExternalVeniceFile(fileName);

                    // evaluate the code
                    loadCode(code, fileName, venice, env);

                    // remember the loaded file
                    loadedFiles.add(file);
                }

                processNsAlias(
                    aliasOpt,
                    currNS,
                    String.format("load-file '%s'", fileName));

                return load;
            }
            catch(VncException ex) {
                throw ex;
            }
            catch(RuntimeException ex) {
                throw new VncException("Failed to load file '" + file.getValue() + "'", ex);
            }
            finally {
                Namespaces.setCurrentNamespace(currNS);
           }
        }
    }

    public boolean loadVeniceClasspathFile(
            final VncString file,
            final IVeniceInterpreter venice,
            final IInterceptor interceptor,
            final Env env,
            final boolean force,
            final VncVector aliasOpt
    ) {
        final VncSet loadedFiles = getLoadedFiles(env);

        synchronized (loadedFiles) {
            final Namespace currNS = Namespaces.getCurrentNamespace();

            try {
                final String fileName = file.getValue();

                final boolean load = !loadedFiles.contains(file) || force;

                if (load) {
                    // load the file's code
                    final String code = ModuleLoader.loadClasspathVeniceFile(fileName);
                    if (code == null ) {
                        throw new VncException("Failed to load Venice classpath file");
                    }

                    // evaluate the code
                    loadCode(code, fileName, venice, env);

                    // remember the loaded file
                    loadedFiles.add(file);
                }

                processNsAlias(
                    aliasOpt,
                    currNS,
                    String.format("load-classpath-file '%s'", fileName));

                return load;
            }
            catch(VncException ex) {
                throw ex;
            }
            catch(RuntimeException ex) {
                throw new VncException("Failed to load classpath file '" + file.getValue() + "'", ex);
            }
            finally {
                Namespaces.setCurrentNamespace(currNS);
           }
        }
    }

    public VncVal loadCode(
            final String code,
            final String name,
            final IVeniceInterpreter venice,
            final Env env
    ) {
        VncVal ast = venice.READ("(do " + code + ")", name);

        if (venice.isMacroExpandOnLoad()) {
            ast = venice.MACROEXPAND(ast, env);
        }

        ast = venice.EVAL(ast, env);

        return ast;
    }

    private void processNsAlias(
            final VncVector aliasOpt,
            final Namespace ns,
            final String contextInfo
    ) {
        if (aliasOpt != null) {
            validateNsAlias(contextInfo, aliasOpt);

            final VncSymbol aliasName = unquoteSymbol(aliasOpt.third());
            final VncSymbol nsName = unquoteSymbol(aliasOpt.first());
            ns.addAlias(aliasName.getName(), nsName.getName());
        }
    }


    private VncSet getLoadedModules(final Env env) {
        return Coerce.toVncSet(env.getGlobalOrNull(new VncSymbol("*loaded-modules*")));
    }

    private VncSet getLoadedFiles(final Env env) {
        return Coerce.toVncSet(env.getGlobalOrNull(new VncSymbol("*loaded-files*")));
    }

    private void validateNsAlias(
            final String caller,
            final VncVector aliasOpt
    ) {
        final boolean ok = aliasOpt.size() == 3
                            && (Types.isVncSymbol(aliasOpt.first()) || isQuotedSymbol(aliasOpt.first()))
                            && Types.isVncKeyword(aliasOpt.second())  && "as".equals(Coerce.toVncKeyword(aliasOpt.second()).getValue())
                            && (Types.isVncSymbol(aliasOpt.third()) || isQuotedSymbol(aliasOpt.third()));

        if (!ok) {
            throw new VncException(
                        String.format(
                                "Invalid ns alias definition '%s' for %s!",
                                Printer.pr_str(aliasOpt, true),
                                caller));
        }
    }

    private boolean isQuotedSymbol(final VncVal v) {
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

    private VncSymbol unquoteSymbol(final VncVal v) {
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

    private void reportModuleLoad(
            final String moduleName,
            final long elapsed,
            final boolean loaded,
            final boolean force
   ) {
//      System.out.println(String.format(
//          "Loaded module :%s in %dms, loaded, force=%s",
//          moduleName,
//          elapsed / 1_000_000,
//          force ? "true" : "false"));
    }
}
