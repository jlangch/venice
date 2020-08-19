/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl.env;

import java.util.concurrent.atomic.AtomicLong;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.util.StringUtil;


public class GenSym {
	
	public GenSym() {		
	}

	public static VncSymbol generate() {
		return generate(null);
	}

	public static VncSymbol generate(final String prefix) {
		final String suffix = String.valueOf(value.incrementAndGet());
		final String prefix_ = StringUtil.isBlank(prefix) ? DEFAULT_PREFIX : prefix;
		
		return new VncSymbol(prefix_ + suffix);
	}

	public static VncSymbol generateAutoSym(final String name) {
		final String suffix = String.valueOf(value.incrementAndGet());
		
		return new VncSymbol(name + "__" + suffix + "__auto");
	}
	
	
	private static final String DEFAULT_PREFIX = "G__";
	private static final AtomicLong value = new AtomicLong(0);
}
