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
package com.github.jlangch.venice.impl.specialforms;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.CustomTypeDefRegistry;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.SpecialForms;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncTinyList;
import com.github.jlangch.venice.impl.types.custom.VncChoiceTypeDef;
import com.github.jlangch.venice.impl.types.custom.VncCustomTypeDef;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.Doc;


public class DocForm {

	public static VncString doc(
			final VncVal ref,
			final Env env,
			final CustomTypeDefRegistry typeDefRegistry
	) {			
		if (Types.isVncSymbol(ref)) {
			return docForSymbol((VncSymbol)ref, env);
		}
		else if (Types.isVncKeyword(ref)) {
			return docForCustomType((VncKeyword)ref, typeDefRegistry);
		}
		else if (Types.isVncString(ref)) {
			return docForSymbol(((VncString)ref).toSymbol(), env);
		}
		else {
			// last resort
			final VncString name = (VncString)CoreFunctions.name.apply(VncTinyList.of(ref));
			return docForSymbol(name.toSymbol(), env);
		}
	}
	
	private static VncString docForSymbol(final VncSymbol sym, final Env env) {
		VncVal docVal = SpecialForms.ns.get(sym); // special form?
		if (docVal == null) {
			docVal = env.get(sym); // var?
		}
		
		return Doc.getDoc(docVal);
	}
	
	private static VncString docForCustomType(
			final VncKeyword type, 
			final CustomTypeDefRegistry typeDefRegistry
	) {
		if (typeDefRegistry.existsCustomType(type)) {
			final VncCustomTypeDef typeDef = typeDefRegistry.getCustomType(type);
			final StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("Custom type :%s\n", type.getValue()));
			sb.append("Fields: \n");
			typeDef.getFieldDefs().forEach(f -> sb.append(String.format(
																"   %s :%s\n", 
																f.getName().getValue(),
																f.getType().getValue())));
			if (typeDef.getValidationFn() != null) {
				sb.append(String.format("Validation function: :%s\n", typeDef.getValidationFn().getQualifiedName()));
			}
			
			return new VncString(sb.toString());
		}
		else if (typeDefRegistry.existsWrappedType(type)) {
			final VncWrappingTypeDef typeDef = typeDefRegistry.getWrappedType(type);
			final StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("Custom wrapped type :%s\n", type.getValue()));
			sb.append(String.format("Base type :%s\n", typeDef.getBaseType().getValue()));
			if (typeDef.getValidationFn() != null) {
				sb.append(String.format("Validation function: :%s\n", typeDef.getValidationFn().getQualifiedName()));
			}
			
			return new VncString(sb.toString());
		}
		else if (typeDefRegistry.existsChoiceType(type)) {
			final VncChoiceTypeDef typeDef = typeDefRegistry.getChoiceType(type);
			
			final VncList types = typeDef.typesOnly();
			final VncList values = typeDef.valuesOnly();
			
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format("Custom choice type :%s\n", type.getValue()));
			if (!types.isEmpty()) {
				sb.append("Types: \n");
				typeDef.typesOnly().forEach(v -> sb.append(String.format("   %s\n", v.toString())));
			}
			if (!values.isEmpty()) {
				sb.append("Values: \n");
				typeDef.valuesOnly().forEach(v -> sb.append(String.format("   %s\n", v.toString())));
			}
			
			return new VncString(sb.toString());
		}
		else {
			if (Namespaces.isQualified(type)) {
				throw new VncException(String.format(
						":%s is not a custom type. No documentation available!",
						type.getValue()));
			}
			else {
				throw new VncException(String.format(
						":%s is not a custom type. Please qualify the type with its namespace!",
						type.getValue()));
			}
		}
	}

}
