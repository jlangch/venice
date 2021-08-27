/*
 * Copyright (C) 2000-2013 Heinz Max Kabutz
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Heinz Max Kabutz licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.impl.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadPoolUtil;


public abstract class ManagedExecutor {

	public ManagedExecutor(final String threadPoolName) {
		this.threadPoolName = threadPoolName;
	}
	

	public ExecutorService getExecutor() {
		synchronized(this) {
			if (executor == null) {
				executor = createExecutorService();				
			}
			return executor;
		}
	}

	public boolean exists() {
		synchronized(this) {
			return executor != null;
		}
	}

	public boolean isShutdown() {
		synchronized(this) {
			return executor == null ? true : executor.isShutdown();
		}
	}

	public void awaitTermination(final long timeoutMillis) {
		synchronized(this) {
			if (executor != null) {
				try {
					executor.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
				}
				catch(Exception ex) {
					throw new VncException(
							"Failed awaiting for executor termination", 
							ex);
				}
			}
		}
	}

	public boolean isTerminated() {
		synchronized(this) {
			return executor == null ? true : executor.isTerminated();
		}
	}

	public void shutdown() {
		synchronized(this) {
			if (executor != null) {
				try {
					executor.shutdown();
				}
				catch(Exception ex) {
					// silently
				}
				finally {
					executor = null;
				}
			}
		}
	}

	public void shutdownNow() {
		synchronized(this) {
			if (executor != null) {
				try {
					executor.shutdownNow();
				}
				catch(Exception ex) {
					// silently
				}
				finally {
					executor = null;
				}
			}
		}
	}

	abstract protected ExecutorService createExecutorService();

	protected ThreadFactory createThreadFactory() {
		return ThreadPoolUtil.createThreadFactory(
					threadPoolName + "-%d", 
					threadPoolCounter,
					true /* daemon threads */);
	}
	

	private final String threadPoolName;
	private final AtomicLong threadPoolCounter = new AtomicLong(0);
	private ExecutorService executor;
}
