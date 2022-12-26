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

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncVal;


/**
 * A Venice exception to throw any value
 */
public class ValueException extends VncException {

    public ValueException(final Object value) {
        super("");
        this.value = value;
        this.type = type(value);
    }

    public ValueException(final Object value, final Throwable cause) {
        super("", cause);
        this.value = value;
        this.type = type(value);
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        return value == null
                ? s + ": null"
                : s + ": (" + type + ") " + format(value);
    }

    private static String format(final Object value) {
        if (value == null) {
            return null;
        }
        else if (value instanceof VncVal) {
            return ((VncVal)value).toString(true);
        }
        else {
            return value.toString();
        }
    }

    private static String type(final Object value) {
        if (value == null) {
            return Constants.Nil.getType().toString();
        }
        else if (value instanceof VncVal) {
            return ((VncVal)value).getType().toString();
        }
        else {
            return ":" + value.getClass().getName();
        }
    }


    private static final long serialVersionUID = -7070216020647646364L;

    private final Object value;
    private final String type;
}
