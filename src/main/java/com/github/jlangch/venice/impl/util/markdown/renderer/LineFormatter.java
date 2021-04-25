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
package com.github.jlangch.venice.impl.util.markdown.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;


public class LineFormatter {

	public static List<String> leftAlign(final List<String> str, final int width) {
		if (str == null) {
			throw new IllegalArgumentException("A str list must not be null!");
		}	
		if (width < 0) {
			throw new IllegalArgumentException("A width must not be negative!");
		}
		
		return str.stream().map(s -> leftAlign(s, width)).collect(Collectors.toList());
	}

	public static String leftAlign(final String str, final int width) {
		if (width < 0) {
			throw new IllegalArgumentException("A width must not be negative!");
		}
		
		final String s = StringUtil.nullToEmpty(str);
		
		final int delta = width - s.length();
		return delta == 0
				? s
				: delta > 0
					? s + StringUtil.repeat(' ', delta)
					: s.substring(0, width);
	}
	
	public static List<String> rightAlign(final List<String> str, final int width) {
		if (str == null) {
			throw new IllegalArgumentException("A str list must not be null!");
		}	
		if (width < 0) {
			throw new IllegalArgumentException("A width must not be negative!");
		}
		
		return str.stream().map(s -> rightAlign(s, width)).collect(Collectors.toList());
	}

	public static String rightAlign(final String str, final int width) {
		if (width < 0) {
			throw new IllegalArgumentException("A width must not be negative!");
		}
		
		final String s = StringUtil.nullToEmpty(str);
		
		final int delta = width - s.length();
		return delta == 0
				? s
				: delta > 0
					? StringUtil.repeat(' ', delta) + s
					: s.substring(0, width);
	}
	
	public static List<String> centerAlign(final List<String> str, final int width) {
		if (str == null) {
			throw new IllegalArgumentException("A str list must not be null!");
		}	
		if (width < 0) {
			throw new IllegalArgumentException("A width must not be negative!");
		}
		
		return str.stream().map(s -> centerAlign(s, width)).collect(Collectors.toList());
	}
	
	public static String centerAlign(final String str, final int width) {
		if (width < 0) {
			throw new IllegalArgumentException("A width must not be negative!");
		}
		
		final String s = StringUtil.nullToEmpty(str);
		
		final int delta = width - s.length();
		if (delta == 0) {
			return s;
		}
		else if (delta < 0) {
			return s.substring(0, width);
		}
		else {
			final int leftPad = delta / 2;
			final int rightPad = delta - leftPad;
			
			return StringUtil.repeat(' ', leftPad) 
					+ s 
					+ StringUtil.repeat(' ', rightPad);
		}
	}
	
	public static List<String> bottomPad(final List<String> lines, final int height) {
		final int delta = height - lines.size();
		if (delta == 0) {
			return lines;
		}
		else if (delta < 0) {
			return lines.subList(0, height);
		}
		else {
			final List<String> tmp = new ArrayList<>(lines);
			for(int ii=0; ii<delta;ii++) {
				tmp.add("");
			}
			return tmp;
		}
	}

}
