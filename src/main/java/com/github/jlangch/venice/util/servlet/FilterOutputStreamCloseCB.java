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
package com.github.jlangch.venice.util.servlet;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class FilterOutputStreamCloseCB extends FilterOutputStream {
	
	public FilterOutputStreamCloseCB(
			final OutputStream out,
			final Runnable onClose
	) {
		super(out);
		this.onClose = onClose;
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		}
		finally {
			if (onClose != null) {
				onClose.run();
			}
		}
	}
	
	
	private final Runnable onClose;
}
