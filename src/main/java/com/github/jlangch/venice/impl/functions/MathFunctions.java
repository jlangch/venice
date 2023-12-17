/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBigInteger;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncNumber;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncLazySeq;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


public class MathFunctions {

    public static VncFunction add =
        new VncFunction(
                "+",
                VncFunction
                    .meta()
                    .arglists("(+)", "(+ x)", "(+ x y)", "(+ x y & more)")
                    .doc("Returns the sum of the numbers. (+) returns 0.")
                    .examples(
                        "(+)",
                        "(+ 1)",
                        "(+ 1 2)",
                        "(+ 1 2 3 4)",
                        "(+ 1I 2I)",
                        "(+ 1 2.5)",
                        "(+ 1 2.5M)")
                    .seeAlso(
                        "-", "*", "/",
                        "dec/add", "dec/sub", "dec/mul", "dec/div", "dec/scale")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final int arity = args.size();

                if (arity == 0) {
                    return new VncLong(0L);
                }

                VncNumber n = validateNumber("+", args.first());

                if (arity == 1) {
                    return n;
                }
                else if (arity == 2) {
                    return n.add(args.second());
                }
                else {
                    for(VncVal v : args.rest()) { n = n.add(v); }
                    return n;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction subtract =
        new VncFunction(
                "-",
                VncFunction
                    .meta()
                    .arglists("(- x)", "(- x y)", "(- x y & more)")
                    .doc(
                        "If one number is supplied, returns the negation, else subtracts " +
                        "the numbers from x and returns the result.")
                    .examples(
                        "(- 4)",
                        "(- 8 3 -2 -1)",
                        "(- 5I 2I)",
                        "(- 8 2.5)",
                        "(- 8 1.5M)")
                    .seeAlso(
                            "+", "*", "/",
                            "dec/add", "dec/sub", "dec/mul", "dec/div", "dec/scale")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final int arity = args.size();

                if (arity == 0) {
                    return new VncLong(0L);
                }

                VncNumber n = validateNumber("-", args.first());

                if (arity == 1) {
                    return n.negate();
                }
                else if (arity == 2) {
                    return n.sub(args.second());
                }
                else {
                    for(VncVal v : args.rest()) { n = n.sub(v); }
                    return n;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction multiply =
        new VncFunction(
                "*",
                VncFunction
                    .meta()
                    .arglists("(*)", "(* x)", "(* x y)", "(* x y & more)")
                    .doc("Returns the product of numbers. (*) returns 1")
                    .examples(
                        "(*)",
                        "(* 4)",
                        "(* 4 3)",
                        "(* 4 3 2)",
                        "(* 4I 3I)",
                        "(* 6.0 2)",
                        "(* 6 1.5M)")
                    .seeAlso(
                            "+", "-", "/",
                            "dec/add", "dec/sub", "dec/mul", "dec/div", "dec/scale")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final int arity = args.size();

                if (arity == 0) {
                    return new VncLong(1L);
                }

                VncNumber n = validateNumber("*", args.first());

                if (arity == 1) {
                    return n;
                }
                else if (arity == 2) {
                    return n.mul(args.second());
                }
                else {
                    for(VncVal v : args.rest()) { n = n.mul(v); }
                    return n;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction divide =
        new VncFunction(
                "/",
                VncFunction
                    .meta()
                    .arglists("(/ x)", "(/ x y)", "(/ x y & more)")
                    .doc(
                        "If no denominators are supplied, returns 1/numerator, " +
                        "else returns numerator divided by all of the denominators.")
                    .examples(
                            "(/ 2.0)",
                            "(/ 12 2 3)",
                            "(/ 12 3)",
                            "(/ 12I 3I)",
                            "(/ 6.0 2)",
                            "(/ 6 1.5M)")
                    .seeAlso(
                            "+", "-", "*",
                            "dec/add", "dec/sub", "dec/mul", "dec/div", "dec/scale")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                final int arity = args.size();

                if (arity == 0) {
                    ArityExceptions.assertMinArity(this, args, 1);
                    return Nil;
                }

                VncNumber n = validateNumber("/", args.first());

                if (arity == 1) {
                    final VncVal first = args.first();
                    if (Types.isVncLong(first)) {
                        return new VncLong(1L).div(first);
                    }
                    else if (Types.isVncInteger(first)) {
                        return new VncInteger(1).div(first);
                    }
                    else if (Types.isVncDouble(first)) {
                        return new VncDouble(1D).div(first);
                    }
                    else if (Types.isVncBigDecimal(first)) {
                        return new VncBigDecimal(BigDecimal.ONE).div(first);
                    }
                    else if (Types.isVncBigInteger(first)) {
                        return new VncBigInteger(BigInteger.ONE).div(first);
                    }
                    else {
                        return n;
                    }
                }
                else if (arity == 2) {
                    return n.div(args.second());
                }
                else {
                    for(VncVal v : args.rest()) { n = n.div(v); }
                    return n;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction modulo =
        new VncFunction(
                "mod",
                VncFunction
                    .meta()
                    .arglists("(mod n d)")
                    .doc("Modulus of n and d.")
                    .examples(
                        "(mod 10 4)",
                        "(mod -1 5)",
                        "(mod 10I 4I)")
                    .seeAlso("mod-floor")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                try {
                    final VncVal n = args.first();
                    final VncVal d = args.second();

                    if (Types.isVncLong(n)) {
                        if (Types.isVncLong(d)) {
                            return new VncLong(
                                    Math.floorMod(
                                        ((VncLong)n).getValue().longValue(),
                                        ((VncLong)d).getValue().longValue()));
                        }
                        else {
                            throw new VncException(String.format(
                                    "Function 'mod' does not allow %s as denominator if nominator is a long",
                                    Types.getType(args.second())));
                        }
                    }
                    else if (Types.isVncInteger(n)) {
                        if (Types.isVncInteger(d)) {
                            return new VncInteger(
                                    Math.floorMod(
                                        ((VncInteger)n).getValue().intValue(),
                                        ((VncInteger)d).getValue().intValue()));
                        }
                        else {
                            throw new VncException(String.format(
                                    "Function 'mod' does not allow %s as denominator if nominator is an int",
                                    Types.getType(args.second())));
                        }
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'mod' does not allow %s as numerator."
                                    + "long and integer are supported only",
                                Types.getType(args.first())));
                    }
                }
                catch (ArithmeticException ex) {
                    throw new VncException(ex.getMessage());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction modulo_floor =
        new VncFunction(
                "mod-floor",
                VncFunction
                    .meta()
                    .arglists("(mod-floor n m)")
                    .doc("floor a number towards 0 to the nearest multiple of a number")
                    .examples(
                        "(mod-floor 9 3)",
                        "(mod-floor 10 3)",
                        "(mod-floor 11 3)",
                        "(mod-floor -11 3)")
                    .seeAlso("mod")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                try {
                    final VncVal n = args.first();
                    final VncVal m = args.second();

                    if (Types.isVncLong(n)) {
                        if (Types.isVncLong(m)) {
                            final long n_ = ((VncLong)n).getValue().longValue();
                            final long m_ = ((VncLong)m).getValue().longValue();

                            return new VncLong((n_ / m_) * m_);
                        }
                        else {
                            throw new VncException(String.format(
                                    "Function 'mod-floor' does not allow %s as multiple if number is a long",
                                    Types.getType(args.second())));
                        }
                    }
                    else if (Types.isVncInteger(n)) {
                        if (Types.isVncInteger(m)) {
                            final int n_ = ((VncInteger)n).getValue().intValue();
                            final int m_ = ((VncInteger)m).getValue().intValue();
                            return new VncInteger((n_ / m_) * m_);
                         }
                        else {
                            throw new VncException(String.format(
                                    "Function 'mod-floor' does not allow %s as multiple if number is an int",
                                    Types.getType(args.second())));
                        }
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'mod-floor' does not allow %s as number. "
                                    + "long and integer are supported only",
                                Types.getType(args.first())));
                    }
                }
                catch (ArithmeticException ex) {
                    throw new VncException(ex.getMessage());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction inc =
        new VncFunction(
                "inc",
                VncFunction
                    .meta()
                    .arglists("(inc x)")
                    .doc("Increments the number x")
                    .examples(
                        "(inc 10)",
                        "(inc 10I)",
                        "(inc 10.1)",
                        "(inc 10.12M)")
                    .seeAlso("dec")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg instanceof VncNumber) {
                    return ((VncNumber)arg).inc();
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'inc'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction dec =
        new VncFunction(
                "dec",
                VncFunction
                    .meta()
                    .arglists("(dec x)")
                    .doc("Decrements the number x")
                    .examples(
                        "(dec 10)",
                        "(dec 10I)",
                        "(dec 10.1)",
                        "(dec 10.12M)")
                    .seeAlso("inc")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg instanceof VncNumber) {
                    return ((VncNumber)arg).dec();
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'dec'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction nan_Q =
        new VncFunction(
                "nan?",
                VncFunction
                    .meta()
                    .arglists("(nan? x)")
                    .doc("Returns true if x is a NaN else false. x must be a double!")
                    .examples(
                        "(nan? 0.0)",
                        "(nan? (/ 0.0 0))",
                        "(nan? (sqrt -1))",
                        "(pr (sqrt -1))")
                    .seeAlso("infinite?", "double")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg instanceof VncDouble) {
                    return VncBoolean.of(((VncDouble)arg).getValue().isNaN());
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'nan?'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction infinite_Q =
        new VncFunction(
                "infinite?",
                VncFunction
                    .meta()
                    .arglists("(infinite? x)")
                    .doc("Returns true if x is infinite else false. x must be a double!")
                    .examples(
                        "(infinite? 1.0E300)",
                        "(infinite? (* 1.0E300 1.0E100))",
                        "(infinite? (/ 1.0 0))",
                        "(pr (/ 4.1 0))")
                    .seeAlso("nan?", "double")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();
                if (arg instanceof VncDouble) {
                    return VncBoolean.of(((VncDouble)arg).getValue().isInfinite());
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'infinite?'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction max =
        new VncFunction(
                "max",
                VncFunction
                    .meta()
                    .arglists("(max x)", "(max x y)", "(max x y & more)")
                    .doc("Returns the greatest of the values")
                    .examples(
                        "(max 1)", "(max 1 2)", "(max 4 3 2 1)",
                        "(max 1I 2I)",
                        "(max 1.0)", "(max 1.0 2.0)", "(max 4.0 3.0 2.0 1.0)",
                        "(max 1.0M)", "(max 1.0M 2.0M)", "(max 4.0M 3.0M 2.0M 1.0M)",
                        "(max 1.0M 2)")
                    .seeAlso("min")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                if (args.isEmpty()) {
                    return Nil;
                }

                VncVal max = args.first();
                for(VncVal op : args.rest()) {
                    if (Types.isVncNumber(op)) {
                        max = max == Nil
                                ? op
                                : (op.compareTo(max) > 0 ? op : max);
                    }
                    else if (op != Nil){
                        throw new VncException(String.format(
                                                "Function 'max' does not allow %s as operand",
                                                Types.getType(max)));
                    }
                }

                return max;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction min =
        new VncFunction(
                "min",
                VncFunction
                    .meta()
                    .arglists("(min x)", "(min x y)", "(min x y & more)")
                    .doc("Returns the smallest of the values")
                    .examples(
                        "(min 1)", "(min 1 2)", "(min 4 3 2 1)",
                        "(min 1I 2I)",
                        "(min 1.0)", "(min 1.0 2.0)", "(min 4.0 3.0 2.0 1.0)",
                        "(min 1.0M)", "(min 1.0M 2.0M)", "(min 4.0M 3.0M 2.0M 1.0M)",
                        "(min 1.0M 2)")
                    .seeAlso("max")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                if (args.isEmpty()) {
                    return Nil;
                }

                VncVal min = args.first();
                for(VncVal op : args.rest()) {
                    if (Types.isVncNumber(op)) {
                        min = min == Nil
                                ? op
                                : (op.compareTo(min) < 0 ? op : min);
                    }
                    else if (op != Nil) {
                        throw new VncException(String.format(
                                                "Function 'min' does not allow %s as operand",
                                                Types.getType(min)));
                    }
                }

                return min;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction abs =
        new VncFunction(
                "abs",
                VncFunction
                    .meta()
                    .arglists("(abs x)")
                    .doc("Returns the absolute value of the number")
                    .examples(
                        "(abs 10)",
                        "(abs -10)",
                        "(abs -10I)",
                        "(abs -10.1)",
                        "(abs -10.12M)")
                    .seeAlso("sgn", "negate")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (Types.isVncLong(arg)) {
                    return new VncLong(Math.abs(((VncLong)arg).getValue().longValue()));
                }
                else if (Types.isVncInteger(arg)) {
                    return new VncInteger(Math.abs(((VncInteger)arg).getValue().intValue()));
                }
                else if (Types.isVncDouble(arg)) {
                    return new VncDouble(Math.abs(((VncDouble)arg).getValue().doubleValue()));
                }
                else if (Types.isVncBigDecimal(arg)) {
                    return new VncBigDecimal(((VncBigDecimal)arg).getValue().abs());
                }
                else if (Types.isVncBigInteger(arg)) {
                    return new VncBigInteger(((VncBigInteger)arg).getValue().abs());
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'abs'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sgn =
        new VncFunction(
                "sgn",
                VncFunction
                    .meta()
                    .arglists("(sgn x)")
                    .doc(
                        "sgn function for a number. \n\n" +
                        "```\n" +
                        "-1 if x < 0 \n" +
                        " 0 if x = 0 \n" +
                        " 1 if x > 0 \n" +
                        "```")
                    .examples(
                        "(sgn -10)",
                        "(sgn 0)",
                        "(sgn 10)",
                        "(sgn -10I)",
                        "(sgn -10.1)",
                        "(sgn -10.12M)")
                    .seeAlso("abs", "negate")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (Types.isVncLong(arg)) {
                    final long x = ((VncLong)arg).getValue().longValue();
                    return new VncLong(x < 0L ? -1 : (x > 0L ? 1 :0));
                }
                else if (Types.isVncInteger(arg)) {
                    final int x = ((VncInteger)arg).getValue().intValue();
                    return new VncLong(x < 0 ? -1 : (x > 0 ? 1 :0));
                }
                else if (Types.isVncDouble(arg)) {
                    final double x = ((VncDouble)arg).getValue().doubleValue();
                    return new VncLong(x < 0.0 ? -1 : (x > 0.0 ? 1 :0));
                }
                else if (Types.isVncBigDecimal(arg)) {
                    return new VncLong(((VncBigDecimal)arg).getValue().signum());
                }
                else if (Types.isVncBigInteger(arg)) {
                    return new VncLong(((VncBigInteger)arg).getValue().signum());
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'signum'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction negate =
        new VncFunction(
                "negate",
                VncFunction
                    .meta()
                    .arglists("(negate x)")
                    .doc("Negates x")
                    .examples(
                        "(negate 10)",
                        "(negate 10I)",
                        "(negate 1.23)",
                        "(negate 1.23M)")
                    .seeAlso("abs", "sgn")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (arg instanceof VncNumber) {
                    return ((VncNumber)arg).negate();
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'negate'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction floor =
        new VncFunction(
                "floor",
                VncFunction
                    .meta()
                    .arglists("(floor x)")
                    .doc("Returns the largest integer that is less than or equal to x")
                    .examples(
                        "(floor 1.4)",
                        "(floor -1.4)",
                        "(floor 1.23M)",
                        "(floor -1.23M)")
                    .seeAlso("ceil")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (Types.isVncLong(arg)) {
                    return arg;
                }
                else if (Types.isVncInteger(arg)) {
                    return arg;
                }
                else if (Types.isVncDouble(arg)) {
                    return new VncDouble(Math.floor(((VncDouble)arg).getValue().doubleValue()));
                }
                else if (Types.isVncBigDecimal(arg)) {
                    BigDecimal val = ((VncBigDecimal)arg).getValue();
                    final int scale = val.scale();
                    val = val.setScale(0, RoundingMode.FLOOR);
                    val = val.setScale(scale, RoundingMode.FLOOR);
                    return new VncBigDecimal(val);
                }
                else if (Types.isVncBigInteger(arg)) {
                    return arg;
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'floor'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ceil =
        new VncFunction(
                "ceil",
                VncFunction
                    .meta()
                    .arglists("(ceil x)")
                    .doc("Returns the largest integer that is greater than or equal to x")
                    .examples(
                        "(ceil 1.4)",
                        "(ceil -1.4)",
                        "(ceil 1.23M)",
                        "(ceil -1.23M)")
                    .seeAlso("floor")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal arg = args.first();

                if (Types.isVncLong(arg)) {
                    return arg;
                }
                else if (Types.isVncInteger(arg)) {
                    return arg;
                }
                else if (Types.isVncDouble(arg)) {
                    return new VncDouble(Math.ceil(((VncDouble)arg).getValue().doubleValue()));
                }
                else if (Types.isVncBigDecimal(arg)) {
                    BigDecimal val = ((VncBigDecimal)arg).getValue();
                    final int scale = val.scale();
                    val = val.setScale(0, RoundingMode.CEILING);
                    val = val.setScale(scale, RoundingMode.CEILING);
                    return new VncBigDecimal(val);
                }
                else if (Types.isVncBigInteger(arg)) {
                    return arg;
                }
                else {
                    throw new VncException(String.format(
                            "Invalid argument type %s while calling function 'ceil'",
                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction square =
        new VncFunction(
                "square",
                VncFunction
                    .meta()
                    .arglists("(square x)")
                    .doc("Square of x")
                    .examples(
                        "(square 10)",
                        "(square 10I)",
                        "(square 10.23)",
                        "(square 10.23M)")
                    .seeAlso("sqrt")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return Coerce.toVncNumber(args.first()).square();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sqrt =
        new VncFunction(
                "sqrt",
                VncFunction
                    .meta()
                    .arglists("(sqrt x)")
                    .doc("Square root of x")
                    .examples(
                        "(sqrt 10)",
                        "(sqrt 10I)",
                        "(sqrt 10.23)",
                        "(sqrt 10.23M)",
                        "(sqrt 10N)")
                    .seeAlso("square")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return Coerce.toVncNumber(args.first()).sqrt();
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sin =
        new VncFunction(
                "math/sin",
                VncFunction
                    .meta()
                    .arglists("(math/sin x)")
                    .doc("Returns the trigonometric sine of an angle given in radians")
                    .examples(
                        "(math/sin (/ math/PI 3.0))")
                    .seeAlso("math/cos", "math/tan")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.sin(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction cos =
        new VncFunction(
                "math/cos",
                VncFunction
                    .meta()
                    .arglists("(math/cos x)")
                    .doc("Returns the trigonometric cosine of an angle given in radians")
                    .examples(
                        "(math/cos (/ math/PI 3.0))")
                    .seeAlso("math/sin", "math/tan")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.cos(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction tan =
        new VncFunction(
                "math/tan",
                VncFunction
                    .meta()
                    .arglists("(math/tan x)")
                    .doc("Returns the trigonometric tangent of an angle given in radians")
                    .examples(
                        "(math/tan (/ math/PI 3.0))")
                    .seeAlso("math/sin", "math/cos")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.tan(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction asin =
        new VncFunction(
                "math/asin",
                VncFunction
                    .meta()
                    .arglists("(math/asin x)")
                    .doc("Returns the arc sine of a value; the returned angle is "
                            + "in the range `-pi/2` through `pi/2`")
                    .examples(
                        "(math/asin 0.8660254037844386)")
                    .seeAlso("math/sin", "math/acos", "math/atan")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.asin(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction acos =
        new VncFunction(
                "math/acos",
                VncFunction
                    .meta()
                    .arglists("(math/acos x)")
                    .doc("Returns the arc cosine of a value; the returned angle is "
                            + "in the range `0.0` through `pi`")
                    .examples(
                        "(math/acos 0.5)")
                    .seeAlso("math/cos", "math/asin", "math/atan")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.acos(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction atan =
        new VncFunction(
                "math/atan",
                VncFunction
                    .meta()
                    .arglists("(math/atan x)")
                    .doc("Returns the arc tangent of a value; the returned angle is "
                            + "in the range `-pi/2` through `pi/2`.")
                    .examples(
                        "(math/atan 1.7320508075688767)")
                    .seeAlso("math/tan", "math/asin", "math/acos")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.atan(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction to_radians =
        new VncFunction(
                "math/to-radians",
                VncFunction
                    .meta()
                    .arglists("(math/to-radians x)")
                    .doc(
                        "Converts an angle measured in degrees to an approximately equivalent " +
                        "angle measured in radians. The conversion from degrees to radians " +
                        "is generally inexact.")
                    .examples(
                        "(math/to-radians 90)",
                        "(math/to-radians 90.0)",
                        "(math/to-radians 90.0M)")
                    .seeAlso("math/to-degrees")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.toRadians(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction to_degrees =
        new VncFunction(
                "math/to-degrees",
                VncFunction
                    .meta()
                    .arglists("(math/to-degrees x)")
                    .doc(
                        "Converts an angle measured in radians to an approximately equivalent " +
                        "angle measured in degrees. The conversion from radians to degrees is " +
                        "generally inexact; users should not expect (cos (to-radians 90.0)) " +
                        "to exactly equal 0.0")
                    .examples(
                        "(math/to-degrees 3)",
                        "(math/to-degrees 3.1415926)",
                        "(math/to-degrees 3.1415926M)")
                    .seeAlso("math/to-radians")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.toDegrees(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction log =
        new VncFunction(
                "log",
                VncFunction
                    .meta()
                    .arglists("(log x)")
                    .doc("Returns the natural logarithm (base e) of a value")
                    .examples(
                        "(log 10)",
                        "(log 10.23)",
                        "(log 10.23M)")
                    .seeAlso("log10", "log2")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.log(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction log2 =
        new VncFunction(
                "log2",
                VncFunction
                    .meta()
                    .arglists("(log2 x)")
                    .doc("Returns the base 2 logarithm of a value")
                    .examples(
                        "(log2 8)",
                        "(log2 10.23)",
                        "(log2 10.23M)")
                    .seeAlso("log", "log10")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final double n = VncDouble.of(args.first()).getValue();

                final double log2 = Math.log(n) / Math.log(2);

                return new VncDouble(log2);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction log10 =
        new VncFunction(
                "log10",
                VncFunction
                    .meta()
                    .arglists("(log10 x)")
                    .doc("Returns the base 10 logarithm of a value")
                    .examples(
                        "(log10 10)",
                        "(log10 10.23)",
                        "(log10 10.23M)",
                        ";; the number of digits\n" +
                        "(long (+ (floor (log10 235)) 1))")
                    .seeAlso("log", "log2")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.log10(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction exp =
        new VncFunction(
                "exp",
                VncFunction
                    .meta()
                    .arglists("(exp x)")
                    .doc("Returns Euler's number e raised to the power of a value.")
                    .examples(
                        "(exp 10)",
                        "(exp 10.23)",
                        "(exp 10.23M)")
                    .seeAlso("exp")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return new VncDouble(Math.exp(VncDouble.of(args.first()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pow =
        new VncFunction(
                "pow",
                VncFunction
                    .meta()
                    .arglists("(pow x y)")
                    .doc("Returns the value of x raised to the power of y")
                    .examples(
                        "(pow 10 2)",
                        "(pow 10.23 2)",
                        "(pow 10.23 2.5)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                return new VncDouble(Math.pow(
                                        VncDouble.of(args.first()).getValue(),
                                        VncDouble.of(args.second()).getValue()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction digits =
        new VncFunction(
                "digits",
                VncFunction
                    .meta()
                    .arglists("(digits x)")
                    .doc("Returns the number of digits of x. The number x must "+
                         "be of type integer, long, or bigint")
                    .examples(
                        "(digits 124)",
                        "(digits -10)",
                        "(digits 11111111111111111111111111111111N)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal val = args.first();

                if (Types.isVncInteger(val) || Types.isVncLong(val)) {
                    final long v = ((VncNumber)val).toJavaLong();
                    return new VncLong(
                            v == 0
                                ? 1
                                :((int)Math.floor(Math.log10(Math.abs(v)))) + 1);
                }
                else if (Types.isVncBigInteger(val)) {
                    final BigInteger v = ((VncBigInteger)val).getValue().abs();
                    if (v.equals(BigInteger.ZERO)) {
                        return new VncLong(1);
                    }
                    else {
                        final double factor = Math.log(2) / Math.log(10);
                        final int count = (int)(factor * v.bitLength() + 1);
                        return BigInteger.TEN.pow(count-1).compareTo(v) > 0
                                ? new VncLong(count -1)
                                : new VncLong(count);
                    }
                }
                else {
                    throw new VncException(String.format(
                            "Function 'digits' does not allow %s as value",
                            Types.getType(val)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mean =
        new VncFunction(
                "math/mean",
                VncFunction
                    .meta()
                    .arglists("(math/mean x)", "(math/mean x y)", "(math/mean x y & more)")
                    .doc("Returns the mean value of the values")
                    .examples(
                        "(math/mean 10 20 30)",
                        "(math/mean 1.4 3.6)",
                        "(math/mean 2.8M 6.4M)")
                    .seeAlso("math/median", "math/standard-deviation", "math/quantile", "math/quartiles")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                if (args.isEmpty()) {
                    return Nil;
                }
                else {
                    final VncNumber sum = (VncNumber)add.apply(args);

                    final VncNumber divisor = Types.isVncBigDecimal(sum) || Types.isVncBigInteger(sum)
                                                ? new VncBigDecimal(args.size())
                                                : new VncDouble(args.size());

                    return sum.div(divisor);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction standard_deviation =
        new VncFunction(
                "math/standard-deviation",
                VncFunction
                    .meta()
                    .arglists(
                        "(math/standard-deviation type coll)")
                    .doc(
                        "Returns the standard deviation of the values for data sample " +
                        "type `:population` or `:sample`.")
                    .examples(
                        "(math/standard-deviation :sample '(10 8 30 22 15))",
                        "(math/standard-deviation :population '(10 8 30 22 15))",
                        "(math/standard-deviation :sample '(1.4 3.6 7.8 9.0 2.2))",
                        "(math/standard-deviation :sample '(2.8M 6.4M 2.0M 4.4M))")
                    .seeAlso("math/mean", "math/median", "math/quantile", "math/quartiles")
                    .build()
        ) {
            // see: https://www.calculator.net/standard-deviation-calculator.html

            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final boolean sample = "sample".equals(Coerce.toVncKeyword(args.first()).getValue());
                final VncList data = Coerce.toVncList(args.second());

                if (data.isEmpty() || data.size() == 1) {
                    return new VncDouble(0.0);
                }
                else {
                    final VncNumber average = (VncNumber)mean.apply(data);

                    VncNumber deltaSum = new VncDouble(0.0);
                    for(VncVal v : data) {
                        final VncNumber diff = Coerce.toVncNumber(v).sub(average);
                        deltaSum = deltaSum.add(diff.mul(diff));
                    }

                    return VncDouble.of(
                                deltaSum.div(new VncDouble(sample ? data.size() -1 : data.size()))
                                        .sqrt());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction median =
        new VncFunction(
                "math/median",
                VncFunction
                    .meta()
                    .arglists(
                        "(math/median coll)")
                    .doc(
                        "Returns the median of the values")
                    .examples(
                        "(math/median '(3 1 2))",
                        "(math/median '(3 2 1 4))",
                        "(math/median '(3.6 1.4 4.8))",
                        "(math/median '(3.6M 1.4M 4.8M))")
                    .seeAlso("math/mean", "math/standard-deviation", "math/quantile", "math/quartiles")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncList data = Coerce.toVncList(args.first());

                if (data.isEmpty()) {
                    return Nil;
                }
                else {
                    return median((VncList)CoreFunctions.sort.apply(VncList.of(data)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction quartiles =
        // http://web.mnstate.edu/peil/MDEV102/U4/S36/S363.html

        new VncFunction(
                "math/quartiles",
                VncFunction
                    .meta()
                    .arglists(
                        "(math/quartiles coll)")
                    .doc(
                        "Returns the quartiles (1st, 2nd, and 3rd) of the values")
                    .examples(
                        "(math/quartiles '(3, 7, 8, 5, 12, 14, 21, 13, 18))",
                        "(math/quartiles '(3, 7, 8, 5, 12, 14, 21, 15, 18, 14))")
                    .seeAlso("math/mean", "math/median", "math/standard-deviation", "math/quantile")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncList list = Coerce.toVncList(args.first());

                if (list.size() < 2) {
                    return Nil;
                }
                else {
                    final VncList sorted = (VncList)CoreFunctions.sort.apply(VncList.of(list));

                    final VncList data = medianWithHalfs(sorted);

                    return VncList.of(
                            median((VncList)data.second()), // Q1: median lower half
                            data.first(),                   // Q2: median
                            median((VncList)data.third())); // Q3: median upper half
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction quantile =
        new VncFunction(
                "math/quantile",
                VncFunction
                    .meta()
                    .arglists(
                        "(math/quantile q coll)")
                    .doc(
                        "Returns the quantile [0.0 .. 1.0] of the values")
                    .examples(
                        "(math/quantile 0.5 '(3, 7, 8, 5, 12, 14, 21, 13, 18))",
                        "(math/quantile 0.5 '(3, 7, 8, 5, 12, 14, 21, 15, 18, 14))")
                    .seeAlso("math/mean", "math/median", "math/standard-deviation", "math/quartiles")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                // see: http://en.wikipedia.org/wiki/Quantile
                ArityExceptions.assertArity(this, args, 2);

                final double q = Coerce.toVncDouble(args.first()).getValue();
                final VncList list = Coerce.toVncList(args.second());

                if (list.size() < 2) {
                    return Nil;
                }
                else {
                    final VncList data = (VncList)CoreFunctions.sort.apply(VncList.of(list));

                    if (q < 0.0D || q > 1.0D) {
                        throw new VncException("A quantile q must be in the range 0.0 .. 1.0");
                    }

                    if (q == 0.0D) {
                        return data.first(); // minimum value
                    }
                    else if (q == 1.0D) {
                        return data.last();  // maximum value
                    }
                    else {
                        final int n = data.size() - 1;

                        final double x = q * n;

                        final double f = Math.floor(x);
                        final int idx = (int)f;
                        final double p = x - f;

                        final double res = (p * VncDouble.of(data.nth(idx + 1)).getValue())
                                            +
                                           ((1.0D - p) * VncDouble.of(data.nth(idx)).getValue());

                        return new VncDouble(res);
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction rand_long =
        new VncFunction(
                "rand-long",
                VncFunction
                    .meta()
                    .arglists(
                        "(rand-long)",
                        "(rand-long max)")
                    .doc(
                        "Without argument returns a random long between 0 and MAX_LONG. " +
                        "With argument max returns a random long between 0 and max exclusive.\n\n" +
                        "This function is based on a cryptographically strong random number " +
                        "generator (RNG).")
                    .examples(
                        "(rand-long)",
                        "(rand-long 100)")
                    .seeAlso(
                    	"rand-double",
                    	"rand-gaussian",
                    	"bytebuf-allocate-random")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

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

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction rand_double =
        new VncFunction(
                "rand-double",
                VncFunction
                    .meta()
                    .arglists(
                        "(rand-double)",
                        "(rand-double max)")
                    .doc(
                        "Without argument returns a double between 0.0 and 1.0. " +
                        "With argument max returns a random double between 0.0 and max.\n\n" +
                        "This function is based on a cryptographically strong random number " +
                        "generator (RNG).")
                    .examples(
                        "(rand-double)",
                        "(rand-double 100.0)")
                    .seeAlso(
                    	"rand-long",
                    	"rand-gaussian",
                    	"bytebuf-allocate-random")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

                if (args.isEmpty()) {
                    return new VncDouble(random.nextDouble());
                }
                else {
                    final double max = Coerce.toVncDouble(args.first()).getValue();
                    if (max < 0.0) {
                        throw new VncException(
                                "Function 'rand-double' does not allow negative max values");

                    }
                    return new VncDouble(random.nextDouble() * max);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction rand_gaussian =
        new VncFunction(
                "rand-gaussian",
                VncFunction
                    .meta()
                    .arglists(
                        "(rand-gaussian)",
                        "(rand-gaussian mean stddev)")
                    .doc(
                        "Without argument returns a Gaussian distributed double value with " +
                        "mean 0.0 and standard deviation 1.0. " +
                        "With argument mean and stddev returns a Gaussian distributed double " +
                        "value with the given mean and standard deviation.\n\n" +
                        "This function is based on a cryptographically strong random number " +
                        "generator (RNG)")
                    .examples(
                        "(rand-gaussian)",
                        "(rand-gaussian 0.0 5.0)")
                    .seeAlso(
                    	"rand-long",
                    	"rand-double",
                    	"bytebuf-allocate-random")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 2);

                if (args.isEmpty()) {
                    return new VncDouble(random.nextGaussian());
                }
                else {
                    final double mean = Coerce.toVncDouble(args.first()).getValue();
                    final double stddev = Coerce.toVncDouble(args.second()).getValue();
                    return new VncDouble(mean + stddev * random.nextGaussian());
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction zero_Q =
        new VncFunction(
                "zero?",
                VncFunction
                    .meta()
                    .arglists("(zero? x)")
                    .doc("Returns true if x zero else false")
                    .examples(
                        "(zero? 0)",
                        "(zero? 2)",
                        "(zero? (int 0))",
                        "(zero? 0.0)",
                        "(zero? 0.0M)")
                    .seeAlso("neg?", "pos?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal op = args.first();
                if (Types.isVncNumber(op)) {
                    return ((VncNumber)op).zeroQ();
                }
                else {
                    throw new VncException(String.format(
                                            "Function 'zero?' does not allow %s as operand 1",
                                            Types.getType(op)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction pos_Q =
        new VncFunction(
                "pos?",
                VncFunction
                    .meta()
                    .arglists("(pos? x)")
                    .doc("Returns true if x greater than zero else false")
                    .examples(
                        "(pos? 3)",
                        "(pos? -3)",
                        "(pos? (int 3))",
                        "(pos? 3.2)",
                        "(pos? 3.2M)")
                    .seeAlso("zero?", "neg?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal op = args.first();
                if (Types.isVncNumber(op)) {
                    return ((VncNumber)op).posQ();
                }
                else {
                    throw new VncException(String.format(
                                            "Function 'pos?' does not allow %s as operand 1",
                                            Types.getType(op)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction neg_Q =
        new VncFunction(
                "neg?",
                VncFunction
                    .meta()
                    .arglists("(neg? x)")
                    .doc("Returns true if x smaller than zero else false")
                    .examples(
                        "(neg? -3)",
                        "(neg? 3)",
                        "(neg? (int -3))",
                        "(neg? -3.2)",
                        "(neg? -3.2M)")
                    .seeAlso("zero?", "pos?", "negate")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal op = args.first();
                if (Types.isVncNumber(op)) {
                    return ((VncNumber)op).negQ();
                }
                else {
                    throw new VncException(String.format(
                                            "Function 'neg?' does not allow %s as operand 1s",
                                            Types.getType(op)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction even_Q =
        new VncFunction(
                "even?",
                VncFunction
                    .meta()
                    .arglists("(even? n)")
                    .doc("Returns true if n is even, throws an exception if n is not an integer")
                    .examples(
                        "(even? 4)",
                        "(even? 3)",
                        "(even? (int 3))")
                    .seeAlso("odd?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal op1 = args.first();
                if (Types.isVncLong(op1)) {
                    return VncBoolean.of(((VncLong)op1).getValue() % 2L == 0L);
                }
                else if (Types.isVncInteger(op1)) {
                    return VncBoolean.of(((VncInteger)op1).getValue() % 2 == 0);
                }
                else {
                    throw new VncException(String.format(
                                            "Function 'even?' does not allow %s as operand.",
                                            Types.getType(op1)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction odd_Q =
        new VncFunction(
                "odd?",
                VncFunction
                    .meta()
                    .arglists("(odd? n)")
                    .doc("Returns true if n is odd, throws an exception if n is not an integer")
                    .examples(
                        "(odd? 3)",
                        "(odd? 4)",
                        "(odd? (int 4))")
                    .seeAlso("even?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal op1 = args.first();
                if (Types.isVncLong(op1)) {
                    return VncBoolean.of(((VncLong)op1).getValue() % 2L != 0L);
                }
                else if (Types.isVncInteger(op1)) {
                    return VncBoolean.of(((VncInteger)op1).getValue() % 2 != 0);
                }
                else {
                    throw new VncException(String.format(
                                            "Function 'odd?' does not allow %s as operand",
                                            Types.getType(op1)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction dec_add =
        new VncFunction(
                "dec/add",
                VncFunction
                    .meta()
                    .arglists(
                        "(dec/add x y scale rounding-mode)")
                    .doc(
                        "Adds two decimals and scales the result. rounding-mode is " +
                        "one of `:CEILING`, `:DOWN,` `:FLOOR`, `:HALF_DOWN`, " +
                        "`:HALF_EVEN`, `:HALF_UP`, `:UNNECESSARY`, or `:UP`")
                    .examples(
                        "(dec/add 2.44697M 1.79882M 3 :HALF_UP)")
                    .seeAlso("dec/sub", "dec/mul", "dec/div", "dec/scale")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.first());
                final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.second());
                final VncLong scale = Coerce.toVncLong(args.third());
                final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.fourth()));

                return new VncBigDecimal(op1.getValue()
                                .add(op2.getValue())
                                .setScale(scale.getValue().intValue(), roundingMode));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction dec_sub =
        new VncFunction(
                "dec/sub",
                VncFunction
                    .meta()
                    .arglists(
                        "(dec/sub x y scale rounding-mode)")
                    .doc(
                        "Subtract y from x and scales the result. rounding-mode is " +
                        "one of `:CEILING`, `:DOWN,` `:FLOOR`, `:HALF_DOWN`, " +
                        "`:HALF_EVEN`, `:HALF_UP`, `:UNNECESSARY`, or `:UP`")
                    .examples(
                        "(dec/sub 2.44697M 1.79882M 3 :HALF_UP)")
                    .seeAlso("dec/add", "dec/mul", "dec/div", "dec/scale")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.first());
                final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.second());
                final VncLong scale = Coerce.toVncLong(args.third());
                final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.fourth()));

                return new VncBigDecimal(op1.getValue().subtract(op2.getValue()).setScale(scale.getValue().intValue(), roundingMode));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction dec_mul =
        new VncFunction(
                "dec/mul",
                VncFunction
                    .meta()
                    .arglists(
                        "(dec/mul x y scale rounding-mode)")
                    .doc(
                        "Multiplies two decimals and scales the result. rounding-mode is " +
                        "one of `:CEILING`, `:DOWN,` `:FLOOR`, `:HALF_DOWN`, " +
                        "`:HALF_EVEN`, `:HALF_UP`, `:UNNECESSARY`, or `:UP`")
                    .examples(
                        "(dec/mul 2.44697M 1.79882M 5 :HALF_UP)")
                    .seeAlso("dec/add", "dec/sub", "dec/div", "dec/scale")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.first());
                final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.second());
                final VncLong scale = Coerce.toVncLong(args.third());
                final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.fourth()));

                return new VncBigDecimal(op1.getValue().multiply(op2.getValue()).setScale(scale.getValue().intValue(), roundingMode));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction dec_div =
        new VncFunction(
                "dec/div",
                VncFunction
                    .meta()
                    .arglists(
                        "(dec/div x y scale rounding-mode)")
                    .doc(
                        "Divides x by y and scales the result. rounding-mode is " +
                        "one of `:CEILING`, `:DOWN,` `:FLOOR`, `:HALF_DOWN`, " +
                        "`:HALF_EVEN`, `:HALF_UP`, `:UNNECESSARY`, or `:UP`")
                    .examples(
                        "(dec/div 2.44697M 1.79882M 5 :HALF_UP)")
                    .seeAlso("dec/add", "dec/sub", "dec/mul", "dec/scale")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                final VncBigDecimal op1 = Coerce.toVncBigDecimal(args.first());
                final VncBigDecimal op2 = Coerce.toVncBigDecimal(args.second());
                final VncLong scale = Coerce.toVncLong(args.third());
                final RoundingMode roundingMode = VncBigDecimal.toRoundingMode(Coerce.toVncString(args.fourth()));

                return new VncBigDecimal(op1.getValue().divide(op2.getValue(), scale.getValue().intValue(), roundingMode));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction dec_scale =
        new VncFunction(
                "dec/scale",
                VncFunction
                    .meta()
                    .arglists(
                        "(dec/scale x scale rounding-mode)")
                    .doc(
                        "Scales a decimal. rounding-mode is " +
                        "one of `:CEILING`, `:DOWN,` `:FLOOR`, `:HALF_DOWN`, " +
                        "`:HALF_EVEN`, `:HALF_UP`, `:UNNECESSARY`, or `:UP`")
                    .examples(
                        "(dec/scale 2.44697M 0 :HALF_UP)",
                        "(dec/scale 2.44697M 1 :HALF_UP)",
                        "(dec/scale 2.44697M 2 :HALF_UP)",
                        "(dec/scale 2.44697M 3 :HALF_UP)",
                        "(dec/scale 2.44697M 10 :HALF_UP)")
                    .seeAlso("dec/add", "dec/sub", "dec/mul", "dec/div")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final VncVal arg = args.first();
                final VncLong scale = Coerce.toVncLong(args.second());
                final RoundingMode roundingMode = VncBigDecimal.toRoundingMode((VncString)args.nth(2));

                if (Types.isVncBigDecimal(arg)) {
                    final BigDecimal val = ((VncBigDecimal)arg).getValue();
                    return new VncBigDecimal(val.setScale(scale.getValue().intValue(), roundingMode));
                }
                else {
                    throw new VncException(String.format(
                                            "Function 'dec/scale' does not allow %s as operand 1s",
                                            Types.getType(arg)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction range =
        new VncFunction(
                "range",
                VncFunction
                    .meta()
                    .arglists(
                        "(range)",
                        "(range end)",
                        "(range start end)",
                        "(range start end step)")
                    .doc(
                        "Returns a collection of numbers from start (inclusive) to end " +
                        "(exclusive), by step, where start defaults to 0 and step defaults to 1. " +
                        "When start is equal to end, returns empty list. Without args returns a " +
                        "lazy sequence generating numbers starting with 0 and incrementing by 1.")
                    .examples(
                        "(range 10)",
                        "(range 10 20)",
                        "(range 10 20 3)",
                        "(range (int 10) (int 20))",
                        "(range (int 10) (int 20) (int 3))",
                        "(range 10 15 0.5)",
                        "(range 1.1M 2.2M 0.1M)",
                        "(range 100N 200N 10N)",
                        ";; capital letters \n" +
                        "(map char (range (int #\\A) (inc (int #\\Z))))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1, 2, 3);

                VncVal start = null;
                VncVal end = null;
                VncVal step = null;

                switch(args.size()) {
                    case 0:
                        return VncLazySeq.iterate(new VncLong(0), inc, Nil);
                    case 1:
                        start = new VncLong(0);
                        end = args.first();
                        step = new VncLong(1);
                        break;
                    case 2:
                        start = args.first();
                        end = args.second();
                        step = Types.isVncInteger(start) ? new VncInteger(1) : new VncLong(1);
                        break;
                    case 3:
                        start = args.first();
                        end = args.second();
                        step = args.third();
                        break;
                }

                if (!Types.isVncNumber(start)) {
                    throw new VncException("range: start value must be a number");
                }
                if (!Types.isVncNumber(end)) {
                    throw new VncException("range: end value must be a number");
                }
                if (!Types.isVncNumber(step)) {
                    throw new VncException("range: step value must be a number");
                }

                final List<VncVal> values = new ArrayList<>();

                if (VncBoolean.isTrue(zero_Q.apply(VncList.of(step)))) {
                    throw new VncException("range: a step value must not be 0");
                }

                if (VncBoolean.isTrue(MathFunctions.pos_Q.apply(VncList.of(step)))) {
                    if (VncBoolean.isTrue(CoreFunctions.lt.apply(VncList.of(end, start)))) {
                        throw new VncException("range positive step: end must not be lower than start");
                    }

                    VncVal val = start;
                    while(VncBoolean.isTrue(CoreFunctions.lt.apply(VncList.of(val, end)))) {
                        values.add(val);
                        val = add.apply(VncList.of(val, step));
                    }
                }
                else {
                    if (VncBoolean.isTrue(CoreFunctions.gt.apply(VncList.of(end, start)))) {
                        throw new VncException("range negative step: end must not be greater than start");
                    }

                    VncVal val = start;
                    while(VncBoolean.isTrue(CoreFunctions.gt.apply(VncList.of(val, end)))) {
                        values.add(val);
                        val = add.apply(VncList.of(val, step));
                    }
                }

                return VncList.ofList(values);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction softmax =
        new VncFunction(
                "math/softmax",
                VncFunction
                    .meta()
                    .arglists("(math/softmax coll)")
                    .doc("Softmax algorithm")
                    .examples("(math/softmax [3.2 1.3 0.2 0.8])")
                    .seeAlso()
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncSequence coll = Coerce.toVncSequence(args.first());
                if (coll.isEmpty()) {
                    return new VncDouble(Double.valueOf(0.0));
                }

                final double[] exponents = new double[coll.size()];
                double sum = 0.0D;
                int ii = 0;
                for(VncVal c : coll) {
                    double e = Math.exp(Coerce.toVncDouble(c).toJavaDouble());
                    exponents[ii++] = e;
                    sum += e;
                }

                final List<VncDouble> values = new ArrayList<>();
                for(int jj=0; jj<exponents.length; jj++) {
                    values.add(new VncDouble(exponents[jj] / sum));
                }

                return VncVector.ofColl(values);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    private static VncNumber validateNumber(final String fnName, final VncVal val) {
        if (!Types.isVncNumber(val)) {
            throw new VncException(String.format(
                    "Function %s operand of type %s is not a numeric type",
                    fnName,
                    Types.getType(val)));
        }

        return (VncNumber)val;
    }

    private static boolean isOdd(final int val) {
        return val % 2 == 1;
    }

    private static VncList medianWithHalfs(final VncList sortedData) {
        VncVal median;
        VncList lowerHalf;
        VncList upperHalf;

        if (isOdd(sortedData.size())) {
            // (3, 5, 7, 8), 12, (13, 14, 18, 21)
            median = median(sortedData);
            lowerHalf = sortedData.slice(0, sortedData.size() / 2);
            upperHalf = sortedData.slice((sortedData.size() / 2) + 1);
        }
        else {
            // (3, 5, 7, 8, 12), (14, 14, 15, 18, 21)
            median = median(sortedData);
            lowerHalf = sortedData.slice(0, (sortedData.size() / 2));
            upperHalf = sortedData.slice(sortedData.size() / 2);
        }

        return VncList.of(median, lowerHalf, upperHalf);
    }

    private static VncVal median(final VncList sortedData) {
        if (sortedData.isEmpty()) {
            return Nil;
        }
        else {
            if (isOdd(sortedData.size())) {
                final VncVal median = sortedData.nth(sortedData.size() / 2);
                return Types.isVncBigDecimal(median) || Types.isVncBigInteger(median) ? median : VncDouble.of(median);
            }
            else {
                final VncNumber lowerMedian = Coerce.toVncNumber(sortedData.nth(sortedData.size() / 2 - 1));
                final VncVal upperMedian = sortedData.nth(sortedData.size() / 2);
                final VncNumber sum = lowerMedian.add(upperMedian);

                final VncVal divisor = Types.isVncBigDecimal(sum) || Types.isVncBigInteger(sum)
                                            ? new VncBigDecimal(2L)
                                            : new VncDouble(2.0D);

                return sum.div(divisor);
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()

                    .put(new VncSymbol("math", "PI", null), new VncDouble(Math.PI))

                    // Euler's number
                    .put(new VncSymbol("math", "E", null), new VncDouble(Math.E))

                    // Speed of light km/s
                    .put(new VncSymbol("math", "c", null), new VncDouble(299792.458D))


                    .add(add)
                    .add(subtract)
                    .add(multiply)
                    .add(divide)
                    .add(modulo)
                    .add(modulo_floor)
                    .add(inc)
                    .add(dec)
                    .add(abs)
                    .add(sgn)
                    .add(min)
                    .add(max)
                    .add(negate)
                    .add(floor)
                    .add(ceil)

                    .add(square)
                    .add(sqrt)
                    .add(pow)

                    .add(to_radians)
                    .add(to_degrees)

                    .add(sin)
                    .add(cos)
                    .add(tan)
                    .add(asin)
                    .add(acos)
                    .add(atan)
                    .add(exp)
                    .add(log)
                    .add(log2)
                    .add(log10)

                    .add(mean)
                    .add(median)
                    .add(quartiles)
                    .add(quantile)
                    .add(standard_deviation)

                    .add(dec_add)
                    .add(dec_sub)
                    .add(dec_mul)
                    .add(dec_div)
                    .add(dec_scale)

                    .add(zero_Q)
                    .add(pos_Q)
                    .add(neg_Q)
                    .add(even_Q)
                    .add(odd_Q)
                    .add(nan_Q)
                    .add(infinite_Q)

                    .add(rand_long)
                    .add(rand_double)
                    .add(rand_gaussian)

                    .add(range)

                    .add(digits)

                    .add(softmax)

                    .toMap();


    private static final SecureRandom random = new SecureRandom();
}
