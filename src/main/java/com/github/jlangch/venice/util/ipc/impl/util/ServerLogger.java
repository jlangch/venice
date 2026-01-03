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
package com.github.jlangch.venice.util.ipc.impl.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * A simple server logger
 *
 * <p>Use only for TcpServer logging!
 */
public final class ServerLogger {

    public ServerLogger() {
        logFile = null;
    }



    public void enable(final File dir) {
        logFile = dir == null ? null : new File(dir, "server.log");
    }

    public boolean isEnabled() {
        return logFile != null;
    }

    public File getLogFile() {
        return logFile;
    }


    public void info(final String context, final String message) {
        Objects.requireNonNull(message);
        log("INFO", context, message, null);
    }

    public void warn(final String context, final String message) {
        Objects.requireNonNull(message);
        log("WARN", context, message, null);
    }

    public void warn(final String context, final String message, final Exception ex) {
        Objects.requireNonNull(message);
        log("WARN", context, message, ex);
    }

    public void error(final String context, final String message) {
        Objects.requireNonNull(message);
        log("ERROR", context, message, null);
    }

    public void error(final String context, final String message, final Exception ex) {
        Objects.requireNonNull(message);
        log("ERROR", context, message, ex);
    }


    private synchronized void log(
            final String level,
            final String context,
            final String message,
            final Exception ex
    ) {
        if (logFile == null) {
            return;
        }

        final String msg = ex == null ? message : message + ". Cause: " + ex.getMessage();

        final String logMsg = String.format(
                                "%s|%s|%s|%s%n",
                                LocalDateTime.now().format(dtf),
                                level,
                                StringUtil.trimToEmpty(context),
                                msg);
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


    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private volatile File logFile = null;
}