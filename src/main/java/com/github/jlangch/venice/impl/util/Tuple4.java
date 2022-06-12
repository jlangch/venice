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
package com.github.jlangch.venice.impl.util;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Defines a generic tuple with 4 elements
 *
 * @param <A> the 1. element type of the tuple
 * @param <B> the 2. element type of the tuple
 * @param <C> the 3. element type of the tuple
 * @param <D> the 4. element type of the tuple
 */
public class Tuple4<A,B,C,D> implements Comparable<Tuple4<A,B,C,D>>, Serializable {

    public Tuple4(final A _1, final B _2, final C _3, final D _4){
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
    }

    public A getFirst() {
        return _1;
    }

    public B getSecond() {
        return _2;
    }

    public C getThird() {
        return _3;
    }

    public D getFourth() {
        return _4;
    }


    public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> of(final T1 _1, final T2 _2, final T3 _3, final T4 _4) {
        return new Tuple4<T1,T2,T3,T4>(_1, _2, _3, _4);
    }

    public static Tuple4<String,String,String,String> splitString(final String s, final String regex) {
        final String[] elements = s.split(regex);
        if (elements == null || elements.length == 0) {
            throw new RuntimeException("No elements");
        }
        else if (elements.length == 1) {
            return new Tuple4<>(elements[0], null, null, null);
        }
        else if (elements.length == 2) {
            return new Tuple4<>(elements[0], elements[1], null, null);
        }
        else if (elements.length == 3) {
            return new Tuple4<>(elements[0], elements[1], elements[2], null);
        }
        else {
            return new Tuple4<>(elements[0], elements[1], elements[2], elements[3]);
        }
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
        result = prime * result + ((_4 == null) ? 0 : _4.hashCode());
        result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
        result = prime * result + ((_3 == null) ? 0 : _3.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tuple4<?,?,?,?> other = (Tuple4<?,?,?,?>) obj;
        if (_1 == null) {
            if (other._1 != null)
                return false;
        } else if (!_1.equals(other._1))
            return false;
        if (_4 == null) {
            if (other._4 != null)
                return false;
        } else if (!_4.equals(other._4))
            return false;
        if (_2 == null) {
            if (other._2 != null)
                return false;
        } else if (!_2.equals(other._2))
            return false;
        if (_3 == null) {
            if (other._3 != null)
                return false;
        } else if (!_3.equals(other._3))
            return false;
        return true;
    }

    @Override
    public int compareTo(final Tuple4<A,B,C,D> that) {
        return Tuple4.compareTo(this, that);
    }

    @SuppressWarnings("unchecked")
    public static <T1,T2,T3,T4> Comparator<Tuple4<T1,T2,T3,T4>> comparator(
            Comparator<? super T1> t1Comp,
            Comparator<? super T2> t2Comp,
            Comparator<? super T3> t3Comp,
            Comparator<? super T4> t4Comp
    ) {
        return (Comparator<Tuple4<T1,T2,T3,T4>> & Serializable) (t1, t2) -> {
            final int check1 = t1Comp.compare(t1._1, t2._1);
            if (check1 != 0) {
                return check1;
            }

            final int check2 = t2Comp.compare(t1._2, t2._2);
            if (check2 != 0) {
                return check2;
            }

            final int check3 = t3Comp.compare(t1._3, t2._3);
            if (check3 != 0) {
                return check3;
            }

            final int check4 = t4Comp.compare(t1._4, t2._4);
            if (check4 != 0) {
                return check4;
            }

            // all elements are equal
            return 0;
        };
    }

    @SuppressWarnings("unchecked")
    private static <T1 extends Comparable<? super T1>,
                    T2 extends Comparable<? super T2>,
                    T3 extends Comparable<? super T3>,
                    T4 extends Comparable<? super T4>> int compareTo(
            final Tuple4<?,?,?,?> o1,
            final Tuple4<?,?,?,?> o2
    ) {
        return Comparator
                    .comparing((Tuple4<T1,T2,T3,T4> t) -> t.getFirst())
                    .thenComparing((Tuple4<T1,T2,T3,T4> t) -> t.getSecond())
                    .thenComparing((Tuple4<T1,T2,T3,T4> t) -> t.getThird())
                    .thenComparing((Tuple4<T1,T2,T3,T4> t) -> t.getFourth())
                    .compare((Tuple4<T1,T2,T3,T4>)o1, (Tuple4<T1,T2,T3,T4>)o2);
    }


    @Override
    public String toString() {
        return "(" + _1 + "," + _2  + "," + _3 + "," + _4 +")";
    }


    private static final long serialVersionUID = 4043548396519528223L;

    public final A _1;
    public final B _2;
    public final C _3;
    public final D _4;
}
