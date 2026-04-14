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
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.function.Consumer;

import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.impl.functions.CoreSystemFunctions;
import com.github.jlangch.venice.impl.functions.IOFunctions;
import com.github.jlangch.venice.impl.functions.JsonFunctions;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.FileUtil;


public class ReplUpgrade {

    private ReplUpgrade(
             final LocalDateTime createdAt,
             final String upgradeVersion,
             final byte[] binary
   ) {
        this.createdAt = createdAt;
        this.upgradeVersion = StringUtil.trimToNull(upgradeVersion);
        this.name = "venice-" + upgradeVersion + ".jar";
        this.binary = binary;
    }


    public boolean isEmpty() {
        return createdAt == null
               || upgradeVersion == null
               || binary == null
               || binary.length == 0;
    }

    public String getUpgradeVersion() {
        return upgradeVersion;
    }

    public String getName() {
        return name;
    }

    public byte[] getBinary() {
        return binary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String toJson() {
        final VncOrderedMap map = VncOrderedMap.of(
                new VncString("createdAt"),
                new VncString(formatTimestamp(createdAt)),
                new VncString("upgradeVersion"),
                new VncString(upgradeVersion),
                new VncString("binary"),
                new VncString(base64Encode(binary)));

        return ((VncString)JsonFunctions.write_str.applyOf(map)).getValue();
    }

    public static ReplUpgrade fromJson(final String json) {
        final VncMap data = (VncMap)JsonFunctions.read_str.applyOf(new VncString(json));

        final VncString createdAt = (VncString)data.get(new VncString("createdAt"));
        final VncString upgradeVersion = (VncString)data.get(new VncString("upgradeVersion"));
        final VncString binary = (VncString)data.get(new VncString("binary"));

        return new ReplUpgrade(
                    parseTimestamp(createdAt.getValue()),
                    upgradeVersion.getValue(),
                    base64Decode(binary.getValue()));
    }

    public static ReplUpgrade read() throws IOException {
        final String json = new String(
                                    Files.readAllBytes(UPGRADE_FILE.toPath()),
                                    StandardCharsets.UTF_8);
        return ReplUpgrade.fromJson(json);
    }

    public void write() throws IOException {
        final String json = toJson();

        Files.write(
                UPGRADE_FILE.toPath(),
                json.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
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

    public static ReplUpgrade initiate(final String latestVersion, final Consumer<String> log) {
        log.accept("Downloading 'venice-" + latestVersion + ".jar' ...");

        final byte[] binary = downloadVeniceJar(latestVersion);

        log.accept("Downloaded 'venice-" + latestVersion + ".jar'");

        try {
            final ReplUpgrade data = new ReplUpgrade(LocalDateTime.now(), latestVersion, binary);
            data.write();

            log.accept("New Venice version " + latestVersion + " ready for upgrade.");

            return data;
       }
        catch(Exception ex) {
            throw new RuntimeException(
                    "Failed to save downloaded Venice version for upgrade!");
        }
    }

    public static String upgrade() {
        try {
            if (!existsUpgradeFile()) {
                throw new RuntimeException("There is no initiated Venice upgrade!");
            }

            final ReplUpgrade data = read();

            final String currVersion = currentVersion();
            final String upgradeVersion = data.getUpgradeVersion();

            if (upgradeVersion == null) {
                throw new RuntimeException("There is no version for upgrading Venice!");
            }

            if (currVersion.equals(upgradeVersion)) {
                throw new RuntimeException("There is no newer version for upgrading Venice!");
            }

            final File currJar = new File("libs/venice-" + currVersion + ".jar");
            final File upgradeJar = new File("libs/venice-" + upgradeVersion + ".jar");

            if (upgradeJar.exists()) {
                throw new RuntimeException("There is no newer version for upgrading Venice!");
            }

            // [1] save the new version
            FileUtil.save(data.getBinary(), upgradeJar, true);
            System.out.println("Copying new version to " + upgradeJar);

            // [2] remove the old version
            if (!currJar.delete()) {
                upgradeJar.delete();
                throw new RuntimeException("Failed to upgrade Venice to " + upgradeVersion
                                           + ". Could to replace the old version!");
            }
            else {
                System.out.println("Deleted old version " + currJar);
                return data.getUpgradeVersion();
            }
        }
        catch(IOException ex) {
            throw new RuntimeException("Failed to read Venice upgrade data");
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            removeUpgradeFile();
        }
    }


    private static boolean existsUpgradeFile() {
        return UPGRADE_FILE.exists();
    }

    private static void removeUpgradeFile() {
        try {
            UPGRADE_FILE.delete();
        }
        catch(Exception ex) {
            // skipped (best effort)
        }
    }

    public boolean oudated() {
        return createdAt == null || ChronoUnit.HOURS.between(createdAt, readAt) > 2L;
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

    private static String formatTimestamp(final LocalDateTime dt) {
        return dtFormatter.format(dt);
    }

    private static LocalDateTime parseTimestamp(final String s) {
        try {
            return LocalDateTime.parse(s, dtFormatter);
        }
        catch (Exception ex) {
            return null;
        }
    }

    private static String base64Encode(final byte[] buf) {
        return Base64.getEncoder().encodeToString(buf);
    }

    private static byte[] base64Decode(final String base64) {
        return Base64.getDecoder().decode(base64);
    }



    public final static File UPGRADE_FILE = new File(".repl.upgrade");
    private final static DateTimeFormatter dtFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final String upgradeVersion;
    private final LocalDateTime createdAt;
    private final String name;
    private final byte[] binary;
    private final LocalDateTime readAt = LocalDateTime.now();
}
