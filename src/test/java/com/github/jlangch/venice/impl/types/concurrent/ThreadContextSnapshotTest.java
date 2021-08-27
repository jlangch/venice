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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;


public class ThreadContextSnapshotTest {

	@Test
	public void test_snapshot_thread_same() throws Exception {
		final ThreadContext ctx = ThreadContext.get();
		assertNotNull(ctx);
		
		final ThreadContextSnapshot ctxSnapshot = ThreadContext.snapshot();
		assertEquals(Thread.currentThread().getId(), ctxSnapshot.getThreadID());
			
		assertTrue(ctxSnapshot.isSameAsCurrentThread());
	}

	@Test
	public void test_snapshot_thread_different() throws Exception {
		final ThreadContext ctx = ThreadContext.get();
		assertNotNull(ctx);
		
		final ThreadContextSnapshot ctxSnapshot = ThreadContext.snapshot();
		assertEquals(Thread.currentThread().getId(), ctxSnapshot.getThreadID());
		
		final AtomicBoolean diffrentThread = new AtomicBoolean();

		final Runnable r = () -> diffrentThread.set(
									ctxSnapshot.isDifferentFromCurrentThread());
		
		final Thread th = new Thread(r);
		th.start();
		th.join();
			
		assertTrue(diffrentThread.get());
	}

}
