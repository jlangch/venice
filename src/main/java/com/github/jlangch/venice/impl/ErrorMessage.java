package com.github.jlangch.venice.impl;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class ErrorMessage {
	
	public static String buildErrLocation(final Token token) {
		return String.format(
				"File <%s> (%d,%d)",
				token.getFile(),
				token.getLine(),
				token.getColumn());
	}
	
	public static String buildErrLocation(final VncVal val) {
		final VncVal file = val.getMetaVal(FILE);
		final VncVal line = val.getMetaVal(LINE);
		final VncVal column = val.getMetaVal(COLUMN);
		return String.format(
				"File <%s> (%d,%d)",
				file == Constants.Nil ? "unknown" : ((VncString)file).getValue(),
				line == Constants.Nil ? 1 : ((VncLong)line).getValue(),
				column == Constants.Nil ? 1 : ((VncLong)column).getValue());
	}
	
	
	public static final VncSymbol FILE = new VncSymbol(":file"); 
	public static final VncSymbol LINE = new VncSymbol(":line"); 
	public static final VncSymbol COLUMN = new VncSymbol(":column"); 
}
