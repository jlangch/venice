/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.util.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.github.jlangch.venice.impl.util.CollectionUtil;


public class Keystores {

    public static KeyStore load(
            final InputStream is,
            final String password
    ) throws KeyStoreException,
             NoSuchAlgorithmException,
             CertificateException,
             IOException
    {
        final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is, password.toCharArray());

        return keystore;
    }

    public static KeyStore load(
            final byte[] ks,
            final String password
    ) throws KeyStoreException,
             NoSuchAlgorithmException,
             CertificateException,
             IOException {
        try(ByteArrayInputStream is = new ByteArrayInputStream(ks)) {
            return load(is, password);
        }
    }

    public static List<String> aliases(final KeyStore keystore) throws KeyStoreException {
        return CollectionUtil.toList(keystore.aliases());
    }

    public static X509Certificate certificate(final KeyStore keystore, final String alias) throws KeyStoreException {
        return ((X509Certificate)keystore.getCertificate(alias));
    }

    public static String subjectDN(final KeyStore keystore, final String alias) throws KeyStoreException {
        return certificate(keystore, alias).getSubjectDN().getName();
    }

	public static Map<String,Object> parseSubjectDN(final KeyStore keystore, final String alias) {
		try {
			return parseDN(subjectDN(keystore, alias));
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

    public static String issuerDN(final KeyStore keystore, final String alias) throws KeyStoreException {
        return certificate(keystore, alias).getIssuerDN().getName();
    }

	public static Map<String,Object> parseIssuerDN(final KeyStore keystore, final String alias) {
		try {
			return parseDN(issuerDN(keystore, alias));
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static Map<String,Object> parseDN(final String dn) {
		try {
			return parse(new LdapName(dn));
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

    public static boolean hasExpired(final KeyStore keystore, final String alias) throws KeyStoreException {
        return expiryDate(keystore, alias).isBefore(LocalDateTime.now());
    }

    public static LocalDateTime expiryDate(final KeyStore keystore, final String alias) throws KeyStoreException {
        return toLocalDateTime(certificate(keystore, alias).getNotAfter());
    }

    public static LocalDateTime expiryDate(final KeyStore keystore) throws KeyStoreException {
        Date expiryDate = null;

        for(String alias: aliases(keystore)) {
            expiryDate = ((X509Certificate)keystore.getCertificate(alias)).getNotAfter();
        }

        return toLocalDateTime(expiryDate);
    }


    private static LocalDateTime toLocalDateTime(final Date date) {
            if (date == null) {
                return null;
            }
            else {
                final long millis = date.getTime();

                return Instant.ofEpochMilli(millis)
                              .atZone(ZoneId.systemDefault())
                              .toLocalDateTime();
            }
    }

	private static Map<String,Object> parse(final LdapName ln) {
		final Map<String,Object> elements = new HashMap<>();

		for(int ii=0; ii<ln.size(); ii++) {
			final Rdn rdn = ln.getRdn(ii);
			elements.put(rdn.getType(), rdn.getValue());
		}

		return elements;
	}

}
