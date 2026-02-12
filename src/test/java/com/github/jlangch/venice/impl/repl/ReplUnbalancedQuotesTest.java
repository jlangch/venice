/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.impl.repl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.UnbalancedStringParseError;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;


public class ReplUnbalancedQuotesTest {
    @Test
    public void test_triple_quotes_expr__with_errorOnUnbalancedStringQuotes() {
        IVeniceInterpreter interpreter = new VeniceInterpreter(new AcceptAllInterceptor());

        try {
            interpreter.READ("(def x \"\"\"", "test");
            fail("Expected ParseError");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(UnbalancedStringParseError ex) {
            assertEquals("Expected closing \"\"\" for triple quoted string but got EOF. File <test> (1,8)", ex.getMessage());
        }

        try {
            interpreter.READ("(def x \"\"\"123", "test");
            fail("Expected EofException");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(UnbalancedStringParseError ex) {
            assertEquals("Expected closing \"\"\" for triple quoted string but got EOF. File <test> (1,8)", ex.getMessage());
        }

        interpreter.READ("(def x \"\"\"123\"\"\")", "test");
    }


    @Test
    public void test_single_quotes_expr__with_errorOnUnbalancedStringQuotes() {
        IVeniceInterpreter interpreter = new VeniceInterpreter(new AcceptAllInterceptor());

        try {
            interpreter.READ("(def x \"", "test");
            fail("Expected ParseError");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(UnbalancedStringParseError ex) {
            assertEquals("Expected closing \" for single quoted string but got EOF. File <test> (1,8)", ex.getMessage());
        }

        try {
            interpreter.READ("(def x \"123", "test");
            fail("Expected ParseError");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(UnbalancedStringParseError ex) {
            assertEquals("Expected closing \" for single quoted string but got EOF. File <test> (1,8)", ex.getMessage());
        }

        interpreter.READ("(def x \"123\")", "test");
    }

    @Test
    public void test_single_quotes_literal__with_errorOnUnbalancedStringQuotes() {
        IVeniceInterpreter interpreter = new VeniceInterpreter(new AcceptAllInterceptor());

        try {
            interpreter.READ("\"", "test");
            fail("Expected ParseError");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(UnbalancedStringParseError ex) {
            assertEquals("Expected closing \" for single quoted string but got EOF. File <test> (1,1)", ex.getMessage());
        }

        try {
            interpreter.READ("\"123", "test");
            fail("Expected ParseError");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(UnbalancedStringParseError ex) {
            assertEquals("Expected closing \" for single quoted string but got EOF. File <test> (1,1)", ex.getMessage());
        }

        interpreter.READ("\"123\")", "test");
    }

 }
