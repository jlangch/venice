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
package com.github.jlangch.venice.impl.util.markdown.block;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment;
import com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.WidthUnit;


public class TableColFmtParser {

	public TableColFmtParser() {
	}
	
	public TableColFmt parse(final String format) {
		final String fmt = StringUtil.trimToEmpty(format);
		
		final TableColFmt fmtMD = parseMarkdownStyleFormat(fmt);
		if (fmtMD != null) {
			return fmtMD;
		}
		else {
			return parseCssStyleFormat(fmt);
		}
	}


	private TableColFmt parseMarkdownStyleFormat(final String format) {
		final HorzAlignment align = parseMarkdownStyleHorzAlignment(format);
		return align == null ? null : new TableColFmt(align, null);
	}

	@SuppressWarnings("unchecked")
	private TableColFmt parseCssStyleFormat(final String format) {
		if (format.startsWith("[![") && format.endsWith("]]")) {
			final String css = format.substring(3, format.length()-2).trim();
			if (!css.isEmpty()) {
				try {
					final PreCompiled precompiled = getCssParser();
					
					final Venice venice = new Venice();
					
					final Map<String,Object> cssProps = (Map<String,Object>)venice.eval(
															precompiled, 
															Parameters.of("css", css));

					final HorzAlignment align = parseCssStyleHorzAlignment(cssProps);
					
					final TableColFmt.Width width = parseCssStyleWidth(cssProps);

					return new TableColFmt(align, width);
				}
				catch(RuntimeException ex) {
					throw new RuntimeException(
							"Failed to parse markdown table column css '"+ css + "'",
							ex);
				}
			}
		}
		
		return null;
	}
	
	private HorzAlignment parseCssStyleHorzAlignment(final Map<String,Object> cssProps) {
		final String align = (String)cssProps.get("text-align");

		switch(StringUtil.trimToEmpty(align)) {
			case "left":   return HorzAlignment.LEFT;
			case "center": return HorzAlignment.CENTER;
			case "right":  return HorzAlignment.RIGHT;
			default:       return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private TableColFmt.Width parseCssStyleWidth(final Map<String,Object> cssProps) {
		// "auto", [30, "%"]
		Object width = cssProps.get("width");
		if (width != null) {
			if (width instanceof String) {
				if ("auto".equals(width)) {
					return new TableColFmt.Width(0, WidthUnit.AUTO); 
				}
			}
			
			if (width instanceof List) {
				long val = (long)((List<Object>)width).get(0);
				String unit = (String)((List<Object>)width).get(1);

				switch(StringUtil.trimToEmpty(unit)) {
					case "%":  return new TableColFmt.Width(val, WidthUnit.PERCENT);
					case "px": return new TableColFmt.Width(val, WidthUnit.PERCENT);
					case "em": return new TableColFmt.Width(val, WidthUnit.PERCENT);
					default:   return new TableColFmt.Width(0, WidthUnit.AUTO); 
				}
			}
		}
		
		return new TableColFmt.Width(0, WidthUnit.AUTO); 
	}
	
	private HorzAlignment parseMarkdownStyleHorzAlignment(final String format) {
		if (isCenterAlign(format)) {
			return HorzAlignment.CENTER;
		}
		else if (isLeftAlign(format)) {
			return HorzAlignment.LEFT;
		}
		else if (isRightAlign(format)) {
			return HorzAlignment.RIGHT;
		}
		else {
			return null;
		}
	}
	
	private boolean isCenterAlign(final String s) {
		return s.matches("---+") || s.matches("[:]-+[:]");
	}
	
	private boolean isLeftAlign(final String s) {
		return s.matches("[:]-+");
	}
	
	private boolean isRightAlign(final String s) {
		return s.matches("-+[:]");
	}
	
	private PreCompiled getCssParser() {
		PreCompiled pc = cssParser.get();
		if (pc == null) {
			final String parser = new ClassPathResource(CSS_PARSER).getResourceAsString();
			
			pc = new Venice().precompile("CssParser", parser, true);
			cssParser.set(pc);
		}
		
		return pc;
	}

	
	private static AtomicReference<PreCompiled> cssParser = new AtomicReference<>();
	
	private static String CSS_PARSER = 
			"com/github/jlangch/venice/impl/util/markdown/block/table-col-css-parser.venice";
}
