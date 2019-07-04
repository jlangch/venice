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

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
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
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.pdf.HtmlColor;
import com.github.jlangch.venice.pdf.PdfRenderer;
import com.github.jlangch.venice.pdf.PdfWatermark;


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
						"  :font-size n         - font size (double), defaults to 24.0 \n" +
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
				final VncVal fontSize = options.get(new VncKeyword("font-size", new VncDouble(24.0))); 
				final VncVal fontCharSpacing = options.get(new VncKeyword("font-char-spacing", new VncDouble(0.0))); 
				final VncVal color = options.get(new VncKeyword("color", new VncString("#000000"))); 
				final VncVal opacity = options.get(new VncKeyword("opacity", new VncDouble(0.4))); 
				final VncVal angle = options.get(new VncKeyword("angle", new VncDouble(45.0))); 
				final VncVal overContent = options.get(new VncKeyword("over-content", Constants.True)); 
				final VncVal skipTopPages = options.get(new VncKeyword("skip-top-pages", new VncLong(0))); 
				final VncVal skipBottomPages = options.get(new VncKeyword("skip-bottom-pages", new VncLong(0))); 

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

	public static VncFunction pdf_text_to_pdf = 
		new VncFunction(
				"pdf/text-to-pdf", 
				VncFunction
					.meta()
					.module("pdf")
					.arglists("pdf/text-to-pdf text")
					.doc("Creates a PDF from simple text.")
					.examples(
						"(->> (pdf/text-to-pdf \"Lorem Ipsum...\")   \n" +
						"     (io/spit \"text.pdf\"))                  ")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("pdf/text-to-pdf", args, 1);

				try {
					final String text = Coerce.toVncString(args.first()).getValue();

					final List<String> lines = 
							StringUtil
								.splitIntoLines(text)
								.stream()
								.map(s -> StringUtil.isBlank(s) ? " " : s)
								.map(s -> StringUtil.replaceLeadingSpaces(s, '\u00A0'))
								.collect(Collectors.toList());

					final Map<String,Object> data = new HashMap<>();		
					data.put("lines", lines);

					final String script = "(do                                           \n" +
										  "   (load-module :kira)                        \n" +
										  "   (kira/eval template [\"${\" \"}$\"] data))   ";
					
					final String xhtml = (String)new Venice().eval(
											script,
											Parameters.of(
												"template", TEXT_TO_PDF, 
												"data", data));

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
	
		
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()			
					.put("pdf/available?", pdf_available_Q)
					.put("pdf/render", pdf_render)
					.put("pdf/watermark", pdf_watermark)
					.put("pdf/text-to-pdf", pdf_text_to_pdf)
					.toMap();	
	
	
	private static final String TEXT_TO_PDF =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                \n" +
		"<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\"> \n" +
		"  <head>                                                  \n" +
		"    <title>Text to PDF</title>                            \n" +
		"                                                          \n" +
		"    <!-- Local styles -->                                 \n" +
		"    <style type=\"text/css\">                             \n" +
		"      @page {                                             \n" +
		"        size: A4 portrait;                                \n" +
		"        margin: 2cm 1.5cm;                                \n" +
		"        padding: 0;                                       \n" +
		"      }                                                   \n" +
		"                                                          \n" +
		"      body {                                              \n" +
		"        font-family: Helvetica, Sans-Serif;               \n" +
		"        font-size: 9pt;                                   \n" +
		"        font-weight: 200;                                 \n" +
		"      }                                                   \n" +
		"    </style>                                              \n" +
		"  </head>                                                 \n" +
		"                                                          \n" +
		"  <body>                                                  \n" +
		"    ${ (kira/docoll lines (fn [l] (kira/emit }$           \n" +
		"      <div>${ (kira/escape-xml l) }$</div>                \n" +
		"    ${ ))) }$                                             \n" +
		"  </body>                                                 \n" +
		"</html>                                                     ";
}
