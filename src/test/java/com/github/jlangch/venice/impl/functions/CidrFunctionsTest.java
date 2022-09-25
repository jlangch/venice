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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.cidr.CIDR;


public class CidrFunctionsTest {

    @Test
    public void test_IP4() {
        final Venice venice = new Venice();

        final String script = "(cidr/parse \"222.192.0.0/11\")";

        final CIDR cidr = (CIDR)venice.eval(script);

        assertEquals("222.192.0.0/11", cidr.getNotation());

        assertEquals(11, cidr.getRange());

        assertEquals("222.192.0.0", cidr.getLowHostAddress());

        assertEquals("222.223.255.255", cidr.getHighHostAddress());
    }

    @Test
    public void test_IP6() {
        final Venice venice = new Venice();

        final String script =
                "(cidr/parse \"2001:0db8:85a3:08d3:1319:8a2e:0370:7347/64\")";

        final CIDR cidr = (CIDR)venice.eval(script);

        assertEquals("2001:0db8:85a3:08d3:1319:8a2e:0370:7347/64", cidr.getNotation());

        assertEquals(64, cidr.getRange());

        assertEquals("2001:db8:85a3:8d3:0:0:0:0", cidr.getLowHostAddress());

        assertEquals("2001:db8:85a3:8d3:ffff:ffff:ffff:ffff", cidr.getHighHostAddress());
    }

    @Test
    public void test_IP4_in_range() {
        final Venice venice = new Venice();

        final String script_tpl =
                "(cidr/in-range? %s %s)";

        assertFalse((Boolean)venice.eval(String.format(script_tpl, "\"100.0.0.0\"",       "(cidr/parse \"222.192.0.0/11\")")));
        assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.192.0.0\"",     "(cidr/parse \"222.192.0.0/11\")")));
        assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.200.0.0\"",     "(cidr/parse \"222.192.0.0/11\")")));
        assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.223.255.255\"", "(cidr/parse \"222.192.0.0/11\")")));
        assertFalse((Boolean)venice.eval(String.format(script_tpl, "\"240.0.0.0\"",       "(cidr/parse \"222.192.0.0/11\")")));

        assertFalse((Boolean)venice.eval(String.format(script_tpl, "\"100.0.0.0\"",       "\"222.192.0.0/11\"")));
        assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.192.0.0\"",     "\"222.192.0.0/11\"")));
        assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.200.0.0\"",     "\"222.192.0.0/11\"")));
        assertTrue ((Boolean)venice.eval(String.format(script_tpl, "\"222.223.255.255\"", "\"222.192.0.0/11\"")));
        assertFalse((Boolean)venice.eval(String.format(script_tpl, "\"240.0.0.0\"",       "\"222.192.0.0/11\"")));
    }

}
