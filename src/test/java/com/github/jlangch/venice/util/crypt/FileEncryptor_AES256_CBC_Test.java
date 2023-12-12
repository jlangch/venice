package com.github.jlangch.venice.util.crypt;

import static com.github.jlangch.venice.util.crypt.FileEncryptor_AES256_CBC.decryptFileWithKey;
import static com.github.jlangch.venice.util.crypt.FileEncryptor_AES256_CBC.decryptFileWithPassphrase;
import static com.github.jlangch.venice.util.crypt.FileEncryptor_AES256_CBC.encryptFileWithKey;
import static com.github.jlangch.venice.util.crypt.FileEncryptor_AES256_CBC.encryptFileWithPassphrase;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.SecureRandom;

import org.junit.jupiter.api.Test;


public class FileEncryptor_AES256_CBC_Test {

	@Test
    public void testPassphrase() throws Exception {
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


	@Test
    public void testKey() throws Exception {
	    byte[] key = new byte[32];
	    new SecureRandom().nextBytes(key);

        final byte[] data = "1234567890".getBytes("UTF-8");
        final byte[] encrypted = encryptFileWithKey(key, data );
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
