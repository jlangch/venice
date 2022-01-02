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
package com.github.jlangch.venice.impl.util.json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBigInteger;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncConstant;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncJavaList;
import com.github.jlangch.venice.impl.types.collections.VncJavaMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaSet;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.nanojson.JsonAppendableWriter;

public class VncJsonWriter {
	
	public VncJsonWriter(
			final JsonAppendableWriter writer,
			final boolean decimalAsDouble
	) {
		this.writer = writer;
		this.decimalAsDouble = decimalAsDouble;
	}

	public VncJsonWriter write(final VncVal val) {
		write(null, val);
		return this;
	}

	public void done() {
		writer.done();
	}

	private void write(final String key, final VncVal val) {
		if (val == null) {
			write_null(key);
		}
		else if (Types.isVncConstant(val)) {
			write_VncConstant(key, (VncConstant)val);
		}
		else if (Types.isVncBoolean(val)) {
			write_VncBoolean(key, (VncBoolean)val);
		}
		else if (Types.isVncString(val)) {
			write_VncString(key, (VncString)val);
		}
		else if (Types.isVncChar(val)) {
			write_VncChar(key, (VncChar)val);
		}
		else if (Types.isVncInteger(val)) {
			write_VncInteger(key, (VncInteger)val);
		}
		else if (Types.isVncLong(val)) {
			write_VncLong(key, (VncLong)val);
		}
		else if (Types.isVncDouble(val)) {
			write_VncDouble(key, (VncDouble)val);
		}
		else if (Types.isVncBigDecimal(val)) {
			write_VncBigDecimal(key, (VncBigDecimal)val);
		}
		else if (Types.isVncBigInteger(val)) {
			write_VncBigInteger(key, (VncBigInteger)val);
		}
		else if (Types.isVncKeyword(val)) {
			write_VncKeyword(key, (VncKeyword)val);
		}
		else if (Types.isVncSymbol(val)) {
			write_VncSymbol(key, (VncSymbol)val);
		}
		else if (Types.isVncJavaObject(val)) {
			write_VncJavaObject(key, (VncJavaObject)val);
		}
		else if (Types.isVncJavaList(val)) {
			write_VncJavaList(key, (VncJavaList)val);
		}
		else if (Types.isVncSequence(val)) {
			write_VncSequence(key, (VncSequence)val);
		}
		else if (Types.isVncJavaMap(val)) {
			write_VncJavaMap(key, (VncJavaMap)val);
		}
		else if (Types.isVncMap(val)) {
			write_VncMap(key, (VncMap)val);
		}
		else if (Types.isVncJavaSet(val)) {
			write_VncJavaSet(key, (VncJavaSet)val);
		}
		else if (Types.isVncSet(val)) {
			write_VncSet(key, (VncSet)val);
		}
		else if (Types.isVncByteBuffer(val)) {
			write_VncByteBuffer(key, (VncByteBuffer)val);
		}
		else if (val instanceof IDeref) {
			write(key, ((IDeref)val).deref()); // delegate to deref value
		}
		else {
			throw new VncException(String.format(
					"Json serialization error: the type %s can not be serialized",
					Types.getType(val)));
		}
	}

	private void write_null(final String key) {
		if (key == null) {
			writer.nul();
		}
		else {
			writer.nul(key);
		}
	}

	private void write_VncConstant(final String key, final VncConstant val) {
		if (key == null) {
			if (val == Constants.Nil) {
				writer.nul();
			}
		}
		else {
			if (val == Constants.Nil) {
				writer.nul(key);
			}
		}
	}


	private void write_VncBoolean(final String key, final VncBoolean val) {
		if (key == null) {
			if (VncBoolean.isTrue(val)) {
				writer.value(true);
			}
			else if (VncBoolean.isFalse(val)) {
				writer.value(false);
			}
		}
		else {
			if (VncBoolean.isTrue(val)) {
				writer.value(key, true);
			}
			else if (VncBoolean.isFalse(val)) {
				writer.value(key, false);
			}
		}
	}

	private void write_VncString(final String key, final VncString val) {
		final String v = val.getValue();
		if (key == null) {
			writer.value(v);
		}
		else {
			writer.value(key, v);
		}
	}

	private void write_VncChar(final String key, final VncChar val) {
		final String v = val.getValue().toString();
		if (key == null) {
			writer.value(v);
		}
		else {
			writer.value(key, v);
		}
	}

	private void write_VncInteger(final String key, final VncInteger val) {
		final int v = val.getValue().intValue();
		if (key == null) {
			writer.value(v);
		}
		else {
			writer.value(key, v);
		}
	}

	private void write_VncLong(final String key, final VncLong val) {
		final long v = val.getValue().longValue();
		if (key == null) {
			writer.value(v);
		}
		else {
			writer.value(key, v);
		}
	}

	private void write_VncDouble(final String key, final VncDouble val) {
		final double v = val.getValue().doubleValue();
		if (key == null) {
			writer.value(v);
		}
		else {
			writer.value(key, v);
		}
	}

	private void write_VncBigDecimal(final String key, final VncBigDecimal val) {
		if (decimalAsDouble) {
			final double v = val.getValue().doubleValue();
			if (key == null) {
				writer.value(v);
			}
			else {
				writer.value(key, v);
			}
		}
		else {
			final String v = val.getValue().toString();
			if (key == null) {
				writer.value(v);
			}
			else {
				writer.value(key, v);
			}
		}
	}

	private void write_VncBigInteger(final String key, final VncBigInteger val) {
		final String v = val.getValue().toString();
		if (key == null) {
			writer.value(v);
		}
		else {
			writer.value(key, v);
		}
	}

	private void write_VncKeyword(final String key, final VncKeyword val) {
		final String v = val.getValue();
		if (key == null) {
			writer.value(v);
		}
		else {
			writer.value(key, v);
		}
	}

	private void write_VncSymbol(final String key, final VncSymbol val) {
		final String v = val.getName();
		if (key == null) {
			writer.value(v);
		}
		else {
			writer.value(key, v);
		}
	}

	private void write_VncSequence(final String key, final VncSequence val) {
		final Iterator<VncVal> iter = val.iterator();

		array(key);
		while(iter.hasNext()) write(null, iter.next());
		end();
	}

	private void write_VncJavaList(final String key, final VncJavaList val) {
		writer.array(key, ((List<?>)val.getDelegate()));
	}

	private void write_VncSet(final String key, final VncSet val) {
		array(key);
		val.forEach(v -> write(null, v));
		end();
	}

	private void write_VncJavaSet(final String key, final VncJavaSet val) {
		writer.array(key, ((Set<?>)val.getDelegate()));
	}

	private void write_VncJavaMap(final String key, final VncJavaMap val) {
		if (key == null) {
			writer.object(((Map<?,?>)val.getDelegate()));
		}
		else {
			writer.object(key, ((Map<?,?>)val.getDelegate()));
		}
	}

	private void write_VncMap(final String key, final VncMap val) {
		object(key);

		final Map<VncVal,VncVal> map = val.getJavaMap();
		for(Entry<VncVal,VncVal> e : map.entrySet()) {
			final VncVal k = e.getKey();
			final VncVal v = e.getValue();
			if (Types.isVncString(k)) {
				write(((VncString)k).getValue(), v);
			}
			else if (Types.isVncKeyword(k)) {
				write(((VncKeyword)k).getValue(), v);
			}
			else if (Types.isVncLong(k)) {
				write(((VncLong)k).getValue().toString(), v);
			}
			else {
				throw new VncException(String.format(
						"Json serialization error: the map key type %s can not be serialized",
						Types.getType(val)));
			}
		}
		
		end();
	}

	private void write_VncJavaObject(final String key, final VncJavaObject val) {
		final Object delegate = val.getDelegate();
		if (delegate instanceof LocalDate) {
			final String formatted = ((LocalDate)delegate).format(FMT_LOCAL_DATE);
			if (key == null) {
				writer.value(formatted);
			}
			else {
				writer.value(key, formatted);
			}
		}
		else if (delegate instanceof LocalDateTime) {
			final String formatted = ((LocalDateTime)delegate).format(FMT_LOCAL_DATE_TIME);
			if (key == null) {
				writer.value(formatted);
			}
			else {
				writer.value(key, formatted);
			}
		}
		else if (delegate instanceof ZonedDateTime) {
			final String formatted = ((ZonedDateTime)delegate).format(FMT_DATE_TIME);
			if (key == null) {
				writer.value(formatted);
			}
			else {
				writer.value(key, formatted);
			}
		}
		else {
			throw new VncException(String.format(
					"Json serialization error: the type %s can not be serialized",
					Types.getType(val)));
		}
	}

	private void write_VncByteBuffer(final String key, final VncByteBuffer val) {
		final String encoded = Base64.getEncoder().encodeToString(val.getBytes());

		if (key == null) {
			writer.value(encoded);
		}
		else {
			writer.value(key, encoded);
		}
	}

	private void array(final String key) {
		if (key == null) {
			writer.array();
		}
		else {
			writer.array(key);
		}
	}

	private void object(final String key) {
		if (key == null) {
			writer.object();
		}
		else {
			writer.object(key);
		}
	}

	private void end() {
		writer.end();
	}

	
	private static final DateTimeFormatter FMT_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter FMT_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	private static final DateTimeFormatter FMT_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private final JsonAppendableWriter writer;
	private final boolean decimalAsDouble;
}
