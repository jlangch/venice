/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Creates a <code>ThreadFactory</code> that creates daemon threads and gives
 * the threads a unique name with an incremented index. So this threads can be
 * always associated with a <code>ThreadFactory</code> when analyzing Java VM 
 * threads.
 */
public class ThreadPoolUtil {

	/**
	 * Creates a new counted <code>ThreadFactory</code>
	 * 
	 * @param poolName a pool name like: "venice-future-pool"
	 * @param deamon if <code>true</code> create daemon threads
	 * @return the <code>ThreadFactory</code>
	 */
	public static ThreadFactory createCountedThreadFactory(
			final String poolName, 
			final boolean deamon
	) {
		return new CountedThreadFactory(poolName, deamon);
	}
	

	public static class CountedThreadFactory implements ThreadFactory {
		public CountedThreadFactory(
				final String poolName,
				final boolean deamon
		) {
			this.poolName = poolName;
			this.deamon = deamon;
		}
		
		@Override
		public Thread newThread(final Runnable runnable) {
			final Thread thread = new Thread(runnable);
			thread.setDaemon(deamon);
			thread.setName(poolName + "-" + counter.getAndIncrement());
			return thread;
		}
		
		private final String poolName;
		private final boolean deamon;
		private final AtomicLong counter = new AtomicLong(1L);
	}
}
