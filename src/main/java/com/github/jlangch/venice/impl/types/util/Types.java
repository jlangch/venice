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
package com.github.jlangch.venice.impl.types.util;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.impl.functions.Numeric;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.IVncJavaObject;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBigInteger;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncConstant;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncJust;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncMultiFunction;
import com.github.jlangch.venice.impl.types.VncNumber;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.VncVolatile;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaSet;
import com.github.jlangch.venice.impl.types.collections.VncLazySeq;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncMutableList;
import com.github.jlangch.venice.impl.types.collections.VncMutableMap;
import com.github.jlangch.venice.impl.types.collections.VncMutableSet;
import com.github.jlangch.venice.impl.types.collections.VncMutableVector;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncQueue;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncSortedMap;
import com.github.jlangch.venice.impl.types.collections.VncSortedSet;
import com.github.jlangch.venice.impl.types.collections.VncStack;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.custom.VncCustomType;

public class Types {

	public static boolean isIDeref(final Object val) {
		return val != null && (val instanceof IDeref);
	}
	
	public static boolean isVncVal(final Object val) {
		return val != null && (val instanceof VncVal);
	}
	
	public static boolean isVncConstant(final VncVal val) {
		return val != null && (val instanceof VncConstant);
	}

	public static boolean isVncBoolean(final VncVal val) {
		return val != null && (val instanceof VncBoolean);
	}

	public static boolean isVncAtom(final VncVal val) {
		return val != null && (val instanceof VncAtom);
	}

	public static boolean isVncVolatile(final VncVal val) {
		return val != null && (val instanceof VncVolatile);
	}
	
	public static boolean isVncThreadLocal(final VncVal val) {
		return val != null && (val instanceof VncThreadLocal);
	}

	public static boolean isVncString(final VncVal val) {
		return val != null && (val instanceof VncString);
	}

	public static boolean isVncChar(final VncVal val) {
		return val != null && (val instanceof VncChar);
	}

	public static boolean isVncKeyword(final VncVal val) {
		return val != null && (val instanceof VncKeyword);
	}

	public static boolean isVncSymbol(final VncVal val) {
		return val != null && (val instanceof VncSymbol);
	}

	public static boolean isVncInteger(final VncVal val) {
		return val != null && (val instanceof VncInteger);
	}

	public static boolean isVncLong(final VncVal val) {
		return val != null && (val instanceof VncLong);
	}

	public static boolean isVncDouble(final VncVal val) {
		return val != null && (val instanceof VncDouble);
	}

	public static boolean isVncBigDecimal(final VncVal val) {
		return val != null && (val instanceof VncBigDecimal);
	}

	public static boolean isVncBigInteger(final VncVal val) {
		return val != null && (val instanceof VncBigInteger);
	}

	public static boolean isVncNumber(final VncVal val) {
		return val != null && (val instanceof VncNumber);
	}

	public static boolean isVncJust(final VncVal val) {
		return val != null && (val instanceof VncJust);
	}

	public static boolean isVncCustomType(final VncVal val) {
		return val != null && (val instanceof VncCustomType);
	}

	public static boolean isVncByteBuffer(final VncVal val) {
		return val != null && (val instanceof VncByteBuffer);
	}

	public static boolean isVncCollection(final VncVal val) {
		return val != null && (val instanceof VncCollection);
	}

	public static boolean isVncSet(final VncVal val) {
		return val != null && (val instanceof VncSet);
	}

	public static boolean isVncHashSet(final VncVal val) {
		return val != null && (val instanceof VncHashSet);
	}

	public static boolean isVncSortedSet(final VncVal val) {
		return val != null && (val instanceof VncSortedSet);
	}

	public static boolean isVncMutableSet(final VncVal val) {
		return val != null && (val instanceof VncMutableSet);
	}

	public static boolean isVncSequence(final VncVal val) {
		return val != null && (val instanceof VncSequence);
	}
	
	public static boolean isVncList(final VncVal val) {
		return val != null && (val instanceof VncList);
	}
	
	public static boolean isVncLazySeq(final VncVal val) {
		return val != null && (val instanceof VncLazySeq);
	}
	
	public static boolean isVncMutableList(final VncVal val) {
		return val != null && (val instanceof VncMutableList);
	}

	public static boolean isVncVector(final VncVal val) {
		return val != null && (val instanceof VncVector);
	}
	
	public static boolean isVncMutableVector(final VncVal val) {
		return val != null && (val instanceof VncMutableVector);
	}

	public static boolean isVncMap(final VncVal val) {
		return val != null && (val instanceof VncMap);
	}

	public static boolean isVncHashMap(final VncVal val) {
		return val != null && (val instanceof VncHashMap);
	}

	public static boolean isVncOrderedMap(final VncVal val) {
		return val != null && (val instanceof VncOrderedMap);
	}

	public static boolean isVncSortedMap(final VncVal val) {
		return val != null && (val instanceof VncSortedMap);
	}

	public static boolean isVncMutableMap(final VncVal val) {
		return val != null && (val instanceof VncMutableMap);
	}

	public static boolean isVncMapEntry(final VncVal val) {
		return val != null && (val instanceof VncMapEntry);
	}

	public static boolean isVncStack(final VncVal val) {
		return val != null && (val instanceof VncStack);
	}

	public static boolean isVncQueue(final VncVal val) {
		return val != null && (val instanceof VncQueue);
	}

	public static boolean isIVncFunction(final VncVal val) {
		return val != null && (val instanceof IVncFunction);
	}

	public static boolean isVncFunction(final VncVal val) {
		return val != null && (val instanceof VncFunction);
	}
	
	public static boolean isVncMultiArityFunction(final VncVal val) {
		return val != null && (val instanceof VncMultiArityFunction);
	}
	
	public static boolean isVncMultiFunction(final VncVal val) {
		return val != null && (val instanceof VncMultiFunction);
	}

	public static boolean isVncMacro(final VncVal val) {
		return val != null && isVncFunction(val) && ((VncFunction)val).isMacro();
	}

	public static boolean isVncJavaObject(final VncVal val) {
		return val != null && (val instanceof VncJavaObject);
	}

	public static boolean isVncJavaObject(final VncVal val, final Class<?> type) {
		return val != null 
				&& (val instanceof VncJavaObject) 
				&& type.isAssignableFrom(((VncJavaObject)val).getDelegate().getClass());
	}

	public static boolean isVncJavaList(final VncVal val) {
		return val != null && (val instanceof VncJavaList);
	}

	public static boolean isVncJavaSet(final VncVal val) {
		return val != null && (val instanceof VncJavaSet);
	}

	public static boolean isVncJavaMap(final VncVal val) {
		return val != null && (val instanceof VncJavaMap);
	}

	public static VncKeyword getType(final VncVal val) {
		return val.getType();
	}

	public static VncKeyword getSupertype(final VncVal val) {
		return val.getSupertype();
	}

	public static List<VncKeyword> getSupertypes(final VncVal val) {
		return val.getAllSupertypes();
	}

	public static boolean isInstanceOf(final VncKeyword type, final VncVal val) {
		final String clazz = type.getValue();
		
		switch(clazz) {
			case "core/nil":			return val == Nil;

			case "core/char":			return Types.isVncChar(val);
			case "core/string":			return Types.isVncString(val);
			case "core/boolean":		return Types.isVncBoolean(val);
			case "core/number":			return Types.isVncNumber(val);
			case "core/integer":		return Types.isVncInteger(val);
			case "core/long":			return Types.isVncLong(val);
			case "core/double":			return Types.isVncDouble(val);
			case "core/decimal":		return Types.isVncBigDecimal(val);
			case "core/bigint":			return Types.isVncBigInteger(val);
			case "core/bytebuf":		return Types.isVncByteBuffer(val);

			case "core/symbol":			return Types.isVncSymbol(val);
			case "core/keyword":		return Types.isVncKeyword(val);

			case "core/atom":			return Types.isVncAtom(val);
			case "core/volatile":		return Types.isVncVolatile(val);
			case "core/thread-local":	return Types.isVncThreadLocal(val);

			case "core/java-object":	return Types.isVncJavaObject(val);

			case "core/just":			return Types.isVncJust(val);
			case "core/function":		return Types.isVncFunction(val);
			case "core/macro":			return Types.isVncMacro(val);

			case "core/collection":		return Types.isVncCollection(val);
			
			case "core/sequence":		return Types.isVncSequence(val);
			case "core/vector":			return Types.isVncVector(val);
			case "core/list":			return Types.isVncList(val);
			case "core/mutable-list":	return Types.isVncMutableList(val);
			
			case "core/set":			return Types.isVncSet(val);
			case "core/hash-set":		return Types.isVncHashSet(val);
			case "core/sorted-set":		return Types.isVncSortedSet(val);
			case "core/mutable-set":	return Types.isVncMutableSet(val);
			
			case "core/map":			return Types.isVncMap(val);
			case "core/hash-map":		return Types.isVncHashMap(val);
			case "core/ordered-map":	return Types.isVncOrderedMap(val);
			case "core/sorted-map":		return Types.isVncSortedMap(val);
			case "core/mutable-map":	return Types.isVncMutableMap(val);
			case "core/map-entry":		return Types.isVncMapEntry(val);

			case "core/stack":			return Types.isVncStack(val);
			case "core/queue":			return Types.isVncQueue(val);

			case "core/custom-type":	return Types.isVncCustomType(val);

			default:
				try {
					if (Types.isVncCustomType(val)) {
						if (((VncCustomType)val).getType().equals(type)) {
							return true;
						}
						else if (((VncCustomType)val).getAllSupertypes().contains(type)) {
							return true;
						}
						else if (val.isWrapped()) {
							final VncKeyword wrappingType = val.getWrappingTypeDef().getType();
							return type.equals(wrappingType);
						}
						else {
							return false;
						}
					}
					else if (val.isWrapped()) {
						final VncKeyword wrappingType = val.getWrappingTypeDef().getType();
						return type.equals(wrappingType);
					}
					else if (Types.isVncJavaObject(val)) {
						return Class.forName(clazz)
									.isAssignableFrom(((IVncJavaObject)val).getDelegate().getClass());
					}
					else if (Types.isVncJavaSet(val)) {
						return Class.forName(clazz)
									.isAssignableFrom(((IVncJavaObject)val).getDelegate().getClass());
					}
					else if (Types.isVncJavaList(val)) {
						return Class.forName(clazz)
									.isAssignableFrom(((IVncJavaObject)val).getDelegate().getClass());
					}
					else if (Types.isVncJavaMap(val)) {
						return Class.forName(clazz)
									.isAssignableFrom(((IVncJavaObject)val).getDelegate().getClass());
					}
					else {
						return false;
					}	
				}
				catch(Exception ex) {
					return false;
				}
		}		
	}

	public static boolean isCorePrimitiveType(final VncVal val) {
		final String type = Types.isVncKeyword(val)
								? ((VncKeyword)val).getValue()
								: val.getType().getValue();
		
		switch(type) {
			case "core/nil":		return true;
			case "core/boolean":	return true;
			case "core/long":		return true;
			case "core/integer":	return true;
			case "core/double":		return true;
			case "core/decimal":	return true;
			case "core/string":		return true;
			case "core/char":		return true;
			case "core/symbol":		return true;
			case "core/keyword":	return true;
			case "core/function":	return true;
			default:				return false;
		}		
	}

	public static boolean _equal_Q(VncVal a, VncVal b) {
		if (Types.isVncNumber(a) && Types.isVncNumber(b)) {
			return VncBoolean.isTrue(Numeric.equ(a, b));
		}
		else if (Types.isVncString(a) && Types.isVncChar(b)) {
			return ((VncString)a).getValue().equals(((VncChar)b).getValue().toString());
		}
		else if (Types.isVncChar(a) && Types.isVncString(b)) {
			return ((VncChar)a).getValue().toString().equals(((VncString)b).getValue());
		}
		else {
			return _equal_strict_Q(a, b);
		}
	}
	
	public static boolean _equal_strict_Q(VncVal a, VncVal b) {
		final Class<?> ota = a.getClass(), otb = b.getClass();
		if (!((ota == otb) 
				|| (a instanceof VncString && b instanceof VncString) 
				|| (a instanceof VncList && b instanceof VncList))
		) {
			return false;
		} 
		else {
			if (a instanceof VncConstant) {
				return ((VncConstant)a) == ((VncConstant)b);
			} 
			else if (a instanceof VncBoolean) {
				return ((VncBoolean)a).getValue() == ((VncBoolean)b).getValue();
			} 
			else if (a instanceof VncLong) {
				return ((VncLong)a).getValue().equals(((VncLong)b).getValue());
			} 
			else if (a instanceof VncInteger) {
				return ((VncInteger)a).getValue().equals(((VncInteger)b).getValue());
			} 
			else if (a instanceof VncDouble) {
				return ((VncDouble)a).getValue().equals(((VncDouble)b).getValue());
			} 
			else if (a instanceof VncBigDecimal) {
				return ((VncBigDecimal)a).getValue().equals(((VncBigDecimal)b).getValue());
			} 
			else if (a instanceof VncSymbol) {
				return ((VncSymbol)a).getName().equals(((VncSymbol)b).getName());
			} 
			else if (a instanceof VncString) {
				// allow true: (== \"aa\" \"aa\" ), (== :aa :aa ), (== :aa \"aa\" )
				return ((VncString)a).getValue().equals(((VncString)b).getValue());
			} 
			else if (a instanceof VncSequence) {
				if (((VncSequence)a).size() != ((VncSequence)b).size()) {
					return false;
				}
				for (Integer i=0; i<((VncSequence)a).size(); i++) {
					if (!_equal_Q(((VncSequence)a).nth(i), ((VncSequence)b).nth(i))) {
						return false;
					}
				}
				return true;
			} 
			else if (a instanceof VncHashSet) {
				if (((VncHashSet)a).size() != ((VncHashSet)b).size()) {
					return false;
				}
				return ((VncHashSet)a).stream().allMatch(v -> ((VncHashSet)b).contains(v));
			} 
			else if (a instanceof VncMap) {
				if (((VncMap)a).getJavaMap().size() != ((VncMap)b).getJavaMap().size()) {
					return false;
				}
				final VncMap mhm = ((VncMap)a);
				final Map<VncVal,VncVal> hm = mhm.getJavaMap();
				for (VncVal k : hm.keySet()) {
					final VncVal valA = ((VncMap)a).getJavaMap().get(k);
					final VncVal valB = ((VncMap)b).getJavaMap().get(k);
					if (valA == null && valB == null) {
						return true;
					}
					else if (valA != null && valB != null) {
						if (! _equal_Q(valA,valB)) {
							return false;
						}
					}
					else {
						return false;
					}
				}
				return true;
			} 
			else if (a instanceof VncJavaList) {
				return a.equals(b);
			}
			else if (a instanceof VncJavaSet) {
				return a.equals(b);
			}
			else if (a instanceof VncJavaMap) {
				return a.equals(b);
			}
			else {
				return a.equals(b);
			}
		}
	}

	public static boolean isJavaTypeReference(final VncKeyword keyword) {
		final String name = keyword.getValue();
		if (name.indexOf('/') < 0) {
			return name.charAt(name.length()-1) == '.'
					? false  //custom type builder:  person.
					: name.indexOf('.') >= 0;
		}
		else {
			return false;  // a.b.c/list-accounts
		}
	}
		
	
	public static final VncKeyword ANY = new VncKeyword("core/any");
	
}
