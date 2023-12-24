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

import static com.github.jlangch.venice.impl.types.VncBoolean.False;

import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.Watchable;


public class VncAtom extends VncVal implements IDeref {

    public VncAtom(final VncVal value) {
        super(Constants.Nil);
        state.set(value);
        validatorFn = null;
    }

    public VncAtom(final VncVal value, final VncFunction validatorFn, final VncVal meta) {
        super(meta);
        this.state.set(value);
        this.validatorFn = validatorFn;
    }


    @Override
    public VncAtom withMeta(final VncVal meta) {
        return new VncAtom(
                    state.get(),
                    validatorFn,
                    MetaUtil.mergeMeta(getMeta(), meta));
    }

    @Override
    public VncKeyword getType() {
        return new VncKeyword(
                        TYPE,
                        MetaUtil.typeMeta(
                            new VncKeyword(VncVal.TYPE)));
    }

    public VncVal reset(final VncVal newVal) {
        validate(newVal);

        state.set(newVal);
        return newVal;
    }

    @Override
    public VncVal deref() {
        return state.get();
    }

    public VncVal swap(final VncFunction fn, final VncList args) {
        for(;;) {
            final VncVal oldVal = deref();

            final VncList new_args = VncList.of(oldVal).addAllAtEnd(args);
            final VncVal newVal = fn.apply(new_args);
            validate(newVal);

            if (state.compareAndSet(oldVal, newVal)) {
                watchable.notifyWatches(this, oldVal, newVal);
                return state.get();
            }
        }
    }

    public VncVector swap_vals(final VncFunction fn, final VncList args) {
        for(;;) {
            final VncVal oldVal = deref();

            final VncList new_args = VncList.of(oldVal).addAllAtEnd(args);
            final VncVal newVal = fn.apply(new_args);
            validate(newVal);

            if (state.compareAndSet(oldVal, newVal)) {
                watchable.notifyWatches(this, oldVal, newVal);
                return VncVector.of(oldVal, state.get());
            }
        }
    }

    public VncVal compareAndSet(final VncVal expectValue, final VncVal newVal) {
        validate(newVal);

        final VncVal oldVal = deref();
        if (oldVal.equals(expectValue)) {
            final boolean successful = state.compareAndSet(oldVal, newVal);
            if (successful) {
                watchable.notifyWatches(this, oldVal, newVal);
            }
            return VncBoolean.of(successful);
        }
        else {
            return False;
        }
    }

    public void addWatch(final VncKeyword name, final VncFunction fn) {
        watchable.addWatch(name, fn);
    }

    public void removeWatch(final VncKeyword name) {
        watchable.removeWatch(name);
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.ATOM;
    }

    @Override
    public Object convertToJavaObject() {
        return null;
    }

    @Override
    public String toString() {
        return "(atom " + Printer.pr_str(state.get(), true) + ")";
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        return "(atom " + Printer.pr_str(state.get(), print_machine_readably) + ")";
    }

    private void validate(final VncVal newVal) {
        if (validatorFn != null) {
            try {
                final VncVal ok = validatorFn.apply(VncList.of(newVal));
                if (VncBoolean.isFalseOrNil(ok)) {
                    throw new VncException("Invalid atom state");
                }
            }
            catch (VncException ex) {
                throw ex;
            }
            catch (RuntimeException ex) {
                throw new VncException("Invalid atom state");
            }
        }
    }


    public static final String TYPE = ":core/atom";

    private static final long serialVersionUID = -1848883965231344442L;

    private final AtomicReference<VncVal> state = new AtomicReference<>();
    private final VncFunction validatorFn;
    private final Watchable watchable = new Watchable();
}
