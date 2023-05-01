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
 * A document
 *
 * @author juerg
 */
public class Document {

    public Document(
        final String name,
        final String externalName,
        final String mimeType,
        final byte[] data
    ) {
        this.name = name;
        this.externalName = externalName;
        this.mimeType = mimeType;
        this.data = data;
    }


    public String getName() {
        return name;
    }

    public String getExternalName() {
        return externalName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public byte[] getData() {
        return data;
    }


    private final String name;
    private final String externalName;
    private final String mimeType;
    private final byte[] data;
}
