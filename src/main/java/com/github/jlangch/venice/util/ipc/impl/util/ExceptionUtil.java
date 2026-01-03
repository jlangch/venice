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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.github.jlangch.venice.VncException;


public class ExceptionUtil {

    public static boolean isBrokenPipeException(final Throwable th) {
        if (th == null) {
            return false;
        }
        else if ((th instanceof IOException) && hasExMsg("Broken pipe", th)) {
            return true;
        }
        else {
            // process down the exception hierarchy
            return isBrokenPipeException(th.getCause());
        }
    }

    public static boolean isStreamClosedException(final Throwable th) {
        if (th == null) {
            return false;
        }
        else if ((th instanceof IOException) && hasExMsg("Stream closed", th)) {
            return true;
        }
        else {
            // process down the exception hierarchy
            return isStreamClosedException(th.getCause());
        }
    }

    public static String printStackTraceToString(final Exception ex) {
        if (ex instanceof VncException && ((VncException)ex).hasCallStack()) {
            // Venice callstack
            return String.join("\n", ((VncException)ex).getCallStackAsStringList());
        }
        else {
            // Java stacktrace
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        }
    }

    private static boolean hasExMsg(final String msg, final Throwable th) {
        if (msg == null || th == null || th.getMessage() == null) {
            return false;
        }

        return msg.equalsIgnoreCase(th.getMessage());
    }

}
