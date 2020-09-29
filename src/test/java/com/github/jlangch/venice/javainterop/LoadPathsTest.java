/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class LoadPathsTest {

	@Test
	public void test_AcceptAllInterceptor_LoadPaths_UnlimitedAccess() {		
		File root = null;
		try {
			root = createFiles();
					
			try {
				final ILoadPaths loadPaths = LoadPathsFactory.of(
												Arrays.asList(
													new File(root, "dir1"),
													new File(root, "dir2")),
												true);
				
				final Venice venice1 = new Venice(new AcceptAllInterceptor(loadPaths));
				assertEquals(true, venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")"));
				assertEquals(true, venice1.eval("(load-file :sum)"));
				assertEquals(true, venice1.eval("(load-file :sub)"));
				
				// not existing files
				try {
					venice1.eval("(load-file \"" + new File(root, "unknown.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :not-existing)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				
				final Venice venice2 = new Venice(new AcceptAllInterceptor(loadPaths));
				assertEquals(5L,  venice2.eval("(do (load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\") (func))"));
				assertEquals(11L, venice2.eval("(do (load-file :sum) (func))"));
				assertEquals(9L,  venice2.eval("(do (load-file :sub) (func))"));
			}
			finally {
				rmDir(root);
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void test_AcceptAllInterceptor_LoadPaths_NoUnlimitedAccess() {		
		File root = null;
		try {
			root = createFiles();
					
			try {
				final ILoadPaths loadPaths = LoadPathsFactory.of(
												Arrays.asList(
													new File(root, "dir1"),
													new File(root, "dir2")),
												false);
				
				final Venice venice1 = new Venice(new AcceptAllInterceptor(loadPaths));
				assertEquals(true, venice1.eval("(load-file :sum)"));
				assertEquals(true, venice1.eval("(load-file :sub)"));
				
				// not existing files
				try {
					venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file \"" + new File(root, "unknown.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :not-existing)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				
				final Venice venice2 = new Venice(new AcceptAllInterceptor(loadPaths));
				assertEquals(11L, venice2.eval("(do (load-file :sum) (func))"));
				assertEquals(9L,  venice2.eval("(do (load-file :sub) (func))"));
			}
			finally {
				rmDir(root);
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Test
	public void test_AcceptAllInterceptor_NoLoadPaths_UnlimitedAccess() {		
		File root = null;
		try {
			root = createFiles();
					
			try {
				final ILoadPaths loadPaths = LoadPathsFactory.of(null, true);
				
				final Venice venice1 = new Venice(new AcceptAllInterceptor(loadPaths));
				assertEquals(true, venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")"));
				
				// not existing files
				try {
					venice1.eval("(load-file \"" + new File(root, "unknown.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :sum)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :not-existing)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				
				final Venice venice2 = new Venice(new AcceptAllInterceptor(loadPaths));
				assertEquals(5L,  venice2.eval("(do (load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\") (func))"));
				}
			finally {
				rmDir(root);
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void test_AcceptAllInterceptor_NoLoadPaths_NoUnlimitedAccess() {		
		File root = null;
		try {
			root = createFiles();
					
			try {
				final ILoadPaths loadPaths = LoadPathsFactory.of(null, false);
				
				final Venice venice1 = new Venice(new AcceptAllInterceptor(loadPaths));
				
				// not existing files
				try {
					venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file \"" + new File(root, "unknown.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :sum)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :not-existing)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
			}
			finally {
				rmDir(root);
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}


	@Test
	public void test_RejectAllInterceptor() {	
		File root = null;
		try {
			root = createFiles();
					
			try {
				LoadPathsFactory.of(null, false);
				
				final Venice venice1 = new Venice(new RejectAllInterceptor());
				
				// not existing files
				try {
					venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (SecurityException ex) { // Access denied to 'load-file'
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file \"" + new File(root, "unknown.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (SecurityException ex) { // Access denied to 'load-file'
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :sum)");
					fail("Expected VncException");
				}
				catch (SecurityException ex) { // Access denied to 'load-file'
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :not-existing)");
					fail("Expected VncException");
				}
				catch (SecurityException ex) { // Access denied to 'load-file'
					assertTrue(true);
				}
			}
			finally {
				rmDir(root);
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	
	@Test
	public void test_SandboxInterceptor_LoadPaths_UnlimitedAccess() {		
		File root = null;
		try {
			root = createFiles();
					
			try {
				final ILoadPaths loadPaths = LoadPathsFactory.of(
												Arrays.asList(
													new File(root, "dir1"),
													new File(root, "dir2")),
												true);
				
				final Venice venice1 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
				assertEquals(true, venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")"));
				assertEquals(true, venice1.eval("(load-file :sum)"));
				assertEquals(true, venice1.eval("(load-file :sub)"));
				
				// not existing files
				try {
					venice1.eval("(load-file \"" + new File(root, "unknown.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :not-existing)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				
				final Venice venice2 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
				assertEquals(5L,  venice2.eval("(do (load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\") (func))"));
				assertEquals(11L, venice2.eval("(do (load-file :sum) (func))"));
				assertEquals(9L,  venice2.eval("(do (load-file :sub) (func))"));
			}
			finally {
				rmDir(root);
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void test_SandboxInterceptor_LoadPaths_NoUnlimitedAccess() {		
		File root = null;
		try {
			root = createFiles();
					
			try {
				final ILoadPaths loadPaths = LoadPathsFactory.of(
												Arrays.asList(
													new File(root, "dir1"),
													new File(root, "dir2")),
												false);
				
				final Venice venice1 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
				assertEquals(true, venice1.eval("(load-file :sum)"));
				assertEquals(true, venice1.eval("(load-file :sub)"));
				
				// not existing files
				try {
					venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file \"" + new File(root, "unknown.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :not-existing)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				
				final Venice venice2 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
				assertEquals(11L, venice2.eval("(do (load-file :sum) (func))"));
				assertEquals(9L,  venice2.eval("(do (load-file :sub) (func))"));
			}
			finally {
				rmDir(root);
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Test
	public void test_SandboxInterceptor_NoLoadPaths_UnlimitedAccess() {		
		File root = null;
		try {
			root = createFiles();
					
			try {
				final ILoadPaths loadPaths = LoadPathsFactory.of(null, true);
				
				final Venice venice1 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
				assertEquals(true, venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")"));
				
				// not existing files
				try {
					venice1.eval("(load-file \"" + new File(root, "unknown.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :sum)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :not-existing)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				
				final Venice venice2 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
				assertEquals(5L,  venice2.eval("(do (load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\") (func))"));
				}
			finally {
				rmDir(root);
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void test_SandboxInterceptor_NoLoadPaths_NoUnlimitedAccess() {		
		File root = null;
		try {
			root = createFiles();
					
			try {
				final ILoadPaths loadPaths = LoadPathsFactory.of(null, false);
				
				final Venice venice1 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
				
				// not existing files
				try {
					venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file \"" + new File(root, "unknown.venice").getAbsolutePath() + "\")");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :sum)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
				try {
					venice1.eval("(load-file :not-existing)");
					fail("Expected VncException");
				}
				catch (VncException ex) {
					assertTrue(true);
				}
			}
			finally {
				rmDir(root);
			}
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	
	
	
	private File createFiles() throws IOException {
		final File root = Files.createTempDirectory("loadpath").toFile();
		final File dir1 = new File(root, "dir1");
		final File dir2 = new File(root, "dir2");
		dir1.mkdir();
		dir2.mkdir();

		// venice files
		final File file10 = new File(root, "div.venice");
		Files.write(file10.toPath(), "(defn func [] (/ 10 2))".getBytes("UTF-8"), StandardOpenOption.CREATE);

		final File file11 = new File(dir1, "sum.venice");
		Files.write(file11.toPath(), "(defn func [] (+ 10 1))".getBytes("UTF-8"), StandardOpenOption.CREATE);

		final File file12 = new File(dir1, "sub.venice");
		Files.write(file12.toPath(), "(defn func [] (- 10 1)))".getBytes("UTF-8"), StandardOpenOption.CREATE);

		// resource files
		final File file20 = new File(root, "res1.txt");
		Files.write(file20.toPath(), "111".getBytes("UTF-8"), StandardOpenOption.CREATE);

		final File file21 = new File(dir1, "res2.txt");
		Files.write(file21.toPath(), "222".getBytes("UTF-8"), StandardOpenOption.CREATE);

		final File file22 = new File(dir1, "res3.txt");
		Files.write(file22.toPath(), "333)".getBytes("UTF-8"), StandardOpenOption.CREATE);
		
		return root;
	}
	
	private void rmDir(final File dir) throws IOException {
		if (dir != null) {
			Files.walk(dir.toPath())
				 .sorted(Comparator.reverseOrder())
				 .map(Path::toFile)
				  .forEach(File::delete);
		}
	}
}

