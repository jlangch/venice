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
package com.github.jlangch.venice.impl.types.concurrent;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.thread.ThreadPoolUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.CallStack;
import com.github.jlangch.venice.impl.util.Watchable;
import com.github.jlangch.venice.impl.util.concurrent.StripedExecutorService;
import com.github.jlangch.venice.impl.util.concurrent.StripedRunnable;


public class Agent implements IDeref {

	public Agent(final VncVal state, final VncMap opts) {
		value.set(new Value(state == null ? Constants.Nil : state, null));
		
		errorHandler.set(getErrorHandler(opts));
		validatorFn = getValidator(opts);
		
		final VncKeyword errMode = getErrorMode(opts);
		continueOnError =  errMode == null ? true : errMode.equals(ERROR_MODE_CONTINUE);
	}

	public long getID() {
		return id;
	}
	
	@Override
	public VncVal deref() {
		return value.get().deref();
	}
	
	public RuntimeException getError() {
		return value.get().getException();
	}

	public void send(final VncFunction fn, final VncList args) {
		sendExecutor.execute(
				new Action(
						this, 
						fn, 
						args,
						SendType.SEND,
						ThreadBridge.create("send", new CallFrame(fn))));
	}

	public void send_off(final VncFunction fn, final VncList args) {
		sendOffExecutor.execute(
				new Action(
						this, 
						fn, 
						args, 
						SendType.SEND_OFF,
						ThreadBridge.create("send-off", new CallFrame(fn))));
	}

	public void restart(final VncVal state) {
		validate(state);
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
	
	public VncKeyword getErrorMode() {
		return continueOnError ? ERROR_MODE_CONTINUE : ERROR_MODE_FAIL;
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
		sb.append(Printer.pr_str(v.val, print_readably));
		sb.append(")");
		
		return sb.toString();
	}
		
	public static boolean await(final List<Agent> agents, final long timeoutMillis) {		
		final CountDownLatch latch = new CountDownLatch(agents.size() * 2);
		
		final VncFunction fn = new VncFunction(VncFunction.createAnonymousFuncName()) {
			public VncVal apply(final VncList args) {
				latch.countDown();
				return args.first(); // return old value
			}
			private static final long serialVersionUID = 1L;
		};
		
		try {
			agents.forEach(a -> a.send(fn, VncList.empty()));			
			agents.forEach(a -> a.send_off(fn, VncList.empty()));			
			
			if (timeoutMillis <= 0) {
				latch.await(); 
				return true;
			}
			else {
				return latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
			}
		}
		catch(InterruptedException ex) {
			throw new com.github.jlangch.venice.InterruptedException(
					"Interrupted while waiting for agents (await agents).");
		}
		catch(Exception ex) {
			throw new VncException("Failed awaiting for agents", ex);
		}
	}

	public static void shutdown() {
		sendExecutor.shutdown();
		sendOffExecutor.shutdown();
	}

	public static boolean isShutdown() {
		return sendExecutor.isShutdown() && sendOffExecutor.isShutdown();
	}

	public static void awaitTermination(final long timeoutMillis) {
		try {
			sendExecutor.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
			sendOffExecutor.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
		}
		catch(Exception ex) {
			throw new VncException("Failed awaiting for executor termination", ex);
		}
	}

	public static boolean isTerminated() {
		return sendExecutor.isTerminated() && sendOffExecutor.isTerminated();
	}

	public static VncMap sendExecutorInfo() {
		return VncOrderedMap.of(
				new VncKeyword("core-pool-size"),
				new VncLong(sendExecutor.getCoreThreadPoolSize()),
				
				new VncKeyword("maximum-pool-size"),
				new VncLong(sendExecutor.getMaximumThreadPoolSize()),
				
				new VncKeyword("current-pool-size"),
				new VncLong(sendExecutor.getThreadPoolSize()),

				new VncKeyword("largest-pool-size"),
				new VncLong(sendExecutor.getLargestThreadPoolSize()),
				
				new VncKeyword("active-thread-count"),
				new VncLong(sendExecutor.getActiveThreadCount()),
				
				new VncKeyword("scheduled-task-count"),
				new VncLong(sendExecutor.getScheduledTaskCount()),
				
				new VncKeyword("completed-task-count"),
				new VncLong(sendExecutor.getCompletedTaskCount()));
	}

	public static VncMap sendOffExecutorInfo() {
		return VncOrderedMap.of(
				new VncKeyword("core-pool-size"),
				new VncLong(sendOffExecutor.getCoreThreadPoolSize()),
				
				new VncKeyword("maximum-pool-size"),
				new VncLong(sendOffExecutor.getMaximumThreadPoolSize()),
				
				new VncKeyword("current-pool-size"),
				new VncLong(sendOffExecutor.getThreadPoolSize()),

				new VncKeyword("largest-pool-size"),
				new VncLong(sendOffExecutor.getLargestThreadPoolSize()),
				
				new VncKeyword("active-thread-count"),
				new VncLong(sendOffExecutor.getActiveThreadCount()),
				
				new VncKeyword("scheduled-task-count"),
				new VncLong(sendOffExecutor.getScheduledTaskCount()),
				
				new VncKeyword("completed-task-count"),
				new VncLong(sendOffExecutor.getCompletedTaskCount()));
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

	private static VncFunction getValidator(final VncMap options) {
		if (options != null) {
			final VncVal validator = options.get(VALIDATOR);
			if (validator != Constants.Nil) {
				return Coerce.toVncFunction(validator);
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
	
	private void validate(final VncVal newVal) {
		if (validatorFn != null) {
			try {
				final VncVal ok = validatorFn.apply(VncList.of(newVal));
				if (VncBoolean.isFalseOrNil(ok)) {
					throw new VncException("Invalid agent state");
				}
			}
			catch (VncException ex) {
				throw ex;
			}
			catch (RuntimeException ex) {
				throw new VncException("Invalid agent state");
			}
		}
	}
			
	private static class Action implements StripedRunnable {

		public Action(
				final Agent agent, 
				final VncFunction fn, 
				final VncList fnArgs,
				final SendType sendType,
				final ThreadBridge threadBridge
		) {
			this.agent = agent;
			this.fn = fn;
			this.fnArgs = fnArgs;
			this.sendType = sendType;
			this.threadBridge = threadBridge;
		}
		
		@Override
		public Object getStripe() {
			return agent.getID();
		}
	
		@Override
		public void run() {
			final Runnable task = threadBridge.bridgeRunnable(() -> {
					ThreadContext.pushValue(
							new VncKeyword("*agent*"), 
							new VncJavaObject(agent));

					final CallFrame callFrame = new CallFrame(
							String.format(
									"agent->%s->%s", 
									sendType.toString().toLowerCase(), 
									fn.getQualifiedName()),
							fnArgs.getMeta());

					final CallStack callStack = ThreadContext.getCallStack();
					callStack.clear();
					callStack.push(callFrame);
	
					if (agent.getError() == null || agent.continueOnError) {
						final VncVal oldVal = agent.value.get().val;
						try {
							final VncList fnArgs_ = fnArgs.addAtStart(oldVal);
							final VncVal newVal = fn.apply(fnArgs_);
							
							agent.validate(newVal);
	
							agent.value.set(new Value(newVal, null));
							agent.watchable.notifyWatches(new VncJavaObject(agent), oldVal, newVal);
						}
						catch(RuntimeException ex) {
							if (!agent.continueOnError) {
								agent.value.set(new Value(oldVal, ex));
							}
							
							final VncFunction handler = agent.errorHandler.get();
							if (handler != null) {
								handler.apply(
										VncList.of(
												new VncJavaObject(agent), 
												new VncJavaObject(ex)));
							}
						}
					}
				});
			
			task.run();
		}
		
		private final Agent agent;
		private final VncFunction fn; 
		private final VncList fnArgs;
		private final SendType sendType;
		private final ThreadBridge threadBridge;
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
	
	private enum SendType { 
		SEND, 
		SEND_OFF;
	};
	
	
	//private final static VncKeyword META = new VncKeyword("meta");
	private final static VncKeyword VALIDATOR = new VncKeyword("validator");
	private final static VncKeyword ERROR_HANDLER = new VncKeyword("error-handler");
	private final static VncKeyword ERROR_MODE = new VncKeyword("error-mode");
	private final static VncKeyword ERROR_MODE_CONTINUE = new VncKeyword("continue");
	private final static VncKeyword ERROR_MODE_FAIL = new VncKeyword("fail");
	
	private final AtomicReference<VncFunction> errorHandler = new AtomicReference<>();
	private final AtomicReference<Value> value = new AtomicReference<>(new Value(Constants.Nil, null)); 
	private final VncFunction validatorFn;
	private final Watchable watchable = new Watchable();
	private final long id = agentCounter.getAndIncrement();
	
	private final boolean continueOnError;
	
	
	private final static AtomicLong agentCounter = new AtomicLong(0);
	
	private final static AtomicLong sendThreadPoolCounter = new AtomicLong(0);

	private final static AtomicLong sendOffThreadPoolCounter = new AtomicLong(0);

	private final static StripedExecutorService sendExecutor = 
			new StripedExecutorService(
				Executors.newFixedThreadPool(
						2 + Runtime.getRuntime().availableProcessors(),
						ThreadPoolUtil.createThreadFactory(
								"venice-agent-send-pool-%d", 
								sendThreadPoolCounter,
								true /* daemon threads */)));

	private final static StripedExecutorService sendOffExecutor = 
			new StripedExecutorService(
				Executors.newCachedThreadPool(
						ThreadPoolUtil.createThreadFactory(
								"venice-agent-send-off-pool-%d", 
								sendOffThreadPoolCounter,
								true /* daemon threads */)));
}
