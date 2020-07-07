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
package com.github.jlangch.venice.impl.types;

import java.util.Arrays;
import java.util.List;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.functions.FunctionsUtil;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.util.Types;


public class VncKeyword extends VncString implements IVncFunction, INamespaceAware {
	
	public VncKeyword(final String v) { 
		this(v, Constants.Nil); 
	}

	public VncKeyword(final String v, final VncVal meta) {
		super(v.startsWith(":") ? v.substring(1): v, meta); 
		
		final String name = v.startsWith(":") ? v.substring(1): v;
		final int pos = name.indexOf("/");

		qualifiedName = name;
		simpleName = pos < 0 ? name : name.substring(pos+1); 	
		namespace = pos < 0 ? null : name.substring(0, pos);
	}

	private VncKeyword(final String namespace, final String simpleName, final String qualifiedName, final VncVal meta) { 
		super(qualifiedName, meta);
		
		this.qualifiedName = qualifiedName;
		this.simpleName = simpleName;
		this.namespace = namespace;
	}

	private VncKeyword(final VncKeyword other, final VncVal meta) { 
		super(other.qualifiedName, meta);
		
		qualifiedName = other.qualifiedName;
		simpleName = other.simpleName;
		namespace = other.namespace;
	}

	
	public VncVal apply(final VncList args) {
		FunctionsUtil.assertArity("keyword", args, 1, 2);
		
		final VncVal first = args.first();
		
		if (first == Constants.Nil) {
			return args.second();
		}
		else if (Types.isVncMap(first)) {
			final VncMap map = (VncMap)first;
			if (args.size() == 1) {
				return map.get(this);
			}
			else if (VncBoolean.isTrue(map.containsKey(this))) {
				return map.get(this);
			}
			else {
				return args.second();  // return default value
			}
		}
		else if (Types.isVncSet(first)) {
			final VncSet set = (VncSet)first;
			if (args.size() == 1) {
				return set.contains(this) ? this : Constants.Nil;
			}
			else if (set.contains(this)) {
				return this;
			}
			else {
				return args.second();  // return default value
			}
		}
		else {
			throw new VncException(String.format(
					"keyword as function does not allow arg %s.",
					Types.getType(first)));
		}
	}
	
	
	@Override
	public VncKeyword withMeta(final VncVal meta) {
		return new VncKeyword(getValue(), meta);
	}
	
	public VncKeyword withNamespace(final VncSymbol namespace) {
		if (namespace.hasNamespace()) {
			throw new VncException(String.format(
					"A namespace '%s' must not be qualified with an other namespace",
					namespace));
		}

		return withNamespace(namespace.getName());
	}
	
	public VncKeyword withNamespace(final String namespace) {
		if (hasNamespace()) {
			throw new VncException(String.format(
					"The keyword '%s' is already qualified with a namespace",
					qualifiedName));
		}
		
		return new VncKeyword(namespace, simpleName, namespace + "/" + simpleName, getMeta());
	}
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}
	
	@Override
	public VncKeyword getSupertype() {
		return VncString.TYPE;
	}
	
	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncString.TYPE, VncVal.TYPE);
	}

	@Override
	public String getValue() { 
		return qualifiedName; 
	}

	@Override
	public String getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}
	
	@Override
	public boolean hasNamespace() {
		return namespace != null;
	}
	
	
	public VncSymbol toSymbol() {
		return new VncSymbol(qualifiedName);
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.KEYWORD;
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncKeyword(o)) {
			return qualifiedName.compareTo(((VncKeyword)o).qualifiedName);
		}

		return super.compareTo(o);
	}

	@Override 
	public String toString() {
		return ":" + getValue();
	}
	
	public String toString(final boolean print_readably) {
		return toString();
	}
	
	
    public static final VncKeyword TYPE = new VncKeyword(":core/keyword");
	
    private static final long serialVersionUID = -1848883965231344442L;
    

	private final String qualifiedName;
	private final String simpleName;
	private final String namespace;
}