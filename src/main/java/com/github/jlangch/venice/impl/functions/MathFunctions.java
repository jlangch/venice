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

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.github.jlangch.venice.ArityException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class MathFunctions {

	public static VncFunction add = new VncFunction("+") {
		{
			setArgLists("(+)", "(+ x)", "(+ x y)", "(+ x y & more)");
			
			setDoc("Returns the sum of the numbers. (+) returns 0.");
			
			setExamples("(+)", "(+ 1)", "(+ 1 2)", "(+ 1 2 3 4)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				return new VncLong(0);
			}
			else if (args.size() == 1) {
				return args.nth(0);
			}

			VncVal val = args.first();
			for(VncVal v : args.slice(1).getList()) { val = Numeric.add(val, v); }
			return val;
		}
	};
	
	public static VncFunction subtract = new VncFunction("-") {
		{
			setArgLists("(- x)", "(- x y)", "(- x y & more)");
			
			setDoc( "If one number is supplied, returns the negation, else subtracts " +
					"the numbers from x and returns the result.");
			
			setExamples("(- 4)", "(- 8 3 -2 -1)", "(- 8 2.5)", "(- 8 1.5M)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(args, 0, "-");
			}
			else if (args.size() == 1) {
				final VncVal first = args.nth(0);
				if (Types.isVncLong(first)) {
					return Numeric.mul(first, new VncLong(-1L));
				}
				else if (Types.isVncDouble(first)) {
					return Numeric.mul(first, new VncDouble(-1D));
				}
				else if (Types.isVncBigDecimal(first)) {
					return Numeric.mul(first, new VncBigDecimal(new BigDecimal("-1.0")));
				}
				else {
					return first;
				}
			}

			VncVal val = args.first();
			for(VncVal v : args.slice(1).getList()) { val = Numeric.sub(val, v); }
			return val;
		}
	};
	
	public static VncFunction multiply = new VncFunction("*") {
		{
			setArgLists("(*)", "(* x)", "(* x y)", "(* x y & more)");
			
			setDoc("Returns the product of numbers. (*) returns 1");
			
			setExamples("(*)", "(* 4)", "(* 4 3)", "(* 4 3 2)", "(* 6.0 2)", "(* 6 1.5M)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				return new VncLong(1);
			}
			else if (args.size() == 1) {
				return args.nth(0);
			}

			VncVal val = args.first();
			for(VncVal v : args.slice(1).getList()) { val = Numeric.mul(val, v); }
			return val;
		}
	};
	
	public static VncFunction divide = new VncFunction("/") {
		{
			setArgLists("(/ x)", "(/ x y)", "(/ x y & more)");
			
			setDoc( "If no denominators are supplied, returns 1/numerator, " + 
					"else returns numerator divided by all of the denominators.");
			
			setExamples("(/ 2.0)", "(/ 12 2 3)", "(/ 12 3)", "(/ 6.0 2)", "(/ 6 1.5M)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(args, 0, "/");
			}
			else if (args.size() == 1) {
				final VncVal first = args.nth(0);
				if (Types.isVncLong(first)) {
					return Numeric.div(new VncLong(1L), first);
				}
				else if (Types.isVncDouble(first)) {
					return Numeric.div(new VncDouble(1D), first);
				}
				else if (Types.isVncBigDecimal(first)) {
					return Numeric.div(new VncBigDecimal(BigDecimal.ONE), first);
				}
				else {
					return first;
				}
			}

			VncVal val = args.first();
			for(VncVal v : args.slice(1).getList()) { val = Numeric.div(val, v); }
			return val;
		}
	};
	
	public static VncFunction modulo = new VncFunction("mod") {
		{
			setArgLists("(mod n d)");
			
			setDoc("Modulus of n and d.");
			
			setExamples("(mod 10 4)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("mod", args, 2);

			if (!Types.isVncLong(args.nth(0))) {
				throw new VncException(String.format(
						"Function 'mod' does not allow %s as numerator. %s", 
						Types.getClassName(args.nth(0)),
						ErrorMessage.buildErrLocation(args)));
			}
			if (!Types.isVncLong(args.nth(1))) {
				throw new VncException(String.format(
						"Function 'mod' does not allow %s as denominator. %s", 
						Types.getClassName(args.nth(1)),
						ErrorMessage.buildErrLocation(args)));
			}
			
			return new VncLong(
						((VncLong)args.nth(0)).getValue().longValue() 
						% 
						((VncLong)args.nth(1)).getValue().longValue());
		}
	};
	
	public static VncFunction inc = new VncFunction("inc") {
		{
			setArgLists("(inc x)");
			
			setDoc("Increments the number x");
			
			setExamples("(inc 10)", "(inc 10.1)", "(inc 10.12M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("inc", args, 1);

			final VncVal arg = args.nth(0);
			if (Types.isVncLong(arg)) {
				return new VncLong(((VncLong)arg).getValue() + 1);
			}
			else if (Types.isVncDouble(arg)) {
				return new VncDouble(((VncDouble)arg).getValue() + 1D);
			}
			else if (Types.isVncBigDecimal(arg)) {
				return new VncBigDecimal(((VncBigDecimal)arg).getValue().add(new BigDecimal(1)));
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'inc'. %s",
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction dec = new VncFunction("dec") {
		{
			setArgLists("(dec x)");
			
			setDoc("Decrements the number x");
			
			setExamples("(dec 10)", "(dec 10.1)", "(dec 10.12M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec", args, 1);

			final VncVal arg = args.nth(0);
			if (Types.isVncLong(arg)) {
				return new VncLong(((VncLong)arg).getValue() - 1);
			}
			else if (Types.isVncDouble(arg)) {
				return new VncDouble(((VncDouble)arg).getValue() - 1D);
			}
			else if (Types.isVncBigDecimal(arg)) {
				return new VncBigDecimal(((VncBigDecimal)arg).getValue().subtract(new BigDecimal(1)));
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'dec'. %s",
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction max = new VncFunction("max") {
		{
			setArgLists("(max x)", "(max x y)", "(max x y & more)");
			
			setDoc("Returns the greatest of the values");
			
			setExamples(
					"(max 1)", "(max 1 2)", "(max 4 3 2 1)",
					"(max 1.0)", "(max 1.0 2.0)", "(max 4.0 3.0 2.0 1.0)",
					"(max 1.0M)", "(max 1.0M 2.0M)", "(max 4.0M 3.0M 2.0M 1.0M)",
					"(max 1.0M 2)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(args, 0, "max");
			}

			final VncVal op1 = args.nth(0);
			
			VncVal max = op1;
			for(VncVal op : args.rest().getList()) {
				if (Types.isVncNumber(op)) {
					max = op.compareTo(max) > 0 ? op : max;
				}
				else {
					throw new VncException(String.format(
											"Function 'max' does not allow %s as operand 1. %s", 
											Types.getClassName(max),
											ErrorMessage.buildErrLocation(args)));
				}
			}
			
			return max;			
		}
	};
	
	public static VncFunction min = new VncFunction("min") {
		{
			setArgLists("(min x)", "(min x y)", "(min x y & more)");
			
			setDoc("Returns the smallest of the values");
			
			setExamples(
					"(min 1)", "(min 1 2)", "(min 4 3 2 1)",
					"(min 1.0)", "(min 1.0 2.0)", "(min 4.0 3.0 2.0 1.0)",
					"(min 1.0M)", "(min 1.0M 2.0M)", "(min 4.0M 3.0M 2.0M 1.0M)",
					"(min 1.0M 2)");
		}
		
		public VncVal apply(final VncList args) {
			if (args.isEmpty()) {
				throw new ArityException(args, 0, "min");
			}
			
			final VncVal op1 = args.nth(0);
			
			VncVal min = op1;
			for(VncVal op : args.rest().getList()) {
				if (Types.isVncNumber(op)) {
					min = op.compareTo(min) < 0 ? op : min;
				}
				else {
					throw new VncException(String.format(
											"Function 'min' does not allow %s as operand 1. %s", 
											Types.getClassName(min),
											ErrorMessage.buildErrLocation(args)));
				}
			}
			
			return min;			
		}
	};
	
	public static VncFunction abs = new VncFunction("abs") {
		{
			setArgLists("(abs x)");
			
			setDoc("Returns the absolute value of the number");
			
			setExamples("(abs 10)", "(abs -10)", "(abs -10.1)", "(abs -10.12M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("abs", args, 1);
			
			final VncVal arg = args.nth(0);
			
			if (Types.isVncLong(arg)) {
				return new VncLong(Math.abs(((VncLong)arg).getValue().longValue()));
			}
			else if (Types.isVncDouble(arg)) {
				return new VncDouble(Math.abs(((VncDouble)arg).getValue().doubleValue()));
			}
			else if (Types.isVncBigDecimal(arg)) {
				return new VncBigDecimal(((VncBigDecimal)arg).getValue().abs());
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'abs'. %s",
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction negate = new VncFunction("negate") {
		{
			setArgLists("(negate x)");
			
			setDoc("Negates x");
			
			setExamples("(negate 10)", "(negate 1.23)", "(negate 1.23M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("negate", args, 1);
			
			final VncVal arg = args.nth(0);
			
			if (Types.isVncLong(arg)) {
				return new VncLong(Math.negateExact(((VncLong)arg).getValue().longValue()));
			}
			else if (Types.isVncDouble(arg)) {
				return new VncDouble(((VncDouble)arg).getValue().doubleValue() * -1D);
			}
			else if (Types.isVncBigDecimal(arg)) {
				return new VncBigDecimal(Coerce.toVncBigDecimal(args.first()).getValue().negate());
			}
			else {
				throw new VncException(String.format(
						"Invalid argument type %s while calling function 'negate'. %s",
						Types.getClassName(arg),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction rand_long = new VncFunction("rand-long") {
		{
			setArgLists("(rand-long)", "(rand-long max)");
			
			setDoc( "Without argument returns a random long between 0 and MAX_LONG. " +
					"Without argument max returns a random long between 0 and max exclusive.");
			
			setExamples("(rand-long)", "(rand-long 100)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("rand-long", args, 0, 1);
			
			if (args.isEmpty()) {
				return new VncLong(Math.abs(random.nextLong()));
			}
			else {
				final long max = Coerce.toVncLong(args.first()).getValue();
				if (max < 2) {
					throw new VncException("Function 'rand-long' does not allow negative max values");

				}
				return new VncLong(Math.abs(random.nextLong()) % max);
			}
		}
	};
	
	public static VncFunction rand_double = new VncFunction("rand-double") {
		{
			setArgLists("(rand-double)", "(rand-double max)");
			
			setDoc( "Without argument returns a double between 0.0 and 1.0. " +
					"Without argument max returns a random double between 0.0 and max.");
			
			setExamples("(rand-double)", "(rand-double 100.0)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("rand-double", args, 0, 1);
			
			if (args.isEmpty()) {
				return new VncDouble(random.nextDouble());
			}
			else {
				final double max = Coerce.toVncDouble(args.first()).getValue();
				if (max < 0.0) {
					throw new VncException(String.format(
							"Function 'rand-double' does not allow negative max values. %s",
							ErrorMessage.buildErrLocation(args)));

				}
				return new VncDouble(random.nextDouble() * max);
			}
		}
	};
	
	public static VncFunction rand_gaussian = new VncFunction("rand-gaussian") {
		{
			setArgLists("(rand-gaussian)", "(rand-gaussian mean stddev)");
			
			setDoc( "Without argument returns a Gaussion distributed double value with " +
					"mean 0.0 and standard deviation 1.0. " +
					"With argument mean and stddev returns a Gaussion distributed double " +
					"value with the given mean and standard deviation.");
			
			setExamples("(rand-gaussian)", "(rand-gaussian 0.0 5.0)");

		}
		
		public VncVal apply(final VncList args) {
			assertArity("rand-gaussian", args, 0, 2);
			
			if (args.isEmpty()) {
				return new VncDouble(random.nextGaussian());
			}
			else {
				final double mean = Coerce.toVncDouble(args.first()).getValue();
				final double stddev = Coerce.toVncDouble(args.second()).getValue();
				return new VncDouble(mean + stddev * random.nextGaussian());
			}
		}
	};

	public static VncFunction zero_Q = new VncFunction("zero?") {
		{
			setArgLists("(zero? x)");
			
			setDoc("Returns true if x zero else false");
			
			setExamples("(zero? 0)", "(zero? 2)", "(zero? 0.0)", "(zero? 0.0M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("zero?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() == 0 ? True : False;
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).getValue() == 0.0 ? True : False;
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).getValue().compareTo(BigDecimal.ZERO) == 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'zero' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction pos_Q = new VncFunction("pos?") {
		{
			setArgLists("(pos? x)");
			
			setDoc("Returns true if x greater than zero else false");
			
			setExamples("(pos? 3)", "(pos? -3)", "(pos? 3.2)", "(pos? 3.2M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("pos?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() > 0 ? True : False;
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).getValue() > 0 ? True : False;
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).getValue().compareTo(BigDecimal.ZERO) > 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'pos' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction neg_Q = new VncFunction("neg?") {
		{
			setArgLists("(neg? x)");
			
			setDoc("Returns true if x smaller than zero else false");
			
			setExamples("(neg? -3)", "(neg? 3)", "(neg? -3.2)", "(neg? -3.2M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("neg?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() < 0 ? True : False;
			}
			else if (Types.isVncDouble(op1)) {
				return ((VncDouble)op1).getValue() < 0 ? True : False;
			}
			else if (Types.isVncBigDecimal(op1)) {
				return ((VncBigDecimal)op1).getValue().compareTo(BigDecimal.ZERO) < 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'plus' does not allow %s as operand 1. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction even_Q = new VncFunction("even?") {
		{
			setArgLists("(even? n)");
			
			setDoc("Returns true if n is even, throws an exception if n is not an integer");
			
			setExamples("(even? 4)", "(even? 3)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("even?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() % 2 == 0 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'even' does not allow %s as operand. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction odd_Q = new VncFunction("odd?") {
		{
			setArgLists("(odd? n)");
			
			setDoc("Returns true if n is odd, throws an exception if n is not an integer");
			
			setExamples("(odd? 3)", "(odd? 4)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("odd?", args, 1);
			
			final VncVal op1 = args.nth(0);
			if (Types.isVncLong(op1)) {
				return ((VncLong)op1).getValue() % 2 == 1 ? True : False;
			}
			else {
				throw new VncException(String.format(
										"Function 'odd' does not allow %s as operand. %s", 
										Types.getClassName(op1),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	
	public static VncFunction dec_add = new VncFunction("dec/add") {
		{
			setArgLists("(dec/add x y scale rounding-mode)");
			
			setDoc( "Adds two decimals and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples("(dec/add 2.44697M 1.79882M 3 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/add", args, 4);

			final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.nth(0));
			final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.nth(1));
			final VncLong scale = Coerce.toVncLong(args.nth(2));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.nth(3)));
				
			return new VncBigDecimal(op1.getValue()
							.add(op2.getValue())
							.setScale(scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction dec_sub = new VncFunction("dec/sub") {
		{
			setArgLists("(dec/sub x y scale rounding-mode)");
			
			setDoc( "Subtract y from x and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples("(dec/sub 2.44697M 1.79882M 3 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/sub", args, 4);

			final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.nth(0));
			final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.nth(1));
			final VncLong scale = Coerce.toVncLong(args.nth(2));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.nth(3)));
				
			return new VncBigDecimal(op1.getValue().subtract(op2.getValue()).setScale(scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction dec_mul = new VncFunction("dec/mul") {
		{
			setArgLists("(dec/mul x y scale rounding-mode)");
			
			setDoc( "Multiplies two decimals and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples("(dec/mul 2.44697M 1.79882M 5 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/mul", args, 4);

			final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.nth(0));
			final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.nth(1));
			final VncLong scale = Coerce.toVncLong(args.nth(2));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.nth(3)));
				
			return new VncBigDecimal(op1.getValue().multiply(op2.getValue()).setScale(scale.getValue().intValue(), roundingMode));
		}
	};
	
	public static VncFunction dec_div = new VncFunction("dec/div") {
		{
			setArgLists("(dec/div x y scale rounding-mode)");
			
			setDoc( "Divides x by y and scales the result. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples("(dec/div 2.44697M 1.79882M 5 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/div", args, 4);

			final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.nth(0));
			final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.nth(1));
			final VncLong scale = Coerce.toVncLong(args.nth(2));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.nth(3)));
				
			return new VncBigDecimal(op1.getValue().divide(op2.getValue(), scale.getValue().intValue(), roundingMode));
		}
	};

	public static VncFunction dec_scale = new VncFunction("dec/scale") {
		{
			setArgLists("(dec/scale x scale rounding-mode)");
			
			setDoc( "Scales a decimal. rounding-mode is one of (:CEILING, :DOWN, " +
					":FLOOR, :HALF_DOWN, :HALF_EVEN, :HALF_UP, :UNNECESSARY, :UP)");
			
			setExamples(
					"(dec/scale 2.44697M 0 :HALF_UP)",
					"(dec/scale 2.44697M 1 :HALF_UP)",
					"(dec/scale 2.44697M 2 :HALF_UP)",
					"(dec/scale 2.44697M 3 :HALF_UP)",
					"(dec/scale 2.44697M 10 :HALF_UP)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("dec/scale", args, 3);

			final VncVal arg = args.nth(0);
			final VncLong scale = Coerce.toVncLong(args.nth(1));
			final RoundingMode roundingMode = VncBigDecimal.toRoundingMode((VncString)args.nth(2));
						
			if (Types.isVncBigDecimal(arg)) {
				final BigDecimal val = ((VncBigDecimal)arg).getValue();
				return new VncBigDecimal(val.setScale(scale.getValue().intValue(), roundingMode));
			}
			else {
				throw new VncException(String.format(
										"Function 'dec/scale' does not allow %s as operand 1. %s",
										Types.getClassName(arg),
										ErrorMessage.buildErrLocation(args)));
			}
		}
	};


	public static VncFunction range = new VncFunction("range") {
		{
			setArgLists("(range end)", "(range start end)", "(range start end step)");
			
			setDoc( "Returns a collection of numbers from start (inclusive) to end " + 
					"(exclusive), by step, where start defaults to 0 and step defaults to 1. " +
					"When start is equal to end, returns empty list.");
			
			setExamples(
					"(range 10)",
					"(range 10 20)",
					"(range 10 20 3)",
					"(range 10 15 0.5)",
					"(range 1.1M 2.2M 0.1M)");
		}
		
		public VncVal apply(final VncList args) {
			assertArity("range", args, 1, 2, 3);

			VncVal start = new VncLong(0);
			VncVal end = new VncLong(0);
			VncVal step = new VncLong(1);

			switch(args.size()) {
				case 1:
					end = args.nth(0);
					break;
				case 2:
					start = args.nth(0);
					end = args.nth(1);
					break;
				case 3:
					start = args.nth(0);
					end = args.nth(1);
					step = args.nth(2);
					break;
			}
						
			if (!Types.isVncNumber(start)) {
				throw new VncException(String.format(
						"range: start value must be a number. %s",
						ErrorMessage.buildErrLocation(args)));
			}
			if (!Types.isVncNumber(end)) {
				throw new VncException(String.format(
						"range: end value must be a number. %s",
						ErrorMessage.buildErrLocation(args)));	
			}
			if (!Types.isVncNumber(step)) {
				throw new VncException(String.format(
						"range: step value must be a number. %s",
						ErrorMessage.buildErrLocation(args)));	
			}

			final List<VncVal> values = new ArrayList<>();

			if (zero_Q.apply(new VncList(step)) == True) {
				throw new VncException(String.format(
						"range: a step value must not be 0. %s",
						ErrorMessage.buildErrLocation(args)));	
			}
			
			if (MathFunctions.pos_Q.apply(new VncList(step)) == True) {
				if (CoreFunctions.lt.apply(new VncList(end, start)) == True) {
					throw new VncException(String.format(
							"range positive step: end must not be lower than start. %s",
							ErrorMessage.buildErrLocation(args)));	
				}
				
				VncVal val = start;
				while(CoreFunctions.lt.apply(new VncList(val, end)) == True) {
					values.add(val);
					val = add.apply(new VncList(val, step));
				}
			}
			else {
				if (CoreFunctions.gt.apply(new VncList(end, start)) == True) {
					throw new VncException(String.format(
							"range negative step: end must not be greater than start. %s",
							ErrorMessage.buildErrLocation(args)));	
				}
				
				VncVal val = start;
				while(CoreFunctions.gt.apply(new VncList(val, end)) == True) {
					values.add(val);
					val = add.apply(new VncList(val, step));
				}
			}
			
			return new VncList(values);
		}
	};
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("+",					add)
					.put("-",					subtract)
					.put("*",					multiply)
					.put("/",					divide)
					.put("mod",					modulo)
					.put("inc",					inc)
					.put("dec",					dec)
					.put("abs",					abs)
					.put("min",					min)
					.put("max",					max)
					.put("negate",				negate)

					.put("dec/add",				dec_add)
					.put("dec/sub",				dec_sub)
					.put("dec/mul",				dec_mul)
					.put("dec/div",				dec_div)
					.put("dec/scale",			dec_scale)
					
					.put("zero?",				zero_Q)
					.put("pos?",				pos_Q)
					.put("neg?",				neg_Q)
					.put("even?",				even_Q)
					.put("odd?",				odd_Q)

					.put("rand-long",			rand_long)
					.put("rand-double",			rand_double)
					.put("rand-gaussian",		rand_gaussian)
					
					.put("range",				range)

					.toMap();	


	private static final Random random = new Random();
}
