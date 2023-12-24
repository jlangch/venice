/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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


/**
 * Creates arbitrary length <i>Lorem Ipsum</i> based texts.
 */
public class LoremIpsum {

    private LoremIpsum() {
    }

    /**
     * Creates a <i>Lorem Ipsum</i> text with the given text length.
     *
     * @param length
     *          The length of the text to be created. Returns an empty
     *          string if the <code>length</code> is lower than 1
     *
     * @return The created <i>Lorem Ipsum</i> text
     */
    public static String loremIpsum_Chars(final int length) {
        int len = clip(length);

        if (len <= 0) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        int pg = 0;

        while(len > 0) {
            final String paragraph = LOREM_IPSUM[pg];
            final int l = Math.min(len, paragraph.length());
            sb.append(paragraph.substring(0, l));
            len -= l;
            pg = (pg + 1) % LOREM_IPSUM.length;
            if (len > 0) {
                sb.append("\n");
                len -= 1;
            }
        }

        return sb.toString();
    }


    /**
     * Creates a <i>Lorem Ipsum</i> text with the given number of paragraphs.
     *
     * @param paragraphs
     *          The number of paragraphs (limited to 100). Returns an empty
     *          string if the <code>paragraphs</code> is lower than 1
     *
     * @return The created <i>Lorem Ipsum</i> text
     */
    public static String loremIpsum_Paragraphs(final int paragraphs) {
        int len = Math.min(MAX_PARAGRAPHS, paragraphs);

        if (len <= 0) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        for(int ii=0; ii<len; ii++) {
            sb.append(LOREM_IPSUM[ii % LOREM_IPSUM.length]);

            if (ii < (len-1)) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public static int getMaxChars() {
        return MAX_LENGTH;
    }

    public static int getMaxParagraphs() {
        return MAX_PARAGRAPHS;
    }

    private static int clip(final int length) {
        return Math.max(MIN_LENGTH, Math.min(MAX_LENGTH, length));
    }


    private static final int MAX_PARAGRAPHS = 100;

    private static final int MAX_LENGTH = 1_000_000;
    private static final int MIN_LENGTH = 0;

    private static final String[] LOREM_IPSUM = {

        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent ac iaculis "
        + "turpis. Duis dictum id sem et consectetur. Nullam lobortis, libero non "
        + "consequat aliquet, lectus diam fringilla velit, finibus eleifend ipsum urna "
        + "at lacus. Phasellus sit amet nisl fringilla, cursus est in, mollis lacus. "
        + "Proin dignissim rhoncus dolor. Cras tellus odio, elementum sed erat sit "
        + "amet, euismod tincidunt nisl. In hac habitasse platea dictumst. Duis aliquam "
        + "sollicitudin tempor. Sed gravida tincidunt felis at fringilla. Morbi tempor "
        + "enim at commodo vulputate. Aenean et ultrices lorem, placerat pretium augue. "
        + "In hac habitasse platea dictumst. Cras fringilla ligula quis interdum "
        + "hendrerit. Etiam at massa tempor, facilisis lacus placerat, congue erat.",

        "Donec feugiat sem at eros mollis, a tristique mi aliquet. Pellentesque "
        + "scelerisque, erat vel suscipit accumsan, felis erat tincidunt ex, volutpat "
        + "viverra diam justo vitae erat. Nullam id metus dui. Ut non maximus magna. "
        + "Ut arcu enim, convallis eu diam quis, dictum commodo ex. Sed feugiat dui at "
        + "erat luctus, in scelerisque leo vestibulum. Praesent non ornare orci, ac "
        + "bibendum metus. Maecenas urna augue, molestie non purus quis, viverra "
        + "facilisis urna. Aenean sit amet tincidunt ante, non congue lectus. Nullam "
        + "pretium nibh id nibh congue, ac auctor metus molestie. Fusce lobortis, nisl "
        + "vel dignissim porta, dui nisl tempor quam, non congue felis libero eu diam.",

        "Vestibulum sodales purus ut porttitor ullamcorper. Sed lobortis egestas lorem "
        + "in feugiat. In suscipit, ligula nec fringilla venenatis, sapien purus sagittis "
        + "ipsum, eu consectetur sapien odio vel odio. Aenean posuere lectus et mi faucibus "
        + "vehicula. Donec volutpat est vitae volutpat tincidunt. Fusce id pellentesque "
        + "massa. Praesent sit amet sodales nulla, vitae condimentum leo.",

        "Sed urna lectus, egestas eget mauris nec, gravida fringilla ante. Aliquam "
        + "euismod sapien erat, a tristique quam facilisis nec. Cras in nisl nisl. "
        + "Suspendisse ornare bibendum dolor at sodales. Donec dapibus lacus orci,"
        + " id accumsan ligula convallis eu. Nulla tempor at est sed rhoncus. "
        + "Vestibulum viverra dolor id eros molestie, sit amet viverra sapien "
        + "venenatis. Aliquam erat volutpat. Curabitur vestibulum orci vitae est "
        + "consequat pharetra.",

        "Sed viverra urna id erat semper, eget pellentesque tortor finibus. Cras "
        + "elit ex, elementum ac porta quis, posuere consequat neque. Integer fringilla "
        + "et est et pulvinar. Proin condimentum orci vitae sapien dictum, vitae "
        + "feugiat ante auctor. Nam eros justo, fermentum pretium erat porta, varius "
        + "venenatis risus. Nulla varius facilisis lacus quis blandit. Aliquam sed "
        + "accumsan lacus. Aenean laoreet, nibh a elementum egestas, massa enim "
        + "condimentum sem, a sagittis libero nisi at odio. Nullam ac justo accumsan, "
        + "accumsan ipsum id, iaculis odio. Etiam vehicula ex at neque vulputate luctus "
        + "quis sed est. Donec eleifend aliquam felis, eu vehicula mauris mattis non. "
        + "Vestibulum viverra urna felis, id rhoncus quam tristique quis. Curabitur "
        + "elementum consectetur venenatis. Maecenas vel tellus urna. Vestibulum "
        + "dapibus gravida lorem, eu pharetra mauris bibendum eget.",

        "Maecenas interdum justo sit amet blandit fermentum. Vivamus vel metus "
        + "volutpat, tempor mauris id, ultrices neque. Vivamus bibendum lectus ac "
        + "imperdiet molestie. Ut iaculis vel lacus non fermentum. Donec vestibulum "
        + "luctus ultricies. Nulla facilisi. Curabitur sed consequat ligula. Ut a "
        + "augue et tellus maximus bibendum et dignissim erat. Aenean non velit "
        + "ligula. Mauris tempor imperdiet enim fringilla consectetur. Quisque felis "
        + "turpis, scelerisque in elit sed, accumsan porta nibh.",

        "Interdum et malesuada fames ac ante ipsum primis in faucibus. Nunc non "
        + "hendrerit eros. Nullam at fermentum leo. Pellentesque lacus leo, elementum "
        + "sit amet finibus eget, dapibus sit amet libero. Nulla facilisi. Fusce ac "
        + "risus vulputate, volutpat dolor nec, viverra augue. Morbi dolor lacus, "
        + "facilisis vel nulla rutrum, ultrices dignissim mi. Nunc et tempus sapien, "
        + "nec tincidunt sapien. Aenean posuere finibus luctus. In hendrerit arcu ut "
        + "risus consectetur, lobortis vestibulum elit mattis. Mauris facilisis est "
        + "ut lorem scelerisque, at accumsan risus ullamcorper. Vestibulum "
        + "condimentum, libero at semper blandit, ligula ante euismod sem, ac "
        + "dignissim nulla odio at sapien. Aliquam elementum augue a diam accumsan "
        + "condimentum.",

        "Praesent consequat porta lobortis. Aliquam aliquet eleifend ultricies. "
        + "Vestibulum et tincidunt turpis, sed posuere eros. Aenean at purus blandit, "
        + "maximus metus ac, suscipit lacus. Suspendisse quis rhoncus dolor, ac"
        + " malesuada velit. Nullam rhoncus augue sed nibh sodales, sit amet eleifend "
        + "risus egestas. Proin sed mauris ut dolor lacinia auctor. Mauris fringilla "
        + "euismod dolor, dictum elementum augue commodo quis. Etiam ornare dui nulla, "
        + "nec ultricies turpis ultrices ac. Sed pretium, purus a tincidunt euismod, "
        + "lectus tortor egestas massa, id pulvinar lacus felis sed lorem. Pellentesque "
        + "ac tellus a augue euismod volutpat semper vitae justo. Integer tempus sapien "
        + "a congue aliquam.",

        "Sed rutrum iaculis sollicitudin. Nulla sagittis mauris vel leo condimentum, et "
        + "aliquet velit volutpat. Nullam sit amet libero nibh. Etiam id elit aliquam, "
        + "fermentum nibh id, rhoncus ex. Mauris vitae ex lacus. Maecenas sed velit eget "
        + "dolor tristique pulvinar nec vitae elit. Nunc dolor ex, volutpat vel felis eget, "
        + "convallis mollis enim. Donec pharetra, nisl nec sollicitudin malesuada, felis "
        + "nisi dapibus nisl, in consequat est nibh vitae nisi. In dignissim ultrices velit "
        + "quis imperdiet. Sed mattis eleifend est ac semper. Nam vestibulum pharetra sem, "
        + "non mollis nulla imperdiet in. Fusce a sem vitae mauris dictum sagittis non id "
        + "leo. Aenean lacinia nulla vel lorem venenatis accumsan. Donec consequat nec "
        + "ligula quis gravida. Cras sed est egestas ligula viverra bibendum. Pellentesque "
        + "aliquam neque et mi dapibus, at faucibus enim euismod.",

        "Nunc commodo elit at metus ullamcorper, vitae tempor mauris mattis. Phasellus "
        + "congue in mi at fringilla. Maecenas tristique luctus magna nec pellentesque. "
        + "Vivamus imperdiet eros enim, ut cursus eros pretium eu. Praesent mollis luctus "
        + "massa in mollis. Ut id nibh laoreet, facilisis nisi quis, dapibus elit. Fusce "
        + "vitae condimentum augue. Proin aliquet pharetra mattis. Nam volutpat volutpat "
        + "nibh egestas elementum. Class aptent taciti sociosqu ad litora torquent per "
        + "conubia nostra, per inceptos himenaeos. Sed eget dolor non orci consequat "
        + "ultrices nec non ipsum. Aliquam massa arcu, malesuada vel elementum nec, "
        + "sagittis nec enim. Fusce ut euismod quam. Duis efficitur, metus ut vulputate "
        + "elementum, purus ligula blandit eros, eu mattis orci arcu vel lorem."
    };
}
