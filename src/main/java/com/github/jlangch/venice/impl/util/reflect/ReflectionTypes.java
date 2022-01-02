/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.util.reflect;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Type helpers
 */
public class ReflectionTypes {

	public static boolean isBaseType(final Class<?> type) {
		return  isStringType(type)
					|| isCharType(type)
					|| isBooleanType(type)
					|| isByteType(type)
					|| isIntegerType(type)
					|| isFloatType(type);
	}

	public static boolean isEnumType(final Class<?> type) {
		return type.isEnum();
	}

	public static boolean isArrayType(final Class<?> type) {
		return type.isArray();
	}

	public static boolean isStringType(final Class<?> type) {
		return String.class == type;
	}

	public static boolean isCharType(final Class<?> type) {
		return isPrimitiveCharType(type) || isObjectCharType(type);
	}

	public static boolean isPrimitiveCharType(final Class<?> type) {
		return type == char.class;
	}

	public static boolean isObjectCharType(final Class<?> type) {
		return type == Character.class;
	}
	
	public static boolean isByteType(final Class<?> type) {
		return isPrimitiveByteType(type) || isObjectByteType(type);
	}

	public static boolean isPrimitiveByteType(final Class<?> type) {
		return type == byte.class;
	}

	public static boolean isObjectByteType(final Class<?> type) {
		return type == Byte.class;
	}

	public static boolean isDateType(final Class<?> type) {
		return type == Date.class;
	}

	public static boolean isBigIntegerType(final Class<?> type) {
		return type == BigInteger.class;
	}

	public static boolean isBigDecimalType(final Class<?> type) {
		return type == BigDecimal.class;
	}

	public static boolean isNumberType(final Class<?> type) {
		return isIntegerType(type) || isFloatType(type);
	}

	public static boolean isIntegerType(final Class<?> type) {
		return isPrimitiveIntegerType(type) || isObjectIntegerType(type);
	}

	public static boolean isBooleanType(final Class<?> type) {
		return isPrimitiveBooleanType(type) || isObjectBooleanType(type);
	}

	public static boolean isPrimitiveBooleanType(final Class<?> type) {
		return type == boolean.class;
	}

	public static boolean isObjectBooleanType(final Class<?> type) {
		return type == Boolean.class;
	}

	public static boolean isPrimitiveIntegerType(final Class<?> type) {
		return     type == short.class 
				|| type == int.class 
				|| type == long.class;
	}

	public static boolean isObjectIntegerType(final Class<?> type) {
		return     type == Short.class
				|| type == Integer.class
				|| type == Long.class
				|| type == BigInteger.class;
	}

	public static boolean isFloatType(final Class<?> type) {
		return  isPrimitiveFloatType(type) || isObjectFloatType(type);
	}

	public static boolean isPrimitiveFloatType(final Class<?> type) {
		return     type == float.class
				|| type == double.class;
	}

	public static boolean isObjectFloatType(final Class<?> type) {
		return     type == Float.class 
				|| type == Double.class 
				|| type == BigDecimal.class;
	}

	public static boolean isAbstractType(final Class<?> type) {
		return Modifier.isAbstract(type.getModifiers());
	}

	public static List<String> enumValues(final Class<? extends Enum<?>> clazz) {
		final List<String> values = new ArrayList<>();
		for(Enum<?> e : clazz.getEnumConstants()) {
			values.add(e.name());
		}
		return values;
	}

	public static boolean isCollection(final Class<?> type) {
		return Collection.class.isAssignableFrom(type);
	}

	public static boolean isList(final Class<?> type) {
		return List.class.isAssignableFrom(type);
	}

	public static boolean isList(final ParameterizedType type) {
		return isList((Class<?>)type.getRawType());
	}

	public static boolean isSet(final Class<?> type) {
		return Set.class.isAssignableFrom(type);
	}

	public static boolean isSet(final ParameterizedType type) {
		return isSet((Class<?>)type.getRawType());
	}

	public static boolean isListOrSet(final Class<?> type) {
		return isList(type) || isSet(type);
	}

	public static boolean isListOrSet(final ParameterizedType type) {
		return isList(type) || isSet(type);
	}

	public static boolean isMap(final Class<?> type) {
		return Map.class.isAssignableFrom(type);		
	}

	public static boolean isMap(final ParameterizedType type) {
		return isMap((Class<?>)type.getRawType());
	}
	
	public static boolean isParameterizedType(final Type type) {
		return (type instanceof ParameterizedType);
	}

	public static Type[] getParameterizedTypeArguments(final Type type) {
		if (!isParameterizedType(type)) {
			throw new RuntimeException("Not a ParameterizedType");
		}
		
		return ((ParameterizedType)type).getActualTypeArguments();
	}

	public static boolean isParameterizedTypeWithArgumentTypes(
			final Type type, 
			final Class<?> argType1, 
			final Class<?> argType2
	) {
		if (!isParameterizedType(type)) {
			return false;
		}
		
		final Type[] elementTypes = getParameterizedTypeArguments(type);

		if (elementTypes.length != 2) {
			return false;
		}
		
		for(int ii=0; ii<elementTypes.length; ii++) {
			if (isParameterizedType(elementTypes[ii])) {
				return false;
			}
		}
		
		return argType1 == (Class<?>)elementTypes[0] 
					&& argType2 == (Class<?>)elementTypes[1];
	}

	public static List<String> extractGenericTypes(final Type type) {
		if (!isParameterizedType(type)) {
			throw new IllegalArgumentException("Not a parameterized type");
		}
		
		final List<String> genericTypes = new ArrayList<String>();

		final Type[] actualArguments = ((ParameterizedType)type).getActualTypeArguments();

		for (int ii=0; ii<actualArguments.length; ii++) {
			final Type actType = actualArguments[ii];
			
			if (actType instanceof ParameterizedType) {
				genericTypes.addAll(
						extractGenericTypes((ParameterizedType)actType));
			}
			else {
				final String stdType = actType.toString();
				
				if (stdType.startsWith("class ")){
					genericTypes.add(stdType.substring("class ".length()));	
				} 
				else if (stdType.startsWith("interface ")){
					genericTypes.add(stdType.substring("interface ".length()));
				}
			}
		}

		return genericTypes;
	}
}
