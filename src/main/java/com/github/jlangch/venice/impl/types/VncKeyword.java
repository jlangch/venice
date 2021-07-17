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
package com.github.jlangch.venice.impl.types;

import java.util.Arrays;
import java.util.List;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;


public class VncKeyword extends VncString implements IVncFunction, INamespaceAware {
	
	public VncKeyword(final String v) { 
		this(parse(v), Constants.Nil); 
	}

	public VncKeyword(final String v, final VncVal meta) {
		this(parse(v), meta); 
	}

	public VncKeyword(final String namespace, final String simpleName, final VncVal meta) {
		this(namespace, 
			 simpleName, 
			 namespace == null ? simpleName : namespace + "/" + simpleName, 
			 meta); 
	}

	private VncKeyword(final String namespace, final String simpleName, final String qualifiedName, final VncVal meta) { 
		super(qualifiedName, meta);

		this.namespace = namespace;
		this.simpleName = simpleName;
		this.qualifiedName = qualifiedName;
	}

	private VncKeyword(final String[] elements, final VncVal meta) { 
		super(elements[2], meta);

		this.namespace = elements[0];
		this.simpleName = elements[1];
		this.qualifiedName = elements[2];
	}

	private VncKeyword(final VncKeyword other, final VncVal meta) { 
		super(other.qualifiedName, meta);
		
		qualifiedName = other.qualifiedName;
		simpleName = other.simpleName;
		namespace = other.namespace;
	}

	private static String[] parse(final String name) { 
		final String qn = name.charAt(0) == ':' ? name.substring(1) : name;
		
		final int pos = qn.indexOf("/");

		final String namespace = pos <= 0 ? null : qn.substring(0, pos);
		final String simpleName = pos < 0 ? qn : qn.substring(pos+1);
		final String qualifiedName = namespace == null ? simpleName : namespace + "/" + simpleName;
		
		return new String[] {namespace, simpleName, qualifiedName};
	}

	@Override
	public VncVal apply(final VncList args) {
		ArityExceptions.assertArity(this, FnType.Keyword, args, 1, 2);
		
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
	public VncList getArgLists() { 
		return VncList.of(
				new VncString("(keyword map)"),
				new VncString("(keyword map default-val)"));
	}

	@Override
	public VncKeyword withMeta(final VncVal meta) {
		return new VncKeyword(this, meta);
	}
	
	public VncKeyword withNamespace(final VncSymbol namespace) {
		if (Types.isJavaTypeReference(this)) {
			return this;
		}
		else if (namespace.hasNamespace()) {
			throw new VncException(String.format(
					"A namespace '%s' must not be qualified with an other namespace",
					namespace));
		}
		else {
			return withNamespace(namespace.getName());
		}
	}
	
	public VncKeyword withNamespace(final String namespace) {
		if (hasNamespace()) {
			throw new VncException(String.format(
					"The keyword '%s' is already qualified with a namespace",
					qualifiedName));
		}
		
		final boolean emptyNS = (namespace == null || namespace.isEmpty());

		return new VncKeyword(
					emptyNS ? null : namespace, 
					simpleName,
					emptyNS ? simpleName : namespace + "/" + simpleName,
					getMeta());
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
	public int hashCode() {
		return qualifiedName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		else if (getClass() != obj.getClass()) {
			return false;
		}
		else {
			return qualifiedName.equals(((VncKeyword)obj).qualifiedName);
		}
	}

	@Override 
	public String toString() {
		return ":" + getValue();
	}
	
	public String toString(final boolean print_readably) {
		return toString();
	}
	
	public boolean hasValue(final String value) {
		return value != null && value.contentEquals(getValue());
	}
	
	
    public static final VncKeyword TYPE = new VncKeyword(":core/keyword");
	
    private static final long serialVersionUID = -1848883965231344442L;
    

	private final String qualifiedName;
	private final String simpleName;
	private final String namespace;
}