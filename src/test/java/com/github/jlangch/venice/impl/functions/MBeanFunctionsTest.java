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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class MBeanFunctionsTest {

    @Test
    public void test_platform_mbean_server() {
        final Venice venice = new Venice();

        final String script = "(mbean/platform-mbean-server)";

        assertTrue(venice.eval(script) instanceof MBeanServer);
    }

    @Test
    public void test_query_mbean_object_names() {
        final Venice venice = new Venice();

        final String script = "(mbean/query-mbean-object-names)";

        assertTrue(venice.eval(script) instanceof List);
    }

    @Test
    public void test_object_name_1() {
        final Venice venice = new Venice();

        final String script = "(mbean/object-name \"java.lang:type=OperatingSystem\")";

        assertTrue(venice.eval(script) instanceof ObjectName);
    }

    @Test
    public void test_object_name_2() {
        final Venice venice = new Venice();

        final String script = "(mbean/object-name \"java.lang\" \"type\" \"OperatingSystem\")";

        assertTrue(venice.eval(script) instanceof ObjectName);
    }

    @Test
    public void test_info() {
        final Venice venice = new Venice();

        final String script = "(let [m (mbean/object-name \"java.lang:type=OperatingSystem\")]  \n" +
        		              "   (mbean/info m))";

        assertTrue(venice.eval(script) instanceof Map);
    }

    @Test
    public void test_register() {
    	ensureUnregisteredHelloMBean();

        final Venice venice = new Venice();

        final String script = "(do                                                          \n" +
                              "  (import :com.github.jlangch.venice.impl.util.mbean.Hello)  \n" +
                              "  (let [name (mbean/object-name \"venice:type=Hello\")]      \n" +
                              "     (mbean/register (. :Hello :new) name)))                 ";

        venice.eval(script);
    }

    @Test
    public void test_unregister() {
    	ensureUnregisteredHelloMBean();

        final Venice venice = new Venice();

        final String script = "(do                                                          \n" +
                              "  (import :com.github.jlangch.venice.impl.util.mbean.Hello)  \n" +
                              "  (let [name (mbean/object-name \"venice:type=Hello\")]      \n" +
                              "     (mbean/register (. :Hello :new) name)                   \n" +
                              "     (mbean/unregister name)))                               ";

        venice.eval(script);
    }

    @Test
    public void test_attribute() {
    	ensureUnregisteredHelloMBean();

        final Venice venice = new Venice();

        final String script = "(do                                                          \n" +
                              "  (import :com.github.jlangch.venice.impl.util.mbean.Hello)  \n" +
                              "  (let [name (mbean/object-name \"venice:type=Hello\")]      \n" +
                              "     (mbean/register (. :Hello :new) name)                   \n" +
                              "     (mbean/attribute name \"FourtyTwo\")))                  ";

        assertEquals(42, venice.eval(script));
    }

    @Test
    public void test_operation_1() {
    	ensureUnregisteredHelloMBean();

        final Venice venice = new Venice();

        final String script = "(do                                                          \n" +
                              "  (import :com.github.jlangch.venice.impl.util.mbean.Hello)  \n" +
                              "  (let [name (mbean/object-name \"venice:type=Hello\")]      \n" +
                              "     (mbean/register (. :Hello :new) name)                   \n" +
                              "     (mbean/invoke name \"add\" [1I 2I] [\"int\" \"int\"]))) ";

        assertEquals(3, venice.eval(script));
    }

    @Test
    public void test_operation_2() {
    	ensureUnregisteredHelloMBean();

        final Venice venice = new Venice();

        final String script = "(do                                                          \n" +
                              "  (import :com.github.jlangch.venice.impl.util.mbean.Hello)  \n" +
                              "  (let [name (mbean/object-name \"venice:type=Hello\")]      \n" +
                              "     (mbean/register (. :Hello :new) name)                   \n" +
                              "     (mbean/invoke name \"add\" [1I 2I])))                   ";

        assertEquals(3, venice.eval(script));
    }




    private static void ensureUnregisteredHelloMBean() {
    	try {
	        final String script = "(let [name (mbean/object-name \"venice:type=Hello\")] \n" +
	                              "  (mbean/unregister name))                            ";

	        new Venice().eval(script);
    	}
    	catch(Exception ex) {
    		// don't care
    	}
    }
}
