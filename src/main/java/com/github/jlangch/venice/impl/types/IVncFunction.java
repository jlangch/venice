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
package com.github.jlangch.venice.impl.types;

import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.javainterop.IInterceptor;


public interface IVncFunction {

    VncList getArgLists();

    VncVal apply(VncList args);


    default IInterceptor sandboxFunctionCallValidation() {
    	return null;
    }

    default VncVal applyOf(final VncVal... mvs) {
        return apply(VncList.of(mvs));
    }

    default boolean isAnonymous() {
        return true;
    }

    default boolean isMacro() {
        return false;
    }

    default boolean isNative() {
        return false;
    }

}
