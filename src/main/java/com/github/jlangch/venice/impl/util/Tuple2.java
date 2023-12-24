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
package com.github.jlangch.venice.impl.util;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Defines a generic tuple with 2 elements
 *
 * @param <F> the 1. element type of the tuple
 * @param <S> the 2. element type of the tuple
 */
public class Tuple2<F,S> implements Comparable<Tuple2<F,S>>, Serializable {

    public Tuple2(final F _1, final S _2){
        this._1 = _1;
        this._2 = _2;
    }

    public F getFirst() {
        return _1;
    }

    public S getSecond() {
        return _2;
    }


    public static <T1,T2> Tuple2<T1,T2> of(final T1 _1, final T2 _2) {
        return new Tuple2<T1,T2>(_1, _2);
    }

    public static Tuple2<String,String> splitString(final String s, final String regex) {
        final String[] elements = s.split(regex);
        if (elements == null || elements.length == 0) {
            throw new RuntimeException("No elements");
        }
        else if (elements.length == 1) {
            return new Tuple2<>(elements[0], null);
        }
        else {
            return new Tuple2<>(elements[0], elements[1]);
        }
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
        result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tuple2<?,?> other = (Tuple2<?,?>)obj;
        if (_1 == null) {
            if (other._1 != null)
                return false;
        } else if (!_1.equals(other._1))
            return false;
        if (_2 == null) {
            if (other._2 != null)
                return false;
        } else if (!_2.equals(other._2))
            return false;
        return true;
    }

    @Override
    public int compareTo(final Tuple2<F,S> that) {
        return Tuple2.compareTo(this, that);
    }

    @SuppressWarnings("unchecked")
    public static <T1, T2> Comparator<Tuple2<T1, T2>> comparator(
            final Comparator<? super T1> t1Comp,
            final Comparator<? super T2> t2Comp
    ) {
        return (Comparator<Tuple2<T1, T2>> & Serializable) (t1, t2) -> {
            final int check1 = t1Comp.compare(t1._1, t2._1);
            if (check1 != 0) {
                return check1;
            }

            final int check2 = t2Comp.compare(t1._2, t2._2);
            if (check2 != 0) {
                return check2;
            }

            // all elements are equal
            return 0;
        };
    }

    @SuppressWarnings("unchecked")
    private static <T1 extends Comparable<? super T1>,
                    T2 extends Comparable<? super T2>> int compareTo(
            final Tuple2<?,?> o1,
            final Tuple2<?,?> o2
    ) {
        return Comparator
                    .comparing((Tuple2<T1,T2> t) -> t.getFirst())
                    .thenComparing((Tuple2<T1,T2> t) -> t.getSecond())
                    .compare((Tuple2<T1,T2>)o1, (Tuple2<T1,T2>)o2);
    }

    @Override
    public String toString() {
        return "(" + _1 + "," + _2 + ")";
    }


    private static final long serialVersionUID = 5055666959605753691L;

    public final F _1;
    public final S _2;
}
