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
package org.venice;

import java.util.Map;

import org.venice.impl.Env;
import org.venice.impl.VeniceInterpreter;
import org.venice.impl.javainterop.JavaInterop;
import org.venice.impl.javainterop.JavaInteropUtil;
import org.venice.impl.types.Types;
import org.venice.impl.types.VncString;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.VncVal;
import org.venice.impl.util.reflect.ReflectionAccessor;
import org.venice.javainterop.JavaInterceptor;


public class Venice {

	public Venice() {
		this.interceptor = null;
	}

	public Venice(final JavaInterceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	
	public PreCompiled precompile(final String script) {
		final VeniceInterpreter venice = new VeniceInterpreter();

		return new PreCompiled(venice.READ(script), createEnv(venice, null));
	}

	public Object eval(final PreCompiled precompiled) {		
		return eval(precompiled, null);
	}
	
	public Object eval(
			final PreCompiled precompiled, 
			final Map<String,Object> params
	) {
		try {
			JavaInterop.register(interceptor);
			
			final VeniceInterpreter venice = new VeniceInterpreter();

			final Env env = addParams(new Env(precompiled.getEnv()), params);
				 
			final VncVal result = venice.EVAL((VncVal)precompiled.getPrecompiled(), env);
				
			return JavaInteropUtil.convertToJavaObject(result);
		}
		catch(ValueException ex) {
			final VncVal val = ex.getValue();
			throw new VncException(
						Types.isVncString(val) 
							? ((VncString)val).getValue() 
							: val.toString());
		}
		catch(RuntimeException ex) {
			throw ex;
		}
		finally {
			JavaInterop.unregister();
		}
	}

	public Object eval(final String script) {
		return eval(script, null);
	}

	public Object eval(final String script, final Map<String,Object> params) {
		try {
			JavaInterop.register(interceptor);
			
			final VeniceInterpreter venice = new VeniceInterpreter();

			final Env env = createEnv(venice, params);
			
			final VncVal result = venice.RE(script, env);
			
			return JavaInteropUtil.convertToJavaObject(result);
		}
		catch(ValueException ex) {
			final VncVal val = ex.getValue();
			throw new VncException(
						Types.isVncString(val) 
							? ((VncString)val).getValue() 
							: val.toString());
		}
		catch(RuntimeException ex) {
			throw ex;
		}
		finally {
			JavaInterop.unregister();
		}
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
				
				final VncVal malVal = JavaInteropUtil.convertToVncVal(val); 	
				if (malVal != null) {
					final VncSymbol symbol = new VncSymbol(key);
					if (env.find(symbol) != null) {
						throw new VncException(String.format(
								"A parameter with the name '%' already exists", 
								symbol.getName()));
					}
					env.set(symbol, malVal);
				}
			});
		}
		
		return env;
	}
	
	
	private final JavaInterceptor interceptor;
}
