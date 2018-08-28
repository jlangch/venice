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

import java.math.RoundingMode;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;


/**
 * Mixed precision math
 * 
 * <p>types: long, double, and bigdecimal
 * <p>function: +, -, *, /
 * 
 */
public class Calc {

	public static VncVal add(final VncVal op1, final VncVal op2) {
		validateNumericTypes("+", op1, op2);
		
		if (Types.isVncLong(op1)) {
			if (Types.isVncLong(op2)) {
				return new VncLong(((VncLong)op1).getValue() + ((VncLong)op2).getValue());
			}
			else if (Types.isVncDouble(op2)) {
				return add(((VncLong)op1).toDouble(), op2);
			}
			else if (Types.isVncBigDecimal(op2)) {
				return add(((VncLong)op1).toDecimal(), op2);
			}
		}
		else if (Types.isVncDouble(op1)) {
			if (Types.isVncLong(op2)) {
				return add(op1, ((VncLong)op2).toDouble());
			}
			else if (Types.isVncDouble(op2)) {
				return new VncDouble(((VncDouble)op1).getValue() + ((VncDouble)op2).getValue());
			}
			else if (Types.isVncBigDecimal(op2)) {
				return add(((VncDouble)op1).toDecimal(), op2);
			}
		}
		else if (Types.isVncBigDecimal(op1)) {
			if (Types.isVncLong(op2)) {
				return add(op1, ((VncLong)op2).toDecimal());
			}
			else if (Types.isVncDouble(op2)) {
				return add(op1, ((VncDouble)op2).toDecimal());
			}
			else if (Types.isVncBigDecimal(op2)) {
				return new VncBigDecimal(((VncBigDecimal)op1).getValue().add(((VncBigDecimal)op2).getValue()));
			}
		}
		
		throw new RuntimeException("Unexpected outcome");
	}
	
	public static VncVal sub(final VncVal op1, final VncVal op2) {
		validateNumericTypes("-", op1, op2);
		
		if (Types.isVncLong(op1)) {
			if (Types.isVncLong(op2)) {
				return new VncLong(((VncLong)op1).getValue() - ((VncLong)op2).getValue());
			}
			else if (Types.isVncDouble(op2)) {
				return sub(((VncLong)op1).toDouble(), op2);
			}
			else if (Types.isVncBigDecimal(op2)) {
				return sub(((VncLong)op1).toDecimal(), op2);
			}
		}
		else if (Types.isVncDouble(op1)) {
			if (Types.isVncLong(op2)) {
				return sub(op1, ((VncLong)op2).toDouble());
			}
			else if (Types.isVncDouble(op2)) {
				return new VncDouble(((VncDouble)op1).getValue() - ((VncDouble)op2).getValue());
			}
			else if (Types.isVncBigDecimal(op2)) {
				return sub(((VncDouble)op1).toDecimal(), op2);
			}
		}
		else if (Types.isVncBigDecimal(op1)) {
			if (Types.isVncLong(op2)) {
				return sub(op1, ((VncLong)op2).toDecimal());
			}
			else if (Types.isVncDouble(op2)) {
				return sub(op1, ((VncDouble)op2).toDecimal());
			}
			else if (Types.isVncBigDecimal(op2)) {
				return new VncBigDecimal(((VncBigDecimal)op1).getValue().subtract(((VncBigDecimal)op2).getValue()));
			}
		}
		
		throw new RuntimeException("Unexpected outcome");
	}
	
	public static VncVal mul(final VncVal op1, final VncVal op2) {
		validateNumericTypes("*", op1, op2);
		
		if (Types.isVncLong(op1)) {
			if (Types.isVncLong(op2)) {
				return new VncLong(((VncLong)op1).getValue() * ((VncLong)op2).getValue());
			}
			else if (Types.isVncDouble(op2)) {
				return mul(((VncLong)op1).toDouble(), op2);
			}
			else if (Types.isVncBigDecimal(op2)) {
				return mul(((VncLong)op1).toDecimal(), op2);
			}
		}
		else if (Types.isVncDouble(op1)) {
			if (Types.isVncLong(op2)) {
				return mul(op1, ((VncLong)op2).toDouble());
			}
			else if (Types.isVncDouble(op2)) {
				return new VncDouble(((VncDouble)op1).getValue() * ((VncDouble)op2).getValue());
			}
			else if (Types.isVncBigDecimal(op2)) {
				return mul(((VncDouble)op1).toDecimal(), op2);
			}
		}
		else if (Types.isVncBigDecimal(op1)) {
			if (Types.isVncLong(op2)) {
				return mul(op1, ((VncLong)op2).toDecimal());
			}
			else if (Types.isVncDouble(op2)) {
				return mul(op1, ((VncDouble)op2).toDecimal());
			}
			else if (Types.isVncBigDecimal(op2)) {
				return new VncBigDecimal(((VncBigDecimal)op1).getValue().multiply(((VncBigDecimal)op2).getValue()));
			}
		}
		
		throw new RuntimeException("Unexpected outcome");
	}
	
	public static VncVal div(final VncVal op1, final VncVal op2) {
		validateNumericTypes("/", op1, op2);
		
		if (Types.isVncLong(op1)) {
			if (Types.isVncLong(op2)) {
				return new VncLong(((VncLong)op1).getValue() / ((VncLong)op2).getValue());
			}
			else if (Types.isVncDouble(op2)) {
				return div(((VncLong)op1).toDouble(), op2);
			}
			else if (Types.isVncBigDecimal(op2)) {
				return div(((VncLong)op1).toDecimal(), op2);
			}
		}
		else if (Types.isVncDouble(op1)) {
			if (Types.isVncLong(op2)) {
				return div(op1, ((VncLong)op2).toDouble());
			}
			else if (Types.isVncDouble(op2)) {
				return new VncDouble(((VncDouble)op1).getValue() / ((VncDouble)op2).getValue());
			}
			else if (Types.isVncBigDecimal(op2)) {
				return div(((VncDouble)op1).toDecimal(), op2);
			}
		}
		else if (Types.isVncBigDecimal(op1)) {
			if (Types.isVncLong(op2)) {
				return div(op1, ((VncLong)op2).toDecimal());
			}
			else if (Types.isVncDouble(op2)) {
				return div(op1, ((VncDouble)op2).toDecimal());
			}
			else if (Types.isVncBigDecimal(op2)) {
				return new VncBigDecimal(((VncBigDecimal)op1).getValue().divide(((VncBigDecimal)op2).getValue(), 16, RoundingMode.HALF_UP));
			}
		}
		
		throw new RuntimeException("Unexpected outcome");
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
