/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.impl.util.io;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.nio.charset.Charset;

import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.StringUtil;


public class CharsetUtil {

    public static Charset charset(final VncVal charsetName) {
        if (charsetName == null || charsetName == Nil) {
            return DEFAULT_CHARSET;
        }
        else {
            return Types.isVncKeyword(charsetName)
                    ? charset(Coerce.toVncKeyword(charsetName).getValue())
                    : charset(Coerce.toVncString(charsetName).getValue());
        }
    }

    public static Charset charset(final String charsetName) {
        return StringUtil.isEmpty(charsetName) ? DEFAULT_CHARSET : Charset.forName(charsetName);
    }

    public static Charset charset(final Charset charset) {
        return charset == null ? DEFAULT_CHARSET : charset;
    }


    public static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
}
