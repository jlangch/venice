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
package com.github.jlangch.venice.impl.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncBigDecimal extends VncNumber {

    public VncBigDecimal(final BigDecimal v) {
        this(v, null, Constants.Nil);
    }

    public VncBigDecimal(final double v) {
        this(BigDecimal.valueOf(v), null, Constants.Nil);
    }

    public VncBigDecimal(final long v) {
        this(BigDecimal.valueOf(v), null, Constants.Nil);
    }

    public VncBigDecimal(final BigDecimal v, final VncVal meta) {
        this(v, null, meta);
    }

    public VncBigDecimal(
            final BigDecimal v,
            final VncWrappingTypeDef wrappingTypeDef,
            final VncVal meta
    ) {
        super(wrappingTypeDef, meta);
        value = v;
    }


    public static VncBigDecimal of(final VncVal v) {
        if (Types.isVncNumber(v)) {
            return new VncBigDecimal(((VncNumber)v).toJavaBigDecimal());
        }
        else {
            throw new VncException(String.format(
                    "Cannot convert value of type %s to decimal",
                    Types.getType(v)));
        }
    }
    @Override
    public VncBigDecimal withMeta(final VncVal meta) {
        return new VncBigDecimal(value, getWrappingTypeDef(), meta);
    }

    @Override
    public VncBigDecimal wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
        return new VncBigDecimal(value, wrappingTypeDef, meta);
    }

    @Override
    public VncKeyword getType() {
        return isWrapped() ? new VncKeyword(
                                    getWrappingTypeDef().getType().getQualifiedName(),
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncBigDecimal.TYPE),
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)))
                           : new VncKeyword(
                                    VncBigDecimal.TYPE,
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public VncBigDecimal inc() {
        return new VncBigDecimal(value.add(BigDecimal.ONE));
    }

    @Override
    public VncBigDecimal dec() {
        return new VncBigDecimal(value.subtract(BigDecimal.ONE));
    }

    @Override
    public VncBigDecimal negate() {
        return new VncBigDecimal(value.negate());
    }

    @Override
    public VncNumber add(final VncVal op) {
        if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(value.add(((VncBigDecimal)op).value));
        }
        else if (op instanceof VncLong) {
            return new VncBigDecimal(value.add(((VncLong)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncInteger) {
            return new VncBigDecimal(value.add(((VncInteger)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncDouble) {
            return new VncBigDecimal(value.add(((VncDouble)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigInteger) {
            return new VncBigDecimal(value.add(((VncBigInteger)op).toJavaBigDecimal()));
        }
        else {
            throw new VncException(String.format(
                    "Function + operand of type %s is not a numeric type",
                    Types.getType(op)));
        }
    }

    @Override
    public VncNumber sub(final VncVal op) {
        if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(value.subtract(((VncBigDecimal)op).value));
        }
        else if (op instanceof VncLong) {
            return new VncBigDecimal(value.subtract(((VncLong)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncInteger) {
            return new VncBigDecimal(value.subtract(((VncInteger)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncDouble) {
            return new VncBigDecimal(value.subtract(((VncDouble)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigInteger) {
            return new VncBigDecimal(value.subtract(((VncBigInteger)op).toJavaBigDecimal()));
        }
        else {
            throw new VncException(String.format(
                    "Function - operand of type %s is not a numeric type",
                    Types.getType(op)));
        }
    }

    @Override
    public VncNumber mul(final VncVal op) {
        if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(value.multiply(((VncBigDecimal)op).value));
        }
        else if (op instanceof VncLong) {
            return new VncBigDecimal(value.multiply(((VncLong)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncInteger) {
            return new VncBigDecimal(value.multiply(((VncInteger)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncDouble) {
            return new VncBigDecimal(value.multiply(((VncDouble)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigInteger) {
            return new VncBigDecimal(value.multiply(((VncBigInteger)op).toJavaBigDecimal()));
        }
        else {
            throw new VncException(String.format(
                    "Function * operand of type %s is not a numeric type",
                    Types.getType(op)));
        }
    }

    @Override
    public VncNumber div(final VncVal op) {
        try {
            if (op instanceof VncBigDecimal) {
                return new VncBigDecimal(value.divide(((VncBigDecimal)op).value, 16, RoundingMode.HALF_UP));
            }
            else if (op instanceof VncLong) {
                return new VncBigDecimal(value.divide(((VncLong)op).toJavaBigDecimal(), 16, RoundingMode.HALF_UP));
            }
            else if (op instanceof VncInteger) {
                return new VncBigDecimal(value.divide(((VncInteger)op).toJavaBigDecimal(), 16, RoundingMode.HALF_UP));
            }
            else if (op instanceof VncDouble) {
                return new VncBigDecimal(value.divide(((VncDouble)op).toJavaBigDecimal(), 16, RoundingMode.HALF_UP));
            }
            else if (op instanceof VncBigInteger) {
                return new VncBigDecimal(value.divide(((VncBigInteger)op).toJavaBigDecimal(), 16, RoundingMode.HALF_UP));
            }
            else {
                throw new VncException(String.format(
                        "Function / operand of type %s is not a numeric type",
                        Types.getType(op)));
            }
        }
        catch (ArithmeticException ex) {
            throw new VncException(ex.getMessage());
        }
    }

    @Override
    public VncBoolean equ(final VncVal other) {
        if (other instanceof VncBigDecimal) {
            return VncBoolean.of(value.compareTo(((VncBigDecimal)other).toJavaBigDecimal()) == 0);
        }
        else if (other instanceof VncLong) {
            return VncBoolean.of(value.compareTo(((VncLong)other).toJavaBigDecimal()) == 0);
        }
        else if (other instanceof VncInteger) {
            return VncBoolean.of(value.compareTo(((VncInteger)other).toJavaBigDecimal()) == 0);
        }
        else if (other instanceof VncDouble) {
            return VncBoolean.of(value.compareTo(((VncDouble)other).toJavaBigDecimal()) == 0);
        }
        else if (other instanceof VncBigInteger) {
            return VncBoolean.of(value.compareTo(((VncBigInteger)other).toJavaBigDecimal()) == 0);
        }
        else {
            throw new VncException(String.format(
                    "Function == operand of type %s is not a numeric type",
                    Types.getType(other)));
        }
    }

    @Override
    public VncBoolean zeroQ() {
        return VncBoolean.of(value.compareTo(BigDecimal.ZERO) == 0);
    }

    @Override
    public VncBoolean posQ() {
        return VncBoolean.of(value.compareTo(BigDecimal.ZERO) > 0);
    }

    @Override
    public VncBoolean negQ() {
        return VncBoolean.of(value.compareTo(BigDecimal.ZERO) < 0);
    }

    @Override
    public VncNumber square() {
        return new VncBigDecimal(value.multiply(value));
    }

    @Override
    public VncNumber sqrt() {
        return new VncBigDecimal(new BigDecimal(Math.sqrt(toJavaDouble())));
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.BIGDECIMAL;
    }

    @Override
    public Object convertToJavaObject() {
        return value;
    }

    @Override
    public int toJavaInteger() {
        return value.intValue();
    }

    @Override
    public long toJavaLong() {
        return value.longValue();
    }

    @Override
    public double toJavaDouble() {
        return value.doubleValue();
    }

    @Override
    public BigInteger toJavaBigInteger() {
        return value.toBigInteger();
    }

    @Override
    public BigDecimal toJavaBigDecimal() {
        return value;
    }

    @Override
    public BigDecimal toJavaBigDecimal(final int scale) {
        return value.setScale(scale);
    }

    @Override
    public int compareTo(final VncVal o) {
        if (Types.isVncBigDecimal(o)) {
            return value.compareTo(((VncBigDecimal)o).getValue());
        }
        else if (Types.isVncInteger(o)) {
            return value.compareTo(((VncInteger)o).toJavaBigDecimal());
        }
        else if (Types.isVncDouble(o)) {
            return value.compareTo(((VncDouble)o).toJavaBigDecimal());
        }
        else if (Types.isVncLong(o)) {
            return value.compareTo(((VncLong)o).toJavaBigDecimal());
        }
        else if (Types.isVncBigInteger(o)) {
            return value.compareTo(((VncBigInteger)o).toJavaBigDecimal());
        }
        else if (o == Constants.Nil) {
            return 1;
        }

        return super.compareTo(o);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (getClass() != obj.getClass()) {
            return false;
        }
        else {
            return value.equals(((VncBigDecimal)obj).value);
        }
    }

    @Override
    public String toString() {
        return value.toString() + "M";
    }

    public static RoundingMode toRoundingMode(final VncString val) {
        return RoundingMode.valueOf(RoundingMode.class, val.getValue());
    }


    public static final String TYPE = ":core/decimal";

    private static final long serialVersionUID = -1848883965231344442L;

    private final BigDecimal value;
}
