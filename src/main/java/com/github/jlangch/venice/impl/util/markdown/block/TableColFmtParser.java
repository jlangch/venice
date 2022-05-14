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

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.markdown.block.TableBlock.HorzAlignment;


public class TableColFmtParser {

	public TableColFmtParser() {
	}
	
	public TableColFmt parse(final String format) {
		final String fmt = StringUtil.trimToEmpty(format);

		final HorzAlignment align = parseMarkdownStyleHorzAlignment(fmt);
		return align == null ? null : new TableColFmt(align);
	}

	
	private HorzAlignment parseMarkdownStyleHorzAlignment(final String format) {
		if (isCenterAlign(format)) {
			return TableBlock.HorzAlignment.CENTER;
		}
		else if (isLeftAlign(format)) {
			return TableBlock.HorzAlignment.LEFT;
		}
		else if (isRightAlign(format)) {
			return TableBlock.HorzAlignment.RIGHT;
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

	
}
