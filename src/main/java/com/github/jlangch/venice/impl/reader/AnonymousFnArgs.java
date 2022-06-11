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
package com.github.jlangch.venice.impl.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncVector;


public class AnonymousFnArgs {

    public boolean isCapturing() {
        return capturing;
    }

    public void startCapture() {
        capturing = true;
        fnArgs.clear();
    }

    public void stopCapture() {
        capturing = false;
        fnArgs.clear();
    }

    public boolean isFnArgSymbol(final VncSymbol sym) {
        return possibleFnArgs.contains(sym.getName());
    }

    public void addSymbol(final VncSymbol sym) {
        if (isFnArgSymbol(sym)) {
            fnArgs.add(sym.getName());
        }
    }

    public VncVector buildArgDef() {
        final List<VncVal> argDef = new ArrayList<>();

        if (fnArgs.size() == 1 && fnArgs.contains("%")) {
            argDef.add(new VncSymbol("%"));
        }
        else {
            for(int ii=1; ii<=getMaxArgPos(); ii++) {
                argDef.add(new VncSymbol("%" + ii));
            }

            if (fnArgs.contains("%&")) {
                argDef.add(new VncSymbol("&"));
                argDef.add(new VncSymbol("%&"));
            }
        }

        return VncVector.ofList(argDef);
    }


    private int getMaxArgPos() {
        return fnArgs.stream()
                     .filter(s -> s.matches("%[1-9][0-9]*"))
                     .map(s -> Integer.parseInt(s.substring(1)))
                     .max(Integer::compareTo)
                     .orElse(Integer.valueOf(-1));
    }




    private final Set<String> possibleFnArgs = new HashSet<>(Arrays.asList(
                                                    "%1", "%2","%3","%4","%5",
                                                    "%6", "%7","%8","%9","%10",
                                                    "%", "%&"));

    private final Set<String> fnArgs = new HashSet<>();

    private boolean capturing = false;
}
