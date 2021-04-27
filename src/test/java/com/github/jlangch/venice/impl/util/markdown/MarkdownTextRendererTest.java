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

import com.github.jlangch.venice.impl.util.markdown.renderer.text.TextRenderer;


public class MarkdownTextRendererTest {

	@Test
	public void test_text_block() {
		final String md = 
				  "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
				+ "sed diam nonumy eirmod tempor invidunt ut labore et "
				+ "dolore magna aliquyam erat, sed diam voluptua. At vero "
				+ "eos et accusam et justo duo dolores et ea rebum. Stet "
				+ "clita kasd gubergren, no sea takimata sanctus est "
				+ "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, "
				+ "consetetur sadipscing elitr, sed diam nonumy eirmod "
				+ "tempor invidunt ut labore et dolore magna aliquyam "
				+ "erat, sed diam voluptua.";
			
		final String expected =
				  "Lorem ipsum dolor sit amet, consetetur sadipscing\n"
				+ "elitr, sed diam nonumy eirmod tempor invidunt ut\n"
				+ "labore et dolore magna aliquyam erat, sed diam\n"
				+ "voluptua. At vero eos et accusam et justo duo\n"
				+ "dolores et ea rebum. Stet clita kasd gubergren, no\n"
				+ "sea takimata sanctus est Lorem ipsum dolor sit\n"
				+ "amet. Lorem ipsum dolor sit amet, consetetur\n"
				+ "sadipscing elitr, sed diam nonumy eirmod tempor\n"
				+ "invidunt ut labore et dolore magna aliquyam erat,\n"
				+ "sed diam voluptua.";
		
		final String rendered = TextRenderer.softWrap(50).render(Markdown.parse(md));

		assertEquals(expected, rendered);
	}

	@Test
	public void test_text_block_2() {
		final String md = 
				  "Lorem ipsum *dolor* sit amet, consetetur sadipscing elitr, "
				+ "sed diam nonumy eirmod tempor invidunt ut labore et "
				+ "dolore magna aliquyam erat, sed diam voluptua. At vero "
				+ "eos et accusam et justo ***duo*** dolores et ea rebum. Stet "
				+ "clita kasd gubergren, no sea takimata sanctus est "
				+ "Lorem **ipsum** dolor sit amet. \n\n"
				+ "Lorem   ipsum dolor sit   amet, consetetur sadipscing elitr, "
				+ "sed diam nonumy eirmod tempor `invidunt` ut labore et "
				+ "dolore magna aliquyam erat, sed diam voluptua.";
			
		final String expected =
				  "Lorem ipsum dolor sit amet, consetetur sadipscing\n"
				+ "elitr, sed diam nonumy eirmod tempor invidunt ut\n"
				+ "labore et dolore magna aliquyam erat, sed diam\n"
				+ "voluptua. At vero eos et accusam et justo duo\n"
				+ "dolores et ea rebum. Stet clita kasd gubergren, no\n"
				+ "sea takimata sanctus est Lorem ipsum dolor sit\n"
				+ "amet.\n"
				+ "\n"
				+ "Lorem ipsum dolor sit amet, consetetur sadipscing\n"
				+ "elitr, sed diam nonumy eirmod tempor invidunt ut\n"
				+ "labore et dolore magna aliquyam erat, sed diam\n"
				+ "voluptua.";
		
		final String rendered = TextRenderer.softWrap(50).render(Markdown.parse(md));

		assertEquals(expected, rendered);
	}

	@Test
	public void test_code_block() {
		final String md = 
				  "```venice\n"
				+ "(defmacro\n"
				+ "  ^{ :arglists '(\"(comment & body)\")\n"
				+ "     :doc \"Ignores body, yields nil\"\n"
				+ "     :examples '(\n"
				+ "          \"\"\"\n"
				+ "          (comment\n"
				+ "            (println 1)\n"
				+ "            (println 5))\n"
				+ "          \"\"\" ) }\n"
				+ "\n"
				+ "   comment [& body] nil)\n"
				+ "```";
			
		final String expected =
				  "(defmacro\n"
				+ "  ^{ :arglists '(\"(comment & body)\")\n"
				+ "     :doc \"Ignores body, yields nil\"\n"
				+ "     :examples '(\n"
				+ "          \"\"\"\n"
				+ "          (comment\n"
				+ "            (println 1)\n"
				+ "            (println 5))\n"
				+ "          \"\"\" ) }\n"
				+ "\n"
				+ "   comment [& body] nil)\n";
		
		final String rendered = Markdown.parse(md).renderToText(50);

		assertEquals(expected, rendered);
	}

	@Test
	public void test_list_block() {
		final String md = 
				  "* Lorem ipsum dolor sit amet, consetetur sadipscing elit\n"
				+ "  sed diam nonumy eirmod tempor invidunt ut labore et\n"
				+ "* At vero eos et accusam et justo duo\n"
				+ "  dolores et ea rebum.";
				
		final String expected =
				  "o Lorem ipsum dolor sit amet,\n"
				+ "  consetetur sadipscing elit\n"
				+ "  sed\n"
				+ "  diam nonumy eirmod tempor\n"
				+ "  invidunt ut labore et\n"
				+ "o At vero eos et accusam et\n"
				+ "  justo duo dolores et ea\n"
				+ "  rebum.";
		
		final String rendered = Markdown.parse(md).renderToText(30);

		assertEquals(expected, rendered);
	}

	@Test
	public void test_table_block() {
		final String md = 
				"|T1|T2|\n" +
				"|:-|:-|\n" +
				"|c1|Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
						+ "sed diam nonumy eirmod tempor invidunt ut labore et "
						+ "dolore magna aliquyam erat, sed diam voluptua. At vero "
						+ "eos et accusam et justo duo dolores et ea rebum. Stet "
						+ "clita kasd gubergren, no sea takimata sanctus est "
						+ "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, "
						+ "consetetur sadipscing elitr, sed diam nonumy eirmod "
						+ "tempor invidunt ut labore et dolore magna aliquyam "
						+ "erat, sed diam voluptua.|\n" +
				"|d1|At vero eos et accusam et justo duo dolores et ea "
						+ "rebum. Stet clita kasd gubergren, no sea takimata "
						+ "sanctus est Lorem ipsum dolor sit amet.|";
			
		final String expected =
				"T1  T2\n" +
				"--  ----------------------------------------------------------------------------\n" +
				"c1  Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy\n" +
				"    eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam\n" +
				"    voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet\n" +
				"    clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit\n" +
				"    amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam\n" +
				"    nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed\n" +
				"    diam voluptua.\n" +
				"d1  At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd\n" +
				"    gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
		
		final String rendered = Markdown.parse(md).renderToText(80);

		assertEquals(expected, rendered);
	}

}
