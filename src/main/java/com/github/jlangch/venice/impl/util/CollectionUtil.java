/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CollectionUtil {

    /**
     * Creates a list from a single object.
     *
     * @param   <U> The object type
     * @param   object
     *              The object the list will hold. If the object is null an
     *              empty list will be returned
     *
     * @return  the list
     */
    public static <U> List<U> toList(final U object) {
        final List<U> list = new ArrayList<U>();
        if (object != null) {
            list.add(object);
        }
        return list;
    }

    /**
     * Creates an unmodifiable list from a single object.
     *
     * @param   <U> The object type
     * @param   object
     *              The object the list will hold. If the object is null an
     *              empty list will be returned.
     *
     * @return  the list
     */
    public static <U> List<U> toUnmodifiableList(final U object) {
        return Collections.unmodifiableList(toList(object));
    }

    /**
     * Creates a list from objects.
     *
     * @param   <U> The object type
     * @param   objects
     *              The objects the list will hold. If no objects are passed an
     *              empty list will be returned.
     *
     * @return  the list
     */
    @SafeVarargs
    public static <U> List<U> toList(final U ... objects) {
        return objects == null ? new ArrayList<U>() : Arrays.asList(objects);
    }

    /**
     * Converts an enumeration to an array list
     *
     * @param   <U> The object type
     * @param   enumeration
     *              enumeration providing elements for the returned
     *              array list
     * @return  an array list containing the elements returned
     *          by the specified enumeration.
     */
    public static <U> ArrayList<U> toList(Enumeration<U> enumeration) {
        final ArrayList<U> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    /**
     * Creates an unmodifiable list from objects.
     *
     * @param   <U> The object type
     * @param   objects
     *              The objects the list will hold. If no objects are passed an
     *  empty list will be returned.
     *
     * @return  the list
     */
    @SafeVarargs
    public static <U> List<U> toUnmodifiableList(final U ... objects) {
        return Collections.unmodifiableList(toList(objects));
    }

    /**
     * Creates a set from a single object.
     *
     * @param   <U> The object type
     * @param   object
     *              The object the set will hold. If the object is null an
     *              empty set will be returned.
     *
     * @return  the set
     */
    public static <U> Set<U> toSet(final U object) {
        final Set<U> set = new HashSet<U>();
        if (object != null) {
            set.add(object);
        }
        return set;
    }

    /**
     * Creates an unmodifiable set from a single object.
     *
     * @param   <U> The object type
     * @param   object
     *              The object the set will hold. If the object is null an
     *              empty set will be returned.
     *
     * @return  the set
     */
    public static <U> Set<U> toUnmodifiableSet(final U object) {
        return Collections.unmodifiableSet(toSet(object));
    }

    /**
     * Creates a set from objects.
     *
     * @param   <U> The object type
     * @param   objects
     *              The objects the set will hold. If no objects are passed an
     *              empty set will be returned.
     *
     * @return  the set
     */
    @SafeVarargs
    public static <U> Set<U> toSet(final U ... objects) {
        final Set<U> set = new HashSet<U>();
        if (objects != null) {
            for(U obj : objects) {
                set.add(obj);
            }
        }
        return set;
    }

    /**
     * Creates an unmodifiable set from objects.
     *
     * @param   <U> The object type
     * @param   objects
     *              The objects the set will hold. If no objects are passed an
     *              empty set will be returned.
     *
     * @return  the set
     */
    @SafeVarargs
    public static <U> Set<U> toUnmodifiableSet(final U ... objects) {
        return Collections.unmodifiableSet(toSet(objects));
    }

    public static <T> List<T> toEmpty(final List<T> list) {
        return list == null ? new ArrayList<T>() : list;
    }

    public static <U> List<U> drop(final List<U> items, final int n) {
        if (n <= 0) {
            return items;
        }
        else if (n >= items.size()) {
            return new ArrayList<>();
        }
        else {
            return items.subList(n, items.size());
        }
    }

    public static <U> U first(final List<U> items) {
        return items.isEmpty() ? null : items.get(0);
    }

    public static <U> U second(final List<U> items) {
        return items.size() < 2 ? null : items.get(1);
    }

    public static <U> U third(final List<U> items) {
        return items.size() < 3 ? null : items.get(2);
    }

    public static <U> List<U> addFirst(final List<U> items, final U item) {
        items.add(0, item);
        return items;
    }

    public static <U> List<U> addLast(final List<U> items, final U item) {
        items.add(item);
        return items;
    }

}
