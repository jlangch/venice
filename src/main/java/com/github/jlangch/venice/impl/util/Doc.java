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
package com.github.jlangch.venice.impl.util;

import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Types;


public class Doc {
	
	public static VncString getDoc(final VncVal val) {
		if (val != null && Types.isVncFunction(val)) {
			final VncFunction fn = (VncFunction)val;
			final VncList argsList = fn.getArgLists();
			final VncList examples = fn.getExamples();
			final VncList seeAlso = fn.getSeeAlso();
			
			final StringBuilder sb =  new StringBuilder();
						
			sb.append(argsList
						.stream()
						.map(s -> toString(s))
						.collect(Collectors.joining(", ")));
			
			sb.append("\n\n");
			sb.append(toString(fn.getDoc()));
			
			if (!examples.isEmpty()) {
				sb.append("\n\n");
				sb.append("EXAMPLES:\n");
				sb.append(examples
							.stream()
							.map(s -> toString(s))
							.map(e -> indent(e, "   "))
							.collect(Collectors.joining("\n\n")));
			}

			if (!seeAlso.isEmpty()) {
				sb.append("\n\n");
				sb.append("SEE ALSO:\n   ");
				sb.append(seeAlso
							.stream()
							.map(s -> toString(s))
							.collect(Collectors.joining(", ")));
			}

			sb.append("\n");

			return new VncString(sb.toString());			
		}
				
		return new VncString("<no documentation available>");			
	}
	
	private static String indent(final String text, final String indent) {
		if (StringUtil.isBlank(text)) {
			return text;
		}
		else {
			return StringUtil
						.splitIntoLines(text)
						.stream()
						.map(s -> indent + s)
						.collect(Collectors.joining("\n"));
		}
	}
	
	private static String toString(final VncVal val) {
		return val == Constants.Nil ? "" : ((VncString)val).getValue();
	}
	
}