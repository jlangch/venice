/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.Serializable;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class Var implements Serializable {

	public Var(final VncSymbol name, final VncVal val) {
		this(name, val, true);
	}
	
	public Var(final VncSymbol name, final VncVal val, final boolean overwritable) {
		this.name = name;
		this.val = val == null ? Nil : val;
		this.overwritable = overwritable;
	}
	
	public VncVal getVal() {
		return val;
	}
	
	public VncSymbol getName() {
		return name;
	}
	
 	public boolean isOverwritable() {
		return overwritable;
	}

	@Override 
	public String toString() {
		return String.format(
				"{%s %s :overwritable %b}", 
				name.toString(), 
				val.toString(), 
				overwritable);
	}
	
	public String toString(final boolean print_readably) {
		return String.format(
				"{%s %s :overwritable %b}", 
				name.toString(print_readably), 
				val.toString(print_readably), 
				overwritable);
	}
	

	
	private static final long serialVersionUID = 1598432086227773369L;

	private final VncSymbol name;
	private final VncVal val;
	private final boolean overwritable;
}
