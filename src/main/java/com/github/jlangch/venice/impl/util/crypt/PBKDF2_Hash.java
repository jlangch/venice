/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.impl.util.crypt;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.github.jlangch.venice.impl.util.StringUtil;


public class PBKDF2_Hash {

	/**
	 * Hash a string
	 * 
	 * @param text a text
	 * @param salt a salt
	 * @return the hash
	 */
	public static byte[] hash(final String text, final String salt) {
		return hash(text, salt, 1000, 256);
	}

	/**
	 * Hash a string
	 * 
	 * @param text a text
	 * @param salt a salt
	 * @param iterationCount the number of iterations (e.g. 1000)
	 * @param keyLength the key length (e.g. 256)
	 * @return the hash
	 */
	public static byte[] hash(final String text, final String salt, int iterationCount, int keyLength) {
		if (StringUtil.isEmpty(text)) {
			throw new IllegalArgumentException("A 'text' must not be empty");
		}
		
		try {
			final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			final PBEKeySpec spec = new PBEKeySpec(
											text.toCharArray(), 
											salt == null ? new byte[]{} : salt.getBytes("UTF-8"), 
											iterationCount, 
											keyLength);
			return skf.generateSecret(spec).getEncoded();
		} 
		catch (Exception ex) {
			throw new HashException("Failed to compute PBKDF2 hash.",ex);
		}
	}

}
