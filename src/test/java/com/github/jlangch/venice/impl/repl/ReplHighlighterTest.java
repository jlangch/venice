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
package com.github.jlangch.venice.impl.repl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.junit.jupiter.api.Test;


public class ReplHighlighterTest {

	@Test
	public void test_attribute() {	
		final AttributedStringBuilder sb = new AttributedStringBuilder();
		sb.ansiAppend("\u001b[38;5;208m" + "xxx" + "\u001b[0m");		
		final AttributedString as = sb.toAttributedString();
		
		assertEquals("\u001b[38;5;208mxxx\u001b[0m", as.toAnsi());
	}
}
