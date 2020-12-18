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

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.io.Zipper;


public class ZipFunctions {

	///////////////////////////////////////////////////////////////////////////
	// I/O Zip functions
	///////////////////////////////////////////////////////////////////////////


	public static VncFunction io_zip =
		new VncFunction(
				"io/zip",
				VncFunction
					.meta()
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
					.seeAlso("io/unzip", "io/gzip", "io/spit", "io/zip-list", "io/zip-list-entry-names", "io/zip-file", "io/zip-append", "io/zip-remove")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				sandboxFunctionCallValidation();

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
							data = ((VncByteBuffer)dataVal).getBytes();
						}
						else if (Types.isVncJavaObject(dataVal, InputStream.class)) {
							data = (InputStream)((VncJavaObject)dataVal).getDelegate();
						}
						else if (Types.isVncJavaObject(dataVal, File.class)) {
							data = (File)((VncJavaObject)dataVal).getDelegate();
							validateReadableFile((File)data);
						}
						else if (Types.isVncString(dataVal)) {
							data = new File(Coerce.toVncString(dataVal).getValue());
							validateReadableFile((File)data);
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
					.arglists("(io/zip-append f & entries)")
					.doc(
						"Appends entries to an existing zip file f. Overwrites existing " +
						"entries. An entry is given by a name and data. The entry data " +
						"may be nil, a bytebuf, a file, a string (file path), or an " +
						"InputStream." +
						"An entry name with a trailing '/' creates a directory. ")
					.examples(
						"  (let [data (bytebuf-from-string \"abc\" :utf-8)]                  \n" +
						"    ; create the zip with a first file                              \n" +
						"    (->> (io/zip \"a.txt\" data)                                    \n" +
						"         (io/spit \"test.zip\"))                                    \n" +
						"    ; add text files                                                \n" +
						"    (io/zip-append \"test.zip\" \"b.txt\" data \"x/c.txt\" data)    \n" +
						"    ; add an empty directory                                        \n" +
						"    (io/zip-append \"test.zip\" \"x/y/\" nil))                        ")
					.seeAlso("io/zip-file", "io/zip-remove")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 3);

				sandboxFunctionCallValidation();

				final File file = convertToFile(
									args.first(),
									"Function 'io/zip-append' does not allow %s as f");

				validateReadableFile(file);

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
							data = ((VncByteBuffer)dataVal).getBytes();
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

	public static VncFunction io_zip_remove =
		new VncFunction(
				"io/zip-remove",
				VncFunction
					.meta()
					.arglists("(io/zip-remove f & entry-names)")
					.doc("Remove entries from a zip file f.")
					.examples(
						"; remove files from zip \n" +
						"(io/zip-remove \"test.zip\" \"x/a.txt\" \"x/b.txt\")",

						"; remove directory from zip \n" +
						"(io/zip-remove \"test.zip\" \"x/y/\")"
						)
					.seeAlso("io/zip-file", "io/zip-append")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				sandboxFunctionCallValidation();

				final File file = convertToFile(
									args.first(),
									"Function 'io/zip-remove' does not allow %s as f");

				validateReadableFile(file);

				try {
					Zipper.zipRemove(
							file,
							args.slice(1)
								.stream()
								.map(e -> Coerce.toVncString(e).getValue())
								.collect(Collectors.toList()));

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
					.arglists("(io/zip-size f)")
					.doc(
						"Returns the number of entries in the zip f. f may be a bytebuf, " +
						"a file, a string (file path) or an InputStream.")
					.examples(
						"(io/zip-size (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)))")
					.seeAlso("io/zip")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				if (args.isEmpty()) {
					return new VncLong(0);
				}

				try {
					final VncVal data = args.first();

					if (Types.isVncByteBuffer(data)) {
						return new VncLong(Zipper.listZipEntryNames(((VncByteBuffer)data).getBytes()).size());
					}
					else if (Types.isVncJavaObject(data, InputStream.class)) {
						return new VncLong(Zipper.listZipEntryNames((InputStream)((VncJavaObject)data).getDelegate()).size());
					}
					else if (Types.isVncJavaObject(data, File.class)) {
						final File file = (File)((VncJavaObject)data).getDelegate();
						validateReadableFile(file);
						return new VncLong(Zipper.listZipEntryNames(file).size());
					}
					else if (Types.isVncString(data)) {
						final File file = new File(Coerce.toVncString(data).getValue());
						validateReadableFile(file);
						return new VncLong(Zipper.listZipEntryNames(file).size());
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
					.arglists("(io/unzip f entry-name)")
					.doc(
						"Unzips an entry from zip f the entry's data as a bytebuf. f may be a bytebuf, \n" +
						"a file, a string (file path) or an InputStream.")
					.examples(
						"(-> (io/zip \"a.txt\" (bytebuf-from-string \"abcdef\" :utf-8)) \n" +
						"    (io/unzip \"a.txt\"))")
					.seeAlso("io/zip", "io/zip?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				sandboxFunctionCallValidation();

				final VncVal buf = args.first();
				final String entryName = Coerce.toVncString(args.second()).getValue();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(buf)) {
						final byte[] data = Zipper.unzip(((VncByteBuffer)buf).getBytes(), entryName);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, InputStream.class)) {
						final byte[] data = Zipper.unzip((InputStream)((VncJavaObject)buf).getDelegate(), entryName);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, File.class)) {
						final File file = (File)((VncJavaObject)buf).getDelegate();
						validateReadableFile(file);
						final byte[] data = Zipper.unzip(file, entryName);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncString(buf)) {
						final File file = new File(Coerce.toVncString(buf).getValue());
						validateReadableFile(file);
						final byte[] data = Zipper.unzip(file, entryName);
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
					.arglists("(io/unzip-first zip)")
					.doc(
						"Unzips the first entry of the zip f returning its data as a bytebuf. " +
						"f may be a bytebuf, a file, a string (file path) or an InputStream.")
					.examples(
						"(-> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
						"            \"b.txt\" (bytebuf-from-string \"def\" :utf-8)) \n" +
						"    (io/unzip-first))")
					.seeAlso("io/unzip-to-dir", "io/unzip-nth", "io/unzip-all", "io/zip", "io/zip?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				final VncVal buf = args.first();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(buf)) {
						final byte[] data = Zipper.unzipNthEntry(((VncByteBuffer)buf).getBytes(), 0);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, InputStream.class)) {
						final byte[] data = Zipper.unzipNthEntry((InputStream)((VncJavaObject)buf).getDelegate(), 0);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, File.class)) {
						final File file = (File)((VncJavaObject)buf).getDelegate();
						validateReadableFile(file);
						final byte[] data = Zipper.unzipNthEntry(file, 0);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncString(buf)) {
						final File file = new File(Coerce.toVncString(buf).getValue());
						validateReadableFile(file);
						final byte[] data = Zipper.unzipNthEntry(file, 0);
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
					.arglists("(io/unzip-nth zip n)")
					.doc(
						"Unzips the nth (zero.based) entry of the zip f returning its data as a bytebuf. " +
						"f may be a bytebuf, a file, a string (file path) or an InputStream.")
					.examples(
						"(-> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
						"            \"b.txt\" (bytebuf-from-string \"def\" :utf-8)  \n" +
						"            \"c.txt\" (bytebuf-from-string \"ghi\" :utf-8)) \n" +
						"    (io/unzip-nth 1))")
					.seeAlso("io/unzip-to-dir", "io/unzip-first", "io/unzip-all", "io/zip", "io/zip?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				sandboxFunctionCallValidation();

				final VncVal buf = args.first();
				final int entryIdx = Coerce.toVncLong(args.second()).getIntValue();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(buf)) {
						final byte[] data = Zipper.unzipNthEntry(((VncByteBuffer)buf).getBytes(), entryIdx);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, InputStream.class)) {
						final byte[] data = Zipper.unzipNthEntry((InputStream)((VncJavaObject)buf).getDelegate(), entryIdx);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncJavaObject(buf, File.class)) {
						final File file = (File)((VncJavaObject)buf).getDelegate();
						validateReadableFile(file);
						final byte[] data = Zipper.unzipNthEntry(file, entryIdx);
						return data == null ? Nil : new VncByteBuffer(data);
					}
					else if (Types.isVncString(buf)) {
						final File file = new File(Coerce.toVncString(buf).getValue());
						validateReadableFile(file);
						final byte[] data = Zipper.unzipNthEntry(file, entryIdx);
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
					.seeAlso("io/unzip-to-dir", "io/unzip-nth", "io/unzip-first", "io/zip", "io/zip?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				final VncVal buf = args.first();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else {
						final Map<String,byte[]> data;

						if (Types.isVncByteBuffer(buf)) {
							data = Zipper.unzipAll(((VncByteBuffer)buf).getBytes());
						}
						else if (Types.isVncJavaObject(buf, InputStream.class)) {
							data = Zipper.unzipAll((InputStream)((VncJavaObject)buf).getDelegate());
						}
						else if (Types.isVncJavaObject(buf, File.class)) {
							final File file = (File)((VncJavaObject)buf).getDelegate();
							validateReadableFile(file);
							data = Zipper.unzipAll(file);
						}
						else if (Types.isVncString(buf)) {
							final File file = new File(Coerce.toVncString(buf).getValue());
							validateReadableFile(file);
							data = Zipper.unzipAll(file);
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
					.arglists("(io/zip-file options* zip-file & files)")
					.doc(
						"Zips files. The zip-file my be a file, a string (file path) or " +
						"an OutputStream. \n\n" +
						"Options: \n" +
						"  :filter-fn fn - filters the files to be added to the zip.")
					.examples(
						"; zip files\n" +
						"(io/zip-file \"test.zip\" \"a.txt\" \"x/b.txt\")",

						"; zip all files in a directory\n" +
						"(io/zip-file \"test.zip\" \"dir\")",

						"(io/zip-file :filter-fn (fn [dir name] (str/ends-with? name \".txt\"))  \n" +
						"             \"test.zip\" \n" +
						"             \"test-dir\")")
					.seeAlso("io/zip")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				sandboxFunctionCallValidation();

				int ii = 0;

				// read options
				VncHashMap options = new VncHashMap();
				while (Types.isVncKeyword(args.nth(ii))) {
					final VncVal optName = args.nth(ii++);
					final VncVal optVal = args.nth(ii++);
					options = options.assoc(optName, optVal);
				}

				// destination zip
				final VncVal dest = args.nth(ii++);

				// files
				final VncList files = args.slice(ii);

				// parse filter
				final VncVal filterFnVal = options.get(new VncKeyword("filter-fn"));
				final VncFunction filterFn = filterFnVal == Nil ? null : Coerce.toVncFunction(filterFnVal);

				final FilenameFilter filter = filterFn == null
												? null
												: new FilenameFilter() {
														public boolean accept(File dir, String name) {
															return VncBoolean.isTrue(
																		filterFn.apply(
																				VncList.of(
																					new VncJavaObject(dir),
																					new VncString(name))));
														}};

				// parse files
				final List<File> filesToZip = new ArrayList<>();
				files.forEach(f -> {
					final File file = convertToFile(
											f, "Function 'io/zip-file' does not allow %s as file");

					validateReadableFileOrDirectory(file);

					filesToZip.add(file);
				});

				try {
					if (Types.isVncJavaObject(dest, File.class)) {
						Zipper.zipFileOrDir(
								Coerce.toVncJavaObject(dest, File.class), filesToZip, filter);
					}
					else if (Types.isVncString(dest)) {
						Zipper.zipFileOrDir(
								new File(Coerce.toVncString(dest).getValue()), filesToZip, filter);
					}
					else if (Types.isVncJavaObject(dest, OutputStream.class)) {
						Zipper.zipFileOrDir(
								Coerce.toVncJavaObject(dest, OutputStream.class), filesToZip, filter);
					}
					else {
						throw new VncException(String.format(
								"Function 'io/zip-file' does not allow %s as zip-file",
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
					.arglists("(io/zip-list f & options)")
					.doc(
						"List the content of a the zip f. f may be a bytebuf, a file, " +
						"a string (file path), or an InputStream. \n" +
						"Options: \n" +
						"  :verbose true/false - e.g :verbose true, defaults to false")
					.examples(
						"(io/zip-list \"test-file.zip\")",
						"(io/zip-list \"test-file.zip\" :verbose true)")
					.seeAlso("io/zip-list-entry-names", "io/zip", "io/unzip")
				.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

				sandboxFunctionCallValidation();

				try {
					final VncVal f = args.first();

					final VncHashMap options = VncHashMap.ofAll(args.rest());

					final boolean verbose = VncBoolean.isTrue(options.get(new VncKeyword("verbose")));

					if (Types.isVncByteBuffer(f)) {
						Zipper.listZip(((VncByteBuffer)f).getBytes(), System.out, verbose);
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						final File file = (File)((VncJavaObject)f).getDelegate();
						validateReadableFile(file);
						Zipper.listZip(file, System.out, verbose);
					}
					else if (Types.isVncString(f)) {
						final File file = new File(Coerce.toVncString(f).getValue());
						validateReadableFile(file);
						Zipper.listZip(file, System.out, verbose);
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

	public static VncFunction io_zip_list_entry_names =
		new VncFunction(
				"io/zip-list-entry-names",
				VncFunction
					.meta()
					.arglists("(io/zip-list-entry-names)")
					.doc(
						"Returns a list of the zip's entry names.")
					.examples(
						"(io/zip-list-entry-names \"test-file.zip\")")
					.seeAlso("io/zip-list", "io/zip", "io/unzip")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

				sandboxFunctionCallValidation();

				try {
					final VncVal f = args.first();
					List<String> entries;

					if (Types.isVncByteBuffer(f)) {
						entries = Zipper.listZipEntryNames(((VncByteBuffer)f).getBytes());
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						final File file = (File)((VncJavaObject)f).getDelegate();
						validateReadableFile(file);
						entries = Zipper.listZipEntryNames(file);
					}
					else if (Types.isVncString(f)) {
						final File file = new File(Coerce.toVncString(f).getValue());
						validateReadableFile(file);
						entries = Zipper.listZipEntryNames(file);
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						entries = Zipper.listZipEntryNames(Coerce.toVncJavaObject(f, InputStream.class));
					}
					else {
						throw new VncException(String.format(
								"Function 'io/zip-list-entry-names' does not allow %s as f",
								Types.getType(f)));
					}

					return VncList.ofList(entries.stream().map(s -> new VncString(s)).collect(Collectors.toList()));
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
					.arglists("(io/unzip-to-dir f dir)")
					.doc(
						"Unzips f to a directory. f may be a file, a string (file path), " +
						"a bytebuf, or an InputStream.")
					.examples(
						"(-> (io/zip \"a.txt\" (bytebuf-from-string \"abc\" :utf-8)  \n" +
						"            \"b.txt\" (bytebuf-from-string \"def\" :utf-8)  \n" +
						"            \"c.txt\" (bytebuf-from-string \"ghi\" :utf-8)) \n" +
						"    (io/unzip-to-dir \".\")")
					.seeAlso("io/unzip", "io/unzip-nth", "io/unzip-first", "io/unzip-all", "io/zip", "io/zip?")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				sandboxFunctionCallValidation();

				final VncVal f = args.first();
				final File dir = Coerce.toVncJavaObject(args.second(), File.class);

				validateReadableDirectory(dir);

				try {
					if (Types.isVncByteBuffer(f)) {
						Zipper.unzipToDir(((VncByteBuffer)f).getBytes(), dir);
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						final File file = (File)((VncJavaObject)f).getDelegate();
						validateReadableFile(file);
						Zipper.unzipToDir(file, dir);
					}
					else if (Types.isVncString(f)) {
						final File file = new File(Coerce.toVncString(f).getValue());
						validateReadableFile(file);
						Zipper.unzipToDir(file, dir);
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
					.arglists("(io/gzip f)")
					.doc(
						"gzips f. f may be a file, a string (file path), a bytebuf or an " +
						"InputStream. Returns a bytebuf.")
					.examples(
						"(->> (io/gzip \"a.txt\")  \n" +
						"     (io/spit \"a.gz\"))    ",

						"(io/gzip (bytebuf-from-string \"abcdef\" :utf-8))")
					.seeAlso("io/gzip?", "io/ungzip", "io/zip", "io/spit")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				final VncVal f = args.first();
				try {
					if (f == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(f)) {
						return new VncByteBuffer(Zipper.gzip(((VncByteBuffer)f).getBytes()));
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						final File file = (File)((VncJavaObject)f).getDelegate();
						validateReadableFile(file);
						return new VncByteBuffer(Zipper.gzip(file));
					}
					else if (Types.isVncString(f)) {
						final File file = new File(Coerce.toVncString(f).getValue());
						validateReadableFile(file);
						return new VncByteBuffer(Zipper.gzip(file));
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
					.seeAlso("io/gzip")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 2);

				sandboxFunctionCallValidation();

				final VncVal f = args.first();
				final OutputStream os = (OutputStream)Coerce.toVncJavaObject(args.second()).getDelegate();
				try {
					if (f == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(f)) {
						Zipper.gzip(((VncByteBuffer)f).getBytes(), os);
						return Nil;
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						final File file = (File)((VncJavaObject)f).getDelegate();
						validateReadableFile(file);
						Zipper.gzip(file);
						return Nil;
					}
					else if (Types.isVncString(f)) {
						final File file = new File(Coerce.toVncString(f).getValue());
						validateReadableFile(file);
						Zipper.gzip(file);
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
					.arglists("(io/ungzip f)")
					.doc(
						"ungzips f. f may be a file, a string (file path), a bytebuf, " +
						"or an InputStream. Returns a bytebuf.")
					.examples(
						"(-> (bytebuf-from-string \"abcdef\" :utf-8) \n" +
						"    (io/gzip) \n" +
						"    (io/ungzip))")
					.seeAlso("io/gzip", "io/gzip?", "io/ungzip-to-stream")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				final VncVal f = args.first();
				try {
					if (f == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(f)) {
						return new VncByteBuffer(Zipper.ungzip(((VncByteBuffer)f).getBytes()));
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						final File file = (File)((VncJavaObject)f).getDelegate();
						validateReadableFile(file);
						return new VncByteBuffer(Zipper.ungzip(file));
					}
					else if (Types.isVncString(f)) {
						final File file = new File(Coerce.toVncString(f).getValue());
						validateReadableFile(file);
						return new VncByteBuffer(Zipper.ungzip(file));
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
					.arglists("(io/ungzip-to-stream buf)")
					.doc(
						"ungzips a bytebuf returning an InputStream to read the deflated " +
						"data from.")
					.examples(
							"(-> (bytebuf-from-string \"abcdef\" :utf-8) \n" +
							"    (io/gzip) \n" +
							"    (io/ungzip-to-stream) \n" +
							"    (io/slurp-stream :binary false :encoding :utf-8))")
					.seeAlso("io/gzip")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				sandboxFunctionCallValidation();

				final VncVal buf = args.first();
				try {
					if (buf == Nil) {
						return Nil;
					}
					else if (Types.isVncByteBuffer(buf)) {
						return new VncJavaObject(Zipper.ungzipToStream(((VncByteBuffer)buf).getBytes()));
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
					.arglists("(io/zip? f)")
					.doc(
						"Returns true if f is a zipped file. f may be a file, a string (file path), " +
						"a bytebuf, or an InputStream")
					.examples(
						"(-> (io/zip \"a\" (bytebuf-from-string \"abc\" :utf-8)) " +
						"    (io/zip?))")
					.seeAlso("io/zip")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				if (args.isEmpty()) {
					return False;
				}

				try {
					final VncVal f = args.first();

					if (Types.isVncByteBuffer(f)) {
						return VncBoolean.of(Zipper.isZipFile(((VncByteBuffer)f).getBytes()));
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						final File file = (File)((VncJavaObject)f).getDelegate();
						validateReadableFile(file);
						return VncBoolean.of(Zipper.isZipFile(file));
					}
					else if (Types.isVncString(f)) {
						final File file = new File(Coerce.toVncString(f).getValue());
						validateReadableFile(file);
						return VncBoolean.of(Zipper.isZipFile(file));
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						return VncBoolean.of(Zipper.isZipFile((InputStream)((VncJavaObject)f).getDelegate()));
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
					.arglists("(io/gzip? f)")
					.doc(
						"Returns true if f is a gzipped file. f may be a file, a string (file path), " +
						"a bytebuf, or an InputStream")
					.examples(
						"(-> (io/gzip (bytebuf-from-string \"abc\" :utf-8)) " +
						"    (io/gzip?))")
					.seeAlso("io/gzip")
					.build()
		) {
			public VncVal apply(final VncList args) {
				ArityExceptions.assertArity(this, args, 1);

				if (args.isEmpty()) {
					return False;
				}

				try {
					final VncVal f = args.first();

					if (Types.isVncByteBuffer(f)) {
						return VncBoolean.of(Zipper.isGZipFile(((VncByteBuffer)f).getBytes()));
					}
					else if (Types.isVncJavaObject(f, File.class)) {
						final File file = (File)((VncJavaObject)f).getDelegate();
						validateReadableFile(file);
						return VncBoolean.of(Zipper.isGZipFile(file));
					}
					else if (Types.isVncString(f)) {
						final File file = new File(Coerce.toVncString(f).getValue());
						validateReadableFile(file);
						return VncBoolean.of(Zipper.isGZipFile(file));
					}
					else if (Types.isVncJavaObject(f, InputStream.class)) {
						return VncBoolean.of(Zipper.isGZipFile((InputStream)((VncJavaObject)f).getDelegate()));
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

	private static void validateReadableFile(final File file) {
		if (!file.isFile()) {
			throw new VncException(String.format("'%s' is not a file", file.getPath()));
		}
		if (!file.canRead()) {
			throw new VncException(String.format("The file '%s' has no read permission", file.getPath()));
		}
	}

	private static void validateReadableDirectory(final File file) {
		if (!file.isDirectory()) {
			throw new VncException(String.format("'%s' is not a directory", file.getPath()));
		}
		if (!file.canRead()) {
			throw new VncException(String.format("The directory '%s' has no read permission", file.getPath()));
		}
	}

	private static void validateReadableFileOrDirectory(final File file) {
		if (!(file.isFile() || file.canRead())) {
			throw new VncException(String.format("'%s' is not a file or a dierctory", file.getPath()));
		}
		if (!file.canRead()) {
			throw new VncException(String.format("'%s' has no read permission", file.getPath()));
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns =
			new VncHashMap
					.Builder()
					.add(io_zip)
					.add(io_zip_append)
					.add(io_zip_remove)
					.add(io_zip_file)
					.add(io_zip_list)
					.add(io_zip_list_entry_names)
					.add(io_zip_Q)
					.add(io_unzip)
					.add(io_unzip_first)
					.add(io_unzip_nth)
					.add(io_unzip_all)
					.add(io_unzip_to_dir)
					.add(io_zip_size)
					.add(io_gzip)
					.add(io_gzip_Q)
					.add(io_gzip_to_stream)
					.add(io_ungzip)
					.add(io_ungzip_to_stream)
					.toMap();
}
