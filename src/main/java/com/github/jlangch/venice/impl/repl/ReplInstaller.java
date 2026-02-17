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
package com.github.jlangch.venice.impl.repl;

import org.jline.utils.OSUtils;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.repl.ReplConfig.ColorMode;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class ReplInstaller {

    public static boolean install(final String[] args) {
        try {
            final IInterceptor interceptor = new AcceptAllInterceptor();

            ThreadContext.setInterceptor(interceptor);

            final CommandLineArgs cli = new CommandLineArgs(args);

            final String installDir = cli.switchValue("-dir", ".");
            final boolean minimal = cli.switchPresent("-minimal");

            System.out.println("Venice" + (minimal ? " minimal " : " ") + "REPL setup...");
            System.out.println("Venice REPL: V" + Venice.getVersion());
            System.out.println("Java: " + System.getProperty("java.version"));

            final ReplConfig config = ReplConfig.load(cli, null);

            final VeniceInterpreter venice = new VeniceInterpreter(interceptor);

            final Env env = venice.createEnv(false, false, RunMode.SCRIPT)
                                  .setGlobal(new Var(
                                                  new VncSymbol("*ARGV*"),
                                                  VncList.empty(),
                                                  false,
                                                  Var.Scope.Global))
                                  .setStdoutPrintStream(System.out)
                                  .setStderrPrintStream(System.err)
                                  .setStdinReader(null);

            // on Windows enforce dark mode
            final ColorMode colorMode = config.isColorModeLight() && OSUtils.IS_WINDOWS
                                            ? ColorMode.Dark
                                            : config.getColorMode();

            // slashify 'installDir' to prevent char escaping for windows paths when
            // passing as text parameter to repl-setup/setup script!!
            final String script = String.format(
                                    "(do                                        \n" +
                                    "  (load-module :repl-setup)                \n" +
                                    "  (repl-setup/setup :color-mode :%s        \n" +
                                    "                    :ansi-terminal false   \n" +
                                    "                    :minimal %b            \n" +
                                    "                    :install-dir \"%s\"))  ",
                                    colorMode.name().toLowerCase(),
                                    minimal,
                                    slashifyFilePath(installDir));

            venice.RE(script, "setup", env);

            return true;
        }
        catch (VncException ex) {
            ex.printStackTrace(System.err);
            System.err.println();
            System.err.println(ex.getCallStackAsString(""));
            System.err.println();
            System.err.println("REPL setup failed!");

            return false;
        }
        catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.err.println();
            System.err.println("REPL setup failed!");

            return false;
        }
        finally {
            ThreadContext.remove();
        }
    }


    private static String slashifyFilePath(final String path) {
        return  path.replace('\\', '/');
    }
}
