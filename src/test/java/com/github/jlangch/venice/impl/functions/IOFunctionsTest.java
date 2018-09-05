/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package com.github.jlangch.venice.impl.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class IOFunctionsTest {

	@Test
	public void test_io_copy_file() {
		final Venice venice = new Venice();

		try {
			final File from = File.createTempFile("from__", ".txt");
			venice.eval(
					"(io/spit file \"123456789\" :append true)", 
					Parameters.of("file", from));
			
			final File to = File.createTempFile("to__", ".txt");
			to.delete();

			assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", from)));	
			assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", to)));	
			
			venice.eval("(io/copy-file from to))", Parameters.of("from", from, "to", to));	

			assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", from)));	
			assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", to)));	

			venice.eval("(io/delete-file f))", Parameters.of("f", from));	
			venice.eval("(io/delete-file f))", Parameters.of("f", to));	

			assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", from)));	
			assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", to)));	
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Test
	public void test_io_delete_file() {
		final Venice venice = new Venice();

		try {
			final File file = File.createTempFile("spit", ".txt");
			venice.eval(
					"(io/spit file \"123456789\" :append true)", 
					Parameters.of("file", file));

			assertTrue((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", file)));	
			
			venice.eval("(io/delete-file f))", Parameters.of("f", file));	

			assertFalse((Boolean)venice.eval("(io/exists-file? f))", Parameters.of("f", file)));	
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Test
	public void test_io_exists_dir_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(io/exists-dir? (io/user-dir))"));	
	}

	@Test
	public void test_io_file() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(io/file? (io/file \"/tmp\"))"));	
		assertTrue((Boolean)venice.eval("(io/file? (io/file \"/tmp\" \"a.txt\"))"));	
		assertTrue((Boolean)venice.eval("(io/file? (io/file (io/file \"/tmp\") \"a.txt\"))"));	
	}
	
	@Test
	public void test_io_file_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(io/file? (io/file \"/tmp\"))"));	
	}
	
	@Test
	public void test_io_list_files() throws Exception{
		final Venice venice = new Venice();

		final File file1 = File.createTempFile("spit-", "-1.txt");
		final File file2 = File.createTempFile("spit-", "-2.txt");

		try {
			venice.eval("(io/spit file \"123\" :append true)", Parameters.of("file", file1));
			venice.eval("(io/spit file \"123\" :append true)", Parameters.of("file", file2));

			final File dir = file1.getParentFile();
			
			assertTrue(
					((Long)venice.eval(
							"(count (io/list-files dir))", 
							Parameters.of("dir", dir))
					).longValue() > 2);
			
			assertEquals(Long.valueOf(2), 
					venice.eval(
							"(count " +
							"  (io/list-files " +
							"         dir " +
							"         (fn [f] (match (get f :name) \"spit-.*[.]txt\"))))", 
							Parameters.of("dir", dir)));
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		finally {			
			Files.list(file1.getParentFile().toPath())
				 .filter(f -> f.toFile().isFile())
				 .filter(f -> f.toFile().getName().matches("spit.*[.]txt"))
				 .forEach(f -> f.toFile().delete());
		}
	}
	
	@Test
	public void test_io_tmp_dir() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(not-empty? (io/tmp-dir))"));	
		assertTrue((Boolean)venice.eval("(io/file? (io/tmp-dir))"));	
	}
	
	@Test
	public void test_io_spit_temp_file() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                             " +
				"   (let [file (io/temp-file \"xchart-\", \".chart\")]           " +
				"        (io/spit-temp-file file \"123456789\" :append true)     " +
				"        (io/slurp file :binary false :remove true))             " +
				") ";
				
				assertEquals("123456789",venice.eval(script));					
	}	
	
	@Test
	public void test_io_slurp_temp_file() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                             " +
				"   (let [file (io/temp-file \"xchart-\", \".chart\")]           " +
				"        (io/spit file \"123456789\" :append true)               " +
				"        (io/slurp-temp-file file :binary false :remove true))   " +
				") ";
				
				assertEquals("123456789",venice.eval(script));					
	}	
	
	@Test
	public void test_io_spit() {
		final Venice venice = new Venice();
		
		// with default encoding
		try {
			final File file = File.createTempFile("spit", ".txt");
			try {				
				venice.eval(
						"(io/spit file \"123456789\" :append true)", 
						Parameters.of("file", file.getAbsolutePath()));
				
				assertEquals(
						"123456789", 
						venice.eval(
								"(io/slurp file)", 
								Parameters.of("file", file.getAbsolutePath())));					
			}
			finally {
				file.delete();
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
		// with UTF-8 encoding
		try {
			final File file = File.createTempFile("spit", ".txt");
			try {				
				venice.eval(
						"(io/spit file \"123456789\" :append true :encoding \"UTF-8\")", 
						Parameters.of("file", file.getAbsolutePath()));
				
				assertEquals(
						"123456789", 
						venice.eval(
								"(io/slurp file :encoding \"UTF-8\")", 
								Parameters.of("file", file.getAbsolutePath())));					
			}
			finally {
				file.delete();
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Test
	public void test_io_slurp() {
		final Venice venice = new Venice();
		
		// with default encoding
		try {
			final File file = File.createTempFile("slurp", ".txt");
			try {
				Files.write(file.toPath(), "123456789".getBytes("UTF-8"), StandardOpenOption.APPEND);
				
				assertEquals(
						"123456789", 
						venice.eval(
								"(io/slurp file)", 
								Parameters.of("file", file.getAbsolutePath())));					
			}
			finally {
				file.delete();
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
		// with UTF-8 encoding
		try {
			final File file = File.createTempFile("slurp", ".txt");
			try {
				Files.write(file.toPath(), "123456789".getBytes("UTF-8"), StandardOpenOption.APPEND);
				
				assertEquals(
						"123456789", 
						venice.eval(
								"(io/slurp file :encoding \"UTF-8\")", 
								Parameters.of("file", file.getAbsolutePath())));					
			}
			finally {
				file.delete();
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Test
	public void test_io_slurp_stream() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                        " +
				"   (import :java.io.FileInputStream)                       " +
				"   (let [file (io/temp-file \"test-\", \".txt\")]          " +
				"        (io/spit file \"123456789\" :append true)          " +
				"        (io/delete-file-on-exit file)                      " +
				"        (try-with [is (. :FileInputStream :new file)]      " +
				"           (io/slurp-stream is :binary false)))            " +
				")";
				
				assertEquals("123456789",venice.eval(script));					
	}
	
	@Test
	public void test_io_spit_stream() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                           " +
				"   (import :java.io.FileOutputStream)                         " +
				"   (let [file (io/temp-file \"test-\", \".txt\")]             " +
				"        (io/delete-file-on-exit file)                         " +
				"        (try-with [is (. :FileOutputStream :new file)]        " +
				"           (io/spit-stream is \"123456789\" :flush true))     " +
				"        (io/slurp-temp-file file :binary false))              " +
				")";
				
				assertEquals("123456789",venice.eval(script));					
	}

	@Test
	public void test_io_user_dir() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(not-empty? (io/user-dir))"));	
		assertTrue((Boolean)venice.eval("(io/file? (io/user-dir))"));	
	}

	@Test
	public void test_io_mime_type() {
		final Venice venice = new Venice();

		assertEquals("application/pdf",venice.eval("(io/mime-type \"document.pdf\")"));	
		assertEquals("application/pdf",venice.eval("(io/mime-type (io/file \"document.pdf\"))"));	
	}
	
}
