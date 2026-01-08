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
package com.github.jlangch.venice.util.password;

import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;


public class PBKDF2PasswordEncoder {

    public PBKDF2PasswordEncoder() {
    }

    /**
     * Encode (hash) a password. If the password is already encoded it will
     * be returned unchanged.
     *
     * @param password A clear text password
     *
     * @return The encoded password
     */
    public String encode(final String password) {
        if (StringUtil.isBlank(password)) {
            throw new IllegalArgumentException("A 'password' must not be blank");
        }

        return encodeWithSalt(password, createSalt());
    }


    /**
     * <p>Verifies if a clear text password is identical to a hashed password
     *
     * @param clearTextPwd  A clear text password
     * @param hashedPwd  A hashed salted password
     *
     * @return <code>true</code> if the two passwords are identical otherwise <code>false</code>
     */
    public boolean verify(final String clearTextPwd, final String hashedPwd) {
        try {
            final String salt = extractSalt(hashedPwd);
            final String hashed = encodeWithSalt(clearTextPwd, salt);
            return hashed.equals(hashedPwd);
        }
        catch(RuntimeException ex) {
            return false;
        }
    }


    private String encodeWithSalt(final String password, final String salt) {
        if (StringUtil.isEmpty(password)) {
            throw new IllegalArgumentException("A 'password' must not be empty");
        }
        if (StringUtil.isEmpty(salt)) {
            throw new IllegalArgumentException("A 'salt' must not be empty");
        }

        return toString(hash(password, salt), salt);
    }

    private String extractSalt(final String encodedPassword) {
        final int pos = encodedPassword.indexOf(SALT_DELIMITER);
        return pos >= 0
                ? encodedPassword.substring(pos + SALT_DELIMITER.length())
                : null;
    }

    private String toString(final byte[] hash, final String salt) {
        if (hash == null) {
            throw new IllegalArgumentException("A 'hash' must not be null");
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(toString(hash));
        if (StringUtil.isNotEmpty(salt)) {
            sb.append(SALT_DELIMITER);
            sb.append(salt);
        }

        return sb.toString();
    }

    private byte[] hash(final String password, final String salt) {
        if (StringUtil.isEmpty(password)) {
            throw new IllegalArgumentException("A 'password' must not be empty");
        }

        try {
            final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            final PBEKeySpec spec = new PBEKeySpec(
                                            password.toCharArray(),
                                            salt == null ? new byte[]{} : salt.getBytes("UTF-8"),
                                            1000,
                                            256);
            final SecretKey key = skf.generateSecret(spec);
            return key.getEncoded();
        }
        catch (Exception ex) {
            throw new VncException("Failed to compute PBKDF2 hash.",ex);
        }
    }

    private String createSalt() {
        synchronized(random) {
            final int salt = random.nextInt(10000000);
            return StringUtil.padLeft(String.valueOf(salt), 7, '0');
        }
    }

    private String toString(final byte[] data) {
        final StringBuilder sb = new StringBuilder();

        for(int ii=0; ii<data.length; ii++) {
            final String s = Integer.toHexString(0xFF & data[ii]);
            if (s.length() == 1) sb.append('0');
            sb.append(s);
        }

        return sb.toString();
    }


    private static final SecureRandom random = new SecureRandom();

    public static final String SALT_DELIMITER = ":";
}
