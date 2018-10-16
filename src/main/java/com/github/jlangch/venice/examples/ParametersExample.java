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
package com.github.jlangch.venice.examples;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class ParametersExample {
	
	public static void main(final String[] args) {
		final Venice venice = new Venice();
		
		System.out.println(
				venice.eval(
						"(+ x y 1)", 
						Parameters.of("x", 6, "y", 3L)));
	
		System.out.println(
				venice.eval(
						"(str (:firstname user) \" \" (:lastname user))", 
						Parameters.of("user", new User("Dent", "Arthur", 42))));
	}
	
	
	public static class User {
		public User(
				final String lastname,
				final String firstname,
				final int age
		) {
			this.lastname = lastname;
			this.firstname = firstname;
			this.age = age;
		}
		
		
		public String getLastname() {
			return lastname;
		}
		
		public String getFirstname() {
			return firstname;
		}
		
		public int getAge() {
			return age;
		}


		private final String lastname;
		private final String firstname;
		private final int age;
	}
}