/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class VncWrappingTypeDef extends VncCustomBaseTypeDef {

    public VncWrappingTypeDef(
            final VncKeyword type,
            final VncKeyword baseType
    ) {
        this(type, baseType, null);
    }

    public VncWrappingTypeDef(
            final VncKeyword type,
            final VncKeyword baseType,
            final VncFunction validationFn
    ) {
        super(type);

        this.baseType = baseType;
        this.validationFn = validationFn;
    }


    public VncKeyword getBaseType() {
        return baseType;
    }

    public VncFunction getValidationFn() {
        return validationFn;
    }

    public void validate(final VncVal val) {
        if (validationFn != null) {
            try {
                final VncVal valid = validationFn.apply(VncList.of(val));
                if (valid == Nil || VncBoolean.isFalse(valid)) {
                    throw new AssertionException(String.format(
                            "Invalid value for custom type :%s",
                            getType().getValue()));
                }
            }
            catch(AssertionException ex) {
                throw ex;
            }
            catch(Exception ex) {
                throw new AssertionException(
                        String.format(
                                "Invalid value for custom type :%s",
                                getType().getValue()),
                        ex);
            }
        }
    }

    @Override
    public VncMap toMap() {
        return VncHashMap.of(
                new VncKeyword(":type"),            getType(),
                new VncKeyword(":custom-type"),     new VncKeyword(":wrapping"),
                new VncKeyword(":base-type"),       baseType,
                new VncKeyword(":validation-fn"),   validationFn == null ? Nil : validationFn);
    }


    private static final long serialVersionUID = -1848883965231344442L;

    private final VncKeyword baseType;
    private final VncFunction validationFn;
}
