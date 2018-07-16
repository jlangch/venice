package org.venice.impl;

import org.venice.impl.types.VncLong;
import org.venice.impl.types.VncString;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.VncVal;


public class ReaderUtil {

	public static VncVal withTokenPos(final VncVal val, final Token token) {
		val.setMetaVal(new VncSymbol(":file"), new VncString(token.getFile()));
		val.setMetaVal(new VncSymbol(":line"), new VncLong(token.getLine()));
		val.setMetaVal(new VncSymbol(":column"), new VncLong(token.getColumn()));
		return val;
	}

	public static void copyTokenPos(final VncVal from, final VncVal to) {
		to.setMetaVal(new VncSymbol(":file"), from.getMetaVal(new VncSymbol(":file")));
		to.setMetaVal(new VncSymbol(":line"), from.getMetaVal(new VncSymbol(":line")));
		to.setMetaVal(new VncSymbol(":column"), from.getMetaVal(new VncSymbol(":column")));
	}

}
