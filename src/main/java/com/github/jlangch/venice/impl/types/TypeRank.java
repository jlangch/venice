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
package com.github.jlangch.venice.impl.types;

public enum TypeRank {

    CONSTANT (0),
    BOOLEAN (1),

    INTEGER (10),
    LONG (11),
    FLOAT (12),
    DOUBLE (13),
    BIGDECIMAL (14),
    BIGINTEGER (15),

    STRING (20),
    CHAR (21),

    KEYWORD (30),
    SYMBOL (31),

    BYTEBUFFER (40),
    JAVAOBJECT (41),
    CUSTOMTYPE (42),

    ATOM (50),
    VOLATILE (51),
    THREADLOCAL (52),

    SPECIAL_FORM (60),

    FUNCTION (70),
    MULTI_ARITY_FUNCTION (71),
    MULTI_FUNCTION (72),
    MULTI_PROTOCOL_FUNCTION (73),

    JUST (80),

    CUSTOM_TYPE_DEF (100),
    PROTOCOL_TYPE (101),

    LIST (200),
    JAVALIST (201),
    MUTABLELIST (202),
    LAZYSEQ(203),

    VECTOR (210),
    MUTABLEVECTOR (211),

    HASHMAP (220),
    ORDEREDMAP (221),
    SORTEDMAP (222),
    JAVAMAP (223),
    MAPENTRY (224),
    MUTABLEMAP (225),

    HASHSET (230),
    SORTEDSET (231),
    JAVASET (232),
    MUTABLESET (233),

    DAG (240),

    QUEUE (250),
    DELAYQUEUE (251),
    STACK (252),
    LOCK (252);


    private TypeRank(final int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    private final int rank;
}
