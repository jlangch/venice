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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class InetFunctionsTest {

    @Test
    public void test_IP_type() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(inet/ip4? \"100.0.0.0\")"));
        assertTrue((Boolean)venice.eval("(inet/ip4? (inet/inet-addr \"100.0.0.0\"))"));
        assertFalse((Boolean)venice.eval("(inet/ip6? \"100.0.0.0\")"));
        assertFalse((Boolean)venice.eval("(inet/ip6? (inet/inet-addr \"100.0.0.0\"))"));

        assertFalse((Boolean)venice.eval("(inet/ip4? \"2001:db8:85a3:8d3:ffff:ffff:ffff:ffff\")"));
        assertFalse((Boolean)venice.eval("(inet/ip4? (inet/inet-addr \"2001:db8:85a3:8d3:ffff:ffff:ffff:ffff\"))"));
        assertTrue((Boolean)venice.eval("(inet/ip6? \"2001:db8:85a3:8d3:ffff:ffff:ffff:ffff\")"));
        assertTrue((Boolean)venice.eval("(inet/ip6? (inet/inet-addr \"2001:db8:85a3:8d3:ffff:ffff:ffff:ffff\"))"));
    }

}
