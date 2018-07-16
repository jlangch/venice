package org.venice.impl;

import org.venice.impl.types.Constants;
import org.venice.impl.types.VncLong;
import org.venice.impl.types.VncString;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.VncVal;


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
	
	
	private static final VncSymbol FILE = new VncSymbol(":file"); 
	private static final VncSymbol LINE = new VncSymbol(":line"); 
	private static final VncSymbol COLUMN = new VncSymbol(":column"); 
}
