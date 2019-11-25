/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice.examples;

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;

// Without precompilation
public class Embed_05_PrecompiledShootout_1 {

    public static void main(final String[] args) {
        final int iterations = 10000;
        final String expr = "(cond (< x 0) -1 (> x 0) 1 :else 0)";

        final Venice venice = new Venice();
        
        // warmup
        for(int ii=0; ii<iterations; ii++) {
            venice.eval("test", expr, Parameters.of("x", (ii%3) - 1));
        }

        final List<Long> raw = new ArrayList<>();
        for(int ii=0; ii<iterations; ii++) {
            final long start = System.nanoTime();
            
            venice.eval("test", expr, Parameters.of("x", (ii%3) - 1));
            
            raw.add(System.nanoTime() - start);
        }
        final List<Long> measures = TimeFormatter.stripOutlier(raw);
        final long elapsed = TimeFormatter.sum(measures);
        
        System.out.println("Elapsed : " + TimeFormatter.formatNanos(elapsed));
        System.out.println("Per call: " + TimeFormatter.formatNanos(elapsed / measures.size()));
    }
}