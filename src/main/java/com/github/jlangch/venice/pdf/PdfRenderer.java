/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXParseException;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.Tuple2;


public class PdfRenderer {

	public static ByteBuffer render(
			final String xhtml
	) {
		return render(xhtml, null, null, null);
	}

	public static ByteBuffer render(
			final String xhtml,  
			final String baseUrl,
			final List<String> alternateBasePaths
	) {
		return render(xhtml, baseUrl, alternateBasePaths, null);
	}
	
	public static ByteBuffer render(
			final String xhtml,  
			final String baseUrl,
			final List<String> alternateBasePaths,
			final Map<String,ByteBuffer> resources
	) {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			final ITextRenderer renderer = new ITextRenderer(DOTS_PER_POINT, DOTS_PER_PIXEL);

			final ClasspathUserAgent userAgent = new ClasspathUserAgent(renderer.getOutputDevice());
			if (alternateBasePaths != null) {
				for(String path : alternateBasePaths) {
					userAgent.addAlternateBasePath(path);
				}
			}
			if (resources != null) {
				for(Map.Entry<String,ByteBuffer> entry : resources.entrySet()) {
					userAgent.addResource(entry.getKey(), entry.getValue());
				}
			}

			userAgent.setSharedContext(renderer.getSharedContext());
			renderer.getSharedContext().setUserAgentCallback(userAgent);

			// PDF meta data creation listener
			final PdfMetaDataCreationListener mcl = new PdfMetaDataCreationListener()
															.parseMetaTags(parseXHTML(xhtml));
			renderer.setListener(mcl);

			renderer.setDocumentFromString(xhtml, baseUrl);
			renderer.layout();
			renderer.createPDF(os);
			os.flush();

			return ByteBuffer.wrap(os.toByteArray());
		}
		catch(Exception ex) {
			throw new RuntimeException("Failed to render PDF cheatsheet.", ex);
		}		
	}
	
	private static Document parseXHTML(final String xhtml) throws Exception {
		try {
			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			final InputStream is = new ByteArrayInputStream(xhtml.getBytes("UTF-8"));
		
			return builder.parse(is);
		}
		catch(SAXParseException ex) {
			// Get an extract of the failure to simplify error analysis (+/- 10 lines)
			final int lineNr = ex.getLineNumber();
			final String excerpt = (lineNr == -1) ? null : extractLines(xhtml, Math.max(0, lineNr-10), lineNr+10);
			throw new RuntimeException(
					String.format(
							"Invalid XHTML template regarding XML validation.\nXML parser error: %s %s",
							ex.getMessage(),
							excerpt == null ? "" : "\n" + excerpt), 
					ex);
		}		
		catch(Exception ex) {
			throw new RuntimeException("Failed to parse XHTML template", ex);
		}
	}
	
	private static String extractLines(
			final String text, 
			final int startLine, 
			final int endLine
	) {
		if (text == null || startLine > endLine) {
			return null;
		}
		
		final AtomicInteger lineNr = new AtomicInteger(0);
		
		return StringUtil
				.splitIntoLines(text)
				.stream()
				.map(line -> new Tuple2<Integer,String>(lineNr.incrementAndGet(), line))
				.filter(line -> line.getFirst() >= startLine &&  line.getFirst() <= endLine)
				.map(line -> String.format("%d: %s", line.getFirst(), line.getSecond()) )
				.collect(Collectors.joining("\n"));
	}
	

	private static final int DOTS_PER_PIXEL = 20;
	private static final float DOTS_PER_POINT = (float)DOTS_PER_PIXEL * 96f / 72f;
}
