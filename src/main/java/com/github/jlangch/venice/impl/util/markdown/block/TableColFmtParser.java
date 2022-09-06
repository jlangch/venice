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
package com.github.jlangch.venice.impl.util.markdown.block;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.IPreCompiled;
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.threadpool.GlobalThreadFactory;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment;
import com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.WidthUnit;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class TableColFmtParser {

    public TableColFmtParser() {
    }

    public TableColFmt parse(final String format) {
        final String fmt = StringUtil.trimToEmpty(format);

        final TableColFmt fmtMD = parseMarkdownStyleFormat(fmt);
        if (fmtMD != null) {
            return fmtMD;
        }
        else {
            return parseCssStyleFormat(fmt);
        }
    }


    private TableColFmt parseMarkdownStyleFormat(final String format) {
        final HorzAlignment align = parseMarkdownStyleHorzAlignment(format);
        return align == null ? null : new TableColFmt(align, null);
    }

    @SuppressWarnings("unchecked")
    private TableColFmt parseCssStyleFormat(final String format) {
        if (format.startsWith("[![") && format.endsWith("]]")) {
            final String css = format.substring(3, format.length()-2).trim();
            if (!css.isEmpty()) {
                try {
                    // Run the CSS parser in another thread. The parser runs a new
                    // Venice instance to run a Parsifal parser.
                    // Starting a new Venice instance will reset the sandbox to
                    // reject-all in the current thread, thus overwriting the current
                    // sandbox!
                    final Map<String,Object> cssProps =
                            runAsync(
                                new Callable<Map<String,Object>>() {
                                    @Override
                                    public Map<String, Object> call() throws Exception {
                                       final IPreCompiled precompiled = getCssParser();
                                       final Venice venice = new Venice(getParserSandbox());
                                       return (Map<String,Object>)venice.eval(
                                                                          precompiled,
                                                                          Parameters.of("css", css));
                                    }
                                });

                    final HorzAlignment align = parseCssStyleHorzAlignment(cssProps);

                    final TableColFmt.Width width = parseCssStyleWidth(cssProps);

                    return new TableColFmt(align, width);
                }
                catch(Exception ex) {
                    throw new RuntimeException(
                            "Failed to parse markdown table column css '"+ css + "'",
                            ex);
                }
            }
        }

        return null;
    }

    private HorzAlignment parseCssStyleHorzAlignment(final Map<String,Object> cssProps) {
        final String align = (String)cssProps.get("text-align");

        switch(StringUtil.trimToEmpty(align)) {
            case "left":   return HorzAlignment.LEFT;
            case "center": return HorzAlignment.CENTER;
            case "right":  return HorzAlignment.RIGHT;
            default:       return null;
        }
    }

    @SuppressWarnings("unchecked")
    private TableColFmt.Width parseCssStyleWidth(final Map<String,Object> cssProps) {
        // "auto", [30, "%"]
        Object width = cssProps.get("width");
        if (width != null) {
            if (width instanceof String) {
                if ("auto".equals(width)) {
                    return new TableColFmt.Width(0, WidthUnit.AUTO);
                }
            }

            if (width instanceof List) {
                long val = (long)((List<Object>)width).get(0);
                String unit = (String)((List<Object>)width).get(1);

                switch(StringUtil.trimToEmpty(unit)) {
                    case "%":  return new TableColFmt.Width(val, WidthUnit.PERCENT);
                    case "px": return new TableColFmt.Width(val, WidthUnit.PX);
                    case "em": return new TableColFmt.Width(val, WidthUnit.EM);
                    default:   return new TableColFmt.Width(0, WidthUnit.AUTO);
                }
            }
        }

        return new TableColFmt.Width(0, WidthUnit.AUTO);
    }

    private HorzAlignment parseMarkdownStyleHorzAlignment(final String format) {
        if (isCenterAlign(format)) {
            return HorzAlignment.CENTER;
        }
        else if (isLeftAlign(format)) {
            return HorzAlignment.LEFT;
        }
        else if (isRightAlign(format)) {
            return HorzAlignment.RIGHT;
        }
        else {
            return null;
        }
    }

    private boolean isCenterAlign(final String s) {
        return s.matches("---+") || s.matches("[:]-+[:]");
    }

    private boolean isLeftAlign(final String s) {
        return s.matches("[:]-+");
    }

    private boolean isRightAlign(final String s) {
        return s.matches("-+[:]");
    }

    private IPreCompiled getCssParser() {
    	IPreCompiled pc = cssParser.get();
        if (pc == null) {
            final String parser = new ClassPathResource(CSS_PARSER).getResourceAsString();

            pc = new Venice().precompile("CssParser", parser, true);
            cssParser.set(pc);
        }

        return pc;
    }

    private IInterceptor getParserSandbox() {
        IInterceptor sb = sandbox.get();
        if (sb == null) {
            sb = new SandboxInterceptor(
                        new SandboxRules()
                                .rejectAllUnsafeFunctions()
                                .whitelistVeniceFunctions("load-module")
                                .withVeniceModules("parsifal"));

             sandbox.set(sb);
        }

        return sb;
    }

    private <T> T runAsync(final Callable<T> callable)
    throws InterruptedException, ExecutionException {
        final FutureTask<T> futureTask = new FutureTask<>(callable);
        final Thread t = GlobalThreadFactory.newThread("venice-markdown-parser", futureTask);
        t.start();

        return futureTask.get();
    }


    private static AtomicReference<IPreCompiled> cssParser = new AtomicReference<>();
    private static AtomicReference<IInterceptor> sandbox = new AtomicReference<>();

    private static String CSS_PARSER =
            "com/github/jlangch/venice/docgen/table-col-css-parser.venice";
}
