package com.github.jlangch.venice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;


public class BugsTest {

	@Test
	public void test_0_2_0_meta_for_vectors() {	
		// Create a VeniceInterpreter without 'core.venice' for simpler testing
		final VeniceInterpreter venice = new VeniceInterpreter();
		final Env env = new Env(null);
		
		// Add the 'meta' function
		env.set(new VncSymbol(CoreFunctions.meta.getName()), CoreFunctions.meta);
		
		// Test...
		final VncVal result = venice.RE("(meta [1 2 3])", null, env);		
		assertNotEquals(Constants.Nil, result);
	}

	@Test
	public void test_0_2_0_str_unicode() {
		// Create a VeniceInterpreter without 'core.venice' for simpler testing
		final VeniceInterpreter venice = new VeniceInterpreter();
		final Env env = new Env(null);
		
		// Add the 'meta' function
		env.set(new VncSymbol(CoreFunctions.str.getName()), CoreFunctions.str);
		
		// Test...
		final VncVal result = venice.RE("(str \"\\u0041\\u0042\\u0043\")", null, env);		
		assertEquals("ABC", ((VncString)result).getValue());
	}

}
