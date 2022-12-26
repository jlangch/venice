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
package com.github.jlangch.venice.impl.types.custom;

import java.util.Set;

import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBigInteger;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJust;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.util.CollectionUtil;


public class CustomWrappableTypes {

    public CustomWrappableTypes() {
        types = CollectionUtil.toSet(
                    new VncKeyword(VncBoolean.TYPE),
                    new VncKeyword(VncChar.TYPE),
                    new VncKeyword(VncString.TYPE),
                    new VncKeyword(VncLong.TYPE),
                    new VncKeyword(VncInteger.TYPE),
                    new VncKeyword(VncDouble.TYPE),
                    new VncKeyword(VncBigDecimal.TYPE),
                    new VncKeyword(VncBigInteger.TYPE),
                    new VncKeyword(VncByteBuffer.TYPE),
                    new VncKeyword(VncJust.TYPE));
    }

    public boolean isWrappable(final VncKeyword type) {
        return types.contains(type);
    }


    private final Set<VncKeyword> types;
}
