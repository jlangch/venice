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
package com.github.jlangch.venice.impl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class StringUtil {

    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the first {@code max} values of the search String,
     * case sensitively/insensitively based on {@code ignoreCase} value.</p>
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for (case insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @param max  maximum number of values to replace, or {@code -1} if no maximum
     * @param ignoreCase if true replace is case insensitive, otherwise case sensitive
     * @return the text with any replacements processed,
     */
    public static String replace(
            final String text,
            final String searchString,
            final String replacement,
            final int max,
            final boolean ignoreCase
    ) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }

        final String textLowerCase = ignoreCase ? text.toLowerCase() : null;
        final String searchStringLowerCase  = ignoreCase ? searchString.toLowerCase() : null;


        int start = 0;
        int end = ignoreCase
                        ? textLowerCase.indexOf(searchStringLowerCase, start)
                        : text.indexOf(searchString, start);
        if (end == -1) {
            return text;
        }

        int count = max;

        final int replLength = searchString.length();
        final StringBuilder buf = new StringBuilder(text.length());
        while (end != -1) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--count == 0) {
                break;
            }
            end = ignoreCase
                        ? textLowerCase.indexOf(searchStringLowerCase, start)
                        : text.indexOf(searchString, start);
        }

        if (start < text.length()) {
            buf.append(text, start, text.length());
        }
        return buf.toString();
    }

    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the last value of the search String,
     * case sensitively/insensitively based on {@code ignoreCase} value.</p>
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for (case insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @param ignoreCase if true replace is case insensitive, otherwise case sensitive
     * @return the text with any replacements processed,
     */
    public static String replaceLast(
            final String text,
            final String searchString,
            final String replacement,
            final boolean ignoreCase
    ) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null) {
            return text;
        }

        final String textLowerCase = ignoreCase ? text.toLowerCase() : null;
        final String searchStringLowerCase  = ignoreCase ? searchString.toLowerCase() : null;

        int end = ignoreCase
                        ? textLowerCase.lastIndexOf(searchStringLowerCase)
                        : text.lastIndexOf(searchString);
        if (end == -1) {
            return text;
        }

        final StringBuilder buf = new StringBuilder(text.length());
        buf.append(text, 0, end);
        buf.append(replacement);
        buf.append(text, end + searchString.length(), text.length());

        return buf.toString();
    }

	/**
	 * Splits a string into columns
	 *
	 * @param text	a string
	 * @param colStartPos	a list of colum start pos
	 *
	 * @return the splitted columns
	 */
	public static List<String> splitColumns(final String text, final int[] colStartPos) {
		if (colStartPos == null || colStartPos.length == 0) {
			throw new IllegalArgumentException("A 'colStartPos' array must not be null or empty");
		}

		final List<String> columns = new ArrayList<>();

		String tmp = text;

		for(int ii=colStartPos.length-1; ii>=0; ii--) {
			int pos = colStartPos[ii];
			if (pos <= 0) {
				columns.add(StringUtil.trimToEmpty(tmp));
				tmp = "";
			}
			else if (pos >= tmp.length()) {
				columns.add("");
			}
			else {
				columns.add(StringUtil.trimToEmpty(tmp.substring(pos)));
				tmp = tmp.substring(0, pos);
			}
		}

		Collections.reverse(columns);
		return columns;
	}

    /**
     * Splits a text into lines
     *
     * @param text  a string
     *
     * @return the lines (maybe empty if the text was <code>null</code> or empty
     */
    public static List<String> splitIntoLines(final String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        else {
            try(final BufferedReader br = new BufferedReader(new StringReader(text))) {
                return br.lines().collect(Collectors.toList());
            }
            catch(IOException | RuntimeException ex) {
                throw new RuntimeException("Failed to split text into lines", ex);
            }
        }
    }

    /**
     * Escapes a text
     *
     * Backspace is replaced with \b
     * Form feed is replaced with \f
     * Newline is replaced with \n
     * Carriage return is replaced with \r
     * Tab is replaced with \t
     * Double quote is replaced with \"
     * Backslash is replaced with \\
     *
     * @param text text to escape
     * @return the escaped text
     */
    public static String escape(final String text) {
        if (text == null) {
            return text;
        }

        final StringBuilder sb = new StringBuilder();

        for(char c : text.toCharArray()) {
            switch(c) {
                case '\n': sb.append('\\').append('n'); break;
                case '\r': sb.append('\\').append('r'); break;
                case '\t': sb.append('\\').append('t'); break;
                case '\f': sb.append('\\').append('f'); break;
                case '\b': sb.append('\\').append('b'); break;
                case '"':  sb.append('\\').append('"'); break;
                case '\\': sb.append('\\').append('\\'); break;
                default:
                    sb.append(c < 32 || c == 127 ? toEscapedUnicode(c) : c);
                    break;
            }
        }

        return sb.toString();
    }

    public static int indexOneCharOf(final String text, final String searchChars, final int startPos) {
        if (text == null) {
            throw new IllegalArgumentException("A text must not be null");
        }
        if (isEmpty(searchChars)) {
            throw new IllegalArgumentException("A searchChars must not be empty");
        }
        if (startPos < 0) {
            throw new IllegalArgumentException("A startPos must not be negativ");
        }


        if (startPos >= text.length()) {
            return -1;
        }

        final Set<Character> chars = searchChars.chars().mapToObj(c -> (char)c).collect(Collectors.toSet());

        int pos = startPos;
        while(pos < text.length()) {
            if (chars.contains(text.charAt(pos))) {
                return pos;
            }
            else {
                pos++;
            }
        }

        return -1;
    }

    public static int indexNotOf(final String text, final String searchChars, final int startPos) {
        if (text == null) {
            throw new IllegalArgumentException("A text must not be null");
        }
        if (isEmpty(searchChars)) {
            throw new IllegalArgumentException("A searchChars must not be empty");
        }
        if (startPos < 0) {
            throw new IllegalArgumentException("A startPos must not be negativ");
        }


        if (startPos >= text.length()) {
            return -1;
        }

        final Set<Character> chars = searchChars.chars().mapToObj(c -> (char)c).collect(Collectors.toSet());

        int pos = startPos;
        while(pos < text.length()) {
            if (chars.contains(text.charAt(pos))) {
                pos++;
            }
            else {
                return pos;
            }
        }

        return -1;
    }

    public static String indent(final String text, final int indent) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        final String indentStr = repeat(' ', Math.min(200, Math.max(0, indent)));

        return StringUtil
                    .splitIntoLines(text)
                    .stream()
                    .map(s -> indentStr + s)
                    .collect(Collectors.joining("\n"));
    }

    public static String stripIndent(final String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        List<String> lines = StringUtil.splitIntoLines(text);

        // get the indent from the first line
        final String indent = indentStr(lines.get(0)) ;

        if (indent != null) {
            lines = stripIndent(indent, lines);
        }

        return String.join("\n", lines);
    }

    public static String stripIndentIfFirstLineEmpty(final String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        List<String> lines = StringUtil.splitIntoLines(text);
        if (lines.size() == 1 || !lines.get(0).isEmpty()) {
            // just a single line or does not start with an empty line
            return String.join("\n", lines);
        }

        // skip the first empty line
        lines = lines.subList(1, lines.size());

        final String indent = indentStr(lines.get(0)) ;

        if (indent != null) {
            lines = stripIndent(indent, lines);

            // remove an optional last empty line
            if (lines.get(lines.size()-1).isEmpty()) {
                lines = lines.subList(0, lines.size()-1);
            }
        }

        lines = joinLinesEndingWithBackslash(lines);

        return String.join("\n", lines);
    }

    public static String stripMargin(final String text, final char margin) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return StringUtil
                    .splitIntoLines(text)
                    .stream()
                    .map(s -> { int pos = s.indexOf(margin); return pos < 0 ? s : s.substring(pos+1); })
                    .collect(Collectors.joining("\n"));
    }

    /**
     * Truncates a string.
     *
     * @param text a string
     * @param maxLen the max length of the truncated string (truncation marker included)
     * @param truncationMarker a truncation marker
     * @return the truncated string
     */
    public static String truncate(
            final String text,
            final int maxLen,
            final String truncationMarker
    ) {
        if (truncationMarker == null) {
            throw new IllegalArgumentException("A truncationMarker must not be null");
        }

        int lenTruncationMarker = truncationMarker.length();

        if (maxLen <= lenTruncationMarker){
            throw new IllegalArgumentException(
                    "A maxLen must greater than the length of the truncation marker");
        }

        if (text == null || text.length() <= maxLen) {
            return text;
        }

        return text.substring(0, maxLen - lenTruncationMarker) + truncationMarker;
    }

    public static String repeat(final String s, final int times) {
        if (s == null) {
            throw new IllegalArgumentException("s must not be null");
        }
        if (times < 0) {
            throw new IllegalArgumentException("A times must not be negative");
        }

        if (times == 0) {
            return "";
        }
        else {
            final StringBuilder sb = new StringBuilder();
            for(int ii=0; ii<times; ii++) sb.append(s);
            return sb.toString();
        }
    }

    public static String repeat(final char c, final int times) {
        if (times < 0) {
            throw new IllegalArgumentException("A times must not be negative");
        }

        return times == 0 ? "" : new String(new char[times]).replace('\0', c);
    }

    public static boolean isEmpty(final String s){
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(final String s){
        return !isEmpty(s);
    }

    public static boolean isBlank(final String s){
        return s == null || s.length() == 0 || s.trim().length() == 0;
    }

    public static boolean isNotBlank(final String s){
        return !isBlank(s);
    }

    public static boolean isAsciiAlphaUpper(final char ch){
        return ch >= 'A' && ch <= 'Z';
    }

    public static String removeStart(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        else if (str.startsWith(remove)) {
            return str.substring(remove.length());
        }
        else {
            return str;
        }
    }

    public static String removeEnd(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        else if (str.endsWith(remove)) {
            return str.substring(0, str.length()-remove.length());
        }
        else {
            return str;
        }
    }

    public static String emptyToNull(final String s) {
        return isEmpty(s) ? null : s;
    }

    public static String nullToEmpty(final String s) {
        return s == null ? "" : s;
    }

    public static String trimToEmpty(final String str) {
        return str == null ? "" : str.trim();
    }

    public static String trimToNull(final String str) {
        final String s = str == null ? null : str.trim();
        return isEmpty(s) ? null : s;
    }

    public static String trimLeft(final String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        int pos = 0;

        while (pos < str.length() && str.charAt(pos) <= ' ') {
            pos++;
        }
        return str.substring(pos);
    }

    public static String trimRight(final String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        int end = str.length();

        while (end != 0 && str.charAt(end - 1) <= ' ') {
            end--;
        }
        return str.substring(0, end);
    }

    public static String decodeUnicode(final String s) {
        String working = s;
        int index;
        index = working.indexOf("\\u");
        while(index > -1) {
            int length = working.length();
            if(index > (length-6)) break;
            int numStart = index + 2;
            int numFinish = numStart + 4;
            String substring = working.substring(numStart, numFinish);
            int number = Integer.parseInt(substring,16);
            String stringStart = working.substring(0, index);
            String stringEnd   = working.substring(numFinish);
            working = stringStart + ((char)number) + stringEnd;
            index = working.indexOf("\\u");
        }
        return working;
    }

    public static String quote(final String str, final char quote) {
        return new StringBuilder().append(quote).append(str).append(quote).toString();
    }

    public static String replaceLeadingSpaces(final String text, final char replaceChar) {
        if (text == null) {
            return null;
        }

        final int count = StringUtil.indexNotOf(text, " ", 0);

        if (count > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append(repeat(replaceChar, count));
            sb.append(text.substring(count));
            return sb.toString();
        }
        else {
            return text;
        }
    }

    public static String padLeft(final String s, final int len) {
        final int padLen = len - s.length();
        return padLen > 0 ? repeat(' ', padLen) + s : s;
    }

    public static String padRight(final String s, final int len) {
        final int padLen = len - s.length();
        return padLen > 0 ? s + repeat(' ', padLen) : s;
    }

    public static String padCenter(final String s, final int len) {
        final int padLen = len - s.length();
        final int padRight = padLen / 2;
        final int padLeft = padLen - padRight;
        return padLen > 0 ? repeat(' ', padLeft) + s  + repeat(' ', padRight) : s;
    }

    public static String toEscapedUnicode(final char ch) {
        return String.format("\\u%04x", (int)ch);
    }

    private static List<String> stripIndent(final String indent, final List<String> lines) {
        final int skipChars = indent.length();
        return lines
                .stream()
                .map(s -> s.startsWith(indent) ? s.substring(skipChars) : s)
                .collect(Collectors.toList());
    }

    private static String indentStr(final String line) {
        final int firstBlankPos = StringUtil.indexOneCharOf(line, " \t", 0);

        if (firstBlankPos < 0) {
            return null;
        }
        else {
            final int firstNonBlankPos = StringUtil.indexNotOf(line, " \t", 0);
            return firstNonBlankPos < 0 ? line : line.substring(0, firstNonBlankPos);
        }
    }

    private static List<String> joinLinesEndingWithBackslash(final List<String> lines) {
        final List<String> joined = new ArrayList<>();

        // join lines if the line ends with a '\'
        String joinLine = null;
        for(int ii=0;  ii<lines.size(); ii++) {
            String line = lines.get(ii);
            if (line.endsWith("\\")) {
                line = line.substring(0, line.length()-1);
                joinLine = joinLine == null ? line : joinLine + line;
            }
            else {
                joined.add(joinLine == null ? line : joinLine + line);
                joinLine = null;
            }
        }

        if (joinLine != null) {
            joined.add(joinLine);
        }

        return joined;
    }
}
