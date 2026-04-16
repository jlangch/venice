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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
            final File replHome = ReplDirs.getReplHomeDir();

            // try local repo first
            final String latestLocalRepo = newestVersionLocalRepo(replHome);
            if (latestLocalRepo != null) {
                return latestLocalRepo;
            }
            else {
                final VncVal latest = CoreSystemFunctions.latest.applyOf();
                return latest == Constants.Nil
                        ? null
                        : Coerce.toVncString(latest).getValue();
            }
        }
        catch(Exception ex) {
            return null;
        }
    }

    public static boolean isNewerVersionAvailable() {
        final String currVersion = currentVersion();
        final String latestVersion = stripLocalRepoPrefix(latestVersion());

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

        VeniceJar veniceJar = null;

        if (hasLocalRepoPrefix(latestVersion)) {
            veniceJar = loadFromLocalRepo(replHome, stripLocalRepoPrefix(latestVersion));
            log.accept("Downloaded " + veniceJar.name + " from local repo");
        }
        else {
            log.accept("Downloading venice-" + latestVersion + ".jar ...");
            veniceJar = downloadVeniceJar(latestVersion);
            log.accept("Downloaded " + veniceJar.name + ".jar'");
        }

        final String veniceVersion = stripLocalRepoPrefix(latestVersion);

        final String versionFileName = "version";
        final String jarFileName =  veniceJar.name;

        try {
            deleteDirRecursively(upgradeDir);

            FileUtil.mkdir(upgradeDir);

            // {REPL_HOME}/.upgrade/version
            Files.write(
                    new File(upgradeDir, versionFileName).toPath(),
                    veniceVersion.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            // {REPL_HOME}/.upgrade/venice-1.x.y.jar
            Files.write(
                    new File(upgradeDir, jarFileName).toPath(),
                    veniceJar.binary,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            log.accept("Ready for upgrading to Venice " + veniceVersion);
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

            if (upgradeLibsJar.exists()) {
                throw new RuntimeException("There is no newer version for upgrading Venice!");
            }

            // read: {REPL_HOME}/.upgrade/venice-x.y.z.jar
            final byte[] binary = Files.readAllBytes(new File(upgradeDir, jarName).toPath());

            // list old Venice versions
            final File[] oldVersions = libsDir.listFiles(new RegexFileFilter("venice-.*.jar"));

            // save the new version
            FileUtil.save(binary, upgradeLibsJar, true);

            // remove the old Venice versions
            if (oldVersions.length > 0) {
                for(File f : oldVersions) {
                   f.delete();
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
            deleteDirRecursively(upgradeDir);
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
                                .anyMatch(line -> line.contains("com.github.jlangch.venice.Upgrader"));
                }
            }
            else if (OS.isWindows()) {
                final File replSh = new File(replHome, "repl.bat");
                if (replSh.isFile()) {
                    return Files.readAllLines(replSh.toPath())
                                .stream()
                                .anyMatch(line -> line.contains("com.github.jlangch.venice.Upgrader"));
                }
            }

            return false;
        }
        catch(Exception ignore) {
            return false;
        }
    }

    private static String newestVersionLocalRepo(final File replHome) {
        try {
            final File localRepo = new File(replHome, "local-repo");
            if (localRepo.isDirectory()) {
                final List<File> files = Arrays.asList(localRepo.listFiles(new RegexFileFilter("venice-.*[.]jar")));
                return files.stream()
                            .map(f -> f.getName())
                            .map(f -> extractVeniceJarVersion(f))
                            .sorted(Comparator.comparing(v -> v))
                            .map(f -> "local:" + f)
                            .findFirst()
                            .orElse(null);
            }

            return null;
        }
        catch(Exception ex) {
            return null;
        }
    }

    private static VeniceJar loadFromLocalRepo(final File replHome, final String version) {
        final String jarName = "venice-" + version + ".jar";
        final File localRepo = new File(replHome, "local-repo");
        final File file = new File(localRepo, jarName);

        if (file.isFile()) {
            try {
                return new VeniceJar(jarName, Files.readAllBytes(file.toPath()));
            }
            catch(Exception ex) {
                throw new RuntimeException(
                        "Failed to upgrade Venice to " + version
                        + ". Could to download the new version from the local repo!");
            }
        }
        else {
            throw new RuntimeException(
                    "Failed to upgrade Venice to " + version
                    + ". The new version does not exist in the local repo!");
        }
    }

    private static VeniceJar downloadVeniceJar(final String version) {
        try {
            final String jarName = "venice-" + version + ".jar";

            // standard download
            final String url = "https://repo1.maven.org/maven2/com/github/jlangch/venice/"
                               + version + "/" + jarName;

            final VncByteBuffer jar = (VncByteBuffer)IOFunctions.io_download.applyOf(
                                            new VncString(url),
                                            new VncKeyword("binary"),
                                            VncBoolean.True,
                                            new VncKeyword("user-agent"),
                                            new VncString("Mozilla"));

            return new VeniceJar(jarName, jar.getBytes());
        }
        catch(Exception ex) {
            throw new RuntimeException(
                        "Failed to upgrade Venice to " + version
                        + ". Could to download the new version from Maven repo!");
        }
    }

    private static String extractVeniceJarVersion(final String filename) {
        if (filename.matches("^venice[-][0-9]+[.][0-9]+[.][0-9]+.*[.]jar$")) {
            String v = filename;
            if (v.startsWith("venice-")) v = v.replace("venice-", "");
            if (v.endsWith(".jar")) v = v.replace(".jar", "");
            return v;
        }
        else {
            return null;
        }
    }


    private static boolean deleteDirRecursively(final File dir) {
        if (dir.isDirectory()) {
            try {
                Files.walk(dir.toPath())
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
            catch(Exception ignore) {}
        }

        return !dir.isDirectory();
    }

    private static String stripLocalRepoPrefix(final String version) {
        return version.startsWith("local:")
                ? version.substring("local:".length())
                : version;
    }

    private static boolean hasLocalRepoPrefix(final String version) {
        return version.startsWith("local:");
    }

    private static class VeniceJar {
        public VeniceJar(String name, byte[] binary) {
            this.name = name;
            this.binary = binary;
        }
        public final String name;
        public final byte[] binary;
    }

}
