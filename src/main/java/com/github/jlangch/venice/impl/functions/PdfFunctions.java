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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.pdf.PdfRenderer;


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
	
		
		
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("pdf/render", pdf_render)
					.toMap();	
	
}
