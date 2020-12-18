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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;


public class SHA1_Hash {

	/**
	 * Hash a text
	 * 
	 * @param text a text
	 * @param salt a salt
	 * @return the hash
	 */
	public static byte[] hash(final String text, final String salt) {
		if (text == null) {
			throw new IllegalArgumentException("A text must not be null");
		}
		
		try {
			return hash(text.getBytes("UTF-8"), salt);
		}
		catch(UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}	
	}

	/**
	 * Hash a text
	 * 
	 * @param buffer a buffer
	 * @param salt a salt
	 * @return the hash
	 */
	public static byte[] hash(final byte[] buffer, final String salt) {
		if (buffer == null) {
			throw new IllegalArgumentException("A buffer must not be null");
		}
		        
		try {
	        final MessageDigest md = MessageDigest.getInstance("SHA-1");
	        md.reset();
	        if (salt != null) {
	        	md.update(salt.getBytes("UTF-8"));
	        }
			md.update(buffer);
			return md.digest();
		} 
		catch (Exception ex) {
			throw new HashException("Failed to compute SHA-1 hash.",ex);
		}
 	}

}
