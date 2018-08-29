/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jlangch.venice.impl.util.MacroDef;


public class CoreMacroDefs {

	public static MacroDef getMacroDef(final String name) {
		return getMacros().stream().filter(m -> m.getName().equals(name)).findFirst().orElse(null);
	}
	
	public static List<MacroDef> getMacros() {
		final List<MacroDef> macros = new ArrayList<>();
		
		macros.add(new MacroDef(
				"comment", 
				Arrays.asList("(comment & body)"), 
				"Ignores body, yields nil",
				null));

		macros.add(new MacroDef(
				"assert", 
				Arrays.asList("(assert expr)", "(assert expr message)"), 
				"Evaluates expr and throws an exception if it does not evaluate " + 
				"to logical true.",
				null));

		macros.add(new MacroDef(
				"and", 
				Arrays.asList("(and & pred-forms)"), 
				"Ands the predicate forms",
				null));
		
		macros.add(new MacroDef(
				"or", 
				Arrays.asList("(or & pred-forms)"), 
				"Ors the predicate forms",
				null));
		
		macros.add(new MacroDef(
				"cond",
				Arrays.asList("(cond & clauses)"), 
				"Takes a set of test/expr pairs. It evaluates each test one at a " + 
				"time.  If a test returns logical true, cond evaluates and returns " + 
				"the value of the corresponding expr and doesn't evaluate any of the " + 
				"other tests or exprs. (cond) returns nil.",
				null));
		
		macros.add(new MacroDef(
				"when", 
				Arrays.asList("(when test & body)"), 
				"Evaluates test. If logical true, evaluates body in an implicit do.",
				null));
		
		macros.add(new MacroDef(
				"when-not", 
				Arrays.asList("(when-not test & body)"), 
				"Evaluates test. If logical false, evaluates body in an implicit do.",
				null));		
		
		macros.add(new MacroDef(
				"dotimes", 
				Arrays.asList("(dotimes bindings & body)"),
				"Repeatedly executes body with name " + 
				"bound to integers from 0 through n-1.",
				null));	
		
		macros.add(new MacroDef(
				"while", 
				Arrays.asList("(take-while pred)", "(take-while pred coll)"),
				"Repeatedly executes body while test expression is true. Presumes " +
				"some side-effect will cause test to become false/nil. Returns nil",
				null));		

		macros.add(new MacroDef(
				"doto", 
				Arrays.asList("(doto x & forms)"), 
				"Evaluates x then calls all of the methods and functions with the " + 
				"value of x supplied at the front of the given arguments.  The forms " + 
				"are evaluated in order. Returns x.",
				Arrays.asList(
						"(doto (. :java.util.HashMap :new)  \n" +
						"      (. :put :a 1)                \n" +
						"      (. :put :b 2))                 ")));		
		
		macros.add(new MacroDef(
				"->", 
				Arrays.asList("(-> x & forms)"), 
				"Threads the expr through the forms. Inserts x as the " + 
				"second item in the first form, making a list of it if it is not a " + 
				"list already. If there are more forms, inserts the first form as the " + 
				"second item in second form, etc.",
				Arrays.asList(
						"(-> 5 (+ 3) (/ 2) (- 1))")));		
		
		macros.add(new MacroDef(
				"->>", 
				Arrays.asList("(->> x & forms)"), 
				"Threads the expr through the forms. Inserts x as the " + 
				"last item in the first form, making a list of it if it is not a " + 
				"list already. If there are more forms, inserts the first form as the " + 
				"last item in second form, etc.",
				Arrays.asList(
						"(->> 5 (+ 3) (/ 32) (- 1))",
						"(->> [ {:a 1 :b 2} {:a 3 :b 4} {:a 5 :b 6} {:a 7 :b 8} ] \n" +
						"     (map (fn [x] (get x :b)))                           \n" +
						"     (filter (fn [x] (> x 4)))                           \n" +
						"     (map inc))))                                          ")));
		
		macros.add(new MacroDef(
				"list-comp", 
				Arrays.asList("(list-comp seq-exprs body-expr)"), 
				"List comprehension. Takes a vector of one or more " + 
				"binding-form/collection-expr pairs, each followed by zero or more " + 
				"modifiers, and yields a collection of evaluations of expr. " + 
				"Supported modifiers are: :when test.",
				Arrays.asList(
						"(list-comp [x (range 10)] x)",
						"(list-comp [x (range 5)] (* x 2))",
						"(list-comp [x (range 10) :when (odd? x)] x)",
						"(list-comp [x (range 10) :when (odd? x)] (* x 2))",
						"(list-comp [x (list \"abc\") y [0 1 2]] [x y])")));	
		
		macros.add(new MacroDef(
				"if-let", 
				Arrays.asList("(if-let bindings then else)"), 
				"bindings is a vector with 2 elements: binding-form test. \n" + 
				"If test is true, evaluates then with binding-form bound to the value of " + 
				"test, if not, yields else",
				null));	
		
		macros.add(new MacroDef(
				"time", 
				Arrays.asList("(time [expr])"), 
				"Evaluates expr and prints the time it took.  Returns the value of expr.",
				null));	

		return macros;
	}

}
