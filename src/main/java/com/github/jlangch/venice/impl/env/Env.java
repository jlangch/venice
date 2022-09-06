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
package com.github.jlangch.venice.impl.env;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.SymbolNotFoundException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.PreCompiled;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.WithCallStack;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;


public class Env implements Serializable {

    public Env() {
        this((Env)null);
    }

    public Env(final Env outer) {
        if (outer == null) {
            this.outer = null;
            this.level = 0;
            this.safeGlobalSymbols = null;
            this.globalSymbols = new ConcurrentHashMap<>(2048);
            this.localSymbols = new ConcurrentHashMap<>(64);
        }
        else {
            this.outer = outer;
            this.level = outer.level() + 1;
            this.safeGlobalSymbols = outer.safeGlobalSymbols;
            this.globalSymbols = outer.globalSymbols;
            this.localSymbols = new ConcurrentHashMap<>(64);
        }
    }

    private Env(
            final SymbolTable coreSystemGlobalSymbols,
            final SymbolTable precompiledGlobalSymbols
     ) {
        this.outer = null;
        this.level = 0;
        this.safeGlobalSymbols = coreSystemGlobalSymbols.getSymbolMap();
        this.globalSymbols = new ConcurrentHashMap<>(precompiledGlobalSymbols.getSymbolMap());
        this.localSymbols = new ConcurrentHashMap<>(64);
    }

    public Env parent() {
        return outer;
    }

    /**
     * @return the 0-based environment level
     */
    public int level() {
        return level;
    }

    /**
     * Look up a local or global symbol's value
     *
     * <p>Unqualified symbol resolution:
     * <ol>
     *  <li>try to resolve the symbol from the local namespace</li>
     *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
     *  <li>try to resolve the symbol from the global 'core' namespace</li>
     * </ol>
     *
     * <p>Qualified symbol resolution:
     * <ol>
     *  <li>qualified symbols are resolved exclusively from the global symbols</li>
     * </ol>
     *
     * @param sym a symbol
     * @return the value
     * @throws SymbolNotFoundException if the symbol does not exist.
     */
    public VncVal get(final VncSymbol sym) {
        final VncVal val = getOrElse(sym, null);
        if (val != null) {
            return val;
        }
        else {
            try (WithCallStack cs = new WithCallStack(CallFrame.from(sym))) {
                final String symName = sym.getQualifiedName();
                if (symName.startsWith("\\")) {
                    throw new SymbolNotFoundException(
                                String.format(
                                        "Symbol '%s' not found. Did you mean the char literal '#%s'?",
                                        symName, symName),
                                symName);
                }

                if (sym.hasNamespace()) {
                   throw new SymbolNotFoundException(
                                 String.format("Symbol '%s' not found.", symName),
                                 symName);
                }

                final List<VncSymbol> candidates = EnvSymbolLookupUtil.getGlobalSymbolCandidates(
                                                        sym.getSimpleName(), this, 5);

                if (candidates.isEmpty()) {
                    throw new SymbolNotFoundException(
                            String.format("Symbol '%s' not found.", symName),
                            symName);
                }

                throw new SymbolNotFoundException(
                                EnvSymbolLookupUtil.getSymbolNotFoundMsg(sym, candidates),
                                symName);
            }
        }
    }

    /**
     * Checks if a symbol is global
     *
     * @param sym a symbol
     * @return returns true if a symbol is global else false
     */
    public boolean isGlobal(final VncSymbol sym) {
        final Var dv = getGlobalVar(sym);
        return dv != null && !(dv instanceof DynamicVar);
    }

    /**
     * Checks if a symbol is dynamic (thread local)
     *
     * @param sym a symbol
     * @return returns true if a symbol is dynamic else false
     */
    public boolean isDynamic(final VncSymbol sym) {
        final Var dv = getGlobalVar(sym);
        return dv != null && dv instanceof DynamicVar;
    }

    /**
     * Checks if a symbol is local
     *
     * @param sym a symbol
     * @return returns true if a symbol is local else false
     */
    public boolean isLocal(final VncSymbol sym) {
        return sym.hasNamespace() ? false : findLocalVar(sym) != null;
    }

    /**
     * Checks if a symbol is bound to a value
     *
     * @param sym a symbol
     * @return returns true if a symbol is bound to a value else false
     */
    public boolean isBound(final VncSymbol sym) {
        if (sym.hasNamespace()) {
            // if we got a namespace it must be a global var
            return getGlobalVar(sym) != null;
        }
        else {
            final Var v = findLocalVar(sym);
            return v != null ? true : getGlobalVar(sym) != null;
        }
    }

    /**
     * Look up a local or global symbol's value
     *
     * <p>Unqualified symbol resolution:
     * <ol>
     *  <li>try to resolve the symbol from the local namespace</li>
     *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
     *  <li>try to resolve the symbol from the global 'core' namespace</li>
     * </ol>
     *
     * <p>Qualified symbol resolution:
     * <ol>
     *  <li>qualified symbols are resolved exclusively from the global symbols</li>
     * </ol>
     *
     * @param sym a symbol
     * @return the value or <code>Nil</code> if not found
     */
    public VncVal getOrNil(final VncSymbol sym) {
        return getOrElse(sym, Nil);
    }

    /**
     * Look up a global symbol's value
     *
     * <p>Unqualified symbol resolution:
     * <ol>
     *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
     *  <li>try to resolve the symbol from the global 'core' namespace</li>
     * </ol>
     *
     * <p>Qualified symbol resolution:
     * <ol>
     *  <li>try to resolve the symbol from the global namespace</li>
     * </ol>
     *
     * @param sym a symbol
     * @return the value or <code>Nil</code> if not found
     */
    public VncVal getGlobalOrNil(final VncSymbol sym) {
        final Var v = getGlobalVar(sym);
        return v != null ? v.getVal() : Nil;
    }

    /**
     * Look up a global symbol's value
     *
     * <p>Unqualified symbol resolution:
     * <ol>
     *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
     *  <li>try to resolve the symbol from the global 'core' namespace</li>
     * </ol>
     *
     * <p>Qualified symbol resolution:
     * <ol>
     *  <li>try to resolve the symbol from the global namespace</li>
     * </ol>
     *
     * @param sym a symbol
     * @return the value or <code>null</code> if not found
     */
    public VncVal getGlobalOrNull(final VncSymbol sym) {
        final Var v = getGlobalVar(sym);
        return v != null ? v.getVal() : null;
    }

    /**
     * Look up a global symbol's var
     *
     * <p>Unqualified symbol resolution:
     * <ol>
     *  <li>try to resolve the symbol from the global current namespace defined by *ns*</li>
     *  <li>try to resolve the symbol from the global 'core' namespace</li>
     * </ol>
     *
     * <p>Qualified symbol resolution:
     * <ol>
     *  <li>try to resolve the symbol from the global namespace</li>
     * </ol>
     *
     * @param sym a symbol
     * @return the value or <code>null</code> if not found
     */
    public Var getGlobalVarOrNull(final VncSymbol sym) {
        return getGlobalVar(sym);
    }

    public Env setLocal(final Var localVar) {
        final VncSymbol sym = localVar.getName();

        if (sym.isReservedName()) {
            try (WithCallStack cs = new WithCallStack(CallFrame.from(sym))) {
                throw new VncException(String.format(
                            "Rejected setting local var with rerved name '%s'. Use another name, please.",
                            sym.getName()));
            }
        }
        if (sym.isSpecialFormName()) {
            try (WithCallStack cs = new WithCallStack(CallFrame.from(sym))) {
                throw new VncException(String.format(
                            "Rejected setting local var with special form name '%s'. Use another name, please.",
                            sym.getName()));
            }
        }

        if (!allowShadowingGlobalVars) {
            final Var globVar = getGlobalVar(sym);

            // check shadowing of a global non function var by a local var
            //
            // e.g.:   (do (defonce x 1) (let [x 10 y 20] (+ x y)))
            //         (let [+ 10] (core/+ + 20))
            if (globVar != null && !globVar.isOverwritable() && Types.isVncFunction(globVar.getVal())) {
                try (WithCallStack cs = new WithCallStack(CallFrame.from(sym))) {
                    throw new VncException(String.format(
                                "The global var '%s' must not be shadowed by a local var!",
                                sym.getQualifiedName()));
                }
            }
        }

        localSymbols.put(sym, localVar);

        return this;
    }

    public Env setGlobal(final Var val) {
        final VncSymbol sym = val.getName();

        if (sym.isSpecialFormName() && !(val.getVal() instanceof VncSpecialForm)) {
            try (WithCallStack cs = new WithCallStack(CallFrame.from(sym))) {
                throw new VncException(String.format(
                            "Rejected setting var %s with name of a special form",
                            sym.getName()));
            }
        }

        final Var v = getGlobalVar(sym);
        if (v != null && !v.isOverwritable()) {
            try (WithCallStack cs = new WithCallStack(CallFrame.from(sym))) {
                throw new VncException(String.format(
                            "The existing global var '%s' must not be overwritten!",
                            sym.getQualifiedName()));
            }
        }

        setGlobalVar(sym, val);

        return this;
    }

    public Env addGlobalVars(final List<Var> vars) {
        vars.forEach(v -> setGlobal(v));
        return this;
    }

    public Env addLocalVars(final List<Var> vars) {
        for(Var b : vars) setLocal(b);
        return this;
    }

    /**
     * Get the local vars from an arbitrary level.
     *
     * @param levelsUp the number of levels the to move up through the outer
     *                 levels starting from the current level. Stops on the last
     *                 available level.
     *                 Must be greater or equal to 0. Negative values are
     *                 treated as 0.
     * @return the local vars at the referenced level.
     */
    public List<Var> getLocalVars(final int levelsUp) {
        Env env = this;
        for(int ii=0; ii<levelsUp; ii++) {
            env = env == null ? null : env.outer;
        }

        return env == null
                ? new ArrayList<>()
                : env.localSymbols
                     .values()
                     .stream()
                     .filter(v -> !(v instanceof GlobalRefVar))
                     .collect(Collectors.toList());
    }

    public void pushGlobalDynamic(final VncSymbol sym, final VncVal val) {
        final DynamicVar dv = findGlobalDynamicVar(sym);
        if (dv != null) {
            dv.pushVal(val);
        }
        else {
            final DynamicVar nv = new DynamicVar(sym, Nil);
            setGlobalVar(sym, nv);
            nv.pushVal(val);
        }
    }

    public VncVal popGlobalDynamic(final VncSymbol sym) {
        final DynamicVar dv = findGlobalDynamicVar(sym);
        return dv != null ? dv.popVal() : Nil;
    }

    public VncVal peekGlobalDynamic(final VncSymbol sym) {
        final DynamicVar dv = findGlobalDynamicVar(sym);
        return dv != null ? dv.peekVal() : Nil;
    }

    public void setGlobalDynamic(final VncSymbol sym, final VncVal val) {
        final Var gv = getGlobalVar(sym);
        if (gv == null) {
            final DynamicVar nv = new DynamicVar(sym, Nil);
            setGlobalVar(sym, nv);
            nv.pushVal(val);
        }
        else if (gv instanceof DynamicVar) {
            final DynamicVar nv = ((DynamicVar)gv);
            nv.pushVal(val);
        }
        else {
            final DynamicVar nv = new DynamicVar(sym, gv.getVal());
            setGlobalVar(sym, nv);
            nv.pushVal(val);
        }
    }

    public void replaceGlobalDynamic(final VncSymbol sym, final VncVal val) {
        final DynamicVar nv = new DynamicVar(sym, Nil);
        setGlobalVar(sym, nv);
        nv.pushVal(val);
    }

    public void removeGlobalSymbol(final VncSymbol sym) {
        // Do not care about precompiledGlobalSymbols.
        // Only system namespaces like core, time, ... are part of the pre-compiled
        // global symbols, and these namespaces are sealed anyway!
        //
        // The calling VeniceInterpreter is preventing the removal of global
        // system namespace symbols!

        globalSymbols.remove(sym);
    }

    public SymbolTable getGlobalSymbolTable() {
        return new SymbolTable(globalSymbols);
    }

    public SymbolTable getSafeGlobalSymbolTable() {
        return new SymbolTable(safeGlobalSymbols);
    }

    public static Env createPrecompiledEnv(
            final SymbolTable coreSystemGlobalSymbols,
            final PreCompiled preCompiled
    ) {
        // Used for precompiled scripts.
        // Move the global symbols to core global symbols so they remain untouched
        // while running the precompiled script and thus can be reused by subsequent
        // precompiled script invocations
        return new Env(coreSystemGlobalSymbols, (SymbolTable)preCompiled.getSymbols());
    }

    public SymbolTable getGlobalSymbolTableWithoutCoreSystemSymbols() {
        // remove all native global functions
        Map<VncSymbol,Var> symbols = globalSymbols
                                         .entrySet()
                                         .stream()
                                         .filter(e ->  {
                                             final VncSymbol sym = e.getKey();
                                             final String symNS = sym.getNamespace();
                                             final VncVal value = e.getValue().getVal();
                                             if (symNS == null || "core".equals(symNS) || "math".equals(symNS)) {
                                                 return false;
                                             }
                                             else if (value instanceof VncFunction) {
                                                 return !((VncFunction)value).isNative();
                                             }
                                             else if (value instanceof VncSpecialForm) {
                                                 return false;
                                             }
                                              return true;
                                         })
                                         .collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));

        // remove system global vars
        symbols.remove(new VncSymbol("*version*"));
        symbols.remove(new VncSymbol("*ns*"));
        symbols.remove(new VncSymbol("*newline*"));
        symbols.remove(new VncSymbol("*ansi-term*"));
        symbols.remove(new VncSymbol("*run-mode*"));
        symbols.remove(new VncSymbol("*ARGV*"));

        // keep, do not remove
        // symbols.remove(new VncSymbol("*loaded-modules*"));
        // symbols.remove(new VncSymbol("*loaded-files*"));

        return new SymbolTable(symbols);
    }

    public void removeGlobalSymbolsByNS(final VncSymbol ns) {
        // Do not care about precompiledGlobalSymbols.
        // Only system namespaces like core, time, ... are part of the pre-compiled
        // global symbols, and these namespaces are sealed anyway!
        //
        // The calling VeniceInterpreter is preventing the removal of global
        // system namespaces!

        final String nsName = ns.getName();

        globalSymbols
            .keySet()
            .stream()
            .filter(s -> nsName.equals(s.getNamespace()))
            .forEach(s -> globalSymbols.remove(s));
    }

    public Env setStdoutPrintStream(final PrintStream ps) {
        replaceGlobalDynamic(
                new VncSymbol("*out*"),
                VncJavaObject.from(
                        ps != null ? ps : IOStreamUtil.nullPrintStream(),
                        PrintStream.class));

        return this;
    }

    public Env setStderrPrintStream(final PrintStream ps) {
        replaceGlobalDynamic(
                new VncSymbol("*err*"),
                VncJavaObject.from(
                        ps != null ? ps : IOStreamUtil.nullPrintStream(),
                        PrintStream.class));

        return this;
    }

    public Env setStdinReader(final Reader rd) {
        if (rd == null) {
            replaceGlobalDynamic(
                    new VncSymbol("*in*"),
                    VncJavaObject.from(IOStreamUtil.nullBufferedReader(), Reader.class));
        }
        else if (rd instanceof BufferedReader) {
            replaceGlobalDynamic(
                    new VncSymbol("*in*"),
                    VncJavaObject.from(rd, Reader.class));
        }
        else {
            replaceGlobalDynamic(
                    new VncSymbol("*in*"),
                    VncJavaObject.from(new BufferedReader(rd), Reader.class));
        }

        return this;
    }

    private DynamicVar findGlobalDynamicVar(final VncSymbol sym) {
        final Var dv = getGlobalVar(sym);
        if (dv != null) {
            if (dv instanceof DynamicVar) {
                return (DynamicVar)dv;
            }
        }
        return null;
    }

    private VncVal getOrElse(final VncSymbol sym, final VncVal defaultVal) {
        if (sym.hasNamespace()) {
            // if we got a namespace it must be a global var
            final Var glob = getGlobalVar(sym);
            return glob == null ? defaultVal : glob.getVal();
        }
        else {
            final Var local = findLocalVar(sym);

            if (globalVarLookupOptimization) {
                if (local != null) {
                    if (local instanceof GlobalRefVar) {
                        final Var glob = getGlobalVar(sym);
                        return glob == null ? defaultVal : glob.getVal();
                    }
                    else {
                        return local.getVal();
                    }
                }
                else {
                    final Var glob = getGlobalVar(sym);
                    if (glob != null) {
                        localSymbols.put(sym, new GlobalRefVar(sym));
                        return glob.getVal();
                    }
                    else {
                        return defaultVal;
                    }
                }
            }
            else {
                if (local != null) {
                    return local.getVal();
                }
                else {
                    final Var glob = getGlobalVar(sym);
                    return glob == null ? defaultVal : glob.getVal();
                }
            }
        }
    }

    public Var findLocalVar(final VncSymbol sym) {
        Var v = localSymbols.get(sym);
        if (v != null) return v;

        // descend through the env levels
        Env env = outer;
        while(env != null) {
            v = env.localSymbols.get(sym);
            if (v != null) return v;
            env = env.outer;
        }

        return null;
    }

    private Var getGlobalVar(final VncSymbol sym) {
        Var v = null;

        final String symNsName = sym.getNamespace();
        final String symSimpleName = sym.getSimpleName();

        if (symNsName != null) {
            // qualified symbol
            if ("core".equals(symNsName)) {
                v = getGlobalVarRaw(new VncSymbol(symSimpleName));
            }
            else {
                final String realsNsName = Namespaces.getCurrentNamespace()
                                                     .lookupByAlias(symNsName);
                if (realsNsName != null) {
                    v = getGlobalVarRaw(
                            "core".equals(realsNsName)
                                ? new VncSymbol(symSimpleName)
                                : new VncSymbol(realsNsName, symSimpleName, Nil));
                }
                else {
                    v = getGlobalVarRaw(sym);
                }
            }
        }
        else {
            // unqualified symbol, handle special case for core and
            // special form symbols
            final VncSymbol currNS = Namespaces.getCurrentNS();

            if (!Namespaces.isCoreNS(currNS) && !sym.isSpecialFormName()) {
                // 1st: lookup for current namespace
                final VncSymbol s = new VncSymbol(
                                            currNS.getName(),
                                            symSimpleName,
                                            Constants.Nil);
                v = getGlobalVarRaw(s);
            }

            if (v == null) {
                // 2nd: lookup without namespace for core symbol or special form
                v = getGlobalVarRaw(sym);
            }
        }

        if (v == null) {
            return null;
        }
        else {
            if (failOnPrivateSymbolAccess) {
                rejectPrivateSymbolAccess(sym, v);
            }
            return v;
        }
    }

    private Var getGlobalVarRaw(final VncSymbol sym) {
        if (safeGlobalSymbols != null) {
            final Var v = safeGlobalSymbols.get(sym);
            if (v != null) return v;
        }

        return globalSymbols.get(sym);
    }

    private void setGlobalVar(final VncSymbol sym, final Var value) {
        globalSymbols.put(sym, value);
    }

    public Map<VncSymbol,Var> getAllGlobalSymbols() {
        final Map<VncSymbol,Var> all = new HashMap<>();

        if (safeGlobalSymbols != null) {
            all.putAll(safeGlobalSymbols);
        }

        all.putAll(globalSymbols);

        return all;
    }

    public List<VncSymbol> getAllGlobalFunctionSymbols() {
        return getAllGlobalSymbols()
                .entrySet()
                .stream()
                .filter(e -> e.getValue().getVal() instanceof VncFunction)
                .map(e -> {
                        final VncFunction fn = (VncFunction)e.getValue().getVal();
                        return e.getKey()
                                .withMeta(VncHashMap.of(
                                    new VncKeyword("group"), new VncString(fn.getNamespace()),
                                    new VncKeyword("arglists"), fn.getArgLists(),
                                    new VncKeyword("doc"), fn.getDoc()));
                     })
                .collect(Collectors.toList());
    }


    private void rejectPrivateSymbolAccess(final VncSymbol sym, final Var globalVar) {
        final VncSymbol globalVarSym = globalVar.getName();
        if (globalVarSym.isPrivate()) {
            // note: global symbols without namespace belong to the "core" namespace
            final String currNS = Namespaces.getCurrentNS().getName();
            final String symNS = globalVarSym.hasNamespace() ? globalVarSym.getNamespace() : "core";
            if (!currNS.equals(symNS)) {
                final CallStack callStack = ThreadContext.getCallStack();

                try (WithCallStack cs = new WithCallStack(new CallFrame("symbol", sym.getMeta()))) {
                    throw new VncException(String.format(
                            "Illegal access of private symbol '%s/%s' "
                                + "accessed from namespace '%s'.\n%s",
                            symNS,
                            globalVarSym.getSimpleName(),
                            currNS,
                            callStack.toString()));
                }
            }
        }
    }



    private static final long serialVersionUID = 9002640180394221858L;

    // Note: Clojure allows shadowing global vars by local vars
    private final boolean allowShadowingGlobalVars = true;

    private final boolean failOnPrivateSymbolAccess = true;

    private final boolean globalVarLookupOptimization = true;

    private final Env outer;
    private final int level;
    private final Map<VncSymbol,Var> safeGlobalSymbols;
    private final Map<VncSymbol,Var> globalSymbols;
    private final Map<VncSymbol,Var> localSymbols;
}
