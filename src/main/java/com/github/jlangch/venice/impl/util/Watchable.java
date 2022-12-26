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
package com.github.jlangch.venice.impl.util;

import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class Watchable {

    public void addWatch(final VncKeyword name, final VncFunction fn) {
        watches.put(name, fn);
    }

    public void removeWatch(final VncKeyword name) {
        watches.remove(name);
    }

    public void notifyWatches(final VncVal ref, final VncVal oldVal, final VncVal newVal) {
        watches.entrySet().forEach(e -> {
            try {
                e.getValue().apply(VncList.of(e.getKey(), ref, oldVal, newVal));
            }
            catch(VncException ex) {
                throw ex;
            }
            catch(RuntimeException ex) {
                throw new VncException("Watcher failure!", ex);
            }
        });
    }

    private final ConcurrentHashMap<VncKeyword,VncFunction> watches = new ConcurrentHashMap<>();
}
