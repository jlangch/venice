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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.LoadPath;
import com.github.jlangch.venice.impl.SandboxedCallable;
import com.github.jlangch.venice.impl.ValueException;
import com.github.jlangch.venice.impl.Var;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.functions.ConcurrencyFunctions;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.ThreadPoolUtil;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.util.NullOutputStream;
import com.github.jlangch.venice.util.Timer;


public class Venice {

	public Venice() {
		this(null, null);
	}

	/**
	 * Create new sandboxed Venice instance
	 * 
	 * @param interceptor 
	 * 			an optional interceptor that defines the sandbox 
	 */
	public Venice(final IInterceptor interceptor) {
		this(interceptor, null);
	}

	/**
	 * Create new sandboxed Venice instance
	 * 
	 * @param interceptor
	 * 			 an optional interceptor that defines the sandbox 
	 * @param loadPaths 
	 * 			an optional list of file load paths used by the function 
	 * 			'load-file'
	 */
	public Venice(final IInterceptor interceptor, final List<String> loadPaths) {
		this.interceptor = interceptor == null ? new AcceptAllInterceptor() : interceptor;
		this.loadPaths = LoadPath.sanitize(loadPaths);
	}
	
	/**
	 * Pre-compiles a Venice script.
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
	 * @param expandMacros if true expand all macros
	 * @return the pre-compiled script
	 */
	public PreCompiled precompile(final String scriptName, final String script, final boolean expandMacros) {
		if (StringUtil.isBlank(scriptName)) {
			throw new IllegalArgumentException("A 'scriptName' must not be blank");
		}
		if (StringUtil.isBlank(script)) {
			throw new IllegalArgumentException("A 'script' must not be blank");
		}

		final long nanos = System.nanoTime();

		final VeniceInterpreter venice = new VeniceInterpreter(
												new MeterRegistry(false), 
												new AcceptAllInterceptor(), 
												loadPaths);
		
		final Env env = venice.createEnv(false, new VncKeyword("macroexpand"))
							  .setStdoutPrintStream(null);

		VncVal ast = venice.READ(script, scriptName);	
		if (expandMacros) {
			final VncFunction macroexpand_all = (VncFunction)env.getGlobalOrNull(new VncSymbol("core/macroexpand-all"));
			if (macroexpand_all != null) {
				ast = macroexpand_all.apply(VncList.of(ast));
			}
		}
		
		final PreCompiled pc = new PreCompiled(scriptName, ast);

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

		meterRegistry.disable(); // enable when really needed

		final long nanos = System.nanoTime();

		return runWithSandbox( () -> {
			ThreadLocalMap.clear();

			final VeniceInterpreter venice = new VeniceInterpreter(meterRegistry, interceptor, loadPaths);

			final Env env = addParams(getPrecompiledEnv(), params);

			// re-init namespaces!
			venice.initNS();
			
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
		return eval(null, script, null);
	}

	/**
	 * Evaluates a script with parameters
	 * 
	 * @param scriptName An optional scriptName
	 * @param script A mandatory script
	 * @return The result
	 */
	public Object eval(final String scriptName, final String script) {
		return eval(scriptName, script, null);
	}

	/**
	 * Evaluates a script with parameters
	 * 
	 * @param script A mandatory script
	 * @param params Optional parameters
	 * @return The result
	 */
	public Object eval(final String script, final Map<String,Object> params) {
		return eval(null, script, params);
	}

	/**
	 * Evaluates a script with parameters
	 * 
	 * @param scriptName An optional scriptName
	 * @param script A mandatory script
	 * @param params The optional parameters
	 * @return The result
	 */
	public Object eval(final String scriptName, final String script, final Map<String,Object> params) {
		if (StringUtil.isBlank(script)) {
			throw new IllegalArgumentException("A 'script' must not be blank");
		}


		final long nanos = System.nanoTime();

		return runWithSandbox( () -> {
			ThreadLocalMap.clear();

			final VeniceInterpreter venice = new VeniceInterpreter(meterRegistry, interceptor, loadPaths);

			final Env env = createEnv(venice, params);
			
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
	
	public void enableJavaInteropReflectionCache(final boolean enable) {
		ReflectionAccessor.enableCache(enable);
	}

	public boolean isJavaInteropReflectionCacheEnabled() {
		return ReflectionAccessor.isCacheEnabled();
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
		return meterRegistry.getTimerDataFormatted(title);
	}
	
	
	private Env createEnv(final VeniceInterpreter venice, final Map<String,Object> params) {
		return addParams(venice.createEnv(false, new VncKeyword("script")), params);
	}
	
	private Env addParams(final Env env, final Map<String,Object> params) {
		boolean stdoutAdded = false;
		
		if (params != null) {
			for(Map.Entry<String,Object> entry : params.entrySet()) {
				final String key = entry.getKey();
				final Object val = entry.getValue();

				if (key.equals("*out*")) {
					env.setStdoutPrintStream(buildStdOutPrintStream(val));
					
					stdoutAdded = true;
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
		
		return env;
	}
	
	private PrintStream buildStdOutPrintStream(final Object val) {
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
						"The *out* parameter value (%s) must be either null or an "
							+ "instance of PrintStream or OutputStream",
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
			env = new VeniceInterpreter()
						.createEnv(true, new VncKeyword("script"))
						.setStdoutPrintStream(null);
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
	private final List<String> loadPaths;
	private final MeterRegistry meterRegistry = new MeterRegistry(false);
	private final AtomicReference<Env> precompiledEnv = new AtomicReference<>(null);
	private final PrintStream stdout = new PrintStream(System.out, true);
}
