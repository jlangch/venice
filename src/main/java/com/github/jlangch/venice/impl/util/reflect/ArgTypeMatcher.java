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
package com.github.jlangch.venice.impl.util.reflect;

import java.nio.ByteBuffer;


public class ArgTypeMatcher {
	
	public static boolean isCongruent(
			final Class<?>[] params, 
			final Object[] args,
			final boolean exactMatch,
			final boolean varargs
	) {
		if (args == null) {
			return params.length == 0;
		}
		else if (params.length == args.length) {
			for (int ii=0; ii<params.length; ii++) {
				final Object arg = args[ii];
				final Class<?> argType = (arg == null) ? null : arg.getClass();
				final Class<?> paramType = params[ii];
				
				if (ReflectionTypes.isEnumType(paramType)) {
					if (arg != null) {
						if (arg instanceof String) {
							final ScopedEnumValue scopedEnum = new ScopedEnumValue((String)arg);
							if (scopedEnum.isScoped()) {
								if (!scopedEnum.isCompatible(paramType)) {
									return false; // enum type not matching
								}
							}
							else {
								// non scoped enum name -> test compatibility while boxing
							}
						}
						else {
							// an arg other than string can not be converted to an enum value
							return false;
						}
					}
				}
				else {
					final boolean match = exactMatch 
											? paramArgTypeMatchExact(paramType, argType)
											: paramArgTypeMatch(paramType, argType);
					if (!match) {
						return false;
					}
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	private static boolean paramArgTypeMatchExact(final Class<?> paramType, final Class<?> argType) {
		if (argType == null) {
			// an arg of value <null> can be assigned to any object param type
			return !paramType.isPrimitive(); 
		}
		
		if (paramType == argType || paramType.isAssignableFrom(argType)) {
			return true;
		}
		
		if (paramType == byte.class 
				|| paramType == Byte.class 
				|| paramType == short.class 
				|| paramType == Short.class 
				|| paramType == int.class 
				|| paramType == Integer.class 
				|| paramType == long.class
				|| paramType == Long.class
		) {
			return argType == Byte.class 
					|| argType == Short.class 
					|| argType == Integer.class 
					|| argType == Long.class;
		}
		else if (paramType == float.class 
					|| paramType == Float.class
					|| paramType == double.class
					|| paramType == Double.class
		) {
			return argType == Float.class 
					|| argType == Double.class;
		}
		else if (paramType == char.class || paramType == Character.class) {
			return argType == Character.class;
		}
		else if (paramType == boolean.class || paramType == Boolean.class) {
			return argType == Boolean.class;
		}
		
		return false;
	}

	private static boolean paramArgTypeMatch(final Class<?> paramType, final Class<?> argType) {
		if (argType == null) {
			// an arg of value <null> can be assigned to any object param type
			return !paramType.isPrimitive();
		}
		
		if (paramType == argType || paramType.isAssignableFrom(argType)) {
			return true;
		}
		
		if (paramType == byte.class 
				|| paramType == Byte.class 
				|| paramType == short.class 
				|| paramType == Short.class 
				|| paramType == int.class 
				|| paramType == Integer.class 
				|| paramType == long.class 
				|| paramType == Long.class 
				|| paramType == float.class 
				|| paramType == Float.class 
				|| paramType == double.class
				|| paramType == Double.class 
		) {
			return argType == Byte.class 
					|| argType == Short.class 
					|| argType == Integer.class 
					|| argType == Long.class 
					|| argType == Float.class 
					|| argType == Double.class;
		}
		else if (paramType == char.class || paramType == Character.class) {
			return argType == Character.class;
		}
		else if (paramType == boolean.class || paramType == Boolean.class) {
			return argType == Boolean.class;
		}
		else if (ReflectionTypes.isArrayType(paramType)) {
			final Class<?> paramComponentType = paramType.getComponentType();					
			if (paramComponentType == byte.class) {
				if (argType == String.class) {
					return true;
				}
				else if (ByteBuffer.class.isAssignableFrom(argType)) {
					return true;
				}
			}
			else if (paramComponentType == char.class) {
				if (argType == String.class) {
					return true;
				}
			}
			return false;
		}
		
		return false;
	}
}
