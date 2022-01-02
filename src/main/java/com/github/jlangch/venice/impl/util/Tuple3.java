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
 * Defines a generic tuple with 3 elements
 *
 * @param <F> the 1. element type of the tuple
 * @param <S> the 2. element type of the tuple
 * @param <T> the 3. element type of the tuple
 */
public class Tuple3<F,S,T> implements Comparable<Tuple3<F,S,T>>, Serializable {

	public Tuple3(final F _1, final S _2, final T _3){
		this._1 = _1;
		this._2 = _2;
		this._3 = _3;
	}

	public F getFirst() {
		return _1;
	}
	
	public S getSecond() {
		return _2;
	}
	
	public T getThird() {
		return _3;
	}
	
	
	public static <T1,T2,T3> Tuple3<T1,T2,T3> of(final T1 _1, final T2 _2, final T3 _3) {
		return new Tuple3<T1,T2,T3>(_1, _2, _3);
	}
	
	public static Tuple3<String,String,String> splitString(final String s, final String regex) {
		final String[] elements = s.split(regex);
		if (elements == null || elements.length == 0) {
			throw new RuntimeException("No elements");
		}
		else if (elements.length == 1) {
			return new Tuple3<>(elements[0], null, null); 
		}
		else if (elements.length == 2) {
			return new Tuple3<>(elements[0], elements[1], null); 
		}
		else {
			return new Tuple3<>(elements[0], elements[1], elements[2]); 
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
		result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
		result = prime * result + ((_3 == null) ? 0 : _3.hashCode());
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
		Tuple3<?,?,?> other = (Tuple3<?,?,?>) obj;
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
		if (_3 == null) {
			if (other._3 != null)
				return false;
		} else if (!_3.equals(other._3))
			return false;
		return true;
	}

	@Override
	public int compareTo(final Tuple3<F,S,T> that) {
		return Tuple3.compareTo(this, that);
	}
	
	@SuppressWarnings("unchecked")
	public static <T1,T2,T3> Comparator<Tuple3<T1,T2,T3>> comparator(
			Comparator<? super T1> t1Comp, 
			Comparator<? super T2> t2Comp, 
			Comparator<? super T3> t3Comp
	) {
		return (Comparator<Tuple3<T1,T2,T3>> & Serializable) (t1, t2) -> {
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

			// all elements are equal
			return 0;
		};
	}

	@SuppressWarnings("unchecked")
	private static <T1 extends Comparable<? super T1>, 
					T2 extends Comparable<? super T2>, 
					T3 extends Comparable<? super T3>> int compareTo(
			final Tuple3<?,?,?> o1, 
			final Tuple3<?,?,?> o2
	) {
		return Comparator
				.comparing((Tuple3<T1,T2,T3> t) -> t.getFirst())
				.thenComparing((Tuple3<T1,T2,T3> t) -> t.getSecond())
				.thenComparing((Tuple3<T1,T2,T3> t) -> t.getThird())
				.compare((Tuple3<T1,T2,T3>)o1, (Tuple3<T1,T2,T3>)o2);
	}


	@Override
	public String toString() {
		return "(" + _1 + "," + _2 + "," + _3 + ")";
	}

	
	private static final long serialVersionUID = 960403020076340207L;

	public final F _1;
	public final S _2;
	public final T _3;
}
