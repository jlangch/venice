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
package com.github.jlangch.venice.javainterop;

import java.lang.reflect.Type;

public class ReturnValue {

    public ReturnValue(final Object value) {
        this.value = value;
        this.formalType = null;
        this.genericType = null;
    }

    public ReturnValue(final Object value, final Class<?> formalType) {
        this.value = value;
        this.formalType = formalType;
        this.genericType = null;
    }

    public ReturnValue(final Object value, final Class<?> formalType, final Type genericType) {
        this.value = value;
        this.formalType = formalType;
        this.genericType = genericType;
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getFormalType() {
        return formalType;
    }

    public Type getGenericType() {
        return genericType;
    }


    private final Object value;
    private final Class<?> formalType;
    private final Type genericType;
}
