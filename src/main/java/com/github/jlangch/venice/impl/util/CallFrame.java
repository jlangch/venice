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
package com.github.jlangch.venice.impl.util;

import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.util.StackFrame;


public class CallFrame {

	public CallFrame(final VncFunction fn) {
		this.fnName = fn.getQualifiedName();
		this.args = null;
		this.meta = fn.getMeta();
		this.env = null;
	}
	
	public CallFrame(final VncFunction fn, final VncList args) {
		this.fnName = fn.getQualifiedName();
		this.args = args;
		this.meta = fn.getMeta();
		this.env = null;
	}
	
	public CallFrame(final String fnName, final VncVal meta) {
		this.fnName = fnName;
		this.args = null;
		this.meta = meta;
		this.env = null;
	}
	
	public CallFrame(final String fnName, final VncList args, final VncVal meta) {
		this.fnName = fnName;
		this.args = args;
		this.meta = meta;
		this.env = null;
	}
		
	public CallFrame(final String fnName, final VncList args, final VncVal meta, final Env env) {
		this.fnName = fnName;
		this.args = args;
		this.meta = meta;
		this.env = env;
	}

	public static CallFrame from(final VncSymbol sym) {
		return new CallFrame(sym.getQualifiedName(), sym.getMeta());
	}

	
	public String getFnName() {
		return fnName;
	}

	public VncList getArgs() {
		return args;
	}

	public Env getEnv() {
		return env;
	}
	
	public boolean hasFnName(final String fnName) {
		return this.fnName.equals(fnName);
	}
	
	public String getFile() {
		// Accessing meta data is expensive so its delayed until the data
		// is needed. Creating a CallFrame must be as fast as possible.
		final String file = MetaUtil.getFile(meta);
		return file == null || file.isEmpty() ? "unknown" : file;
	}
	
	public int getLine() {
		// Accessing meta data is expensive so its delayed until the data
		// is needed. Creating a CallFrame must be as fast as possible.
		return MetaUtil.getLine(meta);		
	}
	
	public int getCol() {
		// Accessing meta data is expensive so its delayed until the data
		// is needed. Creating a CallFrame must be as fast as possible.
		return MetaUtil.getCol(meta);		
	}
	
	public StackFrame toStackFrame() {
		return new StackFrame(getFnName(), getFile(), getLine(), getCol());
	}
	
	public String getSourcePosInfo() {
		return String.format("%s: line %d, col %d", getFile(), getLine(), getCol());
	}

	@Override
	public String toString() {
		return fnName == null
				? getSourcePosInfo()
				: String.format("%s (%s)", fnName, getSourcePosInfo());
	}

	
	private final String fnName;
	private final VncList args;
	private final VncVal meta;
	private final Env env;
}
