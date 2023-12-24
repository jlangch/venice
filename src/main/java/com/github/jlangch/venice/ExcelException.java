/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice;


/**
 * Thrown to indicate an error with Excel file processing
 *
 * @author juerg
 */
public class ExcelException extends VncException {

    public ExcelException(final String message) {
        super(message);
    }

    public ExcelException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ExcelException(final Throwable cause) {
        super(cause);
    }


    private static final long serialVersionUID = 1287052357875193031L;
}
