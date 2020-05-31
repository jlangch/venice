/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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

import java.io.PushbackReader;
import java.io.Reader;
import java.io.LineNumberReader;
import java.io.IOException;


public class LineNumberingPushbackReader extends PushbackReader {

	public LineNumberingPushbackReader(final Reader r) {
		super(new LineNumberReader(r));
	}

	public LineNumberingPushbackReader(final Reader r, final int bufSize) {
		super(new LineNumberReader(r, bufSize));
	}

	public int getLineNumber() {
		return ((LineNumberReader)in).getLineNumber() + 1;
	}

	public int getColumnNumber(){
		return columnNumber;
	}

	public int getPos(){
		return pos;
	}

	@Override
	public int read() throws IOException {
		final int ch = super.read();
				
		if (ch == -1) {
			columnNumber = 1;
		}
		else if (ch == LF) {
			columnNumber = 1;
			pos++;
		}
		else {
			columnNumber++;
			pos++;
		}
		
		return ch;
	}

	@Override
	public void unread(final int ch) throws IOException{
		if (ch == LF) {
			throw new IOException("unreading a linefeed is not supported");
		}
		
		if (ch != -1) {
			super.unread(ch);
			
			pos--;			
			columnNumber--;
		}
	}

	
	private static final int LF = (int)'\n';
	
	private int columnNumber = 1;
	private int pos = 0;
}
