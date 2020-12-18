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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class JavaObject {

	public JavaObject() {
	}

	public JavaObject(final Long _Long) {
		this._Long = _Long;
	}
	
	public String getString() {
		return _String;
	}
	public void setString(String _String) {
		this._String = _String;
	}
	public boolean isPrimitiveBoolean() {
		return _primitiveBoolean;
	}
	public void setPrimitiveBoolean(boolean _primitiveBoolean) {
		this._primitiveBoolean = _primitiveBoolean;
	}
	public Boolean getBoolean() {
		return _Boolean;
	}
	public void setBoolean(Boolean _Boolean) {
		this._Boolean = _Boolean;
	}
	public int getPrimitiveInt() {
		return _primitiveInt;
	}
	public void setPrimitiveInt(int _primitiveInt) {
		this._primitiveInt = _primitiveInt;
	}
	public Integer getInteger() {
		return _Integer;
	}
	public void setInteger(Integer _Integer) {
		this._Integer = _Integer;
	}
	public long getPrimitiveLong() {
		return _primitiveLong;
	}
	public void setPrimitiveLong(long _primitiveLong) {
		this._primitiveLong = _primitiveLong;
	}
	public Long getLong() {
		return _Long;
	}
	public void setLong(Long _Long) {
		this._Long = _Long;
	}
	public float getPrimitiveFloat() {
		return _primitiveFloat;
	}
	public void setPrimitiveFloat(float _primitiveFloat) {
		this._primitiveFloat = _primitiveFloat;
	}
	public Float getFloat() {
		return _Float;
	}
	public void setFloat(Float _Float) {
		this._Float = _Float;
	}
	public double getPrimitiveDouble() {
		return _primitiveDouble;
	}
	public void setPrimitiveDouble(double _primitiveDouble) {
		this._primitiveDouble = _primitiveDouble;
	}
	public Double getDouble() {
		return _Double;
	}
	public void setDouble(Double _Double) {
		this._Double = _Double;
	}
	public BigDecimal getBigDecimal() {
		return _BigDecimal;
	}
	public void setBigDecimal(BigDecimal _BigDecimal) {
		this._BigDecimal = _BigDecimal;
	}
	public JavaEnum getJavaEnum() {
		return _JavaEnum;
	}
	public void setJavaEnum(JavaEnum _JavaEnum) {
		this._JavaEnum = _JavaEnum;
	}
	public List<?> getList() {
		return _List;
	}
	public void setList(List<?> _List) {
		this._List = _List;
	}
	public Set<?> getSet() {
		return _Set;
	}
	public void setSet(Set<?> _Set) {
		this._Set = _Set;
	}
	public Map<?, ?> getMap() {
		return _Map;
	}
	public void setMap(Map<?, ?> _Map) {
		this._Map = _Map;
	}
	public byte[] getByteArray() {
		return _ByteArr;
	}
	public void setByteArray(byte[] _ByteArr) {
		this._ByteArr = _ByteArr;
	}
	public void setIntArray(int[] _IntArr) {
		this._IntArr = _IntArr;
	}
	public int[] getIntArray() {
		return _IntArr;
	}
	public void setIntegerArray(Integer[] _IntegerArr) {
		this._IntegerArr = _IntegerArr;
	}
	public Integer[] getIntegerArray() {
		return _IntegerArr;
	}
	public void setStringArray(String[] _StringArr) {
		this._StringArr = _StringArr;
	}
	public String[] getStringArray() {
		return _StringArr;
	}

	public void setOverloaded(byte[] bytes) {
		this._Overload = bytes;
	}
	public void setOverloaded(String s) {
		this._Overload = s;
	}
	public void setOverloaded(Long l) {
		this._Overload = l;
	}
	public Object getOverloaded() {
		return this._Overload;
	}

	public void _void() {
	}

	public String _Overloaded() {
		return "";
	}

	public String _Overloaded(String s1) {
		return s1;
	}
	public String _Overloaded(String s1, String s2) {
		return "" + s1 + "," + s2;
	}

	public String _Overloaded(String s1, String s2, String s3) {
		return "" + s1 + "," + s2 + "," + s3;
	}

	public String _String(String s1) {
		return s1;
	}

	public String _StringString(String s1, String s2) {
		return "" + s1 + "," + s2;
	}

	public String _StringStringString(String s1, String s2, String s3) {
		return "" + s1 + "," + s2 + "," + s3;
	}
	
	public String _StringByteArrString(String p1, byte[] p2, String p3) {
		return "" + p1 + "," + (p2 == null ? "null" : p2.length) + "," + p3;
	}
	
	public static String staticVoid() {
		return "123";
	}

	
	public static enum JavaEnum { one, two, three };

	private String _String;
	private boolean _primitiveBoolean;
	private Boolean _Boolean;
	private int _primitiveInt;
	private Integer _Integer;
	private long _primitiveLong;
	private Long _Long;
	private float _primitiveFloat;
	private Float _Float;
	private double _primitiveDouble;
	private Double _Double;	
	private BigDecimal _BigDecimal;	
	private JavaEnum _JavaEnum;
	private List<?> _List;
	private Set<?> _Set;
	private Map<?,?> _Map;
	private byte[] _ByteArr;
	private int[] _IntArr;
	private Integer[] _IntegerArr;
	private String[] _StringArr;
	private Object _Overload;
}
