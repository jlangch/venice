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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.util.QualifiedName;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncSymbol extends VncVal implements INamespaceAware {

	public VncSymbol(final String v) { 
		this(v, Constants.Nil);
	}

	public VncSymbol(final String v, final VncVal meta) { 
		super(meta);
		
		final QualifiedName qn = QualifiedName
									.parse(v)
									.mapCoreNamespaceToNull();
		
		namespace = qn.getNamespace();
		simpleName = qn.getSimpleName(); 	
		qualifiedName = qn.getQualifiedName();
		
		hash = qualifiedName.hashCode();
	}

	public VncSymbol(final String ns, final String name, final VncVal meta) { 
		super(meta);
		
		final QualifiedName qn = QualifiedName
									.of(ns, name)
									.mapCoreNamespaceToNull();
		
		namespace = qn.getNamespace();
		simpleName = qn.getSimpleName(); 	
		qualifiedName = qn.getQualifiedName();
		
		hash = qualifiedName.hashCode();
	}

	private VncSymbol(final VncSymbol other, final VncVal meta) { 
		super(meta);
		
		namespace = other.namespace;
		simpleName = other.simpleName;
		qualifiedName = other.qualifiedName;
		hash = other.hash;
	}
	
	@Override
	public VncSymbol withMeta(final VncVal meta) {
		return new VncSymbol(this, meta);
	}

	public VncSymbol withNamespace(final VncSymbol namespace) {
		if (namespace.hasNamespace()) {
			throw new VncException(String.format(
					"A namespace '%s' must not be qualified with an other namespace",
					namespace));
		}

		return withNamespace(namespace.getName());
	}

	public VncSymbol withNamespace(final String namespace) {
		if (hasNamespace()) {
			throw new VncException(String.format(
					"The symbol '%s' is already qualified with a namespace",
					qualifiedName));
		}
		
		return new VncSymbol(namespace, simpleName, getMeta());
	}

	
	@Override
	public VncKeyword getType() {
		return new VncKeyword(
						TYPE, 
						MetaUtil.typeMeta(
							new VncKeyword(VncVal.TYPE)));
	}

	public String getName() { 
		return qualifiedName; 
	}

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
	
	public static VncSymbol qualifySymbol(final VncSymbol ns, final VncSymbol sym) {
		if (sym.hasNamespace()) {
			throw new VncException(String.format(
					"The symbol '%s' is already qualified with a namespace",
					sym.getName()));
		}
		return new VncSymbol(ns.getName() + "/" + sym.getName());
	}

	@Override 
	public TypeRank typeRank() {
		return TypeRank.SYMBOL;
	}
	
	@Override
	public Object convertToJavaObject() {
		return qualifiedName;
	}

	@Override 
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncSymbol(o)) {
			return getName().compareTo(((VncSymbol)o).getName());
		}

		return super.compareTo(o);
	}
	
	@Override
	public int hashCode() {
		// performance:
		// pre-calculated hash for symbols (symbol table put/get)
		return hash;  
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
			return qualifiedName.equals(((VncSymbol)obj).qualifiedName);
		}
	}

	@Override 
	public String toString() {
		return qualifiedName;
	}
	
	
    public static final String TYPE = ":core/symbol";

    private static final long serialVersionUID = -1848883965231344442L;

	private final String qualifiedName;
	private final String simpleName;
	private final String namespace;
	private final int hash;

}