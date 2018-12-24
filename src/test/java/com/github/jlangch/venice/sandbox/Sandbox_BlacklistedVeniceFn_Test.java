/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.sandbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.FileUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Sandbox_BlacklistedVeniceFn_Test {

	
	// ------------------------------------------------------------------------
	// Sandbox FAIL
	// ------------------------------------------------------------------------

	@Test
	public void test_RejectAllInterceptor_slurp() {
		assertThrows(SecurityException.class, () -> {
			// RejectAllInterceptor -> all Venice IO functions blacklisted
			new Venice(new RejectAllInterceptor()).eval("(io/slurp \"/tmp/test\")");
		});
	}
	
	@Test
	public void test_rejectAllVeniceIoFunctions_slurp() {
		// Sandbox::rejectAllVeniceIoFunctions() -> all Venice IO functions blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().rejectAllVeniceIoFunctions());				

		assertThrows(SecurityException.class, () -> {
			new Venice(interceptor).eval("(io/slurp \"/tmp/test\")");
		});
	}
	
	@Test
	public void test_blacklistedIO_slurp() {
		// all Venice IO functions blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().withBlacklistedVeniceFn("*io*"));
	
		assertThrows(SecurityException.class, () -> {
			new Venice(interceptor).eval("(io/slurp \"/tmp/test\")");
		});
	}
	
	@Test
	public void test_withBlacklistedVeniceFn_slurp() {
		// Venice 'slurp' function blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().withBlacklistedVeniceFn("io/slurp"));				
		
		assertThrows(SecurityException.class, () -> {
			new Venice(interceptor).eval("(io/slurp \"/tmp/test\")");
		});
	}

	
	
	// ------------------------------------------------------------------------
	// Sandbox PASS
	// ------------------------------------------------------------------------

	@Test
	public void test_NoSandbox_slurp() {
		// AcceptAllInterceptor -> all Venice IO functions available
		final Venice venice = new Venice();
		
		assertEquals("1234567890", venice.eval("(io/slurp f)", Parameters.of("f", tempFile.getPath())));
	}

	@Test
	public void test_AcceptAllInterceptor_slurp() {
		// AcceptAllInterceptor -> all Venice IO functions available
		final Venice venice = new Venice(new AcceptAllInterceptor());
		
		assertEquals("1234567890", venice.eval("(io/slurp f)", Parameters.of("f", tempFile.getPath())));
	}

	
	
	// ------------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------------
	
	@BeforeEach
	public void createTempFile() {
		try {
			tempFile = File.createTempFile("test__", ".txt");
			FileUtil.save("1234567890", tempFile, true);
		}
		catch(IOException ex) {
			throw new RuntimeException("Failed to create temp file");
		}
	}

	@AfterEach
	public void removeTempFile() {
		if (tempFile != null && tempFile.exists()) {
			tempFile.delete();
			tempFile = null;
		}
	}

	
	private File tempFile;
}
