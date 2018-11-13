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

import java.io.Serializable;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;


public class Var implements Serializable {

	public Var(final VncSymbol name, final VncVal val) {
		this(name, val, false);
	}

	public Var(final VncSymbol name, final VncVal val, final boolean dynamic) {
		this.name = name;
		this.val = val == null ? Constants.Nil : val;
		this.dynamic = dynamic;
	}
	
	public VncVal getVal() {
		if (isDynamic()) {
			return ThreadLocalMap.get(new VncKeyword(name.getName()), val);
		}
		else {
			return val;
		}
	}
	
	public void setVal(final VncVal val) {
		this.val = val == null ? Constants.Nil : val;
	}
	
	public void setValDynamic(final VncVal val) {
		if (!isDynamic()) {
			throw new VncException(String.format(
					"The var %s is not defined as dynamic",
					name.getName()));
		}
		ThreadLocalMap.set(new VncKeyword(name.getName()), val);
	}

	public VncSymbol getName() {
		return name;
	}
	
	public boolean isDynamic() {
		return dynamic;
	}

	
	private static final long serialVersionUID = 1598432086227773369L;

	private final VncSymbol name;
	private VncVal val;
	private final boolean dynamic;
}
