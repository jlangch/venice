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
import java.util.Date;
import java.util.List;

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
		keystore.load(is,password.toCharArray());

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

	public List<String> aliases(final KeyStore keystore) throws KeyStoreException {
		return CollectionUtil.toList(keystore.aliases());
	}

	public Date expiryDate(final KeyStore keystore, final String alias) throws KeyStoreException {
		return ((X509Certificate)keystore.getCertificate(alias)).getNotAfter();
	}

	public Date expiryDate(final KeyStore keystore) throws KeyStoreException {
		Date expiryDate = null;

		for(String alias: aliases(keystore)) {
			expiryDate = ((X509Certificate)keystore.getCertificate(alias)).getNotAfter();
		}

		return expiryDate;
	}

}
