/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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

import java.util.Map;

import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncJavaSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncSortedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;

public class Types {
	
	public static boolean isVncAtom(final VncVal val) {
		return val != null && (val instanceof VncAtom);
	}
	
	public static boolean isVncThreadLocal(final VncVal val) {
		return val != null && (val instanceof VncThreadLocal);
	}

	public static boolean isVncString(final VncVal val) {
		return val != null && (val instanceof VncString);
	}

	public static boolean isVncKeyword(final String s) {
		return s.length() != 0 && s.charAt(0) == Constants.KEYWORD_PREFIX;
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
		return val != null && (isVncLong(val) || isVncDouble(val) || isVncBigDecimal(val));
	}

	public static boolean isVncByteBuffer(final VncVal val) {
		return val != null && (val instanceof VncByteBuffer);
	}

	public static boolean isVncCollection(final VncVal val) {
		return val != null && (val instanceof VncCollection);
	}

	public static boolean isVncSequence(final VncVal val) {
		return val != null && (val instanceof VncSequence);
	}

	public static boolean isVncSet(final VncVal val) {
		return val != null && (val instanceof VncSet);
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

	public static boolean isVncFunction(final VncVal val) {
		return val != null && (val instanceof VncFunction);
	}

	public static boolean isVncFunctionOrKeyword(final VncVal val) {
		return val != null && ((val instanceof VncFunction) || (val instanceof VncKeyword));
	}

	public static boolean isVncJavaObject(final VncVal val) {
		return val != null && (val instanceof VncJavaObject);
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

	public static VncString getClassName(final VncVal val) {
		if (val == Constants.Nil) {
			return new VncString("venice.Nil");
		}
		else if (val ==  Constants.True || val == Constants.False) {
			return new VncString("venice.Boolean");
		}
		else if (Types.isVncAtom(val)) {
			return new VncString("venice.Atom");
		}
		else if (Types.isVncThreadLocal(val)) {
			return new VncString("venice.ThreadLocal");
		}
		else if (Types.isVncLong(val)) {
			return new VncString("venice.Long");
		}
		else if (Types.isVncDouble(val)) {
			return new VncString("venice.Double");
		}
		else if (Types.isVncBigDecimal(val)) {
			return new VncString("venice.Decimal");
		}
		else if (Types.isVncByteBuffer(val)) {
			return new VncString("venice.ByteBuffer");
		}
		else if (Types.isVncFunction(val)) {
			return new VncString("venice.Function");
		}
		else if (Types.isVncString(val)) {
			return new VncString("venice.String");
		}
		else if (Types.isVncSymbol(val)) {
			return new VncString("venice.Symbol");
		}
		else if (Types.isVncSet(val)) {
			return new VncString("venice.Set");
		}
		else if (Types.isVncVector(val)) {
			return new VncString("venice.Vector");
		}
		else if (Types.isVncList(val)) {
			return new VncString("venice.List");
		}
		else if (Types.isVncHashMap(val)) {
			return new VncString("venice.HashMap");
		}
		else if (Types.isVncOrderedMap(val)) {
			return new VncString("venice.OrderedMap");
		}
		else if (Types.isVncSortedMap(val)) {
			return new VncString("venice.SortedMap");
		}
		else if (Types.isVncJavaObject(val)) {
			return new VncString("venice.JavaObject(" + ((IVncJavaObject)val).getDelegate().getClass().getName() + ")");
		}
		else if (Types.isVncJavaSet(val)) {
			return new VncString("venice.JavaSet(" + ((IVncJavaObject)val).getDelegate().getClass().getName() + ")");
		}
		else if (Types.isVncJavaList(val)) {
			return new VncString("venice.JavaList(" + ((IVncJavaObject)val).getDelegate().getClass().getName() + ")");
		}
		else if (Types.isVncJavaMap(val)) {
			return new VncString("venice.JavaMap(" + ((IVncJavaObject)val).getDelegate().getClass().getName() + ")");
		}
		else if (Types.isVncMap(val)) {
			return new VncString("venice.Map");
		}
		else {
			return new VncString(val.getClass().getName());
		}
	};

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
			else if (a instanceof VncList) {
				if (((VncList)a).size() != ((VncList)b).size()) {
					return false;
				}
				for (Integer i=0; i<((VncList)a).size(); i++) {
					if (!_equal_Q(((VncList)a).nth(i), ((VncList)b).nth(i))) {
						return false;
					}
				}
				return true;
			} 
			else if (a instanceof VncSet) {
				if (((VncSet)a).size() != ((VncSet)b).size()) {
					return false;
				}
				return ((VncSet)a).getList().stream().allMatch(v -> ((VncSet)b).contains(v));
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
