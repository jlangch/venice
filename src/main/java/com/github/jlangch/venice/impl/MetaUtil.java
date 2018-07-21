package com.github.jlangch.venice.impl;

import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class MetaUtil {

	public static VncVal withTokenPos(final VncVal val, final Token token) {
		val.setMetaVal(FILE, new VncString(token.getFile()));
		val.setMetaVal(LINE, new VncLong(token.getLine()));
		val.setMetaVal(new VncSymbol(":column"), new VncLong(token.getColumn()));
		return val;
	}

	public static void copyTokenPos(final VncVal from, final VncVal to) {
		to.setMetaVal(FILE, from.getMetaVal(FILE));
		to.setMetaVal(LINE, from.getMetaVal(LINE));
		to.setMetaVal(COLUMN, from.getMetaVal(COLUMN));
	}

	
	// Var definition
	public static final VncSymbol ARGSLIST = new VncSymbol(":arglists"); 
	public static final VncSymbol DOC = new VncSymbol(":doc"); 
	
	// File error location
	public static final VncSymbol FILE = new VncSymbol(":file"); 
	public static final VncSymbol LINE = new VncSymbol(":line"); 
	public static final VncSymbol COLUMN = new VncSymbol(":column"); 
}
