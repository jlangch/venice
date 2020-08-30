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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertMinArity;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.VncBoolean.False;
import static com.github.jlangch.venice.impl.types.VncBoolean.True;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ClassPathResource;
import com.github.jlangch.venice.impl.util.IOStreamUtil;
import com.github.jlangch.venice.impl.util.MimeTypes;


public class IOFunctions {


	///////////////////////////////////////////////////////////////////////////
	// I/O functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction io_file =
		new VncFunction(
				"io/file",
				VncFunction
					.meta()
					.arglists(
						"(io/file path)",
						"(io/file parent child)",
						"(io/file parent child & children)")
					.doc(
						"Returns a java.io.File from file path, or from a parent path " +
						"and one or multiple children. The path and parent may be a file or a string " +
						"(file path), child and children must be strings.")
					.examples(
						"(io/file \"/temp/test.txt\")",
						"(io/file \"/temp\" \"test.txt\")",
						"(io/file \"/temp\" \"test\" \"test.txt\")",
						"(io/file (io/file \"/temp\") \"test\" \"test.txt\")",
						"(io/file (. :java.io.File :new \"/temp/test.txt\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/file", args, 1);

				if (args.size() == 1) {
					return new VncJavaObject(
									convertToFile(
											args.first(),
											"Function 'io/file' does not allow %s as path"));
				}
				else {
					final File parent = convertToFile(
											args.first(),
											"Function 'io/file' does not allow %s as parent");

					final List<VncVal> children = args.rest().getList();
					File file = parent;
					for(VncVal child : children) {
						file = new File(file, Coerce.toVncString(child).getValue());
					}

					return new VncJavaObject(file);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_size =
		new VncFunction(
				"io/file-size",
				VncFunction
					.meta()
					.arglists("(io/file-size f)")
					.doc("Returns the size of the file f. f must be a file or a string (file path).")
					.examples("(io/file-size \"/bin/sh\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-size", args, 1);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/file-size");

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-size' does not allow %s as f");

				validateReadableFile(f);

				return new VncLong(f.length());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_path =
		new VncFunction(
				"io/file-path",
				VncFunction
					.meta()
					.arglists("(io/file-path f)")
					.doc("Returns the path of the file f as a string. f must be a file or a string (file path).")
					.examples("(io/file-path (io/file \"/tmp/test/x.txt\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-path", args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-path' does not allow %s as f");

				return new VncString(f.getPath());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_canonical_path =
		new VncFunction(
				"io/file-canonical-path",
				VncFunction
					.meta()
					.arglists("(io/file-canonical-path f)")
					.doc("Returns the canonical path of the file f. f must be a file or a string (file path).")
					.examples("(io/file-canonical-path (io/file \"/tmp/test/../x.txt\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-canonical-path", args, 1);

				try {
					final File f = convertToFile(
										args.first(),
										"Function 'io/file-canonical-path' does not allow %s as f");

					return new VncString(f.getCanonicalPath());
				}
				catch(IOException ex) {
					throw new VncException("Failed to get canonical file path", ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_absolute_path =
		new VncFunction(
				"io/file-absolute-path",
				VncFunction
					.meta()
					.arglists("(io/file-absolute-path f)")
					.doc("Returns the absolute path of the file f. f must be a file or a string (file path).")
					.examples("(io/file-absolute-path (io/file \"/tmp/test/x.txt\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-absolute-path", args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-absolute-path' does not allow %s as f");

				return new VncString(f.getAbsolutePath());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_parent =
		new VncFunction(
				"io/file-parent",
				VncFunction
					.meta()
					.arglists("(io/file-parent f)")
					.doc("Returns the parent file of the file f. f must be a file or a string (file path).")
					.examples("(io/file-path (io/file-parent (io/file \"/tmp/test/x.txt\")))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-parent", args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-parent' does not allow %s as f");

				final File parent = f.getParentFile();
				return parent == null ? Nil : new VncJavaObject(parent);
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_name =
		new VncFunction(
				"io/file-name",
				VncFunction
					.meta()
					.arglists("(io/file-name f)")
					.doc("Returns the name of the file f as a string. f must be a file or a string (file path).")
					.examples("(io/file-name (io/file \"/tmp/test/x.txt\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-name", args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-name' does not allow %s as f");

				return new VncString(f.getName());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_ext_Q =
		new VncFunction(
				"io/file-ext?",
				VncFunction
					.meta()
					.arglists(
						"(io/file-ext? f ext)")
					.doc(
						"Returns true if the file f hast the extension ext. " +
						"f must be a file or a string (file path).")
					.examples(
						"(io/file-ext? \"/tmp/test/x.txt\" \"txt\")",
						"(io/file-ext? (io/file \"/tmp/test/x.txt\") \".txt\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-ext?", args, 2);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-ext?' does not allow %s as f");

				final String ext = Coerce.toVncString(args.second()).getValue();
				return VncBoolean.of(f.getName().endsWith(ext.startsWith(".") ? ext : "." + ext));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_Q =
		new VncFunction(
				"io/file?",
				VncFunction
					.meta()
					.arglists("(io/file? x)")
					.doc("Returns true if x is a java.io.File.")
					.examples("(io/file? (io/file \"/temp/test.txt\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file?", args, 1);

				return VncBoolean.of(Types.isVncJavaObject(args.first(), File.class));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_exists_file_Q =
		new VncFunction(
				"io/exists-file?",
				VncFunction
					.meta()
					.arglists("(io/exists-file? f)")
					.doc("Returns true if the file f exists. f must be a file or a string (file path).")
					.examples("(io/exists-file? \"/temp/test.txt\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/exists-file?", args, 1);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/exists-file?");

				final File f = convertToFile(
									args.first(),
									"Function 'io/exists-file?' does not allow %s as x");

				return VncBoolean.of(f.isFile());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_exists_dir_Q =
		new VncFunction(
				"io/exists-dir?",
				VncFunction
					.meta()
					.arglists("(io/exists-dir? f)")
					.doc(
						"Returns true if the file f exists and is a directory. " +
						"f must be a file or a string (file path).")
					.examples("(io/exists-dir? (io/file \"/temp\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/exists-dir?", args, 1);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/exists-dir?");

				final File f = convertToFile(
									args.first(),
									"Function 'io/exists-dir?' does not allow %s as f");

				return VncBoolean.of(f.isDirectory());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_can_read_Q =
		new VncFunction(
				"io/file-can-read?",
				VncFunction
					.meta()
					.arglists("(io/file-can-read? f)")
					.doc(
						"Returns true if the file or directory f exists and can be read. " +
						"f must be a file or a string (file path).")
					.examples("(io/file-can-read? \"/temp/test.txt\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-can-read?", args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-can-read?' does not allow %s as x");

				return VncBoolean.of((f.isFile() || f.isDirectory()) && f.canRead());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_can_write_Q =
		new VncFunction(
				"io/file-can-write?",
				VncFunction
					.meta()
					.arglists("(io/file-can-write? f)")
					.doc(
						"Returns true if the file or directory f exists and can be written. " +
						"f must be a file or a string (file path).")
					.examples("(io/file-can-write? \"/temp/test.txt\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-can-write?", args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-can-write?' does not allow %s as x");

				return VncBoolean.of((f.isFile() || f.isDirectory()) && f.canWrite());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_can_execute_Q =
		new VncFunction(
				"io/file-can-execute?",
				VncFunction
					.meta()
					.arglists("(io/file-can-execute? f)")
					.doc(
						"Returns true if the file or directory f exists and can be executed. " +
						"f must be a file or a string (file path).")
					.examples("(io/file-can-execute? \"/temp/test.txt\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-can-execute?", args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-can-execute?' does not allow %s as x");

				return VncBoolean.of((f.isFile() || f.isDirectory()) && f.canExecute());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_hidden_Q =
		new VncFunction(
				"io/file-hidden?",
				VncFunction
					.meta()
					.arglists("(io/file-hidden? f)")
					.doc(
						"Returns true if the file or directory f exists and is hidden. " +
						"f must be a file or a string (file path).")
					.examples("(io/file-hidden? \"/temp/test.txt\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/file-hidden?", args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-hidden?' does not allow %s as x");

				return VncBoolean.of((f.isFile() || f.isDirectory()) && f.isHidden());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_delete_file =
		new VncFunction(
				"io/delete-file",
				VncFunction
					.meta()
					.arglists("(io/delete-file f & files)")
					.doc(
						"Deletes one or multiple files. Silently skips delete if the file " +
						"does not exist. If f is a directory the directory must be empty. " +
						"f must be a file or a string (file path)")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/delete-file", args, 0);

				JavaInterop.getInterceptor().validateVeniceFunction("io/delete-file");

				args.forEach(f -> {
					try {
						final File file = convertToFile(
											f,
											"Function 'io/delete-file' does not allow %s as f");

						Files.deleteIfExists(file.toPath());
					}
					catch(Exception ex) {
						throw new VncException(
								String.format("Failed to delete file %s", f.toString()),
								ex);
					}
				});

				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_delete_file_tree =
		new VncFunction(
				"io/delete-file-tree",
				VncFunction
					.meta()
					.arglists("(io/delete-file-tree f & files)")
					.doc(
						"Deletes a file or a directory with all its content. Silently skips delete if " +
						"the file or directory does not exist. f must be a file or a string (file path)")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/delete-file-tree", args, 1);

				JavaInterop.getInterceptor().validateVeniceFunction("io/delete-file-tree");

				args.forEach(f -> {
					final File file = convertToFile(
										f,
										"Function 'io/delete-file-tree' does not allow %s as f");

					if (file.isDirectory()) {
						try {
						    Files.walk(file.toPath())
						    	 .sorted(Comparator.reverseOrder())
						    	 .map(Path::toFile)
						    	 .forEach(File::delete);						
						}
						catch(Exception ex) {
							throw new VncException(
									String.format("Failed to delete dir %s", file.toString()),
									ex);
						}
					}
					else if (file.isFile()) {
						file.delete();
					}
					else {
						// ignore
					}						
				});

				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_delete_file_on_exit =
		new VncFunction(
				"io/delete-file-on-exit",
				VncFunction
					.meta()
					.arglists("(io/delete-file-on-exit f)")
					.doc("Deletes a file f on JVM exit. f must be a file or a string (file path).")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/delete-file-on-exit", args, 1);

				JavaInterop.getInterceptor().validateVeniceFunction("io/delete-file-on-exit");

				final File file = convertToFile(
									args.first(),
									"Function 'io/delete-file-on-exit' does not allow %s as f");

				validateReadableFile(file);

				try {
					file.deleteOnExit();
				}
				catch(Exception ex) {
					throw new VncException(
							String.format(
									"Failed to mark file %s to be deleted on exit",
									file.getPath()),
							ex);
				}

				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_list_files =
		new VncFunction(
				"io/list-files",
				VncFunction
					.meta()
					.arglists(
						"(io/list-files dir)",
						"(io/list-files dir filter-fn)")
					.doc(
						"Lists files in a directory. dir must be a file or a string (file path). " +
						"filter-fn is an optional filter that filters the files found. The filter " +
						"gets a java.io.File as argument. Returns files as java.io.File.\n\n" +
						"(io/list-files \"/tmp\") \n" +
						"(io/list-files \"/tmp\" #(io/file-ext? % \".log\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/list-files", args, 1, 2);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/list-files");

				final File dir = convertToFile(
									args.first(),
									"Function 'io/list-files' does not allow %s as dir");

				validateReadableDirectory(dir);

				try {
					final VncFunction filterFn = (args.size() == 2) ? Coerce.toVncFunction(args.second()) : null;

					final List<VncVal> files = new ArrayList<>();
					for(File f : dir.listFiles()) {
						if (filterFn == null || VncBoolean.isTrue(filterFn.apply(VncList.of(new VncJavaObject(f))))) {
							files.add(new VncJavaObject(f));
						}
					}

					return VncList.ofList(files);
				}
				catch(Exception ex) {
					throw new VncException(
							String.format("Failed to list files %s", dir.getPath()),
							ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_list_file_tree =
		new VncFunction(
				"io/list-file-tree",
				VncFunction
					.meta()
					.arglists(
						"(io/list-file-tree dir)",
						"(io/list-file-tree dir filter-fn)")
					.doc(
						"Lists all files in a directory tree. dir must be a file or a " +
						"string (file path). filter-fn is an optional filter that filters " + 
						"the files found. The filter gets a java.io.File as argument. " +
						"Returns files as java.io.File.\n\n" +
						"(io/list-file-tree \"/tmp\") \n" +
						"(io/list-file-tree \"/tmp\" #(io/file-ext? % \".log\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/list-file-tree", args, 1, 2);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/list-file-tree");

				final File dir = convertToFile(
									args.first(),
									"Function 'io/list-file-tree' does not allow %s as dir");

				validateReadableDirectory(dir);

				try {
					final VncFunction filterFn = (args.size() == 2) ? Coerce.toVncFunction(args.second()) : null;

					final List<VncVal> files = new ArrayList<>();
				    Files.walk(dir.toPath())
				    	 .map(Path::toFile)
				    	 .forEach(f -> {
							if (filterFn == null || VncBoolean.isTrue(filterFn.apply(VncList.of(new VncJavaObject(f))))) {
								files.add(new VncJavaObject(f));
							}
				    	 });						

					return VncList.ofList(files);
				}
				catch(Exception ex) {
					throw new VncException(
							String.format("Failed to list files %s", dir.getPath()),
							ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_list_files_glob_pattern =
		new VncFunction(
				"io/list-files-glob",
				VncFunction
					.meta()
					.arglists("(io/list-files-glob dir glob)")
					.doc(
						"Lists all files in a directory that match the glob pattern. " +
					    "dir must be a file or a string (file path). " +
					    "Returns files as java.io.File.\n\n" +
						"(io/list-files-glob \".\" \"sample*.txt\".")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/list-files-glob", args, 2);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/list-files-glob");

				final File dir = convertToFile(
									args.first(),
									"Function 'io/list-files-glob' does not allow %s as dir");

				final String glob = Coerce.toVncString(args.second()).getValue();
				
				validateReadableDirectory(dir);

				try {
					final List<VncVal> files = new ArrayList<>();
					
					try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath(), glob)) {
				        dirStream.forEach(path -> files.add(new VncJavaObject(path.toFile())));
				    }

					return VncList.ofList(files);
				}
				catch(Exception ex) {
					throw new VncException(
							String.format("Failed to list files %s", dir.getPath()),
							ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

		
	public static VncFunction io_copy_file =
		new VncFunction(
				"io/copy-file",
				VncFunction
					.meta()
					.arglists("(io/copy-file source dest & options)")
					.doc(
						"Copies source to dest. Returns nil or throws IOException. " +
						"Source must be a file or a string (file path), dest must be a file, " +
						"a string (file path), or an OutputStream.\n\n" +
						"Options: \n" +
						"  :replace true/false - e.g if true replace an aexistiong file, defaults to false")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/copy-file", args, 2);

				JavaInterop.getInterceptor().validateVeniceFunction("io/copy-file");

				final VncHashMap options = VncHashMap.ofAll(args.rest().rest());
				final VncVal replaceOpt = options.get(new VncKeyword("replace"));

				final File source = convertToFile(
										args.first(),
										"Function 'io/copy-file' does not allow %s as source");

				validateReadableFile(source);

				final VncVal destVal = args.second();

				if (Types.isVncString(destVal) || Types.isVncJavaObject(destVal, File.class)) {
					final File dest = Types.isVncString(destVal)
										? new File(Coerce.toVncString(destVal).getValue())
										: Coerce.toVncJavaObject(destVal, File.class);

					final List<CopyOption> copyOptions = new ArrayList<>();
					if (VncBoolean.isTrue(replaceOpt)) {
						copyOptions.add(StandardCopyOption.REPLACE_EXISTING);
					}
					
					try {
						if (dest.isDirectory()) {
							Files.copy(
								source.toPath(), 
								dest.toPath().resolve(source.getName()), 
								copyOptions.toArray(new CopyOption[0]));
						}
						else {
							Files.copy(
								source.toPath(), 
								dest.toPath(), 
								copyOptions.toArray(new CopyOption[0]));
						}
					}
					catch(Exception ex) {
						throw new VncException(
								String.format(
										"Failed to copy file %s to %s",
										source.getPath(), dest.getPath()),
								ex);
					}
				}
				else if (Types.isVncJavaObject(destVal, OutputStream.class)) {
					final OutputStream os = (OutputStream)((VncJavaObject)destVal).getDelegate();

					try {
						IOStreamUtil.copyFileToOS(source, os);
					}
					catch(Exception ex) {
						throw new VncException(
								String.format(
										"Failed to copy file %s to stream",
										source.getPath()),
								ex);
					}
				}
				else {
					throw new VncException(String.format(
							"Function 'io/copy-file' does not allow %s as dest",
							Types.getType(destVal)));
				}

				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_move_file =
		new VncFunction(
				"io/move-file",
				VncFunction
					.meta()
					.arglists("(io/move-file source target)")
					.doc(
						"Moves source to target. Returns nil or throws IOException. " +
						"Source and target must be a file or a string (file path).")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/move-file", args, 2);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/move-file");

				final File from = convertToFile(
									args.first(),
									"Function 'io/move-file' does not allow %s as source");

				final File to = convertToFile(
									args.second(),
									"Function 'io/move-file' does not allow %s as target");

				validateReadableFile(from);

				try {
					Files.move(from.toPath(), to.toPath());
				}
				catch(Exception ex) {
					throw new VncException(
							String.format(
								"Failed to move file %s to %s", from.getPath(), to.getPath()),
							ex);
				}

				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_mkdir =
		new VncFunction(
				"io/mkdir",
				VncFunction
					.meta()
					.arglists("(io/mkdir dir)")
					.doc("Creates the directory. dir must be a file or a string (file path).")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/mkdir", args, 1);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/mkdir");

				final File dir = convertToFile(
									args.first(),
									"Function 'io/mkdir' does not allow %s as dir");

				try {
					dir.mkdir();
				}
				catch(Exception ex) {
					throw new VncException(
							String.format("Failed to create dir %s", dir.getPath()),
							ex);
				}

				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_mkdirs =
		new VncFunction(
				"io/mkdirs",
				VncFunction
					.meta()
					.arglists("(io/mkdirs dir)")
					.doc(
						"Creates the directory including any necessary but nonexistent " +
						"parent directories. dir must be a file or a string (file path).")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/mkdirs", args, 1);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/mkdirs");

				final File dir = convertToFile(
									args.first(),
									"Function 'io/mkdirs' does not allow %s as dir");

				try {
					dir.mkdirs();
				}
				catch(Exception ex) {
					throw new VncException(
							String.format("Failed to create dir %s",  dir.getPath()),
							ex);
				}

				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_tmp_dir =
		new VncFunction(
				"io/tmp-dir",
				VncFunction
					.meta()
					.arglists("(io/tmp-dir)")
					.doc("Returns the tmp dir as a java.io.File.")
					.examples("(io/tmp-dir )")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/tmp-dir", args, 0);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/tmp-dir");

				return new VncJavaObject(new File(System.getProperty("java.io.tmpdir")));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_user_dir =
		new VncFunction(
				"io/user-dir",
				VncFunction
					.meta()
					.arglists("(io/user-dir)")
					.doc("Returns the user dir (current working dir) as a java.io.File.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/user-dir", args, 0);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/user-dir");

				return new VncJavaObject(new File(System.getProperty("user.dir")));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_user_home_dir =
		new VncFunction(
				"io/user-home-dir",
				VncFunction
					.meta()
					.arglists("(io/user-home-dir)")
					.doc("Returns the user's home dir as a java.io.File.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/user-home-dir", args, 0);

				JavaInterop.getInterceptor().validateVeniceFunction("io/user-home-dir");

				return new VncJavaObject(new File(System.getProperty("user.home")));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_slurp_lines =
		new VncFunction(
				"io/slurp-lines",
				VncFunction
					.meta()
					.arglists("(io/slurp-lines f & options)")
					.doc(
						"Read all lines from f. f may be a file, a string file path, " +
						"a Java InputStream, or a Java Reader. \n\n" +
						"Options: \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/slurp-lines", args, 1);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/slurp-lines");

				try {
					final VncVal arg = args.first();

					final VncHashMap options = VncHashMap.ofAll(args.rest());

					if (Types.isVncString(arg) || Types.isVncJavaObject(arg, File.class)) {
						final File file = Types.isVncString(arg)
											? new File(((VncString)arg).getValue())
											:  (File)(Coerce.toVncJavaObject(args.first()).getDelegate());

						validateReadableFile(file);

						final VncVal encVal = options.get(new VncKeyword("encoding"));
						final String encoding = encoding(encVal);

						final List<VncString> lines =
								Files.readAllLines(file.toPath(), Charset.forName(encoding))
									 .stream()
									 .map(s -> new VncString(s))
									 .collect(Collectors.toList());

						return VncList.ofList(lines);
					}
					else if (Types.isVncJavaObject(arg, InputStream.class)) {
						final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());

						final VncVal encVal = options.get(new VncKeyword("encoding"));
						final String encoding = encoding(encVal);

						try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, encoding))) {
							return VncList.ofList(rd.lines().map(s -> new VncString(s)).collect(Collectors.toList()));
						}
					}
					else if (Types.isVncJavaObject(arg, Reader.class)) {
						final Reader rd = (Reader)(Coerce.toVncJavaObject(args.first()).getDelegate());
												
						try (BufferedReader brd = new BufferedReader(rd)) {
							return VncList.ofList(brd.lines().map(s -> new VncString(s)).collect(Collectors.toList()));
						}
					}
					else {
						throw new VncException(String.format(
								"Function 'io/slurp-lines' does not allow %s as f",
								Types.getType(args.first())));
					}
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_slurp =
		new VncFunction(
				"io/slurp",
				VncFunction
					.meta()
					.arglists("(io/slurp f & options)")
					.doc(
						"Reads the content of file f as text (string) or binary (bytebuf). " +
						"f may be a file, a string file path, a Java InputStream, " +
						"or a Java Reader. \n\n" +
						"Options: \n" +
						"  :binary true/false - e.g :binary true, defaults to false \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/slurp", args, 1);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/slurp");

				try {
					final VncVal arg = args.first();

					final VncHashMap options = VncHashMap.ofAll(args.rest());
					final VncVal binary = options.get(new VncKeyword("binary"));

					if (Types.isVncString(arg) || Types.isVncJavaObject(arg, File.class)) {
						final File file = Types.isVncString(arg)
											? new File(((VncString)arg).getValue())
											:  (File)(Coerce.toVncJavaObject(args.first()).getDelegate());

						validateReadableFile(file);

						if (VncBoolean.isTrue(binary)) {
							final byte[] data = Files.readAllBytes(file.toPath());
							return new VncByteBuffer(ByteBuffer.wrap(data));
						}
						else {
							final VncVal encVal = options.get(new VncKeyword("encoding"));
							final String encoding = encoding(encVal);

							final byte[] data = Files.readAllBytes(file.toPath());

							return new VncString(new String(data, encoding));
						}
					}
					else if (Types.isVncJavaObject(arg, InputStream.class)) {
						final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());

						if (VncBoolean.isTrue(binary)) {
							final byte[] data = IOStreamUtil.copyIStoByteArray(is);
							return data == null ? Nil : new VncByteBuffer(ByteBuffer.wrap(data));
						}
						else {
							final VncVal encVal = options.get(new VncKeyword("encoding"));
							final String encoding = encoding(encVal);

							return new VncString(IOStreamUtil.copyIStoString(is, encoding));
						}
					}
					else if (Types.isVncJavaObject(arg, Reader.class)) {
						final Reader rd = (Reader)(Coerce.toVncJavaObject(args.first()).getDelegate());
						
						try (BufferedReader brd = new BufferedReader(rd)) {
							final String s = brd.lines().collect(Collectors.joining(System.lineSeparator()));
	
							if (VncBoolean.isTrue(binary)) {
								final VncVal encVal = options.get(new VncKeyword("encoding"));
								final String encoding = encoding(encVal);
	
								return new VncByteBuffer(s.getBytes(encoding));
							}
							else {
								return new VncString(s);
							}
						}
					}
					else {
						throw new VncException(String.format(
								"Function 'io/slurp' does not allow %s as f",
								Types.getType(args.first())));
					}
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_spit =
		new VncFunction(
				"io/spit",
				VncFunction
					.meta()
					.arglists("(io/spit f content & options)")
					.doc(
						"Opens file f, writes content, and then closes f. " +
						"f may be a file or a string (file path). " +
						"The content may be a string or a bytebuf.\n\n" +
						"Options: \n" +
						"  :append true/false - e.g :append true, defaults to false \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/spit", args, 2);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/spit");

				try {
					final File file = convertToFile(
										args.first(),
										"Function 'io/spit' does not allow %s as f");


					final VncVal content = args.second();

					final VncHashMap options = VncHashMap.ofAll(args.slice(2));
					final VncVal append = options.get(new VncKeyword("append"));
					final VncVal encVal = options.get(new VncKeyword("encoding"));
					final String encoding = encoding(encVal);

					byte[] data;

					if (Types.isVncString(content)) {
						data = ((VncString)content).getValue().getBytes(encoding);
					}
					else if (Types.isVncByteBuffer(content)) {
						data = ((VncByteBuffer)content).getBytes();
					}
					else {
						throw new VncException(String.format(
								"Function 'io/spit' does not allow %s as content",
								Types.getType(content)));
					}

					final List<OpenOption> openOptions = new ArrayList<>();
					openOptions.add(StandardOpenOption.CREATE);
					openOptions.add(StandardOpenOption.WRITE);
					openOptions.add(VncBoolean.isTrue(append) 
										? StandardOpenOption.APPEND 
										: StandardOpenOption.TRUNCATE_EXISTING);

					Files.write(
							file.toPath(),
							data,
							openOptions.toArray(new OpenOption[0]));

					return Nil;
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_download =
		new VncFunction(
				"io/download",
				VncFunction
					.meta()
					.arglists("(io/download uri & options)")
					.doc(
						"Downloads the content from the uri and reads it as text (string) " +
						"or binary (bytebuf). \n\n" +
						"Options: \n" +
						"  :binary true/false - e.g :binary true, defaults to false \n" +
						"  :user-agent agent  - e.g :user-agent \"Mozilla\", defaults to nil \n" +
						"  :encoding enc      - e.g :encoding :utf-8, defaults to :utf-8\n" +
						"  :conn-timeout val  - e.g :conn-timeout 10000, \n" +
						"                           connection timeout in milli seconds. \n" +
						"                           0 is interpreted as an infinite timeout. \n" +
						"  :read-timeout val  - e.g :read-timeout 10000, \n" +
						"                           read timeout in milli seconds. \n" +
						"                           0 is interpreted as an infinite timeout. \n" +
						"  :progress-fn fn    - a progress function that takes 2 args \n" +
						"                           [1] progress (0..100%) \n" +
						"                           [2] status {:start :progress :end :failed}\n\n" +
						"If the server returns a 403 (access denied) sending a user-agent\n" +
						"may fool the website.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/download", args, 1);

				JavaInterop.getInterceptor().validateVeniceFunction("io/download");

				final String uri = Coerce.toVncString(args.first()).getValue();

				try {
					final VncHashMap options = VncHashMap.ofAll(args.rest());
					final VncVal binary = options.get(new VncKeyword("binary"));
					final VncVal useragent = options.get(new VncKeyword("user-agent"));
					final VncVal encVal = options.get(new VncKeyword("encoding"));
					final VncVal progressVal = options.get(new VncKeyword("progress-fn"));
					final VncVal connTimeoutMillisVal = options.get(new VncKeyword("conn-timeout"));
					final VncVal readTimeoutMillisVal = options.get(new VncKeyword("read-timeout"));
					
					final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();
					final VncFunction progressFn = progressVal == Nil 
										? new VncFunction("io/progress-default") {
											private static final long serialVersionUID = 1L;
											public VncVal apply(final VncList args) { return Nil; }
										  }
										: Coerce.toVncFunction(progressVal);
					
					final URLConnection conn = (URLConnection)new URL(uri).openConnection();
					if (Types.isVncString(useragent)) {
						conn.addRequestProperty("User-Agent", ((VncString)useragent).getValue());
					}

					if (connTimeoutMillisVal != Nil) {
						conn.setConnectTimeout(Coerce.toVncLong(connTimeoutMillisVal).getIntValue());
					}
					if (readTimeoutMillisVal != Nil) {
						conn.setReadTimeout(Coerce.toVncLong(readTimeoutMillisVal).getIntValue());
					}

					conn.connect();
						
					try {
						if (conn instanceof HttpURLConnection) {
							final int responseCode = ((HttpURLConnection)conn).getResponseCode();
							if (responseCode != HttpURLConnection.HTTP_OK) {
								throw new VncException(
										"No file to download. Server replied HTTP code: " + responseCode);
							}
						}

						final long contentLength = conn.getContentLengthLong();
	
						updateDownloadProgress(progressFn, 0L, new VncKeyword("start"));

						try (BufferedInputStream is = new BufferedInputStream(conn.getInputStream())) {
							final ByteArrayOutputStream output = new ByteArrayOutputStream();							
							try {
								final byte[] buffer = new byte[16 * 1024];
								int n;
								long total = 0L;
								long progressLast = 0L;
								while (-1 != (n = is.read(buffer))) {
									output.write(buffer, 0, n);
									total += n;

									// progress: 0..100%
									long progress = Math.max(0, Math.min(100, total * 100 / contentLength));

									if (progress != progressLast) {
										updateDownloadProgress(progressFn, progress, new VncKeyword("progress"));
									}

									progressLast = progress;
								}

								updateDownloadProgress(progressFn, 100L, new VncKeyword("end"));

								byte data[] = output.toByteArray();

								return VncBoolean.isTrue(binary)
										? new VncByteBuffer(ByteBuffer.wrap(data))
										: new VncString(new String(data, encoding));
							}
							finally {
								output.close();
							}
						}
					}
					catch(Exception ex) {
						updateDownloadProgress(progressFn, 0L, new VncKeyword("failed"));
						throw ex;
					}
					finally {
						if (conn instanceof HttpURLConnection) {
							((HttpURLConnection)conn).disconnect();
						}
					}
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};


	public static VncFunction io_internet_avail_Q =
		new VncFunction(
				"io/internet-avail?",
				VncFunction
					.meta()
					.arglists("(io/internet-avail?)", "(internet-avail? url)")
					.doc("Checks if an internet connection is present for a given url. "
							+ "Defaults to URL http://www.google.com.")
					.examples(
						"(io/internet-avail? \"http://www.google.com\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/internet-avail?", args, 0, 1);

				final String sUrl = args.isEmpty()
										? "http://www.google.com"
										: Coerce.toVncString(args.first()).getValue();

				try {
					final URL url = new URL(sUrl);
					final URLConnection connection = url.openConnection();
					connection.setConnectTimeout(3000);
					connection.connect();
					connection.getInputStream().close();
					return True;
				} 
				catch (Exception e) {
					return False;
				}
			}
			
			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_copy_stream =
		new VncFunction(
				"io/copy-stream",
				VncFunction
					.meta()
					.arglists("(io/copy-file in-stream out-stream)")
					.doc(
						"Copies input stream to an output stream. Returns nil or throws IOException. " +
						"Input and output must be a java.io.InputStream and java.io.OutputStream.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/copy-stream", args, 2);

				JavaInterop.getInterceptor().validateVeniceFunction("io/copy-stream");

				final Object is = Coerce.toVncJavaObject(args.first()).getDelegate();
				final Object os = Coerce.toVncJavaObject(args.second()).getDelegate();

				if (!(is instanceof InputStream)) {
					throw new VncException(String.format(
							"Function 'io/copy-stream' does not allow %s as in-stream",
							Types.getType(args.first())));
				}
				if (!(os instanceof OutputStream)) {
					throw new VncException(String.format(
							"Function 'io/copy-stream' does not allow %s as out-stream",
							Types.getType(args.second())));
				}

				try {
					IOStreamUtil.copy((InputStream)is, (OutputStream)os);
				}
				catch(Exception ex) {
					throw new VncException("Failed to copy stream");
				}

				return Nil;
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_slurp_stream =
		new VncFunction(
				"io/slurp-stream",
				VncFunction
					.meta()
					.arglists("(io/slurp-stream is & options)")
					.doc(
						"Slurps binary or string data from a Java InputStream is. " +
						"Supports the option :binary to either slurp binary or string data. " +
						"For string data an optional encoding can be specified.\n\n" +
						"Options: \n" +
						"  :binary true/false - e.g :binary true, defaults to false \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.examples(
						"(do \n" +
						"   (import :java.io.FileInputStream) \n" +
						"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
						"        (io/delete-file-on-exit file) \n" +
						"        (io/spit file \"123456789\" :append true) \n" +
						"        (try-with [is (. :FileInputStream :new file)] \n" +
						"           (io/slurp-stream is :binary false))) \n" +
						")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/slurp-stream", args, 1);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/slurp-stream");

				try {
					final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());

					final VncHashMap options = VncHashMap.ofAll(args.rest());
					final VncVal binary = options.get(new VncKeyword("binary"));

					if (VncBoolean.isTrue(binary)) {
						final byte[] data = IOStreamUtil.copyIStoByteArray(is);
						return data == null ? Nil : new VncByteBuffer(ByteBuffer.wrap(data));
					}
					else {
						final VncVal encVal = options.get(new VncKeyword("encoding"));

						final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();

						return new VncString(IOStreamUtil.copyIStoString(is, encoding));
					}
				}
				catch (VncException ex) {
					throw ex;
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_spit_stream =
		new VncFunction(
				"io/spit-stream",
				VncFunction
					.meta()
					.arglists("(io/spit-stream os content & options)")
					.doc(
						"Writes content (string or bytebuf) to the Java OutputStream os. " +
						"If content is of type string an optional encoding (defaults to " +
						"UTF-8) is supported. The stream can optionally be flushed after " +
						"the operation.\n\n" +
						"Options: \n" +
						"  :flush true/false - e.g :flush true, defaults to false \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.examples(
						"(do \n" +
						"   (import :java.io.FileOutputStream) \n" +
						"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
						"        (io/delete-file-on-exit file) \n" +
						"        (try-with [os (. :FileOutputStream :new file)] \n" +
						"           (io/spit-stream os \"123456789\" :flush true))) \n" +
						")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/spit-stream", args, 2);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/spit-stream");

				try {
					final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());

					final VncVal content = args.second();

					final VncHashMap options = VncHashMap.ofAll(args.slice(2));
					final VncVal encVal = options.get(new VncKeyword("encoding"));
					final String encoding = encVal == Nil ? "UTF-8" : ((VncString)encVal).getValue();
					final VncVal flushVal = options.get(new VncKeyword("flush"));
					final boolean flush = VncBoolean.isTrue(flushVal);

					byte[] data;

					if (Types.isVncString(content)) {
						data = ((VncString)content).getValue().getBytes(encoding);
					}
					else if (Types.isVncByteBuffer(content)) {
						data = ((VncByteBuffer)content).getBytes();
					}
					else {
						throw new VncException(String.format(
								"Function 'spit-stream' does not allow %s as content",
								Types.getType(content)));
					}

					os.write(data);

					if (flush) {
						os.flush();
					}

					return Nil;
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_uri_stream =
			new VncFunction(
					"io/uri-stream",
					VncFunction
						.meta()
						.arglists("(io/uri-stream uri)")
						.doc("Returns a Java InputStream from the uri.")
						.examples(
							"(-> (io/uri-stream \"https://www.w3schools.com/xml/books.xml\") \n" + 
							"    (io/slurp-stream :binary false :encoding :utf-8))             ")
						.build()
			) {
				public VncVal apply(final VncList args) {
					assertMinArity("io/uri-stream", args, 1);

					final String uri = Coerce.toVncString(args.first()).getValue();

					try {
						return new VncJavaObject(new URL(uri).openStream());
					}
					catch (Exception ex) {
						throw new VncException(ex.getMessage(), ex);
					}
				}

				private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction io_wrap_os_with_buffered_writer =
		new VncFunction(
				"io/wrap-os-with-buffered-writer",
				VncFunction
					.meta()
					.arglists("(io/wrap-os-with-buffered-writer os encoding?)")
					.doc(
						"Wraps an OutputStream os with a BufferedWriter using an optional " +
						"encoding (defaults to :utf-8).")
					.examples(
						"(do                                                         \n" +
						"   (import :java.io.ByteArrayOutputStream)                  \n" +
						"   (let [os (. :ByteArrayOutputStream :new)                 \n" +
						"         wr (io/wrap-os-with-buffered-writer os :utf-8)]    \n" +
						"      (. wr :write \"line 1\")                              \n" +
						"      (. wr :newLine)                                       \n" +
						"      (. wr :write \"line 2\")                              \n" +
						"      (. wr :flush)                                         \n" +
						"      (. os :toByteArray)))                                   ")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/wrap-os-with-buffered-writer", args, 1, 2);

				try {
					final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
					final String encoding = args.size() == 1 ? "UTF-8" : ((VncString)args.second()).getValue();

					return new VncJavaObject(new BufferedWriter(new OutputStreamWriter(os, encoding)));
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_wrap_os_with_print_writer =
		new VncFunction(
				"io/wrap-os-with-print-writer",
				VncFunction
					.meta()
					.arglists("(io/wrap-os-with-print-writer os encoding?)")
					.doc(
						"Wraps an OutputStream os with a PrintWriter using an optional " +
						"encoding (defaults to :utf-8).")
					.examples(
						"(do                                                      \n" +
						"   (import :java.io.ByteArrayOutputStream)               \n" +
						"   (let [os (. :ByteArrayOutputStream :new)              \n" +
						"         wr (io/wrap-os-with-print-writer os :utf-8)]    \n" +
						"      (. wr :println \"line 1\")                         \n" +
						"      (. wr :println \"line 2\")                         \n" +
						"      (. wr :flush)                                      \n" +
						"      (. os :toByteArray)))                                ")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/wrap-os-with-print-writer", args, 1, 2);

				try {
					final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
					final String encoding = args.size() == 1 ? "UTF-8" : ((VncString)args.second()).getValue();

					return new VncJavaObject(new PrintWriter(new OutputStreamWriter(os, encoding)));
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_wrap_is_with_buffered_reader =
			new VncFunction(
					"io/wrap-is-with-buffered-reader",
					VncFunction
						.meta()
						.arglists(
							"(io/wrap-is-with-buffered-reader is encoding?)")
						.doc(
							"Wraps an InputStream is with a BufferedReader using an optional " +
							"encoding (defaults to :utf-8).")
						.examples(
							"(do                                                                          \n" +
							"   (import :java.io.ByteArrayInputStream)                                    \n" +
							"   (let [data (byte-array [108 105 110 101 32 49 10 108 105 110 101 32 50])  \n" +
							"         is (. :ByteArrayInputStream :new data)                              \n" +
							"         rd (io/wrap-is-with-buffered-reader is :utf-8)]                     \n" +
							"      (println (. rd :readLine))                                             \n" +
							"      (println (. rd :readLine))))                                             ")
						.build()
			) {
				public VncVal apply(final VncList args) {
					assertArity("io/wrap-is-with-buffered-reader", args, 1, 2);

					 if (Types.isVncJavaObject(args.first())) {
						final Object delegate = ((VncJavaObject)args.first()).getDelegate();
						if (delegate instanceof InputStream) {
							try {
								final InputStream is = (InputStream)delegate;
								final String encoding = args.size() == 1 ? "UTF-8" : ((VncString)args.second()).getValue();
	
								return new VncJavaObject(new BufferedReader(new InputStreamReader(is, encoding)));
							}
							catch (Exception ex) {
								throw new VncException(ex.getMessage(), ex);
							}
						}
					}
					
					throw new VncException(String.format(
							"Function 'io/wrap-is-with-buffered-reader' requires an InputStream " +
							"or a Reader. %s as is not allowed!",
							Types.getType(args.first())));
				}

				private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction io_buffered_reader =
		new VncFunction(
				"io/buffered-reader",
				VncFunction
					.meta()
					.arglists(
						"(io/buffered-reader is encoding?)",
						"(io/buffered-reader rdr)")
					.doc(
						"Creates a BufferedReader from an InputStream is with optional " +
						"encoding (defaults to :utf-8), from a Reader or from a string.")
					.examples(
						"(do                                                                          \n" +
						"   (import :java.io.ByteArrayInputStream)                                    \n" +
						"   (let [data (byte-array [108 105 110 101 32 49 10 108 105 110 101 32 50])  \n" +
						"         is (. :ByteArrayInputStream :new data)                              \n" +
						"         rd (io/buffered-reader is :utf-8)]                                  \n" +
						"      (println (. rd :readLine))                                             \n" +
						"      (println (. rd :readLine))))                                             ",
						"(do                                                                          \n" +
						"   (let [rd (io/buffered-reader \"1\\n2\\n3\\n4\")]                          \n" +
						"      (println (. rd :readLine))                                             \n" +
						"      (println (. rd :readLine))))                                             ")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/buffered-reader", args, 1, 2);

				if (Types.isVncString(args.first())) {
					return new VncJavaObject(
							new BufferedReader(
									new StringReader(((VncString)args.first()).getValue())));
				}
				else if (Types.isVncJavaObject(args.first())) {
					final Object delegate = ((VncJavaObject)args.first()).getDelegate();
					if (delegate instanceof InputStream) {
						try {
							final InputStream is = (InputStream)delegate;
							final String encoding = args.size() == 1 ? "UTF-8" : ((VncString)args.second()).getValue();

							return new VncJavaObject(new BufferedReader(new InputStreamReader(is, encoding)));
						}
						catch (Exception ex) {
							throw new VncException(ex.getMessage(), ex);
						}
					}
					else if (delegate instanceof BufferedReader) {
						return args.first();
					}
					else if (delegate instanceof Reader) {
						return new VncJavaObject(new BufferedReader((Reader)delegate));
					}
				}
				
				throw new VncException(String.format(
						"Function 'io/buffered-reader' requires an InputStream, " +
						"a Reader, or a string. %s as is not allowed!",
						Types.getType(args.first())));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_buffered_writer =
		new VncFunction(
				"io/buffered-writer",
				VncFunction
					.meta()
					.arglists(
						"(io/buffered-writer os encoding?)",
						"(io/buffered-writer wr)")
					.doc(
						"Creates a BufferedWriter from an OutputStream os with optional " +
						"encoding (defaults to :utf-8) or from a Writer.")
					.examples()
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/buffered-writer", args, 1, 2);

				if (Types.isVncJavaObject(args.first())) {
					final Object delegate = ((VncJavaObject)args.first()).getDelegate();
					if (delegate instanceof OutputStream) {
						try {
							final OutputStream os = (OutputStream)delegate;
							final String encoding = args.size() == 1 ? "UTF-8" : ((VncString)args.second()).getValue();

							return new VncJavaObject(new BufferedWriter(new OutputStreamWriter(os, encoding)));
						}
						catch (Exception ex) {
							throw new VncException(ex.getMessage(), ex);
						}
					}
					else if (delegate instanceof BufferedWriter) {
						return args.first();
					}
					else if (delegate instanceof Writer) {
						return new VncJavaObject(new BufferedWriter((Writer)delegate));
					}
				}
				
				throw new VncException(String.format(
						"Function 'io/buffered-writer' requires an OutputStream " +
						"or a Writer. %s as is not allowed!",
						Types.getType(args.first())));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_mime_type =
		new VncFunction(
				"io/mime-type",
				VncFunction
					.meta()
					.arglists("(io/mime-type file)")
					.doc("Returns the mime-type for the file if available else nil.")
					.examples(
						"(io/mime-type \"document.pdf\")",
						"(io/mime-type (io/file \"document.pdf\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertMinArity("io/mime-type", args, 1);

				final VncVal file = args.first();

				if (Types.isVncString(file)) {
					return new VncString(
								MimeTypes.getMimeTypeFromFileName(
										((VncString)file).getValue()));
				}
				else if (Types.isVncJavaObject(file, File.class)) {
					return new VncString(
							MimeTypes.getMimeTypeFromFile(
									(File)(Coerce.toVncJavaObject(file).getDelegate())));
				}
				else {
					throw new VncException(String.format(
							"Function 'io/mime-type' does not allow %s as fs",
							Types.getType(file)));
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};


	///////////////////////////////////////////////////////////////////////////
	// IO TEMP functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction io_temp_file =
		new VncFunction(
				"io/temp-file",
				VncFunction
					.meta()
					.arglists("(io/temp-file prefix suffix)")
					.doc(
						"Creates an empty temp file with prefix and suffix.")
					.examples(
						"(do \n" +
						"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
						"        (io/spit file \"123456789\" :append true) \n" +
						"        (io/slurp file :binary false :remove true)) \n" +
						")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/temp-file", args, 2);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/temp-file");

				final String prefix = Coerce.toVncString(args.first()).getValue();
				final String suffix = Coerce.toVncString(args.second()).getValue();
				try {
					return new VncString(Files.createTempFile(prefix, suffix).normalize().toString());
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_temp_dir =
		new VncFunction(
				"io/temp-dir",
				VncFunction
					.meta()
					.arglists("(io/temp-dir prefix)")
					.doc("Creates a temp directory with prefix.")
					.examples("(io/temp-dir \"test-\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/temp-dir", args, 1);
				
				JavaInterop.getInterceptor().validateVeniceFunction("io/temp-dir");

				final String prefix = Coerce.toVncString(args.first()).getValue();
				try {
					return new VncString(Files.createTempDirectory(prefix).normalize().toString());
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_load_classpath_resource =
		new VncFunction(
				"io/load-classpath-resource",
				VncFunction
					.meta()
					.arglists(
						"(io/load-classpath-resource name)")
					.doc(
						"Loads a classpath resource.")
					.examples(
						"(io/load-classpath-resource \"org/foo/images/foo.png\"")
					.build()
		) {
			public VncVal apply(final VncList args) {
				try {
					assertArity("io/load-classpath-resource", args, 1);
					
					JavaInterop.getInterceptor().validateVeniceFunction("io/load-classpath-resource");

					final VncVal name = args.first();

					if (Types.isVncString(name)) {
						final String res = ((VncString)args.first()).getValue();
						final byte[] data = JavaInterop.getInterceptor().onLoadClassPathResource(res);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncKeyword(name)) {
						final String res = ((VncKeyword)args.first()).getValue();
						final byte[] data = JavaInterop.getInterceptor().onLoadClassPathResource(res);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncSymbol(name)) {
						final String res = ((VncSymbol)args.first()).getName();
						final byte[] data = JavaInterop.getInterceptor().onLoadClassPathResource(res);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else {
						return Nil;
					}
				}
				catch (SecurityException ex) {
					throw ex;
				}
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_classpath_resource_Q =
		new VncFunction(
				"io/classpath-resource?",
				VncFunction
					.meta()
					.arglists(
						"(io/classpath-resource? name)")
					.doc(
						"Returns true if the classpath resource exists otherwise false.")
					.examples(
						"(io/classpath-resource? \"org/foo/images/foo.png\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				try {
					assertArity("io/classpath-resource?", args, 1);

					final VncVal name = args.first();

					if (Types.isVncString(name)) {
						final String path = ((VncString)args.first()).getValue();
						return VncBoolean.of(new ClassPathResource(path).getResource() != null);
					}
					else if (Types.isVncKeyword(name)) {
						final String path = ((VncKeyword)args.first()).getValue();
						return VncBoolean.of(new ClassPathResource(path).getResource() != null);
					}
					else if (Types.isVncSymbol(name)) {
						final String path = ((VncSymbol)args.first()).getName();
						return VncBoolean.of(new ClassPathResource(path).getResource() != null);
					}
					else {
						return VncBoolean.False;
					}
				}
				catch (Exception ex) {
					return VncBoolean.False;
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_default_charset =
		new VncFunction(
				"io/default-charset",
				VncFunction
					.meta()
					.arglists("(io/default-charset)")
					.doc("Returns the default charset.")
					.build()
		) {
			public VncVal apply(final VncList args) {
				assertArity("io/default-charset", args, 0);

				return new VncString(Charset.defaultCharset().name());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};


	public static String encoding(final VncVal enc) {
		return enc == Nil
				? "UTF-8"
				: Types.isVncKeyword(enc)
					? Coerce.toVncKeyword(enc).getValue()
					: Coerce.toVncString(enc).getValue();
	}

	public static File convertToFile(final VncVal f, final String errFormat) {
		if (Types.isVncString(f)) {
			return new File(((VncString)f).getValue());
		}
		else if (Types.isVncJavaObject(f, File.class)) {
			return (File)((VncJavaObject)f).getDelegate();
		}
		else if (Types.isVncJavaObject(f, Path.class)) {
			return ((Path)((VncJavaObject)f).getDelegate()).toFile();
		}
		else {
			throw new VncException(String.format(errFormat, Types.getType(f)));
		}
	}

	public static void validateReadableFile(final File file) {
		if (!file.isFile()) {
			throw new VncException(String.format("'%s' is not a file", file.getPath()));
		}
		if (!file.canRead()) {
			throw new VncException(String.format("The file '%s' has no read permission", file.getPath()));
		}
	}

	public static void validateReadableDirectory(final File file) {
		if (!file.isDirectory()) {
			throw new VncException(String.format("'%s' is not a directory", file.getPath()));
		}
		if (!file.canRead()) {
			throw new VncException(String.format("The directory '%s' has no read permission", file.getPath()));
		}
	}


	private static void updateDownloadProgress(
			final VncFunction fn,
			final long percentage,
			final VncKeyword status
	) {
		try {
			fn.apply(VncList.of(new VncLong(percentage), status));
		}
		catch(Exception ex) {
			// do nothing
		}
	}

	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns =
			new VncHashMap
					.Builder()
					.add(io_file)
					.add(io_file_Q)
					.add(io_file_path)
					.add(io_file_canonical_path)
					.add(io_file_absolute_path)
					.add(io_file_parent)
					.add(io_file_name)
					.add(io_file_ext_Q)
					.add(io_file_size)
					.add(io_exists_file_Q)
					.add(io_exists_dir_Q)
					.add(io_file_can_read_Q)
					.add(io_file_can_write_Q)
					.add(io_file_can_execute_Q)
					.add(io_file_hidden_Q)
					.add(io_list_files)
					.add(io_list_file_tree)
					.add(io_list_files_glob_pattern)
					.add(io_delete_file)
					.add(io_delete_file_on_exit)
					.add(io_delete_file_tree)
					.add(io_copy_file)
					.add(io_move_file)
					.add(io_mkdir)
					.add(io_mkdirs)
					.add(io_temp_file)
					.add(io_temp_dir)
					.add(io_tmp_dir)
					.add(io_user_dir)
					.add(io_user_home_dir)
					.add(io_slurp)
					.add(io_slurp_lines)
					.add(io_spit)
					.add(io_download)
					.add(io_internet_avail_Q)
					.add(io_copy_stream)
					.add(io_slurp_stream)
					.add(io_spit_stream)
					.add(io_uri_stream)
					.add(io_wrap_os_with_buffered_writer)
					.add(io_wrap_os_with_print_writer)
					.add(io_wrap_is_with_buffered_reader)
					.add(io_buffered_reader)
					.add(io_buffered_writer)
					.add(io_mime_type)
					.add(io_default_charset)
					.add(io_load_classpath_resource)
					.add(io_classpath_resource_Q)
					.toMap();
}
