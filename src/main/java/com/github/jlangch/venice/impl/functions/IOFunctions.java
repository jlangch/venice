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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.VncBoolean.False;
import static com.github.jlangch.venice.impl.types.VncBoolean.True;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
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
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.VncException;
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
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalSnapshot;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.MimeTypes;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;


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
						"(io/file \"/tmp/test.txt\")",
						"(io/file \"/temp\" \"test.txt\")",
						"(io/file \"/temp\" \"test\" \"test.txt\")",
						"(io/file (io/file \"/temp\") \"test\" \"test.txt\")",
						"(io/file (. :java.io.File :new \"/tmp/test.txt\"))")
					.seeAlso("io/file-name", "io/file-parent", "io/file-path", "io/file-absolute-path", "io/file-canonical-path")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

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

					File file = parent;
					for(VncVal child : args.rest()) {
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
					.examples("(io/file-size \"/tmp/test.txt\")")
					.seeAlso("io/file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				sandboxFunctionCallValidation();

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
					.seeAlso("io/file-absolute-path", "io/file-canonical-path", "io/file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

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
					.seeAlso("io/file-path", "io/file-absolute-path", "io/file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

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
					.seeAlso("io/file-path", "io/file-canonical-path", "io/file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

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
					.seeAlso("io/file-name", "io/file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

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
					.seeAlso("io/file-parent", "io/file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

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
					.seeAlso("io/file-ext")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-ext?' does not allow %s as f");

				final String ext = Coerce.toVncString(args.second()).getValue();
				return VncBoolean.of(f.getName().endsWith(ext.startsWith(".") ? ext : "." + ext));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_ext =
		new VncFunction(
				"io/file-ext",
				VncFunction
					.meta()
					.arglists(
						"(io/file-ext f)")
					.doc(
						"Returns the file extension of a file. " +
						"f must be a file or a string (file path).")
					.examples(
						"(io/file-ext \"some.txt\")",
						"(io/file-ext \"/tmp/test/some.txt\")",
						"(io/file-ext \"/tmp/test/some\")")
					.seeAlso("io/file-ext?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-ext' does not allow %s as f");

				final String ext = FileUtil.getFileExt(f.getName());
				return ext == null ? Nil : new VncString(ext);
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
					.examples("(io/file? (io/file \"/tmp/test.txt\"))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

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
					.doc("Returns true if the file f exists and is a file. f must be a file or a string (file path).")
					.examples("(io/exists-file? \"/tmp/test.txt\")")
					.seeAlso("io/exists-dir?", "io/file-symbolic-link?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				sandboxFunctionCallValidation();

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
					.seeAlso("io/exists-file?", "io/file-symbolic-link?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				sandboxFunctionCallValidation();

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
					.examples("(io/file-can-read? \"/tmp/test.txt\")")
					.seeAlso("io/file-can-write?", "io/file-can-execute?", "io/file-hidden?", "io/file-symbolic-link?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

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
					.examples("(io/file-can-write? \"/tmp/test.txt\")")
					.seeAlso("io/file-can-read?", "io/file-can-execute?", "io/file-hidden?", "io/file-symbolic-link?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

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
					.examples("(io/file-can-execute? \"/tmp/test.txt\")")
					.seeAlso("io/file-can-read?", "io/file-can-write?", "io/file-hidden?", "io/file-symbolic-link?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

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
					.examples("(io/file-hidden? \"/tmp/test.txt\")")
					.seeAlso("io/file-can-read?", "io/file-can-write?", "io/file-can-execute?", "io/file-symbolic-link?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-hidden?' does not allow %s as x");

				return VncBoolean.of((f.isFile() || f.isDirectory()) && f.isHidden());
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_symbolicl_link_Q =
		new VncFunction(
				"io/file-symbolic-link?",
				VncFunction
					.meta()
					.arglists("(io/file-symbolic-link? f)")
					.doc(
						"Returns true if the file f exists and is a symbolic link. " +
						"f must be a file or a string (file path).")
					.examples("(io/file-symbolic-link? \"/tmp/test.txt\")")
					.seeAlso("io/file-hidden?", "io/file-can-read?", "io/file-can-write?", "io/file-can-execute?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/symbolic-link?' does not allow %s as x");

				final Path p = f.toPath();
				
				return VncBoolean.of(Files.isSymbolicLink(p));
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_file_last_modified =
		new VncFunction(
				"io/file-last-modified",
				VncFunction
					.meta()
					.arglists("(io/file-last-modified f)")
					.doc(
						"Returns the last modification time (a Java LocalDateTime) of f or nil " +
						"if f does not exist. f must be a file or a string (file path).")
					.examples("(io/file-last-modified \"/tmp/test.txt\")")
					.seeAlso("io/file-can-read?", "io/file-can-write?", "io/file-can-execute?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				final File f = convertToFile(
									args.first(),
									"Function 'io/file-last-modified' does not allow %s as x");

				if (f.exists()) {
					final long millis = f.lastModified();
					return new VncJavaObject(
									Instant.ofEpochMilli(millis)
										   .atZone(ZoneId.systemDefault())
										   .toLocalDateTime());
				}
				else {
					return Nil;
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	// https://github.com/juxt/dirwatch/blob/master/src/juxt/dirwatch.clj
	public static VncFunction io_await_for =
		new VncFunction(
				"io/await-for",
				VncFunction
					.meta()
					.arglists("(io/await-for timeout time-unit file & modes)")		
					.doc(
						"Blocks the current thread until the file has been created, deleted, or " + 
						"modified according to the passed modes {:created, :deleted, :modified}, " +
						"or the timeout has elapsed. Returns logical false if returning due to " +
						"timeout, logical true otherwise. \n\n" +
						"Supported time units are: {:milliseconds, :seconds, :minutes, :hours, :days}")
					.examples(
						"(io/await-for 10 :seconds \"/tmp/data.json\" :created)")
					.seeAlso("io/watch-dir")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 3);

				sandboxFunctionCallValidation();

				final long timeout = Coerce.toVncLong(args.first()).getValue();

				final TimeUnit unit = toTimeUnit(Coerce.toVncKeyword(args.second()));

				final long timeoutMillis = unit.toMillis(Math.max(0,timeout));

				final File file = convertToFile(
										args.third(),
										"Function 'io/await-for' does not allow %s as file").getAbsoluteFile();

			
				final Set<WatchEvent.Kind<?>> events = new HashSet<>();
				for(VncVal v : args.slice(3)) {
					final VncKeyword mode = Coerce.toVncKeyword(v);
					switch(mode.getSimpleName()) {
						case "created":
							events.add(StandardWatchEventKinds.ENTRY_CREATE);
							break;
						case "deleted":
							events.add(StandardWatchEventKinds.ENTRY_DELETE);
							break;
						case "modified":
							events.add(StandardWatchEventKinds.ENTRY_MODIFY);
							break;
						default:
							throw new VncException(
									String.format(
											"Function 'io/await-for' invalid mode '%s'. Use one or " +
											"multiple of {:created, :deleted, :modified}",
											mode.toString()));
					}
				}
				
				if (events.isEmpty()) {
					throw new VncException(
							"Function 'io/await-for' missing a mode. Pass one or " +
							"multiple of {:created, :deleted, :modified}");
				}
				
				try {
					return VncBoolean.of(FileUtil.awaitFile(
											file.getCanonicalFile().toPath(), 
											timeoutMillis, 
											events));
				}
				catch(InterruptedException ex) {
					throw new com.github.jlangch.venice.InterruptedException(
							"Interrupted while calling function 'io/await-for'", ex);
				}
				catch(IOException ex) {
					throw new VncException(
							String.format(
									"Function 'io/await-for' failed to await for file '%s'",
									file.getPath()),
							ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_watch_dir =
		new VncFunction(
				"io/watch-dir",
				VncFunction
					.meta()
					.arglists(
						"(io/watch-dir dir event-fn)",	
						"(io/watch-dir dir event-fn failure-fn)",		
						"(io/watch-dir dir event-fn failure-fn termination-fn)")		
					.doc(
						"Watch a directory for changes, and call the function `event-fn` when it " +
						"does. Calls the optional `failure-fn` if errors occur. On closing " +
						"the watcher `termination-fn` is called. \n\n" +
						"`event-fn` is a two argument function that receives the path and mode " +
						"{:created, :deleted, :modified} of the changed file. \n\n" +
						"`failure-fn` is a two argument function that receives the watch dir and the " +
						"failure exception. \n\n" +
						"`termination-fn` is a one argument function that receives the watch dir.\n\n" +
						"Returns a *watcher* that is activley watching a directory. The *watcher* is \n" +
						"a resource which should be closed with `(io/close-watcher w)`.")
					.examples(
						"(do                                                           \n" +
					    "  (defn log [msg] (locking log (println msg)))                \n" +
						"                                                              \n" +
						"  (let [w (io/watch-dir \"/tmp\" #(log (str %1 \" \" %2)))]   \n" +
					    "    (sleep 30 :seconds)                                       \n" +
						"    (io/close-watcher w)))                                      ")
					.seeAlso("io/await-for")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2, 3, 4);

				sandboxFunctionCallValidation();

				final File dir = convertToFile(
										args.first(),
										"Function 'io/watch-dir' does not allow %s as file").getAbsoluteFile();	
				
				if (!dir.isDirectory()) {
					throw new VncException(
							String.format(
									"Function 'io/watch-dir': dir '%s' is not a directpry",
									dir.toString()));
				}
				
				final VncFunction eventFn = Coerce.toVncFunction(args.second());
				final VncFunction failFn = Coerce.toVncFunctionOptional(args.third());
				final VncFunction termFn = Coerce.toVncFunctionOptional(args.fourth());
				
				final Function<WatchEvent.Kind<?>, VncKeyword> convert = (event) -> {
					switch(event.name()) {
						case "ENTRY_CREATE": return new VncKeyword("created");
						case "ENTRY_DELETE": return new VncKeyword("deleted");
						case "ENTRY_MODIFY": return new VncKeyword("modified");
						default: return new VncKeyword("unknown");
					}
				};
				
				// thread local values from the parent thread
				final AtomicReference<ThreadLocalSnapshot> parentThreadLocalSnapshot = 
						new AtomicReference<>(ThreadLocalMap.snapshot());

				final Consumer<Runnable> wrapper = (runnable) -> {
					// The watch-dir listeners is called from the JavaVM. Rig a
					// Venice context with the thread local vars and the sandbox
					try {
						// inherit thread local values to the child thread
						ThreadLocalMap.inheritFrom(parentThreadLocalSnapshot.get());
						ThreadLocalMap.clearCallStack();
						
						runnable.run();
					}
					finally {
						// clean up
						ThreadLocalMap.remove();
					}
				};
				
				final BiConsumer<Path,WatchEvent.Kind<?>> eventListener =
						(path, event) -> wrapper.accept( () ->	
											ConcurrencyFunctions.future.applyOf(
												CoreFunctions.partial.applyOf(
														eventFn,
														new VncString(path.toString()),
														convert.apply(event))));
						
				final BiConsumer<Path,Exception> errorListener =
						failFn == null 
							? null
							: (path, ex) -> wrapper.accept( () ->
												ConcurrencyFunctions.future.applyOf(
													CoreFunctions.partial.applyOf(
														failFn,
														new VncString(path.toString()),
														new VncJavaObject(ex))));
							
				final Consumer<Path> terminationListener =
						termFn == null 
							? null
							: (path) -> wrapper.accept( () ->
											ConcurrencyFunctions.future.applyOf(
												CoreFunctions.partial.applyOf(
													termFn,
													new VncString(path.toString()))));

				try {
					return new VncJavaObject(
									FileUtil.watchDir(
										dir.toPath(), 
										eventListener,
										errorListener,
										terminationListener));
				}
				catch(IOException ex) {
					throw new VncException(
							String.format(
									"Function 'io/watch-dir' failed to watch dir '%s'",
									dir.toString()),
							ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction io_close_watcher =
		new VncFunction(
				"io/close-watcher",
				VncFunction
					.meta()
					.arglists("(io/close-watcher watcher)")		
					.doc("Closes a watcher created from 'io/watch-dir'.")
					.seeAlso("io/watch-dir")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				final WatchService ws = Coerce.toVncJavaObject(args.first(), WatchService.class);
				try {
					ws.close();
					return Nil;
				}
				catch(IOException ex) {
					throw new VncException(
							"Function 'io/close-watcher' failed to close watch service",
							ex);
				}
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
					.seeAlso(
						"io/delete-file-tree", 
						"io/delete-file-on-exit", 
						"io/copy-file", 
						"io/move-file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 0);

				sandboxFunctionCallValidation();

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
					.seeAlso("io/delete-file", "io/delete-file-on-exit")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

				sandboxFunctionCallValidation();

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
					.seeAlso("io/delete-file", "io/delete-file-tree")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

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
						"`filter-fn` is an optional filter that filters the files found. The filter " +
						"gets a `java.io.File` as argument. Returns files as `java.io.File`")
					.examples(
						"(io/list-files \"/tmp\")",
						"(io/list-files \"/tmp\" #(io/file-ext? % \".log\"))")
					.seeAlso("io/list-file-tree", "io/list-files-glob")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);
				
				sandboxFunctionCallValidation();

				final File dir = convertToFile(
									args.first(),
									"Function 'io/list-files' does not allow %s as dir");

				validateReadableDirectory(dir);

				try {
					final VncFunction filterFn = args.size() == 2 
													? Coerce.toVncFunction(args.second()) 
													: null;

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
						"string (file path). `filter-fn` is an optional filter that filters " + 
						"the files found. The filter gets a `java.io.File` as argument. " +
						"Returns files as `java.io.File`")
					.examples(
						"(io/list-file-tree \"/tmp\")",
						"(io/list-file-tree \"/tmp\" #(io/file-ext? % \".log\"))")
					.seeAlso("io/list-files", "io/list-files-glob")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);
				
				sandboxFunctionCallValidation();

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
							String.format("Failed to list files from %s", dir.getPath()),
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
					    "Returns files as `java.io.File`\n\n" +
					    "Globbing patterns: \n\n" +
					    "| `*.txt`       | Matches a path that represents a file name ending in .txt |\n" +
					    "| `*.*`         | Matches file names containing a dot |\n" +
					    "| `*.{txt,xml}` | Matches file names ending with .txt or .xml |\n" +
					    "| `foo.?`       | Matches file names starting with foo. and a single character extension |\n" +
					    "| `/home/*/*`   | Matches `/home/gus/data` on UNIX platforms |\n" +
					    "| `/home/**`    | Matches `/home/gus` and `/home/gus/data` on UNIX platforms |\n" +
					    "| `C:\\\\*`     | Matches `C:\\\\foo` and `C:\\\\bar` on the Windows platform |\n")
					.examples(
						"(io/list-files-glob \".\" \"sample*.txt\")")
					.seeAlso("io/list-files", "io/list-file-tree")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
				
				sandboxFunctionCallValidation();

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
						"Copies source to dest. Returns nil or throws a VncException. " +
						"Source must be a file or a string (file path), dest must be a file, " +
						"a string (file path), or an `java.io.OutputStream`.\n\n" +
						"Options: \n\n" +
						"| :replace true/false | e.g if true replace an existing file, defaults to false |\n")
					.seeAlso("io/move-file", "io/delete-file", "io/copy-stream")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				sandboxFunctionCallValidation();

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
						"Moves source to target. Returns nil or throws a VncException. " +
						"Source and target must be a file or a string (file path).")
					.seeAlso("io/copy-file", "io/delete-file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
				
				sandboxFunctionCallValidation();

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
					.seeAlso("io/mkdirs")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				sandboxFunctionCallValidation();

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
					.seeAlso("io/mkdir")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				sandboxFunctionCallValidation();

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
					.doc("Returns the tmp dir as a `java.io.File`.")
					.examples("(io/tmp-dir)")
					.seeAlso("io/user-dir", "io/user-home-dir", "io/temp-dir")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
				
				sandboxFunctionCallValidation();

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
					.seeAlso("io/tmp-dir", "io/user-home-dir")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);
				
				sandboxFunctionCallValidation();

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
					.doc("Returns the user's home dir as a `java.io.File`.")
					.seeAlso("io/user-dir", "io/tmp-dir")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0);

				sandboxFunctionCallValidation();

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
						"a `java.io.InputStream`, or a `java.io.Reader`. \n\n" +
						"Options: \n\n" +
						"| :encoding enc | e.g :encoding :utf-8, defaults to :utf-8 |\n")
					.seeAlso("io/slurp", "io/slurp-stream", "io/spit")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);
				
				sandboxFunctionCallValidation();

				final VncVal arg = args.first();

				final VncHashMap options = VncHashMap.ofAll(args.rest());

				if (Types.isVncString(arg) || Types.isVncJavaObject(arg, File.class)) {
					final File file = Types.isVncString(arg)
										? new File(((VncString)arg).getValue())
										:  (File)(Coerce.toVncJavaObject(args.first()).getDelegate());

					try {
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
					catch (Exception ex) {
						throw new VncException(
								"Failed to slurp text lines from the file " + file.getPath(), 
								ex);
					}
				}
				else if (Types.isVncJavaObject(arg, InputStream.class)) {
					try {
						final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
	
						final VncVal encVal = options.get(new VncKeyword("encoding"));
						final String encoding = encoding(encVal);
	
						try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, encoding))) {
							return VncList.ofList(rd.lines().map(s -> new VncString(s)).collect(Collectors.toList()));
						}
					}
					catch (Exception ex) {
						throw new VncException("Failed to slurp text lines from a :java.io.InputStream", ex);
					}
				}
				else if (Types.isVncJavaObject(arg, Reader.class)) {
					try {
						final Reader rd = (Reader)(Coerce.toVncJavaObject(args.first()).getDelegate());
												
						try (BufferedReader brd = new BufferedReader(rd)) {
							return VncList.ofList(brd.lines().map(s -> new VncString(s)).collect(Collectors.toList()));
						}
					}
					catch (Exception ex) {
						throw new VncException("Failed to slurp text lines from a :java.io.Reader", ex);
					}
				}
				else {
					throw new VncException(String.format(
							"Function 'io/slurp-lines' does not allow %s as f",
							Types.getType(args.first())));
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
						"f may be a file, a string file path, a `java.io.InputStream`, " +
						"or a `java.io.Reader`. \n\n" +
						"Options: \n\n" +
						"| :binary true/false | e.g :binary true, defaults to false |\n" +
						"| :encoding enc      | e.g :encoding :utf-8, defaults to :utf-8 |\n")
					.seeAlso("io/slurp-lines", "io/slurp-stream", "io/spit")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);
				
				sandboxFunctionCallValidation();

				final VncVal arg = args.first();

				final VncHashMap options = VncHashMap.ofAll(args.rest());
				final VncVal binary = options.get(new VncKeyword("binary"));

				if (Types.isVncString(arg) || Types.isVncJavaObject(arg, File.class)) {
					final File file = Types.isVncString(arg)
										? new File(((VncString)arg).getValue())
										:  (File)(Coerce.toVncJavaObject(args.first()).getDelegate());
					try {
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
					catch (Exception ex) {
						throw new VncException("Failed to slurp data from the file " + file.getPath(), ex);
					}
				}
				else if (Types.isVncJavaObject(arg, InputStream.class)) {
					try {
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
					catch (Exception ex) {
						throw new VncException("Failed to slurp data from a :java.io.InputStream", ex);
					}
				}
				else if (Types.isVncJavaObject(arg, Reader.class)) {
					try {
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
					catch (Exception ex) {
						throw new VncException("Failed to slurp data from a :java.io.Reader", ex);
					}
				}
				else {
					throw new VncException(String.format(
							"Function 'io/slurp' does not allow %s as f",
							Types.getType(args.first())));
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
						"Options: \n\n" +
						"| :append true/false | e.g :append true, defaults to false |\n" +
						"| :encoding enc      | e.g :encoding :utf-8, defaults to :utf-8 |\n")
					.seeAlso("io/spit-stream", "io/slurp", "io/slurp-lines")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);
				
				sandboxFunctionCallValidation();

				final File file = convertToFile(
									args.first(),
									"Function 'io/spit' does not allow %s as f");

				try {
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
					throw new VncException(
							"Failed to spit data to the file " + file.getPath(), 
							ex);
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
						"Options: \n\n" +
						"| :binary true/false | e.g :binary true, defaults to false |\n" +
						"| :user-agent agent  | e.g :user-agent \"Mozilla\", defaults to nil |\n" +
						"| :encoding enc      | e.g :encoding :utf-8, defaults to :utf-8 |\n" +
						"| :conn-timeout val  | e.g :conn-timeout 10000, " +
						"                       connection timeout in milliseconds. ¶" +
						"                       0 is interpreted as an infinite timeout. |\n" +
						"| :read-timeout val  | e.g :read-timeout 10000, " +
						"                       read timeout in milliseconds. ¶" +
						"                       0 is interpreted as an infinite timeout. |\n" +
						"| :progress-fn fn    | a progress function that takes 2 args ¶" +
						"                       [1] progress (0..100%) ¶" +
						"                       [2] status {:start :progress :end :failed}|\n\n" +
						"Note:¶" +
						"If the server returns the HTTP response status code 403 (Access Denied) " +
						"sending a user agent like \"Mozilla\" may fool the website and solve the " +
						"problem.")
					.examples(
						"(-<> \"https://live.staticflickr.com/65535/51007202541_ea453871d8_o_d.jpg\"\n" +
						"     (io/download <> :binary true :user-agent \"Mozilla\")\n" +
						"     (io/spit \"space-x.jpg\" <>))",
						"(do \n" +
						"  (load-module :ansi) \n" +
						"  (-<> \"https://live.staticflickr.com/65535/51007202541_ea453871d8_o_d.jpg\" \n" +
						"       (io/download <> :binary true \n" +
						"                       :user-agent \"Mozilla\" \n" +
						"                       :progress-fn (ansi/progress :caption \"Download:\")) \n" +
						"       (io/spit \"space-x.jpg\" <>)))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

				sandboxFunctionCallValidation();

				final String uri = Coerce.toVncString(args.first()).getValue();

				VncFunction progressFn = null;
				
				try {
					final VncHashMap options = VncHashMap.ofAll(args.rest());
					final VncVal binary = options.get(new VncKeyword("binary"));
					final VncVal useragent = options.get(new VncKeyword("user-agent"));
					final VncVal encVal = options.get(new VncKeyword("encoding"));
					final VncVal progressVal = options.get(new VncKeyword("progress-fn"));
					final VncVal connTimeoutMillisVal = options.get(new VncKeyword("conn-timeout"));
					final VncVal readTimeoutMillisVal = options.get(new VncKeyword("read-timeout"));
					
					final String encoding = encVal == Nil ? "UTF-8" : Coerce.toVncString(encVal).getValue();
					progressFn = progressVal == Nil 
										? new VncFunction("io/progress-default") {
											private static final long serialVersionUID = 1L;
											public VncVal apply(final VncList args) { return Nil; }
										  }
										: Coerce.toVncFunction(progressVal);
	
					updateDownloadProgress(progressFn, 0L, new VncKeyword("start"));

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
	
						try (BufferedInputStream is = new BufferedInputStream(conn.getInputStream())) {
							final ByteArrayOutputStream output = new ByteArrayOutputStream();							
							try {
								updateDownloadProgress(progressFn, 0L, new VncKeyword("progress"));

								final byte[] buffer = new byte[16 * 1024];
								int n;
								long total = 0L;
								long progressLast = 0L;
								while (-1 != (n = is.read(buffer))) {
									output.write(buffer, 0, n);
									total += n;

									// progress: 0..100%
									long progress = Math.max(0, Math.min(100, (total * 100) / contentLength));

									if (progress != progressLast) {
										updateDownloadProgress(progressFn, progress, new VncKeyword("progress"));
									}

									progressLast = progress;
								}

								if (progressVal != Nil) {
									updateDownloadProgress(progressFn, 100L, new VncKeyword("progress"));
									Thread.sleep(100); // leave the 100% progress for a blink of an eye
								}

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
						throw ex;
					}
					finally {
						if (conn instanceof HttpURLConnection) {
							((HttpURLConnection)conn).disconnect();
						}
						updateDownloadProgress(progressFn, 100L, new VncKeyword("end"));
					}
				}
				catch (Exception ex) {
					if (progressFn != null) {
						updateDownloadProgress(progressFn, 0L, new VncKeyword("failed"));
					}
					
					throw new VncException("Failed to download data from the URI: " + uri, ex);
				}
			}

			private static final long serialVersionUID = -1848883965231344442L;
		};


	public static VncFunction io_internet_avail_Q =
		new VncFunction(
				"io/internet-avail?",
				VncFunction
					.meta()
					.arglists("(io/internet-avail?)", "(io/internet-avail? url)")
					.doc("Checks if an internet connection is present for a given url. "
							+ "Defaults to URL *http://www.google.com*.")
					.examples(
						"(io/internet-avail? \"http://www.google.com\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 0, 1);

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
					.arglists("(io/copy-strean in-stream out-stream)")
					.doc(
						"Copies the input stream to the output stream. Returns nil or throws a VncException. " +
						"Input and output must be a `java.io.InputStream` and `java.io.OutputStream`.")
					.seeAlso("io/copy-file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				sandboxFunctionCallValidation();

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
					throw new VncException(
							"Failed to copy data from a :java.io.InputStream to an :java.io.OutputStream");
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
						"Slurps binary or string data from a `java.io.InputStream` is. " +
						"Supports the option :binary to either slurp binary or string data. " +
						"For string data an optional encoding can be specified.\n\n" +
						"Options: \n\n" +
						"| :binary true/false | e.g :binary true, defaults to false |\n" +
						"| :encoding enc      | e.g :encoding :utf-8, defaults to :utf-8 |\n")
					.examples(
						"(do \n" +
						"   (import :java.io.FileInputStream) \n" +
						"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
						"        (io/delete-file-on-exit file) \n" +
						"        (io/spit file \"123456789\" :append true) \n" +
						"        (try-with [is (. :FileInputStream :new file)] \n" +
						"           (io/slurp-stream is :binary false))) \n" +
						")")
					.seeAlso("io/slurp", "io/slurp-lines", "io/spit")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);
				
				sandboxFunctionCallValidation();

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
					throw new VncException(
							"Failed to slurp data from a :java.io.InputStream", 
							ex);
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
						"Writes content (string or bytebuf) to the `java.io.OutputStream` os. " +
						"If content is of type string an optional encoding (defaults to " +
						"UTF-8) is supported. The stream can optionally be flushed after " +
						"the operation.\n\n" +
						"Options: \n\n" +
						"| :flush true/false | e.g :flush true, defaults to false |\n" +
						"| :encoding enc     | e.g :encoding :utf-8, defaults to :utf-8 |\n")
					.examples(
						"(do \n" +
						"   (import :java.io.FileOutputStream) \n" +
						"   (let [file (io/temp-file \"test-\", \".txt\")] \n" +
						"        (io/delete-file-on-exit file) \n" +
						"        (try-with [os (. :FileOutputStream :new file)] \n" +
						"           (io/spit-stream os \"123456789\" :flush true))) \n" +
						")")
					.seeAlso("io/spit")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);
				
				sandboxFunctionCallValidation();

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
					throw new VncException(
							"Failed to spit data to a :java.io.OutputStream", 
							ex);
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
						.doc("Returns a `java.io.InputStream` from the uri.")
						.examples(
							"(-> (io/uri-stream \"https://www.w3schools.com/xml/books.xml\") \n" + 
							"    (io/slurp-stream :binary false :encoding :utf-8))             ")
						.build()
			) {
				public VncVal apply(final VncList args) {
					ArityExceptions.assertMinArity(this, args, 1);
					
					sandboxFunctionCallValidation();

					final String uri = Coerce.toVncString(args.first()).getValue();

					try {
						return new VncJavaObject(new URL(uri).openStream());
					}
					catch (Exception ex) {
						throw new VncException(
								"Failed to create a :java.io.InputStream from an URI", 
								ex);
					}
				}

				private static final long serialVersionUID = -1848883965231344442L;
			};

	public static VncFunction io_bytebuf_in_stream =
		new VncFunction(
				"io/bytebuf-in-stream",
				VncFunction
					.meta()
					.arglists("(io/bytebuf-in-stream)")
					.doc("Returns a `java.io.InputStream` from a bytebuf.")
					.examples(
						"(io/bytebuf-in-stream (bytebuf [97 98 99]))")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
	
				final ByteBuffer buf = Coerce.toVncByteBuffer(args.first()).getValue();
	
				try {
					return new VncJavaObject(new ByteArrayInputStream(buf.array()));
				}
				catch(Exception ex) {
					throw new VncException(String.format(
							"Failed to create a :java.io.InputStream from a bytebuf"));
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
						"Wraps a `java.io.OutputStream` os with a `java.io.BufferedWriter` using an optional " +
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
					.seeAlso("io/wrap-os-with-print-writer")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				try {
					final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
					final String encoding = args.size() == 1 ? "UTF-8" : ((VncString)args.second()).getValue();

					return new VncJavaObject(new BufferedWriter(new OutputStreamWriter(os, encoding)));
				}
				catch (Exception ex) {
					throw new VncException(
							"Failed to wrap an :java.io.OutputStream with a :java.io.BufferedWriter",
							ex);
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
						"Wraps an `java.io.OutputStream` os with a `java.io.PrintWriter` using an optional " +
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
					.seeAlso("io/wrap-os-with-buffered-writer")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

				try {
					final OutputStream os = (OutputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());
					final String encoding = args.size() == 1 ? "UTF-8" : ((VncString)args.second()).getValue();

					return new VncJavaObject(new PrintWriter(new OutputStreamWriter(os, encoding)));
				}
				catch (Exception ex) {
					throw new VncException(
							"Failed to wrap an :java.io.OutputStream with a :java.io.PrintWriter", 
							ex);
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
							"Wraps an `java.io.InputStream` is with a `java.io.BufferedReader` using an optional " +
							"encoding (defaults to :utf-8).")
						.examples(
							"(do                                                                          \n" +
							"   (import :java.io.ByteArrayInputStream)                                    \n" +
							"   (let [data (byte-array [108 105 110 101 32 49 10 108 105 110 101 32 50])  \n" +
							"         is (. :ByteArrayInputStream :new data)                              \n" +
							"         rd (io/wrap-is-with-buffered-reader is :utf-8)]                     \n" +
							"      (println (. rd :readLine))                                             \n" +
							"      (println (. rd :readLine))))                                             ")
						.seeAlso("io/buffered-reader")
						.build()
			) {
				public VncVal apply(final VncList args) {
					ArityExceptions.assertArity(this, args, 1, 2);

					 if (Types.isVncJavaObject(args.first())) {
						final Object delegate = ((VncJavaObject)args.first()).getDelegate();
						if (delegate instanceof InputStream) {
							try {
								final InputStream is = (InputStream)delegate;
								final String encoding = args.size() == 1 ? "UTF-8" : ((VncString)args.second()).getValue();
	
								return new VncJavaObject(new BufferedReader(new InputStreamReader(is, encoding)));
							}
							catch (Exception ex) {
								throw new VncException(
										"Failed to wrap an :java.io.InputStream with a :java.io.BufferReader", 
										ex);
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
						"Creates a `java.io.BufferedReader` from a `java.io.InputStream` is with optional " +
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
					.seeAlso("io/buffered-writer")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

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
						"Function 'io/buffered-reader' requires a :java.io.InputStream, " +
						"a :java.io.Reader, or a string. %s as is not allowed!",
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
						"Creates a `java.io.BufferedWriter` from a `java.io.OutputStream` os with optional " +
						"encoding (defaults to :utf-8) or from a Writer.")
					.examples()
					.seeAlso("io/buffered-reader")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1, 2);

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
						"Function 'io/buffered-writer' requires a :java.io.OutputStream " +
						"or a :java.io.Writer. %s as is not allowed!",
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
				ArityExceptions.assertMinArity(this, args, 1);

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
						"Creates an empty temp file with the given prefix and suffix.")
					.examples(
						"(do \n" +
						"  (let [file (io/temp-file \"test-\", \".txt\")] \n" +
						"    (io/spit file \"123456789\" :append true) \n" +
						"    (io/slurp file :binary false :remove true)) \n" +
						")")
					.seeAlso("io/temp-dir")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);
				
				sandboxFunctionCallValidation();

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
					.seeAlso("io/tmp-dir", "io/temp-file")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				sandboxFunctionCallValidation();

				final String prefix = Coerce.toVncString(args.first()).getValue();
				try {
					return new VncString(Files.createTempDirectory(prefix).normalize().toString());
				}
				catch (Exception ex) {
					throw new VncException("Failed to create a temp directory", ex);
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
						"Loads a classpath resource. Returns a bytebuf")
					.examples(
						"(io/load-classpath-resource \"org/foo/images/foo.png\")")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);
				
				sandboxFunctionCallValidation();

				final VncVal name = args.first();

				try {
					if (Types.isVncString(name)) {
						final String res = ((VncString)args.first()).getValue();
						final byte[] data = ThreadLocalMap.getInterceptor().onLoadClassPathResource(res);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncKeyword(name)) {
						final String res = ((VncKeyword)args.first()).getValue();
						final byte[] data = ThreadLocalMap.getInterceptor().onLoadClassPathResource(res);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncSymbol(name)) {
						final String res = ((VncSymbol)args.first()).getName();
						final byte[] data = ThreadLocalMap.getInterceptor().onLoadClassPathResource(res);
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
					throw new VncException(
							"Failed to load classpath resource: " + name.toString(), 
							ex);
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
				ArityExceptions.assertArity(this, args, 1);

				final VncVal name = args.first();

				try {
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
				ArityExceptions.assertArity(this, args, 0);

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
	
	private static TimeUnit toTimeUnit(final VncKeyword unit) {
		switch(unit.getValue()) {
			case "milliseconds": return TimeUnit.MILLISECONDS;
			case "seconds": return TimeUnit.SECONDS;
			case "minutes":  return TimeUnit.MINUTES;
			case "hours": return TimeUnit.HOURS;
			case "days": return TimeUnit.DAYS;
			default: throw new VncException(
							"Invalid time-unit " + unit.getValue() + ". " 
								+ "Use one of {:milliseconds, :seconds, :minutes, :hours, :days}");
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
					.add(io_file_ext)
					.add(io_file_size)
					.add(io_file_last_modified)
					.add(io_exists_file_Q)
					.add(io_exists_dir_Q)
					.add(io_file_can_read_Q)
					.add(io_file_can_write_Q)
					.add(io_file_can_execute_Q)
					.add(io_file_hidden_Q)
					.add(io_file_symbolicl_link_Q)
					.add(io_await_for)	
					.add(io_watch_dir)
					.add(io_close_watcher)
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
					.add(io_bytebuf_in_stream)
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
