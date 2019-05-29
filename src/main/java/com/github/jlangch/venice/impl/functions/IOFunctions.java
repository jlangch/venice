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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertMinArity;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.Constants;
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
import com.github.jlangch.venice.impl.util.IOStreamUtil;
import com.github.jlangch.venice.impl.util.MimeTypes;
import com.github.jlangch.venice.impl.util.Zipper;


public class IOFunctions {


	///////////////////////////////////////////////////////////////////////////
	// IO functions
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction io_file = 
		new VncFunction(
				"io/file", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/file path) (io/file parent child)")		
					.doc(
						"Returns a java.io.File. path, parent, may be a file or a string (file path) " +
						"child must be a string")
					.examples(
						"(io/file \"/temp/test.txt\")",
						"(io/file \"/temp\" \"test.txt\")",
						"(io/file (io/file \"/temp\") \"test.txt\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/file", args, 1, 2);
							
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
					
					final String child = Coerce.toVncString(args.second()).getValue();
					
					return new VncJavaObject(new File(parent, child));
				}		
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_size = 
		new VncFunction(
				"io/file-size", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/file-size f)")		
					.doc("Returns the size of the file f. f must be a file or a string (file path).")
					.examples("(io/file-size \"/bin/sh\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/file-size", args, 1);

				final File f = convertToFile(
									args.first(), 
									"Function 'io/file-size' does not allow %s as f");
				
				return new VncLong(f.length());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_path = 
		new VncFunction(
				"io/file-path", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/file-path f)")		
					.doc("Returns the path of the file f. f must be a file or a string (file path).")
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
					.module("io")
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
					.module("io")
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
					.module("io")
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
					.module("io")
					.arglists("(io/file-name f)")		
					.doc("Returns the name of the file f. f must be a file or a string (file path).")
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

	public static VncFunction io_file_Q = 
		new VncFunction(
				"io/file?", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/file? x)")		
					.doc("Returns true if x is a java.io.File.")
					.examples("(io/file? (io/file \"/temp/test.txt\"))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/file?", args, 1);
	
				return Types.isVncJavaObject(args.first(), File.class) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_exists_file_Q = 
		new VncFunction(
				"io/exists-file?", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/exists-file? f)")		
					.doc("Returns true if the file f exists. fx must be a file or a string (file path).")
					.examples("(io/exists-file? \"/temp/test.txt\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/exists-file?", args, 1);

				final File f = convertToFile(
									args.first(), 
									"Function 'io/exists-file?' does not allow %s as x");
						
				return Constants.bool(f.isFile());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_exists_dir_Q = 
		new VncFunction(
				"io/exists-dir?", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/exists-dir? f)")		
					.doc(
						"Returns true if the file f exists and is a directory. " +
						"f must be a file or a string (file path).")
					.examples("(io/exists-dir? (io/file \"/temp\"))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/exists-dir?", args, 1);

				final File f = convertToFile(
									args.first(), 
									"Function 'io/exists-dir?' does not allow %s as f");
						
				return Constants.bool(f.isDirectory());
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_delete_file = 
		new VncFunction(
				"io/delete-file", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/delete-file f & files)")		
					.doc("Deletes one or multiple files. f must be a file or a string (file path)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertMinArity("io/delete-file", args, 1);
	
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

	public static VncFunction io_delete_file_on_exit = 
		new VncFunction(
				"io/delete-file-on-exit", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/delete-file-on-exit f)")		
					.doc("Deletes a file on JVM exit. f must be a file or a string (file path).")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/delete-file-on-exit", args, 1);
	
				final File file = convertToFile(
									args.first(), 
									"Function 'io/delete-file-on-exit' does not allow %s as f");
						
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
					.module("io")
					.arglists("(io/list-files dir filterFn?)")		
					.doc(
						"Lists files in a directory. dir must be a file or a string (file path). " +
						"filterFn is an optional filter that filters the files found.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/list-files", args, 1, 2);

				final File dir = convertToFile(
									args.first(), 
									"Function 'io/list-files' does not allow %s as dir");
				
				try {
					final VncFunction filterFn = (args.size() == 2) ? Coerce.toVncFunction(args.second()) : null;
	
					final List<VncVal> files = new ArrayList<>();
	
					for(File f : dir.listFiles()) {
						final VncVal result = (filterFn == null) 
												? True 
												: filterFn.apply(VncList.of(new VncJavaObject(f)));
						if (result == True) {
							files.add(new VncJavaObject(f));
						}
					}
					
					return new VncList(files);
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
					.module("io")
					.arglists("(io/copy-file source dest)")		
					.doc(
						"Copies source to dest. Returns nil or throws IOException. " + 
						"Source must be a file or a string (file path), dest must be a file, " +
						"a string (file path), or an OutputStream.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/copy-file", args, 2);

				final File source = convertToFile(
										args.first(), 
										"Function 'io/copy-file' does not allow %s as source");
	
				final VncVal destVal = args.second();
				
				if (Types.isVncString(destVal) || Types.isVncJavaObject(destVal, File.class)) {
					final File dest = Types.isVncString(destVal)
										? new File(Coerce.toVncString(destVal).getValue())
										: Coerce.toVncJavaObject(destVal, File.class);
					
					try {
						Files.copy(source.toPath(), dest.toPath());
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
					.module("io")
					.arglists("(io/move-file source target)")		
					.doc(
						"Moves source to target. Returns nil or throws IOException. " + 
						"Source and target must be a file or a string (file path).")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/move-file", args, 2);

				final File from = convertToFile(
									args.first(), 
									"Function 'io/move-file' does not allow %s as source");

				final File to = convertToFile(
									args.second(), 
									"Function 'io/move-file' does not allow %s as target");

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
					.module("io")
					.arglists("(io/mkdir dir)")		
					.doc("Creates the directory. dir must be a file or a string (file path).")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/mkdir", args, 1);

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
				"io/mkdir", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/mkdirs dir)")		
					.doc(
						"Creates the directory including any necessary but nonexistent " +
						"parent directories. dir must be a file or a string (file path).")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/mkdirs", args, 1);

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
					.module("io")
					.arglists("(io/tmp-dir)")		
					.doc("Returns the tmp dir as a java.io.File.")
					.examples("(io/tmp-dir )")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/tmp-dir", args, 0);
	
				return new VncJavaObject(new File(System.getProperty("java.io.tmpdir")));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_user_dir = 
		new VncFunction(
				"io/user-dir", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/user-dir)")		
					.doc("Returns the user dir (current working dir) as a java.io.File.")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/user-dir", args, 0);
	
				return new VncJavaObject(new File(System.getProperty("user.dir")));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_slurp_lines = 
		new VncFunction(
				"io/slurp-lines", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/slurp-lines file & options)")		
					.doc(
						"Read all lines from f. f may be a file, a string file path, " +
						"a Java InputStream, or a Java Reader. \n\n" + 
						"Options: \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("io/slurp-lines", args, 1);
	
				try {	
					final VncVal arg = args.first();

					final VncHashMap options = VncHashMap.ofAll(args.rest());
					
					if (Types.isVncString(arg) || Types.isVncJavaObject(arg, File.class)) {
						final File file = Types.isVncString(arg) 
											? new File(((VncString)arg).getValue())
											:  (File)(Coerce.toVncJavaObject(args.first()).getDelegate());

						final VncVal encVal = options.get(new VncKeyword("encoding")); 					
						final String encoding = encoding(encVal);

						final List<VncString> lines = 
								Files.readAllLines(file.toPath(), Charset.forName(encoding))
									 .stream()
									 .map(s -> new VncString(s))
									 .collect(Collectors.toList());
						
						return new VncList(lines);
					}
					else if (Types.isVncJavaObject(arg, InputStream.class)) {
						final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());

						final VncVal encVal = options.get(new VncKeyword("encoding")); 					
						final String encoding = encoding(encVal);

						final BufferedReader rd = new BufferedReader(new InputStreamReader(is, encoding));
						return new VncList(rd.lines().map(s -> new VncString(s)).collect(Collectors.toList()));
					}
					else if (Types.isVncJavaObject(arg, Reader.class)) {
						final BufferedReader rd = new BufferedReader(
														(Reader)(Coerce.toVncJavaObject(args.first()).getDelegate()));
						
						return new VncList(rd.lines().map(s -> new VncString(s)).collect(Collectors.toList()));
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
					.module("io")
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
	
				try {	
					final VncVal arg = args.first();

					final VncHashMap options = VncHashMap.ofAll(args.rest());
					
					final VncVal binary = options.get(new VncKeyword("binary")); 


					if (Types.isVncString(arg) || Types.isVncJavaObject(arg, File.class)) {
						final File file = Types.isVncString(arg) 
											? new File(((VncString)arg).getValue())
											:  (File)(Coerce.toVncJavaObject(args.first()).getDelegate());

						if (binary == True) {
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

						if (binary == True) {
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
						final BufferedReader rd = new BufferedReader(
														(Reader)(Coerce.toVncJavaObject(args.first()).getDelegate()));
						final String s = rd.lines().collect(Collectors.joining(System.lineSeparator()));
						
						if (binary == True) {
							final VncVal encVal = options.get(new VncKeyword("encoding")); 						
							final String encoding = encoding(encVal);

							return new VncByteBuffer(s.getBytes(encoding));
						}
						else {
							return new VncString(s);
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
					.module("io")
					.arglists("(io/spit f content & options)")		
					.doc(
						"Opens f, writes content, and then closes f. " +
						"f may be a file or a string (file path). \n\n" +
						"Options: \n" +
						"  :append true/false - e.g :append true, defaults to false \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertMinArity("io/spit", args, 2);
	
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
						data = ((VncByteBuffer)content).getValue().array();
					}
					else {
						throw new VncException(String.format(
								"Function 'io/spit' does not allow %s as content",
								Types.getType(content)));
					}
	
					final List<OpenOption> openOptions = new ArrayList<>();
					openOptions.add(StandardOpenOption.CREATE);
					openOptions.add(StandardOpenOption.WRITE);
					
					if (append == True) {
						openOptions.add(StandardOpenOption.APPEND);
					}
					else {
						openOptions.add(StandardOpenOption.TRUNCATE_EXISTING);
					}
					
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

	public static VncFunction io_zip = 
		new VncFunction(
				"io/zip", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/zip & entries)")		
					.doc(
						"Creates a zip containing the entries. An entry is given by a " +
						"name and data. The entry data may be nil, a bytebuf, a file, " +
						"a string (file path), or an InputStream. " +
						"An entry name with a trailing '/' creates a directory. " +
						"Returns the zip as bytebuf.")
					.examples(
						"; single entry                                                   \n" +
						"(->> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8))     \n" +
						"     (io/spit \"test.zip\"))                                       ",
						
						"; multiple entries                                               \n" +
						"(->> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)      \n" +
						"             \"b.txt\" (bytebuf-from-string \"def\" :utf-8)      \n" +
						"             \"c.txt\" (bytebuf-from-string \"ghi\" :utf-8))     \n" +
						"     (io/spit \"test.zip\"))                                       ",
						
						"; multiple entries with subdirectories                           \n" +
						"(->> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)      \n" +
						"             \"x/b.txt\" (bytebuf-from-string \"def\" :utf-8)    \n" +
						"             \"x/y/c.txt\" (bytebuf-from-string \"ghi\" :utf-8)) \n" +
						"     (io/spit \"test.zip\"))                                       ",
					
						"; empty directory z/                                             \n" +
						"(->> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)      \n" +
						"             \"z/\" nil)                                         \n" +
						"     (io/spit \"test.zip\"))                                       ")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertMinArity("io/zip", args, 2);
	
				if (args.isEmpty()) {
					return Nil;
				}
				
				try {
					if (args.size() % 2 == 1) {
						throw new VncException("Function 'io/zip' requires an even number of arguments");
					}
					
					int idx = 0;
					final LinkedHashMap<String,Object> map = new LinkedHashMap<>();
					
					while (idx < args.size()) {
						final String name = Coerce.toVncString(args.nth(idx++)).getValue();
						
						if (map.containsKey(name)) {
							throw new VncException(String.format(
									"Function 'io/zip' duplicate entry name %s", name));
						}

						final VncVal dataVal = args.nth(idx++);
						Object data;
						if (dataVal == Nil) {
							data = new byte[0];
						}
						else if (Types.isVncByteBuffer(dataVal)) {
							data = ((VncByteBuffer)dataVal).getValue().array();
						}
						else if (Types.isVncJavaObject(dataVal, InputStream.class)) {
							data = (InputStream)((VncJavaObject)dataVal).getDelegate();
						}
						else if (Types.isVncJavaObject(dataVal, File.class)) {
							data = (File)((VncJavaObject)dataVal).getDelegate();
						}
						else if (Types.isVncString(dataVal)) {
							data = new File(Coerce.toVncString(dataVal).getValue());
						}
						else {
							throw new VncException(String.format(
									"Function 'io/zip' does not allow %s as f",
									Types.getType(dataVal)));
						}
						
						map.put(name, data);
					}
					
					return new VncByteBuffer(Zipper.zip(map));
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_zip_append = 
		new VncFunction(
				"io/zip-append", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/zip-append f & entries)")		
					.doc(
						"Appends entries to an existing zip file f. Overwrites existing " +
						"entries. An entry is given by a name and data. The entry data " +
						"may be nil, a bytebuf, a file, a string (file path), or an " +
						"InputStream." +
						"An entry name with a trailing '/' creates a directory. ")
					.examples( 
						"(do                                                                 \n" +
						"  (let [data (bytebuf-from-string \"abc\" :utf-8)]                  \n" +
						"    ; create the zip with a first file                              \n" +
						"    (->> (io/zip \"a.txt\" data)                                    \n" +
						"         (io/spit \"test.zip\"))                                    \n" +
						"    ; add text files                                                \n" +
						"    (io/zip-append \"test.zip\" \"b.txt\" data \"x/c.txt\" data)    \n" +
						"    ; add an empty directory                                        \n" +
						"    (io/zip-append \"test.zip\" \"x/y/\" nil)))                       ")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertMinArity("io/zip-append", args, 3);
	
				final VncVal fileVal = args.first();
				File file = null;
				if (Types.isVncJavaObject(fileVal, File.class)) {
					file = (File)((VncJavaObject)fileVal).getDelegate();
				}
				else if (Types.isVncString(fileVal)) {
					file = new File(Coerce.toVncString(fileVal).getValue());
				}
				else {
					throw new VncException(String.format(
							"Function 'io/zip-append' does not allow %s as f",
							Types.getType(fileVal)));
				}

				final VncList entryArgs = args.slice(1);
				try {
					if (entryArgs.size() % 2 == 1) {
						throw new VncException("Function 'io/zip-append' requires an even number of entry arguments");
					}
					
					int idx = 0;
					final LinkedHashMap<String,Object> map = new LinkedHashMap<>();
					
					while (idx < entryArgs.size()) {
						final String name = Coerce.toVncString(entryArgs.nth(idx++)).getValue();
						
						if (map.containsKey(name)) {
							throw new VncException(String.format(
									"Function 'io/zip-append' duplicate entry name %s", name));
						}

						final VncVal dataVal = entryArgs.nth(idx++);
						Object data;
						if (dataVal == Nil) {
							data = new byte[0];
						}
						else if (Types.isVncByteBuffer(dataVal)) {
							data = ((VncByteBuffer)dataVal).getValue().array();
						}
						else if (Types.isVncJavaObject(dataVal, InputStream.class)) {
							data = (InputStream)((VncJavaObject)dataVal).getDelegate();
						}
						else if (Types.isVncJavaObject(dataVal, File.class)) {
							data = (File)((VncJavaObject)dataVal).getDelegate();
						}
						else if (Types.isVncString(dataVal)) {
							data = new File(Coerce.toVncString(dataVal).getValue());
						}
						else {
							throw new VncException(String.format(
									"Function 'io/zip-append' does not allow %s as entry data",
									Types.getType(dataVal)));
						}
						
						map.put(name, data);
					}
					
					Zipper.zipAppend(file, map);
					
					return Nil;
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_zip_size = 
		new VncFunction(
				"io/zip-size", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/zip-size f)")		
					.doc(
						"Returns the number of entries in the zip f. f may be a bytebuf, " +
						"a file, a string (file path) or an InputStream.")
					.examples(
						"(io/zip-size (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/zip-size", args, 1);
	
				if (args.isEmpty()) {
					return new VncLong(0);
				}
				
				try {
					final VncVal data = args.first();
					
					if (Types.isVncByteBuffer(data)) {
						return new VncLong(Zipper.listZipEntryNames(((VncByteBuffer)data).getValue().array()).size());
					}
					else if (Types.isVncJavaObject(data, InputStream.class)) {
						return new VncLong(Zipper.listZipEntryNames((InputStream)((VncJavaObject)data).getDelegate()).size());
					}
					else if (Types.isVncJavaObject(data, File.class)) {
						return new VncLong(Zipper.listZipEntryNames((File)((VncJavaObject)data).getDelegate()).size());
					}
					else if (Types.isVncString(data)) {
						return new VncLong(Zipper.listZipEntryNames(new File(Coerce.toVncString(data).getValue())).size());
					}
					else {
						throw new VncException(String.format(
								"Function 'io/zip-size' does-size not allow %s as f",
								Types.getType(data)));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_unzip = 
		new VncFunction(
				"io/unzip", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/unzip f entry-name)")		
					.doc(
						"Unzips an entry from zip f the entry's data as a bytebuf. f may be a bytebuf, \n" + 
						"a file, a string (file path) or an InputStream.")
					.examples(
						"(-> (io/zip \"a.txt\" (bytebuf-from-string \"abcdef\" :utf-8)) \n" +
						"    (io/unzip \"a.txt\"))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/unzip", args, 2);
	
				final VncVal buf = args.first();
				final String entryName = Coerce.toVncString(args.second()).getValue();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(buf)) {
						final byte[] data = Zipper.unzip(((VncByteBuffer)buf).getValue().array(), entryName);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, InputStream.class)) {
						final byte[] data = Zipper.unzip((InputStream)((VncJavaObject)buf).getDelegate(), entryName);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, File.class)) {
						final byte[] data = Zipper.unzip((File)((VncJavaObject)buf).getDelegate(), entryName);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncString(buf)) {
						final byte[] data = Zipper.unzip(new File(Coerce.toVncString(buf).getValue()), entryName);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else {
						throw new VncException(String.format(
								"Function 'io/unzip' does not allow %s as f",
								Types.getType(buf)));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_unzip_first = 
		new VncFunction(
				"io/unzip-first", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/unzip-first zip)")		
					.doc(
						"Unzips the first entry of the zip f returning its data as a bytebuf. " +
						"f may be a bytebuf, a file, a string (file path) or an InputStream.")
					.examples(
						"(-> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
						"            \"b.txt\" (bytebuf-from-string \"def\" :utf-8)) \n" +
						"    (io/unzip-first))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/unzip-first", args, 1);
	
				final VncVal buf = args.first();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(buf)) {
						final byte[] data = Zipper.unzipNthEntry(((VncByteBuffer)buf).getValue().array(), 0);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, InputStream.class)) {
						final byte[] data = Zipper.unzipNthEntry((InputStream)((VncJavaObject)buf).getDelegate(), 0);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, File.class)) {
						final byte[] data = Zipper.unzipNthEntry((File)((VncJavaObject)buf).getDelegate(), 0);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncString(buf)) {
						final byte[] data = Zipper.unzipNthEntry(new File(Coerce.toVncString(buf).getValue()), 0);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else {
						throw new VncException(String.format(
								"Function 'io/unzip-first' does not allow %s as f",
								Types.getType(buf)));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_unzip_nth = 
		new VncFunction(
				"io/unzip-nth", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/unzip-nth zip n)")		
					.doc(
						"Unzips the nth (zero.based) entry of the zip f returning its data as a bytebuf. " +
						"f may be a bytebuf, a file, a string (file path) or an InputStream.")
					.examples(
						"(-> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
						"            \"b.txt\" (bytebuf-from-string \"def\" :utf-8)  \n" +
						"            \"c.txt\" (bytebuf-from-string \"ghi\" :utf-8)) \n" +
						"    (io/unzip-nth 1))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/unzip-nth", args, 2);
	
				final VncVal buf = args.first();
				final int entryIdx = Coerce.toVncLong(args.second()).getIntValue();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(buf)) {
						final byte[] data = Zipper.unzipNthEntry(((VncByteBuffer)buf).getValue().array(), entryIdx);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, InputStream.class)) {
						final byte[] data = Zipper.unzipNthEntry((InputStream)((VncJavaObject)buf).getDelegate(), entryIdx);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, File.class)) {
						final byte[] data = Zipper.unzipNthEntry((File)((VncJavaObject)buf).getDelegate(), entryIdx);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncString(buf)) {
						final byte[] data = Zipper.unzipNthEntry(new File(Coerce.toVncString(buf).getValue()), entryIdx);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else {
						throw new VncException(String.format(
								"Function 'io/unzip-nth' does not allow %s as f",
								Types.getType(buf)));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_unzip_all = 
		new VncFunction(
				"io/unzip-all", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/unzip-all f)")		
					.doc(
						"Unzips all entries of the zip f returning a map with " +
						"the entry names as key and the entry data as bytebuf values. " +
						"f may be a bytebuf, a file, a string (file path) or an InputStream.")
					.examples(
						"(-> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
						"            \"b.txt\" (bytebuf-from-string \"def\" :utf-8)  \n" +
						"            \"c.txt\" (bytebuf-from-string \"ghi\" :utf-8)) \n" +
						"    (io/unzip-all))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/unzip-all", args, 1);
	
				final VncVal buf = args.first();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else {
						final Map<String,byte[]> data;
						
						if (Types.isVncByteBuffer(buf)) {
							data = Zipper.unzipAll(((VncByteBuffer)buf).getValue().array());
						}
						else if (Types.isVncJavaObject(buf, InputStream.class)) {
							data = Zipper.unzipAll((InputStream)((VncJavaObject)buf).getDelegate());
						}
						else if (Types.isVncJavaObject(buf, File.class)) {
							data = Zipper.unzipAll((File)((VncJavaObject)buf).getDelegate());
						}
						else if (Types.isVncString(buf)) {
							data = Zipper.unzipAll(new File(Coerce.toVncString(buf).getValue()));
						}
						else {
							throw new VncException(String.format(
									"Function 'io/unzip-all' does not allow %s as f",
									Types.getType(buf)));
						}
						
						if (data == null) {
							return Nil;
						}
						else {
							final Map<VncString,VncByteBuffer> tmp = 
								data.entrySet()
									.stream()
									.collect(Collectors.toMap(
												e -> new VncString(e.getKey()), 
												e -> new VncByteBuffer(e.getValue())));
							return new VncHashMap(tmp);
						}

					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_zip_file = 
		new VncFunction(
				"io/zip-file", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/zip-file src-file dest)")		
					.doc(
						"Zips a file or directory to a file (given as File or string " +
						"file path) or an OutputStream.")
					.examples(
						"(io/zip-file \"test-dir\" \"test.zip\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/zip-file", args, 2);
	
				final File sourceFile = Coerce.toVncJavaObject(args.first(), File.class);
				final VncVal dest = args.second();
				
				try {
					if (Types.isVncJavaObject(dest, File.class)) {
						Zipper.zipFileOrDir(sourceFile, null, Coerce.toVncJavaObject(dest, File.class));
					}
					else if (Types.isVncString(dest)) {
						Zipper.zipFileOrDir(sourceFile, null, new File(Coerce.toVncString(dest).getValue()));
					}
					else if (Types.isVncJavaObject(dest, OutputStream.class)) {
						Zipper.zipFileOrDir(sourceFile, null, Coerce.toVncJavaObject(dest, OutputStream.class));
					}
					else {
						throw new VncException(String.format(
								"Function 'io/zip-file' does not allow %s as dest",
								Types.getType(dest)));
					}
					
					return Nil;
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_zip_list = 
		new VncFunction(
				"io/zip-list", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/zip-list f & options)")		
					.doc(
						"List the content of a the zip f. f may be a bytebuf, a file, " +
						"a string (file path), or an InputStream. \n" +
						"Options: \n" +
						"  :verbose true/false - e.g :verbose true, defaults to false")
					.examples(
						"(io/zip-list \"test-file.zip\")",
						"(io/zip-list \"test-file.zip\" :verbose true)")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertMinArity("io/zip-list", args, 1);
				
				try {
					final VncVal f = args.first();

					final VncHashMap options = VncHashMap.ofAll(args.rest());
					
					final boolean verbose = options.get(new VncKeyword("verbose")) == True ? true : false; 

					if (Types.isVncByteBuffer(f)) {
						Zipper.listZip(((VncByteBuffer)f).getValue().array(), System.out, verbose);
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						Zipper.listZip(Coerce.toVncJavaObject(f, File.class), System.out, verbose);
					}
					else if (Types.isVncString(f)) {
						Zipper.listZip(new File(Coerce.toVncString(f).getValue()), System.out, verbose);
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						Zipper.listZip(Coerce.toVncJavaObject(f, InputStream.class), System.out, verbose);
					}
					else {
						throw new VncException(String.format(
								"Function 'io/zip-list' does not allow %s as f",
								Types.getType(f)));
					}
				
					return Nil;
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_unzip_to_dir = 
		new VncFunction(
				"io/unzip-to-dir", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/unzip-to-dir f dir)")		
					.doc(
						"Unzips f to a directory. f may be a file, a string (file path), " +
						"a bytebuf, or an InputStream.")
					.examples(
						"(-> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
						"            \"b.txt\" (bytebuf-from-string \"def\" :utf-8)  \n" +
						"            \"c.txt\" (bytebuf-from-string \"ghi\" :utf-8)) \n" +
						"    (io/unzip-to-dir \".\")")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/unzip-to-dir", args, 2);
	
				final VncVal f = args.first();
				final File dir = Coerce.toVncJavaObject(args.second(), File.class);
				
				
				try {
					if (Types.isVncByteBuffer(f)) {
						Zipper.unzipToDir(((VncByteBuffer)f).getValue().array(), dir);
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						Zipper.unzipToDir(Coerce.toVncJavaObject(f, File.class), dir);
					}
					else if (Types.isVncString(f)) {
						Zipper.unzipToDir(new File(Coerce.toVncString(f).getValue()), dir);
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						Zipper.unzipToDir(Coerce.toVncJavaObject(f, InputStream.class), dir);
					}
					else {
						throw new VncException(String.format(
								"Function 'io/unzip-to-dir' does not allow %s as f",
								Types.getType(f)));
					}
					
					return Nil;
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_gzip = 
		new VncFunction(
				"io/gzip", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/gzip f)")		
					.doc(
						"gzips f. f may be a file, a string (file path), a bytebuf or an " +
						"InputStream. Returns a bytebuf.")
					.examples(
						"(->> (io/gzip \"a.txt\")  \n" +
						"     (io/spit \"a.gz\"))    ",
						
						"(io/gzip (bytebuf-from-string \"abcdef\" :utf-8))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/gzip", args, 1);
	
				final VncVal f = args.first();
				try {
					if (f == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(f)) {
						return new VncByteBuffer(Zipper.gzip(((VncByteBuffer)f).getValue().array()));
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						return new VncByteBuffer(Zipper.gzip(Coerce.toVncJavaObject(f, File.class)));
					}
					else if (Types.isVncString(f)) {
						return new VncByteBuffer(Zipper.gzip(new File(Coerce.toVncString(f).getValue())));
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						return new VncByteBuffer(Zipper.gzip((InputStream)((VncJavaObject)f).getDelegate()));
					}
					else {
						throw new VncException(String.format(
								"Function 'io/gzip' does not allow %s as f",
								Types.getType(f)));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_gzip_to_stream = 
		new VncFunction(
				"io/gzip-to-stream", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/gzip f os)")		
					.doc(
						"gzips f to the OutputStream os. f may be a file, a string " +
						"(file path), a bytebuf, or an InputStream.")
					.examples(
						"(do                                                 \n" +
						"  (import :java.io.ByteArrayOutputStream)           \n" +						
						"  (try-with [os (. :ByteArrayOutputStream :new)]    \n" +
						"      (-> (bytebuf-from-string \"abcdef\" :utf-8)   \n" +
						"          (io/gzip-to-stream os))                   \n" +
						"      (-> (. os :toByteArray)                       \n" +
						"          (io/ungzip)                               \n" +
						"          (bytebuf-to-string :utf-8))))               ")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/gzip-to-stream", args, 2);
	
				final VncVal f = args.first();
				final OutputStream os = (OutputStream)Coerce.toVncJavaObject(args.second()).getDelegate();
				try {
					if (f == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(f)) {
						Zipper.gzip(((VncByteBuffer)f).getValue().array(), os);
						return Nil;
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						Zipper.gzip(Coerce.toVncJavaObject(f, File.class));
						return Nil;
					}
					else if (Types.isVncString(f)) {
						Zipper.gzip(new File(Coerce.toVncString(f).getValue()));
						return Nil;
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						Zipper.gzip((InputStream)((VncJavaObject)f).getDelegate(), os);
						return Nil;
					}
					else {
						throw new VncException(String.format(
								"Function 'io/gzip-to-stream' does not allow %s as f",
								Types.getType(f)));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_ungzip = 
		new VncFunction(
				"io/ungzip", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/ungzip f)")		
					.doc(
						"ungzips f. f may be a file, a string (file path), a bytebuf, " +
						"or an InputStream. Returns a bytebuf.")
					.examples(
						"(-> (bytebuf-from-string \"abcdef\" :utf-8) \n" +
						"    (io/gzip) \n" +
						"    (io/ungzip))")	
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/ungzip", args, 1);
	
				final VncVal f = args.first();
				try {
					if (f == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(f)) {
						return new VncByteBuffer(Zipper.ungzip(((VncByteBuffer)f).getValue().array()));
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						return new VncByteBuffer(Zipper.ungzip(Coerce.toVncJavaObject(f, File.class)));
					}
					else if (Types.isVncString(f)) {
						return new VncByteBuffer(Zipper.ungzip(new File(Coerce.toVncString(f).getValue())));
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						return new VncByteBuffer(Zipper.ungzip((InputStream)((VncJavaObject)f).getDelegate()));
					}
					else {
						throw new VncException(String.format(
								"Function 'io/ungzip' does not allow %s as f",
								Types.getType(f)));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction io_ungzip_to_stream = 
		new VncFunction(
				"io/ungzip-to-stream", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/ungzip-to-stream buf)")		
					.doc(
						"ungzips a bytebuf returning an InputStream to read the deflated " +
						"data from.")
					.examples(
							"(-> (bytebuf-from-string \"abcdef\" :utf-8) \n" +
							"    (io/gzip) \n" +
							"    (io/ungzip-to-stream) \n" +
							"    (io/slurp-stream :binary false :encoding :utf-8))")	
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/ungzip-to-stream", args, 1);
	
				final VncVal buf = args.first();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(buf)) {
						return new VncJavaObject(Zipper.ungzipToStream(((VncByteBuffer)buf).getValue().array()));
					}
					else {
						throw new VncException(String.format(
								"Function 'io/ungzip-to-stream' does not allow %s as f",
								Types.getType(buf)));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_zip_Q = 
		new VncFunction(
				"io/zip?", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/zip? f)")		
					.doc(
						"Returns true if f is a zipped file. f may be a file, a string (file path), " +
						"a bytebuf, or an InputStream")
					.examples(
						"(-> (io/zip \"a\" (bytebuf-from-string \"abc\" :utf-8)) " +
						"    (io/zip?))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/zip?", args, 1);
	
				if (args.isEmpty()) {
					return False;
				}
				
				try {
					final VncVal f = args.first();
					
					if (Types.isVncByteBuffer(f)) {
						return Zipper.isZipFile(((VncByteBuffer)f).getValue().array()) ? True : False;
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						return Zipper.isZipFile(Coerce.toVncJavaObject(f, File.class)) ? True : False;
					}
					else if (Types.isVncString(f)) {
						return Zipper.isZipFile(new File(Coerce.toVncString(f).getValue())) ? True : False;
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						return Zipper.isZipFile((InputStream)((VncJavaObject)f).getDelegate()) ? True : False;
					}
					else {
						throw new VncException(String.format(
								"Function 'io/zip?' does not allow %s as f",
								Types.getType(f)));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_gzip_Q = 
		new VncFunction(
				"io/gzip?", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/gzip? f)")		
					.doc(
						"Returns true if f is a gzipped file. f may be a file, a string (file path), " +
						"a bytebuf, or an InputStream")
					.examples(
						"(-> (io/gzip (bytebuf-from-string \"abc\" :utf-8)) " +
						"    (io/gzip?))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/gzip?", args, 1);
	
				if (args.isEmpty()) {
					return False;
				}
				
				try {
					final VncVal f = args.first();
					
					if (Types.isVncByteBuffer(f)) {
						return Zipper.isGZipFile(((VncByteBuffer)f).getValue().array()) ? True : False;
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						return Zipper.isGZipFile(Coerce.toVncJavaObject(f, File.class)) ? True : False;
					}
					else if (Types.isVncString(f)) {
						return Zipper.isGZipFile(new File(Coerce.toVncString(f).getValue())) ? True : False;
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						return Zipper.isGZipFile((InputStream)((VncJavaObject)f).getDelegate()) ? True : False;
					}
					else {
						throw new VncException(String.format(
								"Function 'io/gzip?' does not allow %s as f",
								Types.getType(f)));
					}
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
					.module("io")
					.arglists("(io/download uri & options)")		
					.doc(
						"Downloads the content from the uri and reads it as text (string) " +
						"or binary (bytebuf). \n\n" +
						"Options: \n" +
						"  :binary true/false - e.g :binary true, defaults to false \n" +
						"  :encoding enc - e.g :encoding :utf-8, defaults to :utf-8")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("io/download", args, 1);
	
				final String uri = Coerce.toVncString(args.first()).getValue();

				try {	
					final VncHashMap options = VncHashMap.ofAll(args.rest());
					
					final VncVal binary = options.get(new VncKeyword("binary")); 

					final VncVal encVal = options.get(new VncKeyword("encoding")); 						
					final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();

					try (BufferedInputStream is = new BufferedInputStream(new URL(uri).openStream())) {
						byte data[] = IOStreamUtil.copyIStoByteArray(is);

						return binary == True
								? new VncByteBuffer(ByteBuffer.wrap(data))
								: new VncString(new String(data, encoding));
					}
				} 
				catch (Exception ex) {
					throw new VncException(ex.getMessage(), ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_copy_stream = 
		new VncFunction(
				"io/copy-stream", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/copy-file in-stream out-stream)")		
					.doc(
						"Copies input stream to an output stream. Returns nil or throws IOException. " + 
						"Input and output must be a java.io.InputStream and java.io.OutputStream.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/copy-stream", args, 2);
	
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
					.module("io")
					.arglists("(io/slurp-stream is & options)")		
					.doc(
						"Slurps binary or string data from a Java InputStream. " +
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
	
				try {	
					final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
									
					final VncHashMap options = VncHashMap.ofAll(args.rest());
	
					final VncVal binary = options.get(new VncKeyword("binary")); 
	
					if (binary == True) {
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
					.module("io")
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
	
				try {
					final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
			
					final VncVal content = args.second();
	
					final VncHashMap options = VncHashMap.ofAll(args.slice(2));
	
					final VncVal encVal = options.get(new VncKeyword("encoding")); 					
					final String encoding = encVal == Nil ? "UTF-8" : ((VncString)encVal).getValue();
	
					final VncVal flushVal = options.get(new VncKeyword("flush")); 
					final boolean flush = flushVal == True ? true : false;
	
					byte[] data;
					
					if (Types.isVncString(content)) {
						data = ((VncString)content).getValue().getBytes(encoding);
					}
					else if (Types.isVncByteBuffer(content)) {
						data = ((VncByteBuffer)content).getValue().array();
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
						.module("io")
						.arglists("(io/uri-stream uri)")		
						.doc("Returns a Java InputStream from the uri.")
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
					.module("io")
					.arglists("(io/wrap-os-with-buffered-writer os encoding?)")		
					.doc(
						"Wraps an OutputStream with a BufferedWriter using an optional " +
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
				"io/wrap-os-with-printwriter", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/wrap-os-with-print-writer os encoding?)")		
					.doc(
						"Wraps an OutputStream with a PrintWriter using an optional " +
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
						.module("io")
						.arglists("(io/wrap-is-with-buffered-reader is encoding?)")		
						.doc(
							"Wraps an InputStream with a BufferedReader using an optional " +
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
		
					try {
						final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
						final String encoding = args.size() == 1 ? "UTF-8" : ((VncString)args.second()).getValue();
							
						return new VncJavaObject(new BufferedReader(new InputStreamReader(is, encoding)));
					} 
					catch (Exception ex) {
						throw new VncException(ex.getMessage(), ex);
					}
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};
		
	public static VncFunction io_mime_type = 
		new VncFunction(
				"io/mime-type", 
				VncFunction
					.meta()
					.module("io")
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
					.module("io")
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
					.module("io")
					.arglists("(io/temp-dir prefix)")		
					.doc("Creates a temp directory with prefix.")
					.examples("(io/temp-dir \"test-\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/temp-dir", args, 1);
	
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
					.module("io")
					.arglists("(io/load-classpath-resource name)")		
					.doc("Loads a classpath resource.")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				try {	
					assertArity("io/load-classpath-resource", args, 1);
					
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
	
	public static VncFunction io_default_charset = 
		new VncFunction(
				"io/default-charset", 
				VncFunction
					.meta()
					.module("io")
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
	

	private static String encoding(final VncVal enc) {
		return enc == Nil 
				? "UTF-8" 
				: Types.isVncKeyword(enc)
					? Coerce.toVncKeyword(enc).getValue()
					: Coerce.toVncString(enc).getValue();
	}
	
	private static File convertToFile(final VncVal f, final String errFormat) {
		if (Types.isVncString(f)) {
			return new File(((VncString)f).getValue());
		}
		else if (Types.isVncJavaObject(f, File.class)) {
			return (File)((VncJavaObject)f).getDelegate();
		}
		else {
			throw new VncException(String.format(errFormat, f));
		}
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("io/file",							io_file)
					.put("io/file?",						io_file_Q)
					.put("io/file-path",					io_file_path)
					.put("io/file-canonical-path",			io_file_canonical_path)
					.put("io/file-absolute-path",			io_file_absolute_path)
					.put("io/file-parent",					io_file_parent)
					.put("io/file-name",					io_file_name)
					.put("io/file-size",					io_file_size)
					.put("io/exists-file?",					io_exists_file_Q)
					.put("io/exists-dir?",					io_exists_dir_Q)
					.put("io/list-files",					io_list_files)
					.put("io/delete-file",					io_delete_file)
					.put("io/delete-file-on-exit",			io_delete_file_on_exit)
					.put("io/copy-file",					io_copy_file)
					.put("io/move-file",					io_move_file)
					.put("io/mkdir",						io_mkdir)
					.put("io/mkdirs",						io_mkdirs)
					.put("io/temp-file",					io_temp_file)
					.put("io/temp-dir",						io_temp_dir)
					.put("io/tmp-dir",						io_tmp_dir)
					.put("io/user-dir",						io_user_dir)
					.put("io/slurp",						io_slurp)
					.put("io/slurp-lines",					io_slurp_lines)
					.put("io/spit",							io_spit)
					.put("io/download",						io_download)
					.put("io/copy-stream",					io_copy_stream)
					.put("io/slurp-stream",					io_slurp_stream)
					.put("io/spit-stream",					io_spit_stream)
					.put("io/uri-stream",					io_uri_stream)
					.put("io/wrap-os-with-buffered-writer",	io_wrap_os_with_buffered_writer)					
					.put("io/wrap-os-with-print-writer",	io_wrap_os_with_print_writer)					
					.put("io/wrap-is-with-buffered-reader",	io_wrap_is_with_buffered_reader)					
					.put("io/mime-type",					io_mime_type)
					.put("io/default-charset",				io_default_charset)
					.put("io/load-classpath-resource",		io_load_classpath_resource)
					.put("io/zip",							io_zip)
					.put("io/zip-append",					io_zip_append)
					.put("io/zip-file",						io_zip_file)
					.put("io/zip-list",						io_zip_list)
					.put("io/zip?",							io_zip_Q)
					.put("io/unzip",						io_unzip)
					.put("io/unzip-first",					io_unzip_first)
					.put("io/unzip-nth",					io_unzip_nth)
					.put("io/unzip-all",					io_unzip_all)
					.put("io/unzip-to-dir",					io_unzip_to_dir)
					.put("io/zip-size",						io_zip_size)
					.put("io/gzip",							io_gzip)
					.put("io/gzip?",						io_gzip_Q)
					.put("io/gzip-to-stream",				io_gzip_to_stream)
					.put("io/ungzip",						io_ungzip)
					.put("io/ungzip-to-stream",				io_ungzip_to_stream)
					.toMap();
}
