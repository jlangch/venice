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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncConstant;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ClassPathResource;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.pdf.HtmlColor;
import com.github.jlangch.venice.pdf.PdfRenderer;
import com.github.jlangch.venice.pdf.PdfWatermark;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;


public class PdfFunctions {

	///////////////////////////////////////////////////////////////////////////
	// PDF
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction pdf_render = 
		new VncFunction(
				"pdf/render", 
				VncFunction
					.meta()
					.module("pdf")
					.arglists(
						"(pdf/render xhtml)",
						"(pdf/render xhtml base-url)",
						"(pdf/render xhtml resources)",
						"(pdf/render xhtml base-url alternate-base-paths)",
						"(pdf/render xhtml base-url alternate-base-paths resources)")		
					.doc("Renders a PDF.")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("pdf/render", args, 1, 2, 3, 4);
				
				if (args.size() == 1) {
					return new VncByteBuffer(
									PdfRenderer.render(
											Coerce.toVncString(args.first()).getValue()));
				}
				else if (args.size() == 2) {
					if (Types.isVncMap(args.second())) {
						return new VncByteBuffer(
								PdfRenderer.render(
										Coerce.toVncString(args.first()).getValue(),
										mapResources((VncMap)args.second())));
					}
					else if (Types.isVncString(args.second())) {
						return new VncByteBuffer(
								PdfRenderer.render(
										Coerce.toVncString(args.first()).getValue(),
										Coerce.toVncString(args.second()).getValue(),
										null));
					}
					else {
						throw new VncException(String.format(
								"Function 'pdf/render' does not allow %s as 2nd argument",
								Types.getType(args.second())));
					}
				}
				else if (args.size() == 3) {
					return new VncByteBuffer(
							PdfRenderer.render(
									Coerce.toVncString(args.first()).getValue(),
									Coerce.toVncString(args.second()).getValue(),
									mapAlternateBasePaths(Coerce.toVncSequence(args.third()))));
				}
				else {	
					return new VncByteBuffer(
							PdfRenderer.render(
									Coerce.toVncString(args.first()).getValue(),
									Coerce.toVncString(args.second()).getValue(),
									mapAlternateBasePaths(Coerce.toVncSequence(args.third())),
									mapResources(Coerce.toVncMap(args.nth(3)))));
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction pdf_watermark = 
		new VncFunction(
				"pdf/watermark", 
				VncFunction
					.meta()
					.module("pdf")
					.arglists(
						"(pdf/watermark pdf options-map)",		
						"(pdf/watermark pdf & options)")		
					.doc(
						"Adds a watermark text to the pages of a PDF. The passed PDF pdf is " +
						"a bytebuf. Returns the new PDF as a bytebuf.\n\n" +
						"Options: \n" +
						"  :text s              - watermark text (string), defaults to \"WATERMARK\" \n" +
						"  :font-size n         - font size in pt (double), defaults to 24.0 \n" +
						"  :font-char-spacing n - font character spacing (double), defaults to 0.0 \n" +
						"  :color s             - font color (HTML color string), defaults to #000000 \n" +
						"  :opacity n           - opacity 0.0 ... 1.0 (double), defaults to 0.4 \n" +
						"  :angle n             - angle 0.0 ... 360.0 (double), defaults to 45.0 \n" +
						"  :over-content b      - print text over the content (boolean), defaults to true \n" +
						"  :skip-top-pages n    - the number of top pages to skip (long), defaults to 0 \n" +
						"  :skip-bottom-pages n - the number of bottom pages to skip (long), defaults to 0")
					.examples(
						"(pdf/watermark pdf :text \"CONFIDENTIAL\" :font-size 64 :font-char-spacing 10.0)",							
						"(let [watermark { :text \"CONFIDENTIAL\"      \n" +
						"                  :font-size 64               \n" +
						"                  :font-char-spacing 10.0 } ] \n" +
						"   (pdf/watermark pdf watermark))                ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("pdf/watermark", args, 2);
				
				final VncVal pdf = args.first();
				
				final VncMap options = Types.isVncMap(args.second())
										? Coerce.toVncMap(args.second())
										: VncHashMap.ofAll(args.slice(1));

				final VncVal text = options.get(new VncKeyword("text", new VncString("WATERMARK"))); 
				final VncDouble fontSize = getVncDoubleOption("font-size", options, 24.0); 
				final VncDouble fontCharSpacing = getVncDoubleOption("font-char-spacing", options, 0.0); 
				final VncVal color = options.get(new VncKeyword("color", new VncString("#000000"))); 
				final VncDouble opacity = getVncDoubleOption("opacity", options, 0.4); 
				final VncDouble angle = getVncDoubleOption("angle", options, 45.0); 
				final VncConstant overContent = getBooleanOption("over-content", options, true); 
				final VncLong skipTopPages = getVncLongOption("skip-top-pages", options, 0); 
				final VncLong skipBottomPages = getVncLongOption("skip-bottom-pages", options, 0); 

				final ByteBuffer pdf_ = Coerce.toVncByteBuffer(pdf).getValue();
				final String text_ = Coerce.toVncString(text).getValue();
				final float fontSize_ = Coerce.toVncDouble(fontSize).getValue().floatValue();
				final float fontCharSpacing_ = Coerce.toVncDouble(fontCharSpacing).getValue().floatValue();
				final Color color_ = HtmlColor.getColor(Coerce.toVncString(color).getValue());
				final float opacity_= Coerce.toVncDouble(opacity).getValue().floatValue();
				final float angle_= Coerce.toVncDouble(angle).getValue().floatValue();
				final boolean overContent_ = Coerce.toVncBoolean(overContent) == Constants.True ? true : false;
				final int skipTopPages_ = Coerce.toVncLong(skipTopPages).getValue().intValue();
				final int skipBottomPages_ = Coerce.toVncLong(skipBottomPages).getValue().intValue();

				if (StringUtil.isBlank(text_)) {
					return pdf;
				}

				return new VncByteBuffer(
						new PdfWatermark()
								.addWatermarkText(
									pdf_, 
									text_,
									fontSize_,
									fontCharSpacing_,
									color_,
									opacity_,
									angle_,
									overContent_,
									skipTopPages_, 
									skipBottomPages_));
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction pdf_available_Q = 
		new VncFunction(
				"pdf/available?", 
				VncFunction
					.meta()
					.module("pdf")
					.arglists("(pdf/available?)")
					.doc("Checks if the 3rd party libraries required for generating PDFs are available.")
					.examples("(pdf/available?)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("pdf/available?", args, 0);
				
				// com.github.librepdf:openpdf:xxx
				if (!ReflectionAccessor.classExists("com.lowagie.text.Anchor")) {
					return Constants.False;
				}
				
				// org.xhtmlrenderer:flying-saucer-core:xxx
				if (!ReflectionAccessor.classExists("org.xhtmlrenderer.DefaultCSSMarker")) {
					return Constants.False;
				}
				
				// org.xhtmlrenderer:flying-saucer-pdf-openpdf:xxx
				if (!ReflectionAccessor.classExists("org.xhtmlrenderer.pdf.AbstractFormField")) {
					return Constants.False;
				}

				return Constants.True;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction pdf_merge = 
		new VncFunction(
				"pdf/merge", 
				VncFunction
					.meta()
					.module("pdf")
					.arglists("pdf/merge pdfs")
					.doc("Merge multiple PDFs into a single PDF.")
					.examples(
						"(pdf/merge pdf1 pdf2)",
						"(pdf/merge pdf1 pdf2 pdf3)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("pdf/merge", args, 1);

				final List<VncVal> pdfs = args.getList();

				if (pdfs.isEmpty()) {
					throw new VncException("pdf/merge: A PDF list must not be empty");
				}
				else if (pdfs.size() == 1) {
					return pdfs.get(0);
				}
				else {
					try {
						final ByteArrayOutputStream os = new ByteArrayOutputStream();
						final Document document = new Document();
				        final PdfCopy copy = new PdfCopy(document, os);
				
				        document.open();
				        for (VncVal val : pdfs){
				        	if (val == Nil) continue;
				        	
				        	final ByteBuffer pdf = Coerce.toVncByteBuffer(val).getValue();
				        	
				            final PdfReader reader = new PdfReader(pdf.array());
				            for (int ii=1; ii<=reader.getNumberOfPages(); ii++){
				                copy.addPage(copy.getImportedPage(reader, ii));
				            }
				            copy.freeReader(reader);
				            reader.close();
				        }
				        document.close();
				        copy.close();
				        
				        return new VncByteBuffer(os.toByteArray());
					}
					catch(Exception ex) {
						throw new VncException(
								String.format("pdf/merge: Failed to merge %d PDFs", pdfs.size()),
								ex);
					}
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction pdf_copy = 
		new VncFunction(
				"pdf/copy", 
				VncFunction
					.meta()
					.module("pdf")
					.arglists("pdf/copy pdf")
					.doc("Copies pages from a PDF to a new PDF.")
					.examples("(pdf/copy pdf :1 :2 :6-10 :12)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("pdf/copy", args, 1);

				final ByteBuffer pdf = Coerce.toVncByteBuffer(args.first()).getValue();

				final Set<Long> pages = new HashSet<>();
				for(VncVal p : args.rest().getList()) {
					final String spec = Coerce.toVncKeyword(p).getValue();
					if (spec.matches("[0-9]+^$")) {
						pages.add(Long.parseLong(spec));
					}
					else if (spec.matches("[0-9]+-[0-9]+^$")) {
						final String[] range = spec.split("-");
						final long start = Long.parseLong(range[0]);
						final long end = Long.parseLong(range[1]);
						for(long ii=start; ii<=end; ii++) {
							pages.add(ii);
						}
					}
					else {
						throw new VncException("pdf/copy: Invalid page specifier " + spec);
					}
				}
				
				try {
					final ByteArrayOutputStream os = new ByteArrayOutputStream();
					final Document document = new Document();
			        final PdfCopy copy = new PdfCopy(document, os);
			
			        document.open();
		        	
		            final PdfReader reader = new PdfReader(pdf.array());
		            for (int ii=1; ii<=reader.getNumberOfPages(); ii++){
		            	if (pages.contains((long)ii)) {
		            		copy.addPage(copy.getImportedPage(reader, ii));
		            	}
		            }
		            copy.freeReader(reader);
		            reader.close();

			        document.close();
			        copy.close();
			        
			        return new VncByteBuffer(os.toByteArray());
				}
				catch(Exception ex) {
					throw new VncException("pdf/copy: Failed to copy PDFs", ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction pdf_pages = 
		new VncFunction(
				"pdf/pages", 
				VncFunction
					.meta()
					.module("pdf")
					.arglists("pdf/pages pdf")
					.doc("Returns the number of pages of a PDF.")
					.examples("(pdf/pages pdf)")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("pdf/pages", args, 1);

				final ByteBuffer pdf = Coerce.toVncByteBuffer(args.first()).getValue();
				
				try {
		            final PdfReader reader = new PdfReader(pdf.array());
		            final int pages = reader.getNumberOfPages();
			        reader.close();
			        
			        return new VncLong(pages);
				}
				catch(Exception ex) {
					throw new VncException("pdf/pages: Failed to count the PDF's pages", ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

	public static VncFunction pdf_text_to_pdf = 
		new VncFunction(
				"pdf/text-to-pdf", 
				VncFunction
					.meta()
					.module("pdf")
					.arglists("pdf/text-to-pdf text & options")
					.doc(
						"Creates a PDF from simple text. The tool process line-feeds '\\n' " +
						"and form-feeds. To start a new page just insert a form-feed " +
						"marker \"<form-feed>\".\n\n" +
						"Options: \n" +
						"  :font-size n      - font size in pt (double), defaults to 9.0\n" +
						"  :font-weight n    - font weight (0...1000) (long), defaults to 200\n" +
						"  :font-monospace b - monospaced font (true/false) (boolean), defaults to false")

					.examples(
						"(->> (pdf/text-to-pdf \"Lorem Ipsum...\")   \n" +
						"     (io/spit \"text.pdf\"))                  ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("pdf/text-to-pdf", args, 1);

				try {
					final String text = Coerce.toVncString(args.first()).getValue();
					
					final VncMap options = VncHashMap.ofAll(args.slice(1));
					final VncDouble fontSize = getVncDoubleOption("font-size", options, 9.0); 
					final VncLong fontWeight = getVncLongOption("font-weight", options, 200); 
					final VncConstant fontMonoSpace = getBooleanOption("font-monospace", options, false); 


					final List<List<String>> pages = splitIntoPages(text)
														.stream()
														.map(p -> splitIntoLines(p))
														.collect(Collectors.toList());

					final Map<String,Object> data = new HashMap<>();		
					data.put("pages", pages);
					data.put("fontSize", fontSize.getValue().toString());
					data.put("fontWeight", fontWeight.getValue().toString());
					data.put("fontFamiliy", fontMonoSpace == True ? "Courier" : "Helvetica, Sans-Serif");

					final String template = loadText2PdfTemplate();
					
					// Need to run the template evaluation in its own thread because 
					// it runs a Venice interpreter, that must not conflict with this 
					// Venice interpreter.
					final String xhtml = runAsync(() -> evaluateTemplate(template, data));
	
					return new VncByteBuffer(PdfRenderer.render(xhtml));
				}
				catch(VncException ex) {
					throw ex;
				}
				catch(Exception ex) {
					throw new VncException("Failed to render text PDF", ex);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};

		
	private static List<String> mapAlternateBasePaths(final VncSequence paths) {
		return paths
			      .getList()
			      .stream()
			      .map(i -> Coerce.toVncString(i).getValue())
			      .collect(Collectors.toList());
	}
		
	private static Map<String,ByteBuffer> mapResources(final VncMap resourceMap) {
		final Map<String,ByteBuffer> resources = new HashMap<>();
		for (VncMapEntry entry : resourceMap.entries()) {
			resources.put(
				Coerce.toVncString(entry.getKey()).getValue(), 
				Coerce.toVncByteBuffer(entry.getValue()).getValue());
		}		
		return resources;
	}
	
	private static String loadText2PdfTemplate() {
		return new ClassPathResource("com/github/jlangch/venice/templates/text-2-pdf.kira")
						.getResourceAsString();
	}
	
	private static String evaluateTemplate(
			final String template, 
			final Map<String,Object> data
	) {
		final String script = 
				"(do                                           \n" +
				"   (load-module :kira)                        \n" +
				"   (kira/eval template [\"${\" \"}$\"] data))   ";

		return (String)new Venice().eval(
							script,
							Parameters.of("template", template, "data", data));
	}
	
	private static <T> T runAsync(final Callable<T> callable) throws Exception {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			return executor.submit(callable).get();
		}
		finally {
			executor.shutdownNow();
		}
	}

	private static List<String> splitIntoPages(final String text) {
		final List<String> pages = new ArrayList<>();
		
		if (text != null && !text.isEmpty()) {
			int lastPos = 0;
			while(lastPos < text.length()) {
				int pos = text.indexOf("<form-feed>", lastPos);
				if (pos < 0) {
					pages.add(text.substring(lastPos));
					break;
				}
				else {
					pages.add(text.substring(lastPos, pos));
					lastPos = pos + "<form-feed>".length();
				}
			}
		}
		
		return pages;
	}
	
	private static List<String> splitIntoLines(final String text) {
		return StringUtil
					.splitIntoLines(text)
					.stream()
					.map(s -> StringUtil.isBlank(s) ? "\u2002" : s)
					.map(s -> StringUtil.replaceLeadingSpaces(s, '\u00A0'))
					.collect(Collectors.toList());
	}
	
	private static VncDouble getVncDoubleOption(final String optName, final VncMap options, final double defaultVal) {
		final VncVal val = options.get(new VncKeyword(optName), new VncDouble(defaultVal));
		if (Types.isVncLong(val)) {
			return new VncDouble(((VncLong)val).getValue().doubleValue());
		}
		else if (Types.isVncInteger(val)) {
			return new VncDouble(((VncInteger)val).getValue().doubleValue());
		}
		else if (Types.isVncDouble(val)) {
			return (VncDouble)val;
		}
		else {
			throw new VncException("Invalid '" + optName + "' option type " + Types.getType(val));
		}
	}
	
	private static VncLong getVncLongOption(final String optName, final VncMap options, final long defaultVal) {
		final VncVal val = options.get(new VncKeyword(optName), new VncLong(defaultVal));
		if (Types.isVncLong(val)) {
			return (VncLong)val;
		}
		else if (Types.isVncInteger(val)) {
			return new VncLong(((VncInteger)val).getValue().longValue());
		}
		else if (Types.isVncDouble(val)) {
			return new VncLong(((VncDouble)val).getValue().longValue());
		}
		else {
			throw new VncException("Invalid '" + optName + "' option type " + Types.getType(val));
		}
	}
	
	private static VncConstant getBooleanOption(final String optName, final VncMap options, final boolean defaultVal) {
		final VncVal val = options.get(new VncKeyword(optName), defaultVal ? True : False);
		if (val == True) {
			return True;
		}
		else if (val == False) {
			return False;
		}
		else {
			throw new VncException("Invalid '" + optName + "' option type " + Types.getType(val));
		}
	}

	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()			
					.put("pdf/available?", pdf_available_Q)
					.put("pdf/render", pdf_render)
					.put("pdf/watermark", pdf_watermark)
					.put("pdf/merge", pdf_merge)
					.put("pdf/copy", pdf_copy)
					.put("pdf/pages", pdf_pages)
					.put("pdf/text-to-pdf", pdf_text_to_pdf)
					.toMap();	
}
