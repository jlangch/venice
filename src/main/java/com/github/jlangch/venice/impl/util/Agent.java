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
package com.github.jlangch.venice.impl.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class Agent {

	public Agent(final VncVal state, final VncList options) {
		value.set(new Value(state == null ? Constants.Nil : state, null));
		
		final VncMap opts = new VncHashMap(options);

		errorHandler.set(getErrorHandler(opts));
		
		final VncKeyword errMode = getErrorMode(opts);
		continueOnError =  errMode == null ? true : errMode.equals(ERROR_MODE_CONTINUE);
	}

	public VncVal deref() {
		return value.get().deref();
	}
	
	public RuntimeException getError() {
		return value.get().getException();
	}

	public void send(final VncFunction fn, final VncList args) {
		sendExecutor.execute(() -> update(fn, args));
	}

	public void send_off(final VncFunction fn, final VncList args) {
		sendOffExecutor.execute(() -> update(fn, args));
	}

	public void restart(final VncVal state) {
		value.set(new Value(state, null));
	}
	
	public void addWatch(final VncKeyword name, final VncFunction fn) {
		watchable.addWatch(name, fn);
	}
	
	public void removeWatch(final VncKeyword name) {
		watchable.removeWatch(name);
	}
	
	public void setErrorHandler(final VncFunction errorHandler) {
		this.errorHandler.set(errorHandler);
	}

	@Override 
	public String toString() {
		return toString(true);
	}

	public String toString(final boolean print_readably) {
		final Value v = value.get();
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(agent ");
		if (v.ex != null) {
			sb.append(":error ");
			sb.append(v.ex.getClass().getName());
		}
		sb.append(":value ");
		sb.append(Printer._pr_str(v.val, print_readably));
		sb.append(")");
		
		return sb.toString();
	}

	public static void shutdown(){
		sendExecutor.shutdown();
		sendOffExecutor.shutdown();
	}

	private static VncFunction getErrorHandler(final VncMap options) {
		if (options != null) {
			final VncVal errHandler = options.get(ERROR_HANDLER);
			if (errHandler != Constants.Nil) {
				return Coerce.toVncFunction(errHandler);
			}
		}
				
		return null;
	}
	
	private static VncKeyword getErrorMode(final VncMap options) {
		final VncVal mode = options == null ? Constants.Nil : options.get(ERROR_MODE);		
		if (mode == Constants.Nil) {
			return null;
		}
		
		final VncKeyword errMode = Coerce.toVncKeyword(mode);
		if (((VncKeyword)errMode).equals(ERROR_MODE_CONTINUE)) {
			return ERROR_MODE_CONTINUE;
		}
		else if (((VncKeyword)errMode).equals(ERROR_MODE_FAIL)) {
			return ERROR_MODE_FAIL;
		}
		else {
			return null;
		}
	}
	
	private void update(final VncFunction fn, final VncList args) {
		if (getError() == null || continueOnError) {
			final VncVal oldVal = value.get().val;
			try {
				final VncList fnArgs = args.copy().addAtStart(value.get().val);
				final VncVal newVal = fn.apply(fnArgs);
				
				value.set(new Value(newVal, null));
				watchable.notifyWatches(new VncJavaObject(this), oldVal, newVal);
			}
			catch(RuntimeException ex) {
				if (!continueOnError) {
					value.set(new Value(oldVal, ex));
				}
				
				final VncFunction handler = errorHandler.get();
				if (handler != null) {
					handler.apply(
							new VncList(
									new VncJavaObject(this), new VncJavaObject(ex)));
				}
			}
		}
	}
		
	private static class Value {
		public Value(final VncVal val, final RuntimeException ex) {
			this.val = val;
			this.ex = ex;
		}
		
		public VncVal deref() {
			if (ex != null) {
				throw ex;
			}
			else {
				return val;
			}
		}
		
		public RuntimeException getException() {
			return ex;
		}

		
		private final VncVal val;
		private final RuntimeException ex;
	}
	
	
	private final static VncKeyword ERROR_HANDLER = new VncKeyword("error-handler");
	private final static VncKeyword ERROR_MODE = new VncKeyword("error-mode");
	private final static VncKeyword ERROR_MODE_CONTINUE = new VncKeyword("continue");
	private final static VncKeyword ERROR_MODE_FAIL = new VncKeyword("fail");
	
	private final AtomicReference<VncFunction> errorHandler = new AtomicReference<>();
	private final AtomicReference<Value> value = new AtomicReference<>(new Value(Constants.Nil, null)); 
	private final Watchable watchable = new Watchable();
	
	private final boolean continueOnError;
	
	
	private final static AtomicLong sendThreadPoolCounter = new AtomicLong(0);

	private final static AtomicLong sendOffThreadPoolCounter = new AtomicLong(0);

	private final static ExecutorService sendExecutor = 
			Executors.newFixedThreadPool(
					2 + Runtime.getRuntime().availableProcessors(),
					ThreadPoolUtil.createThreadFactory(
							"venice-agent-send-pool-%d", 
							sendThreadPoolCounter,
							true /* daemon threads */));

	private final static ExecutorService sendOffExecutor = 
			Executors.newCachedThreadPool(
					ThreadPoolUtil.createThreadFactory(
							"venice-agent-send-off-pool-%d", 
							sendOffThreadPoolCounter,
							true /* daemon threads */));
}
