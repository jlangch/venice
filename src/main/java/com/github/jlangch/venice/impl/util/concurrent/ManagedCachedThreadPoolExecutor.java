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

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class ManagedCachedThreadPoolExecutor extends ManagedExecutor {

	public ManagedCachedThreadPoolExecutor(
			final String threadPoolName, 
			final int maxPoolSize
	) {
		super(threadPoolName);
		this.maximumThreadPoolSize = maxPoolSize;
	}
	
	@Override
	public ThreadPoolExecutor getExecutor() {
		return (ThreadPoolExecutor)super.getExecutor();
	}

	@Override
	protected ThreadPoolExecutor createExecutorService() {
		synchronized(this) {
			final ThreadPoolExecutor es = 
					(ThreadPoolExecutor)Executors.newCachedThreadPool(createThreadFactory());
			es.setMaximumPoolSize(maximumThreadPoolSize);
			return es;
		}
	}

	
	public void setMaximumThreadPoolSize(final int maximumPoolSize) {
		synchronized(this) {
			if (super.exists()) {
				maximumThreadPoolSize = Math.max(1, maximumPoolSize);
				getExecutor().setMaximumPoolSize(maximumThreadPoolSize);
			}
		}
	}

	
	private int maximumThreadPoolSize;
}

