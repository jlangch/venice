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
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.FileUtil;


public class ReplUpgrade {

    private ReplUpgrade(final List<String> lines) {
        this.lines = lines;
        this.createdAt = lines.isEmpty() ? null : parse(lines.get(0));
    }

    public static String currentVersion() {
        return Version.VERSION;
    }

    public static String latestVersion() {
        try {
            final VncVal latest = CoreSystemFunctions.latest.applyOf();
            if (latest == Constants.Nil) {
                return null;
            }
            else {
                return Coerce.toVncString(latest).getValue();
            }
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

    public static void initiate(final String latestVersion) {
        try {
            final List<String> lines = new ArrayList<>();

            lines.add(format(LocalDateTime.now()));
            lines.add(latestVersion);

            Files.write(
                    UPGRADE_FILE.toPath(),
                    lines,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch(Exception ex) {
            // skipped (best effort)
        }
    }

    public static String upgrade() {
        try {
            if (!exists()) {
                throw new RuntimeException("There is no initiated Venice upgrade!");
            }

            final ReplUpgrade data = read();
            if (data.lines.isEmpty()) {
                throw new RuntimeException("Failed to read the Venice upgrade meta data!");
            }

            final String currVersion = currentVersion();
            final String upgradeVersion = data.upgradeVersion();

            if (upgradeVersion == null) {
                throw new RuntimeException("There is no version for upgrading Venice!");
            }

            if (currVersion.equals(upgradeVersion)) {
                throw new RuntimeException("There is no newer version for upgrading Venice!");
            }

            final File currJar = new File("lib/venice-" + currVersion + ".jar");
            final File upgradeJar = new File("lib/venice-" + upgradeVersion + ".jar");

            if (upgradeJar.exists()) {
                throw new RuntimeException("There is no newer version for upgrading Venice!");
            }

            // [1] download the new version
            final byte[] jar = download(upgradeVersion);

            // [2] save the new version
            FileUtil.save(jar, upgradeJar, true);

            // [3] remove the old version
            if (!currJar.delete()) {
                upgradeJar.delete();
                throw new RuntimeException("Failed to upgrade Venice to " + upgradeVersion
                                           + ". Could to replace the old version!");
            }
            else {
                return data.upgradeVersion();
            }
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            remove();
        }
    }

    private static ReplUpgrade read() {
        try {
            return new ReplUpgrade(
                    Files.readAllLines(UPGRADE_FILE.toPath())
                         .stream()
                         .map(s -> StringUtil.trimToNull(s))
                         .filter(s -> s != null)
                         .collect(Collectors.toList()));
        }
        catch(Exception ex) {
            return new ReplUpgrade(new ArrayList<>());
        }
    }

    private static boolean exists() {
        try {
            return UPGRADE_FILE.exists();
        }
        catch(Exception ex) {
            return false;
        }
    }

    private static void remove() {
        try {
            UPGRADE_FILE.delete();
        }
        catch(Exception ex) {
            // skipped (best effort)
        }
    }

    private String upgradeVersion() {
        try {
            return lines.size() == 2 ? lines.get(1) : null;
        }
        catch(Exception ex) {
            return null;
        }
    }

    public boolean oudated() {
        return createdAt == null || ChronoUnit.HOURS.between(createdAt, readAt) > 2L;
    }


    private static byte[] download(final String upgradeVersion) {
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
            throw new RuntimeException("Failed to upgrade Venice to " + upgradeVersion
                    + ". Could to download the new version from Maven repo!");
        }
    }

    private static String format(final LocalDateTime dt) {
        return dtFormatter.format(dt);
    }

    private static LocalDateTime parse(final String s) {
        try {
            return LocalDateTime.parse(s, dtFormatter);
        }
        catch (Exception ex) {
            return null;
        }
    }


    private final static File UPGRADE_FILE = new File(".repl.upgrade");
    private final static DateTimeFormatter dtFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final List<String> lines;
    private final LocalDateTime createdAt;
    private final LocalDateTime readAt = LocalDateTime.now();
}
