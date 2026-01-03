/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util.ipc.impl.wal;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


/**
 * A simple WAL logger
 *
 * <p>Use only for Write-Ahead-Log logging!
 */
public final class WalLogger {

    private WalLogger(final File logFile) {
        Objects.requireNonNull(logFile);
        this.logFile = logFile;
    }


    public static WalLogger withinDir(final File walDir) {
        Objects.requireNonNull(walDir);
        return new WalLogger(new File(walDir, "wal.log"));
    }

    public static WalLogger asTemporary() throws IOException {
        final File file = Files.createTempFile("wal", ".log").toFile();
        file.deleteOnExit();
        return new WalLogger(file);
    }


    public void info(final File walFile, final String message) {
        Objects.requireNonNull(message);
        log("INFO", walFile, message, null);
    }

    public void warn(final File walFile, final String message) {
        Objects.requireNonNull(message);
        log("WARN", walFile, message, null);
    }

    public void warn(final File walFile, final String message, final Exception ex) {
        Objects.requireNonNull(message);
        log("WARN", walFile, message, ex);
    }

    public void error(final File walFile, final String message) {
        Objects.requireNonNull(message);
        log("ERROR", walFile, message, null);
    }

    public void error(final File walFile, final String message, final Exception ex) {
        Objects.requireNonNull(message);
        log("ERROR", walFile, message, ex);
    }


    private synchronized void log(
            final String level,
            final File walFile,
            final String message,
            final Exception ex
    ) {
        final String logMsg = String.format(
                                "%s|%s|%s|%s%n",
                                LocalDateTime.now().format(dtf),
                                level,
                                walFile.getName(),
                                formatMessage(message, ex));
        try {
            Files.write(
                logFile.toPath(),
                logMsg.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE);
        }
        catch(Exception ignore) { }
    }

    private String formatMessage(
            final String message,
            final Exception ex
    ) {
        return ex == null
                ? message
                : message + "\n" + formatException(ex);
    }

    private String formatException(final Exception ex) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)
        ) {
            ex.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        }
        catch(IOException e) {
            return ex.getMessage();
        }
    }


    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final File logFile;
}