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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.StringUtil;
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
						"(pdf/render xhtml base-url alternate-base-paths)")		
					.doc("Renders a PDF.")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertArity("pdf/render", args, 1, 2, 3);
				
				if (args.size() == 1) {
					return new VncByteBuffer(
									PdfRenderer.render(
											Coerce.toVncString(args.first()).getValue()));
				}
				else if (args.size() == 2) {
					return new VncByteBuffer(
									PdfRenderer.render(
											Coerce.toVncString(args.first()).getValue(),
											Coerce.toVncString(args.second()).getValue(),
											null));
				}
				else {
					final List<String> alternateBasePaths = 
							Coerce.toVncSequence(args.third())
							      .getList()
							      .stream()
							      .map(i -> i.toString())
							      .collect(Collectors.toList());
					
					return new VncByteBuffer(
							PdfRenderer.render(
									Coerce.toVncString(args.first()).getValue(),
									Coerce.toVncString(args.second()).getValue(),
									alternateBasePaths));
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
					.arglists("(pdf/watermark pdf text & options)")		
					.doc(
						"Adds a watermark text to the PDF pages.\n\n" +
						"Options: \n" +
						"  :font-size n         - font size (double), defaults to 24.0 \n" +
						"  :font-char-spacing n - font character spacing (double), defaults to 0.0 \n" +
						"  :color c             - font color (HTML color string), defaults to #000000 \n" +
						"  :opacity n           - opacity 0.0 ... 1.0 (double), defaults to 0.8 \n" +
						"  :angle n             - angle 0.0 ... 360.0 (double), defaults to 45.0 \n" +
						"  :over-content b      - print text over the content (boolean), defaults to true \n" +
						"  :skip-top-pages n    - the number of top pages to skip (long), defaults to 0 \n" +
						"  :skip-bottom-pages n - the number of bottom pages to skip (long), defaults to 0")
					.build()
		) {		
			public VncVal apply(final VncList args) {
				assertMinArity("pdf/watermark", args, 2);
				
				final VncVal pdf = args.first();
				final VncVal text = args.second();

				final VncHashMap options = VncHashMap.ofAll(args.slice(2));

				final VncVal fontSize = options.get(new VncKeyword("font-size", new VncDouble(24.0))); 
				final VncVal fontCharSpacing = options.get(new VncKeyword("font-char-spacing", new VncDouble(0.0))); 
				final VncVal color = options.get(new VncKeyword("color", new VncString("#000000"))); 
				final VncVal opacity = options.get(new VncKeyword("opacity", new VncDouble(0.8))); 
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
	
		
		
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("pdf/render", pdf_render)
					.put("pdf/watermark", pdf_watermark)
					.toMap();	
	
}
