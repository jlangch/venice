package com.github.jlangch.venice.util.crypt;

import static com.github.jlangch.venice.util.crypt.FileEncryptor_ChaCha20_BouncyCastle.decryptFileWithKey;
import static com.github.jlangch.venice.util.crypt.FileEncryptor_ChaCha20_BouncyCastle.decryptFileWithPassphrase;
import static com.github.jlangch.venice.util.crypt.FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithKey;
import static com.github.jlangch.venice.util.crypt.FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithPassphrase;
import static com.github.jlangch.venice.util.crypt.FileEncryptor_ChaCha20_BouncyCastle.isSupported;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.SecureRandom;

import org.junit.jupiter.api.Test;


public class FileEncryptor_ChaCha20_BouncyCastle_Test {

	@Test
    public void testPassphrase() throws Exception {
		if (isSupported()) {
	        final byte[] data = "1234567890".getBytes("UTF-8");
	        final byte[] encrypted = encryptFileWithPassphrase("passphrase", data);
	        final byte[] decrypted = decryptFileWithPassphrase("passphrase", encrypted);

	        if (data.length != decrypted.length) {
	        	fail("FAIL (length)");
	        	return;
	        }

	        for(int ii=0; ii<data.length; ii++) {
	        	if (data[ii] != decrypted[ii]) {
	            	fail("FAIL (@index " + ii + ")");
	            	return;
	        	}
	        }
		}
    }


	@Test
    public void testKey() throws Exception {
		if (isSupported()) {
		    byte[] key = new byte[32];
		    new SecureRandom().nextBytes(key);

	        final byte[] data = "1234567890".getBytes("UTF-8");
	        final byte[] encrypted = encryptFileWithKey(key, data);
	        final byte[] decrypted = decryptFileWithKey(key, encrypted);

	        if (data.length != decrypted.length) {
	        	fail("FAIL (length)");
	        	return;
	        }

	        for(int ii=0; ii<data.length; ii++) {
	        	if (data[ii] != decrypted[ii]) {
	            	fail("FAIL (@index " + ii + ")");
	            	return;
	        	}
	        }
		}
    }

}