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
package com.github.jlangch.venice.util;

import java.util.Objects;


public class VeniceVersion implements Comparable<VeniceVersion>{

    public VeniceVersion(
            final long major,
            final long minor,
            final long patch,
            final String suffix,
            final String version
    ) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.suffix = suffix;
        this.version = version;
    }


    public long getMajor() {
        return major;
    }

    public long getMinor() {
        return minor;
    }

    public long getPatch() {
        return patch;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getVersion() {
        return version;
    }


    public static VeniceVersion parse(final String version) {
        long major = 0;
        long minor = 0;
        long patch = 0;
        String suffix = "";

        if (version != null && !version.isEmpty()) {
            if (version.matches("^[0-9]+$")) {
                major = Long.parseLong(version);
            }
            else if (version.matches("^[0-9]+[.][0-9]+$")) {
                final String e[] = version.split("[.]");
                major = Long.parseLong(e[0]);
                minor = Long.parseLong(e[1]);
            }
            else if (version.matches("^[0-9]+[.][0-9]+[.][0-9]+$")) {
                final String e[] = version.split("[.]");
                major = Long.parseLong(e[0]);
                minor = Long.parseLong(e[1]);
                patch = Long.parseLong(e[2]);
            }
            else if (version.matches("^[0-9]+[.][0-9]+[.][0-9]+-.*$")) {
                final String e[] = version.split("[.-]");
                major = Long.parseLong(e[0]);
                minor = Long.parseLong(e[1]);
                patch = Long.parseLong(e[2]);
                suffix = version.substring(version.indexOf('-') + 1);
            }
        }

        return new VeniceVersion(major, minor, patch, suffix, version);
    }

    public boolean isEqual(final VeniceVersion other) {
       Objects.requireNonNull(other);

       return Objects.equals(major, other.major)
              && Objects.equals(minor, other.minor)
              && Objects.equals(patch, other.patch)
              && Objects.equals(suffix, other.suffix);
    }

    public boolean isNewerThan(final VeniceVersion other) {
        Objects.requireNonNull(other);

        if (major > other.major) return true;
        if (major < other.major) return false;

        if (minor > other.minor) return true;
        if (minor < other.minor) return false;

        if (patch > other.patch) return true;
        if (patch < other.patch) return false;

        if (suffix.isEmpty() && !other.suffix.isEmpty()) return true;
        if (!suffix.isEmpty() && other.suffix.isEmpty()) return false;
        return suffix.compareTo(other.suffix) > 0;
    }

    @Override
    public int compareTo(final VeniceVersion other) {
        if (other == null) {
            return 1;
        }
        else if (isEqual(other)) {
            return 0;
        }
        else if (isNewerThan(other)) {
            return 1;
        }
        else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return version;
    }

    private final long major;
    private final long minor;
    private final long patch;
    private final String suffix;
    private final String version;
}
