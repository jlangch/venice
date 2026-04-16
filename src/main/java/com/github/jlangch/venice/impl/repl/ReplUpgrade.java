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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.function.Consumer;

import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.impl.functions.CoreSystemFunctions;
import com.github.jlangch.venice.impl.functions.IOFunctions;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.io.RegexFileFilter;
import com.github.jlangch.venice.util.OS;


public class ReplUpgrade {

    private ReplUpgrade() {
    }


    public static String currentVersion() {
        return Version.VERSION;
    }

    public static String latestVersion() {
        try {
            final VncVal latest = CoreSystemFunctions.latest.applyOf();
            return latest == Constants.Nil
                    ? null
                    : Coerce.toVncString(latest).getValue();
        }
        catch(Exception ex) {
            return null;
        }
    }

    public static boolean isNewerVersionAvailable() {
        final String currVersion = currentVersion();
        final String latestVersion = latestVersion();

        if (latestVersion == null) {
            return false;
        }
        else {
            return VncBoolean.isTrue(
                CoreSystemFunctions.newerVersion_Q.applyOf(
                        new VncString(latestVersion),
                        new VncString(currVersion)));
        }
    }

    public static void initiate(
            final String latestVersion,
            final Consumer<String> log
    ) {
        final File replHome = ReplDirs.getReplHomeDir();
        final File upgradeDir = new File(replHome, ".upgrade");
        final String jarName =  "venice-" + latestVersion + ".jar";
        final String versionName = "version";

        log.accept("Downloading " + jarName + " ...");

        final byte[] binary = downloadVeniceJar(latestVersion);

        log.accept("Downloaded 'venice-" + latestVersion + ".jar'");

        try {
            FileUtil.mkdir(upgradeDir);

            // {REPL_HOME}/.upgrade/version
            Files.write(
                    new File(upgradeDir, versionName).toPath(),
                    latestVersion.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            // {REPL_HOME}/.upgrade/venice-1.x.y.jar
            Files.write(
                    new File(upgradeDir, jarName).toPath(),
                    binary,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
       }
        catch(Exception ex) {
            throw new RuntimeException(
                    "Failed to save downloaded Venice version for upgrade!");
        }
    }

    public static String upgrade() {
        final File replHome = ReplDirs.getReplHomeDir();
        final File libsDir = new File(replHome, "libs");
        final File upgradeDir = new File(replHome, ".upgrade");

        try {
            if (!upgradeDir.isDirectory()) {
                throw new RuntimeException("There is no initiated Venice upgrade!");
            }

            // read: {REPL_HOME}/.upgrade/version
            final byte[] versionData = Files.readAllBytes(new File(upgradeDir, "version").toPath());
            final String version = new String(versionData, StandardCharsets.UTF_8);
            final String jarName =  "venice-" + version + ".jar";
            final File upgradeLibsJar = new File(libsDir, "venice-" + version + ".jar");

            // read: {REPL_HOME}/.upgrade/venice-1.x.y.jar
            final byte[] binary = Files.readAllBytes(new File(upgradeDir, jarName).toPath());

            if (upgradeLibsJar.exists()) {
                throw new RuntimeException("There is no newer version for upgrading Venice!");
            }

            // list old Venice versions
            final File[] oldVersions = libsDir.listFiles(new RegexFileFilter("venice-.*.jar"));

            // save the new version
            FileUtil.save(binary, upgradeLibsJar, true);
            System.out.println("Copying new version to " + upgradeLibsJar);

            // remove the old Venice versions
            if (oldVersions.length > 0) {
                for(File f : oldVersions) {
                   f.delete();
                   System.out.println("Deleted old version " + f.getName());
                }
            }

            return version;
        }
        catch(IOException ex) {
            throw new RuntimeException("Failed to upgrade Venice", ex);
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            try {
                Files.walk(upgradeDir.toPath())
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
            catch(Exception ignore) {}
        }
    }

    public static boolean isReplSupportingUpgrades() {
        final File replHome = ReplDirs.getReplHomeDir();

        if (replHome == null || !replHome.isDirectory()) {
            return false;
        }

        try {
            if (OS.isLinux() || OS.isMacOSX()) {
                final File replSh = new File(replHome, "repl.sh");
                if (replSh.isFile()) {
                    return Files.readAllLines(replSh.toPath())
                                .stream()
                                .anyMatch(line -> line.contains("-repl-upgrade"));
                }
            }
            else if (OS.isWindows()) {
                final File replSh = new File(replHome, "repl.bat");
                if (replSh.isFile()) {
                    return Files.readAllLines(replSh.toPath())
                                .stream()
                                .anyMatch(line -> line.contains("-repl-upgrade"));
                }
            }

            return false;
        }
        catch(Exception ignore) {
            return false;
        }
    }

    private static byte[] downloadVeniceJar(final String upgradeVersion) {
        try {
            final String url = "https://repo1.maven.org/maven2/com/github/jlangch/venice/"
                               + upgradeVersion + "/venice-" + upgradeVersion + ".jar";

            final VncByteBuffer jar = (VncByteBuffer)IOFunctions.io_download.applyOf(
                                            new VncString(url),
                                            new VncKeyword("binary"),
                                            VncBoolean.True,
                                            new VncKeyword("user-agent"),
                                            new VncString("Mozilla"));

            return jar.getBytes();
        }
        catch(Exception ex) {
            throw new RuntimeException(
                        "Failed to upgrade Venice to " + upgradeVersion
                        + ". Could to download the new version from Maven repo!");
        }
    }

}
