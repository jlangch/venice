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


public class VncBigInteger extends VncNumber {

    public VncBigInteger(final BigInteger v) {
        this(v, null, Constants.Nil);
    }

    public VncBigInteger(final double v) {
        this(BigInteger.valueOf((long)v), null, Constants.Nil);
    }

    public VncBigInteger(final long v) {
        this(BigInteger.valueOf(v), null, Constants.Nil);
    }

    public VncBigInteger(final BigInteger v, final VncVal meta) {
        this(v, null, meta);
    }

    public VncBigInteger(
            final BigInteger v,
            final VncWrappingTypeDef wrappingTypeDef,
            final VncVal meta
    ) {
        super(wrappingTypeDef, meta);
        value = v;
    }


    public static VncBigInteger of(final VncVal v) {
        if (Types.isVncNumber(v)) {
            return new VncBigInteger(((VncNumber)v).toJavaBigInteger());
        }
        else {
            throw new VncException(String.format(
                    "Cannot convert value of type %s to big integer",
                    Types.getType(v)));
        }
    }


    @Override
    public VncBigInteger withMeta(final VncVal meta) {
        return new VncBigInteger(value, getWrappingTypeDef(), meta);
    }

    @Override
    public VncBigInteger wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
        return new VncBigInteger(value, wrappingTypeDef, meta);
    }

    @Override
    public VncKeyword getType() {
        return isWrapped() ? new VncKeyword(
                                    getWrappingTypeDef().getType().getQualifiedName(),
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncBigInteger.TYPE),
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)))
                           : new VncKeyword(
                                    VncBigInteger.TYPE,
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)));
    }


    @Override
    public VncBigInteger inc() {
        return new VncBigInteger(value.add(BigInteger.ONE));
    }

    @Override
    public VncBigInteger dec() {
        return new VncBigInteger(value.subtract(BigInteger.ONE));
    }

    @Override
    public VncBigInteger negate() {
        return new VncBigInteger(value.negate());
    }

    @Override
    public VncNumber add(final VncVal op) {
        if (op instanceof VncBigInteger) {
            return new VncBigInteger(value.add(((VncBigInteger)op).toJavaBigInteger()));
        }
        else if (op instanceof VncLong) {
            return new VncBigInteger(value.add(((VncLong)op).toJavaBigInteger()));
        }
        else if (op instanceof VncInteger) {
            return new VncBigInteger(value.add(((VncInteger)op).toJavaBigInteger()));
        }
        else if (op instanceof VncDouble) {
            return new VncBigDecimal(toJavaBigDecimal().add(((VncDouble)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(toJavaBigDecimal().add(((VncBigDecimal)op).toJavaBigDecimal()));
        }
        else {
            throw new VncException(String.format(
                    "Function + operand of type %s is not a numeric type",
                    Types.getType(op)));
        }
    }

    @Override
    public VncNumber sub(final VncVal op) {
        if (op instanceof VncBigInteger) {
            return new VncBigInteger(value.subtract(((VncBigInteger)op).toJavaBigInteger()));
        }
        else if (op instanceof VncLong) {
            return new VncBigInteger(value.subtract(((VncLong)op).toJavaBigInteger()));
        }
        else if (op instanceof VncInteger) {
            return new VncBigInteger(value.subtract(((VncInteger)op).toJavaBigInteger()));
        }
        else if (op instanceof VncDouble) {
            return new VncBigDecimal(toJavaBigDecimal().subtract(((VncDouble)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(toJavaBigDecimal().subtract(((VncBigDecimal)op).toJavaBigDecimal()));
        }
        else {
            throw new VncException(String.format(
                    "Function - operand of type %s is not a numeric type",
                    Types.getType(op)));
        }
    }

    @Override
    public VncNumber mul(final VncVal op) {
        if (op instanceof VncBigInteger) {
            return new VncBigInteger(value.multiply(((VncBigInteger)op).toJavaBigInteger()));
        }
        else if (op instanceof VncLong) {
            return new VncBigInteger(value.multiply(((VncLong)op).toJavaBigInteger()));
        }
        else if (op instanceof VncInteger) {
            return new VncBigInteger(value.multiply(((VncInteger)op).toJavaBigInteger()));
        }
        else if (op instanceof VncDouble) {
            return new VncBigDecimal(toJavaBigDecimal().multiply(((VncDouble)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(toJavaBigDecimal().multiply(((VncBigDecimal)op).toJavaBigDecimal()));
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
            if (op instanceof VncBigInteger) {
                return new VncBigInteger(value.divide(((VncBigInteger)op).toJavaBigInteger()));
            }
            else if (op instanceof VncLong) {
                return new VncBigInteger(value.divide(((VncLong)op).toJavaBigInteger()));
            }
            else if (op instanceof VncInteger) {
                return new VncBigInteger(value.divide(((VncInteger)op).toJavaBigInteger()));
            }
            else if (op instanceof VncDouble) {
                return new VncBigDecimal(toJavaBigDecimal().divide(((VncDouble)op).toJavaBigDecimal(), 16, RoundingMode.HALF_UP));
            }
            else if (op instanceof VncBigDecimal) {
                return new VncBigDecimal(toJavaBigDecimal().divide(((VncBigDecimal)op).toJavaBigDecimal(), 16, RoundingMode.HALF_UP));
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
        if (other instanceof VncBigInteger) {
            return VncBoolean.of(value.compareTo(((VncBigInteger)other).toJavaBigInteger()) == 0);
        }
        else if (other instanceof VncLong) {
            return VncBoolean.of(value.compareTo(((VncLong)other).toJavaBigInteger()) == 0);
        }
        else if (other instanceof VncInteger) {
            return VncBoolean.of(value.compareTo(((VncInteger)other).toJavaBigInteger()) == 0);
        }
        else if (other instanceof VncDouble) {
            return VncBoolean.of(toJavaBigDecimal().compareTo(((VncDouble)other).toJavaBigDecimal()) == 0);
        }
        else if (other instanceof VncBigDecimal) {
            return VncBoolean.of(toJavaBigDecimal().compareTo(((VncBigDecimal)other).toJavaBigDecimal()) == 0);
        }
        else {
            throw new VncException(String.format(
                    "Function == operand of type %s is not a numeric type",
                    Types.getType(other)));
        }
    }

    @Override
    public VncBoolean zeroQ() {
        return VncBoolean.of(value.compareTo(BigInteger.ZERO) == 0);
    }

    @Override
    public VncBoolean posQ() {
        return VncBoolean.of(value.compareTo(BigInteger.ZERO) > 0);
    }

    @Override
    public VncBoolean negQ() {
        return VncBoolean.of(value.compareTo(BigInteger.ZERO) < 0);
    }

    @Override
    public VncNumber square() {
        return new VncBigInteger(value.multiply(value));
    }

    @Override
    public VncNumber sqrt() {
        return new VncBigDecimal(new BigDecimal(Math.sqrt(toJavaDouble())));
    }

    public BigInteger getValue() {
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
        return value;
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
        if (Types.isVncBigInteger(o)) {
            return value.compareTo(((VncBigInteger)o).getValue());
        }
        else if (Types.isVncBigDecimal(o)) {
            return value.compareTo(((VncBigDecimal)o).toJavaBigInteger());
        }
        else if (Types.isVncInteger(o)) {
            return value.compareTo(((VncInteger)o).toJavaBigInteger());
        }
        else if (Types.isVncDouble(o)) {
            return value.compareTo(((VncDouble)o).toJavaBigInteger());
        }
        else if (Types.isVncLong(o)) {
            return value.compareTo(((VncLong)o).toJavaBigInteger());
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
            return value.equals(((VncBigInteger)obj).value);
        }
    }

    @Override
    public String toString() {
        return value.toString() + "N";
    }


    public static final String TYPE = ":core/bigint";

    private static final long serialVersionUID = -1848883965231344442L;

    private final BigInteger value;
}
