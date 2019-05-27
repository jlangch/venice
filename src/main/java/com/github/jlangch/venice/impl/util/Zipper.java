/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * A helper to compress/uncompress binary data blocks using the zip/gzip
 * inflater/deflater.
 */
public class Zipper {

	public static byte[] zip(final byte[] binary, final String entryName) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
		if (StringUtil.isEmpty(entryName)) {
			throw new IllegalArgumentException("A 'entryName' must not be null or empty");
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				final ZipEntry entry = new ZipEntry(entryName);
				entry.setMethod(ZipEntry.DEFLATED);
				
				zos.putNextEntry(entry);
				zos.write(binary, 0, binary.length);
				zos.closeEntry();
				
				zos.flush();
			}
	
			return baos.toByteArray();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] zip(final Map<String, Object> entries) {
		if (entries == null ) {
			throw new IllegalArgumentException("An 'entries' map must not be null");
		}
		
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				for (Map.Entry<String,Object> entry : entries.entrySet()) {
					final Object value = entry.getValue();
					byte[] bytes;
					if (entry.getValue() != null) {
						if (value instanceof byte[]) {
							bytes = (byte[])value;
						}
						else if (value instanceof InputStream) {
							bytes = IOStreamUtil.copyIStoByteArray((InputStream)value);
						}
						else {
							throw new IllegalArgumentException(
									"Only values of type byte[] or InputStream are supoorted!");
						}
						
						final ZipEntry e = new ZipEntry(entry.getKey());
						e.setMethod(ZipEntry.DEFLATED);
						zos.putNextEntry(e);
						zos.write(bytes, 0, bytes.length);
						zos.closeEntry();
					}
				}
				
				zos.flush();
			}
	
			return baos.toByteArray();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static byte[] unzip(final byte[] binary, final String entryName) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
		if (StringUtil.isEmpty(entryName)) {
			throw new IllegalArgumentException("A 'entryName' must not be null or empty");
		}

		try {
			final ByteArrayInputStream bais = new ByteArrayInputStream(binary);
	
			try (ZipInputStream zis = new ZipInputStream(bais)) {
				while(true) {
					final ZipEntry entry = zis.getNextEntry();
					if (entry == null) {
						break;
					}
					
					final byte[] data = IOStreamUtil.copyIStoByteArray(zis);

					zis.closeEntry();

					if (entryName.equals(entry.getName())) {
						return data;
					}					
				}
				
				return null; // ZIP entry not found 
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] unzipNthEntry(final byte[] binary, final int nth) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}

		try {
			final ByteArrayInputStream bais = new ByteArrayInputStream(binary);
	
			try (ZipInputStream zis = new ZipInputStream(bais)) {
				int entryIdx = 0;
				
				while(true) {
					final ZipEntry entry = zis.getNextEntry();
					if (entry == null) {
						break;
					}
					
					final byte[] data = IOStreamUtil.copyIStoByteArray(zis);
					
					if (entryIdx == nth) {
						return data;
					}
					
					zis.closeEntry();
					entryIdx++;
				}
				
				return null; // ZIP entry not found 
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static InputStream unzipToStream(final byte[] binary, final String entryName) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
		if (StringUtil.isEmpty(entryName)) {
			throw new IllegalArgumentException("A 'entryName' must not be null or empty");
		}

		
		try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(binary))) {			
			while(true) {
				final ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}						
				if (entryName.equals(entry.getName())) {
					return zis;
				}		
				zis.closeEntry();
			}
			return null;
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static InputStream unzipFirstToStream(final byte[] binary) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}

		return unzipFirstToStream(new ByteArrayInputStream(binary));
	}
	
	public static InputStream unzipFirstToStream(final InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("An 'inputStream' must not be null");
		}

		try {
			final ZipInputStream zis = new ZipInputStream(inputStream);
			
			// return the first entry that is not a directory
			while (true) {
				final ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}
				else if (!entry.isDirectory()) {
					return zis;
				}
			}
			return null;
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static Map<String, byte[]> unzipAll(
			final byte[] binary,
			final boolean includeDirs
	) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
		
		try {
			final Map<String, byte[]> files = new HashMap<String, byte[]>();
			
			final ByteArrayInputStream bais = new ByteArrayInputStream(binary);
	
			try (ZipInputStream zis = new ZipInputStream(bais)) {
				while(true) {
					final ZipEntry entry = zis.getNextEntry();
					if (entry == null) {
						break;
					}
					
					final byte[] data = IOStreamUtil.copyIStoByteArray(zis);
					
					if (!entry.isDirectory() || includeDirs) {
						files.put(
							entry.getName(), 
							entry.isDirectory() ? null : data);
					}
					
					zis.closeEntry();
				}
				
				return files;
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static Map<String, byte[]> unzipAll(final byte[] binary) {
		return unzipAll(binary, false);
	}
	
	public static byte[] gzip(final byte[] binary) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
			try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
				gzos.write(binary, 0, binary.length);
				gzos.flush();
			}
	
			return baos.toByteArray();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] gzip(final InputStream is) {
		if (is == null) {
			throw new IllegalArgumentException("An 'is' must not be null");
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
			try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
				IOStreamUtil.copy(is, gzos);
				gzos.flush();
			}
	
			return baos.toByteArray();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void gzip(final byte[] binary, final OutputStream os) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
		if (os == null) {
			throw new IllegalArgumentException("An 'os' must not be null");
		}

		try {
			try (GZIPOutputStream gzos = new GZIPOutputStream(os)) {
				gzos.write(binary, 0, binary.length);
				gzos.flush();
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void gzip(final InputStream is, final OutputStream os) {
		if (is == null) {
			throw new IllegalArgumentException("An 'is' must not be null");
		}
		if (os == null) {
			throw new IllegalArgumentException("An 'os' must not be null");
		}

		try {
			try (GZIPOutputStream gzos = new GZIPOutputStream(os)) {
				IOStreamUtil.copy(is, gzos);
				gzos.flush();
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] ungzip(final byte[] binary) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}

		final ByteArrayInputStream bais = new ByteArrayInputStream(binary);

		try {
			try (GZIPInputStream gzis = new GZIPInputStream(bais)) {
				return IOStreamUtil.copyIStoByteArray(gzis);
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] ungzip(final InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("A 'inputStream' must not be null");
		}

		try {
			try (GZIPInputStream gzis = new GZIPInputStream(inputStream)) {
				return IOStreamUtil.copyIStoByteArray(gzis);
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static InputStream ungzipToStream(final byte[] binary) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
		
		return ungzipToStream(new ByteArrayInputStream(binary));		
	}
	
	public static InputStream ungzipToStream(final InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("An 'inputStream' must not be null");
		}
		
		try {
			return new GZIPInputStream(inputStream);			
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static byte[] getZipEntryData(final ZipInputStream zipInputStream) {
		if (zipInputStream == null) {
			throw new IllegalArgumentException("A 'zipInputStream' must not be null");
		}

		try {
			return IOStreamUtil.copyIStoByteArray(zipInputStream);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static boolean isZipFile(final InputStream inputStream) {
		try {
			inputStream.mark(4);
			final byte[] bytes = IOStreamUtil.copyIStoByteArray(inputStream, 4);
			inputStream.reset();
	
			return isZipFile(bytes);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static boolean isZipFile(final byte[] bytes) {
		if (bytes == null || bytes.length < 4) {
			return false;
		}
		
		return ByteBuffer.wrap(bytes).getInt() == ZIP_HEADER;
	}
	
	public static boolean isGZipFile(final InputStream inputStream) {
		try {
			inputStream.mark(2);
			final byte[] bytes = IOStreamUtil.copyIStoByteArray(inputStream, 2);
			inputStream.reset();
	
			return isGZipFile(bytes);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static boolean isGZipFile(final byte[] bytes) {
		if (bytes == null || bytes.length < 2) {
			return false;
		}
		
		return ByteBuffer.wrap(bytes).getShort() == GZIP_HEADER;
	}
	
	public static List<String> listZipEntries(final byte[] binary) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
	
		try {
			final List<String> entries = new ArrayList<>();
			
			try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(binary))) {		
				while(true) {
					final ZipEntry entry = zis.getNextEntry();
					if (entry == null) {
						break;
					}
					
					entries.add(entry.getName());	
					zis.closeEntry();
				}
				return entries;
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	
	public static final int ZIP_HEADER = 0x504b0304;
	public static final short GZIP_HEADER = 0x1f8b;
}
