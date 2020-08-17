/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
import java.math.BigInteger;
import java.math.RoundingMode;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBigInteger;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;


/**
 * Numeric operations
 * 
 * <p>type conversion between int, long, double, and decimal 
 * 
 * <p>mixed precision math for types int, long, double, and decimal with for +, -, *, /
 */
public class Numeric {

	
	// conversion ------------------------------------------------------------
	
	public static VncInteger toInteger(final VncVal val) {
		if (Types.isVncInteger(val)) {
			return (VncInteger)val;
		}
		else if (Types.isVncLong(val)) {
			return longToInt((VncLong)val);
		}
		else if (Types.isVncDouble(val)) {
			return doubleToInt((VncDouble)val);
		}
		else if (Types.isVncBigDecimal(val)) {
			return decimalToInt((VncBigDecimal)val);
		}
		else if (Types.isVncBigInteger(val)) {
			return bigintToInt((VncBigInteger)val);
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to long", 
					Types.getType(val)));
		}
	}
	
	public static VncLong toLong(final VncVal val) {
		if (Types.isVncLong(val)) {
			return (VncLong)val;
		}
		else if (Types.isVncInteger(val)) {
			return intToLong((VncInteger)val);
		}
		else if (Types.isVncDouble(val)) {
			return doubleToLong((VncDouble)val);
		}
		else if (Types.isVncBigDecimal(val)) {
			return decimalToLong((VncBigDecimal)val);
		}
		else if (Types.isVncBigInteger(val)) {
			return bigintToLong((VncBigInteger)val);
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
		else if (Types.isVncInteger(val)) {
			return intToDouble((VncInteger)val);
		}
		else if (Types.isVncDouble(val)) {
			return (VncDouble)val;
		}
		else if (Types.isVncBigDecimal(val)) {
			return decimalToDouble((VncBigDecimal)val);
		}
		else if (Types.isVncBigInteger(val)) {
			return bigintToDouble((VncBigInteger)val);
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
		else if (Types.isVncInteger(val)) {
			return intToDecimal((VncInteger)val);
		}
		else if (Types.isVncDouble(val)) {
			return doubleToDecimal((VncDouble)val);
		}
		else if (Types.isVncBigDecimal(val)) {
			return (VncBigDecimal)val;
		}
		else if (Types.isVncBigInteger(val)) {
			return bigintToDecimal((VncBigInteger)val);
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to decimal", 
					Types.getType(val)));
		}
	}

	public static VncBigInteger toBigint(final VncVal val) {
		if (Types.isVncLong(val)) {
			return longToBigint((VncLong)val);
		}
		else if (Types.isVncInteger(val)) {
			return intToBigint((VncInteger)val);
		}
		else if (Types.isVncDouble(val)) {
			return doubleToBigint((VncDouble)val);
		}
		else if (Types.isVncBigDecimal(val)) {
			return decimalToBigint((VncBigDecimal)val);
		}
		else if (Types.isVncBigInteger(val)) {
			return (VncBigInteger)val;
		}
		else {
			throw new VncException(String.format(
					"Cannot convert value of type %s to big integer", 
					Types.getType(val)));
		}
	}

	
	// integer ---------------------------------------------------------------
	
	public static VncLong intToLong(final VncInteger val) {
		return new VncLong(val.getValue());
	}

	public static VncDouble intToDouble(final VncInteger val) {
		return new VncDouble(val.getValue().doubleValue());
	}

	public static VncBigDecimal intToDecimal(final VncInteger val) {
		return new VncBigDecimal(new BigDecimal(val.getValue()));
	}

	public static VncBigDecimal intToDecimal(final VncInteger val, final int scale) {
		return new VncBigDecimal(new BigDecimal(val.getValue()).setScale(scale));
	}
	
	public static VncBigInteger intToBigint(final VncInteger val) {
		return new VncBigInteger(val.getValue());
	}

	
	// long ---------------------------------------------------------------
	
	public static VncInteger longToInt(final VncLong val) {
		return new VncInteger(val.getValue());
	}
	
	public static VncDouble longToDouble(final VncLong val) {
		return new VncDouble(val.getValue().doubleValue());
	}

	public static VncBigDecimal longToDecimal(final VncLong val) {
		return new VncBigDecimal(new BigDecimal(val.getValue()));
	}

	public static VncBigDecimal longToDecimal(final VncLong val, final int scale) {
		return new VncBigDecimal(new BigDecimal(val.getValue()).setScale(scale));
	}
	
	public static VncBigInteger longToBigint(final VncLong val) {
		return new VncBigInteger(val.getValue());
	}

	
	// double ---------------------------------------------------------------
	
	public static VncInteger doubleToInt(final VncDouble val) {
		return new VncInteger(val.getValue().intValue());
	}

	public static VncLong doubleToLong(final VncDouble val) {
		return new VncLong(val.getValue().longValue());
	}

	public static VncBigDecimal doubleToDecimal(final VncDouble val) {
		return new VncBigDecimal(new BigDecimal(val.getValue()));
	}
	
	public static VncBigInteger doubleToBigint(final VncDouble val) {
		return new VncBigInteger(val.getValue());
	}

	
	// big-decimal -----------------------------------------------------------
	
	public static VncInteger decimalToInt(final VncBigDecimal val) {
		return new VncInteger(val.getValue().intValue());
	}

	public static VncLong decimalToLong(final VncBigDecimal val) {
		return new VncLong(val.getValue().longValue());
	}

	public static VncDouble decimalToDouble(final VncBigDecimal val) {
		return new VncDouble(val.getValue().doubleValue());
	}

	public static VncBigInteger decimalToBigint(final VncBigDecimal val) {
		return new VncBigInteger(val.getValue().toBigInteger());
	}

	
	// big-integer -----------------------------------------------------------
	
	public static VncInteger bigintToInt(final VncBigInteger val) {
		return new VncInteger(val.getValue().intValue());
	}

	public static VncLong bigintToLong(final VncBigInteger val) {
		return new VncLong(val.getValue().longValue());
	}

	public static VncDouble bigintToDouble(final VncBigInteger val) {
		return new VncDouble(val.getValue().doubleValue());
	}

	public static VncBigDecimal bigintToDecimal(final VncBigInteger val) {
		return new VncBigDecimal(new BigDecimal(val.getValue()));
	}

	
	// operations ------------------------------------------------------------
	
	public static VncVal calc(final MathOp op, final VncVal op1, final VncVal op2) {
		try {
			if (Types.isVncLong(op1)) {
				if (Types.isVncLong(op2)) {
					return calcLong(op, ((VncLong)op1).getValue(), ((VncLong)op2).getValue());
				}
				else if (Types.isVncInteger(op2)) {
					return calcLong(op, ((VncLong)op1).getValue(), ((VncInteger)op2).getLongValue());
				}
				else if (Types.isVncDouble(op2)) {
					return calcDouble(op, longToDouble((VncLong)op1).getValue(), ((VncDouble)op2).getValue());
				}
				else if (Types.isVncBigDecimal(op2)) {
					return calcDecimal(op, longToDecimal((VncLong)op1).getValue(), ((VncBigDecimal)op2).getValue());
				}
				else if (Types.isVncBigInteger(op2)) {
					return calcBigInt(op, longToBigint((VncLong)op1).getValue(), ((VncBigInteger)op2).getValue());
				}
			}
			else if (Types.isVncDouble(op1)) {
				if (Types.isVncDouble(op2)) {
					return calcDouble(op, ((VncDouble)op1).getValue(), ((VncDouble)op2).getValue());
				}
				else if (Types.isVncLong(op2)) {
					return calcDouble(op, ((VncDouble)op1).getValue(), longToDouble((VncLong)op2).getValue());
				}
				else if (Types.isVncInteger(op2)) {
					return calcDouble(op, ((VncDouble)op1).getValue(), intToDouble((VncInteger)op2).getValue());
				}
				else if (Types.isVncBigDecimal(op2)) {
					return calcDecimal(op, doubleToDecimal((VncDouble)op1).getValue(), ((VncBigDecimal)op2).getValue());
				}
				else if (Types.isVncBigInteger(op2)) {
					return calcDecimal(op, doubleToDecimal((VncDouble)op1).getValue(), bigintToDecimal((VncBigInteger)op2).getValue());
				}
			}
			else if (Types.isVncBigDecimal(op1)) {
				if (Types.isVncBigDecimal(op2)) {
					return calcDecimal(op, ((VncBigDecimal)op1).getValue(), ((VncBigDecimal)op2).getValue());
				}
				else if (Types.isVncLong(op2)) {
					return calcDecimal(op, ((VncBigDecimal)op1).getValue(), longToDecimal((VncLong)op2).getValue());
				}
				else if (Types.isVncInteger(op2)) {
					return calcDecimal(op, ((VncBigDecimal)op1).getValue(), intToDecimal((VncInteger)op2).getValue());
				}
				else if (Types.isVncDouble(op2)) {
					return calcDecimal(op, ((VncBigDecimal)op1).getValue(), doubleToDecimal((VncDouble)op2).getValue());
				}
				else if (Types.isVncBigInteger(op2)) {
					return calcDecimal(op, ((VncBigDecimal)op1).getValue(), bigintToDecimal((VncBigInteger)op2).getValue());
				}
			}
			else if (Types.isVncBigInteger(op1)) {
				if (Types.isVncBigInteger(op2)) {
					return calcBigInt(op, ((VncBigInteger)op1).getValue(), ((VncBigInteger)op2).getValue());
				}
				else if (Types.isVncLong(op2)) {
					return calcBigInt(op, ((VncBigInteger)op1).getValue(), longToBigint((VncLong)op2).getValue());
				}
				else if (Types.isVncInteger(op2)) {
					return calcBigInt(op, ((VncBigInteger)op1).getValue(), intToBigint((VncInteger)op2).getValue());
				}
				else if (Types.isVncDouble(op2)) {
					return calcDecimal(op, bigintToDecimal((VncBigInteger)op1).getValue(), doubleToDecimal((VncDouble)op2).getValue());
				}
				else if (Types.isVncBigDecimal(op2)) {
					return calcDecimal(op, bigintToDecimal((VncBigInteger)op1).getValue(), ((VncBigDecimal)op2).getValue());
				}
			}
			else if (Types.isVncInteger(op1)) {
				if (Types.isVncInteger(op2)) {
					return calcInteger(op, ((VncInteger)op1).getValue(), ((VncInteger)op2).getValue());
				}
				else if (Types.isVncLong(op2)) {
					return calcLong(op, ((VncInteger)op1).getLongValue(), ((VncLong)op2).getValue());
				}
				else if (Types.isVncDouble(op2)) {
					return calcDouble(op, intToDouble((VncInteger)op1).getValue(), ((VncDouble)op2).getValue());
				}
				else if (Types.isVncBigDecimal(op2)) {
					return calcDecimal(op, intToDecimal((VncInteger)op1).getValue(), ((VncBigDecimal)op2).getValue());
				}
				else if (Types.isVncBigInteger(op2)) {
					return calcBigInt(op, intToBigint((VncInteger)op1).getValue(), ((VncBigInteger)op2).getValue());
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
	
	public static VncVal square(final VncVal val) {
		return calc(MathOp.MUL, val, val);
	}
	
	public static VncVal sqrt(final VncVal val) {
		if (Types.isVncLong(val)) {
			return new VncDouble(Math.sqrt(((VncLong)val).getValue().doubleValue()));
		}
		else if (Types.isVncInteger(val)) {
			return new VncDouble(Math.sqrt(((VncInteger)val).getValue().doubleValue()));
		}
		else if (Types.isVncDouble(val)) {
			return new VncDouble(Math.sqrt(((VncDouble)val).getValue()));
		}
		else if (Types.isVncBigDecimal(val)) {
			return new VncBigDecimal(
						new BigDecimal(
								Math.sqrt(
									Coerce.toVncBigDecimal(val).getValue().doubleValue())));
		}
		else if (Types.isVncBigInteger(val)) {
			return new VncBigDecimal(
						new BigDecimal(
								Math.sqrt(
									Coerce.toVncBigInteger(val).getValue().doubleValue())));
		}
		else {
			throw new VncException(String.format(
					"Invalid argument type %s while calling function 'sqrt'",
					Types.getType(val)));
		}
	}
	
	private static VncVal calcInteger(final MathOp op, final Integer op1, final Integer op2) {
		switch(op) {
			case EQU: return VncBoolean.of(op1.compareTo(op2) == 0);
			case ADD: return new VncInteger(op1 + op2);
			case SUB: return new VncInteger(op1 - op2);
			case MUL: return new VncInteger(op1 * op2);
			case DIV: return new VncInteger(op1 / op2);
			default: throw new RuntimeException("Invalid integer math operation '" + op + "'");
		}
	}
	
	private static VncVal calcLong(final MathOp op, final Long op1, final Long op2) {
		switch(op) {
			case EQU: return VncBoolean.of(op1.compareTo(op2) == 0);
			case ADD: return new VncLong(op1 + op2);
			case SUB: return new VncLong(op1 - op2);
			case MUL: return new VncLong(op1 * op2);
			case DIV: return new VncLong(op1 / op2);
			default: throw new RuntimeException("Invalid integer math operation '" + op + "'");
		}
	}

	private static VncVal calcDouble(final MathOp op, final Double op1, final Double op2) {
		switch(op) {
			case EQU: return VncBoolean.of(op1.compareTo(op2) == 0);
			case ADD: return new VncDouble(op1 + op2);
			case SUB: return new VncDouble(op1 - op2);
			case MUL: return new VncDouble(op1 * op2);
			case DIV: return new VncDouble(op1 / op2);
			default: throw new RuntimeException("Invalid double math operation '" + op + "'");
		}
	}

	private static VncVal calcDecimal(final MathOp op, final BigDecimal op1, final BigDecimal op2) {
		switch(op) {
			case EQU: return VncBoolean.of(op1.compareTo(op2) == 0);
			case ADD: return new VncBigDecimal(op1.add(op2));
			case SUB: return new VncBigDecimal(op1.subtract(op2));
			case MUL: return new VncBigDecimal(op1.multiply(op2));
			case DIV: return new VncBigDecimal(op1.divide(op2, 16, RoundingMode.HALF_UP));
			default: throw new RuntimeException("Invalid big decimal math operation '" + op + "'");
		}
	}

	private static VncVal calcBigInt(final MathOp op, final BigInteger op1, final BigInteger op2) {
		switch(op) {
			case EQU: return VncBoolean.of(op1.compareTo(op2) == 0);
			case ADD: return new VncBigInteger(op1.add(op2));
			case SUB: return new VncBigInteger(op1.subtract(op2));
			case MUL: return new VncBigInteger(op1.multiply(op2));
			case DIV: return new VncBigInteger(op1.divide(op2));
			default: throw new RuntimeException("Invalid big integer math operation '" + op + "'");
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
