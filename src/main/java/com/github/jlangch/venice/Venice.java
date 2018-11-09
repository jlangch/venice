/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
import java.util.Map;
import java.util.concurrent.Callable;

import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.ValueException;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.util.NullOutputStream;


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
		
		final VeniceInterpreter venice = new VeniceInterpreter();
		
		final Env env = createEnv(venice, null);
		
		// The default stdout PrintStream is not serializable, so remove it
		final Env root = env.getRootEnv();
		root.set(new VncSymbol("*out*"), Constants.Nil);

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

		// The stdout PrintStream is not serializable, so readd it as default stream
		final Env root = precompiled.getEnv().getRootEnv();
		root.set(new VncSymbol("*out*"), new VncJavaObject(new PrintStream(System.out, true)));
		
		return runWithSandbox( () -> {
			final VeniceInterpreter venice = new VeniceInterpreter();

			final Env env = addParams(new Env(precompiled.getEnv()), params);
				 
			final VncVal result = venice.EVAL((VncVal)precompiled.getPrecompiled(), env);
				
			return JavaInteropUtil.convertToJavaObject(result);
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

		return runWithSandbox( () -> {
			final VeniceInterpreter venice = new VeniceInterpreter();

			final Env env = createEnv(venice, params);
			
			final VncVal result = venice.RE(script, scriptName, env);
			
			return JavaInteropUtil.convertToJavaObject(result);
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
					if (val == null) {
						env.set(symbol, JavaInteropUtil.convertToVncVal(new PrintStream(new NullOutputStream())));
					}
					else if (val instanceof PrintStream) {
						env.set(symbol, JavaInteropUtil.convertToVncVal(val));
					}
					else if (val instanceof OutputStream) {
						env.set(symbol, JavaInteropUtil.convertToVncVal(new PrintStream((OutputStream)val, true)));
					}
					else {
						throw new VncException(
								"The *out* parameter value must be an instance of PrintStream or OutputStream");
					}
				}
				else {
					if (env.findEnv(symbol) != null) {
						throw new VncException(String.format(
								"A parameter with the name '%' already exists", 
								symbol.getName()));
					}
					env.set(symbol, JavaInteropUtil.convertToVncVal(val));
				}
			});
		}
		
		return env;
	}
	
	private Object runWithSandbox(final Callable<Object> callable) {
		try {
			JavaInterop.register(interceptor);
			
			return callable.call();
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
		finally {
			// clean up
			ThreadLocalMap.remove();
			JavaInterop.unregister();
		}
	}

	
	private final IInterceptor interceptor;
}
