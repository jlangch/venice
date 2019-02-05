/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.DynamicVar;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.SandboxedCallable;
import com.github.jlangch.venice.impl.ValueException;
import com.github.jlangch.venice.impl.Var;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
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
		this(null);
	}

	/**
	 * Create new sandboxed Venice instance
	 * 
	 * @param interceptor an interceptor that defines the sandbox 
	 */
	public Venice(final IInterceptor interceptor) {
		this.interceptor = interceptor == null ? new AcceptAllInterceptor() : interceptor;
	}
	
	
	/**
	 * Pre-compiles a Venice script.
	 * 
	 * @param scriptName A mandatory script name
	 * @param script A mandatory script
	 * @return the pre-compiled script
	 */
	public PreCompiled precompile(final String scriptName, final String script) {
		if (StringUtil.isBlank(scriptName)) {
			throw new IllegalArgumentException("A 'scriptName' must not be blank");
		}
		if (StringUtil.isBlank(script)) {
			throw new IllegalArgumentException("A 'script' must not be blank");
		}
		
		final VeniceInterpreter venice = new VeniceInterpreter(new MeterRegistry(false));
		
		final Env env = createEnv(venice, null);
		
		// The default stdout PrintStream is not serializable, so remove it
		final Env root = env.getRootEnv();
		root.setGlobal(new DynamicVar(new VncSymbol("*out*"), Constants.Nil));

		return new PreCompiled(scriptName, venice.READ(script, scriptName), env);
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

		// The stdout PrintStream is not serializable, so re-add it as default stream
		final Env root = precompiled.getEnv().getRootEnv();
		root.setGlobal(
				new DynamicVar(
						new VncSymbol("*out*"), 
						new VncJavaObject(new PrintStream(System.out, true))));
		
		return runWithSandbox( () -> {
			final VeniceInterpreter venice = new VeniceInterpreter(meterRegistry);

			final Env env = addParams(new Env(precompiled.getEnv()), params);
			
			meterRegistry.record("venice.setup", System.nanoTime() - nanos);
				 
			final VncVal result = venice.EVAL((VncVal)precompiled.getPrecompiled(), env);

			final Object jResult = JavaInteropUtil.convertToJavaObject(result);
			
			meterRegistry.record("venice.total", System.nanoTime() - nanos);

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
			final VeniceInterpreter venice = new VeniceInterpreter(meterRegistry);

			final Env env = createEnv(venice, params);
			
			meterRegistry.record("venice.setup", System.nanoTime() - nanos);
			
			final VncVal result = venice.RE(script, scriptName, env);
						
			final Object jResult = JavaInteropUtil.convertToJavaObject(result);
	
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

	public String getTimerDataFormatted() {
		return meterRegistry
					.getTimerData()
					.stream()
					.sorted((u,v) -> Long.valueOf(v.elapsedNanos).compareTo(u.elapsedNanos))
					.map(v -> String.format("%-20s [%3d]: %10s", v.name, v.count, Timer.formatNanos(v.elapsedNanos)))
					.collect(Collectors.joining("\n"));
	}
	
	
	private Env createEnv(final VeniceInterpreter venice, final Map<String,Object> params) {
		final Env env = venice.createEnv();
		
		addParams(env, params);
		
		return env;
	}
	
	private Env addParams(final Env env, final Map<String,Object> params) {
		if (params != null) {
			params.entrySet().forEach(entry -> {
				final String key = entry.getKey();
				final Object val = entry.getValue();

				final VncSymbol symbol = new VncSymbol(key);

				if (key.equals("*out*")) {
					env.setGlobal(new DynamicVar(symbol, JavaInteropUtil.convertToVncVal(buildStdOutPrintStream(val))));
				}
				else {
					env.setGlobal(new Var(symbol, JavaInteropUtil.convertToVncVal(val)));
				}
			});
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
			throw new VncException(
					"The *out* parameter value must be either null or an "
							+ "instance of PrintStream or OutputStream");
		}
	}
	
	private Object runWithSandbox(final Callable<Object> callable) {
		try {
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
			throw new JavaValueException(
						JavaInteropUtil.convertToJavaObject(ex.getValue()));
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
	
	
	private final static AtomicLong timeoutThreadPoolCounter = new AtomicLong(0);

	private final static ExecutorService executor = 
			Executors.newCachedThreadPool(
					ThreadPoolUtil.createThreadFactory(
							"venice-timeout-pool-%d", 
							timeoutThreadPoolCounter,
							true /* daemon threads */));
	
	private final IInterceptor interceptor;
	private final MeterRegistry meterRegistry = new MeterRegistry(false);
}
