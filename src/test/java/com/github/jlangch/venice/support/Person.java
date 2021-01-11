/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.support;

import java.util.HashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.util.excel.EntityRecord;


public class Person {
	
	public Person(final String firstName, final String lastName, final Integer age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}

	public String getFirstName() { return firstName; }
	public String getLastName() { return lastName; }
	public Integer getAge() { return age; }
	
	public EntityRecord toEntityRecord() {
		final Map<String,Object> entity = new HashMap<>();
		entity.put("firstName", firstName);
		entity.put("lastName", lastName);
		entity.put("age", age);
		return EntityRecord.of(entity);
	}
	
	public String toString() { 
		return String.format("%s %s (%d)", firstName, lastName, age); 
	}
	

	private final String firstName;
	private final String lastName;
	private final Integer age;
}
