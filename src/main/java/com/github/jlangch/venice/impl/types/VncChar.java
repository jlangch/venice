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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.StringUtil;


public class VncChar extends VncVal {

    public VncChar(final char v) {
        this(Character.valueOf(v), null);
    }

    public VncChar(final Character v) {
        this(v, null);
    }

    public VncChar(
            final Character v,
            final VncWrappingTypeDef wrappingTypeDef
    ) {
        super(wrappingTypeDef, Constants.Nil);
        value = v;
    }

    public Character getValue() {
        return value;
    }

    public String toUnicode() {
        return String.format("\\u%04X", (int)value.charValue());
    }

    @Override
    public VncChar withMeta(final VncVal meta) {
        return this;
    }

    @Override
    public VncChar wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
        return new VncChar(value, wrappingTypeDef);
    }

    @Override
    public VncKeyword getType() {
        return isWrapped() ? new VncKeyword(
                                    getWrappingTypeDef().getType().getQualifiedName(),
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncChar.TYPE),
                                        new VncKeyword(VncVal.TYPE)))
                           : new VncKeyword(
                                    VncChar.TYPE,
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.CHAR;
    }

    @Override
    public Object convertToJavaObject() {
        return value;
    }

    @Override
    public int compareTo(final VncVal o) {
        if (o == Constants.Nil) {
            return 1;
        }
        else if (Types.isVncChar(o)) {
            return getValue().compareTo(((VncChar)o).getValue());
        }
        else if (Types.isVncString(o)) {
            return getValue().toString().compareTo(((VncString)o).getValue());
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
            return value.equals(((VncChar)obj).value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        if (print_machine_readably) {
            final char ch = value.charValue();
            if (ch <= 32) {
                if (ch == ' ') {
                    return "#\\space";
                }
                else if (ch == '\n') {
                    return "#\\newline";
                }
                else if (ch == '\t') {
                    return "#\\tab";
                }
                else if (ch == '\r') {
                    return "#\\return";
                }
                else if (ch == '\f') {
                    return "#\\formfeed";
                }
                else if (ch == '\b') {
                    return "#\\backspace";
                }
                else {
                    return "#" + StringUtil.toEscapedUnicode(ch);
                }
            }
            else if (ch == '\\') {
                return "#\\backslash";
            }
            else if (ch == 127) {
                return "#" + StringUtil.toEscapedUnicode(ch);
            }
            else {
                return "#\\" + ch;
            }
        }
        else {
            return value.toString();
        }
    }

    public static VncChar fromSymbol(final String symbol) {
        return SYMBOLS.get(symbol);
    }


    public static Map<String, VncChar> symbols() {
        final  Map<String, VncChar> map = new LinkedHashMap<>();

        map.put("#\\space",              new VncChar(' '));
        map.put("#\\newline",            new VncChar('\n'));
        map.put("#\\tab",                new VncChar('\t'));
        map.put("#\\formfeed",           new VncChar('\f'));
        map.put("#\\return",             new VncChar('\r'));
        map.put("#\\backspace",          new VncChar('\b'));
        map.put("#\\lparen",             new VncChar('('));
        map.put("#\\rparen",             new VncChar(')'));
        map.put("#\\quote",              new VncChar('"'));
        map.put("#\\backslash",          new VncChar('\\')); // \u005C

        // https://unicode-table.com/en/

        map.put("#\\pilcrow",            new VncChar('\u00B6'));  // pilcrow '¶'
        map.put("#\\middle-dot",         new VncChar('\u00B7'));  // middle dot '·'
        map.put("#\\right-guillemet",    new VncChar('\u00BB'));  // right guillemet  '»'
        map.put("#\\left-guillemet",     new VncChar('\u00AB'));  // left guillemet  '«'
        map.put("#\\copyright",          new VncChar('\u00A9'));  // copyright '©'

        map.put("#\\bullet",             new VncChar('\u2022'));  // bullet '•'
        map.put("#\\horz-ellipsis",      new VncChar('\u2026'));  // horizontal ellipsis '…'
        map.put("#\\per-mille-sign",     new VncChar('\u2030'));  // per mille sign '‰'
        map.put("#\\diameter-sign",      new VncChar('\u2300'));  // diameter sign '⌀'
        map.put("#\\check-mark",         new VncChar('\u2713'));  // check mark '✓'
        map.put("#\\cross-mark",         new VncChar('\u2717'));  // cross mark '✗'
        map.put("#\\pi",                 new VncChar('\u03C0'));  // PI 'π'

        map.put("#\\nbsp",               new VncChar('\u00A0'));  // no break space
        map.put("#\\en-space",           new VncChar('\u2002'));  // en space
        map.put("#\\em-space",           new VncChar('\u2003'));  // em space, 1 en (= 1/2 em)
        map.put("#\\three-per-em-space", new VncChar('\u2004'));  // three-per-em space
        map.put("#\\four-per-em-space",  new VncChar('\u2005'));  // four-per-em space
        map.put("#\\six-per-em-space",   new VncChar('\u2006'));  // six-per-em space

        return Collections.unmodifiableMap(map);
    }


    private static final Map<String, VncChar> SYMBOLS = symbols();

    public static final String TYPE = ":core/char";

    private static final long serialVersionUID = -1848883965231344442L;

    private final Character value;
}
