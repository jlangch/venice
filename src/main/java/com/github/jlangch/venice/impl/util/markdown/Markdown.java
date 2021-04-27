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
package com.github.jlangch.venice.impl.util.markdown;

import com.github.jlangch.venice.impl.util.markdown.block.BlockParser;
import com.github.jlangch.venice.impl.util.markdown.block.Blocks;
import com.github.jlangch.venice.impl.util.markdown.renderer.html.HtmlRenderer;
import com.github.jlangch.venice.impl.util.markdown.renderer.text.TextRenderer;


public class Markdown {
	
	private Markdown(final Blocks blocks) {
		this.blocks = blocks;
	}
	
	public static Markdown parse(final String text) {
		return new Markdown(new BlockParser(text).parse());
	}
	
	public Blocks blocks() {
		return blocks;
	}
	
	public String renderToText(final int width) {
		return TextRenderer.softWrap(width).render(this);
	}
	
	public String renderToHtml() {
		return new HtmlRenderer().render(this);
	}
	
	
	private final Blocks blocks;
}
