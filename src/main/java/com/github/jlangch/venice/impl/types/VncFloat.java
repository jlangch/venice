/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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


public class VncFloat extends VncNumber {

    public VncFloat(final Float v) {
        super(null, Constants.Nil);
        value = v;
    }

    public VncFloat(final Double v) {
        super(null, Constants.Nil);
        value = v.floatValue();
    }

    public VncFloat(final Long v) {
        super(null, Constants.Nil);
        value = v.floatValue();
    }

    public VncFloat(final Integer v) {
        super(null, Constants.Nil);
        value = v.floatValue();
    }

    public VncFloat(final Float v, final VncVal meta) {
        super(null, meta);
        value = v;
    }

    public VncFloat(
            final Float v,
            final VncWrappingTypeDef wrappingTypeDef,
            final VncVal meta
    ) {
        super(wrappingTypeDef, meta);
        value = v;
    }


    public static VncFloat of(final VncVal v) {
        if (Types.isVncNumber(v)) {
            return new VncFloat(((VncNumber)v).toJavaFloat());
        }
        else {
            throw new VncException(String.format(
                    "Cannot convert value of type %s to float",
                    Types.getType(v)));
        }
    }


    @Override
    public VncFloat withMeta(final VncVal meta) {
        return new VncFloat(value, getWrappingTypeDef(), meta);
    }

    @Override
    public VncFloat wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
        return new VncFloat(value, wrappingTypeDef, meta);
    }

    @Override
    public VncKeyword getType() {
        return isWrapped() ? new VncKeyword(
                                    getWrappingTypeDef().getType().getQualifiedName(),
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncFloat.TYPE),
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)))
                           : new VncKeyword(
                                    VncFloat.TYPE,
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public VncFloat inc() {
        return new VncFloat(value + 1.0F);
    }

    @Override
    public VncFloat dec() {
        return new VncFloat(value - 1.0F);
    }

    @Override
    public VncFloat negate() {
        return new VncFloat(value * -1.0F);
    }

    @Override
    public VncNumber add(final VncVal op) {
        if (op instanceof VncFloat) {
            return new VncFloat(value + ((VncFloat)op).value);
        }
        else if (op instanceof VncDouble) {
            return new VncDouble(value + ((VncDouble)op).toJavaDouble());
        }
        else if (op instanceof VncLong) {
            return new VncFloat(value + ((VncLong)op).toJavaFloat());
        }
        else if (op instanceof VncInteger) {
            return new VncFloat(value + ((VncInteger)op).toJavaFloat());
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
        if (op instanceof VncFloat) {
            return new VncFloat(value - ((VncFloat)op).value);
        }
        else if (op instanceof VncDouble) {
            return new VncDouble(value - ((VncDouble)op).toJavaDouble());
        }
        else if (op instanceof VncLong) {
            return new VncFloat(value - ((VncLong)op).toJavaFloat());
        }
        else if (op instanceof VncInteger) {
            return new VncFloat(value - ((VncInteger)op).toJavaFloat());
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
        if (op instanceof VncFloat) {
            return new VncFloat(value * ((VncFloat)op).value);
        }
        else if (op instanceof VncDouble) {
            return new VncDouble(value * ((VncDouble)op).toJavaDouble());
        }
        else if (op instanceof VncLong) {
            return new VncFloat(value * ((VncLong)op).toJavaFloat());
        }
        else if (op instanceof VncInteger) {
            return new VncFloat(value * ((VncInteger)op).toJavaFloat());
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
            if (op instanceof VncFloat) {
                return new VncFloat(value / ((VncFloat)op).value);
            }
            else if (op instanceof VncDouble) {
                return new VncDouble(value / ((VncDouble)op).toJavaDouble());
            }
           else if (op instanceof VncLong) {
                return new VncFloat(value / ((VncLong)op).toJavaFloat());
            }
            else if (op instanceof VncInteger) {
                return new VncFloat(value / ((VncInteger)op).toJavaFloat());
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
        if (other instanceof VncFloat) {
            return VncBoolean.of(value == ((VncFloat)other).value);
        }
        else if (other instanceof VncDouble) {
            return VncBoolean.of((double)value == ((VncDouble)other).toJavaFloat());
        }
        else if (other instanceof VncLong) {
            return VncBoolean.of(value == ((VncLong)other).toJavaFloat());
        }
        else if (other instanceof VncInteger) {
            return VncBoolean.of(value == ((VncInteger)other).toJavaFloat());
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
        return VncBoolean.of(value == 0F);
    }

    @Override
    public VncBoolean posQ() {
        return VncBoolean.of(value > 0F);
    }

    @Override
    public VncBoolean negQ() {
        return VncBoolean.of(value < 0F);
    }

    @Override
    public VncNumber square() {
        return new VncFloat(value * value);
    }

    @Override
    public VncNumber sqrt() {
        return new VncFloat(Math.sqrt(value));
    }

    public Float getValue() {
        return value;
    }

    public Float getFloatValue() {
        return Float.valueOf(value);
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.FLOAT;
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
    public float toJavaFloat() {
        return value;
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
        if (Types.isVncFloat(o)) {
            final float other = ((VncFloat)o).value;
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncDouble(o)) {
            final double other = ((VncDouble)o).toJavaDouble();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncInteger(o)) {
            final float other = ((VncInteger)o).toJavaFloat();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncLong(o)) {
            final float other = ((VncLong)o).toJavaFloat();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncBigDecimal(o)) {
            final BigDecimal other = ((VncBigDecimal)o).toJavaBigDecimal();
            final BigDecimal thisVal = toJavaBigDecimal();
            return thisVal.compareTo(other);
        }
        else if (Types.isVncBigInteger(o)) {
            final BigInteger other = ((VncBigInteger)o).toJavaBigInteger();
            final BigInteger thisVal = toJavaBigInteger();
            return thisVal.compareTo(other);
        }
        else if (o == Constants.Nil) {
            return 1;
        }

        return super.compareTo(o);
    }

    @Override
    public int hashCode() {
        return Float.hashCode(value);
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
            return value == ((VncFloat)obj).value;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        if (print_machine_readably) {
            if (Float.isInfinite(value)) {
                return ":Infinite";
            }
            else if (Float.isNaN(value)) {
                return ":NaN";
            }
            else {
                return  String.valueOf(value) + "F";
            }
        }
        else {
            return String.valueOf(value);
        }
    }


    public static final String TYPE = ":core/float";

    private static final long serialVersionUID = -1848883965231344442L;

    private final float value;
}
