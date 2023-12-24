/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.impl.util.loadpath.fn;

public class LoadPaths_file_move_stream_Test {


// Load paths for 'io/move-file' are NOT enabled!!



//    @Test
//    public void test_no_loadpaths() {
//        TempFS.with((tempFS, root) -> {
//            final Venice venice = new Venice();
//
//            assertEquals("res1", venice.eval(
//            						"(do (io/move-file src dst) (io/slurp dst))",
//            						Parameters.of(
//            							"src", new File(root, "res1.txt"),
//            							"dst", new File(root, "tmp/res1.txt"))));
//
//            assertEquals("res4", venice.eval(
//            						"(do (io/move-file src dst) (io/slurp dst))",
//            						Parameters.of(
//            							"src", new File(root, "dir1/11/res4.txt"),
//            							"dst", new File(root, "tmp/res4___.txt"))));
//
//            // non existing source file
//            assertThrows(
//                    VncException.class,
//                    () -> venice.eval(
//    						"(do (io/move-file src dst) (io/slurp dst))",
//    						Parameters.of(
//    							"src", new File(root, "unknown.txt"),
//    							"dst", new File(root, "tmp/res1.txt"))));
//
//            // non existing destination dir
//            assertThrows(
//                    VncException.class,
//                    () -> venice.eval(
//    						"(do (io/move-file src dst) (io/slurp dst))",
//    						Parameters.of(
//    							"src", new File(root, "dir1/res2.txt"),
//    							"dst", new File(root, "unknown/res2.txt"))));
//        });
//    }
//
//    @Test
//    public void test_relative_limited() {
//        TempFS.with((tempFS, root) -> {
//            final Venice venice = new Venice(tempFS.createSandbox(false));
//
//            // inside -> inside
//            assertEquals("res2", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File("res2.txt"),
//										"dst", new File("11/res2__.txt"))));
//
//            // outside -> inside
//            assertThrows(
//            		VncException.class,
//            		() -> venice.eval(
//							"(do (io/move-file src dst) (io/slurp dst))",
//							Parameters.of(
//								"src", new File("dir2/res5.txt"),
//								"dst", new File("11/res5__.txt"))));
//
//            // inside -> outside
//            assertThrows(
//            		VncException.class,
//            		() -> venice.eval(
//							"(do (io/move-file src dst) (io/slurp dst))",
//							Parameters.of(
//								"src", new File("11/res4.txt"),
//								"dst", new File("dir2/res4__.txt"))));
//
//            // outside -> outside
//            assertThrows(
//            		VncException.class,
//            		() -> venice.eval(
//							"(do (io/move-file src dst) (io/slurp dst))",
//							Parameters.of(
//								"src", new File("dir2/res6.txt"),
//								"dst", new File("dir2/res6__.txt"))));
//      });
//    }
//
//    @Test
//    public void test_relative_unlimited() {
//        TempFS.with((tempFS, root) -> {
//            final Venice venice = new Venice(tempFS.createSandbox(true));
//
//
//            // inside -> inside
//            assertEquals("res2", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File("res2.txt"),
//										"dst", new File("11/res2__.txt"))));
//
//            // outside -> inside
//            assertEquals("res5", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File(root, "dir2/res5.txt"),
//										"dst", new File("11/res5__.txt"))));
//
//            // inside -> outside
//            assertEquals("res3", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File("res3.txt"),
//										"dst", new File(root, "dir2/res3__.txt"))));
//
//            // outside -> outside
//            assertEquals("res6", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File(root, "dir2/res6.txt"),
//										"dst", new File(root, "dir2/res6__.txt"))));
//        });
//    }
//
//    @Test
//    public void test_absolute_limited() {
//        TempFS.with((tempFS, root) -> {
//            final Venice venice = new Venice(tempFS.createSandbox(false));
//
//            // inside -> inside
//            assertEquals("res2", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File(root, "dir1/res2.txt"),
//										"dst", new File(root, "dir1/res2__.txt"))));
//
//            // outside -> inside
//            assertThrows(
//            		VncException.class,
//            		() -> venice.eval(
//							"(do (io/move-file src dst) (io/slurp dst))",
//							Parameters.of(
//								"src", new File(root, "dir2/res5.txt"),
//								"dst", new File(root, "dir1/res5__.txt"))));
//
//            // inside -> outside
//            assertThrows(
//            		VncException.class,
//            		() -> venice.eval(
//							"(do (io/move-file src dst) (io/slurp dst))",
//							Parameters.of(
//								"src", new File(root, "dir1/res3.txt"),
//								"dst", new File(root, "dir2/res3__.txt"))));
//
//            // outside -> outside
//            assertThrows(
//            		VncException.class,
//            		() -> venice.eval(
//							"(do (io/move-file src dst) (io/slurp dst))",
//							Parameters.of(
//								"src", new File(root, "dir2/res6.txt"),
//								"dst", new File(root, "dir2/res6__.txt"))));
//      });
//    }
//
//    @Test
//    public void test_absolute_unlimited() {
//        TempFS.with((tempFS, root) -> {
//            final Venice venice = new Venice(tempFS.createSandbox(true));
//
//
//            // inside -> inside
//            assertEquals("res2", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File(root, "dir1/res2.txt"),
//										"dst", new File(root, "dir1/res2__.txt"))));
//
//            // outside -> inside
//            assertEquals("res5", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File(root, "dir2/res5.txt"),
//										"dst", new File(root, "dir1/res5__.txt"))));
//
//            // inside -> outside
//            assertEquals("res3", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File(root, "dir1/res3.txt"),
//										"dst", new File(root, "dir2/res3__.txt"))));
//
//            // outside -> outside
//            assertEquals("res6", venice.eval(
//									"(do (io/move-file src dst) (io/slurp dst))",
//									Parameters.of(
//										"src", new File(root, "dir2/res6.txt"),
//										"dst", new File(root, "dir2/res6__.txt"))));
//        });
//    }

}

