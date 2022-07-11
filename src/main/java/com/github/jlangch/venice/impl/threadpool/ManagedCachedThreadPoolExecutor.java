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
package com.github.jlangch.venice.impl.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;


public class ManagedCachedThreadPoolExecutor extends ManagedExecutor {

    public ManagedCachedThreadPoolExecutor(
            final String threadPoolName,
            final int maxPoolSize
    ) {
        this.threadPoolName = threadPoolName;
    	this.maxThreadPoolSize = maxPoolSize;
    }


    @Override
    public ThreadPoolExecutor getExecutor() {
        return (ThreadPoolExecutor)super.getExecutor();
    }

    @Override
	protected ExecutorService createExecutorService() {
        final ThreadPoolExecutor es = (ThreadPoolExecutor)Executors.newCachedThreadPool(
                                            ThreadPoolUtil.createCountedThreadFactory(
                                                    threadPoolName, true));
        es.setMaximumPoolSize(maxThreadPoolSize);
        return es;
    }


    public void setMaximumThreadPoolSize(final int poolSize) {
        synchronized(this) {
        	maxThreadPoolSize = Math.max(1, poolSize);
            if (super.exists()) {
                getExecutor().setMaximumPoolSize(maxThreadPoolSize);
            }
        }
    }

    public int getCoreThreadPoolSize() {
        return getExecutor().getCorePoolSize();
    }

    public int getMaximumThreadPoolSize() {
        return getExecutor().getMaximumPoolSize();
    }

    public int getLargestThreadPoolSize() {
        return getExecutor().getLargestPoolSize();
    }

    public int getActiveThreadCount() {
        return getExecutor().getActiveCount();
    }

    public int getThreadPoolSize() {
        return getExecutor().getPoolSize();
    }

    public long getScheduledTaskCount() {
        return getExecutor().getTaskCount();
    }

    public long getCompletedTaskCount() {
        return getExecutor().getCompletedTaskCount();
    }

    public VncMap info() {
        return VncOrderedMap.of(
                new VncKeyword("core-pool-size"),
                new VncLong(getCoreThreadPoolSize()),

                new VncKeyword("maximum-pool-size"),
                new VncLong(getMaximumThreadPoolSize()),

                new VncKeyword("current-pool-size"),
                new VncLong(getThreadPoolSize()),

                new VncKeyword("largest-pool-size"),
                new VncLong(getLargestThreadPoolSize()),

                new VncKeyword("active-thread-count"),
                new VncLong(getActiveThreadCount()),

                new VncKeyword("scheduled-task-count"),
                new VncLong(getScheduledTaskCount()),

                new VncKeyword("completed-task-count"),
                new VncLong(getCompletedTaskCount()));
    }


    private final String threadPoolName;
    private volatile int maxThreadPoolSize;
}

