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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.CustomTypeDefRegistry;
import com.github.jlangch.venice.impl.CustomWrappableTypes;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.custom.VncChoiceTypeDef;
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
					qualifyBaseType(
						Coerce.toVncKeyword(fieldItems.get(ii * 2 + 1)),
						registry), 
					ii));
		}
		

		final VncKeyword qualifiedType = qualifyMainTypeWithCurrentNS(type, "deftype");
		
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
		final VncKeyword qualifiedType = qualifyMainTypeWithCurrentNS(type, "deftype-of");

		final VncKeyword qualifiedBaseType = qualifyBaseType(baseType, registry);

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

	public static VncVal defineCustomChoiceType(
			final VncKeyword type,
			final VncList choiceVals,
			final CustomTypeDefRegistry registry
	) {
		final VncKeyword qualifiedType = qualifyMainTypeWithCurrentNS(type, "deftype-or");

		if (choiceVals.isEmpty()) {
			throw new VncException("There is at least one value required for a choice type."); 
		}
		
		final Set<VncVal> choiceTypes = new HashSet<>();
		final Set<VncVal> choiceValues = new HashSet<>();
		
		for (VncVal v : choiceVals.getList()) {
			if (Types.isVncKeyword(v)) {
				final VncKeyword k = (VncKeyword)v;		
				
				if (Namespaces.isQualified(k)) {
					if (registry.existsType(k)) {
						choiceTypes.add(k);
					}
					else {
						throw new VncException(String.format(
								"The type :%s is not defined.", 
								k.getValue())); 
					}
				}
				else {
					final VncKeyword qualified = qualifyBaseType(k, registry);
					if (registry.existsType(qualified)) {
						choiceTypes.add(qualified);
					}
					else if (Types.isCorePrimitiveType(qualified)) {
						choiceTypes.add(qualified);
					}
					else {
						choiceValues.add(v);
					}
				}

			}
			else {
				choiceValues.add(v);
			}
		}
		
		registry.addChoiceType(
					new VncChoiceTypeDef(
							qualifiedType, 
							VncHashSet.ofAll(choiceTypes), 
							VncHashSet.ofAll(choiceValues)));
		
		
		return qualifiedType;
	}

	public static boolean isCustomType(
			final VncVal val,
			final CustomTypeDefRegistry registry
	) {	
		if (Types.isVncKeyword(val)) {
			final VncKeyword type = Types.qualify(
											Namespaces.getCurrentNS(), 
											(VncKeyword)val);
			return registry.existsType(type);
		}
		else if (Types.isVncCustomType(val)) {
			return true;
		}
		else if (val.isWrapped()) {
			final VncKeyword type = val.getWrappingTypeDef().getType();
			return registry.existsType(type);
		}
		else {
			return false;
		}
	}

	public static VncVal createType(
			final List<VncVal> args, 
			final CustomTypeDefRegistry registry
	) {
		final VncKeyword type = Coerce.toVncKeyword(args.get(0));

		final VncKeyword qualifiedType = Types.qualify(Namespaces.getCurrentNS(), type);

		// custom type
		final VncCustomTypeDef customTypeDef = registry.getCustomType(qualifiedType);
		if (customTypeDef != null) {
			final List<VncVal> typeArgs = args.subList(1, args.size());
			return createCustomType(customTypeDef, typeArgs);
		}
		
		// custom wrapped type
		final VncWrappingTypeDef wrappedTypeDef = registry.getWrappedType(qualifiedType);
		if (wrappedTypeDef != null) {
			return createWrappedType(wrappedTypeDef, args.get(1));
		}
		
		// custom choice type (OR)
		final VncChoiceTypeDef choiceTypeDef = registry.getChoiceType(qualifiedType);
		if (choiceTypeDef != null) {
			return createChoiceType(choiceTypeDef, args.get(1));
		}

		throw new VncException(String.format(
				"The custom type :%s is not defined.", 
				qualifiedType.getValue())); 
	}

	public static VncVal createCustomType(
			final VncCustomTypeDef typeDef, 
			final List<VncVal> typeArgs
	) {
		if (typeDef.count() != typeArgs.size()) {
			throw new VncException(String.format(
					"The custom type :%s requires %d args. %d have been passed", 
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
			final VncVal val
	) {		
		typeDef.validate(val);
		return val.wrap(typeDef, val.getMeta());
	}

	public static VncVal createChoiceType(
			final VncChoiceTypeDef typeDef, 
			final VncVal val
	) {		

		if (typeDef.valuesOnly().contains(val)) {
			return val.wrap(
					new VncWrappingTypeDef(typeDef.getType(), val.getType()), 
					val.getMeta());
		}
		
		final VncKeyword type = val.isWrapped()
				? val.getWrappingTypeDef().getType()
				: val.getType();
		
		if (typeDef.typesOnly().contains(type)) {
			return val.wrap(
					new VncWrappingTypeDef(typeDef.getType(), val.getType()), 
					val.getMeta());
		}
		
		// not a choice type
		if (Types.isCorePrimitiveType(val)) {
			throw new VncException(String.format(
					"The choice type :%s  is not compatible with the value %s", 
					typeDef.getType().getValue(), 
					val.toString(true))); 
		}
		else {
			throw new VncException(String.format(
					"The choice type :%s  is not compatible with a value of type :%s", 
					typeDef.getType().getValue(), 
					type.getValue())); 
		}
	}

	private static void validateTypeCompatibility(
			final VncKeyword type,
			final VncCustomTypeFieldDef fieldDef,
			final VncVal arg
	) {
		VncKeyword argType = Types.getType(arg);
		if (fieldDef.getType().equals(argType)) {
			return;
		}

		if (arg.isWrapped()) {
			argType = arg.getWrappingTypeDef().getType();
			if (fieldDef.getType().equals(argType)) {
				return;
			}
		}
		
		throw new VncException(String.format(
				"The type :%s requires arg %d of type :%s "
						+ "instead of the passed :%s", 
					type.getValue(), 
					fieldDef.getIndex() + 1,
					fieldDef.getType().getValue(),
					argType.getValue())); 
	}
	
	private static VncKeyword qualifyBaseType(
			final VncKeyword type,
			final CustomTypeDefRegistry registry
	) {
		if (Namespaces.isQualified(type)) {
			return type;
		}
		else {
			final VncKeyword type_ =  Namespaces.qualifyKeyword(
										Namespaces.getCurrentNS(), 
										type); 
			
			if (registry.existsType(type_)) {
				return type_;
			}
			else {
				return Namespaces.qualifyKeyword(Namespaces.NS_CORE, type);
			}
		}
	}
	
	public static VncKeyword qualifyMainTypeWithCurrentNS(
			final VncKeyword type,
			final String fnName
	) {
		if (Namespaces.isQualified(type)) {
			// do not allow to hijack another namespace
			final String ns = Namespaces.getNamespace(type.getValue());
			if (!ns.equals(Namespaces.getCurrentNS().getName())) {
				throw new VncException(String.format(
						"%s: the type :%s can only be defined for the current namespace '%s'.",
						fnName,
						type.getValue(),
						Namespaces.getCurrentNS().getValue())); 
			}	
			
			return type;
		}
		else {
			return Namespaces.qualifyKeyword(Namespaces.getCurrentNS(), type);
		}
	}

}
