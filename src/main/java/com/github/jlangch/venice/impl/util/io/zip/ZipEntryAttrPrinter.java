/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

import java.io.PrintStream;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.github.jlangch.venice.util.NullOutputStream;


public class ZipEntryAttrPrinter {

    public ZipEntryAttrPrinter(final PrintStream ps, final boolean verbose) {
        this.ps = ps;
        this.verbose = verbose;
    }

    public static ZipEntryAttrPrinter nullPrinter() {
        return new ZipEntryAttrPrinter(
                    new PrintStream(new NullOutputStream(), true),
                    false);
    }

    public void start() {
        totCount = 0L;
        totSize = 0L;
        totCompressedSize = 0L;

        printZipListLineHead(ps, verbose);
        printZipListLineDelim(ps, verbose);
    }

    public void print(final ZipEntryAttr entry) {
        final long size = entry.getSize();
        final long compressedSize = entry.getCompressedSize();

        totCount++;
        totSize += Math.max(0, size);
        totCompressedSize += Math.max(0, compressedSize);

        printZipListLine(
                ps, verbose, size, entry.getMethod(), compressedSize,
                entry.getLastModifiedTime(), entry.getCrc(), entry.getName());

    }

    public void end() {
        printZipListLineDelim(ps, verbose);

        printZipListLine(
                ps, verbose,
                totSize, null, totCompressedSize,
                null, null,
                totCount == 1 ? "1 file" : String.format("%d files", totCount));
    }


    private static void printZipListLine(
            final PrintStream ps,
            final boolean verbose,
            final long size,
            final String method,
            final long compressedSize,
            final FileTime time,
            final Long crc,
            final String name
    ) {
        final String sCompression = String.valueOf(compressionPercentage(size, compressedSize)) + "%";

        final String sTime = time == null
                                ? " "
                                : LocalDateTime
                                    .ofInstant(time.toInstant(), ZoneOffset.UTC)
                                    .format(ziplist_formatter);

        final String sCrc = crc == null ? "" : (crc == -1 ? "-" : String.format("%08X", crc & 0xFFFFFFFF));

        printZipListLine(
                ps, verbose, String.valueOf(size), method,
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
                "----------", "------", "----------", "----", "----------------", "--------", "--------------------");
    }

    private static long compressionPercentage(final long size, final long compressedSize) {
        return (size <= 0 || compressedSize <= 0)
                    ? 0L
                    : ((size - compressedSize) * 100L + (size / 2L)) / size;
    }


    private static final String ziplist_format = "%10s  %6s  %10s  %4s  %16s  %8s  %s";
    private static final String ziplist_format_short = "%10s  %16s %s";
    private static final DateTimeFormatter ziplist_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PrintStream ps;
    private final boolean verbose;

    private long totCount = 0L;
    private long totSize = 0L;
    private long totCompressedSize = 0L;
}
