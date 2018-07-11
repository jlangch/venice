package org.venice;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.venice.impl.CoreFunctions;
import org.venice.impl.Env;
import org.venice.impl.VeniceInterpreter;
import org.venice.impl.types.Constants;
import org.venice.impl.types.VncSymbol;
import org.venice.impl.types.VncVal;


public class BugsTest {

	@Test
	public void test_meta_for_vectors() {	
		// Create a VeniceInterpreter without 'core.vnc' for simpler testing
		final VeniceInterpreter venice = new VeniceInterpreter();
		final Env env = new Env(null);
		
		// Add the 'meta' function
		env.set(new VncSymbol(CoreFunctions.meta.getName()), CoreFunctions.meta);
		
		// Test...
		final VncVal result = venice.RE("(meta [1 2 3])", null, env);		
		assertNotEquals(Constants.Nil, result);
	}

}
