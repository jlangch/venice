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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * A helper to compress/uncompress binary data blocks using the zip/gzip
 * inflater/deflater.
 * 
 * <p> Use <pre>unzip -vl a.zip</pre> to list a zip
 */
public class Zipper {

	public static byte[] zip(final byte[] binary, final String entryName) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
		if (StringUtil.isEmpty(entryName)) {
			throw new IllegalArgumentException("A 'entryName' must not be null or empty");
		}

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				if ("/".equals(entryName)) {
					throw new IllegalArgumentException("A 'entryName' must not be \"/\"");
				}
				final String name = normalizeAndValidateEntryName(entryName);
				if (name.endsWith("/")) {
					// directory
					final ZipEntry e = new ZipEntry(name);
					e.setMethod(ZipEntry.STORED);
					e.setSize(0);
					e.setCrc(0);
					
					zos.putNextEntry(e);
					zos.closeEntry();
				}
				else {
					// file
					final ZipEntry e = new ZipEntry(name);
					e.setMethod(ZipEntry.DEFLATED);
					
					zos.putNextEntry(e);
					zos.write(binary);
					zos.closeEntry();
				}
				
				zos.finish();
				zos.flush();
			}
	
			baos.flush();
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
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				for (Map.Entry<String,Object> entry : entries.entrySet()) {					
					final String entryName = normalizeAndValidateEntryName(entry.getKey());

					if (entryName.endsWith("/")) {
						// directory
						final ZipEntry e = new ZipEntry(entryName);
						e.setMethod(ZipEntry.STORED);
						e.setSize(0);
						e.setCrc(0);
						
						zos.putNextEntry(e);
						zos.closeEntry();
					}
					else {
						// file
						if (entry.getValue() != null) {
							final byte[] entryBytes = slurpBytes(entry.getValue());
	 							
							final ZipEntry e = new ZipEntry(entryName);
							e.setMethod(ZipEntry.DEFLATED);
							
							zos.putNextEntry(e);
							zos.write(entryBytes);
							zos.closeEntry();
						}
					}
				}
				
				zos.finish();
				zos.flush();
			}
			
			baos.flush();
			return baos.toByteArray();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static void zipAppend(final File zip, final Map<String, Object> entries) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}
		if (entries == null ) {
			throw new IllegalArgumentException("An 'entries' map must not be null");
		}

		try {
			final Path zipFile = Paths.get(zip.getPath());

			try (FileSystem fs = FileSystems.newFileSystem(zipFile, null)) {		
				for (Map.Entry<String,Object> entry : entries.entrySet()) {
					final String entryName = normalizeAndValidateEntryName(entry.getKey());
					if (entry.getValue() != null) {
						final byte[] entryBytes = slurpBytes(entry.getValue());
	
						if (entryName.endsWith("/")) {
							// directory
							final Path nf = fs.getPath(entryName);
							if (Files.notExists(nf)) { 
								Files.createDirectories(nf);
							}
						}
						else {
							// file
							final Path nf = fs.getPath(entryName);
							
							// create missing directories
							final Path dir = nf.getParent();
							if (dir != null) {
								if (Files.notExists(dir)) { 
									Files.createDirectories(dir);
								}
							}
							
							// allow overwrite
							Files.deleteIfExists(nf);
							
							try (OutputStream os = Files.newOutputStream(nf)) {
								os.write(entryBytes);
								os.flush();
							}
						}
					}
				}
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void zipRemove(final File zip, final List<String> entryNames) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}
		
		if (entryNames == null || entryNames.isEmpty()) {
			return;
		}

		try {
			final Path zipFile = Paths.get(zip.getPath());

			try (FileSystem fs = FileSystems.newFileSystem(zipFile, null)) {		
				for (String entryName : entryNames) {
					final Path nf = fs.getPath(entryName);

					if (entryName.endsWith("/")) {
						// directory
						if (Files.isDirectory(nf)) { 
							try {
								final List<Path> tree = Files.walk(nf).collect(Collectors.toList());
								Collections.reverse(tree);
								tree.forEach(p -> deletePath(p));
							}
							catch(IOException ex) {
								throw new RuntimeException(ex.getMessage(), ex);
							}
						}
					}
					else {
						// file
						if (Files.isRegularFile(nf)) { 
							Files.deleteIfExists(nf);
						}
					}
				}
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static byte[] unzip(final File zip, final String entryName) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}
		if (StringUtil.isEmpty(entryName)) {
			throw new IllegalArgumentException("A 'entryName' must not be null or empty");
		}

		try (FileInputStream is = new FileInputStream(zip)) {
			return unzip(is, entryName); 
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

		try (ByteArrayInputStream is = new ByteArrayInputStream(binary)) {
			return unzip(is, entryName); 
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static byte[] unzip(final InputStream is, final String entryName) {
		if (is == null) {
			throw new IllegalArgumentException("A 'is' must not be null");
		}

		final String name = normalizeAndValidateEntryName(entryName);

		try (ZipInputStream zis = new ZipInputStream(is)) {
			while(true) {
				final ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}
				
				final byte[] data = slurpBytes(zis);

				zis.closeEntry();

				if (name.equals(entry.getName())) {
					return data;
				}					
			}
			
			return null; // ZIP entry not found 
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] unzipNthEntry(final File zip, final int nth) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}

		try (FileInputStream is = new FileInputStream(zip)) {
			return unzipNthEntry(is, nth);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] unzipNthEntry(final byte[] binary, final int nth) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}

		try (ByteArrayInputStream is = new ByteArrayInputStream(binary)) {
			return unzipNthEntry(is, nth);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] unzipNthEntry(final InputStream is, final int nth) {
		if (is == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}

		try (ZipInputStream zis = new ZipInputStream(is)) {
			int entryIdx = 0;
			
			while(true) {
				final ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}
				
				final byte[] data = slurpBytes(zis);
				
				if (entryIdx == nth) {
					return data;
				}
				
				zis.closeEntry();
				entryIdx++;
			}
			
			return null; // ZIP entry not found 
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

		final String name = normalizeAndValidateEntryName(entryName);
		
		try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(binary))) {			
			while(true) {
				final ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}						
				if (name.equals(entry.getName())) {
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

	public static Map<String, byte[]> unzipAll(final File zip) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}
		
		try (FileInputStream is = new FileInputStream(zip)) {			
			return unzipAll(is);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static Map<String, byte[]> unzipAll(final byte[] binary) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
		
		try (ByteArrayInputStream is = new ByteArrayInputStream(binary)) {			
			return unzipAll(is);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static Map<String, byte[]> unzipAll(final InputStream is) {
		if (is == null) {
			throw new IllegalArgumentException("A 'is' must not be null");
		}
		
		final Map<String, byte[]> files = new HashMap<String, byte[]>();
		
		try (ZipInputStream zis = new ZipInputStream(is)) {
			while(true) {
				final ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}
				
				final byte[] data = slurpBytes(zis);
				
				files.put(
					entry.getName(), 
					entry.isDirectory() ? null : data);
				
				zis.closeEntry();
			}
			
			return files;
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] gzip(final byte[] binary) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
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

	public static void zipFileOrDir(
			final File zip,
			final List<File> sourceFileOrDirs, 
			final FilenameFilter filter
	) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}
		if (sourceFileOrDirs == null || sourceFileOrDirs.isEmpty()) {
			throw new IllegalArgumentException("A 'sourceFileOrDirs' must not be null or empty");
		}

		try (FileOutputStream fos = new FileOutputStream(zip)) {
			zipFileOrDir(fos, sourceFileOrDirs, filter);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void zipFileOrDir(
			final OutputStream os, 
			final List<File> sourceFileOrDirs, 
			final FilenameFilter filter
	) {
		if (os == null) {
			throw new IllegalArgumentException("An 'os' must not be null");
		}
		if (sourceFileOrDirs == null || sourceFileOrDirs.isEmpty()) {
			throw new IllegalArgumentException("A 'sourceFileOrDirs' must not be null or empty");
		}

		try {
			try (ZipOutputStream zipOut = new ZipOutputStream(os)) {
				for(File f : sourceFileOrDirs) {
					if (f.isDirectory()) {
						zipFile(f, f.getName(), filter, zipOut);
					}
					else if (f.isFile()) {
						zipFile(f, f.getName(), filter, zipOut);
					}
				}
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void listZip(final byte[] binary, final PrintStream ps, final boolean verbose) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}

		try (ByteArrayInputStream is = new ByteArrayInputStream(binary)) {
			listZip(is, ps, verbose);
		} 
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void listZip(final File zip, final PrintStream ps, final boolean verbose) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}

		try (FileInputStream fis = new FileInputStream(zip)) {
			listZip(fis, ps, verbose);
		} 
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void listZip(final InputStream is, final PrintStream ps, final boolean verbose) {
		if (is == null) {
			throw new IllegalArgumentException("An 'is' must not be null");
		}

		try {
			printZipListLineHead(ps, verbose);
			printZipListLineDelim(ps, verbose);

			long totCount = 0L;
			long totSize = 0L;
			long totCompressedSize = 0L;
			
			final ZipInputStream zis = new ZipInputStream(is);
			
			while(true) {
				final ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}

				// close the entry first to get the entry's data available
				zis.closeEntry();

				final long size = entry.isDirectory() ? 0 : entry.getSize();
				final long compressedSize = entry.isDirectory() ? 0 : entry.getCompressedSize();
				
				totCount++;
				totSize += Math.max(0, size);
				totCompressedSize += Math.max(0, compressedSize);
	
		    	printZipListLine(
		    			ps, verbose, size, entry.getMethod(), compressedSize, 
		    			entry.getLastModifiedTime(), entry.getCrc(), entry.getName());
			}
			zis.close();

			printZipListLineDelim(ps, verbose);
	    	
			printZipListLine(
	    			ps, verbose, 
	    			totSize, null, totCompressedSize,
	    			null, null, 
	    			totCount == 1 ? "1 file" : String.format("%d files", totCount));
		} 
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void unzipToDir(final File zip, final File destDir) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}
		if (destDir == null) {
			throw new IllegalArgumentException("A 'dir' must not be null");
		}

		try (FileInputStream fis = new FileInputStream(zip)) {
			unzipToDir(fis, destDir);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static void unzipToDir(byte[] zipBinary, final File destDir) {
		if (zipBinary == null) {
			throw new IllegalArgumentException("A 'zipBinary' must not be null");
		}
		if (destDir == null) {
			throw new IllegalArgumentException("A 'dir' must not be null");
		}

		try (InputStream is = new ByteArrayInputStream(zipBinary)) {
			unzipToDir(is, destDir);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static void unzipToDir(final InputStream zipIS, final File destDir) {
		if (zipIS == null) {
			throw new IllegalArgumentException("A 'zipIS' must not be null");
		}
		if (destDir == null) {
			throw new IllegalArgumentException("A 'dir' must not be null");
		}

		try {
			final ZipInputStream zis = new ZipInputStream(zipIS);
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				final File f = newFile(destDir, zipEntry);
				if (zipEntry.isDirectory()) {
					f.mkdirs();
				}
				else {
					f.getParentFile().mkdirs();
					try (FileOutputStream fos = new FileOutputStream(f)) {
						IOStreamUtil.copy(zis, fos);
					}
				}
				
				zipEntry = zis.getNextEntry();
			}
				
			zis.closeEntry();
			zis.close();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] gzip(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("A 'file' must not be null");
		}

		try (InputStream is = new FileInputStream(file)) {
			return gzip(is);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] gzip(final InputStream is) {
		if (is == null) {
			throw new IllegalArgumentException("An 'is' must not be null");
		}

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {	
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

		try (GZIPOutputStream gzos = new GZIPOutputStream(os)) {
			gzos.write(binary, 0, binary.length);
			gzos.flush();
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

		try (GZIPOutputStream gzos = new GZIPOutputStream(os)) {
			IOStreamUtil.copy(is, gzos);
			gzos.flush();
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

		try (GZIPInputStream gzis = new GZIPInputStream(bais)) {
			return slurpBytes(gzis);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] ungzip(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("A 'file' must not be null");
		}

		try (FileInputStream is = new FileInputStream(file)) {
			return ungzip(is);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] ungzip(final InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("A 'inputStream' must not be null");
		}

		try (GZIPInputStream gzis = new GZIPInputStream(inputStream)) {
			return slurpBytes(gzis);
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
			return slurpBytes(zipInputStream);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static boolean isZipFile(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("A 'file' must not be null");
		}

		try (FileInputStream is = new FileInputStream(file)) {
			return isZipFile(is);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static boolean isZipFile(final InputStream is) {
		if (is == null) {
			throw new IllegalArgumentException("An 'is' must not be null");
		}

		try {
			is.mark(4);
			final byte[] bytes = IOStreamUtil.copyIStoByteArray(is, 4);
			is.reset();
	
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
	
	public static boolean isGZipFile(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("A 'file' must not be null");
		}

		try (FileInputStream is = new FileInputStream(file)) {
			return isGZipFile(is);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static boolean isGZipFile(final InputStream is) {
		if (is == null) {
			throw new IllegalArgumentException("An 'is' must not be null");
		}

		try {
			is.mark(2);
			final byte[] bytes = IOStreamUtil.copyIStoByteArray(is, 2);
			is.reset();
	
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

	public static List<String> listZipEntryNames(final File zip) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}
	
		try(InputStream is = new FileInputStream(zip)) {		
			return listZipEntryNames(is);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static List<String> listZipEntryNames(final byte[] binary) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
	
		try(InputStream is = new ByteArrayInputStream(binary)) {		
			return listZipEntryNames(is);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static List<String> listZipEntryNames(final InputStream is) {
		if (is == null) {
			throw new IllegalArgumentException("A 'is' must not be null");
		}
	
		final List<String> entries = new ArrayList<>();
		
		try(ZipInputStream zis = new ZipInputStream(is)) {		
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
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static void zipFile(
			final File fileToZip, 
			final String fileName, 
			final FilenameFilter filter, 
			final ZipOutputStream zipOut
	) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				final ZipEntry e = new ZipEntry(fileName);
				e.setMethod(ZipEntry.STORED);
				e.setSize(0);
				e.setCrc(0);
				zipOut.putNextEntry(e);
				zipOut.closeEntry();
			} 
			else {
				final ZipEntry e = new ZipEntry(fileName + "/");
				e.setMethod(ZipEntry.STORED);
				e.setSize(0);
				e.setCrc(0);
				zipOut.putNextEntry(e);
				zipOut.closeEntry();
			}
			
			final File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), filter, zipOut);
			}
		}
		else if (fileToZip.isFile()) {
			if (filter == null || filter.accept(fileToZip.getParentFile(), fileToZip.getName())) {
				try (FileInputStream fis = new FileInputStream(fileToZip)) {
					final ZipEntry zipEntry = new ZipEntry(fileName);
					zipEntry.setMethod(ZipEntry.DEFLATED);
					zipOut.putNextEntry(zipEntry);		
					IOStreamUtil.copy(new FileInputStream(fileToZip), zipOut);
					zipOut.closeEntry();
				}
			}
		}
    }
	
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    	final File destFile = new File(destinationDir, zipEntry.getName());
         
    	final String destDirPath = destinationDir.getCanonicalPath();
    	final String destFilePath = destFile.getCanonicalPath();
         
    	if (!destFilePath.startsWith(destDirPath + File.separator)) {
    		throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    	}
    	
    	return destFile;
    }
    
    private static String normalizeAndValidateEntryName(final String entryName) {
		if (StringUtil.isEmpty(entryName)) {
			throw new IllegalArgumentException("A 'entryName' must not be null or empty");
		}
		if ("/".equals(entryName)) {
			throw new IllegalArgumentException("A 'entryName' must not be \"/\"");
		}
		
		return entryName.startsWith("/") ? entryName.substring(1) : entryName;
    }
    
    private static byte[] slurpBytes(final Object source) throws IOException {
		if (source == null) {
			return new byte[0];
		}
		else {
			if (source instanceof byte[]) {
				return (byte[])source;
			}
			else if (source instanceof InputStream) {
				return IOStreamUtil.copyIStoByteArray((InputStream)source);
			}
			else if (source instanceof File) {
				try (FileInputStream fis = new FileInputStream((File)source)) {
					return IOStreamUtil.copyIStoByteArray((InputStream)fis);
				}
			}
			else {
				throw new IllegalArgumentException(
						"Only entry values of type byte[], File or InputStream are supported!");
			}
		}
    }
 
    private static void printZipListLine(
    		final PrintStream ps, 
    		final boolean verbose,
    		final long size, 
    		final Integer method,
    		final long compressedSize,
    		final FileTime time,
    		final Long crc,
    		final String name
    ) {
		final String sCompression = String.valueOf(compressionPercentage(size, compressedSize)) + "%";

		final String sMethod = method == null ? "" : (method == 0 ? "Stored" : "Defl:N");

		final String sTime = time == null
								? "-"
								: LocalDateTime
									.ofInstant(time.toInstant(), ZoneOffset.UTC)
									.format(ziplist_formatter);

		final String sCrc = crc == null ? "" : (crc == -1 ? "-" : String.format("%08X", crc & 0xFFFFFFFF));

    	printZipListLine(
    			ps, verbose, String.valueOf(size), sMethod, 
    			String.valueOf(compressedSize), 
    			sCompression, sTime, sCrc, name);
    }

    private static void printZipListLine(
    		final PrintStream ps, 
    		final boolean verbose,
    		final String length, 
    		final String method,
    		final String size,
    		final String compression,
    		final String time,
    		final String crc,
    		final String name
    ) {
    	if (verbose) {
        	ps.println(String.format(ziplist_format, length, method, size, compression, time, crc, name));
    	}
    	else {
        	ps.println(String.format(ziplist_format_short, length, time, name));
    	}
    }

    private static void printZipListLineHead(final PrintStream ps, final boolean verbose) {
    	printZipListLine(
    			ps, verbose, 
    			"Length", "Method", "Size", "Cmpr", "Date/Time", "CRC-32", "Name");
    }

    private static void printZipListLineDelim(final PrintStream ps, final boolean verbose) {
    	printZipListLine(
    			ps, verbose, 
    			"----------", "------", "----------", "----", "----------------", "--------", "----");
    }
    
    private static long compressionPercentage(final long size, final long compressedSize) {
    	return (size <= 0 || compressedSize <= 0)
    				? 0L
    				: ((size - compressedSize) * 100L + (size / 2L)) / size;
    }
    
    private static void deletePath(Path p) {
		try {
			Files.delete(p);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
    }

    
    private static final String ziplist_format = "%10s  %6s  %10s  %4s  %16s  %8s  %s";
    private static final String ziplist_format_short = "%10s  %16s %s";
	private static final DateTimeFormatter ziplist_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  
	public static final int ZIP_HEADER = 0x504b0304;
	public static final short GZIP_HEADER = 0x1f8b;
}
