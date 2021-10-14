/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.AppRunner;
import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.javainterop.LoadPathsFactory;
import com.github.jlangch.venice.util.CapturingPrintStream;
import com.github.jlangch.venice.util.NullInputStream;


public class AppModuleTest {

	@Test
	public void test_base() throws Exception {
		final Venice venice = new Venice();

		final File dir = Files.createTempDirectory("test").toFile();
		final File subdir = new File(dir, "util");
		FileUtil.mkdir(subdir);	
		
		FileUtil.save(
				"(do                                  \n" +
				"  (load-file \"util/def.venice\")    \n" +
				"  (println XXX))                       ",
				new File(dir, "main.venice"),
				true);
		
		FileUtil.save(
				"(def XXX 100)",
				new File(subdir, "def.venice"),
				true);
		
		try {
			final String script =
				"(do                                                                       \n"
			  + "  (load-module :app)                                                      \n"
			  + "  (app/build                                                              \n"
			  + "     \"test\"                                                             \n"
			  + "     \"main.venice\"                                                      \n"
			  + "     { \"main.venice\"      \"" + new File(dir, "main.venice") + "\"      \n"
			  + "       \"util/def.venice\"  \"" + new File(subdir, "def.venice") + "\" }  \n"
			  + "     \"" + dir + "\"))"; 
					
			final Map<?,?> result = (Map<?,?>)venice.eval(script);
			
			final File appArchive = (File)result.get("file");
			
			assertEquals("test", result.get("name"));
			assertEquals("test.zip", appArchive.getName());
			
			assertTrue(new File(dir, "test.zip").exists());
			
			final CapturingPrintStream stdout = new CapturingPrintStream();
			final CapturingPrintStream stderr = new CapturingPrintStream();

			AppRunner.run(
				appArchive, 
				null, 
				LoadPathsFactory.acceptAll(),
				stdout,
				stderr,
				new InputStreamReader(new NullInputStream()));
			
			stdout.flush();
			stderr.flush();
			
			assertEquals("100\n", stdout.getOutput());
			assertEquals("", stderr.getOutput());
		}
		catch(Exception ex) {
			throw ex;
		}
		finally {
			FileUtil.rmdir(dir);
		}
		
		assertFalse(dir.exists());
	}

}
