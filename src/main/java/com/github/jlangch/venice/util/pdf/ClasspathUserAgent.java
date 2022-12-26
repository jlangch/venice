/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.util.pdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;


/**
 * An XhtmlRenderer User Agent that loads resources from the classpath
 *
 * <pre>
 *   ITextRenderer renderer = new ITextRenderer(..);
 *   ITextUserAgent userAgent = new ClasspathUserAgent(renderer.getOutputDevice());
 *   userAgent.setSharedContext(renderer.getSharedContext());
 *   renderer.getSharedContext().setUserAgentCallback(userAgent);
 *   renderer.setDocument(doc, "classpath:/");
 * </pre>
 */
public class ClasspathUserAgent extends ITextUserAgent {

    public ClasspathUserAgent(
        final ITextOutputDevice outputDevice
    ) {
        super(outputDevice);
    }

    public ClasspathUserAgent addResource(final String path, final ByteBuffer data) {
        if (StringUtil.isBlank(path)) {
            throw new IllegalArgumentException("A 'resource' path must not be blank");
        }
        if (data == null) {
            throw new IllegalArgumentException("A 'resource' data must not be null");
        }

        if (isClasspathScheme(path) || isMemoryScheme(path)) {
            throw new RuntimeException(
                "An in-memory resource path must not be an URI with a scheme "
                + "like 'memory:/charts/001.png' just pass '/charts/001.png'. "
                + "Path was: " + path);
        }

        cachedResources.put(path, data);

        return this;
    }

    @Override
    protected InputStream resolveAndOpenStream(final String uri) {
        if (uri == null) return null;

        final boolean debug = isDebugScheme(uri);

        if (isClasspathScheme(uri)) {
            log(debug, "FlyingSaucer: Classpath URI=" + uri);

            // [1] try to get the resource from the cached resources
            ByteBuffer data = cachedResources.get(uri);
            if (data != null) {
                log(debug, String.format("FlyingSaucer: Resolved '" + uri + "' from cache."));

                return new ByteArrayInputStream(data.array());
            }

            // [2] try to get the resource from the classpath (root)
            final String path = stripLeadingSlashes(stripScheme(uri));
            data = slurp(new ClassPathResource(path));
            if (data != null) {
                final byte[] bytes = data.array();
                log(debug, String.format("FlyingSaucer: Resolved reource '%s' (%d bytes) from classpath.", path, bytes.length));

                cachedResources.put(uri, data);
                return new ByteArrayInputStream(bytes);
            }

            // [3] the resource has not been found
            log(debug, String.format("FlyingSaucer: Resource '%s' not found on classpath.", path));
            return null;
        }
        else if (isMemoryScheme(uri)) {
            log(debug, "FlyingSaucer: Memory URI=" + uri);

            final String path = stripScheme(uri);

            // try to get the resource from the cached resources
            final ByteBuffer data = cachedResources.get(path);
            if (data != null) {
                final byte[] bytes = data.array();
                log(debug, String.format("FlyingSaucer: Resolved '%s' (%d bytes) from memory.", path, bytes.length));

                return new ByteArrayInputStream(bytes);
            }

            log(debug, String.format("FlyingSaucer: Resource '%s' not found in memory.", path));
            return null; // the resource has not been found
        }
        else if (isFileScheme(uri)) {
            log(debug, "FlyingSaucer: File URI=" + uri);

            // [1] try to get the resource from the cached resources
            ByteBuffer data = cachedResources.get(uri);
            if (data != null) {
                log(debug, String.format("FlyingSaucer: Resolved '%s' from cache.", uri));

                return new ByteArrayInputStream(data.array());
            }

            // [2] try to get the resource from the file
            final String path = stripScheme(uri);
            try {
                data = ByteBuffer.wrap(Files.readAllBytes(new File(path).toPath()));
                if (data != null) {
                    final byte[] bytes = data.array();
                    log(debug, String.format("FlyingSaucer: Resolved reource '%s' (%d bytes) from file.", path, bytes.length));

                    cachedResources.put(uri, data);
                    return new ByteArrayInputStream(bytes);
                }
            }
            catch(Exception ex) { /*not handled here*/ }

            // [3] the resource has not been found
            log(debug, String.format("FlyingSaucer: Resource '%s' not found as file.", uri));
            return null;
        }
       else {
            log(debug, "FlyingSaucer: Unknown URI=" + uri);
            return super.resolveAndOpenStream(uri);
        }
    }

    private boolean isDebugScheme(final String uri) {
        return isScheme(uri, "classpath-debug:", "memory-debug:", "file-debug:");
    }

    private boolean isClasspathScheme(final String uri) {
        return isScheme(uri, "classpath:", "classpath-debug:");
    }

    private boolean isMemoryScheme(final String uri) {
        return isScheme(uri, "memory:", "memory-debug:");
    }

    private boolean isFileScheme(final String uri) {
        return isScheme(uri, "file:", "file-debug:");
    }

    private boolean isScheme(final String uri, final String ... scheme) {
        return Arrays.stream(scheme).anyMatch(s -> uri.startsWith(s));
    }

    private String stripScheme(final String uri) {
        final int pos = uri.indexOf(':');
        return pos < 0 ? uri : uri.substring(pos+1);
    }

    private String stripLeadingSlashes(final String path) {
        if (StringUtil.isBlank(path)) {
            return path;
        }
        else {
            String p = path.trim();
            while(p.startsWith("/")) {
                p = StringUtil.removeStart(p, "/");
            }
            return p;
        }
    }

    private ByteBuffer slurp(final ClassPathResource cpResource) {
        return cpResource.getResourceAsByteBuffer();
    }

    private void log(final boolean debug, final String message) {
        if (debug) {
            System.out.println(message);
        }
    }


    private final Map<String, ByteBuffer> cachedResources = new ConcurrentHashMap<>();
}
