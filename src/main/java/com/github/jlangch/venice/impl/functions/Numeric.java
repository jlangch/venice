/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;


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
			return doubleToLong((VncDouble)val);
		}
		else if (Types.isVncBigDecimal(val)) {
			return decimalToLong((VncBigDecimal)val);
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to long", 
					Types.getType(val)));
		}
	}
	
	public static VncDouble toDouble(final VncVal val) {
		if (Types.isVncLong(val)) {
			return longToDouble((VncLong)val);
		}
		else if (Types.isVncDouble(val)) {
			return (VncDouble)val;
		}
		else if (Types.isVncBigDecimal(val)) {
			return decimalToDouble((VncBigDecimal)val);
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to double", 
					Types.getType(val)));
		}
	}

	public static VncBigDecimal toDecimal(final VncVal val) {
		if (Types.isVncLong(val)) {
			return longToDecimal((VncLong)val);
		}
		else if (Types.isVncDouble(val)) {
			return doubleToDecimal((VncDouble)val);
		}
		else if (Types.isVncBigDecimal(val)) {
			return (VncBigDecimal)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to decimal", 
					Types.getType(val)));
		}
	}

	public static VncDouble longToDouble(final VncLong val) {
		return new VncDouble(val.getValue().doubleValue());
	}

	public static VncBigDecimal longToDecimal(final VncLong val) {
		return new VncBigDecimal(new BigDecimal(val.getValue()));
	}

	public static VncLong doubleToLong(final VncDouble val) {
		return new VncLong(val.getValue().longValue());
	}

	public static VncBigDecimal doubleToDecimal(final VncDouble val) {
		return new VncBigDecimal(new BigDecimal(val.getValue()));
	}

	public static VncLong decimalToLong(final VncBigDecimal val) {
		return new VncLong(val.getValue().longValue());
	}

	public static VncDouble decimalToDouble(final VncBigDecimal val) {
		return new VncDouble(val.getValue().doubleValue());
	}

	public static VncVal calc(final MathOp op, final VncVal op1, final VncVal op2) {
		try {
			if (Types.isVncLong(op1)) {
				if (Types.isVncLong(op2)) {
					return calcLong(op, (VncLong)op1, (VncLong)op2);
				}
				else if (Types.isVncDouble(op2)) {
					return calcDouble(op, longToDouble((VncLong)op1), (VncDouble)op2);
				}
				else if (Types.isVncBigDecimal(op2)) {
					return calcDecimal(op, longToDecimal((VncLong)op1), (VncBigDecimal)op2);
				}
			}
			else if (Types.isVncDouble(op1)) {
				if (Types.isVncDouble(op2)) {
					return calcDouble(op, (VncDouble)op1, (VncDouble)op2);
				}
				else if (Types.isVncLong(op2)) {
					return calcDouble(op, (VncDouble)op1, longToDouble((VncLong)op2));
				}
				else if (Types.isVncBigDecimal(op2)) {
					return calcDecimal(op, doubleToDecimal((VncDouble)op1), (VncBigDecimal)op2);
				}
			}
			else if (Types.isVncBigDecimal(op1)) {
				if (Types.isVncBigDecimal(op2)) {
					return calcDecimal(op, (VncBigDecimal)op1, (VncBigDecimal)op2);
				}
				else if (Types.isVncLong(op2)) {
					return calcDecimal(op, (VncBigDecimal)op1, longToDecimal((VncLong)op2));
				}
				else if (Types.isVncDouble(op2)) {
					return calcDecimal(op, (VncBigDecimal)op1, doubleToDecimal((VncDouble)op2));
				}
			}
			
			// error: bad numeric types - check add the end to minimize the type checks
			validateNumericTypes(op, op1, op2);
		}
		catch (ArithmeticException ex) {
			throw new VncException(ex.getMessage(), ex);
		}
		
		throw new RuntimeException("Unexpected outcome");
	}
	
	private static VncLong calcLong(final MathOp op, final VncLong op1, final VncLong op2) {
		switch(op) {
			case ADD: return new VncLong(op1.getValue() + op2.getValue());
			case SUB: return new VncLong(op1.getValue() - op2.getValue());
			case MUL: return new VncLong(op1.getValue() * op2.getValue());
			case DIV: return new VncLong(op1.getValue() / op2.getValue());
			default: throw new RuntimeException("Invalid integer math operation '" + op + "'");
		}
	}

	private static VncDouble calcDouble(final MathOp op, final VncDouble op1, final VncDouble op2) {
		switch(op) {
			case ADD: return new VncDouble(op1.getValue() + op2.getValue());
			case SUB: return new VncDouble(op1.getValue() - op2.getValue());
			case MUL: return new VncDouble(op1.getValue() * op2.getValue());
			case DIV: return new VncDouble(op1.getValue() / op2.getValue());
			default: throw new RuntimeException("Invalid double math operation '" + op + "'");
		}
	}

	private static VncBigDecimal calcDecimal(final MathOp op, final VncBigDecimal op1, final VncBigDecimal op2) {
		switch(op) {
			case ADD: return new VncBigDecimal(op1.getValue().add(op2.getValue()));
			case SUB: return new VncBigDecimal(op1.getValue().subtract(op2.getValue()));
			case MUL: return new VncBigDecimal(op1.getValue().multiply(op2.getValue()));
			case DIV: return new VncBigDecimal(op1.getValue().divide(op2.getValue(), 16, RoundingMode.HALF_UP));
			default: throw new RuntimeException("Invalid big decimal math operation '" + op + "'");
		}
	}

	private static void validateNumericTypes(final MathOp op, final VncVal op1, final VncVal op2) {
		if (!Types.isVncNumber(op1)) {
			throw new VncException(String.format(
					"Function '%s' operand 1 (%s) is not a numeric type", 
					op.getFnName(),
					Types.getType(op1)));
		}

		if (!Types.isVncNumber(op2)) {
			throw new VncException(String.format(
					"Function '%s' operand 2 (%s) is not a numeric type", 
					op.getFnName(),
					Types.getType(op2)));
		}
	}
	
}
