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


public class VncDouble extends VncNumber {

    public VncDouble(final Double v) {
        super(null, Constants.Nil);
        value = v;
    }

    public VncDouble(final Float v) {
        super(null, Constants.Nil);
        value = v.doubleValue();
    }

    public VncDouble(final Long v) {
        super(null, Constants.Nil);
        value = v.doubleValue();
    }

    public VncDouble(final Integer v) {
        super(null, Constants.Nil);
        value = v.doubleValue();
    }

    public VncDouble(final Double v, final VncVal meta) {
        super(null, meta);
        value = v;
    }

    public VncDouble(
            final Double v,
            final VncWrappingTypeDef wrappingTypeDef,
            final VncVal meta
    ) {
        super(wrappingTypeDef, meta);
        value = v;
    }


    public static VncDouble of(final VncVal v) {
        if (Types.isVncNumber(v)) {
            return new VncDouble(((VncNumber)v).toJavaDouble());
        }
        else {
            throw new VncException(String.format(
                    "Cannot convert value of type %s to double",
                    Types.getType(v)));
        }
    }


    @Override
    public VncDouble withMeta(final VncVal meta) {
        return new VncDouble(value, getWrappingTypeDef(), meta);
    }

    @Override
    public VncDouble wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
        return new VncDouble(value, wrappingTypeDef, meta);
    }

    @Override
    public VncKeyword getType() {
        return isWrapped() ? new VncKeyword(
                                    getWrappingTypeDef().getType().getQualifiedName(),
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncDouble.TYPE),
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)))
                           : new VncKeyword(
                                    VncDouble.TYPE,
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public VncDouble inc() {
        return new VncDouble(value + 1.0D);
    }

    @Override
    public VncDouble dec() {
        return new VncDouble(value - 1.0D);
    }

    @Override
    public VncDouble negate() {
        return new VncDouble(value * -1.0D);
    }

    @Override
    public VncNumber add(final VncVal op) {
        if (op instanceof VncDouble) {
            return new VncDouble(value + ((VncDouble)op).value);
        }
        else if (op instanceof VncLong) {
            return new VncDouble(value + ((VncLong)op).toJavaDouble());
        }
        else if (op instanceof VncInteger) {
            return new VncDouble(value + ((VncInteger)op).toJavaDouble());
        }
        else if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(toJavaBigDecimal().add(((VncBigDecimal)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigInteger) {
            return new VncBigDecimal(toJavaBigDecimal().add(((VncBigInteger)op).toJavaBigDecimal()));
        }
        else {
            throw new VncException(String.format(
                    "Function + operand of type %s is not a numeric type",
                    Types.getType(op)));
        }
    }

    @Override
    public VncNumber sub(final VncVal op) {
        if (op instanceof VncDouble) {
            return new VncDouble(value - ((VncDouble)op).value);
        }
        else if (op instanceof VncLong) {
            return new VncDouble(value - ((VncLong)op).toJavaDouble());
        }
        else if (op instanceof VncInteger) {
            return new VncDouble(value - ((VncInteger)op).toJavaDouble());
        }
        else if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(toJavaBigDecimal().subtract(((VncBigDecimal)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigInteger) {
            return new VncBigDecimal(toJavaBigDecimal().subtract(((VncBigInteger)op).toJavaBigDecimal()));
        }
        else {
            throw new VncException(String.format(
                    "Function - operand of type %s is not a numeric type",
                    Types.getType(op)));
        }
    }

    @Override
    public VncNumber mul(final VncVal op) {
        if (op instanceof VncDouble) {
            return new VncDouble(value * ((VncDouble)op).value);
        }
        else if (op instanceof VncLong) {
            return new VncDouble(value * ((VncLong)op).toJavaDouble());
        }
        else if (op instanceof VncInteger) {
            return new VncDouble(value * ((VncInteger)op).toJavaDouble());
        }
        else if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(toJavaBigDecimal().multiply(((VncBigDecimal)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigInteger) {
            return new VncBigDecimal(toJavaBigDecimal().multiply(((VncBigInteger)op).toJavaBigDecimal()));
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
            if (op instanceof VncDouble) {
                return new VncDouble(value / ((VncDouble)op).value);
            }
            else if (op instanceof VncLong) {
                return new VncDouble(value / ((VncLong)op).toJavaDouble());
            }
            else if (op instanceof VncInteger) {
                return new VncDouble(value / ((VncInteger)op).toJavaDouble());
            }
            else if (op instanceof VncBigDecimal) {
                return new VncBigDecimal(toJavaBigDecimal().divide(((VncBigDecimal)op).toJavaBigDecimal(), 16, RoundingMode.HALF_UP));
            }
            else if (op instanceof VncBigInteger) {
                return new VncBigDecimal(toJavaBigDecimal().divide(((VncBigInteger)op).toJavaBigDecimal(), 16, RoundingMode.HALF_UP));
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
        if (other instanceof VncDouble) {
            return VncBoolean.of(value == ((VncDouble)other).value);
        }
        else if (other instanceof VncLong) {
            return VncBoolean.of(value == ((VncLong)other).toJavaDouble());
        }
        else if (other instanceof VncInteger) {
            return VncBoolean.of(value == ((VncInteger)other).toJavaDouble());
        }
        else if (other instanceof VncBigDecimal) {
            return VncBoolean.of(toJavaBigDecimal().compareTo(((VncBigDecimal)other).toJavaBigDecimal()) == 0);
        }
        else if (other instanceof VncBigInteger) {
            return VncBoolean.of(toJavaBigInteger().compareTo(((VncBigInteger)other).toJavaBigInteger()) == 0);
        }
        else {
            throw new VncException(String.format(
                    "Function == operand of type %s is not a numeric type",
                    Types.getType(other)));
        }
    }

    @Override
    public VncBoolean zeroQ() {
        return VncBoolean.of(value == 0D);
    }

    @Override
    public VncBoolean posQ() {
        return VncBoolean.of(value > 0D);
    }

    @Override
    public VncBoolean negQ() {
        return VncBoolean.of(value < 0D);
    }

    @Override
    public VncNumber square() {
        return new VncDouble(value * value);
    }

    @Override
    public VncNumber sqrt() {
        return new VncDouble(Math.sqrt(value));
    }

    public Double getValue() {
        return value;
    }

    public Float getFloatValue() {
        return Float.valueOf((float)value);
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.DOUBLE;
    }

    @Override
    public Object convertToJavaObject() {
        return value;
    }

    @Override
    public int toJavaInteger() {
        return (int)value;
    }

    @Override
    public long toJavaLong() {
        return (long)value;
    }

    @Override
    public double toJavaDouble() {
        return value;
    }

    @Override
    public BigInteger toJavaBigInteger() {
        return BigInteger.valueOf((long)value);
    }

    @Override
    public BigDecimal toJavaBigDecimal() {
        return new BigDecimal(value);
    }

    @Override
    public BigDecimal toJavaBigDecimal(final int scale) {
        return new BigDecimal(value).setScale(scale);
    }

    @Override
    public int compareTo(final VncVal o) {
        if (Types.isVncDouble(o)) {
            final double other = ((VncDouble)o).value;
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncInteger(o)) {
            final double other = ((VncInteger)o).toJavaDouble();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncLong(o)) {
            final double other = ((VncLong)o).toJavaDouble();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncBigDecimal(o)) {
            final double other = ((VncBigDecimal)o).toJavaDouble();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncBigInteger(o)) {
            final double other = ((VncBigInteger)o).toJavaDouble();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (o == Constants.Nil) {
            return 1;
        }

        return super.compareTo(o);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
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
            return value == ((VncDouble)obj).value;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        if (print_machine_readably) {
            if (Double.isInfinite(value)) {
                return ":Infinite";
            }
            else if (Double.isNaN(value)) {
                return ":NaN";
            }
            else {
                return  String.valueOf(value);
            }
        }
        else {
            return String.valueOf(value);
        }
    }


    public static final String TYPE = ":core/double";

    private static final long serialVersionUID = -1848883965231344442L;

    private final double value;
}
