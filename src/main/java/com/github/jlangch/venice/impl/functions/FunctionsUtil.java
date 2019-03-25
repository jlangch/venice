package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Types;


public class FunctionsUtil {

	public static void assertArity(
			final String fnName, 
			final VncList args, 
			final int... expectedArities
	) {
		final int arity = args.size();
		for (int a : expectedArities) {
			if (a == arity) return;
		}		
		throw new ArityException(args, arity, fnName);
	}
	
	public static void assertMinArity(
			final String fnName, 
			final VncList args, 
			final int minArity
	) {
		final int arity = args.size();
		if (arity < minArity) {
			throw new ArityException(args, arity, fnName);
		}
	}

	public static boolean isJavaIoFile(final VncVal val) {
		return Types.isVncJavaObject(val, File.class);
	}

	public static VncList removeNilValues(final VncList list) {
		return new VncList(removeNilValues(list.getList()));
	}

	public static List<VncVal> removeNilValues(final List<VncVal> items) {
		return items.stream()
					.filter(v -> v != Nil)
					.collect(Collectors.toList());
	}
}
