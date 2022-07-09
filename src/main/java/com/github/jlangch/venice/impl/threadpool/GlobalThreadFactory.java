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
package com.github.jlangch.venice.impl.threadpool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

import com.github.jlangch.venice.impl.threadpool.ThreadPoolUtil.CountedThreadFactory;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * A global factory that creates non-pooled systems threads. It uses
 * the <code>CountedThreadFactory</code>
 *
 * @author juerg
 * @see CountedThreadFactory
 */
public class GlobalThreadFactory {

    public static Thread newThread(final Runnable runnable) {
        return newThread(null, runnable);
    }

    public static Thread newThread(final String name, final Runnable runnable) {
        final ThreadFactory tf = threadFactories.computeIfAbsent(
                                    makeName(name),
                                    n -> new CountedThreadFactory(n, true));

        return tf.newThread(runnable);
    }

    private static String makeName(final String name) {
        final String n = StringUtil.trimToNull(name);
        return n == null ? defaultName : n;
    }


    private static String defaultName = "venice-thread";

    private static Map<String,CountedThreadFactory> threadFactories =
            new ConcurrentHashMap<>();
}
