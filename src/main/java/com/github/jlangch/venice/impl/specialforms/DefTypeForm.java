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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.CustomTypeDefRegistry;
import com.github.jlangch.venice.impl.CustomWrappableTypes;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.custom.VncCustomType;
import com.github.jlangch.venice.impl.types.custom.VncCustomTypeDef;
import com.github.jlangch.venice.impl.types.custom.VncCustomTypeFieldDef;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;



public class DefTypeForm {

	public static VncVal defineCustomType(
			final VncKeyword type,
			final VncVector fields,
			final VncFunction validationFn,
			final CustomTypeDefRegistry registry
	) {											
		if (fields.isEmpty() || ((fields.size() % 2) != 0)) {
			throw new VncException("deftype invalid field definition."); 
		}
		
		final List<VncCustomTypeFieldDef> fieldDefs = new ArrayList<>();
		final List<VncVal> fieldItems = fields.getList();
		for(int ii=0; ii<fieldItems.size()/2; ii++) {		
			fieldDefs.add(
				new VncCustomTypeFieldDef(
					new VncKeyword(
							Coerce.toVncSymbol(fieldItems.get(ii * 2)).getName()), 
					Types.qualify(
						Namespaces.NS_CORE,
						Coerce.toVncKeyword(fieldItems.get(ii * 2 + 1))), 
					ii));
		}
		
		final VncKeyword qualifiedType = Types.qualify(Namespaces.getCurrentNS(), type);
		
		if (registry.existsCustomType(qualifiedType)) {
			throw new VncException(String.format(
					"deftype: the type :%s already exists.", 
					qualifiedType.getValue())); 
		}

		
		registry.addCustomType(new VncCustomTypeDef(qualifiedType, fieldDefs, validationFn));
		
		return qualifiedType;
	}

	public static VncVal defineCustomWrapperType(
			final VncKeyword type,
			final VncKeyword baseType,
			final VncFunction validationFn,
			final CustomTypeDefRegistry registry,
			final CustomWrappableTypes wrappableTypes
	) {
		final VncKeyword qualifiedType = Types.qualify(Namespaces.getCurrentNS(), type);

		final VncKeyword qualifiedBaseType = Types.qualify(Namespaces.NS_CORE, baseType);

		if (!wrappableTypes.isWrappable(qualifiedBaseType)) {
			throw new VncException(String.format(
					"deftype-of: the type :%s can not be wrapped.", 
					baseType.getValue())); 
		}

		if (registry.existsWrappedType(qualifiedType)) {
			throw new VncException(String.format(
					"deftype: the type :%s already exists.", 
					qualifiedType.getValue())); 
		}

		registry.addWrappedType(new VncWrappingTypeDef(qualifiedType, qualifiedBaseType, validationFn));
		
		return qualifiedType;
	}

	public static VncVal createType(
			final List<VncVal> args, 
			final CustomTypeDefRegistry registry
	) {
		final VncKeyword type = Coerce.toVncKeyword(args.get(0));

		final VncKeyword qualifiedType = Types.qualify(Namespaces.getCurrentNS(), type);

		final VncCustomTypeDef customTypeDef = registry.getCustomType(qualifiedType);
		if (customTypeDef != null) {
			final List<VncVal> typeArgs = args.subList(1, args.size());
			return createCustomType(customTypeDef, typeArgs, registry);
		}
		
		final VncWrappingTypeDef wrappedTypeDef = registry.getWrappedType(qualifiedType);
		if (wrappedTypeDef != null) {
			return createWrappedType(wrappedTypeDef, args.get(1), registry);
		}
		
		throw new VncException(String.format(
				"the custom type :%s is not defined.", 
				qualifiedType.getValue())); 
	}

	public static VncVal createCustomType(
			final VncCustomTypeDef typeDef, 
			final List<VncVal> typeArgs,
			final CustomTypeDefRegistry registry
	) {
		if (typeDef.count() != typeArgs.size()) {
			throw new VncException(String.format(
					"deftype: the type :%s requires %d args. %d have been passed", 
					typeDef.getType().getValue(), 
					typeDef.count(),
					typeArgs.size())); 
		}
		
		final Map<VncVal,VncVal> fields = new LinkedHashMap<>();
		
		for(int ii=0; ii<typeArgs.size(); ii++) {
			final VncCustomTypeFieldDef fieldDef = typeDef.getFieldDef(ii);
			final VncVal arg = typeArgs.get(ii);
			
			validateTypeCompatibility(typeDef.getType(), fieldDef, arg);
			
			fields.put(fieldDef.getName(), arg);
		}
		
		final VncMap data = new VncOrderedMap(fields, Constants.Nil);
		
		typeDef.validate(data);
		
		return new VncCustomType(
						typeDef, 
						data, 
						Constants.Nil);
	}

	public static VncVal createWrappedType(
			final VncWrappingTypeDef typeDef, 
			final VncVal val,
			final CustomTypeDefRegistry registry
	) {		
		typeDef.validate(val);
		return val.wrap(typeDef, val.getMeta());
	}

	private static void validateTypeCompatibility(
			final VncKeyword type,
			final VncCustomTypeFieldDef fieldDef,
			final VncVal arg
	) {
		final VncKeyword argType = Types.getType(arg);
		
		if (!fieldDef.getType().equals(argType)) {
			throw new VncException(String.format(
					"deftype: the type :%s requires arg %d of type :%s "
						+ "instead the passed :%s", 
					type.getValue(), 
					fieldDef.getIndex() + 1,
					fieldDef.getType().getValue(),
					argType.getValue())); 
		}
	}
}
