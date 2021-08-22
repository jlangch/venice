/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.impl.repl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.github.jlangch.venice.InterruptedException;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalSnapshot;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class ScriptExecuter {

	public ScriptExecuter() {
		
	}
	
	
	public void runSync(
			final String script,
			final IVeniceInterpreter venice,
			final Env env,
			final TerminalPrinter printer,
			final String resultPrefix,
			final ReplResultHistory resultHistory,
			final Consumer<Exception> errorHandler
	) {
		try {
			final VncVal result = venice.RE(script, "user", env);
			if (result != null) {
				printer.println("result", resultPrefix + venice.PRINT(result));
				
				// do not add the result for "*1", "*2", "*3", "**" to the result history 
				if (!resultHistory.isResultHistorySymbol(script)) {
					resultHistory.add(result);
				}
			}
		}
		catch (Exception ex) {
			errorHandler.accept(ex);
		}
	}
	
	public void runAsync(
			final String script,
			final IVeniceInterpreter venice,
			final Env env,
			final TerminalPrinter printer,
			final String resultPrefix,
			final ReplResultHistory resultHistory,
			final Consumer<Exception> errorHandler
	) {
		final long asyncID = asyncCounter.getAndIncrement();
		
		printer.println("debug", String.format("[%d] Async ...", asyncID));

		final Thread replThread = Thread.currentThread();
		
		// thread local values from the parent thread
		final AtomicReference<ThreadLocalSnapshot> parentThreadLocalSnapshot = 
				new AtomicReference<>(ThreadLocalMap.snapshot());
		
		final IInterceptor interceptor = JavaInterop.getInterceptor();
		
		final Callable<Boolean> task = () -> {
			ThreadLocalMap.inheritFrom(parentThreadLocalSnapshot.get());
			ThreadLocalMap.clearCallStack();
			JavaInterop.register(interceptor);	

			try {
				final VncVal result = venice.RE(script, "user", env);
	
				printer.println("result", String.format(
											"[%d] %s%s", 
											asyncID, 
											resultPrefix, 
											venice.PRINT(result)));
				
				printer.println("debug", String.format(
											"[%d] Async execution finished.", 
											asyncID));
				
				// do not add the result for "*1", "*2", "*3", "**" to the result history 
				if (!resultHistory.isResultHistorySymbol(script)) {
					resultHistory.add(result);
				}
	
				try { Thread.sleep(200); } catch(Exception ex) {}
	
				return true;
			}
			catch (InterruptedException ex) {
				printer.println("debug", "Script under debugging interrupted and terminated!");
				return false;
			}
			catch (Exception ex) {
				errorHandler.accept(ex);
				return false;
			}
			finally {
				ThreadLocalMap.remove();

				// Interrupt the LineReader of the REPLto display a new prompt
				replThread.interrupt();
			}
		};

		cancellableAsynScript = executor.submit(task);
	}
	
	public void cancelAsyncScript() {
		final Future<Boolean> future = cancellableAsynScript;
		if (future != null) {
			future.cancel(true);
		}
	}

	public void runDebuggerExpressionAsync(
			final String expr,
			final IVeniceInterpreter venice,
			final Env env,
			final TerminalPrinter printer,
			final Consumer<Exception> errorHandler
	) {
		// thread local values from the parent thread
		final AtomicReference<ThreadLocalSnapshot> parentThreadLocalSnapshot = 
				new AtomicReference<>(ThreadLocalMap.snapshot());

		final IInterceptor interceptor = JavaInterop.getInterceptor();

		// run the expression in another thread without debugger!! 
		final Runnable task = () -> {
			ThreadLocalMap.inheritFrom(parentThreadLocalSnapshot.get());
			ThreadLocalMap.clearCallStack();
			JavaInterop.register(interceptor);
			DebugAgent.unregister();  // do not run under debugger!!

			try {
				final Env safeEnv = new Env(env);
				
				final VncVal result = venice.RE(expr, "debugger", safeEnv);
				printer.println("debug", venice.PRINT(result));
			}
			catch (Exception ex) {
				errorHandler.accept(ex);
			}
			finally {
				ThreadLocalMap.remove();
			}
		};

		try {
			executor.submit(task).get();
		}
		catch(Exception ex) {
			errorHandler.accept(ex);
		}
	}

	public boolean runInitialLoadFile(
			final String loadFile, 
			final IVeniceInterpreter venice,
			final Env env, 
			final TerminalPrinter printer,
			final String resultPrefix
	) {
		try {
			if (loadFile != null) {
				printer.println("stdout", "Loading file \"" + loadFile + "\"");
				final VncVal result = venice.RE(
										"(load-file \"" + loadFile + "\")", 
										"user", 
										env);
				printer.println("stdout", resultPrefix + venice.PRINT(result));
			}
			return true;
		}
		catch(Exception ex) {
			printer.printex("error", ex);
			return false;
		}
	}
	
	
	
	private volatile Future<Boolean> cancellableAsynScript = null;
	
	private final AtomicLong asyncCounter = new AtomicLong(1L);

	// need at least 2 parallel threads!!
	private final ExecutorService executor = Executors.newFixedThreadPool(4);
}
