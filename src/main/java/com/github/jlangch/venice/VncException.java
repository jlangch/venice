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
package com.github.jlangch.venice;

import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.CallStack;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;


public class VncException extends RuntimeException {

	public VncException() {
	}
	
	public VncException(final String message) {
		super(message);
	}

	public VncException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public VncException(final Throwable cause) {
		super(cause);
	}

	public boolean hasCallStack() {
		return !callstack.isEmpty();
	}

	public VncVector getCallStack() {
		return callstack;
	}

	public List<String> getCallStackAsStringList() {
		return getCallStack()
					.getList()
					.stream()
					.map(v -> callFrameToString((VncMap)v))
					.collect(Collectors.toList());
	}

	public String getCallStackAsString(final String indent) {
		return getCallStack()
					.getList()
					.stream()
					.map(v -> (indent == null ? "" : indent) + callFrameToString((VncMap)v))
					.collect(Collectors.joining("\n"));
	}
	
	public void printVncStacktrace() {
		System.out.println(String.format(
				"Exception in thread \"%s\" %s: %s",
				Thread.currentThread().getName(),
				getClass().getSimpleName(),
				getMessage()));
		
		if (hasCallStack()) {
			System.out.println(getCallStackAsString("    at: "));
		}
		
		if (getCause() != null) {
			printVncCauseStacktrace(getCause());
		}
	}

	private void printVncCauseStacktrace(final Throwable ex) {
		System.out.println();
		System.out.println(String.format(
				"Caused by: %s: %s",
				ex.getClass().getSimpleName(),
				ex.getMessage()));

		if (ex instanceof VncException) {
			final VncException vncEx = (VncException)ex;
			if (vncEx.hasCallStack()) {
				System.out.println(vncEx.getCallStackAsString("    at: "));
			}
		}
		
		if (ex.getCause() != null) {
			printVncCauseStacktrace(ex.getCause());
		}
	}

	private String callFrameToString(final VncMap callFrame) {
		return String.format(
				"%s (%s: line %d, col %d)", 
				((VncString)callFrame.get(CallStack.KEY_FN_NAME)).getValue(),
				((VncString)callFrame.get(CallStack.KEY_FILE)).getValue(),
				((VncLong)callFrame.get(CallStack.KEY_LINE)).getValue(),
				((VncLong)callFrame.get(CallStack.KEY_COL)).getValue());
	}

	
	private static final long serialVersionUID = 5439694361809280080L;
	
	private final VncVector callstack = ThreadLocalMap.getCallStack().toVncVector();
}