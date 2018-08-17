package com.github.jlangch.venice.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class StreamUtil {

    public static byte[] toByteArray(final InputStream is) throws IOException{
    	if (is == null) {
    		return null;
    	}
    	
    	try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
        	final byte[] buffer = new byte[16 * 1024];
        	int n;
        	while (-1 != (n = is.read(buffer))) {
        		output.write(buffer, 0, n);
        	}

        	return output.toByteArray();
        }
    }
    
    public static String toString(final InputStream is, final String encoding) throws IOException{
    	return is == null ? null : new String(StreamUtil.toByteArray(is), encoding);
    }
 
}
