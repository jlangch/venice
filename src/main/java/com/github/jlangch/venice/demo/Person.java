/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.demo;

import java.time.LocalDate;


/**
 * Util class used for some examples.
 */
public class Person {

    public Person(
            final String firstName,
            final String lastName,
            final LocalDate birthdate,
            final Gender gender
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.gender = gender;
    }


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public Gender getGender() {
        return gender;
    }

    public int getAge() {
        return LocalDate.now().getYear() - birthdate.getYear();
    }


    @Override
    public String toString() {
        return String.format("%s %s, %d", firstName, lastName, getAge());
    }


    public static enum Gender {Male, Female};

    private final String firstName;
    private final String lastName;
    private final LocalDate birthdate;
    private final Gender gender;
}
