package com.github.jlangch.venice.impl.functions;

import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class Functions {

	public static VncFunction getFunction(final String name) {
		return (VncFunction)functions.get(new VncSymbol(name));
	}
	
	public static final Map<VncVal,VncVal> functions = new HashMap<>();
	
	static {
		functions.putAll(CoreFunctions.ns);
		functions.putAll(ModuleFunctions.ns);
		functions.putAll(StringFunctions.ns);
		functions.putAll(RegexFunctions.ns);
		functions.putAll(ArrayFunctions.ns);
		functions.putAll(MathFunctions.ns);
		functions.putAll(IOFunctions.ns);
		functions.putAll(TimeFunctions.ns);
		functions.putAll(ShellFunctions.ns);
		functions.putAll(SystemFunctions.ns);
		functions.putAll(ConcurrencyFunctions.ns);
		functions.putAll(NanoJsonFunctions.ns);
	}

}
