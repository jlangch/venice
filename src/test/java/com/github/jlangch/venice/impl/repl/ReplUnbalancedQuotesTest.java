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
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;


public class ReplUnbalancedQuotesTest {

    @Test
    public void test_triple_quotes__no_errorOnUnbalancedStringQuotes() {
        IVeniceInterpreter interpreter = new VeniceInterpreter(new AcceptAllInterceptor());

        try {
            interpreter.READ("(def x \"\"\"", "test", false);
            fail("Expected EofException");
        }
        catch(EofException ex) {
           assertEquals("Expected ')', got EOF. File <unknown> (1,1)", ex.getMessage());
        }
        catch(ParseError ex) {
            fail("Did not expect a ParseError exception");
        }

        try {
            interpreter.READ("(def x \"\"\"123", "test", false);
            fail("Expected EofException");
        }
        catch(EofException ex) {
           assertEquals("Expected ')', got EOF. File <unknown> (1,1)", ex.getMessage());
        }
        catch(ParseError ex) {
            fail("Did not expect a ParseError exception");
        }

        interpreter.READ("(def x \"\"\"123\"\"\")", "test", false);
    }

    @Test
    public void test_triple_quotes__with_errorOnUnbalancedStringQuotes() {
        IVeniceInterpreter interpreter = new VeniceInterpreter(new AcceptAllInterceptor());

        try {
            interpreter.READ("(def x \"\"\"", "test", true);
            fail("Expected ParseError");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(ParseError ex) {
            assertEquals("Parse error (tokenizer phase) while reading from input", ex.getMessage());
        }

        try {
            interpreter.READ("(def x \"\"\"123", "test", true);
            fail("Expected EofException");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(ParseError ex) {
            assertEquals("Parse error (tokenizer phase) while reading from input", ex.getMessage());
        }

        interpreter.READ("(def x \"\"\"123\"\"\")", "test", true);
    }


    @Test
    public void test_single_quotes__no_errorOnUnbalancedStringQuotes() {
        IVeniceInterpreter interpreter = new VeniceInterpreter(new AcceptAllInterceptor());

        try {
            interpreter.READ("(def x \"", "test", false);
            fail("Expected EofException");
        }
        catch(EofException ex) {
           assertEquals("Expected ')', got EOF. File <unknown> (1,1)", ex.getMessage());
        }
        catch(ParseError ex) {
            fail("Did not expect a ParseError exception");
        }

        try {
            interpreter.READ("(def x \"123", "test", false);
            fail("Expected EofException");
        }
        catch(EofException ex) {
           assertEquals("Expected ')', got EOF. File <unknown> (1,1)", ex.getMessage());
        }
        catch(ParseError ex) {
            fail("Did not expect a ParseError exception");
        }

        interpreter.READ("(def x \"123\")", "test", false);
    }

    @Test
    public void test_single_quotes__with_errorOnUnbalancedStringQuotes() {
        IVeniceInterpreter interpreter = new VeniceInterpreter(new AcceptAllInterceptor());

        try {
            interpreter.READ("(def x \"", "test", true);
            fail("Expected ParseError");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(ParseError ex) {
            assertEquals("Parse error (tokenizer phase) while reading from input", ex.getMessage());
        }

        try {
            interpreter.READ("(def x \"123", "test", true);
            fail("Expected ParseError");
        }
        catch(EofException ex) {
            fail("Did not expect a EofException exception");
        }
        catch(ParseError ex) {
            assertEquals("Parse error (tokenizer phase) while reading from input", ex.getMessage());
        }

        interpreter.READ("(def x \"123\")", "test", true);
    }

 }
