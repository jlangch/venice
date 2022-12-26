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
package com.github.jlangch.venice.impl.util.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.util.Types;


public class CSVWriter {

    public CSVWriter() {
        this(',', '"', "\n");
    }

    public CSVWriter(final char separator, final char quote, final String newline) {
        this.separator = String.valueOf(separator);
        this.quote = String.valueOf(quote);
        this.newline = newline == null ? "\n" : newline;
    }


    public void write(final Writer writer, final List<List<String>> data) {
        try {
            boolean first = true;
            for(List<String> record : data) {
                if (!first) {
                    writer.write(newline);
                }

                writeRecord(writer, record);

                first = false;
            }
        }
        catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void write(final Writer writer, final VncSequence data) {
        try {
            boolean first = true;
            for(VncVal record : data) {
                if (!first) {
                    writer.write(newline);
                }

                if (Types.isVncSequence(record)) {
                    writeRecord(writer, (VncSequence)record);
                }
                else {
                    throw new VncException("CSV data records must be of type VncSequence");
                }

                first = false;
            }
        }
        catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void writeRecord(final Writer writer, final List<String> record) throws IOException {
        boolean first = true;
        for(String s : record) {
            if (!first) {
                writer.write(separator);
            }

            if (s != null) {
                writer.write(quote(s));
            }

            first = false;
        }
    }

    private void writeRecord(final Writer writer, VncSequence record) throws IOException {
        boolean first = true;
        for(VncVal v : record) {
            if (!first) {
                writer.write(separator);
            }

            if (v != Constants.Nil) {
                writer.write(quote(v.toString()));
            }

            first = false;
        }
    }

    private String escape(final String s) {
        final StringBuilder sb = new StringBuilder();

        int pos = s.indexOf(quote);
        if (pos < 0) {
            return s;
        }
        else {
            int curr = 0;
            while(curr < s.length()) {
                if (pos >= 0) {
                    sb.append(s.substring(curr, pos+1));
                    sb.append(quote);
                    curr = pos + 1;
                }
                else {
                    sb.append(s.substring(pos));
                    break;
                }
                pos = s.indexOf(quote, curr);
            }
            return sb.toString();
        }
    }

    private String quote(final String s) {
        return needsQuote(s) ? quote + escape(s) + quote : s;
    }

    private boolean needsQuote(final String s) {
        return s != null && (s.contains(" ") || s.contains(quote) || s.contains(separator));
    }



    private final String separator;
    private final String quote;
    private final String newline;
}
