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
package com.github.jlangch.venice;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.PreCompiled;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.ServiceRegistry;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.SymbolTable;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.ConcurrencyFunctions;
import com.github.jlangch.venice.impl.functions.ScheduleFunctions;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.namespaces.NamespaceRegistry;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.concurrent.Agent;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.shell.SmartShell;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.util.FunctionExecutionMeter;
import com.github.jlangch.venice.util.NullInputStream;
import com.github.jlangch.venice.util.NullOutputStream;


/**
 * Evaluator for Venice scripts
 */
public class Venice {

    /**
     * Create new Venice instance without a sandbox
     */
    public Venice() {
        this(null);
    }

    /**
     * Create new sandboxed Venice instance
     *
     * @param interceptor
     *          an optional interceptor that defines the sandbox
     */
    public Venice(final IInterceptor interceptor) {
        this.interceptor = interceptor == null ? new AcceptAllInterceptor() : interceptor;
        this.meterRegistry = new MeterRegistry(false);
    }

    /**
     * Pre-compiles a Venice script with disabled up-front macro expansion.
     *
     * <p>Note: for best performance up-front macro expansion should be enabled
     * for pre-compilation!
     *
     * @param scriptName A mandatory script name
     * @param script A mandatory script
     * @return the pre-compiled script
     */
    public IPreCompiled precompile(final String scriptName, final String script) {
        return precompile(scriptName, script, false);
    }

    /**
     * Pre-compiles a Venice script with optional up-front macro expansion
     *
     * @param scriptName A mandatory script name
     * @param script A mandatory script
     * @param macroexpand If true expand macros up-front (this can speed-up
     *                    execution significantly)
     * @return the pre-compiled script
     */
    public IPreCompiled precompile(
            final String scriptName,
            final String script,
            final boolean macroexpand
    ) {
        if (StringUtil.isBlank(scriptName)) {
            throw new IllegalArgumentException("A 'scriptName' must not be blank");
        }
        if (StringUtil.isBlank(script)) {
            throw new IllegalArgumentException("A 'script' must not be blank");
        }


        final String scriptEff = resolveScript(script);

        final long startTime = System.currentTimeMillis();

        final ThreadContext tc = ThreadContext.get();
        try {
            tc.clear(true);
            tc.setInterceptor_(interceptor);
            tc.setMeterRegistry_(meterRegistry);

            final IVeniceInterpreter venice = new VeniceInterpreter(interceptor);

            final Env env = venice.createEnv(macroexpand, false, RunMode.PRECOMPILE)
                                  .setStdoutPrintStream(null)
                                  .setStderrPrintStream(null)
                                  .setStdinReader(null);

            VncVal ast = venice.READ(scriptEff, scriptName);
            if (macroexpand) {
                ast = venice.MACROEXPAND(ast, env);
            }

            final NamespaceRegistry nsRegistry = venice.getNamespaceRegistry()
                                                       .remove(new VncSymbol("core"))
                                                       .copy();

            final SymbolTable symbols = env.getGlobalSymbolTableWithoutCoreSystemSymbols();

            return new PreCompiled(
                        scriptName,
                        scriptEff,
                        ast,
                        macroexpand,  // remember for runtime
                        nsRegistry,
                        symbols);
        }
        finally {
            lastPrecompileElapsedTimeMillis = System.currentTimeMillis() - startTime;
            tc.clear(false);
        }
    }

    /**
     * Evaluates a pre-compiled script without passing any parameters.
     *
     * @param precompiled A mandatory pre-compiled script
     * @return the result
     */
    public Object eval(final IPreCompiled precompiled) {
        if (precompiled == null) {
            throw new IllegalArgumentException("A 'precompiled' script must not be null");
        }

        return eval(precompiled, null);
    }

    /**
     * Evaluates a pre-compiled script with parameters.
     *
     * @param precompiledScript A mandatory pre-compiled script
     * @param params Optional parameters
     * @return the result
     */
    public Object eval(
            final IPreCompiled precompiledScript,
            final Map<String,Object> params
    ) {
        if (precompiledScript == null) {
            throw new IllegalArgumentException("A 'precompiled' script must not be null");
        }

        final PreCompiled precompiled = (PreCompiled)precompiledScript;

        final long nanos = System.nanoTime();

        final ThreadContext tc = ThreadContext.get();
        try {
            tc.clear(true);
            tc.setInterceptor_(interceptor);
            tc.setMeterRegistry_(meterRegistry);

            final IVeniceInterpreter venice = new VeniceInterpreter(
                                                    interceptor, meterRegistry, serviceRegistry);

            return runWithSandbox(venice, () -> {
                final NamespaceRegistry nsRegistryPrecompile = precompiled.getNamespaceRegistry();

                final SymbolTable coreSystemGlobalSymbols = getCoreSystemGlobalSymbols();

                Env env = Env.createPrecompiledEnv(coreSystemGlobalSymbols, precompiled);
                env = addParams(env, params);

                precompiled.getSymbols().put(new Var(
                                                new VncSymbol("*run-mode*"),
                                                RunMode.SCRIPT.mode,
                                                false,
                                                Var.Scope.Global));

                // re-init namespaces!
                venice.initNS();
                if (!nsRegistryPrecompile.isEmpty()) {
                    venice.presetNS(nsRegistryPrecompile);
                }
                venice.sealSystemNS();

                venice.setMacroExpandOnLoad(precompiled.isMacroexpand());

                if (meterRegistry.enabled) {
                    meterRegistry.record("venice.setup", System.nanoTime() - nanos);
                }

                final VncVal result = venice.EVAL(precompiled.getPrecompiled(), env);

                final Object jResult = result.convertToJavaObject();

                if (meterRegistry.enabled) {
                    meterRegistry.record("venice.total", System.nanoTime() - nanos);
                }

                return jResult;
            });
        }
        finally {
            tc.clear(false);
        }
    }

    /**
     * Evaluates a script with disabled up-front macro expansion
     *
     * @param script A mandatory script
     * @return The result
     */
    public Object eval(final String script) {
        return eval(null, script, false, null);
    }

    /**
     * Evaluates a script with disabled up-front macro expansion
     *
     * @param scriptName An optional scriptName
     * @param script A mandatory script
     * @return The result
     */
    public Object eval(final String scriptName, final String script) {
        return eval(scriptName, script, false, null);
    }

    /**
     * Evaluates a script with parameters and disabled up-front macro expansion
     *
     * @param script A mandatory script
     * @param params Optional parameters
     * @return The result
     */
    public Object eval(final String script, final Map<String,Object> params) {
        return eval(null, script, false, params);
    }

    /**
     * Evaluates a script with parameters and disabled up-front macro expansion
     *
     * @param scriptName An optional scriptName
     * @param script A mandatory script
     * @param params The optional parameters
     * @return The result
     */
    public Object eval(
            final String scriptName,
            final String script,
            final Map<String,Object> params
    ) {
        return eval(scriptName, script, false, params);
    }

    /**
     * Evaluates a script with parameters and optional up-front macro expansion
     *
     * @param scriptName An optional scriptName
     * @param script A mandatory script
     * @param macroexpand If true expand macros up-front (this can speed-up
     *                    execution significantly)
     * @param params The optional parameters
     * @return The result
     */
    public Object eval(
            final String scriptName,
            final String script,
            final boolean macroexpand,
            final Map<String,Object> params
    ) {
        if (StringUtil.isBlank(script)) {
            throw new IllegalArgumentException("A 'script' must not be blank");
        }

        final String scriptEff = resolveScript(script);

        final long nanos = System.nanoTime();

        final ThreadContext tc = ThreadContext.get();
        try {
            tc.clear(true);
            tc.setInterceptor_(interceptor);
            tc.setMeterRegistry_(meterRegistry);

            final IVeniceInterpreter venice = new VeniceInterpreter(
                                                    interceptor, meterRegistry, serviceRegistry);

            return runWithSandbox(venice, () -> {
                final Env env = createEnv(venice, macroexpand, params);

                meterRegistry.reset();  // no metrics for creating env and loading modules
                meterRegistry.record("venice.setup", System.nanoTime() - nanos);

                final VncVal result = venice.RE(scriptEff, scriptName, env);
                final Object jResult = result.convertToJavaObject();

                meterRegistry.record("venice.total", System.nanoTime() - nanos);

                return jResult;
            });
        }
        finally {
            tc.clear(false);
        }
    }

    /**
     * @return the function meter that manages collected runtime execution time
     *         for functions
     */
    public FunctionExecutionMeter getFunctionExecutionMeter() {
        return new FunctionExecutionMeter(meterRegistry);
    }

    /**
     * @return the elapsed time in milliseconds for the last pre-compilation, or -1
     *         if there wasn't a pre-compilation yet.
     */
    public long getLastPrecompileElapsedTimeMillis() {
        return lastPrecompileElapsedTimeMillis;
    }

    /**
     * @return the service registry
     */
    public IServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    /**
     * @return the Venice version
     */
    public static String getVersion() {
        return Version.VERSION;
    }

    /**
     * Shutdown all Venice executor services.
     *
     * <p>Be aware that executor services are shared across multiple Venice instances.
     * After shutdown, some Venice functions like agents may not work anymore.
     */
    public static void shutdownExecutorServices() {
        ConcurrencyFunctions.shutdown();
        ScheduleFunctions.shutdown();
        Agent.shutdown();
        SmartShell.shutdown();
    }

    private Env createEnv(
            final IVeniceInterpreter venice,
            final boolean macroexpand,
            final Map<String,Object> params
    ) {
        return addParams(venice.createEnv(macroexpand, false, RunMode.SCRIPT), params);
    }

    private Env addParams(final Env env, final Map<String,Object> params) {
        boolean stdoutAdded = false;
        boolean stderrAdded = false;
        boolean stdinAdded = false;

        if (params != null) {
            for(Map.Entry<String,Object> entry : params.entrySet()) {
                final String key = entry.getKey();
                final Object val = entry.getValue();

                if (key.charAt(0) == '*') {
                    if (key.equals("*out*")) {
                        env.setStdoutPrintStream(buildPrintStream(val, "*out*"));
                        stdoutAdded = true;
                    }
                    else if (key.equals("*err*")) {
                        env.setStderrPrintStream(buildPrintStream(val, "*err*"));
                        stderrAdded = true;
                    }
                    else if (key.equals("*in*")) {
                        env.setStdinReader(buildIOReader(val, "*in*"));
                        stdinAdded = true;
                    }
                    else {
                        env.setGlobal(
                            new Var(
                                new VncSymbol(key),
                                JavaInteropUtil.convertToVncVal(val),
                                Var.Scope.Global));
                    }
                }
                else {
                    env.setGlobal(
                        new Var(
                            new VncSymbol(key),
                            JavaInteropUtil.convertToVncVal(val),
                            Var.Scope.Global));
                }
            }
        }

        if (!stdoutAdded) {
            env.setStdoutPrintStream(stdout);
        }
        if (!stderrAdded) {
            env.setStderrPrintStream(stderr);
        }
        if (!stdinAdded) {
            env.setStdinReader(stdin);
        }

        return env;
    }

    private PrintStream buildPrintStream(final Object val, final String type) {
        if (val == null) {
            return new PrintStream(new NullOutputStream());
        }
        else if (val instanceof PrintStream) {
            return (PrintStream)val;
        }
        else if (val instanceof OutputStream) {
            return new PrintStream((OutputStream)val, true);
        }
        else {
            throw new VncException(String.format(
                        "The %s parameter value (%s) must be either null or an "
                            + "instance of PrintStream or OutputStream",
                        type,
                        val.getClass().getSimpleName()));
        }
    }

    private Reader buildIOReader(final Object val, final String type) {
        if (val == null) {
            return new InputStreamReader(new NullInputStream());
        }
        else if (val instanceof InputStream) {
            return new InputStreamReader((InputStream)val);
        }
        else if (val instanceof Reader) {
            return (Reader)val;
        }
        else {
            throw new VncException(String.format(
                        "The %s parameter value (%s) must be either null or an "
                            + "instance of Reader or InputStream",
                        type,
                        val.getClass().getSimpleName()));
        }
    }

    private Object runWithSandbox(
            final IVeniceInterpreter venice,
            final Callable<Object> callable
    ) {
        try {
            if (interceptor.getMaxFutureThreadPoolSize() != null) {
                ConcurrencyFunctions.setMaximumFutureThreadPoolSize(
                        interceptor.getMaxFutureThreadPoolSize());
            }

            if (interceptor.getMaxExecutionTimeSeconds() == null) {
                return callable.call();
            }
            else {
                final Callable<Object> wrapped = () -> {
                    final ThreadContext tc = ThreadContext.get();
                    try {
                        tc.clear(true);
                        tc.setInterceptor_(interceptor);
                        tc.setMeterRegistry_(meterRegistry);

                        return callable.call();
                    }
                    finally {
                        // clean up
                        tc.clear(false);
                    }
                };

                return runWithTimeout(
                        wrapped,
                        interceptor.getMaxExecutionTimeSeconds());
            }
        }
        catch(ValueException ex) {
            // convert the Venice value to a Java value
            final Object value = ex.getValue();
            throw new ValueException(
                    value instanceof VncVal ? ((VncVal)value).convertToJavaObject() : value);
        }
        catch(SecurityException ex) {
            throw ex;
        }
        catch(ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof VncException) {
                throw (VncException)cause;
            }
            else {
                throw new RuntimeException(ex.getMessage(), cause);
            }
        }
        catch(RuntimeException ex) {
            throw ex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private Object runWithTimeout(
            final Callable<Object> callable,
            final int timeoutSeconds
    ) throws Exception {
        final Future<Object> future = mngdExecutor
                                        .getExecutor()
                                        .submit(callable);

        try {
            return future.get(
                    interceptor.getMaxExecutionTimeSeconds(),
                    TimeUnit.SECONDS);
        }
        catch (TimeoutException ex) {
            future.cancel(true);
            throw new SecurityException(
                    "Venice Sandbox: The sandbox exceeded the max execution time. "
                        + "Requested cancellation!");
        }
    }

    private SymbolTable getCoreSystemGlobalSymbols() {
        SymbolTable symbols = coreSystemGlobalSymbols.get();
        if (symbols == null) {
            Env env = new VeniceInterpreter(interceptor, meterRegistry, serviceRegistry)
                            .createEnv(true, false, RunMode.SCRIPT)
                            .setStdoutPrintStream(null)
                            .setStderrPrintStream(null)
                            .setStdinReader(null);

            symbols = env.getGlobalSymbolTable();
            coreSystemGlobalSymbols.set(symbols);
        }

        return symbols;
    }

    private String resolveScript(final String script) {
        if (script.startsWith("classpath:")) {
            return new ClassPathResource(script.substring("classpath:".length()))
                          .getResourceAsString("UTF-8");
        }
        else {
            return script;
        }
    }

    private static ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-timeout-pool", 100);


    private long lastPrecompileElapsedTimeMillis = -1L;

    private final IInterceptor interceptor;
    private final MeterRegistry meterRegistry;
    private final IServiceRegistry serviceRegistry = new ServiceRegistry();
    private final AtomicReference<SymbolTable> coreSystemGlobalSymbols = new AtomicReference<>(null);

    // This is pretty expensive so just doit once at startup!
    private final PrintStream stdout = new PrintStream(System.out, true);
    private final PrintStream stderr = new PrintStream(System.err, true);
    private final Reader stdin = new InputStreamReader(System.in);
}
