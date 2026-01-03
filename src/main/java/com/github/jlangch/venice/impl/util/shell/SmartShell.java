/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.impl.util.shell;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ShellException;
import com.github.jlangch.venice.TimeoutException;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.callstack.WithCallStack;
import com.github.jlangch.venice.impl.util.io.CharsetUtil;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;


public class SmartShell {

    /**
     * Executes a shell command
     *
     * @param cmd
     * @param opts
     * @param executor
     * @return
     */
    public static VncHashMap exec(
            final VncList cmd,
            final VncMap opts,
            final ExecutorService executor
    ) {
        //   Streams:
        //
        //   +-----------------+                  +-----------------+
        //   |              out|----------------->|in               |
        //   |   PARENT      in|<-----------------|err      CHILD   |
        //   |               in|<-----------------|out              |
        //   +-----------------+                  +-----------------+

        try {
            final String[] cmdArr = toStringArray(cmd);

            // options
            final String[] envArr = toEnvStrings(opts.get(new VncKeyword(":env")));
            final File dir_ = toFile(opts.get(new VncKeyword(":dir")));
            final VncVal in = opts.get(new VncKeyword(":in"));
            final VncVal slurpOutFn = opts.get(new VncKeyword(":out-fn"));
            final VncVal slurpErrFn = opts.get(new VncKeyword(":err-fn"));
            final VncVal inEnc = opts.get(new VncKeyword(":in-enc"));
            final VncVal outEnc = opts.get(new VncKeyword(":out-enc"));
            final VncVal timeout = opts.get(new VncKeyword(":timeout"));

            final boolean throwExOnFailure = VncBoolean.isTrue(opts.get(new VncKeyword(":throw-ex")));
            final Integer timeoutMillis = Types.isVncLong(timeout) ? ((VncLong)timeout).getIntValue() : null;

            //new ProcessBuilder().inheritIO().directory(dir_).command(cmdArr).environment()

            validateProcessDir(dir_);

            final Process proc = Runtime.getRuntime().exec(cmdArr, envArr, dir_);

            final OutputStream stdin = proc.getOutputStream();

            Future<Object> future_stdin = null;
            if (in != Nil) {
                // spit to subprocess' stdin a string, a bytebuf, or a File

                if (Types.isVncString(in)) {
                    future_stdin = getExecutorService(executor).submit(
                                    () -> copyAndClose((VncString)in, CharsetUtil.charset(inEnc), stdin));
                }
                else if (Types.isVncByteBuffer(in)) {
                    future_stdin = getExecutorService(executor).submit(
                                    () -> copyAndClose((VncByteBuffer)in, stdin));
                }
                else if (Types.isVncJavaObject(in, File.class)) {
                    future_stdin = getExecutorService(executor).submit(
                                    () -> copyAndClose((File)((VncJavaObject)in).getDelegate(), stdin));
                }
            }

            if (future_stdin == null) {
                // we're not sending anything to the subprocess' stdin
                stdin.close();
            }

            try(InputStream stdout = proc.getInputStream();
                InputStream stderr = proc.getErrorStream()
            ) {
                final String enc = getEncoding(outEnc);
                final Charset charset = "bytes".equals(enc) ? CharsetUtil.DEFAULT_CHARSET : CharsetUtil.charset(enc);

                // slurp the subprocess' stdout (as string or bytebuf)
                Future<VncVal> future_stdout;
                if ("bytes".equals(enc)) {
                    future_stdout = getExecutorService(executor).submit(() -> slurpToBytes(stdout));
                }
                else if (Types.isVncFunction(slurpOutFn)) {
                    final VncFunction fn = (VncFunction)slurpOutFn;
                    fn.sandboxFunctionCallValidation();

                    final ThreadBridge threadBridge = ThreadBridge.create("sh-process-stdout-slurper");
                    final Callable<VncVal> task = threadBridge.bridgeCallable(() ->  {
                                                            slurp(stdout, enc, fn);
                                                            return VncString.empty();
                                                        });

                    future_stdout = getExecutorService(executor).submit(task);
                }
                else {
                    future_stdout = getExecutorService(executor).submit(() -> slurpToString(stdout, charset));
                }

                // slurp the subprocess' stderr as string with platform default encoding
                Future<VncVal> future_stderr;
                if (Types.isVncFunction(slurpErrFn)) {
                    final VncFunction fn = (VncFunction)slurpErrFn;
                    fn.sandboxFunctionCallValidation();

                    final ThreadBridge threadBridge = ThreadBridge.create("sh-process-stderr-slurper");
                    final Callable<VncVal> task = threadBridge.bridgeCallable(() ->  {
                                                            slurp(stderr, enc, fn);
                                                            return VncString.empty();
                                                        });

                    future_stderr = getExecutorService(executor).submit(task);
                }
                else {
                    future_stderr = getExecutorService(executor).submit(() -> slurpToString(stderr, charset));
                }

                // wait for the process to exit
                final int exitCode;
                final boolean waitTimeout;
                if (timeoutMillis == null) {
                    exitCode = proc.waitFor();
                    waitTimeout = false;
                }
                else {
                    final boolean ok = proc.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
                    if (ok) {
                        exitCode = proc.exitValue();
                        waitTimeout = false;
                   }
                    else {
                        exitCode = -1;
                        waitTimeout = true;
                    }
                }

                if (future_stdin != null) {
                    future_stdin.get();
                }

                if (waitTimeout) {
                    future_stdout.cancel(true);
                    future_stderr.cancel(true);

                    //try (WithCallStack cs = new WithCallStack(new CallFrame("sh", cmd.getMeta()))) {
                        throw new TimeoutException(
                                String.format("Shell execution timeout after %ds", timeoutMillis));
                    //}
                }
                else if (exitCode != 0 && throwExOnFailure) {
                    //try (WithCallStack cs = new WithCallStack(new CallFrame("sh", cmd.getMeta()))) {
                        final VncVal out = future_stdout.get();
                        final VncVal err = future_stderr.get();

                        final String sOut = out == Constants.Nil ? null : StringUtil.trimToNull(out.toString());
                        final String sErr = err == Constants.Nil ? null : StringUtil.trimToNull(err.toString());

                        final String sErrOverview;
                        if (sErr != null) {
                            sErrOverview = "\n\nstderr:\n" + StringUtil.truncate(sErr, 250, "...");
                        }
                        else if (sOut != null) {
                            sErrOverview = "\n\nstdout:\n" + StringUtil.truncate(sOut, 250, "...");
                        }
                        else {
                            sErrOverview = "";
                        }

                        throw new ShellException(
                                String.format(
                                    "Shell execution failed: (sh %s). Exit code: %d%s",
                                    cmd.stream()
                                       .map(v -> v.toString())
                                       .collect(Collectors.joining(" ")),
                                    exitCode,
                                    sErrOverview),
                                exitCode,
                                sOut,
                                sErr);
                    //}
                }
                else {
                    return VncHashMap.of(
                            new VncKeyword(":exit"), new VncLong(exitCode),
                            new VncKeyword(":out"),  future_stdout.get(),
                            new VncKeyword(":err"),  future_stderr.get());
                }
            }
        }
        catch(ShellException ex) {
            throw ex;
        }
        catch(TimeoutException ex) {
            throw ex;
        }
        catch(Exception ex) {
            try (WithCallStack cs = new WithCallStack(new CallFrame("sh", cmd))) {
                throw new ShellException(
                        String.format(
                                "Shell execution processing failed: (sh %s)",
                                cmd.stream()
                                   .map(v -> v.toString())
                                   .collect(Collectors.joining(" "))),
                        ex);
            }
        }
    }

    private static ExecutorService getExecutorService(final ExecutorService suppliedExecService) {
        return suppliedExecService != null
                ? suppliedExecService
                : mngdExecutor.getExecutor();
    }


    private static File toFile(final VncVal dir) {
        if (dir == Nil) {
            return null;
        }
        else if (Types.isVncString(dir)) {
            return new File(((VncString)dir).getValue());
        }
        else if (Types.isVncJavaObject(dir, File.class)) {
            return (File)((VncJavaObject)dir).getDelegate();
        }
        else if (Types.isVncJavaObject(dir, Path.class)) {
            return ((Path)((VncJavaObject)dir).getDelegate()).toFile();
        }

        return null;
    }

    private static String[] toStringArray(final VncList list) {
        return list
                .stream()
                .map(it -> CoreFunctions.name.apply(VncList.of(it)))
                .map(it -> ((VncString)it).getValue())
                .collect(Collectors.toList())
                .toArray(new String[] {});
    }

    private static String[] toEnvStrings(final VncVal envMap) {
        if (envMap == Nil || ((VncMap)envMap).isEmpty()) {
            return null;
        }
        else {
            return ((VncMap)envMap)
                        .entries()
                        .stream()
                        .map(e ->
                            String.format(
                                    "%s=%s",
                                    CoreFunctions.name.apply(VncList.of(e.getKey())),
                                    e.getValue().toString()))
                        .collect(Collectors.toList())
                        .toArray(new String[] {});
        }
    }
    private static void validateProcessDir(final File dir) {
        if (dir != null && !dir.isDirectory()) {
            throw new ShellException(
                    String.format(
                        "Shell execution failed: The process directory '%s' does not exist!",
                        dir.getPath()));
        }
    }

    private static String getEncoding(final VncVal enc) {
        return enc == Nil
                ? Charset.defaultCharset().name()
                : ((VncString)CoreFunctions.name.apply(VncList.of(enc))).getValue();
    }

    private static Object copyAndClose(
            final VncString data,
            final Charset charset,
            final OutputStream os
    ) throws IOException {
        IOStreamUtil.copyStringToOS(data.getValue(), os, charset);
        os.flush();
        os.close();
        return null;
    }

    private static Object copyAndClose(final VncByteBuffer data, final OutputStream os)
    throws IOException {
        IOStreamUtil.copyByteArrayToOS(data.getBytes(), os);
        os.flush();
        os.close();
        return null;
    }

    private static Object copyAndClose(final File data, final OutputStream os)
    throws IOException {
        IOStreamUtil.copyFileToOS(data, os);
        os.flush();
        os.close();
        return null;
    }
    private static void slurp(final InputStream is, final String enc, final VncFunction fn) {
        try {
            final BufferedReader rd = new BufferedReader(new InputStreamReader(is, enc));
            while(true) {
                final String line = rd.readLine();
                if (line == null) {
                    break;
                }
                else {
                    fn.apply(VncList.of(new VncString(line)));
                }
            }
        }
        catch(Exception ex) {
            fn.apply(VncList.of(Nil));
        }
    }

    private static VncString slurpToString(
            final InputStream is,
            final Charset charset
    ) throws Exception{
        return new VncString(IOStreamUtil.copyIStoString(is, charset));
    }

    private static VncByteBuffer slurpToBytes(final InputStream is) throws Exception{
        return new VncByteBuffer(IOStreamUtil.copyIStoByteArray(is));
    }

    public static void shutdown() {
        mngdExecutor.shutdown();
    }


    private static final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-shell-pool", 6);  // 3 is the absolute minimum
}
