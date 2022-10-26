/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Simple mime-type util for the most common mime-types
 */
public class MimeTypes {

    private MimeTypes() {
    }

    /**
     * Returns a file extension associated with a mime type.
     *
     * <p>E.g.: text/html ==&gt; html
     *
     * <p>Note: A mime type may have multiple file extensions assigned.
     *    E.g.: text/html ==&gt; html, htm
     *
     * @param mimeType A mime type
     * @return The mime type's file extension
     */
    public static List<String> getFileExtension(final String mimeType) {
        final List<String> list = mimeTypeFileExtMap.get(mimeType);

        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    public static String getMimeTypeFromFileExtension(final String fileExtension) {
        final String ext = StringUtil.trimToEmpty(fileExtension);

        return mimeTypeFileExtMap
                .entrySet()
                .stream()
                .filter(e -> e.getValue().contains(ext))
                .map(e -> e.getKey())
                .findFirst()
                .orElse(null);
    }

    public static String getMimeTypeFromFileName(final String fileName) {
        return getMimeTypeFromFileExtension(getFileExt(fileName));
    }

    public static String getMimeTypeFromFile(final File file) {
        return getMimeTypeFromFileName(file.getName());
    }

    public static Set<String> getAvailableMimeTypes() {
            return new HashSet<>(mimeTypeFileExtMap.keySet());
    }

    private static String getFileExt(final String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("A fileName must not be null");
        }

        int pos = fileName.lastIndexOf('.');
        return (pos < 0) ? null : fileName.substring(pos+1);
    }



    public static final String APPLICATION_PDF     = "application/pdf";
    public static final String APPLICATION_XML     = "application/xml";
    public static final String APPLICATION_ZIP     = "application/zip";
    public static final String APPLICATION_JSON    = "application/json";
    public static final String APPLICATION_XLS     = "application/vnd.ms-msexcel";
    public static final String APPLICATION_DOC     = "application/msword";
    public static final String APPLICATION_PPT     = "application/vnd.ms-powerpoint";
    public static final String APPLICATION_XLSX    = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String APPLICATION_DOCX    = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String APPLICATION_PPTX    = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    public static final String APPLICATION_TTF     = "application/x-font-ttf";
    public static final String APPLICATION_BINARY  = "application/octet-stream";
    public static final String TEXT_HTML           = "text/html";
    public static final String TEXT_XML            = "text/xml";
    public static final String TEXT_CSV            = "text/csv";
    public static final String TEXT_CSS            = "text/css";
    public static final String TEXT_PLAIN          = "text/plain";
    public static final String TEXT_URL            = "text/url";
    public static final String IMAGE_JPEG          = "image/jpeg";
    public static final String IMAGE_PNG           = "image/png";
    public static final String IMAGE_GIF           = "image/gif";
    public static final String IMAGE_SVG_XML       = "image/svg+xml";
    public static final String EMAIL               = "message/rfc822";



    private static final Map<String,List<String>> mimeTypeFileExtMap = new HashMap<>();

    static {
        mimeTypeFileExtMap.put(APPLICATION_PDF,    Arrays.asList("pdf"));
        mimeTypeFileExtMap.put(APPLICATION_XML,    Arrays.asList("xml"));
        mimeTypeFileExtMap.put(APPLICATION_JSON,   Arrays.asList("json"));
        mimeTypeFileExtMap.put(APPLICATION_ZIP,    Arrays.asList("zip"));
        mimeTypeFileExtMap.put(APPLICATION_TTF,    Arrays.asList("ttf"));
        mimeTypeFileExtMap.put(APPLICATION_BINARY, Arrays.asList("exe"));
        mimeTypeFileExtMap.put(TEXT_HTML,          Arrays.asList("html", "htm"));
        mimeTypeFileExtMap.put(TEXT_XML,           Arrays.asList("xml"));
        mimeTypeFileExtMap.put(TEXT_CSV,           Arrays.asList("csv"));
        mimeTypeFileExtMap.put(TEXT_CSS,           Arrays.asList("css"));
        mimeTypeFileExtMap.put(TEXT_PLAIN,         Arrays.asList("txt"));
        mimeTypeFileExtMap.put(TEXT_URL,           Arrays.asList("url"));
        mimeTypeFileExtMap.put(IMAGE_JPEG,         Arrays.asList("jpg"));
        mimeTypeFileExtMap.put(IMAGE_PNG,          Arrays.asList("png"));
        mimeTypeFileExtMap.put(IMAGE_GIF,          Arrays.asList("gif"));
        mimeTypeFileExtMap.put(IMAGE_SVG_XML,      Arrays.asList("svg"));
        mimeTypeFileExtMap.put(EMAIL,              Arrays.asList("eml"));

        // Microsoft (http://filext.com/faq/office_mime_types.php)
        mimeTypeFileExtMap.put(APPLICATION_XLS,    Arrays.asList("xls"));
        mimeTypeFileExtMap.put(APPLICATION_XLSX,   Arrays.asList("xlsx"));
        mimeTypeFileExtMap.put(APPLICATION_DOC,    Arrays.asList("doc"));
        mimeTypeFileExtMap.put(APPLICATION_DOCX,   Arrays.asList("docx"));
        mimeTypeFileExtMap.put(APPLICATION_PPT,    Arrays.asList("ppt"));
        mimeTypeFileExtMap.put(APPLICATION_PPTX,   Arrays.asList("pptx"));
    }
}
