/*   __	__		 _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *	\ \/ / _ \ '_ \| |/ __/ _ \
 *	 \  /  __/ | | | | (_|  __/
 *	  \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.impl.types.custom;

import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncProtocol extends VncVal {

	public VncProtocol(
			final VncSymbol name,
			final VncMap functions,
			final VncVal meta
	) {
		super(meta);
		this.name = name;
		this.functions = functions;
	}

	
	public VncSymbol getName() {
		return name;
	}
	
	public VncMap getFunctions() {
		return functions;
	}

	public VncMultiArityFunction getFunctionForName(final VncString name) {
		return (VncMultiArityFunction)functions.get(name);
	}
	
	public void register(final VncKeyword type) {
		types.put(type, type);
	}
	
	public boolean isRegistered(final VncKeyword type) {
		return types.containsKey(type);
	}
	
	public void unregister(final VncKeyword type) {
		types.remove(type);
	}
	
	@Override
	public VncVal withMeta(final VncVal meta) {
		return new VncProtocol(name, functions, meta);
	}

	@Override
	public VncKeyword getType() {
		return new VncKeyword(
					TYPE, 
					MetaUtil.typeMeta(
						new VncKeyword(VncVal.TYPE)));
	}

	@Override 
	public TypeRank typeRank() {
		return TypeRank.PROTOCOL_TYPE;
	}
	
	@Override
	public Object convertToJavaObject() {
		return null; // not supported
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (o instanceof VncCustomBaseTypeDef) {
			return name.getValue().compareTo(((VncProtocol)o).name.getValue());
		}

		return super.compareTo(o);
	}

	@Override 
	public String toString() {
		return name.toString();
	}
	
	@Override 
	public String toString(final boolean print_readably) {
		return name.toString(print_readably);
	}

	
    public static final String TYPE = ":core/protocol";

    private static final long serialVersionUID = -1848883965231344442L;	
	
    private final VncSymbol name;
    private final VncMap functions;
	private final ConcurrentHashMap<VncKeyword,VncKeyword> types = new ConcurrentHashMap<>();
}
