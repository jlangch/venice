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
package com.github.jlangch.venice.impl;

import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.javainterop.JavaImports;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class Namespace {
	
	public Namespace(final VncSymbol ns) {
		this.ns = ns == null ? Namespaces.NS_USER : ns;
	}

	public VncSymbol getNS() {
		return ns;
	}

	public JavaImports getJavaImports() {
		return javaImports;
	}
	
	public VncList getJavaImportsAsVncList() {
		return VncList.ofList(
				javaImports
					.list()
					.stream().map(s -> new VncKeyword(s))
					.collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		return ns.getName();
	}
	
	
	private final VncSymbol ns;
	private final JavaImports javaImports = new JavaImports();
}
