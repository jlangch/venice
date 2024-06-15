/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.util.Map;

import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
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
import com.github.jlangch.venice.impl.types.VncFloat;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncJust;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncMultiArityFunction;
import com.github.jlangch.venice.impl.types.VncMultiFunction;
import com.github.jlangch.venice.impl.types.VncNumber;
import com.github.jlangch.venice.impl.types.VncScalar;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.VncVolatile;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncDAG;
import com.github.jlangch.venice.impl.types.collections.VncDelayQueue;
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
import com.github.jlangch.venice.impl.types.concurrent.VncLock;
import com.github.jlangch.venice.impl.types.custom.VncCustomType;
import com.github.jlangch.venice.impl.types.custom.VncProtocol;
import com.github.jlangch.venice.impl.util.MetaUtil;

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

    public static boolean isVncFloat(final VncVal val) {
        return val != null && (val instanceof VncFloat);
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

    public static boolean isVncDelayQueue(final VncVal val) {
        return val != null && (val instanceof VncDelayQueue);
    }

    public static boolean isVncDAG(final VncVal val) {
        return val != null && (val instanceof VncDAG);
    }

    public static boolean isVncLock(final VncVal val) {
        return val != null && (val instanceof VncLock);
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

    public static boolean isVncSpecialForm(final VncVal val) {
        return val != null && (val instanceof VncSpecialForm);
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

    public static boolean isVncProtocol(final VncVal val) {
        return val != null && (val instanceof VncProtocol);
    }

    public static VncKeyword getType(final VncVal val) {
        return val.getType();
    }

    public static VncVal getSupertype(final VncVal val) {
        // may not have a supertype -> return nil
        return MetaUtil.getSupertypes(val.getType().getMeta()).first();
    }

    public static VncList getSupertypes(final VncVal val) {
        return MetaUtil.getSupertypes(val.getType().getMeta());
    }

    public static boolean isInstanceOf(final VncKeyword type, final VncVal val) {
        final String sType = ":" + type.getQualifiedName();

        switch(sType) {
            case ":core/nil":            return val == Nil;

            case ":core/char":           return Types.isVncChar(val);
            case ":core/string":         return Types.isVncString(val);
            case ":core/boolean":        return Types.isVncBoolean(val);
            case ":core/number":         return Types.isVncNumber(val);
            case ":core/integer":        return Types.isVncInteger(val);
            case ":core/long":           return Types.isVncLong(val);
            case ":core/double":         return Types.isVncDouble(val);
            case ":core/decimal":        return Types.isVncBigDecimal(val);
            case ":core/bigint":         return Types.isVncBigInteger(val);
            case ":core/bytebuf":        return Types.isVncByteBuffer(val);

            case ":core/symbol":         return Types.isVncSymbol(val);
            case ":core/keyword":        return Types.isVncKeyword(val);

            case ":core/atom":           return Types.isVncAtom(val);
            case ":core/volatile":       return Types.isVncVolatile(val);
            case ":core/thread-local":   return Types.isVncThreadLocal(val);

            case ":core/java-object":    return Types.isVncJavaObject(val);

            case ":core/just":           return Types.isVncJust(val);
            case ":core/function":       return Types.isVncFunction(val);
            case ":core/macro":          return Types.isVncMacro(val);

            case ":core/collection":     return Types.isVncCollection(val);

            case ":core/sequence":       return Types.isVncSequence(val);
            case ":core/vector":         return Types.isVncVector(val);
            case ":core/list":           return Types.isVncList(val);
            case ":core/mutable-list":   return Types.isVncMutableList(val);
            case ":core/mutable-vector": return Types.isVncMutableVector(val);

            case ":core/lazyseq":         return Types.isVncSequence(val);

            case ":core/set":            return Types.isVncSet(val);
            case ":core/hash-set":       return Types.isVncHashSet(val);
            case ":core/sorted-set":     return Types.isVncSortedSet(val);
            case ":core/mutable-set":    return Types.isVncMutableSet(val);

            case ":core/map":            return Types.isVncMap(val);
            case ":core/hash-map":       return Types.isVncHashMap(val);
            case ":core/ordered-map":    return Types.isVncOrderedMap(val);
            case ":core/sorted-map":     return Types.isVncSortedMap(val);
            case ":core/mutable-map":    return Types.isVncMutableMap(val);
            case ":core/map-entry":      return Types.isVncMapEntry(val);

            case ":core/stack":          return Types.isVncStack(val);
            case ":core/queue":          return Types.isVncQueue(val);
            case ":core/delay-queue":    return Types.isVncDelayQueue(val);

            case ":core/custom-type":    return Types.isVncCustomType(val);

            case ":core/protocol":       return Types.isVncProtocol(val);

            case ":core/lock":           return Types.isVncLock(val);

            case ":dag/dag":             return Types.isVncDAG(val);

            default:
                try {
                    if (Types.isVncCustomType(val)) {
                        if (((VncCustomType)val).getType().equals(type)) {
                            return true;
                        }
                        else if (getSupertypes(val).getJavaList().contains(type)) {
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

                    // lookup imports
                    final Class<?> javaClazz = JavaInteropUtil.toClass(
                                                type,
                                                Namespaces.getCurrentNamespace().getJavaImports());

                    if (Types.isVncJavaObject(val)) {
                        return Class.forName(javaClazz.getName())
                                    .isAssignableFrom(((IVncJavaObject)val).getDelegate().getClass());
                    }
                    else if (Types.isVncJavaSet(val)) {
                        return Class.forName(javaClazz.getName())
                                    .isAssignableFrom(((IVncJavaObject)val).getDelegate().getClass());
                    }
                    else if (Types.isVncJavaList(val)) {
                        return Class.forName(javaClazz.getName())
                                    .isAssignableFrom(((IVncJavaObject)val).getDelegate().getClass());
                    }
                    else if (Types.isVncJavaMap(val)) {
                        return Class.forName(javaClazz.getName())
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
                                ? ((VncKeyword)val).getQualifiedName()
                                : val.getType().getQualifiedName();

        switch(type) {
            case "core/nil":        return true;
            case "core/boolean":    return true;
            case "core/long":       return true;
            case "core/integer":    return true;
            case "core/double":     return true;
            case "core/bigint":     return true;
            case "core/decimal":    return true;
            case "core/string":     return true;
            case "core/char":       return true;
            case "core/symbol":     return true;
            case "core/keyword":    return true;
            case "core/function":   return true;
            default:                return false;
        }
    }

    public static boolean isCoreType(final VncVal val) {
        if (val == null) {
            return false;
        }

        return isCoreType(Types.isVncKeyword(val)
                                ? ((VncKeyword)val).getQualifiedName()
                                : val.getType().getQualifiedName());
    }

    public static boolean isCoreType(final String type) {
        if (type == null) {
            return false;
        }

        if (type.startsWith("core/")) {
            return true;
        }

        switch(type) {
            case "nil":            return true;

            case "char":           return true;
            case "string":         return true;
            case "boolean":        return true;
            case "number":         return true;
            case "integer":        return true;
            case "long":           return true;
            case "double":         return true;
            case "decimal":        return true;
            case "bigint":         return true;
            case "bytebuf":        return true;

            case "symbol":         return true;
            case "keyword":        return true;

            case "atom":           return true;
            case "volatile":       return true;
            case "thread-local":   return true;

            case "java-object":    return true;

            case "just":           return true;
            case "function":       return true;
            case "macro":          return true;

            case "collection":     return true;

            case "sequence":       return true;
            case "vector":         return true;
            case "list":           return true;
            case "mutable-list":   return true;
            case "mutable-vector": return true;

            case "lazyseq":        return true;

            case "set":            return true;
            case "hash-set":       return true;
            case "sorted-set":     return true;
            case "mutable-set":    return true;

            case "map":            return true;
            case "hash-map":       return true;
            case "ordered-map":    return true;
            case "sorted-map":     return true;
            case "mutable-map":    return true;
            case "map-entry":      return true;

            case "stack":          return true;
            case "queue":          return true;
            case "delay-queue":    return true;

            case "custom-type":    return true;

            case "protocol":       return true;

            case "lock":           return true;

            default:               return false;
        }
    }

    public static boolean _equal_Q(final VncVal a, final VncVal b) {
        return _equal_Q(a, b, false);
    }

    public static boolean _equal_strict_Q(final VncVal a, final VncVal b) {
        return _equal_Q(a, b, true);
    }

    private static boolean _equal_Q(final VncVal a, final VncVal b, final boolean strict) {
        if (!strict) {
            if (Types.isVncNumber(a) && Types.isVncNumber(b)) {
                return VncBoolean.isTrue(((VncNumber)a).equ(b));
            }
            else if (Types.isVncString(a) && Types.isVncChar(b)) {
                return ((VncString)a).getValue().equals(((VncChar)b).getValue().toString());
            }
            else if (Types.isVncChar(a) && Types.isVncString(b)) {
                return ((VncChar)a).getValue().toString().equals(((VncString)b).getValue());
            }
        }

        if (a instanceof VncScalar) {
            return a.equals(b);
        }
        else if (a instanceof VncSymbol) {
            return a.equals(b);
        }
        else if (a instanceof VncSequence && b instanceof VncSequence) {
            if (((VncSequence)a).size() != ((VncSequence)b).size()) {
                return false;
            }
            for (int i=0; i<((VncSequence)a).size(); i++) {
                if (!_equal_Q(((VncSequence)a).nth(i), ((VncSequence)b).nth(i), strict)) {
                    return false;
                }
            }
            return true;
        }
        else if (a instanceof VncSet && b instanceof VncSet) {
            if (((VncSet)a).size() != ((VncSet)b).size()) {
                return false;
            }
            return ((VncSet)a).stream().allMatch(v -> ((VncSet)b).contains(v));
        }
        else if (a instanceof VncCustomType && b instanceof VncCustomType) {
            final VncCustomType valA = (VncCustomType)a;
            final VncCustomType valB = (VncCustomType)b;

            if (!valA.getTypeDef().getType().getValue().equals(valB.getTypeDef().getType().getValue())) {
                return false;
            }

            return _equal_Q(valA.getValuesAsVector(), valB.getValuesAsVector(), strict);
        }
        else if (a instanceof VncMap && b instanceof VncMap) {
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
                    if (! _equal_Q(valA, valB, strict)) {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
            return true;
        }
        else if (a instanceof VncByteBuffer && b instanceof VncByteBuffer) {
            return a.equals(b);
        }
        else if (a instanceof VncVolatile && b instanceof VncVolatile) {
            return _equal_Q(((VncVolatile)a).deref(), ((VncVolatile)b).deref(), strict);
        }
        else if (a instanceof VncAtom && b instanceof VncAtom) {
            return _equal_Q(((VncAtom)a).deref(), ((VncAtom)b).deref(), strict);
        }
        else if (a instanceof VncJust && b instanceof VncJust) {
            return _equal_Q(((VncJust)a).deref(), ((VncJust)b).deref(), strict);
        }
        else {
            return a.equals(b);
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
