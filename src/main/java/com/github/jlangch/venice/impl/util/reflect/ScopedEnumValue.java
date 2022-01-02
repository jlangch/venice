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
package com.github.jlangch.venice.impl.util.reflect;

/**
 * Defines a scoped enum value and its meta data
 * 
 * <p>E.g.: scopedEnumValue: java.time.Month.JANUARY
 * <ul>
 *   <li>enumValue: JANUARY</li>
 *   <li>enumClassName: java.time.Month</li>
 *   <li>enumClassSimpleName: Month</li>
 * </ul>
 */
public class ScopedEnumValue {

	/**
	 * @param scopedEnumValue A scoped enum 'java.time.Month.JANUARY'
	 */
	public ScopedEnumValue(final String scopedEnumValue) {
		this.scopedEnumValue = scopedEnumValue;
		
		final int p1 = scopedEnumValue.lastIndexOf('.');
		if (p1 > 0) {
			enumValue = scopedEnumValue.substring(p1+1);
			final String className = scopedEnumValue.substring(0, p1);
			
			final int p2 = className.lastIndexOf('.');
			if (p2 > 0) {
				enumClassSimpleName = className.substring(p2+1);
				enumClassName = className.substring(0, p2);
			}
			else {
				enumClassSimpleName = className;
				enumClassName = null;
			}
		}
		else {
			enumValue = scopedEnumValue;
			enumClassName = null;
			enumClassSimpleName = null;
		}
		
	}
	
	
	public String getScopedEnumValue() {
		return scopedEnumValue;
	}
	
	public String getEnumValue() {
		return enumValue;
	}
	
	public String getEnumClassName() {
		return enumClassName;
	}
	
	public String getEnumClassSimpleName() {
		return enumClassSimpleName;
	}
	
	public boolean isScoped() {
		return enumClassSimpleName != null;
	}

	public boolean isCompatible(final Class<?> clazz) {
		return (enumClassName != null && clazz.getName().equals(enumClassName))
					|| (enumClassSimpleName != null && clazz.getSimpleName().equals(enumClassSimpleName));
	}
	
	public Enum<?> getEnum(final Class<? extends Enum<?>> clazz) {
		for(Enum<?> e : clazz.getEnumConstants()) {
			if (enumValue.equals(e.name())) {
				return e;
			}
		}
		return null;
	}

	
	
	private final String scopedEnumValue;
	private final String enumValue;
	private final String enumClassName;
	private final String enumClassSimpleName;
}
