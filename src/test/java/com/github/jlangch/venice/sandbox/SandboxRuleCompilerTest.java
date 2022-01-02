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
package com.github.jlangch.venice.sandbox;

import static com.github.jlangch.venice.impl.sandbox.SandboxRuleCompiler.compile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class SandboxRuleCompilerTest {

	@Test
	public void testClassMethodMatch() {
		assertTrue(compile("x").asPredicate().test("x"));
		
		assertTrue(compile("x.y.z.Aaa").asPredicate().test("x.y.z.Aaa"));
		assertTrue(compile("x.y.z.*").asPredicate().test("x.y.z.Aaa"));
		assertTrue(compile("x.y.**").asPredicate().test("x.y.z.Aaa"));
		assertTrue(compile("x.**").asPredicate().test("x.y.z.Aaa"));
		
		assertTrue(compile("x.y.z.Aaa:size").asPredicate().test("x.y.z.Aaa:size"));
		assertTrue(compile("x.y.z.*:size").asPredicate().test("x.y.z.Aaa:size"));
		assertTrue(compile("x.y.**:size").asPredicate().test("x.y.z.Aaa:size"));
		assertTrue(compile("x.**:size").asPredicate().test("x.y.z.Aaa:size"));
		assertTrue(compile("x.**.Aaa:size").asPredicate().test("x.y.z.Aaa:size"));
		
		assertTrue(compile("x.y.z.Aaa:*").asPredicate().test("x.y.z.Aaa:size"));
		assertTrue(compile("x.y.z.*:*").asPredicate().test("x.y.z.Aaa:size"));
		assertTrue(compile("x.y.**:*").asPredicate().test("x.y.z.Aaa:size"));
		assertTrue(compile("x.**:*").asPredicate().test("x.y.z.Aaa:size"));
		assertTrue(compile("x.**.Aaa:*").asPredicate().test("x.y.z.Aaa:size"));
		
		assertTrue(compile("x.y.z.Aaa$Bbb:size").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.z.Aaa$*:size").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.z.*:size").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.**:size").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.**$Bbb:size").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.**.Aaa$Bbb:size").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.**.Aaa$*:size").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		
		assertTrue(compile("x.y.z.Aaa$Bbb:*").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.z.Aaa$*:*").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.z.*:*").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.**:*").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.**$Bbb:*").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.**.Aaa$Bbb:*").asPredicate().test("x.y.z.Aaa$Bbb:size"));
		assertTrue(compile("x.y.**.Aaa$*:*").asPredicate().test("x.y.z.Aaa$Bbb:size"));
	}

	@Test
	public void testClassMethodNotMatch() {
		assertFalse(compile("x").asPredicate().test("y"));
		
		assertFalse(compile("x.y.z.Aaa").asPredicate().test("x.y.c.Baa"));
		assertFalse(compile("x.y.z.*").asPredicate().test("x.y.c.Baa"));
		assertFalse(compile("x.y.**").asPredicate().test("x.b.c.Baa"));
		assertFalse(compile("x.**").asPredicate().test("a.y.z.Baa"));
		
		assertFalse(compile("x.y.z.Aaa:size").asPredicate().test("x.y.z.Aaa:length"));
		assertFalse(compile("x.y.z.Aaa:size").asPredicate().test("x.y.z.Baa:size"));
		assertFalse(compile("x.y.z.*:size").asPredicate().test("x.y.z.Aaa:length"));
		assertFalse(compile("x.y.z.*:size").asPredicate().test("x.y.v.Aaa:length"));
		assertFalse(compile("x.y.**:size").asPredicate().test("x.y.z.Aaa:length"));
		assertFalse(compile("x.y.**:size").asPredicate().test("x.y.v.Aaa:length"));
		assertFalse(compile("x.**:size").asPredicate().test("x.y.z.Aaa:length"));
		assertFalse(compile("x.**.Aaa:size").asPredicate().test("x.y.z.Aaa:length"));
		
		assertFalse(compile("x.y.z.Aaa:*").asPredicate().test("x.y.z.Baa:size"));
		assertFalse(compile("x.y.z.*:*").asPredicate().test("x.y.c.Aaa:size"));
		assertFalse(compile("x.y.**:*").asPredicate().test("x.b.c.Aaa:size"));
		assertFalse(compile("x.**:*").asPredicate().test("a.b.c.Aaa:size"));
		assertFalse(compile("x.**.Aaa:*").asPredicate().test("x.y.z.Baa:size"));

		assertFalse(compile("x.y.z.Aaa$Bbb:size").asPredicate().test("x.y.z.Aaa$Cbb:size"));
		assertFalse(compile("x.y.z.Aaa$*:size").asPredicate().test("x.y.z.Baa$Cbb:size"));
		assertFalse(compile("x.y.**$Bbb:size").asPredicate().test("x.y.z.Aaa$Cbb:size"));
		assertFalse(compile("x.y.**.Aaa$Bbb:size").asPredicate().test("x.y.z.Aaa$Cbb:size"));
		assertFalse(compile("x.y.**.Aaa$Bbb:size").asPredicate().test("x.y.z.Baa$Bbb:size"));
		assertFalse(compile("x.y.**.Aaa$*:size").asPredicate().test("x.y.z.Baa$Bbb:size"));

		assertFalse(compile("x.y.z.Aaa$Bbb:size").asPredicate().test("x.y.z.Aaa$Bbb:length"));
		assertFalse(compile("x.y.z.Aaa$*:size").asPredicate().test("x.y.z.Aaa$Bbb:length"));
		assertFalse(compile("x.y.z.*:size").asPredicate().test("x.y.z.Aaa$Bbb:length"));
		assertFalse(compile("x.y.**:size").asPredicate().test("x.y.z.Aaa$Bbb:length"));
		assertFalse(compile("x.y.**$Bbb:size").asPredicate().test("x.y.z.Aaa$Bbb:length"));
		assertFalse(compile("x.y.**.Aaa$Bbb:size").asPredicate().test("x.y.z.Aaa$Bbb:length"));
		assertFalse(compile("x.y.**.Aaa$*:size").asPredicate().test("x.y.z.Aaa$Bbb:length"));
	}

}
