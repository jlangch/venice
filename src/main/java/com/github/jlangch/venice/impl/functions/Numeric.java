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
 */
public class Numeric {
	
	public static VncVal add(final VncVal op1, final VncVal op2) {
		try {
			if (op1 instanceof VncLong) {
				if (op2 instanceof VncLong) {
					return new VncLong(((VncLong)op1).toJavaLong() + ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncInteger) {
					return new VncLong(((VncLong)op1).toJavaLong() + ((VncInteger)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return new VncDouble(((VncLong)op1).toJavaDouble() + ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncLong)op1).toJavaBigDecimal().add(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigInteger(((VncLong)op1).toJavaBigInteger().add(((VncBigInteger)op2).toJavaBigInteger()));
				}
			}
			else if (op1 instanceof VncDouble) {
				if (op2 instanceof VncDouble) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() + ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncLong) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() + ((VncLong)op2).toJavaDouble());
				}
				else if (op2 instanceof VncInteger) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() + ((VncInteger)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncDouble)op1).toJavaBigDecimal().add(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigDecimal(((VncDouble)op1).toJavaBigDecimal().add(((VncBigInteger)op2).toJavaBigDecimal()));
				}
			}
			else if (op1 instanceof VncBigDecimal) {
				if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().add(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncLong) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().add(((VncLong)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncInteger) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().add(((VncInteger)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncDouble) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().add(((VncDouble)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().add(((VncBigInteger)op2).toJavaBigDecimal()));
				}
			}
			else if (op1 instanceof VncBigInteger) {
				if (op2 instanceof VncBigInteger) {
					return new VncBigInteger(((VncBigInteger)op1).toJavaBigInteger().add(((VncBigInteger)op2).toJavaBigInteger()));
				}
				else if (op2 instanceof VncLong) {
					return new VncBigInteger(((VncBigInteger)op1).toJavaBigInteger().add(((VncLong)op2).toJavaBigInteger()));
				}
				else if (op2 instanceof VncInteger) {
					return new VncBigInteger(((VncBigInteger)op1).toJavaBigInteger().add(((VncInteger)op2).toJavaBigInteger()));
				}
				else if (op2 instanceof VncDouble) {
					return new VncBigDecimal(((VncBigInteger)op1).toJavaBigDecimal().add(((VncDouble)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncBigInteger)op1).toJavaBigDecimal().add(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
			}
			else if (op1 instanceof VncInteger) {
				if (op2 instanceof VncInteger) {
					return new VncInteger(((VncInteger)op1).toJavaInteger() + ((VncInteger)op2).toJavaInteger());
				}
				else if (op2 instanceof VncLong) {
					return new VncLong(((VncInteger)op1).toJavaLong() + ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return new VncDouble(((VncInteger)op1).toJavaDouble() + ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncInteger)op1).toJavaBigDecimal().add(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigInteger(((VncInteger)op1).toJavaBigInteger().add(((VncBigInteger)op2).toJavaBigInteger()));
				}
			}
			
			// error: bad numeric types - check add the end to minimize the type checks
			validateNumericTypes("+", op1, op2);
		}
		catch (ArithmeticException ex) {
			throw new VncException(ex.getMessage());
		}
		
		throw new RuntimeException("Unexpected outcome");
	}

	public static VncVal sub(final VncVal op1, final VncVal op2) {
		try {
			if (op1 instanceof VncLong) {
				if (op2 instanceof VncLong) {
					return new VncLong(((VncLong)op1).toJavaLong() - ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncInteger) {
					return new VncLong(((VncLong)op1).toJavaLong() - ((VncInteger)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return new VncDouble(((VncLong)op1).toJavaDouble() - ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncLong)op1).toJavaBigDecimal().subtract(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigInteger(((VncLong)op1).toJavaBigInteger().subtract(((VncBigInteger)op2).toJavaBigInteger()));
				}
			}
			else if (op1 instanceof VncDouble) {
				if (op2 instanceof VncDouble) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() - ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncLong) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() - ((VncLong)op2).toJavaDouble());
				}
				else if (op2 instanceof VncInteger) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() - ((VncInteger)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncDouble)op1).toJavaBigDecimal().subtract(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigDecimal(((VncDouble)op1).toJavaBigDecimal().subtract(((VncBigInteger)op2).toJavaBigDecimal()));
				}
			}
			else if (op1 instanceof VncBigDecimal) {
				if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().subtract(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncLong) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().subtract(((VncLong)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncInteger) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().subtract(((VncInteger)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncDouble) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().subtract(((VncDouble)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().subtract(((VncBigInteger)op2).toJavaBigDecimal()));
				}
			}
			else if (op1 instanceof VncBigInteger) {
				if (op2 instanceof VncBigInteger) {
					return new VncBigInteger(((VncBigInteger)op1).toJavaBigInteger().subtract(((VncBigInteger)op2).toJavaBigInteger()));
				}
				else if (op2 instanceof VncLong) {
					return new VncBigInteger(((VncBigInteger)op1).toJavaBigInteger().subtract(((VncLong)op2).toJavaBigInteger()));
				}
				else if (op2 instanceof VncInteger) {
					return new VncBigInteger(((VncBigInteger)op1).toJavaBigInteger().subtract(((VncInteger)op2).toJavaBigInteger()));
				}
				else if (op2 instanceof VncDouble) {
					return new VncBigDecimal(((VncBigInteger)op1).toJavaBigDecimal().subtract(((VncDouble)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncBigInteger)op1).toJavaBigDecimal().subtract(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
			}
			else if (op1 instanceof VncInteger) {
				if (op2 instanceof VncInteger) {
					return new VncInteger(((VncInteger)op1).toJavaInteger() - ((VncInteger)op2).toJavaInteger());
				}
				else if (op2 instanceof VncLong) {
					return new VncLong(((VncInteger)op1).toJavaLong() - ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return new VncDouble(((VncInteger)op1).toJavaDouble() - ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncInteger)op1).toJavaBigDecimal().subtract(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigInteger(((VncInteger)op1).toJavaBigInteger().subtract(((VncBigInteger)op2).toJavaBigInteger()));
				}
			}
			
			// error: bad numeric types - check add the end to minimize the type checks
			validateNumericTypes("-", op1, op2);
		}
		catch (ArithmeticException ex) {
			throw new VncException(ex.getMessage());
		}
		
		throw new RuntimeException("Unexpected outcome");
	}
	public static VncVal mul(final VncVal op1, final VncVal op2) {
		try {
			if (op1 instanceof VncLong) {
				if (op2 instanceof VncLong) {
					return new VncLong(((VncLong)op1).toJavaLong() * ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncInteger) {
					return new VncLong(((VncLong)op1).toJavaLong() * ((VncInteger)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return new VncDouble(((VncLong)op1).toJavaDouble() * ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncLong)op1).toJavaBigDecimal().multiply(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigInteger(((VncLong)op1).toJavaBigInteger().multiply(((VncBigInteger)op2).toJavaBigInteger()));
				}
			}
			else if (op1 instanceof VncDouble) {
				if (op2 instanceof VncDouble) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() * ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncLong) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() * ((VncLong)op2).toJavaDouble());
				}
				else if (op2 instanceof VncInteger) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() * ((VncInteger)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncDouble)op1).toJavaBigDecimal().multiply(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigDecimal(((VncDouble)op1).toJavaBigDecimal().multiply(((VncBigInteger)op2).toJavaBigDecimal()));
				}
			}
			else if (op1 instanceof VncBigDecimal) {
				if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().multiply(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncLong) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().multiply(((VncLong)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncInteger) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().multiply(((VncInteger)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncDouble) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().multiply(((VncDouble)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigDecimal(((VncBigDecimal)op1).toJavaBigDecimal().multiply(((VncBigInteger)op2).toJavaBigDecimal()));
				}
			}
			else if (op1 instanceof VncBigInteger) {
				if (op2 instanceof VncBigInteger) {
					return new VncBigInteger(((VncBigInteger)op1).toJavaBigInteger().multiply(((VncBigInteger)op2).toJavaBigInteger()));
				}
				else if (op2 instanceof VncLong) {
					return new VncBigInteger(((VncBigInteger)op1).toJavaBigInteger().multiply(((VncLong)op2).toJavaBigInteger()));
				}
				else if (op2 instanceof VncInteger) {
					return new VncBigInteger(((VncBigInteger)op1).toJavaBigInteger().multiply(((VncInteger)op2).toJavaBigInteger()));
				}
				else if (op2 instanceof VncDouble) {
					return new VncBigDecimal(((VncBigInteger)op1).toJavaBigDecimal().multiply(((VncDouble)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncBigInteger)op1).toJavaBigDecimal().multiply(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
			}
			else if (op1 instanceof VncInteger) {
				if (op2 instanceof VncInteger) {
					return new VncInteger(((VncInteger)op1).toJavaInteger() * ((VncInteger)op2).toJavaInteger());
				}
				else if (op2 instanceof VncLong) {
					return new VncLong(((VncInteger)op1).toJavaLong() * ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return new VncDouble(((VncInteger)op1).toJavaDouble() * ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return new VncBigDecimal(((VncInteger)op1).toJavaBigDecimal().multiply(((VncBigDecimal)op2).toJavaBigDecimal()));
				}
				else if (op2 instanceof VncBigInteger) {
					return new VncBigInteger(((VncInteger)op1).toJavaBigInteger().multiply(((VncBigInteger)op2).toJavaBigInteger()));
				}
			}
			
			// error: bad numeric types - check add the end to minimize the type checks
			validateNumericTypes("*", op1, op2);
		}
		catch (ArithmeticException ex) {
			throw new VncException(ex.getMessage());
		}
		
		throw new RuntimeException("Unexpected outcome");
	}
	public static VncVal div(final VncVal op1, final VncVal op2) {
		try {
			if (op1 instanceof VncLong) {
				if (op2 instanceof VncLong) {
					return new VncLong(((VncLong)op1).toJavaLong() / ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncInteger) {
					return new VncLong(((VncLong)op1).toJavaLong() / ((VncInteger)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return new VncDouble(((VncLong)op1).toJavaDouble() / ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return div(((VncLong)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigInteger) {
					return div(((VncLong)op1).toJavaBigInteger(), ((VncBigInteger)op2).toJavaBigInteger());
				}
			}
			else if (op1 instanceof VncDouble) {
				if (op2 instanceof VncDouble) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() / ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncLong) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() / ((VncLong)op2).toJavaDouble());
				}
				else if (op2 instanceof VncInteger) {
					return new VncDouble(((VncDouble)op1).toJavaDouble() / ((VncInteger)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return div(((VncDouble)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigInteger) {
					return div(((VncDouble)op1).toJavaBigDecimal(), ((VncBigInteger)op2).toJavaBigDecimal());
				}
			}
			else if (op1 instanceof VncBigDecimal) {
				if (op2 instanceof VncBigDecimal) {
					return div(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncLong) {
					return div(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncLong)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncInteger) {
					return div(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncInteger)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncDouble) {
					return div(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncDouble)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigInteger) {
					return div(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncBigInteger)op2).toJavaBigDecimal());
				}
			}
			else if (op1 instanceof VncBigInteger) {
				if (op2 instanceof VncBigInteger) {
					return div(((VncBigInteger)op1).toJavaBigInteger(), ((VncBigInteger)op2).toJavaBigInteger());
				}
				else if (op2 instanceof VncLong) {
					return div(((VncBigInteger)op1).toJavaBigInteger(), ((VncLong)op2).toJavaBigInteger());
				}
				else if (op2 instanceof VncInteger) {
					return div(((VncBigInteger)op1).toJavaBigInteger(), ((VncInteger)op2).toJavaBigInteger());
				}
				else if (op2 instanceof VncDouble) {
					return div(((VncBigInteger)op1).toJavaBigDecimal(), ((VncDouble)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigDecimal) {
					return div(((VncBigInteger)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
			}
			else if (op1 instanceof VncInteger) {
				if (op2 instanceof VncInteger) {
					return new VncInteger(((VncInteger)op1).toJavaInteger() / ((VncInteger)op2).toJavaInteger());
				}
				else if (op2 instanceof VncLong) {
					return new VncLong(((VncInteger)op1).toJavaLong() / ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return new VncDouble(((VncInteger)op1).toJavaDouble() / ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return div(((VncInteger)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigInteger) {
					return div(((VncInteger)op1).toJavaBigInteger(), ((VncBigInteger)op2).toJavaBigInteger());
				}
			}
			
			// error: bad numeric types - check add the end to minimize the type checks
			validateNumericTypes("/", op1, op2);
		}
		catch (ArithmeticException ex) {
			throw new VncException(ex.getMessage());
		}
		
		throw new RuntimeException("Unexpected outcome");
	}
	
	public static VncBoolean equ(final VncVal op1, final VncVal op2) {
		try {
			if (op1 instanceof VncLong) {
				if (op2 instanceof VncLong) {
					return equ(((VncLong)op1).toJavaLong(), ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncInteger) {
					return equ(((VncLong)op1).toJavaLong(), ((VncInteger)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return equ(((VncLong)op1).toJavaDouble(), ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return equ(((VncLong)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigInteger) {
					return equ(((VncLong)op1).toJavaBigInteger(), ((VncBigInteger)op2).toJavaBigInteger());
				}
			}
			else if (op1 instanceof VncDouble) {
				if (op2 instanceof VncDouble) {
					return equ(((VncDouble)op1).toJavaDouble(), ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncLong) {
					return equ(((VncDouble)op1).toJavaDouble(), ((VncLong)op2).toJavaDouble());
				}
				else if (op2 instanceof VncInteger) {
					return equ(((VncDouble)op1).toJavaDouble(), ((VncInteger)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return equ(((VncDouble)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigInteger) {
					return equ(((VncDouble)op1).toJavaBigDecimal(), ((VncBigInteger)op2).toJavaBigDecimal());
				}
			}
			else if (op1 instanceof VncBigDecimal) {
				if (op2 instanceof VncBigDecimal) {
					return equ(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncLong) {
					return equ(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncLong)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncInteger) {
					return equ(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncInteger)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncDouble) {
					return equ(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncDouble)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigInteger) {
					return equ(((VncBigDecimal)op1).toJavaBigDecimal(), ((VncBigInteger)op2).toJavaBigDecimal());
				}
			}
			else if (op1 instanceof VncBigInteger) {
				if (op2 instanceof VncBigInteger) {
					return equ(((VncBigInteger)op1).toJavaBigInteger(), ((VncBigInteger)op2).toJavaBigInteger());
				}
				else if (op2 instanceof VncLong) {
					return equ(((VncBigInteger)op1).toJavaBigInteger(), ((VncLong)op2).toJavaBigInteger());
				}
				else if (op2 instanceof VncInteger) {
					return equ(((VncBigInteger)op1).toJavaBigInteger(), ((VncInteger)op2).toJavaBigInteger());
				}
				else if (op2 instanceof VncDouble) {
					return equ(((VncBigInteger)op1).toJavaBigDecimal(), ((VncDouble)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigDecimal) {
					return equ(((VncBigInteger)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
			}
			else if (op1 instanceof VncInteger) {
				if (op2 instanceof VncInteger) {
					return equ(((VncInteger)op1).toJavaInteger(), ((VncInteger)op2).toJavaInteger());
				}
				else if (op2 instanceof VncLong) {
					return equ(((VncInteger)op1).toJavaLong(), ((VncLong)op2).toJavaLong());
				}
				else if (op2 instanceof VncDouble) {
					return equ(((VncInteger)op1).toJavaDouble(), ((VncDouble)op2).toJavaDouble());
				}
				else if (op2 instanceof VncBigDecimal) {
					return equ(((VncInteger)op1).toJavaBigDecimal(), ((VncBigDecimal)op2).toJavaBigDecimal());
				}
				else if (op2 instanceof VncBigInteger) {
					return equ(((VncInteger)op1).toJavaBigInteger(), ((VncBigInteger)op2).toJavaBigInteger());
				}
			}
			
			// error: bad numeric types - check add the end to minimize the type checks
			validateNumericTypes("==", op1, op2);
		}
		catch (ArithmeticException ex) {
			throw new VncException(ex.getMessage());
		}
		
		throw new RuntimeException("Unexpected outcome");
	}

	public static VncVal square(final VncVal val) {
		return mul(val, val);
	}
	
	public static VncVal sqrt(final VncVal val) {
		if (val instanceof VncLong) {
			return new VncDouble(Math.sqrt(((VncLong)val).toJavaDouble()));
		}
		else if (val instanceof VncInteger) {
			return new VncDouble(Math.sqrt(((VncInteger)val).toJavaDouble()));
		}
		else if (val instanceof VncDouble) {
			return new VncDouble(Math.sqrt(((VncDouble)val).toJavaDouble()));
		}
		else if (val instanceof VncBigDecimal) {
			return new VncBigDecimal(
						new BigDecimal(
								Math.sqrt(
									Coerce.toVncBigDecimal(val).toJavaDouble())));
		}
		else if (val instanceof VncBigInteger) {
			return new VncBigDecimal(
						new BigDecimal(
								Math.sqrt(
									Coerce.toVncBigInteger(val).toJavaDouble())));
		}
		else {
			throw new VncException(String.format(
					"Invalid argument type %s while calling function 'sqrt'",
					Types.getType(val)));
		}
	}
	
	
	
	private static VncVal div(final BigDecimal op1, final BigDecimal op2) {
		return new VncBigDecimal(op1.divide(op2, 16, RoundingMode.HALF_UP));
	}
	
	private static VncVal div(final BigInteger op1, final BigInteger op2) {
		return new VncBigInteger(op1.divide(op2));
	}
	
	
	private static VncBoolean equ(final Integer op1, final Integer op2) {
		return VncBoolean.of(op1.equals(op2));
	}
	
	private static VncBoolean equ(final Long op1, final Long op2) {
		return VncBoolean.of(op1.equals(op2));
	}
	
	private static VncBoolean equ(final Double op1, final Double op2) {
		return VncBoolean.of(op1.equals(op2));
	}
	
	private static VncBoolean equ(final BigDecimal op1, final BigDecimal op2) {
		return VncBoolean.of(op1.compareTo(op2) == 0);
	}
	
	private static VncBoolean equ(final BigInteger op1, final BigInteger op2) {
		return VncBoolean.of(op1.compareTo(op2) == 0);
	}
	
	
	private static void validateNumericTypes(final String op, final VncVal op1, final VncVal op2) {
		if (!Types.isVncNumber(op1)) {
			throw new VncException(String.format(
					"Function '%s' operand 1 (%s) is not a numeric type", 
					op,
					Types.getType(op1)));
		}

		if (!Types.isVncNumber(op2)) {
			throw new VncException(String.format(
					"Function '%s' operand 2 (%s) is not a numeric type", 
					op,
					Types.getType(op2)));
		}
	}
	
}
