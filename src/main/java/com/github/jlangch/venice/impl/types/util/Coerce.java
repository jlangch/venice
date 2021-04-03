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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBigInteger;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncChar;
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
import com.github.jlangch.venice.impl.types.VncVolatile;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMutableList;
import com.github.jlangch.venice.impl.types.collections.VncMutableMap;
import com.github.jlangch.venice.impl.types.collections.VncMutableSet;
import com.github.jlangch.venice.impl.types.collections.VncMutableVector;
import com.github.jlangch.venice.impl.types.collections.VncQueue;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncSortedSet;
import com.github.jlangch.venice.impl.types.collections.VncStack;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class Coerce {

	public static IDeref toIDeref(final Object val) {
		if (val == null || Types.isIDeref(val)) {
			return (IDeref)val;
		}
		else if (Types.isVncVal(val)) {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to IDeref. %s", 
					Types.getType((VncVal)val),
					ErrorMessage.buildErrLocation((VncVal)val)));
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to IDeref", 
					val.getClass()));
		}
	}

	public static VncAtom toVncAtom(final VncVal val) {
		if (val == null || Types.isVncAtom(val)) {
			return (VncAtom)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to atom. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncVolatile toVncVolatile(final VncVal val) {
		if (val == null || Types.isVncVolatile(val)) {
			return (VncVolatile)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to volatile. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncThreadLocal toVncThreadLocal(final VncVal val) {
		if (val == null || Types.isVncThreadLocal(val)) {
			return (VncThreadLocal)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to thread-local. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncKeyword toVncKeyword(final VncVal val) {
		if (val == null || Types.isVncKeyword(val)) {
			return (VncKeyword)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to keyword. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncSymbol toVncSymbol(final VncVal val) {
		if (val == null || Types.isVncSymbol(val)) {
			return (VncSymbol)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to symbol. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static IVncFunction toIVncFunction(final VncVal val) {
		if (val == null || Types.isIVncFunction(val)) {
			if (((IVncFunction)val).isMacro()) {
				throw new VncException(String.format(
						"Cannot coerce a macro to a function. The macro '%s' can " +
						"not be passed as an argument if a function is expected. %s",
						((VncFunction)val).getQualifiedName(),
						ErrorMessage.buildErrLocation(val)));
			}
			else {
				return (IVncFunction)val;
			}
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to function. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncFunction toVncFunction(final VncVal val) {
		if (val == null || Types.isVncFunction(val)) {
			return (VncFunction)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to function. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncFunction toVncFunctionOptional(final VncVal val) {
		if (val == null || val == Constants.Nil) {
			return null;
		}
		else if (Types.isVncFunction(val)) {
			return (VncFunction)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to function. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncMultiFunction toVncMultiFunction(final VncVal val) {
		if (val == null || Types.isVncMultiFunction(val)) {
			return (VncMultiFunction)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to multi function. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncString toVncString(final VncVal val) {
		if (val == null || Types.isVncString(val)) {
			return (VncString)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to string. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncChar toVncChar(final VncVal val) {
		if (val == null || Types.isVncChar(val)) {
			return (VncChar)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to char. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncBoolean toVncBoolean(final VncVal val) {
		if (val == null || Types.isVncBoolean(val)) {
			return (VncBoolean)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to boolean. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncInteger toVncInteger(final VncVal val) {
		if (val == null || Types.isVncInteger(val)) {
			return (VncInteger)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to int. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncLong toVncLong(final VncVal val) {
		if (val == null || Types.isVncLong(val)) {
			return (VncLong)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to long. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncDouble toVncDouble(final VncVal val) {
		if (val == null || Types.isVncDouble(val)) {
			return (VncDouble)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to double. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncBigDecimal toVncBigDecimal(final VncVal val) {
		if (val == null || Types.isVncBigDecimal(val)) {
			return (VncBigDecimal)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to big-decimal. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncBigInteger toVncBigInteger(final VncVal val) {
		if (val == null || Types.isVncBigInteger(val)) {
			return (VncBigInteger)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to big-integer. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncByteBuffer toVncByteBuffer(final VncVal val) {
		if (val == null || Types.isVncByteBuffer(val)) {
			return (VncByteBuffer)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to bytebuf. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncCollection toVncCollection(final VncVal val) {
		if (val == null || Types.isVncCollection(val)) {
			return (VncCollection)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to collection. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncSequence toVncSequence(final VncVal val) {
		if (val == null || Types.isVncSequence(val)) {
			return (VncSequence)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to a sequential collection. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncList toVncList(final VncVal val) {
		if (val == null || Types.isVncList(val)) {
			return (VncList)val;
		}
		else if (Types.isVncSequence(val)) {
			return ((VncSequence)val).toVncList();
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to list. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncVector toVncVector(final VncVal val) {
		if (val == null || Types.isVncVector(val)) {
			return (VncVector)val;
		}
		else if (Types.isVncSequence(val)) {
			return ((VncSequence)val).toVncVector();
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to vector. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncMutableList toVncMutableList(final VncVal val) {
		if (val == null || val instanceof VncMutableList) {
			return (VncMutableList)val;
		}
		else if (Types.isVncSequence(val)) {
			return VncMutableList.ofAll((VncSequence)val, Constants.Nil);
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to mutable-list. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncMutableVector toVncMutableVector(final VncVal val) {
		if (val == null || val instanceof VncMutableVector) {
			return (VncMutableVector)val;
		}
		else if (Types.isVncSequence(val)) {
			return VncMutableVector.ofAll((VncSequence)val, Constants.Nil);
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to mutable-vector. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncMap toVncMap(final VncVal val) {
		if (val == null || Types.isVncMap(val)) {
			return (VncMap)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to map. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncHashMap toVncHashMap(final VncVal val) {
		if (val == null || val instanceof VncHashMap) {
			return (VncHashMap)val;
		}
		else if (Types.isVncMap(val)) {
			return new VncHashMap(((VncMap)val).getJavaMap());
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to hash-map. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncMutableMap toVncMutableMap(final VncVal val) {
		if (val == null || val instanceof VncMutableMap) {
			return (VncMutableMap)val;
		}
		else if (Types.isVncMap(val)) {
			return new VncMutableMap(((VncMap)val).getJavaMap());
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to mutable-map. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncSet toVncSet(final VncVal val) {
		if (val == null || Types.isVncSet(val)) {
			return (VncSet)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to set. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncHashSet toVncHashSet(final VncVal val) {
		if (val == null || Types.isVncHashSet(val)) {
			return (VncHashSet)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to set. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncSortedSet toVncSortedSet(final VncVal val) {
		if (val == null || Types.isVncSortedSet(val)) {
			return (VncSortedSet)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to sorted set. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncMutableSet toVncMutableSet(final VncVal val) {
		if (val == null || val instanceof VncMutableSet) {
			return (VncMutableSet)val;
		}
		else if (Types.isVncSet(val)) {
			return VncMutableSet.ofAll((VncSet)val, Constants.Nil);
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to mutable-set. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncStack toVncStack(final VncVal val) {
		if (val == null || val instanceof VncStack) {
			return (VncStack)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to stack. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncQueue toVncQueue(final VncVal val) {
		if (val == null || val instanceof VncQueue) {
			return (VncQueue)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to queue. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncJavaObject toVncJavaObject(final VncVal val) {
		if (val == null || Types.isVncJavaObject(val)) {
			return (VncJavaObject)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to java-object. %s", 
					Types.getType(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T toVncJavaObject(final VncVal val, final Class<T> type) {
		if (val == null) {
			return null;
		}
		else if (Types.isVncJavaObject(val, type)) {
			return (T)((VncJavaObject)val).getDelegate();
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to java-object of type %s. %s", 
					Types.getType(val),
					type.getName(),
					ErrorMessage.buildErrLocation(val)));
		}
	}
}
