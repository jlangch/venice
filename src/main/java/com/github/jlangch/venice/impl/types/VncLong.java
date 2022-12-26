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
package com.github.jlangch.venice.impl.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncLong extends VncNumber {

    public VncLong(final long v) {
        super(null, Constants.Nil);
        value = v;
    }

    public VncLong(final long v, final VncVal meta) {
        super(null, meta);
        value = v;
    }

    public VncLong(
            final Long v,
            final VncWrappingTypeDef wrappingTypeDef,
            final VncVal meta
    ) {
        super(wrappingTypeDef, meta);
        value = v;
    }


    public static VncLong of(final VncVal v) {
        if (Types.isVncNumber(v)) {
            return new VncLong(((VncNumber)v).toJavaLong());
        }
        else {
            throw new VncException(String.format(
                    "Cannot convert value of type %s to long",
                    Types.getType(v)));
        }
    }


    @Override
    public VncLong withMeta(final VncVal meta) {
        return new VncLong(value, getWrappingTypeDef(), meta);
    }

    @Override
    public VncLong wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
        return new VncLong(value, wrappingTypeDef, meta);
    }

    @Override
    public VncKeyword getType() {
        return isWrapped() ? new VncKeyword(
                                    getWrappingTypeDef().getType().getQualifiedName(),
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncLong.TYPE),
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)))
                           : new VncKeyword(
                                    VncLong.TYPE,
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncNumber.TYPE),
                                        new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public VncLong inc() {
        return new VncLong(value + 1L);
    }

    @Override
    public VncLong dec() {
        return new VncLong(value - 1L);
    }

    @Override
    public VncLong negate() {
        return new VncLong(Math.negateExact(value));
    }

    @Override
    public VncNumber add(final VncVal op) {
        if (op instanceof VncLong) {
            return new VncLong(value + ((VncLong)op).value);
        }
        else if (op instanceof VncInteger) {
            return new VncLong(value + ((VncInteger)op).toJavaLong());
        }
        else if (op instanceof VncDouble) {
            return new VncDouble(value + ((VncDouble)op).toJavaDouble());
        }
        else if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(toJavaBigDecimal().add(((VncBigDecimal)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigInteger) {
            return new VncBigInteger(toJavaBigInteger().add(((VncBigInteger)op).toJavaBigInteger()));
        }
        else {
            throw new VncException(String.format(
                    "Function + operand of type %s is not a numeric type",
                    Types.getType(op)));
        }
    }

    @Override
    public VncNumber sub(final VncVal op) {
        if (op instanceof VncLong) {
            return new VncLong(value - ((VncLong)op).value);
        }
        else if (op instanceof VncInteger) {
            return new VncLong(value - ((VncInteger)op).toJavaLong());
        }
        else if (op instanceof VncDouble) {
            return new VncDouble(value - ((VncDouble)op).toJavaDouble());
        }
        else if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(toJavaBigDecimal().subtract(((VncBigDecimal)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigInteger) {
            return new VncBigInteger(toJavaBigInteger().subtract(((VncBigInteger)op).toJavaBigInteger()));
        }
        else {
            throw new VncException(String.format(
                    "Function - operand of type %s is not a numeric type",
                    Types.getType(op)));
        }
    }

    @Override
    public VncNumber mul(final VncVal op) {
        if (op instanceof VncLong) {
            return new VncLong(value * ((VncLong)op).value);
        }
        else if (op instanceof VncInteger) {
            return new VncLong(value * ((VncInteger)op).toJavaLong());
        }
        else if (op instanceof VncDouble) {
            return new VncDouble(value * ((VncDouble)op).toJavaDouble());
        }
        else if (op instanceof VncBigDecimal) {
            return new VncBigDecimal(toJavaBigDecimal().multiply(((VncBigDecimal)op).toJavaBigDecimal()));
        }
        else if (op instanceof VncBigInteger) {
            return new VncBigInteger(toJavaBigInteger().multiply(((VncBigInteger)op).toJavaBigInteger()));
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
            if (op instanceof VncLong) {
                return new VncLong(value / ((VncLong)op).value);
            }
            else if (op instanceof VncInteger) {
                return new VncLong(value / ((VncInteger)op).toJavaLong());
            }
            else if (op instanceof VncDouble) {
                return new VncDouble(value / ((VncDouble)op).toJavaDouble());
            }
            else if (op instanceof VncBigDecimal) {
                return new VncBigDecimal(toJavaBigDecimal().divide(((VncBigDecimal)op).toJavaBigDecimal(), 16, RoundingMode.HALF_UP));
            }
            else if (op instanceof VncBigInteger) {
                return new VncBigInteger(toJavaBigInteger().divide(((VncBigInteger)op).toJavaBigInteger()));
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
        if (other instanceof VncLong) {
            return VncBoolean.of(value == ((VncLong)other).value);
        }
        else if (other instanceof VncInteger) {
            return VncBoolean.of(value == ((VncInteger)other).toJavaLong());
        }
        else if (other instanceof VncDouble) {
            return VncBoolean.of(value == ((VncDouble)other).toJavaDouble());
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
        return VncBoolean.of(value == 0L);
    }

    @Override
    public VncBoolean posQ() {
        return VncBoolean.of(value > 0L);
    }

    @Override
    public VncBoolean negQ() {
        return VncBoolean.of(value < 0L);
    }

    @Override
    public VncNumber square() {
        return new VncLong(value * value);
    }

    @Override
    public VncNumber sqrt() {
        return new VncDouble(Math.sqrt(value));
    }

    public Long getValue() {
        return value;
    }

    public Integer getIntValue() {
        return Integer.valueOf((int)value);
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.LONG;
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
        return value;
    }

    @Override
    public double toJavaDouble() {
        return value;
    }

    @Override
    public BigInteger toJavaBigInteger() {
        return BigInteger.valueOf(value);
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
        if (Types.isVncLong(o)) {
            final long other = ((VncLong)o).value;
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncInteger(o)) {
            final long other = ((VncInteger)o).toJavaLong();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncDouble(o)) {
            final long other = ((VncDouble)o).toJavaLong();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncBigDecimal(o)) {
            final long other = ((VncBigDecimal)o).toJavaLong();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (Types.isVncBigInteger(o)) {
            final long other = ((VncBigInteger)o).toJavaLong();
            return value < other ? -1 : (value == other ? 0 : 1);
        }
        else if (o == Constants.Nil) {
            return 1;
        }

        return super.compareTo(o);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
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
            return value == ((VncLong)obj).value;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }


    public static final String TYPE = ":core/long";

    private static final long serialVersionUID = -1848883965231344442L;

    private final long value;
}
