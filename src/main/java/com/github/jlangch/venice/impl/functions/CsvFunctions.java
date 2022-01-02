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
package com.github.jlangch.venice.impl.functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.csv.CSVReader;
import com.github.jlangch.venice.impl.util.csv.CSVWriter;


public class CsvFunctions {

	///////////////////////////////////////////////////////////////////////////
	// CSV
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction read = 
		new VncFunction(
				"csv/read", 
				VncFunction
					.meta()
					.arglists("(csv/read source & options)")		
					.doc(
						"Reads CSV-data from a source.                                     \n\n" + 
						"The source may be a:                                              \n\n" +
						" * `string`                                                       \n" +
						" * `bytebuf`                                                      \n" +
						" * `java.io.File`, e.g: `(io/file \"/temp/foo.json\")`            \n" +
						" * `java.io.InputStream`                                          \n" +
						" * `java.io.Reader`                                               \n" +
						" * `java.net.URL`                                                 \n" +
						" * `java.net.URI`                                                 \n\n" +
						"Options:\n\n" + 
						"| :encoding enc  | used when reading from a binary data source " + 
						"                   e.g :encoding :utf-8, defaults to :utf-8 |\n" + 
						"| :separator val | e.g. \",\", defaults to a comma |\n" + 
						"| :quote val     | e.g. \"'\", defaults to a double quote |")
					.examples(
					    "(csv/read \"1,\\\"ab\\\",false\")",
					    "(csv/read \"1:::'ab':false\" :separator \":\" :quote \"'\")") 
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 1);

				sandboxFunctionCallValidation();

				try {
					final VncVal source = args.first();
					final VncHashMap options = VncHashMap.ofAll(args.rest());
					final char separator = toChar(options.get(new VncKeyword("separator")), ',');
					final char quote = toChar(options.get(new VncKeyword("quote")), '"');

					final CSVReader parser = new CSVReader(separator, quote);

					if (Types.isVncString(source)) {
						return map(parser.parse(((VncString)source).getValue()));
					}
					else if (Types.isVncJavaObject(source, File.class)) {
						final File file = Types.isVncString(source)
											? new File(((VncString)source).getValue())
											: (File)(Coerce.toVncJavaObject(args.first()).getDelegate());
	
						IOFunctions.validateReadableFile(file);
	
						final VncVal encVal = options.get(new VncKeyword("encoding"));

						try(FileInputStream is = new FileInputStream(file)) {
							return map(parser.parse(is, IOFunctions.encoding(encVal)));
						}
					}
					else if (Types.isVncJavaObject(source, InputStream.class)) {
						final InputStream is = (InputStream)(Coerce.toVncJavaObject(args.first()).getDelegate());

						final VncVal encVal = options.get(new VncKeyword("encoding"));

						try(InputStream is_ = is) {
							return map(parser.parse(is_, IOFunctions.encoding(encVal)));
						}
					}
					else if (Types.isVncJavaObject(source, URL.class)) {
						final URL url = (URL)(Coerce.toVncJavaObject(args.first()).getDelegate());

						final VncVal encVal = options.get(new VncKeyword("encoding"));

						try(InputStream is = url.openStream()) {
							return map(parser.parse(is, IOFunctions.encoding(encVal)));
						}
					}
					else if (Types.isVncJavaObject(source, URI.class)) {
						final URI uri = (URI)(Coerce.toVncJavaObject(args.first()).getDelegate());

						final VncVal encVal = options.get(new VncKeyword("encoding"));

						try(InputStream is = uri.toURL().openStream()) {
							return map(parser.parse(is, IOFunctions.encoding(encVal)));
						}
					}
					else if (Types.isVncJavaObject(source, Reader.class)) {
						final Reader rd = (Reader)(Coerce.toVncJavaObject(args.first()).getDelegate());
						try(Reader rd_ = rd) {
							return map(parser.parse(rd_));
						}
					}
					else {
						throw new VncException(String.format(
								"Function 'csv/read' does not allow %s as f",
								Types.getType(args.first())));
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

	public static VncFunction write = 
		new VncFunction(
				"csv/write", 
				VncFunction
					.meta()
					.arglists("(csv/write writer records & options)")		
					.doc(
						"Writes data to a writer in CSV format. The writer is a\n" + 
						"Java java.io.Writer\n" + 
						"\n" + 
						"Options:\n\n" + 
						"| :separator val | e.g. \",\", defaults to a comma |\n" + 
						"| :quote val     | e.g. \"'\", defaults to a double quote |\n" + 
						"| :newline val   | :lf (default) or :cr+lf |")
					.examples(
					    "(let [file (io/file \"test.csv\")                                       \n" + 
					    "      fs (. :java.io.FileOutputStream :new file)]                       \n" + 
					    "  (try-with [writer (. :java.io.OutputStreamWriter :new fs \"utf-8\")]  \n" + 
					    "    (csv/write writer [[1 \"AC\" false] [2 \"WS\" true]])))               ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				ArityExceptions.assertMinArity(this, args, 2);

				sandboxFunctionCallValidation();

				try {
					final VncVal vWriter = args.first();
					
					final VncHashMap options = VncHashMap.ofAll(args.rest().rest());
					final char separator = toChar(options.get(new VncKeyword("separator")), ',');
					final char quote = toChar(options.get(new VncKeyword("quote")), '"');
					final String newline = toNewLine(options.get(new VncKeyword("newline")));

					final CSVWriter csvWriter = new CSVWriter(separator, quote, newline);

					if (Types.isVncJavaObject(vWriter, Writer.class)) {
						final Writer writer = (Writer)(Coerce.toVncJavaObject(vWriter).getDelegate());

						try(Writer wr = writer) {
							csvWriter.write(wr, Coerce.toVncSequence(args.second()));
						}
						
						return Constants.Nil;
					}
					else {
						throw new VncException(String.format(
								"Function 'csv/write' does not allow %s as writer",
								Types.getType(args.first())));
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

	public static VncFunction write_str = 
			new VncFunction(
					"csv/write-str", 
					VncFunction
						.meta()
						.arglists("(csv/write-str records & options)")		
						.doc(
							"Writes data to a string in CSV format.\n" + 
							"\n" + 
							"Options:\n\n" + 
							"| :separator val | e.g. \",\", defaults to a comma |\n" + 
							"| :quote val     | e.g. \"'\", defaults to a double quote |\n" + 
							"| :newline val   | :lf (default) or :cr+lf |")
						.examples(
							"(csv/write-str [[1 \"AC\" false] [2 \"WS\" true]])",
						    "(csv/write-str [[1 \"AC\" false] [2 \"WS, '-1'\" true]]\n" + 
						    "               :quote \"'\"\n" + 
						    "               :separator \",\"\n" + 
						    "               :newline :cr+lf)")
						.build()
			) {		
				public VncVal apply(final VncList args) {
					ArityExceptions.assertMinArity(this, args, 1);
		
					try {
						final VncHashMap options = VncHashMap.ofAll(args.rest());
						final char separator = toChar(options.get(new VncKeyword("separator")), ',');
						final char quote = toChar(options.get(new VncKeyword("quote")), '"');
						final String newline = toNewLine(options.get(new VncKeyword("newline")));

						final CSVWriter csvWriter = new CSVWriter(separator, quote, newline);

						final StringWriter sw = new StringWriter();
						csvWriter.write(sw, Coerce.toVncSequence(args.first()));
						return new VncString(sw.toString());
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
		
		
	private static VncList map(final List<List<String>> data) {
		final List<VncVal> vncRecords = new ArrayList<>();
		for(List<String> record : data) {
			final List<VncVal> vncRecord = new ArrayList<>();
			for(String s : record) {
				vncRecord.add(s == null ? Constants.Nil : new VncString(s));
			}
			vncRecords.add(VncList.ofList(vncRecord));
		}
		return VncList.ofList(vncRecords);
	}
		
	private static char toChar(final VncVal v, final char defaultChar) {
		if (v == Constants.Nil) {
			return defaultChar;
		}
		else if (Types.isVncChar(v)) {
			return ((VncChar)v).getValue();
		}
		else if (Types.isVncString(v)) {
			final String s = ((VncString)v).getValue();
			return s.isEmpty() ? defaultChar : s.charAt(0);
		}
		else {
			return defaultChar;
		}
	}

	private static String toNewLine(final VncVal v) {
		if (Types.isVncKeyword(v)) {
			final String s = ((VncKeyword)v).getValue();
			if (s.equals("lf")) return "\n";
			else if (s.equals("cr+lf")) return "\r\n";
			else return "\n";
		}
		else if (Types.isVncString(v)) {
			return ((VncString)v).getValue();
		}
		else {
			return "\n";
		}
	}

	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(read)
					.add(write)
					.add(write_str)
					.toMap();	
}
