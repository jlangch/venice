package org.venice.impl.types;

import org.venice.impl.types.collections.VncHashMap;
import org.venice.impl.types.collections.VncJavaObject;
import org.venice.impl.types.collections.VncList;
import org.venice.impl.types.collections.VncMap;
import org.venice.impl.types.collections.VncSet;
import org.venice.impl.types.collections.VncVector;


public class Coerce {

	public static VncAtom toVncAtom(final VncVal val) {
		return (VncAtom)val;
	}

	public static VncSymbol toVncSymbol(final VncVal val) {
		return (VncSymbol)val;
	}

	public static VncFunction toVncFunction(final VncVal val) {
		return (VncFunction)val;
	}

	public static VncString toVncString(final VncVal val) {
		return (VncString)val;
	}
	
	public static VncLong toVncLong(final VncVal val) {
		return (VncLong)val;
	}
	
	public static VncDouble toVncDouble(final VncVal val) {
		return (VncDouble)val;
	}
	
	public static VncBigDecimal toVncBigDecimal(final VncVal val) {
		return (VncBigDecimal)val;
	}
	
	public static VncList toVncList(final VncVal val) {
		return (VncList)val;
	}
	
	public static VncVector toVncVector(final VncVal val) {
		return (VncVector)val;
	}
	
	public static VncMap toVncMap(final VncVal val) {
		return (VncMap)val;
	}
	
	public static VncHashMap toVncHashMap(final VncVal val) {
		return (VncHashMap)val;
	}
	
	public static VncSet toVncSet(final VncVal val) {
		return (VncSet)val;
	}
	
	public static VncJavaObject toVncJavaObject(final VncVal val) {
		return (VncJavaObject)val;
	}
	
	public static VncByteBuffer toVncByteBuffer(final VncVal val) {
		return (VncByteBuffer)val;
	}
}
