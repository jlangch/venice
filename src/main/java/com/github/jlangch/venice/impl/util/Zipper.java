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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
					else if (value instanceof File) {
						try (FileInputStream fis = new FileInputStream((File)value)) {
							bytes = IOStreamUtil.copyIStoByteArray((InputStream)value);
						}
					}
					else {
						throw new IllegalArgumentException(
								"Only values of type byte[], File or InputStream are supported!");
					}
					
					final ZipEntry e = new ZipEntry(entry.getKey());
					e.setMethod(ZipEntry.DEFLATED);
					zos.putNextEntry(e);
					zos.write(bytes, 0, bytes.length);
					zos.closeEntry();
				}
			}
			
			zos.flush();
			
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
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static byte[] unzipNthEntry(final byte[] binary, final int nth) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}

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
			final File sourceFileOrDir, 
			final FilenameFilter filter,
			final File destZip
	) {
		if (sourceFileOrDir == null) {
			throw new IllegalArgumentException("A 'sourceFileOrDir' must not be null");
		}
		if (destZip == null) {
			throw new IllegalArgumentException("A 'destZip' must not be null");
		}

		try (FileOutputStream fos = new FileOutputStream(destZip)) {
			zipFileOrDir(sourceFileOrDir, filter, fos);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void zipFileOrDir(
			final File sourceFileOrDir, 
			final FilenameFilter filter, 
			final OutputStream os
	) {
		if (sourceFileOrDir == null) {
			throw new IllegalArgumentException("A 'sourceFileOrDir' must not be null");
		}
		if (os == null) {
			throw new IllegalArgumentException("An 'os' must not be null");
		}

		try (ZipOutputStream zipOut = new ZipOutputStream(os)) {
			zipFile(sourceFileOrDir, sourceFileOrDir.getName(), filter, zipOut);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void listZip(final File zip, final PrintStream ps) {
		if (zip == null) {
			throw new IllegalArgumentException("A 'zip' must not be null");
		}

		try (FileInputStream fis = new FileInputStream(zip)) {
			listZip(fis, ps);
		} 
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void listZip(final InputStream is, final PrintStream ps) {
		if (is == null) {
			throw new IllegalArgumentException("An 'is' must not be null");
		}

		final String format1 = "%10s  %6s  %10s  %3s%%  %16s  %8s  %s";
		final String format2 = "%10s          %10s  %3s%%                              %d files";


		try {
			ps.println("    Length  Method       Size   Cmpr         Date/Time    CRC-32  Name");
			ps.println("----------  ------  ----------  ----  ----------------  --------  ----");

			long totCount = 0L;
			long totLength = 0L;
			long totSize = 0L;
			
			final ZipInputStream zis = new ZipInputStream(is);
			
			while(true) {
				final ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}

				final long length = entry.isDirectory() ? 0 : entry.getSize();
				final long size = entry.isDirectory() ? 0 : entry.getCompressedSize();
				final String crc = entry.getCrc() == -1 ? "-" : String.format("%08X", entry.getCrc() & 0xFFFFFFFF);
				final long compression = size <= 0 || length <= 0 ? 0 : (size * 100L) / length;
				final String method = entry.getMethod() == 0 ? "Stored" : "Defl:N";
				final FileTime ftime = entry.getLastModifiedTime();
				final String time = ftime == null
											? "-"
											: LocalDateTime
												.ofInstant(ftime.toInstant(), ZoneOffset.UTC)
												.format(formatter);
				totCount++;
				totLength +=  Math.max(0, length);
				totSize +=  Math.max(0, size);
										
				ps.println(String.format(format1, length, method, size, compression, time, crc, entry.getName()));

				zis.closeEntry();
			}
			zis.close();

			final long totCompression = totSize == 0 || totLength == 0 ? 0 : (totSize * 100L) / totLength;

			ps.println("----------  ------  ----------  ----  ----------------  --------  ----");
			ps.println(String.format(format2, totLength, totSize, totCompression, totCount));
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
			return IOStreamUtil.copyIStoByteArray(gzis);
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
			return IOStreamUtil.copyIStoByteArray(gzis);
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
	
	public static List<String> listZipEntryNames(final byte[] binary) {
		if (binary == null) {
			throw new IllegalArgumentException("A 'binary' must not be null");
		}
	
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
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} 
			else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
				zipOut.closeEntry();
			}
			
			final File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), filter, zipOut);
			}
			
			return;
		}
		
		try (FileInputStream fis = new FileInputStream(fileToZip)) {
			final ZipEntry zipEntry = new ZipEntry(fileName);
			zipOut.putNextEntry(zipEntry);
		
			IOStreamUtil.copy(new FileInputStream(fileToZip), zipOut);		
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
   
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  
	public static final int ZIP_HEADER = 0x504b0304;
	public static final short GZIP_HEADER = 0x1f8b;
}
