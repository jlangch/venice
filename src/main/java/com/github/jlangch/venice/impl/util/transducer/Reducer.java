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
package com.github.jlangch.venice.impl.util.transducer;

import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.VncFunction.applyWithMeter;

import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncQueue;
import com.github.jlangch.venice.impl.util.MeterRegistry;


public class Reducer {

    public static VncVal reduce(
            final IVncFunction reduceFn,
            final VncVal init,
            final Iterable<VncVal> coll,
            final MeterRegistry meterRegistry
    ) {
        VncVal value = init;

        for(VncVal v : coll) {
            value = meterRegistry.enabled
                     ? applyWithMeter(reduceFn, VncList.of(value, v), meterRegistry)
                     : reduceFn.apply(VncList.of(value, v));

            if (Reduced.isReduced(value)) {
                return Reduced.unreduced(value);
            }
        }

        return value;
    }

    public static VncVal reduce(
            final IVncFunction reduceFn,
            final VncVal init,
            final VncQueue queue,
            final MeterRegistry meterRegistry
    ) {
        VncVal value = init;

        while(true) {
            final VncVal v = queue.take();
            if (v == Nil) break;  // queue has been closed

            value = meterRegistry.enabled
                     ? applyWithMeter(reduceFn, VncList.of(value, v), meterRegistry)
                     : reduceFn.apply(VncList.of(value, v));

            if (Reduced.isReduced(value)) {
                return Reduced.unreduced(value);
            }
        }

        return value;
    }

}
