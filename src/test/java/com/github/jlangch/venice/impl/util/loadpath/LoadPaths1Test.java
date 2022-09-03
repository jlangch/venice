/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.util.loadpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;
import com.github.jlangch.venice.javainterop.LoadPathsFactory;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class LoadPaths1Test {

    @Test
    public void test_valid_LoadPaths() {
        TempFS.with((tempFS, root) -> {
             assertNotNull(
                    LoadPathsFactory.of(
                        Arrays.asList(
                            new File(root, "dir1"),
                            new File(root, "dir2"),
                            new File(root, "res1.txt"),
                            new File(root, "dir1/res2.txt"),
                            new File(root, "dir1/res3.txt")),
                        false));
        });
    }

    @Test
    public void test_invalid_LoadPaths_unknown_dir() {
        TempFS.with((tempFS, root) -> {
            assertThrows(
                    VncException.class,
                    () -> LoadPathsFactory.of(
                            Arrays.asList(new File(root, "dir-unknown")),
                            false));
        });
    }

    @Test
    public void test_invalid_LoadPaths_unknown_file() {
        TempFS.with((tempFS, root) -> {
            assertThrows(
                    VncException.class,
                    () -> LoadPathsFactory.of(
                            Arrays.asList(new File(root, "dir1/file-unknown.zip")),
                            false));
        });
    }

    @Test
    public void test_valid_isOnLoadPath_unlimitedAccess() {
        TempFS.with((tempFS, root) -> {
            final ILoadPaths loadPaths = LoadPathsFactory.of(
                                            Arrays.asList(
                                                new File(root, "dir1"),
                                                new File(root, "dir2"),
                                                new File(root, "res1.txt")),
                                            true); // unlimited access: true

            // absolute files on load path
            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/any.txt"),      Access.Read));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/some/any.txt"), Access.Read));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "res1.txt"),          Access.Read));

            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/any.txt"),      Access.Write));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/some/any.txt"), Access.Write));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "res1.txt"),          Access.Write));

            // absolute files not on load path but unlimited access turned on
            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/../foo/any.txt"),      Access.Read));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/../foo/some/any.txt"), Access.Read));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "/tmp/any.txt"),             Access.Read));

            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/../foo/any.txt"),      Access.Write));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/../foo/some/any.txt"), Access.Write));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "/tmp/any.txt"),             Access.Write));

            // relative files on load path
            assertTrue(loadPaths.isOnLoadPath(new File("any.txt"),      Access.Read));
            assertTrue(loadPaths.isOnLoadPath(new File("some/any.txt"), Access.Read));

            assertTrue(loadPaths.isOnLoadPath(new File("any.txt"),      Access.Write));
            assertTrue(loadPaths.isOnLoadPath(new File("some/any.txt"), Access.Write));

            // relative files not on load path but unlimited access turned on
            assertTrue(loadPaths.isOnLoadPath(new File("../foo/any.txt"),      Access.Read));
            assertTrue(loadPaths.isOnLoadPath(new File("../foo/some/any.txt"), Access.Read));

            assertTrue(loadPaths.isOnLoadPath(new File("../foo/any.txt"),      Access.Write));
            assertTrue(loadPaths.isOnLoadPath(new File("../foo/some/any.txt"), Access.Write));
        });
    }

    @Test
    public void test_valid_isOnLoadPath_limitedAccess() {
        TempFS.with((tempFS, root) -> {
            final ILoadPaths loadPaths = LoadPathsFactory.of(
                                            Arrays.asList(
                                                new File(root, "dir1"),
                                                new File(root, "dir2"),
                                                new File(root, "res1.txt")),
                                            false);  // unlimited access: false

            // absolute files on load path
            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/any.txt"),      Access.Read));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/some/any.txt"), Access.Read));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "res1.txt"),          Access.Read));

            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/any.txt"),      Access.Write));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "dir1/some/any.txt"), Access.Write));
            assertTrue(loadPaths.isOnLoadPath(new File(root, "res1.txt"),          Access.Write));

            // absolute files not on load path
            assertFalse(loadPaths.isOnLoadPath(new File(root, "dir1/../foo/any.txt"),      Access.Read));
            assertFalse(loadPaths.isOnLoadPath(new File(root, "dir1/../foo/some/any.txt"), Access.Read));
            assertFalse(loadPaths.isOnLoadPath(new File("/tmp/any.txt"),                   Access.Read));

            assertFalse(loadPaths.isOnLoadPath(new File(root, "dir1/../foo/any.txt"),      Access.Write));
            assertFalse(loadPaths.isOnLoadPath(new File(root, "dir1/../foo/some/any.txt"), Access.Write));
            assertFalse(loadPaths.isOnLoadPath(new File("/tmp/any.txt"),                   Access.Write));

            // relative files on load path
            assertTrue(loadPaths.isOnLoadPath(new File("any.txt"),      Access.Read));
            assertTrue(loadPaths.isOnLoadPath(new File("some/any.txt"), Access.Read));

            assertTrue(loadPaths.isOnLoadPath(new File("any.txt"),      Access.Write));
            assertTrue(loadPaths.isOnLoadPath(new File("some/any.txt"), Access.Write));

            // relative files not on load path
            assertFalse(loadPaths.isOnLoadPath(new File("../foo/any.txt"),      Access.Read));
            assertFalse(loadPaths.isOnLoadPath(new File("../foo/some/any.txt"), Access.Read));

            assertFalse(loadPaths.isOnLoadPath(new File("../foo/any.txt"),      Access.Write));
            assertFalse(loadPaths.isOnLoadPath(new File("../foo/some/any.txt"), Access.Write));
        });
    }

    @Test
    public void test_valid_LoadPaths_invalid_dir() {
        TempFS.with((tempFS, root) -> {
            assertThrows(
                    VncException.class,
                    () -> LoadPathsFactory.of(
                            Arrays.asList(
                                new File(root, "dir1"),
                                new File(root, "dir-not-exist")),
                            true));
        });
    }

    @Test
    public void test_AcceptAllInterceptor_LoadPaths_UnlimitedAccess() {
        TempFS.with((tempFS, root) -> {
            final ILoadPaths loadPaths = LoadPathsFactory.of(
                                            Arrays.asList(
                                                new File(root, "dir1"),
                                                new File(root, "dir2")),
                                            true);

            final Venice venice1 = new Venice(new AcceptAllInterceptor(loadPaths));
            assertNotNull(venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")"));
            assertNotNull(venice1.eval("(load-file :sum)"));
            assertNotNull(venice1.eval("(load-file :sub)"));

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
        });
    }

    @Test
    public void test_AcceptAllInterceptor_LoadPaths_NoUnlimitedAccess() {
        TempFS.with((tempFS, root) -> {
            final ILoadPaths loadPaths = LoadPathsFactory.of(
                                            Arrays.asList(
                                                new File(root, "dir1"),
                                                new File(root, "dir2")),
                                            false);

            final Venice venice1 = new Venice(new AcceptAllInterceptor(loadPaths));
            assertNotNull(venice1.eval("(load-file :sum)"));
            assertNotNull(venice1.eval("(load-file :sub)"));

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
        });
    }

    @Test
    public void test_AcceptAllInterceptor_NoLoadPaths_UnlimitedAccess() {
        TempFS.with((tempFS, root) -> {
            final ILoadPaths loadPaths = LoadPathsFactory.of(null, true);

            final Venice venice1 = new Venice(new AcceptAllInterceptor(loadPaths));
            assertNotNull(venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")"));

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
        });
    }

    @Test
    public void test_AcceptAllInterceptor_NoLoadPaths_NoUnlimitedAccess() {
        TempFS.with((tempFS, root) -> {
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
        });
    }


    @Test
    public void test_RejectAllInterceptor() {
        TempFS.with((tempFS, root) -> {
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
        });
    }


    @Test
    public void test_SandboxInterceptor_LoadPaths_UnlimitedAccess() {
        TempFS.with((tempFS, root) -> {
            final ILoadPaths loadPaths = LoadPathsFactory.of(
                                            Arrays.asList(
                                                new File(root, "dir1"),
                                                new File(root, "dir2")),
                                            true);

            final Venice venice1 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
            assertNotNull(venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")"));
            assertNotNull(venice1.eval("(load-file :sum)"));
            assertNotNull(venice1.eval("(load-file :sub)"));

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
        });
    }

    @Test
    public void test_SandboxInterceptor_LoadPaths_NoUnlimitedAccess() {
        TempFS.with((tempFS, root) -> {
            final ILoadPaths loadPaths = LoadPathsFactory.of(
                                            Arrays.asList(
                                                new File(root, "dir1"),
                                                new File(root, "dir2")),
                                            false);

            final Venice venice1 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
            assertNotNull(venice1.eval("(load-file :sum)"));
            assertNotNull(venice1.eval("(load-file :sub)"));

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
        });
    }

    @Test
    public void test_SandboxInterceptor_NoLoadPaths_UnlimitedAccess() {
        TempFS.with((tempFS, root) -> {
            final ILoadPaths loadPaths = LoadPathsFactory.of(null, true);

            final Venice venice1 = new Venice(new SandboxInterceptor(new SandboxRules(), loadPaths));
            assertNotNull(venice1.eval("(load-file \"" + new File(root, "div.venice").getAbsolutePath() + "\")"));

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
        });
    }

    @Test
    public void test_SandboxInterceptor_NoLoadPaths_NoUnlimitedAccess() {
        TempFS.with((tempFS, root) -> {
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
        });
    }
}

