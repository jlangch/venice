/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;


public class IOStreamUtil {

	public static byte[] copyIStoByteArray(final InputStream is) throws IOException{
		if (is == null) {
			return null;
		}
		
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			final byte[] buffer = new byte[16 * 1024];
			int n;
			while (-1 != (n = is.read(buffer))) {
				output.write(buffer, 0, n);
			}

			return output.toByteArray();
		}
	}
	
	public static String copyIStoString(
			final InputStream is,
			final String encoding
	) throws IOException{
		return is == null 
				? null 
				: new String(
						IOStreamUtil.copyIStoByteArray(is), 
						encoding == null ? Charset.defaultCharset().name() : encoding);
	}

	public static void copyByteArrayToOS(
			final byte[] data, 
			final OutputStream os
	) throws IOException{
		if (os == null || data == null) {
			return;
		}
		
		os.write(data);
		os.flush();
	}

	public static void copyFileToOS(
			final File file, 
			final OutputStream os
	) throws IOException{
		if (os == null || file == null) {
			return;
		}
		
		try (FileInputStream is = new FileInputStream(file)) {
			copy(is, os);
		}
	}

	public static void copyStringToOS(
			final String data, 
			final OutputStream os, 
			final String encoding
	) throws IOException{
		if (os == null || data == null) {
			return;
		}
		
		os.write(encoding == null 
					? data.getBytes(Charset.defaultCharset())
					: data.getBytes(encoding));
		
		os.flush();
	}

	
	public static void copy(final InputStream is, final OutputStream os) 
	throws IOException {
		int len;
		byte[] buf=new byte[4096];
		 
		while ((len=is.read(buf))!=-1) {
			os.write(buf,0,len);
		}
		
		os.flush();
	}
}
