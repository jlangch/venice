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
package com.github.jlangch.venice;


/**
 * Thrown if a recursion expression is not in tail position.
 */
public class NotInTailPositionException extends ParseError {

    public NotInTailPositionException(final String message) {
        super(message);
    }

    public NotInTailPositionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NotInTailPositionException(final Throwable cause) {
        super(cause);
    }


    private static final long serialVersionUID = -23568367901801596L;
}
