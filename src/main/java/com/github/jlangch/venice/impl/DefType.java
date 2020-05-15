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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncCustomType;
import com.github.jlangch.venice.impl.types.VncCustomTypeDef;
import com.github.jlangch.venice.impl.types.VncCustomTypeFieldDef;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;



public class DefType {

	public static VncVal defineCustomType(
			final VncList args, 
			final CustomTypeDefRegistry registry
	) {
		final VncKeyword type = Coerce.toVncKeyword(args.first());
		final VncVector fields = Coerce.toVncVector(args.second());
		
		if (fields.isEmpty() || ((fields.size() % 2) != 0)) {
			throw new VncException("deftype invalid field definition."); 
		}
		
		final List<VncCustomTypeFieldDef> fieldDefs = new ArrayList<>();
		final List<VncVal> fieldItems = fields.getList();
		for(int ii=0; ii<fieldItems.size()/2; ii++) {
			fieldDefs.add(
				new VncCustomTypeFieldDef(
						new VncKeyword(Coerce.toVncSymbol(fieldItems.get(ii * 2)).getName()),
						Coerce.toVncKeyword(fieldItems.get(ii * 2 + 1)),
						ii));
		}
		
		final VncKeyword qualifiedType = qualify(type);
		
		final VncCustomTypeDef typeDef = new VncCustomTypeDef(qualifiedType, fieldDefs);
		
		if (registry.exists(qualifiedType)) {
			throw new VncException(String.format(
					"deftype: the type :%s already exists.", qualifiedType.getValue())); 
		}
		
		registry.add(typeDef);
		
		return qualifiedType;
	}
	
	public static VncVal createCustomType(
			final VncList args, 
			final CustomTypeDefRegistry registry
	) {
		final VncKeyword type = Coerce.toVncKeyword(args.first());
		final List<VncVal> typeArgs = Coerce.toVncSequence(args.rest()).getList();

		final VncKeyword qualifiedType = qualify(type);

		final VncCustomTypeDef typeDef = registry.get(qualifiedType);
		
		if (typeDef == null) {
			throw new VncException(String.format(
					"deftype: the type :%s is not defined exists.", qualifiedType.getValue())); 
		}
		
		if (typeDef.count() != typeArgs.size()) {
			throw new VncException(String.format(
					"deftype: the type :%s requires %d args. %d have been passed", 
					qualifiedType.getValue(), 
					typeDef.count(),
					typeArgs.size())); 
		}
		
		// TODO
		// check types 
		
		final Map<VncVal,VncVal> fields = new LinkedHashMap<>();
		
		for(int ii=0; ii<typeArgs.size(); ii++) {
			fields.put(typeDef.getFieldDef(ii).getName(), typeArgs.get(ii));
		}
		
		return new VncCustomType(
						typeDef, 
						new VncOrderedMap(fields, Constants.Nil), 
						Constants.Nil);
	}

	
	private static VncKeyword qualify(final VncKeyword keyword) {
		return Namespaces.isQualified(keyword)
					? keyword
					: Namespaces.qualifyKeyword(
							Namespaces.getCurrentNS(), 
							keyword);	
	}
}
