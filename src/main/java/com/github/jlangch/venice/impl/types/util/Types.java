/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.util.Map;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.IVncJavaObject;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncConstant;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncMultiFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMutableMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncSortedMap;
import com.github.jlangch.venice.impl.types.collections.VncSortedSet;
import com.github.jlangch.venice.impl.types.collections.VncStack;
import com.github.jlangch.venice.impl.types.collections.VncVector;

public class Types {

	public static boolean isVncVal(final Object val) {
		return val != null && (val instanceof VncVal);
	}
	
	public static boolean isVncConstant(final VncVal val) {
		return val != null && (val instanceof VncConstant);
	}

	public static boolean isVncAtom(final VncVal val) {
		return val != null && (val instanceof VncAtom);
	}
	
	public static boolean isVncThreadLocal(final VncVal val) {
		return val != null && (val instanceof VncThreadLocal);
	}

	public static boolean isVncString(final VncVal val) {
		return val != null && (val instanceof VncString);
	}

	public static boolean isVncKeyword(final VncVal val) {
		return val != null && (val instanceof VncKeyword);
	}

	public static boolean isVncSymbol(final VncVal val) {
		return val != null && (val instanceof VncSymbol);
	}

	public static boolean isVncBoolean(final VncVal val) {
		return val == Constants.True || val == Constants.False;
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

	public static boolean isVncNumber(final VncVal val) {
		return val != null && (isVncLong(val) || isVncInteger(val) || isVncDouble(val) || isVncBigDecimal(val));
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

	public static boolean isVncSequence(final VncVal val) {
		return val != null && (val instanceof VncSequence);
	}
	
	public static boolean isVncList(final VncVal val) {
		return val != null && (val instanceof VncList);
	}

	public static boolean isVncVector(final VncVal val) {
		return val != null && (val instanceof VncVector);
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

	public static boolean isVncStack(final VncVal val) {
		return val != null && (val instanceof VncStack);
	}

	public static boolean isIVncFunction(final VncVal val) {
		return val != null && (val instanceof IVncFunction);
	}

	public static boolean isVncFunction(final VncVal val) {
		return val != null && (val instanceof VncFunction);
	}
	
	public static boolean isVncMultiFunction(final VncVal val) {
		return val != null && (val instanceof VncMultiFunction);
	}

	public static boolean isVncMacro(final VncVal val) {
		return val != null && isVncFunction(val) && ((VncFunction)val).isMacro();
	}

	public static boolean isVncFunctionOrKeyword(final VncVal val) {
		return val != null && ((val instanceof VncFunction) || (val instanceof VncKeyword));
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
		if (val == Constants.Nil) {
			return new VncKeyword("venice.Nil");
		}
		else if (val ==  Constants.True || val == Constants.False) {
			return new VncKeyword("venice.Boolean");
		}
		else if (Types.isVncAtom(val)) {
			return new VncKeyword("venice.Atom");
		}
		else if (Types.isVncThreadLocal(val)) {
			return new VncKeyword("venice.ThreadLocal");
		}
		else if (Types.isVncLong(val)) {
			return new VncKeyword("venice.Long");
		}
		else if (Types.isVncInteger(val)) {
			return new VncKeyword("venice.Integer");
		}
		else if (Types.isVncDouble(val)) {
			return new VncKeyword("venice.Double");
		}
		else if (Types.isVncBigDecimal(val)) {
			return new VncKeyword("venice.Decimal");
		}
		else if (Types.isVncByteBuffer(val)) {
			return new VncKeyword("venice.ByteBuffer");
		}
		else if (Types.isVncFunction(val)) {
			return new VncKeyword("venice.Function");
		}
		else if (Types.isVncSymbol(val)) {
			return new VncKeyword("venice.Symbol");
		}
		else if (Types.isVncKeyword(val)) {
			return new VncKeyword("venice.Keyword");
		}
		else if (Types.isVncString(val)) {
			return new VncKeyword("venice.String");
		}
		else if (Types.isVncVector(val)) {
			return new VncKeyword("venice.Vector");
		}
		else if (Types.isVncList(val)) {
			return new VncKeyword("venice.List");
		}
		else if (Types.isVncHashSet(val)) {
			return new VncKeyword("venice.HashSet");
		}
		else if (Types.isVncSortedSet(val)) {
			return new VncKeyword("venice.SortedSet");
		}
		else if (Types.isVncHashMap(val)) {
			return new VncKeyword("venice.HashMap");
		}
		else if (Types.isVncOrderedMap(val)) {
			return new VncKeyword("venice.OrderedMap");
		}
		else if (Types.isVncSortedMap(val)) {
			return new VncKeyword("venice.SortedMap");
		}
		else if (Types.isVncMutableMap(val)) {
			return new VncKeyword("venice.MutableMap");
		}
		else if (Types.isVncJavaObject(val)) {
			return new VncKeyword(((IVncJavaObject)val).getDelegate().getClass().getName());
		}
		else if (Types.isVncJavaSet(val)) {
			return new VncKeyword(((IVncJavaObject)val).getDelegate().getClass().getName());
		}
		else if (Types.isVncJavaList(val)) {
			return new VncKeyword(((IVncJavaObject)val).getDelegate().getClass().getName());
		}
		else if (Types.isVncJavaMap(val)) {
			return new VncKeyword(((IVncJavaObject)val).getDelegate().getClass().getName());
		}
		else if (Types.isVncSet(val)) {
			return new VncKeyword("venice.Set");
		}
		else if (Types.isVncMap(val)) {
			return new VncKeyword("venice.Map");
		}
		else {
			return new VncKeyword(val.getClass().getName());
		}
	};
	
	public static boolean isInstanceOf(final VncKeyword type, final VncVal val) {
		final String clazz = type.getValue();
		
		switch(clazz) {
			case "venice.Nil":			return val == Nil;
			case "venice.Boolean":		return val == True || val == False;
			case "venice.Atom":			return Types.isVncAtom(val);
			case "venice.ThreadLocal":	return Types.isVncThreadLocal(val);
			case "venice.Long":			return Types.isVncLong(val);
			case "venice.Integer":		return Types.isVncInteger(val);
			case "venice.Double":		return Types.isVncDouble(val);
			case "venice.Decimal":		return Types.isVncBigDecimal(val);
			case "venice.ByteBuffer":	return Types.isVncByteBuffer(val);
			case "venice.Function":		return Types.isVncFunction(val);
			case "venice.String":		return Types.isVncString(val);
			case "venice.Symbol":		return Types.isVncSymbol(val);
			case "venice.Keyword":		return Types.isVncKeyword(val);
			case "venice.Collection":	return Types.isVncCollection(val);
			case "venice.Sequence":		return Types.isVncSequence(val);
			case "venice.Vector":		return Types.isVncVector(val);
			case "venice.List":			return Types.isVncList(val);
			case "venice.Set":			return Types.isVncSet(val);
			case "venice.HashSet":		return Types.isVncHashSet(val);
			case "venice.SortedSet":	return Types.isVncSortedSet(val);
			case "venice.Map":			return Types.isVncMap(val);
			case "venice.HashMap":		return Types.isVncHashMap(val);
			case "venice.OrderedMap":	return Types.isVncOrderedMap(val);
			case "venice.SortedMap":	return Types.isVncSortedMap(val);
			case "venice.MutableMap":	return Types.isVncMutableMap(val);
			default:
				try {
					if (Types.isVncJavaObject(val)) {
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

	public static boolean _equal_Q(VncVal a, VncVal b) {
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
				return ((VncHashSet)a).getList().stream().allMatch(v -> ((VncHashSet)b).contains(v));
			} 
			else if (a instanceof VncMap) {
				if (((VncMap)a).getMap().size() != ((VncMap)b).getMap().size()) {
					return false;
				}
				final VncMap mhm = ((VncMap)a);
				final Map<VncVal,VncVal> hm = mhm.getMap();
				for (VncVal k : hm.keySet()) {
					final VncVal valA = ((VncMap)a).getMap().get(k);
					final VncVal valB = ((VncMap)b).getMap().get(k);
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
	
	public static boolean _match_Q(VncVal a, VncVal b) {
		if (a instanceof VncString) {
			return ((VncString)a).getValue().matches(((VncString)b).getValue());
		} 
		else {
			return false;
		}
	}

}
