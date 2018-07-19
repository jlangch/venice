package com.github.jlangch.venice.impl.types;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.ErrorMessage;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.collections.VncVector;


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
