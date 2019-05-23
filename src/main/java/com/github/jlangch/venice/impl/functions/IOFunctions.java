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
import static com.github.jlangch.venice.impl.functions.FunctionsUtil.isJavaIoFile;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
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
						"Returns a java.io.File. path, parent, and child can be a string " +
						"or java.io.File")
					.examples(
						"(io/file \"/temp/test.txt\")",
						"(io/file \"/temp\" \"test.txt\")",
						"(io/file (io/file \"/temp\") \"test.txt\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/file", args, 1, 2);
							
				if (args.size() == 1) {
					final VncVal path = args.first();
					if (Types.isVncString(path)) {
						return new VncJavaObject(new File(((VncString)path).getValue()));
					}
					else if (isJavaIoFile(path) ) {
						return path;
					}
					else {
						throw new VncException(String.format(
								"Function 'io/file' does not allow %s as path",
								Types.getType(path)));
					}
				}
				else {
					final VncVal parent = args.first();
					final VncVal child = args.second();
					
					File parentFile;
	
					if (Types.isVncString(parent)) {
						parentFile = new File(((VncString)parent).getValue());
					}
					else if (isJavaIoFile(parent) ) {
						parentFile = (File)((VncJavaObject)parent).getDelegate();
					}
					else {
						throw new VncException(String.format(
								"Function 'io/file' does not allow %s as parent",
								Types.getType(parent)));
					}
	
					if (Types.isVncString(child)) {
						 return new VncJavaObject(new File(parentFile, ((VncString)child).getValue()));
					}
					else {
						throw new VncException(String.format(
								"Function 'io/file' does not allow %s as child",
								Types.getType(child)));
					}
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
					.doc("Returns the size of the file f. f must be a java.io.File.")
					.examples("(io/file-size (io/file \"/bin/sh\"))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/file-size", args, 1);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/file-size' does not allow %s as f",
							Types.getType(args.first())));
				}
	
				final File file = (File)((VncJavaObject)args.first()).getDelegate();
				return new VncLong(file.length());
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
					.doc("Returns the path of the file f. f must be a java.io.File.")
					.examples("(io/file-path (io/file \"/tmp/test/x.txt\"))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/file-path", args, 1);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/file-path' does not allow %s as f",
							Types.getType(args.first())));
				}
	
				final File file = (File)((VncJavaObject)args.first()).getDelegate();
				return new VncString(file.getPath());
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
					.doc("Returns the parent file of the file f. f must be a java.io.File.")
					.examples("(io/file-path (io/file-parent (io/file \"/tmp/test/x.txt\")))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/file-parent", args, 1);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/file-parent' does not allow %s as f",
							Types.getType(args.first())));
				}
	
				final File file = (File)((VncJavaObject)args.first()).getDelegate();
				return new VncJavaObject(file.getParentFile());
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
					.doc("Returns the name of the file f. f must be a java.io.File.")
					.examples("(io/file-name (io/file \"/tmp/test/x.txt\"))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/file-name", args, 1);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/file-name' does not allow %s as f",
							Types.getType(args.first())));
				}
	
				final File file = (File)((VncJavaObject)args.first()).getDelegate();
				return new VncString(file.getName());
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
	
				final VncVal path = args.first();
				return isJavaIoFile(path) ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_exists_file_Q = 
		new VncFunction(
				"io/exists-file?", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/exists-file? x)")		
					.doc("Returns true if the file x exists. x must be a java.io.File.")
					.examples("(io/exists-file? (io/file \"/temp/test.txt\"))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/exists-file?", args, 1);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/exists-file?' does not allow %s as x",
							Types.getType(args.first())));
				}
	
				final File file = (File)((VncJavaObject)args.first()).getDelegate();
				return file.isFile() ? True : False;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_exists_dir_Q = 
		new VncFunction(
				"io/exists-dir?", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/exists-dir? x)")		
					.doc("Returns true if the file x exists and is a directory. x must be a java.io.File.")
					.examples("(io/exists-dir? (io/file \"/temp\"))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/exists-dir?", args, 1);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/exists-dir?' does not allow %s as x",
							Types.getType(args.first())));
				}
	
				final File file = (File)((VncJavaObject)args.first()).getDelegate();
				return file.isDirectory() ? True : False;
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
					.doc("Deletes one or multiple files. f must be a java.io.File.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertMinArity("io/delete-file", args, 1);
	
				args.forEach(f -> {
					if (!isJavaIoFile(f) ) {
						throw new VncException(String.format(
								"Function 'io/delete-file' does not allow %s as f",
								Types.getType(args.first())));
					}
	
					final File file = (File)((VncJavaObject)f).getDelegate();
					try {
						Files.deleteIfExists(file.toPath());	
					}
					catch(Exception ex) {
						throw new VncException(
								String.format("Failed to delete file %s", file.getPath()),
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
					.arglists("(io/delete-file-on-exit x)")		
					.doc("Deletes a file on JVM exit. x must be a string or java.io.File.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/delete-file-on-exit", args, 1);
	
				File file;
				if (Types.isVncString(args.first()) ) {
					file = new File(((VncString)args.first()).getValue());
				}
				else if (isJavaIoFile(args.first()) ) {
					file = (File)((VncJavaObject)args.first()).getDelegate();
				}
				else {
					throw new VncException(String.format(
							"Function 'io/delete-file-on-exit' does not allow %s as x",
							Types.getType(args.first())));
				}
	
				try {
					file.deleteOnExit();;	
				}
				catch(Exception ex) {
					throw new VncException(
							String.format("Failed to marke file %s to delete on exit", file.getPath()),
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
						"Lists files in a directory. dir must be a java.io.File. filterFn " +
						"is an optional filter that filters the files found")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/list-files", args, 1, 2);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/list-files' does not allow %s as x",
							Types.getType(args.first())));
				}
				
				final File file = (File)((VncJavaObject)args.first()).getDelegate();
				try {
					final VncFunction filterFn = (args.size() == 2) ? Coerce.toVncFunction(args.second()) : null;
	
					final List<VncVal> files = new ArrayList<>();
	
					for(File f : file.listFiles()) {
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
							String.format("Failed to list files %s", file.getPath()), 
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
					.arglists("(io/copy-file input output)")		
					.doc(
						"Copies input to output. Returns nil or throws IOException. " + 
						"Input must be a java.io.File, output must be a java.io.File or java.io.OutputStream.")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertArity("io/copy-file", args, 2);
	
				if (!isJavaIoFile(args.first())) {
					throw new VncException(String.format(
							"Function 'io/copy-file' does not allow %s as input",
							Types.getType(args.first())));
				}

				
				final File from = (File)((VncJavaObject)args.first()).getDelegate();

				if (isJavaIoFile(args.second())) {
					final File to = (File)((VncJavaObject)args.second()).getDelegate();
					
					try {
						Files.copy(from.toPath(), to.toPath());
					}
					catch(Exception ex) {
						throw new VncException(
								String.format(
										"Failed to copy file %s to %s", 
										from.getPath(), 
										to.getPath()),
								ex);
					}
				}
				else if (Types.isVncJavaObject(args.second(), OutputStream.class)) {
					final Object os = Coerce.toVncJavaObject(args.second()).getDelegate();
					try {
						IOStreamUtil.copyFileToOS(from, (OutputStream)os);
					}
					catch(Exception ex) {
						throw new VncException(
								String.format(
										"Failed to copy file %s to stream", 
										from.getPath()),
								ex);
					}
				}
				else {
					throw new VncException(String.format(
							"Function 'io/copy-file' does not allow %s as output",
							Types.getType(args.second())));
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
						"Source and target must be a java.io.File.")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/move-file", args, 2);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/move-file' does not allow %s as source",
							Types.getType(args.first())));
				}
				if (!isJavaIoFile(args.second()) ) {
					throw new VncException(String.format(
							"Function 'io/move-file' does not allow %s as target",
							Types.getType(args.second())));
				}
	
	
				final File from = (File)((VncJavaObject)args.first()).getDelegate();
				final File to = (File)((VncJavaObject)args.second()).getDelegate();
				
				try {
					Files.move(from.toPath(), to.toPath());
				}
				catch(Exception ex) {
					throw new VncException(
							String.format(
									"Failed to move file %s to %s", 
									from.getPath(), 
									to.getPath()),
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
					.doc("Creates the directory. dir must be a java.io.File.")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/mkdir", args, 1);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/mkdir' does not allow %s as dir",
							Types.getType(args.first())));
				}
	
				final File dir = (File)((VncJavaObject)args.first()).getDelegate();				
				try {
					dir.mkdir();
				}
				catch(Exception ex) {
					throw new VncException(
							String.format(
									"Failed to create dir %s", 
									dir.getPath()),
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
						"parent directories. dir must be a java.io.File.")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("io/mkdirs", args, 1);
	
				if (!isJavaIoFile(args.first()) ) {
					throw new VncException(String.format(
							"Function 'io/mkdirs' does not allow %s as dir",
							Types.getType(args.first())));
				}
	
				final File dir = (File)((VncJavaObject)args.first()).getDelegate();				
				try {
					dir.mkdirs();
				}
				catch(Exception ex) {
					throw new VncException(
							String.format(
									"Failed to create dir %s", 
									dir.getPath()),
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
						"a Java InputStream, or a Java Reader. \n" + 
						"Defaults encoding=UTF-8. \n" +
						"Options: :encoding \"UTF-8")
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
						final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();

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
						final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();

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
						"Reads the content of f as text (string) or binary (bytebuf). " +
						"f may be a file, a string file path, a Java InputStream, " +
						"or a Java Reader. \n" +
						"Defaults to binary=false and encoding=UTF-8. \n" +
						"Options: :encoding \"UTF-8\" :binary true/false.")
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
							final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();
		
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
							final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();
		
							return new VncString(IOStreamUtil.copyIStoString(is, encoding));
						}
					}
					else if (Types.isVncJavaObject(arg, Reader.class)) {
						final BufferedReader rd = new BufferedReader(
														(Reader)(Coerce.toVncJavaObject(args.first()).getDelegate()));
						final String s = rd.lines().collect(Collectors.joining(System.lineSeparator()));
						
						if (binary == True) {
							final VncVal encVal = options.get(new VncKeyword("encoding")); 						
							final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();

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
						"f may be a file or a string file path. \n" +
						"Options default to append=true and encoding=UTF-8. \n" +
						"Options: :append true/false, :encoding \"UTF-8\"")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertMinArity("io/spit", args, 2);
	
				try {
					File file;
					
					if (Types.isVncString(args.first()) ) {
						file = new File(((VncString)args.first()).getValue());
					}
					else if (isJavaIoFile(args.first()) ) {
						file = (File)(Coerce.toVncJavaObject(args.first()).getDelegate());
					}
					else {
						throw new VncException(String.format(
								"Function 'io/spit' does not allow %s as f",
								Types.getType(args.first())));
					}
	
			
					final VncVal content = args.second();
	
					final VncHashMap options = VncHashMap.ofAll(args.slice(2));
	
					final VncVal append = options.get(new VncKeyword("append")); 
					
					final VncVal encVal = options.get(new VncKeyword("encoding")); 
						
					final String encoding = encVal == Nil ? "UTF-8" : ((VncString)encVal).getValue();
	
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
					
					if (append != False) {
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

	public static VncFunction io_download = 
		new VncFunction(
				"io/download", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/download uri & options)")		
					.doc(
						"Downloads the content from uri and reads it as text (string) " +
						"or binary (bytebuf). \n" +
						"Defaults to binary=false and encoding=UTF-8. \n" +
						"Options: :encoding \"UTF-8\" :binary true/false.")
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
			
				if (!(is instanceof InputStream) ) {
					throw new VncException(String.format(
							"Function 'io/copy-stream' does not allow %s as in-stream",
							Types.getType(args.first())));
				}
				if (!(os instanceof OutputStream) ) {
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
						"For string data an optional encoding can be specified.\n" +
						"Options: :encoding \"UTF-8\" :binary true/false. ")
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
						"the operation.\n" +
						"Options: :flush true/false :encoding \"UTF-8\"")
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

	public static VncFunction io_wrap_os_with_buffered_writer = 
		new VncFunction(
				"io/wrap-os-with-buffered-writer", 
				VncFunction
					.meta()
					.module("io")
					.arglists("(io/wrap-os-with-buffered-writer os encoding?)")		
					.doc("Wraps an OutputStream with a BufferedWriter using an optional encoding (defaults to UTF-8).")
					.examples(
						"(do                                                         \n" +
						"   (import :java.io.ByteArrayOutputStream)                  \n" +
						"   (let [os (. :ByteArrayOutputStream :new)                 \n" +
						"         wr (io/wrap-os-with-buffered-writer os \"utf-8\")] \n" +
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
					.doc("Wraps an OutputStream with a PrintWriter using an optional encoding (defaults to UTF-8).")
					.examples(
						"(do                                                      \n" +
						"   (import :java.io.ByteArrayOutputStream)               \n" +
						"   (let [os (. :ByteArrayOutputStream :new)              \n" +
						"         wr (io/wrap-os-with-print-writer os \"utf-8\")] \n" +
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
						.doc("Wraps an InputStream with a BufferedReader using an optional encoding (defaults to UTF-8).")
						.examples(
							"(do                                                                          \n" +
							"   (import :java.io.ByteArrayInputStream)                                    \n" +						
							"   (let [data (byte-array [108 105 110 101 32 49 10 108 105 110 101 32 50])  \n" +
							"         is (. :ByteArrayInputStream :new data)                              \n" +
							"         rd (io/wrap-is-with-buffered-reader is \"utf-8\")]                  \n" +
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
					.doc("Returns the mime-type for the file if available else nil")
					.examples(
						"(io/mime-type \"document.pdf\")",
						"(io/mime-type (io/file \"document.pdf\"))")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("io/mime-type", args, 1);
	
				final VncVal file = args.first();
				
				if (Types.isVncString(file) ) {
					return new VncString(
								MimeTypes.getMimeTypeFromFileName(
										((VncString)file).getValue()));
				}
				else if (isJavaIoFile(file) ) {
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
					final String path = File.createTempFile(prefix, suffix).getPath();
					tempFiles.add(path);
					return new VncString(path);
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
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("io/file",							io_file)
					.put("io/file?",						io_file_Q)
					.put("io/file-path",					io_file_path)
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
					.put("io/tmp-dir",						io_tmp_dir)
					.put("io/user-dir",						io_user_dir)
					.put("io/slurp",						io_slurp)
					.put("io/slurp-lines",					io_slurp_lines)
					.put("io/spit",							io_spit)
					.put("io/download",						io_download)
					.put("io/copy-stream",					io_copy_stream)
					.put("io/slurp-stream",					io_slurp_stream)
					.put("io/spit-stream",					io_spit_stream)
					.put("io/wrap-os-with-buffered-writer",	io_wrap_os_with_buffered_writer)					
					.put("io/wrap-os-with-print-writer",	io_wrap_os_with_print_writer)					
					.put("io/wrap-is-with-buffered-reader",	io_wrap_is_with_buffered_reader)					
					.put("io/mime-type",					io_mime_type)
					.put("io/default-charset",				io_default_charset)
					.put("io/load-classpath-resource",		io_load_classpath_resource)
					.toMap();

	
	private static final HashSet<String> tempFiles = new HashSet<>();
}
