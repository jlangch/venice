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
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;


/**
 * A helper to compress/uncompress binary data blocks using the zip
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

    public static void zipAppend(final File zipFile, final Map<String, Object> entries) {
        if (zipFile == null) {
            throw new IllegalArgumentException("A 'zipFile' must not be null");
        }
        if (entries == null ) {
            throw new IllegalArgumentException("An 'entries' map must not be null");
        }

        try {
            final Path zipPath = zipFile.toPath();

            try (FileSystem fs = FileSystems.newFileSystem(zipPath, (ClassLoader)null)) {
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

    public static void zipRemove(final File zipFile, final List<String> entryNames) {
        if (zipFile == null) {
            throw new IllegalArgumentException("A 'zipFile' must not be null");
        }

        if (entryNames == null || entryNames.isEmpty()) {
            return;
        }

        try {
            final Path zipPath = zipFile.toPath();

            try (FileSystem fs = FileSystems.newFileSystem(zipPath, (ClassLoader)null)) {
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

    public static byte[] unzip(final File zipFile, final String entryName) {
        if (zipFile == null) {
            throw new IllegalArgumentException("A 'zipFile' must not be null");
        }
        if (StringUtil.isEmpty(entryName)) {
            throw new IllegalArgumentException("A 'entryName' must not be null or empty");
        }

        try (FileInputStream is = new FileInputStream(zipFile)) {
            return unzip(is, entryName);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static byte[] unzip(final byte[] zipFileBinary, final String entryName) {
        if (zipFileBinary == null) {
            throw new IllegalArgumentException("A 'zipFileBinary' must not be null");
        }
        if (StringUtil.isEmpty(entryName)) {
            throw new IllegalArgumentException("A 'entryName' must not be null or empty");
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(zipFileBinary)) {
            return unzip(is, entryName);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static byte[] unzip(final InputStream zipFileIS, final String entryName) {
        if (zipFileIS == null) {
            throw new IllegalArgumentException("A 'zipFileIS' must not be null");
        }

        final String name = normalizeAndValidateEntryName(entryName);

        try (ZipInputStream zis = zipFileIS instanceof ZipInputStream
                                    ? (ZipInputStream)zipFileIS
                                    : new ZipInputStream(zipFileIS)
        ) {
            while(true) {
                final ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                validatedEntryNameForUnzip(entry);

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

    public static byte[] unzipNthEntry(final File zipFile, final int nth) {
        if (zipFile == null) {
            throw new IllegalArgumentException("A 'zipFile' must not be null");
        }

        try (FileInputStream is = new FileInputStream(zipFile)) {
            return unzipNthEntry(is, nth);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static byte[] unzipNthEntry(final byte[] zipFileBinary, final int nth) {
        if (zipFileBinary == null) {
            throw new IllegalArgumentException("A 'zipFileBinary' must not be null");
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(zipFileBinary)) {
            return unzipNthEntry(is, nth);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static byte[] unzipNthEntry(final InputStream zipFileIS, final int nth) {
        if (zipFileIS == null) {
            throw new IllegalArgumentException("A 'zipFileIS' must not be null");
        }

        try (ZipInputStream zis = zipFileIS instanceof ZipInputStream
                                    ? (ZipInputStream)zipFileIS
                                    : new ZipInputStream(zipFileIS)
        ) {
            int entryIdx = 0;

            while(true) {
                final ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                final byte[] data = slurpBytes(zis);

                if (entryIdx == nth) {
                    validatedEntryNameForUnzip(entry);

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

    public static InputStream unzipToStream(final byte[] zipFileBinary, final String entryName) {
        if (zipFileBinary == null) {
            throw new IllegalArgumentException("A 'zipFileBinary' must not be null");
        }
        if (StringUtil.isEmpty(entryName)) {
            throw new IllegalArgumentException("A 'entryName' must not be null or empty");
        }

        final String name = normalizeAndValidateEntryName(entryName);

        try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipFileBinary))) {
            while(true) {
                final ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                if (name.equals(entry.getName())) {
                    validatedEntryNameForUnzip(entry);

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

    public static InputStream unzipFirstToStream(final byte[] zipFileBinary) {
        if (zipFileBinary == null) {
            throw new IllegalArgumentException("A 'zipFileBinary' must not be null");
        }

        return unzipFirstToStream(new ByteArrayInputStream(zipFileBinary));
    }

    public static InputStream unzipFirstToStream(final InputStream zipFileIS) {
        if (zipFileIS == null) {
            throw new IllegalArgumentException("An 'zipFileIS' must not be null");
        }

        try {
            final ZipInputStream zis = zipFileIS instanceof ZipInputStream
                                            ? (ZipInputStream)zipFileIS
                                            : new ZipInputStream(zipFileIS);

            // return the first entry that is not a directory
            while (true) {
                final ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                validatedEntryNameForUnzip(entry);

                if (!entry.isDirectory()) {
                    return zis;
                }
            }
            return null;
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static Map<String, byte[]> unzipAll(final File zipFile, final PathMatcher matcher) {
        if (zipFile == null) {
            throw new IllegalArgumentException("A 'zipFile' must not be null");
        }

        try (FileInputStream is = new FileInputStream(zipFile)) {
            return unzipAll(is, matcher);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static Map<String, byte[]> unzipAll(final byte[] zipFileBinary, final PathMatcher matcher) {
        if (zipFileBinary == null) {
            throw new IllegalArgumentException("A 'zipFileBinary' must not be null");
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(zipFileBinary)) {
            return unzipAll(is, matcher);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static Map<String, byte[]> unzipAll(final InputStream zipFileIS, final PathMatcher matcher) {
        if (zipFileIS == null) {
            throw new IllegalArgumentException("A 'zipFileIS' must not be null");
        }

        final Map<String, byte[]> files = new HashMap<String, byte[]>();

        try (ZipInputStream zis = zipFileIS instanceof ZipInputStream
                                    ? (ZipInputStream)zipFileIS
                                    : new ZipInputStream(zipFileIS)
        ) {
            while(true) {
                final ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                validatedEntryNameForUnzip(entry);

                final byte[] data = slurpBytes(zis);

                if (matcher == null) {
                    files.put(
                        entry.getName(),
                        entry.isDirectory() ? null : data);
                }
                else if (!entry.isDirectory()) {
                    if (matcher.matches(new File(entry.getName()).toPath())) {
                        files.put(entry.getName(), data);
                    }
                }

                zis.closeEntry();
            }

            return files;
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static void zipFileOrDir(
            final File zip,
            final List<File> sourceFileOrDirs,
            final FilenameFilter filter,
            final Function<File,InputStream> mapper,
            final PrintStream ps
    ) {
        if (zip == null) {
            throw new IllegalArgumentException("A 'zip' must not be null");
        }
        if (sourceFileOrDirs == null || sourceFileOrDirs.isEmpty()) {
            throw new IllegalArgumentException("A 'sourceFileOrDirs' must not be null or empty");
        }

        try (FileOutputStream fos = new FileOutputStream(zip)) {
            zipFileOrDir(fos, sourceFileOrDirs, filter, mapper, ps);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static void zipFileOrDir(
            final OutputStream os,
            final List<File> sourceFileOrDirs,
            final FilenameFilter filter,
            final Function<File,InputStream> mapper,
            final PrintStream ps
    ) {
        if (os == null) {
            throw new IllegalArgumentException("An 'os' must not be null");
        }
        if (sourceFileOrDirs == null || sourceFileOrDirs.isEmpty()) {
            throw new IllegalArgumentException("A 'sourceFileOrDirs' must not be null or empty");
        }

        try {
            try (ZipOutputStream zipOut = new ZipOutputStream(os)) {
                ps.println("Output:");

                for(File f : sourceFileOrDirs) {
                    if (f.isDirectory()) {
                        zipFile(f, f.getName(), filter, mapper, ps, zipOut);
                    }
                    else if (f.isFile()) {
                        zipFile(f, f.getName(), filter, mapper, ps, zipOut);
                    }
                }
            }
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static List<ZipEntryAttr> listZip(final byte[] binary, final ZipEntryAttrPrinter printer) {
        if (binary == null) {
            throw new IllegalArgumentException("A 'binary' must not be null");
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(binary)) {
            return listZip(is, printer);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static List<ZipEntryAttr> listZip(final File zip, final ZipEntryAttrPrinter printer) {
        if (zip == null) {
            throw new IllegalArgumentException("A 'zip' must not be null");
        }

        try (FileInputStream fis = new FileInputStream(zip)) {
            return listZip(fis, printer);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static List<ZipEntryAttr> listZip(final InputStream is, final ZipEntryAttrPrinter printer) {
        if (is == null) {
            throw new IllegalArgumentException("An 'is' must not be null");
        }

        final List<ZipEntryAttr> entryAttrs = new ArrayList<>();

        try {
            printer.start();

            final ZipInputStream zis = new ZipInputStream(is);

            while(true) {
                final ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                // close the entry first to get the entry's data available
                zis.closeEntry();

                final ZipEntryAttr entryAttr = new ZipEntryAttr(
                                                        entry.getName(),
                                                        entry.isDirectory(),
                                                        entry.getMethod() == 0 ? "Stored" : "Defl:N",
                                                        entry.isDirectory() ? 0 : entry.getSize(),
                                                        entry.isDirectory() ? 0 : entry.getCompressedSize(),
                                                        entry.getLastModifiedTime(),
                                                        entry.getCrc());

                entryAttrs.add(entryAttr);

                printer.print(entryAttr);
            }

            zis.close();

            printer.end();

            return entryAttrs;
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
            	final File f = zipValidatedExtractDestFile(destDir, zipEntry);

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
            final Function<File,InputStream> mapper,
            final PrintStream ps,
            final ZipOutputStream zipOut
    ) throws IOException {
        final Path path = fileToZip.toPath();
        if (Files.isHidden(path) || Files.isSymbolicLink(path)) {
            return;
        }
        else if (fileToZip.isDirectory()) {
            final String name = fileName.endsWith("/")
                                    ? fileName
                                    : fileName + "/";

            ps.println("  adding: " + name);

            final ZipEntry e = new ZipEntry(name);
            e.setMethod(ZipEntry.STORED);
            e.setSize(0);
            e.setCrc(0);
            zipOut.putNextEntry(e);
            zipOut.closeEntry();

            final File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, name + childFile.getName(), filter, mapper, ps, zipOut);
            }
        }
        else if (fileToZip.isFile()) {
            if (filter == null || filter.accept(fileToZip.getParentFile(), fileToZip.getName())) {
                InputStream is = mapper == null ? null : mapper.apply(fileToZip);

                ps.println("  adding: " + fileName + (is == null ? "" : "  (mapped)"));

                is = is == null ? new FileInputStream(fileToZip) : is;

                try (InputStream is_ = is) {
                    final ZipEntry zipEntry = new ZipEntry(fileName);
                    zipEntry.setMethod(ZipEntry.DEFLATED);
                    zipOut.putNextEntry(zipEntry);
                    IOStreamUtil.copy(is_, zipOut);
                    zipOut.closeEntry();
                }
            }
        }
    }

    private static File zipValidatedExtractDestFile(
    		final File destDir,
    		final ZipEntry zipEntry
    ) throws IOException {
        // A zip entry name my contain malicious  "../" elements resulting the
        // entry file to be written outside of 'destDirPath'!

    	final Path destDirPath = destDir.getCanonicalFile().toPath();
        final Path destFilePath = new File(destDir, zipEntry.getName()).getCanonicalFile().toPath();
        if (!destFilePath.startsWith(destDirPath)) {
            throw new IOException(
                    String.format(
                        "ZIP entry '%s' slips target dir %s while unzipping it!",
                        zipEntry.getName(),
                        destDir.getPath()));
        }
        else {
            return destFilePath.toFile();
        }
    }

    private static void validatedEntryNameForUnzip(
    		final ZipEntry zipEntry
    ) throws IOException {
        // A zip entry name my contain malicious  "../" elements resulting the
        // entry file to be written outside of an extraction path!

    	final File destDir = new File(".");  // hypothetical unzip dest dir
        final Path destDirPath = destDir.getCanonicalFile().toPath();
        final Path destFilePath = new File(destDir, zipEntry.getName()).getCanonicalFile().toPath();
        if (!destFilePath.startsWith(destDirPath)) {
            throw new IOException(
                    String.format(
                        "ZIP entry '%s' slips a potential target dir!",
                        zipEntry.getName()));
        }
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
                    return IOStreamUtil.copyIStoByteArray(fis);
                }
            }
            else {
                throw new IllegalArgumentException(
                        "Only entry values of type byte[], File or InputStream are supported!");
            }
        }
    }

    private static void deletePath(Path p) {
        try {
            Files.delete(p);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }


    public static final int ZIP_HEADER = 0x504b0304;
}
