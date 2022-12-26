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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.io.CharsetUtil;


public class CSVReader {

    public CSVReader() {
        this(',', '"');
    }

    public CSVReader(final char separator, final char quote) {
        this.separator = String.valueOf(separator);
        this.quote = String.valueOf(quote);

        this.doubleQuotes = this.quote + this.quote;
        this.matcher = Pattern.compile(makeRegex()).matcher("");
    }

    public List<List<String>> parse(final String csv) {
        return parse(new StringReader(csv));
    }

    public List<List<String>> parse(final InputStream is, final Charset charset) {
        return parse(new InputStreamReader(is, CharsetUtil.charset(charset)));
    }

    public List<List<String>> parse(final Reader reader) {
        final List<List<String>> records = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(reader)) {
            String l = br.readLine();
            while(l != null) {
                final List<String> items = split(l);
                final List<String> parsedItems = new ArrayList<>();
                int ii = 0;
                while(ii<items.size()) {
                    final String v = items.get(ii);
                    if (v.equals(separator)) {
                        parsedItems.add(null);
                        ii += 1;
                    }
                    else {
                        parsedItems.add(unquote(v));
                        ii += 2;
                    }
                }

                records.add(parsedItems);
                l = br.readLine();
            }
            return records;
        }
        catch(Exception ex) {
            throw new VncException("Failed to parse CSV", ex);
        }
    }

    public List<String> split(final String line) {
        matcher.reset(line);

        final List<String> items = new ArrayList<>();

        while (matcher.find()) {
            items.add(unquote(matcher.group()));
        }

        return items;
    }

    private String unquote(final String item) {
        if (item.startsWith(quote) && item.endsWith(quote)) {
            return item.substring(1, item.length()-1)
                       .replace(doubleQuotes, quote);
        }
        else {
            return item;
        }
    }

    private String makeRegex() {
        return String.format(
                "[%s]|[^%s%s]+|[%s](?:[^%s]|[%s][%s])*[%s]",
                separator,
                separator,
                quote,
                quote,
                quote,
                quote,
                quote,
                quote);
    }


    private final String separator;
    private final String quote;
    private final String doubleQuotes;
    private final Matcher matcher;
}
