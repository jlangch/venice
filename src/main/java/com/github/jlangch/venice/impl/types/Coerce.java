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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class Coerce {

	public static VncAtom toVncAtom(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncAtom) {
			return (VncAtom)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to atom. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncThreadLocal toVncThreadLocal(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncThreadLocal) {
			return (VncThreadLocal)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to thread-local. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncKeyword toVncKeyword(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncKeyword) {
			return (VncKeyword)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to keyword. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncSymbol toVncSymbol(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncSymbol) {
			return (VncSymbol)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to symbol. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncFunction toVncFunction(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncFunction) {
			return (VncFunction)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to function. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}

	public static VncString toVncString(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncString) {
			return (VncString)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to string. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncConstant toVncBoolean(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val == Constants.False || val == Constants.True) {
			return (VncConstant)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to boolean. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncLong toVncLong(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncLong) {
			return (VncLong)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to long. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncDouble toVncDouble(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncDouble) {
			return (VncDouble)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to double. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncBigDecimal toVncBigDecimal(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncBigDecimal) {
			return (VncBigDecimal)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to big-decimal. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncByteBuffer toVncByteBuffer(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncByteBuffer) {
			return (VncByteBuffer)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to bytebuf. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncCollection toVncCollection(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncCollection) {
			return (VncCollection)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to collection. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncSequence toVncSequence(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncSequence) {
			return (VncSequence)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to a sequential collection. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncList toVncList(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncList) {
			return (VncList)val;
		}
		else if (val instanceof VncJavaList) {
			return ((VncJavaList)val).toVncList();
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to list. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncVector toVncVector(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncVector) {
			return (VncVector)val;
		}
		else if (val instanceof VncJavaList) {
			return ((VncJavaList)val).toVncVector();
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to vector. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncMap toVncMap(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncMap) {
			return (VncMap)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to map. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncHashMap toVncHashMap(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncHashMap) {
			return (VncHashMap)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to hash-map. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncSet toVncSet(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncSet) {
			return (VncSet)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to set. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
	
	public static VncJavaObject toVncJavaObject(final VncVal val) {
		if (val == null) {
			return null;
		}
		else if (val instanceof VncJavaObject) {
			return (VncJavaObject)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot coerce value of type %s to java-object. %s", 
					Types.getClassName(val),
					ErrorMessage.buildErrLocation(val)));
		}
	}
}
