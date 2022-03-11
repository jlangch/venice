package com.github.jlangch.venice.util.word;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;

import com.github.jlangch.venice.VncException;


public class Stamper {

	public static void stamp(
			final InputStream is,
			final OutputStream os,
			final Map<String,Object> properties
	) {
		// SpEl  with maps:  "properties['clientName']"
		//                   "#this['name']"
		
		@SuppressWarnings("unchecked")
		final DocxStamper<DocxStamperContext> stamper = new DocxStamperConfiguration().build();
		stamper.stamp(is, new DocxStamperContext(properties), os);
	}

	public static byte[] stamp(
			final byte[] in,
			final Map<String,Object> properties
	) {
		try (InputStream is = new ByteArrayInputStream(in);
			 ByteArrayOutputStream os = new ByteArrayOutputStream()
		) {
			stamp(is, os, properties);		
			return os.toByteArray();
		}
		catch(Exception ex) {
			throw new VncException("Failed to stamp a docx document", ex);
		}
	}

	public static ByteBuffer stamp(
			final ByteBuffer in,
			final Map<String,Object> properties
	) {
		return ByteBuffer.wrap(stamp(in.array(), properties));
	}
}
