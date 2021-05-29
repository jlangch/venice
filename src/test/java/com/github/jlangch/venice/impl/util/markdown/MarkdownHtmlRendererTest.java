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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class MarkdownHtmlRendererTest {

	@Test
	public void test_text_block1() {
		final String md = 
						"Lorem ipsum dolor 'sit' amet";
			
		final String expected =
						"<div class=\"md\">\n"
						+ "<div class=\"md-text-block\"><div class=\"md-text-normal\">Lorem ipsum dolor &apos;sit&apos; amet</div></div>\n"
						+ "</div>\n";

		final String rendered = Markdown.parse(md).renderToHtml();

		assertEquals(expected, rendered);
	}

	@Test
	public void test_text_block2() {
		final String md = 
						"Lorem *ipsum* \\* dolor **sit** amet";

		final String expected =
						"<div class=\"md\">\n"
						+ "<div class=\"md-text-block\"><div class=\"md-text-normal\">Lorem </div><div class=\"md-text-italic\">ipsum</div><div class=\"md-text-normal\"> * dolor </div><div class=\"md-text-bold\">sit</div><div class=\"md-text-normal\"> amet</div></div>\n"
						+ "</div>\n";

		final String rendered = Markdown.parse(md).renderToHtml();

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_list_block() {
		final String md = 
						"* Lorem ipsum dolor 'sit' amet\n" +
						"* Lorem *ipsum* dolor sit amet";

		final String expected =
						"<div class=\"md\">\n"
						+ "<div class=\"md-list-block\">\n"
						+ "<ul class=\"md-list\">\n"
						+ "<li><div class=\"md-text-block\"><div class=\"md-text-normal\">Lorem ipsum dolor &apos;sit&apos; amet</div></div>\n"
						+ "</li>\n"
						+ "<li><div class=\"md-text-block\"><div class=\"md-text-normal\">Lorem </div><div class=\"md-text-italic\">ipsum</div><div class=\"md-text-normal\"> dolor sit amet</div></div>\n"
						+ "</li>\n"
						+ "</ul>\n"
						+ "</div>\n"
						+ "</div>\n";

		final String rendered = Markdown.parse(md).renderToHtml();

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_code_block() {
		final String md = 
						"```venice\n" +
						"(do\n" +
						"  (> 1 100))\n" +
						"```";

		final String expected =
						"<div class=\"md\">\n"
						+ "<div class=\"md-code-block\">\n"
						+ "<code class=\"md-code\">(do\n"
						+ "  (&gt; 1 100))</code></div>\n"
						+ "</div>\n";
		
		final String rendered = Markdown.parse(md).renderToHtml();

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_table_block() {
		final String md =  
						"|T1|T2|T3|\n" +
						"|:-|:-:|-:|\n" +
						"|c1...|c2.|c3...|\n" +
						"|d1'..|d2.....|d3...|\n" +
						"||e2.||";

		final String expected =
						  "<div class=\"md\">\n"
						  + "<div class=\"md-table-block\">\n"
						  + "<table class=\"md-table\">\n"
						  + "<thead>\n"
						  + "<tr>\n"
						  + "<th class=\"md-align-left\">T1</th>\n"
						  + "<th class=\"md-align-center\">T2</th>\n"
						  + "<th class=\"md-align-right\">T3</th>\n"
						  + "</tr>\n"
						  + "</thead>\n"
						  + "<tbody>\n"
						  + "<tr>\n"
						  + "<td class=\"md-align-left\"><div class=\"md-text-normal\">c1...</div></td>\n"
						  + "<td class=\"md-align-center\"><div class=\"md-text-normal\">c2.</div></td>\n"
						  + "<td class=\"md-align-right\"><div class=\"md-text-normal\">c3...</div></td>\n"
						  + "</tr>\n"
						  + "<tr>\n"
						  + "<td class=\"md-align-left\"><div class=\"md-text-normal\">d1&apos;..</div></td>\n"
						  + "<td class=\"md-align-center\"><div class=\"md-text-normal\">d2.....</div></td>\n"
						  + "<td class=\"md-align-right\"><div class=\"md-text-normal\">d3...</div></td>\n"
						  + "</tr>\n"
						  + "<tr>\n"
						  + "<td class=\"md-align-left\"></td>\n"
						  + "<td class=\"md-align-center\"><div class=\"md-text-normal\">e2.</div></td>\n"
						  + "<td class=\"md-align-right\"></td>\n"
						  + "</tr>\n"
						  + "</tbody>\n"
						  + "</table>\n"
						  + "</div>\n"
						  + "</div>\n"
						  + "";

		final String rendered = Markdown.parse(md).renderToHtml();

		assertEquals(expected, rendered);
	}

}
