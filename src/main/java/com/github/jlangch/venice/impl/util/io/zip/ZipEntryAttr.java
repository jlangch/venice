/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.util.io.zip;

import java.nio.file.attribute.FileTime;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class ZipEntryAttr {

	public ZipEntryAttr(
			final String name,
			final boolean isDirectory,
			final String method,
			final long size,
			final long compressedSize,
			final FileTime lastModifiedTime,
			final Long crc
	) {
		this.method = method;
		this.size = size;
		this.compressedSize = compressedSize;
		this.lastModifiedTime = lastModifiedTime;
		this.crc = crc;
		this.name = name;
		this.isDirectory = isDirectory;
	}

	
	public long getSize() {
		return size;
	}
	
	public String getMethod() {
		return method;
	}
	
	public long getCompressedSize() {
		return compressedSize;
	}
	
	public FileTime getLastModifiedTime() {
		return lastModifiedTime;
	}
	
	public Long getCrc() {
		return crc;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isDirectory() {
		return isDirectory;
	}

	
	public VncMap toVncMap() {
		return VncHashMap.of(
				new VncKeyword("name"),            new VncString(name),
				new VncKeyword("directory"),       VncBoolean.of(isDirectory),
				new VncKeyword("method"),          new VncString(method),
				new VncKeyword("size"),            new VncLong(size),
				new VncKeyword("compressed-size"), new VncLong(compressedSize),
				new VncKeyword("crc"),             crc == null ? Constants.Nil : new VncLong(crc));
	}

	
	private final long size;
	private final String method;
	private final long compressedSize;
	private final FileTime lastModifiedTime;
	private final Long crc;
	private final String name;
	private final boolean isDirectory;
}
