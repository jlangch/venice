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
package com.github.jlangch.venice.impl.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncString;


public class ZipFileSystemUtil {

	public static FileSystem mountZip(final File zip) throws IOException {
		try {
			return FileSystems.newFileSystem(
					zip.toPath(),
					Zipper.class.getClassLoader());
		}
		catch(Exception ex) {
			throw new VncException(String.format(
						"Failed to mount ZIP filesystem from '%s'",
						zip.getPath()));
		}
	}
	
	public static VncByteBuffer loadBinaryFileFromZip(final File zip, final File file) {
		if (!zip.exists()) {
			throw new VncException(String.format(
					"The ZIP file '%s' does not exist",
					zip.getPath()));
		}
		
		try {
			try (FileSystem zipFS = mountZip(zip)) {
				final byte[] data = Files.readAllBytes(zipFS.getPath(file.getPath()));
				return new VncByteBuffer(data);
			}
		}
		catch(Exception ex) {
			throw new VncException(String.format(
						"Failed to load binary file '%s' from ZIP '%s'",
						file.getPath(),
						zip.getPath()));
		}
	}
	
	public static VncString loadTextFileFromZip(final File zip, final File file, final String encoding) {
		if (!zip.exists()) {
			throw new VncException(String.format(
					"The ZIP file '%s' does not exist",
					zip.getPath()));
		}
		
		try {
			try (FileSystem zipFS = mountZip(zip)) {
				final byte[] data = Files.readAllBytes(zipFS.getPath(file.getPath()));
				return new VncString(new String(data, encoding == null ? "utf-8" : encoding));
			}
		}
		catch(Exception ex) {
			throw new VncException(String.format(
						"Failed to load file '%s' from ZIP '%s'",
						file.getPath(),
						zip.getPath()));
		}
	}

}
