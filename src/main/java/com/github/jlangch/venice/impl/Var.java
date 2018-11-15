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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.Serializable;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class Var implements Serializable {

	public Var(final VncSymbol name, final VncVal val) {
		this.name = name;
		this.val = val == null ? Nil : val;
	}
	
	public VncVal getVal() {
		return val;
	}
	
	public void setVal(final VncVal val) {
		this.val = val == null ? Nil : val;
	}
	
	public VncSymbol getName() {
		return name;
	}
	
	@Override 
	public String toString() {
		return String.format("{%s %s}", name.toString(), val.toString());
	}
	
	public String toString(final boolean print_readably) {
		return String.format("{%s %s}", name.toString(print_readably), val.toString(print_readably));
	}
	

	
	private static final long serialVersionUID = 1598432086227773369L;

	private final VncSymbol name;
	private VncVal val;
}
