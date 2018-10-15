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


public class Agent {

	public Agent() {
	}
	
	public static void shutdown(){
		sendExecutor.shutdown();
		sendOffExecutor.shutdown();
	}

	
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
