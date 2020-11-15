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
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.ReadEvalFunction;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.custom.CustomWrappableTypes;
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
			final ReadEvalFunction interpreter,
			final Env env
	) {											
		if (fields.isEmpty() || ((fields.size() % 2) != 0)) {
			throw new VncException("deftype invalid field definition."); 
		}
		
		final List<VncCustomTypeFieldDef> fieldDefs = new ArrayList<>();
		final List<VncVal> fieldItems = fields.getJavaList();
		for(int ii=0; ii<fieldItems.size()/2; ii++) {		
			fieldDefs.add(
				new VncCustomTypeFieldDef(
					new VncKeyword(
						Coerce.toVncSymbol(fieldItems.get(ii * 2)).getName()), 
					qualifyBaseType(
						Coerce.toVncKeyword(fieldItems.get(ii * 2 + 1)),
						env), 
					new VncInteger(ii)));
		}
		

		final VncKeyword qualifiedType = qualifyMainTypeWithCurrentNS(type, "deftype");
		
		validateCustomTypeName(qualifiedType);

		if (isCustomType(qualifiedType, env)) {
			throw new VncException(String.format(
					"deftype: the type %s already exists.", 
					qualifiedType.toString())); 
		}

		
		final VncCustomTypeDef typeDef = new VncCustomTypeDef(qualifiedType, fieldDefs, validationFn);
		
		env.setGlobal(new Var(qualifiedType.toSymbol(), typeDef));
		
		// create builder and type check function for the custom type
		createBuildAndCheckFn(qualifiedType.toSymbol().getName(), fieldDefs.size(), interpreter, env);
		
		return qualifiedType;
	}

	public static VncVal defineCustomWrapperType(
			final VncKeyword type,
			final VncKeyword baseType,
			final VncFunction validationFn,
			final ReadEvalFunction interpreter,
			final Env env,
			final CustomWrappableTypes wrappableTypes
	) {
		final VncKeyword qualifiedType = qualifyMainTypeWithCurrentNS(type, "deftype-of");

		validateCustomTypeName(qualifiedType);

		final VncKeyword qualifiedBaseType = qualifyBaseType(baseType, env);

		if (!wrappableTypes.isWrappable(qualifiedBaseType)) {
			throw new VncException(String.format(
					"deftype-of: the type %s can not be wrapped.", 
					baseType.toString())); 
		}

		if (isCustomType(qualifiedType, env)) {
			throw new VncException(String.format(
					"deftype: the type %s already exists.", 
					qualifiedType.toString())); 
		}
		
		
		final VncWrappingTypeDef typeDef = new VncWrappingTypeDef(qualifiedType, qualifiedBaseType, validationFn);

		env.setGlobal(new Var(qualifiedType.toSymbol(), typeDef));

		// create builder and type check function for the custom type
		createBuildAndCheckFn(qualifiedType.toSymbol().getName(), 1, interpreter, env);

		return qualifiedType;
	}

	public static VncVal defineCustomChoiceType(
			final VncKeyword type,
			final VncList choiceVals,
			final ReadEvalFunction interpreter,
			final Env env
	) {
		final VncKeyword qualifiedType = qualifyMainTypeWithCurrentNS(type, "deftype-or");

		validateCustomTypeName(qualifiedType);

		if (choiceVals.isEmpty()) {
			throw new VncException("There is at least one value required for a choice type."); 
		}

		if (isCustomType(qualifiedType, env)) {
			throw new VncException(String.format(
					"deftype-or: the type %s already exists.", 
					qualifiedType.toString())); 
		}

		final Set<VncVal> choiceTypes = new HashSet<>();
		final Set<VncVal> choiceValues = new HashSet<>();
		
		for (VncVal v : choiceVals) {
			if (Types.isVncKeyword(v)) {
				final VncKeyword k = (VncKeyword)v;		
				
				if (k.hasNamespace()) {
					if (isCustomType(k, env)) {
						choiceTypes.add(k);
					}
					else {
						throw new VncException(String.format(
								"The type %s is not defined.", 
								k.toString())); 
					}
				}
				else {
					final VncKeyword qualified = qualifyBaseType(k, env);
					if (isCustomType(qualified, env)) {
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
		
		final VncChoiceTypeDef typeDef = new VncChoiceTypeDef(
												qualifiedType, 
												VncHashSet.ofAll(choiceTypes), 
												VncHashSet.ofAll(choiceValues));

		env.setGlobal(new Var(qualifiedType.toSymbol(), typeDef));

		// create builder and type check function for the custom type
		createBuildAndCheckFn(qualifiedType.toSymbol().getName(), 1, interpreter, env);

		return qualifiedType;
	}

	public static boolean isCustomType(
			final VncKeyword typeDef,
			final Env env
	) {	
		return env.getGlobalOrNull(typeDef.toSymbol()) != null;
	}

	public static boolean isCustomType(final VncVal val, final Env env) {	
		if (Types.isVncKeyword(val)) {
			final VncKeyword type = (VncKeyword)val;
			final VncKeyword qualifiedType = type.hasNamespace() 
												? type 
												: type.withNamespace(Namespaces.getCurrentNS());
					
			return env.getGlobalOrNull(qualifiedType.toSymbol()) != null;
		}
		else if (Types.isVncCustomType(val)) {
			return true;
		}
		else if (val.isWrapped()) {
			final VncKeyword type = val.getWrappingTypeDef().getType();

			return env.getGlobalOrNull(type.toSymbol()) != null;
		}
		else {
			return false;
		}
	}

	public static VncVal createType(final List<VncVal> args, final Env env) {
		final VncKeyword type = Coerce.toVncKeyword(args.get(0));
		final VncKeyword qualifiedType = type.hasNamespace() 
											? type 
											: type.withNamespace(Namespaces.getCurrentNS());

		final VncVal typeDef = env.getGlobalOrNull(qualifiedType.toSymbol());
		if (typeDef == null) {
			throw new VncException(String.format(
					"The custom type %s is not defined.", 
					qualifiedType.toString())); 
		}
		else if (typeDef instanceof VncCustomTypeDef) {
			final List<VncVal> typeArgs = args.subList(1, args.size());
			return createCustomType((VncCustomTypeDef)typeDef, typeArgs);
		}
		else if (typeDef instanceof VncWrappingTypeDef) {
			return createWrappedType((VncWrappingTypeDef)typeDef, args.get(1));
		}
		else if (typeDef instanceof VncChoiceTypeDef) {
			return createChoiceType((VncChoiceTypeDef)typeDef, args.get(1));
		}
		else {
			throw new VncException(String.format(
					"The type %s is not a custom type.", 
					qualifiedType.toString())); 
		}
	}

	public static VncCustomType createCustomType(
			final VncCustomTypeDef typeDef, 
			final List<VncVal> typeArgs
	) {
		if (typeDef.count() != typeArgs.size()) {
			throw new VncException(String.format(
					"The custom type %s requires %d args. %d have been passed", 
					typeDef.getType().toString(), 
					typeDef.count(),
					typeArgs.size())); 
		}
		
		final Map<VncVal,VncVal> fields = new LinkedHashMap<>();
		
		for(int ii=0; ii<typeDef.count(); ii++) {
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

	public static VncCustomType createCustomType(
			final VncCustomTypeDef typeDef, 
			final VncMap fields
	) {
		if (typeDef.count() != fields.size()) {
			throw new VncException(String.format(
					"The custom type %s requires %d args. %d have been passed", 
					typeDef.getType().toString(), 
					typeDef.count(),
					fields.size())); 
		}
		
		final Map<VncVal,VncVal> fieldsNew = new LinkedHashMap<>();
		
		for(int ii=0; ii<typeDef.count(); ii++) {
			final VncCustomTypeFieldDef fieldDef = typeDef.getFieldDef(ii);
			
			final VncVal arg = fields.get(fieldDef.getName());
			
			validateTypeCompatibility(typeDef.getType(), fieldDef, arg);
			
			fieldsNew.put(fieldDef.getName(), arg);
		}
		
		final VncMap data = new VncOrderedMap(fieldsNew, Constants.Nil);
		
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
		validateTypeCompatibility(typeDef, val);

		typeDef.validate(val);
		return val.wrap(typeDef, val.getMeta());
	}

	public static VncVal createChoiceType(
			final VncChoiceTypeDef typeDef, 
			final VncVal val
	) {
		if (typeDef.valuesOnly().contains(val)) {
			if (val == Constants.Nil) {
				return val;
			}
			else {
				return val.wrap(
						new VncWrappingTypeDef(typeDef.getType(), val.getType()), 
						val.getMeta());
			}
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
					"The choice type %s is not compatible with the value %s", 
					typeDef.getType().toString(), 
					val.toString(true))); 
		}
		else {
			throw new VncException(String.format(
					"The choice type %s is not compatible with a value of type %s", 
					typeDef.getType().toString(), 
					type.toString())); 
		}
	}

	private static void validateTypeCompatibility(
			final VncKeyword type,
			final VncCustomTypeFieldDef fieldDef,
			final VncVal arg
	) {
		if (Types.ANY.equals(fieldDef.getType())) {
			return;
		}

		final VncKeyword argType = Types.getType(arg);
		if (Types.isInstanceOf(fieldDef.getType(), arg)) {
			return;
		}
		
		throw new VncException(String.format(
				"The type %s requires arg %d of type %s "
						+ "instead of the passed %s", 
					type.toString(), 
					fieldDef.getIndex().getValue() + 1,
					fieldDef.getType().toString(),
					argType.toString())); 
	}

	private static void validateTypeCompatibility(
			final VncWrappingTypeDef typeDef,
			final VncVal arg
	) {
		if (Types.ANY.equals(typeDef.getBaseType())) {
			return;
		}

		if (Types.isInstanceOf(typeDef.getBaseType(), arg)) {
			return;
		}
		
		throw new VncException(String.format(
				"The type %s requires an arg of type %s "
						+ "instead of the passed %s", 
					typeDef.getType().toString(), 
					typeDef.getBaseType().toString(),
					arg.getType().toString())); 
	}

	private static VncKeyword qualifyBaseType(
			final VncKeyword type,
			final Env env
	) {
		if (type.hasNamespace()) {
			return type;
		}
		else {
			final VncKeyword type_ = type.withNamespace(Namespaces.getCurrentNS()); 
			
			if (isCustomType(type_, env)) {
				return type_;
			}
			else {
				return type.withNamespace(Namespaces.NS_CORE);
			}
		}
	}
	
	public static VncKeyword qualifyMainTypeWithCurrentNS(
			final VncKeyword type,
			final String fnName
	) {
		if (type.hasNamespace()) {
			// do not allow to hijack another namespace
			final String ns = type.getNamespace();
			if (!ns.equals(Namespaces.getCurrentNS().getName())) {
				throw new VncException(String.format(
						"function %s: Invalid use of namespace. "
							+ "The type '%s' can only be defined for the current namespace '%s'.",
						fnName,
						type.getSimpleName(),
						Namespaces.getCurrentNS().toString())); 
			}	
			
			return type;
		}
		else {
			return type.withNamespace(Namespaces.getCurrentNS());
		}
	}


	private static void createBuildAndCheckFn(
			final String qualifiedTypeName,
			final int builderNumArgs,
			final ReadEvalFunction interpreter,
			final Env env
	) {
		final String typeBuildFn = createBuildTypeFn(qualifiedTypeName, builderNumArgs);
		final String typeCheckFn = createCheckTypeFn(qualifiedTypeName);
		
		interpreter.eval(typeBuildFn, "custom-types", env);
		interpreter.eval(typeCheckFn, "custom-types", env);
	}

	private static String createBuildTypeFn(final String qualifiedTypeName, final int builderNumArgs) {
		// Function args: [x0, x1, x2, ...]
		final StringBuilder args = new StringBuilder();
		args.append("x0");
		for(int ii=1; ii<builderNumArgs; ii++) {
			args.append(" ").append("x").append(ii);
		}
		return String.format(
				"(defn %s. [%s] (.: :%s %s))", 
				qualifiedTypeName, 
				args,
				qualifiedTypeName,
				args);
	}

	private static String createCheckTypeFn(final String qualifiedTypeName) {
		return String.format(
				"(defn %s? [v] (= :%s (type v)))", 
				qualifiedTypeName, 
				qualifiedTypeName);
	}
	
	private static void validateCustomTypeName(final VncKeyword type) {
		final String name = type.getValue();
		
		if (name.endsWith(".")) {
			throw new VncException(String.format(
					"A custom type %s name must not end with '.'.", 
					name)); 
		}
	}

}
