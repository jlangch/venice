/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package com.github.jlangch.venice.impl.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;


/**
 * Numeric operations
 * 
 * <p>type conversion between long, double, and decimal 
 * 
 * <p>mixed precision math for types long, double, and decimal with for +, -, *, /
 */
public class Numeric {
	
	public static VncLong toLong(final VncVal val) {
		if (Types.isVncLong(val)) {
			return (VncLong)val;
		}
		else if (Types.isVncDouble(val)) {
			return new VncLong(((VncDouble)val).getValue().longValue());
		}
		else if (Types.isVncBigDecimal(val)) {
			return new VncLong(((VncBigDecimal)val).getValue().longValue());
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to long", 
					Types.getClassName(val)));
		}
	}
	
	public static VncDouble toDouble(final VncVal val) {
		if (Types.isVncLong(val)) {
			return new VncDouble(((VncLong)val).getValue().doubleValue());
		}
		else if (Types.isVncDouble(val)) {
			return (VncDouble)val;
		}
		else if (Types.isVncBigDecimal(val)) {
			return new VncDouble(((VncBigDecimal)val).getValue().doubleValue());
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to double", 
					Types.getClassName(val)));
		}
	}

	public static VncBigDecimal toDecimal(final VncVal val) {
		if (Types.isVncLong(val)) {
			return new VncBigDecimal(new BigDecimal(((VncLong)val).getValue()));
		}
		else if (Types.isVncDouble(val)) {
			return new VncBigDecimal(new BigDecimal(((VncDouble)val).getValue()));
		}
		else if (Types.isVncBigDecimal(val)) {
			return (VncBigDecimal)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to decimal", 
					Types.getClassName(val)));
		}
	}

	public static VncVal add(final VncVal op1, final VncVal op2) {
		return calc('+', op1, op2);
	}
	
	public static VncVal sub(final VncVal op1, final VncVal op2) {
		return calc('-', op1, op2);
	}
	
	public static VncVal mul(final VncVal op1, final VncVal op2) {
		return calc('*', op1, op2);
	}
	
	public static VncVal div(final VncVal op1, final VncVal op2) {
		return calc('/', op1, op2);
	}

	
	private static VncVal calc(final char op, final VncVal op1, final VncVal op2) {
		validateNumericTypes("" + op, op1, op2);
		
		if (Types.isVncLong(op1)) {
			if (Types.isVncLong(op2)) {
				return calc(op, (VncLong)op1, (VncLong)op2);
			}
			else if (Types.isVncDouble(op2)) {
				return calc(op, Numeric.toDouble(op1), (VncDouble)op2);
			}
			else if (Types.isVncBigDecimal(op2)) {
				return calc(op, Numeric.toDecimal(op1), (VncBigDecimal)op2);
			}
		}
		else if (Types.isVncDouble(op1)) {
			if (Types.isVncLong(op2)) {
				return calc(op, (VncDouble)op1, Numeric.toDouble(op2));
			}
			else if (Types.isVncDouble(op2)) {
				return calc(op, (VncDouble)op1, (VncDouble)op2);
			}
			else if (Types.isVncBigDecimal(op2)) {
				return calc(op, Numeric.toDecimal(op1), (VncBigDecimal)op2);
			}
		}
		else if (Types.isVncBigDecimal(op1)) {
			if (Types.isVncLong(op2)) {
				return calc(op, (VncBigDecimal)op1, Numeric.toDecimal(op2));
			}
			else if (Types.isVncDouble(op2)) {
				return calc(op, (VncBigDecimal)op1, Numeric.toDecimal(op2));
			}
			else if (Types.isVncBigDecimal(op2)) {
				return calc(op, (VncBigDecimal)op1, (VncBigDecimal)op2);
			}
		}
		
		throw new RuntimeException("Unexpected outcome");
	}
	
	private static VncVal calc(final char op, final VncLong op1, final VncLong op2) {
		try {
			switch(op) {
				case '+': return new VncLong(op1.getValue() + op2.getValue());
				case '-': return new VncLong(op1.getValue() - op2.getValue());
				case '*': return new VncLong(op1.getValue() * op2.getValue());
				case '/': return new VncLong(op1.getValue() / op2.getValue());
				default: throw new RuntimeException("Invalid operation");
			}
		}
		catch (ArithmeticException ex) {
			throw new VncException(ex.getMessage(), ex);
		}
	}

	private static VncVal calc(final char op, final VncDouble op1, final VncDouble op2) {
		try {
			switch(op) {
				case '+': return new VncDouble(op1.getValue() + op2.getValue());
				case '-': return new VncDouble(op1.getValue() - op2.getValue());
				case '*': return new VncDouble(op1.getValue() * op2.getValue());
				case '/': return new VncDouble(op1.getValue() / op2.getValue());
				default: throw new RuntimeException("Invalid operation");
			}
		}
		catch (ArithmeticException ex) {
			throw new VncException(ex.getMessage(), ex);
		}
	}

	private static VncVal calc(final char op, final VncBigDecimal op1, final VncBigDecimal op2) {
		try {
			switch(op) {
				case '+': return new VncBigDecimal(op1.getValue().add(op2.getValue()));
				case '-': return new VncBigDecimal(op1.getValue().subtract(op2.getValue()));
				case '*': return new VncBigDecimal(op1.getValue().multiply(op2.getValue()));
				case '/': return new VncBigDecimal(op1.getValue().divide(op2.getValue(), 16, RoundingMode.HALF_UP));
				default: throw new RuntimeException("Invalid operation");
			}
		}
		catch (ArithmeticException ex) {
			throw new VncException(ex.getMessage(), ex);
		}
	}

	private static void validateNumericTypes(final String fn, final VncVal op1, final VncVal op2) {
		if (!Types.isVncNumber(op1)) {
			throw new VncException(String.format(
					"Function '%s' operand 1 (%s) is not a numeric type", 
					fn,
					Types.getClassName(op1)));
		}

		if (!Types.isVncNumber(op2)) {
			throw new VncException(String.format(
					"Function '%s' operand 2 (%s) is not a numeric type", 
					fn,
					Types.getClassName(op2)));
		}
	}
}
