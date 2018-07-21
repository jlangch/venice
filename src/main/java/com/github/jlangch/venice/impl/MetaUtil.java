package com.github.jlangch.venice.impl;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class MetaUtil {

	public static VncVal addDefMeta(final VncVal val, final VncMap meta) {
		final VncVal argslist = meta.get(ARGLIST);
		if (argslist != Constants.Nil) {
			val.setMetaVal(ARGLIST, argslist);
		}
		final VncVal doc = meta.get(DOC);
		if (doc != Constants.Nil) {
			val.setMetaVal(DOC, doc);
		}
		final VncVal examples = meta.get(EXAMPLES);
		if (examples != Constants.Nil) {
			val.setMetaVal(EXAMPLES, examples);
		}
		return val;
	}
	
	public static VncVal setArgList(final VncVal val, final String... argList) {
		val.setMetaVal(
				ARGLIST, 
				new VncList(Arrays.stream(argList).map(s -> new VncString(s)).collect(Collectors.toList())));
		return val;
	}
	
	public static VncVal setDoc(final VncVal val, final String doc) {
		val.setMetaVal(DOC, new VncString(doc));
		return val;
	}
	
	public static VncVal setExamples(final VncVal val, final String... examples) {
		val.setMetaVal(
				EXAMPLES, 
				new VncList(Arrays.stream(examples).map(s -> new VncString(s)).collect(Collectors.toList())));
		return val;
	}
	
	public static VncVal withTokenPos(final VncVal val, final Token token) {
		val.setMetaVal(FILE, new VncString(token.getFile()));
		val.setMetaVal(LINE, new VncLong(token.getLine()));
		val.setMetaVal(COLUMN, new VncLong(token.getColumn()));
		return val;
	}

	public static void copyTokenPos(final VncVal from, final VncVal to) {
		to.setMetaVal(FILE, from.getMetaVal(FILE));
		to.setMetaVal(LINE, from.getMetaVal(LINE));
		to.setMetaVal(COLUMN, from.getMetaVal(COLUMN));
	}

	
	// Var definition
	public static final VncKeyword ARGLIST = new VncKeyword(":arglists"); 
	public static final VncKeyword DOC = new VncKeyword(":doc"); 
	public static final VncKeyword EXAMPLES = new VncKeyword(":examples"); 
	
	// File error location
	public static final VncKeyword FILE = new VncKeyword(":file"); 
	public static final VncKeyword LINE = new VncKeyword(":line"); 
	public static final VncKeyword COLUMN = new VncKeyword(":column"); 
}
