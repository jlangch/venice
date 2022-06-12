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
package com.github.jlangch.venice.impl.util;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.reader.Token;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;


public class MetaUtil {

    public static VncVal addDefMeta(final VncVal val, final VncMap meta) {
        VncVal valMeta = val.getMeta();

        if (valMeta == Constants.Nil) {
            valMeta = meta;
        }
        else if (Types.isVncMap(valMeta)) {
            valMeta = ((VncMap)valMeta).assoc(meta.toVncList());
        }

        return val.withMeta(valMeta);
    }

    public static VncVal toMeta(final Token token) {
        return VncHashMap.of(
                    FILE, new VncString(token.getFile()),
                    LINE, new VncLong(token.getLine()),
                    COLUMN, new VncLong(token.getColumn()));
    }

    public static VncVal addMetaVal(final VncVal meta, final VncVal... mvs) {
        if (meta == Constants.Nil) {
            return VncHashMap.of(mvs);
        }
        else if (Types.isVncMap(meta)) {
            return ((VncMap)meta).assoc(mvs);
        }
        else {
            // not a map
            return meta;
        }
    }

    public static VncVal mergeMeta(final VncVal meta1, VncVal meta2) {
        if (meta1 == Nil || meta1 == null) {
            return meta2 == null ? Nil : meta2;
        }
        else if (meta2 == Nil || meta2 == null) {
            return meta1;
        }
        else if (Types.isVncMap(meta1) && Types.isVncMap(meta2)) {
            return ((VncMap)meta1).putAll((VncMap)meta2);
        }
        else {
            throw new VncException(String.format(
                    "Failed to merge meta data on incompatible old (%s) and new (%s) meta data types",
                    Types.getType(meta1),
                    Types.getType(meta2)));
        }
    }

    public static boolean isPrivate(final VncVal meta) {
        if (meta instanceof VncHashMap) {
            return VncBoolean.isTrue(((VncHashMap)meta).get(PRIVATE));
        }
        else {
            return false;
        }
    }

    public static boolean isDynamic(final VncVal meta) {
        if (meta instanceof VncHashMap) {
            return VncBoolean.isTrue(((VncHashMap)meta).get(DYNAMIC));
        }
        else {
            return false;
        }
    }

    public static VncVal markAsPrivate(final VncVal meta) {
        if (meta == null || meta == Nil) {
            return VncHashMap.of(PRIVATE, VncBoolean.True);
        }
        else if (meta instanceof VncHashMap) {
            ((VncHashMap)meta).assoc(PRIVATE, VncBoolean.True);
            return meta;
        }
        else {
            throw new VncException(String.format(
                    "Failed to add :private flag to meta data. The meta data (%s) is not a map!",
                    Types.getType(meta)));
        }
    }

    public static VncVal markAsDynamic(final VncVal meta) {
        if (meta == null || meta == Nil) {
            return VncHashMap.of(DYNAMIC, VncBoolean.True);
        }
        else if (meta instanceof VncHashMap) {
            ((VncHashMap)meta).assoc(DYNAMIC, VncBoolean.True);
            return meta;
        }
        else {
            throw new VncException(String.format(
                    "Failed to add :dynamic flag to meta data. The meta data (%s) is not a map!",
                    Types.getType(meta)));
        }
    }

    public static String getNamespace(final VncVal meta) {
        if (meta instanceof VncHashMap) {
            final VncVal ns = ((VncHashMap)meta).get(NS);
            return ns == Nil ? null : ((VncString)ns).getValue();
        }
        else {
            return null;
        }
    }

    public static VncVal setNamespace(final VncVal meta, final String ns) {
        return MetaUtil.addMetaVal(meta, NS, new VncString(ns));
    }

    public static String getFile(final VncVal meta) {
        final VncVal vFile = getMetaVal(meta, MetaUtil.FILE);
        final String file = vFile == Nil ? null : Coerce.toVncString(vFile).getValue();
        return file == null || file.isEmpty() ? null : file;
    }

    public static int getLine(final VncVal meta) {
        final VncVal vLine = getMetaVal(meta, MetaUtil.LINE);
        return vLine == Nil ? -1 : Coerce.toVncLong(vLine).getValue().intValue();
    }

    public static int getCol(final VncVal meta) {
        final VncVal vCol = getMetaVal(meta, MetaUtil.COLUMN);
        return vCol == Nil ? -1 : Coerce.toVncLong(vCol).getValue().intValue();
    }

    public static VncVal getMetaVal(final VncVal meta, final VncString key) {
        return (meta instanceof VncHashMap) ? ((VncHashMap)meta).get(key) : Nil;
    }


    public static boolean isType(final VncVal meta) {
        return getMetaVal(meta, MetaUtil.TYPE) != Nil;
    }

    public static VncVal typeMeta(final VncKeyword... supertypes) {
        return VncHashMap.of(
                    TYPE, VncBoolean.True,
                    SUPERTYPES, supertypes == null
                                    ? VncList.empty()
                                    : VncList.of(supertypes));
    }

    public static VncList getSupertypes(final VncVal meta) {
        final VncVal v = getMetaVal(meta, MetaUtil.SUPERTYPES);
        return v instanceof VncList ? (VncList)v : VncList.empty();
    }


    // Var documentation
    public static final VncKeyword ARGLIST = new VncKeyword(":arglists");
    public static final VncKeyword DOC = new VncKeyword(":doc");
    public static final VncKeyword EXAMPLES = new VncKeyword(":examples");
    public static final VncKeyword SEE_ALSO = new VncKeyword(":see-also");
    public static final VncKeyword FUNCTION_REFS = new VncKeyword(":function-refs");

    // File location
    public static final VncKeyword FILE = new VncKeyword(":file");
    public static final VncKeyword LINE = new VncKeyword(":line");
    public static final VncKeyword COLUMN = new VncKeyword(":column");

    public static final VncKeyword MACRO = new VncKeyword(":macro");
    public static final VncKeyword NS = new VncKeyword(":ns");

    public static final VncKeyword PRIVATE = new VncKeyword(":private");
    public static final VncKeyword DYNAMIC = new VncKeyword(":dynamic");

    // Type info
    public static final VncKeyword TYPE = new VncKeyword(":type_");
    public static final VncKeyword SUPERTYPES = new VncKeyword(":supertypes_");
}
