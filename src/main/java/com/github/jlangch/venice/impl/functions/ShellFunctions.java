/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertMinArity;
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.github.jlangch.venice.ShellException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncThreadLocal;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CallFrame;
import com.github.jlangch.venice.impl.util.IOStreamUtil;
import com.github.jlangch.venice.impl.util.ThreadPoolUtil;
import com.github.jlangch.venice.impl.util.WithCallStack;


public class ShellFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Shell
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction sh = 
		new VncFunction(
				"sh",
				VncFunction
					.meta()
					.arglists("(sh & args)")		
					.doc(
						"Passes the given strings to Runtime.exec() to launch a sub-process.\n\n" +
						"Options are\n" + 
						"  :in        may be given followed by input source as InputStream,\n" + 
						"             Reader, File, ByteBuf, or String, to be fed to the\n" + 
						"             sub-process's stdin.\n" + 
						"  :in-enc    option may be given followed by a String, used as a\n" + 
						"             character encoding name (for example \"UTF-8\" or\n" + 
						"             \"ISO-8859-1\") to convert the input string specified\n" + 
						"             by the :in option to the sub-process's stdin. Defaults\n" + 
						"             to UTF-8. If the :in option provides a byte array,\n" + 
						"             then the bytes are passed unencoded, and this option\n" + 
						"             is ignored.\n" + 
						"  :out-enc   option may be given followed by :bytes or a String. If\n" + 
						"             a String is given, it will be used as a character\n" + 
						"             encoding name (for example \"UTF-8\" or \"ISO-8859-1\")\n" + 
						"             to convert the sub-process's stdout to a String which is\n" + 
						"             returned. If :bytes is given, the sub-process's stdout\n" + 
						"             will be stored in a Bytebuf and returned. Defaults to\n" + 
						"             UTF-8.\n" + 
						"  :out-fn    a function with a single string argument that receives\n" + 
						"             line by line from the process' stdout. If passed the \n" + 
						"             :out value in the return map will be empty.\n" + 
						"  :err-fn    a function with a single string argument that receives\n" + 
						"             line by line from the process' stderr. If passed the \n" + 
						"             :err value in the return map will be empty.\n" + 
						"  :env       override the process env with a map.\n" + 
						"  :dir       override the process dir with a String or java.io.File.\n" + 
						"  :throw-ex  If true throw an exception if the exit code is not equal\n" + 
						"             to zero, if false returns the exit code. Defaults to\n" + 
						"             false. It's recommended to use (with-sh-throw (sh \"foo\"))\n" + 
						"             instead.\n" + 
						"\n" +
						"You can bind :env, :dir for multiple operations using with-sh-env or\n" + 
						"with-sh-dir. with-sh-throw is binds :throw-ex as true.\n" + 
						"\n" +
						"sh returns a map of\n" + 
						"  :exit => sub-process's exit code\n" + 
						"  :out  => sub-process's stdout (as Bytebuf or String)\n" + 
						"  :err  => sub-process's stderr (String via platform default encoding)")
					.examples(
						"(println (sh \"ls\" \"-l\"))",
						"(println (sh \"ls\" \"-l\" \"/tmp\"))", 
						"(println (sh \"sed\" \"s/[aeiou]/oo/g\" :in \"hello there\\n\"))",
						"(println (sh \"cat\" :in \"x\\u25bax\\n\"))",
						"(println (sh \"echo\" \"x\\u25bax\"))", 
						"(println (sh \"/bin/sh\" \"-c\" \"ls -l\"))", 

						"(sh \"ls\" \"-l\" :out-fn println)",
						"(sh \"ls\" \"-l\" :out-fn println :err-fn println)",

						";; background process\n" +
						"(println (sh \"/bin/sh\" \"-c\" \"sleep 30 >/dev/null 2>&1 &\"))",
						"(println (sh \"/bin/sh\" \"-c\" \"nohup sleep 30 >/dev/null 2>&1 &\"))",
						
						";; reads 4 single-byte chars\n" +
						"(println (sh \"echo\" \"x\\u25bax\" :out-enc \"ISO-8859-1\"))",
						
						";; reads binary file into bytes[]\n" +
						"(println (sh \"cat\" \"birds.jpg\" :out-enc :bytes))", 
						
						";; working directory\n" +
						"(println (with-sh-dir \"/tmp\" (sh \"ls\" \"-l\") (sh \"pwd\")))", 
						"(println (sh \"pwd\" :dir \"/tmp\"))", 
						
						";; throw an exception if the shell's subprocess exit code is not equal to 0\n" +
						"(println (with-sh-throw (sh \"ls\" \"-l\")))", 
						"(println (sh \"ls\" \"-l\" :throw-ex true))", 
						
						";; windows\n" +
						"(println (sh \"cmd\" \"/c dir 1>&2\"))")
					.build()
		) {	
			public VncVal apply(final VncList args) {
				assertMinArity("sh", args, 1);

				final VncVector v = parseArgs(args);
	
				final VncList cmd = Coerce.toVncList(v.first()).withMeta(args.getMeta());
				final VncMap opts = Coerce.toVncMap(v.second()).withMeta(args.getMeta());
				
				final ExecutorService executor = Executors.newFixedThreadPool(
													3,
													ThreadPoolUtil.createThreadFactory(
															"venice-shell-pool-%d", 
															threadPoolCounter,
															true /* daemon threads */));
				try {
					return exec(cmd, opts, executor);
				}
				finally {
					executor.shutdownNow();
				}
			}
	
			private static final long serialVersionUID = -1848883965231344442L;
		};
		
	
	///////////////////////////////////////////////////////////////////////////
	// Util
	///////////////////////////////////////////////////////////////////////////

	private static VncVal exec(
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
			
			//new ProcessBuilder().inheritIO().directory(dir_).command(cmdArr).environment()
			
			validateProcessDir(dir_);
			 
			final Process proc = Runtime.getRuntime().exec(cmdArr, envArr, dir_);

			final OutputStream stdin = proc.getOutputStream();

			Future<Object> future_stdin = null;
			if (in != Nil) {
				// spit to subprocess' stdin a string, a bytebuf, or a File
				
				if (Types.isVncString(in)) {
					future_stdin = executor.submit(
									() -> copyAndClose((VncString)in, getEncoding(inEnc), stdin));
				}
				else if (Types.isVncByteBuffer(in)) {
					future_stdin = executor.submit(
									() -> copyAndClose((VncByteBuffer)in, stdin));
				}
				else if (Types.isVncJavaObject(in, File.class)) {
					future_stdin = executor.submit(
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

				// slurp the subprocess' stdout (as string or bytebuf)			
				Future<VncVal> future_stdout;
				if ("bytes".equals(enc)) {
					future_stdout = executor.submit(() -> slurpToBytes(stdout));
				}
				else if (Types.isVncFunction(slurpOutFn)) {
					slurp(stdout, enc, (VncFunction)slurpOutFn);
					future_stdout = executor.submit(() -> VncString.empty());
				}
				else {
					future_stdout = executor.submit(() -> slurpToString(stdout, enc));
				}
				
				// slurp the subprocess' stderr as string with platform default encoding
				Future<VncVal> future_stderr; 
				if (Types.isVncFunction(slurpErrFn)) {
					slurp(stderr, enc, (VncFunction)slurpErrFn);
					future_stderr = executor.submit(() -> VncString.empty());
				}
				else {
					future_stderr = executor.submit(() -> slurpToString(stderr, enc));
				}
			
				// wait for the process to exit
				final int exitCode = proc.waitFor();
					
				if (future_stdin != null) {
					future_stdin.get();
				}

				if (exitCode != 0 && VncBoolean.isTrue(opts.get(new VncKeyword(":throw-ex")))) {
					try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("sh", cmd))) {
						throw new ShellException(
								String.format(
									"Shell execution failed: (sh %s). Exit code: %d", 
									((VncString)CoreFunctions.pr_str.apply(cmd)).getValue(),
									exitCode),
								exitCode); 
					}
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
		catch(Exception ex) {
			try (WithCallStack cs = new WithCallStack(CallFrame.fromVal("sh", cmd))) {
				throw new VncException(
						String.format(
								"Shell execution processing failed: (sh %s)", 
								((VncString)CoreFunctions.pr_str.apply(cmd)).getValue()),
						ex);
			}
		}
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
				.getList()
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

	private static VncVector parseArgs(final VncList args) {
		final VncThreadLocal th = new VncThreadLocal();
	
		VncHashMap options = new VncHashMap();
		VncList cmd = VncList.empty();		
		VncList args_ = args;
		while(!args_.isEmpty()) {
			final VncVal v = args_.first();
			args_ = args_.rest();
			
			if (Types.isVncKeyword(v) && optionKeywords.contains(v)) {
				final VncVal optVal = args_.first();
				args_ = args_.rest();
				options = options.assoc(v, optVal);
			}
			else {
				cmd = cmd.addAtEnd(new VncString(v.toString()));
			}
		}
		

		final VncMap defaultOptions = VncHashMap.of(
										new VncKeyword(":out-enc"), new VncString("UTF-8"),
										new VncKeyword(":in-enc"), new VncString("UTF-8"),
										new VncKeyword(":dir"), th.get(":*sh-dir*"),
										new VncKeyword(":throw-ex"), th.get(":*sh-throw-ex*"));

		final VncMap defaultEnv = (VncMap)th.get(":*sh-env*", new VncHashMap());
		
		
		// merge options
		VncMap opts = (VncMap)CoreFunctions.merge.apply(
								VncList.of(defaultOptions, options));
		
		// add merged :env map
		opts = opts.assoc(
					new VncKeyword(":env"),
					CoreFunctions.merge.apply(
							VncList.of(
									defaultEnv, 
									options.get(new VncKeyword(":env")))));
		
		return VncVector.of(cmd, opts);
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
			final String encoding, 
			final OutputStream os
	) throws IOException {
		IOStreamUtil.copyStringToOS(((VncString)data).getValue(), os, encoding);
		os.flush();
		os.close();
		return null;
	}
	
	private static Object copyAndClose(final VncByteBuffer data, final OutputStream os) 
	throws IOException {
		IOStreamUtil.copyByteArrayToOS(((VncByteBuffer)data).getBytes(), os);
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

	private static VncString slurpToString(final InputStream is, final String enc) throws Exception{
		return new VncString(IOStreamUtil.copyIStoString(is, enc));
	}

	private static VncByteBuffer slurpToBytes(final InputStream is) throws Exception{
		return new VncByteBuffer(IOStreamUtil.copyIStoByteArray(is));
	}

	
	private final static AtomicLong threadPoolCounter = new AtomicLong(0);

	private static final VncHashSet optionKeywords = VncHashSet.of(
														new VncKeyword(":in"),
														new VncKeyword(":in-enc"),
														new VncKeyword(":out-enc"),
														new VncKeyword(":out-fn"),
														new VncKeyword(":err-fn"),
														new VncKeyword(":env"),
														new VncKeyword(":dir"),
														new VncKeyword(":throw-ex"));

	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap
					.Builder()
					.add(sh)
					.toMap();	
}
