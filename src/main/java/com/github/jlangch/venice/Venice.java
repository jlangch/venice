/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.SandboxedCallable;
import com.github.jlangch.venice.impl.ValueException;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.ConcurrencyFunctions;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.ThreadPoolUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.util.NullInputStream;
import com.github.jlangch.venice.util.NullOutputStream;
import com.github.jlangch.venice.util.Timer;


public class Venice {

	public Venice() {
		this(null);
	}

	/**
	 * Create new sandboxed Venice instance
	 * 
	 * @param interceptor 
	 * 			an optional interceptor that defines the sandbox 
	 */
	public Venice(final IInterceptor interceptor) {
		this.interceptor = interceptor == null ? new AcceptAllInterceptor() : interceptor;
		this.meterRegistry = this.interceptor.getMeterRegistry();
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
	public PreCompiled precompile(final String scriptName, final String script) {
		return precompile(scriptName, script, false);
	}
	
	/**
	 * Pre-compiles a Venice script.
	 * 
	 * @param scriptName A mandatory script name
	 * @param script A mandatory script
	 * @param macroexpand If true expand macros up-front
	 * @return the pre-compiled script
	 */
	public PreCompiled precompile(
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

		final long nanos = System.nanoTime();

		// Note: For security reasons use the RejectAllInterceptor because
		//       macros can execute code while being expanded. Thus we need
		//       to have a safe sandbox in-place if macros are misused to
		//       execute code at expansion time.
		final VeniceInterpreter venice = new VeniceInterpreter(
												new RejectAllInterceptor());
		
		final Env env = venice.createEnv(macroexpand, false, RunMode.PRECOMPILE)
							  .setStdoutPrintStream(null)
							  .setStderrPrintStream(null)
							  .setStdinReader(null);

		VncVal ast = venice.READ(script, scriptName);
		if (macroexpand) {
			ast = venice.MACROEXPAND(ast, env);
		}
		
		final PreCompiled pc = new PreCompiled(scriptName, ast, macroexpand);

		meterRegistry.record("venice.precompile", System.nanoTime() - nanos);
		
		return pc;
	}

	/**
	 * Evaluates a pre-compiled script without passing any parameters.
	 * 
	 * @param precompiled A mandatory pre-compiled script
	 * @return the result
	 */
	public Object eval(final PreCompiled precompiled) {		
		if (precompiled == null) {
			throw new IllegalArgumentException("A 'precompiled' script must not be null");
		}

		return eval(precompiled, null);
	}
	
	/**
	 * Evaluates a pre-compiled script with parameters.
	 * 
	 * @param precompiled A mandatory pre-compiled script
	 * @param params Optional parameters
	 * @return the result
	 */
	public Object eval(
			final PreCompiled precompiled, 
			final Map<String,Object> params
	) {
		if (precompiled == null) {
			throw new IllegalArgumentException("A 'precompiled' script must not be null");
		}

		final long nanos = System.nanoTime();

		return runWithSandbox( () -> {
			ThreadLocalMap.clear();

			final VeniceInterpreter venice = new VeniceInterpreter(interceptor);

			final Env env = addParams(getPrecompiledEnv(), params);

			// re-init namespaces!
			venice.initNS();
			venice.sealSystemNS();
			
			if (meterRegistry.enabled) {
				meterRegistry.record("venice.setup", System.nanoTime() - nanos);
			}

			final VncVal result = venice.EVAL((VncVal)precompiled.getPrecompiled(), env);

			final Object jResult = result.convertToJavaObject();
			
			if (meterRegistry.enabled) {
				meterRegistry.record("venice.total", System.nanoTime() - nanos);
			}

			return jResult;
		});
	}

	/**
	 * Evaluates a script.
	 * 
	 * @param script A mandatory script
	 * @return The result
	 */
	public Object eval(final String script) {
		return eval(null, script, false, null);
	}

	/**
	 * Evaluates a script with parameters
	 * 
	 * @param scriptName An optional scriptName
	 * @param script A mandatory script
	 * @return The result
	 */
	public Object eval(final String scriptName, final String script) {
		return eval(scriptName, script, false, null);
	}

	/**
	 * Evaluates a script with parameters
	 * 
	 * @param script A mandatory script
	 * @param params Optional parameters
	 * @return The result
	 */
	public Object eval(final String script, final Map<String,Object> params) {
		return eval(null, script, false, params);
	}
	
	/**
	 * Evaluates a script with parameters
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
	 * Evaluates a script with parameters
	 * 
	 * @param scriptName An optional scriptName
	 * @param script A mandatory script
	 * @param macroexpand If true expand macros upfront
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


		final long nanos = System.nanoTime();

		return runWithSandbox( () -> {
			ThreadLocalMap.clear();

			final VeniceInterpreter venice = new VeniceInterpreter(interceptor);

			final Env env = createEnv(venice, macroexpand, params);
			
			meterRegistry.reset();  // no metrics for creating env and loading modules 

			meterRegistry.record("venice.setup", System.nanoTime() - nanos);
			
			final VncVal result = venice.RE(script, scriptName, env);
						
			final Object jResult = result.convertToJavaObject();
	
			meterRegistry.record("venice.total", System.nanoTime() - nanos);

			return jResult;
		});
	}
	
	/**
	 * @return the Venice version
	 */
	public static String getVersion() {
		return Version.VERSION;
	}
	
	public void enableTimer() {
		meterRegistry.enable();
	}
	
	public void disableTimer() {
		meterRegistry.disable();
	}
	
	public void resetTimer() {
		meterRegistry.reset();
	}

	public Collection<Timer> getTimerData() {
		return meterRegistry.getTimerData();
	}

	public String getTimerDataFormatted(final String title) {
		return meterRegistry.getTimerDataFormatted(title, false);
	}
	
	
	private Env createEnv(
			final VeniceInterpreter venice, 
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
							JavaInteropUtil.convertToVncVal(val)));
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
	
	private Object runWithSandbox(final Callable<Object> callable) {
		try {
			if (interceptor.getMaxFutureThreadPoolSize() != null) {
				ConcurrencyFunctions.setMaximumThreadPoolSize(interceptor.getMaxFutureThreadPoolSize());
			}

			if (interceptor.getMaxExecutionTimeSeconds() == null) {
				return new SandboxedCallable<Object>(interceptor, callable).call();
			}
			else {
				return runWithTimeout(
						new SandboxedCallable<Object>(interceptor, callable), 
						interceptor.getMaxExecutionTimeSeconds());
			}
		}
		catch(ValueException ex) {
			// convert the Venice value to a Java value
			throw new JavaValueException(ex.getValue().convertToJavaObject());
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
		try {
			final Future<Object> future = executor.submit(callable);
		    return future.get(interceptor.getMaxExecutionTimeSeconds(), TimeUnit.SECONDS);
		} 
		catch (TimeoutException ex) {
			throw new SecurityException(
					"Venice Sandbox: The sandbox exceeded the max execution time");
		}
	}
	
	private Env getPrecompiledEnv() {
		Env env = precompiledEnv.get();
		if (env == null) {
			env = new VeniceInterpreter(interceptor)
						.createEnv(true, false, RunMode.SCRIPT)
						.setStdoutPrintStream(null)
						.setStderrPrintStream(null)
						.setStdinReader(null);
			precompiledEnv.set(env);
		}
		
		// make the env safe for reuse
		return env.copyGlobalToPrecompiledSymbols();
	}
	
	
	
	private final static AtomicLong timeoutThreadPoolCounter = new AtomicLong(0);

	private final static ExecutorService executor = 
			Executors.newCachedThreadPool(
					ThreadPoolUtil.createThreadFactory(
							"venice-timeout-pool-%d", 
							timeoutThreadPoolCounter,
							true /* daemon threads */));
	
	private final IInterceptor interceptor;
	private final MeterRegistry meterRegistry;
	private final AtomicReference<Env> precompiledEnv = new AtomicReference<>(null);
	private final PrintStream stdout = new PrintStream(System.out, true);
	private final PrintStream stderr = new PrintStream(System.err, true);
	private final Reader stdin = new InputStreamReader(System.in);
}
