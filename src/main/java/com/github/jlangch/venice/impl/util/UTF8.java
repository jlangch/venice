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


public abstract class UTF8 {

    // See:  https://www.compart.com/de/unicode/

    // White Spaces
    //
    //   The characters U+2000…U+2006, when implemented in a font, usually have the
    //   specific width defined for them, though small deviations exist. Their widths
    //   are defined in terms of the em unit, i.e. the size of the font.
    //
    //   The characters U+2007…U+200A and U+202F have no exact width assigned to them
    //   in the standard, and implementations may deviate considerably even from the
    //   suggested widths. Moreover, when concepts with the same names, such as
    //   “thin space”, are used in publishing software, the meanings can be rather
    //   different. For example, in InDesign, “thin space” is now 1/8 em (i.e.
    //   0.125 em, as opposite to the suggested 0.2 em) and “hair space” only 1/24 em
    //   (i.e. about 0.042 em, whereas the width of a THIN SPACE glyph typically
    //   varies between 0.1 em and 0.2 em).
    public static char NBSP                  = '\u00A0';  // no break space
    public static char EN_QUAD               = '\u2000';  //
    public static char EM_QUAD               = '\u2001';  //
    public static char EN_SPACE              = '\u2002';  // en space
    public static char EM_SPACE              = '\u2003';  // em space, 1 en (= 1/2 em)
    public static char THREE_PER_EM_SPACE    = '\u2004';  // three-per-em space
    public static char FOUR_PER_EM_SPACE     = '\u2005';  // four-per-em space
    public static char SIX_PER_EM_SPACE      = '\u2006';  // six-per-em space
    public static char FIGURE_SPACE          = '\u2007';  // figure space
    public static char PUNCTUATION_SPACE     = '\u2008';  // punctuation space
    public static char THIN_SPACE            = '\u2009';  // thin space
    public static char HAIR_SPACE            = '\u200A';  // hair space
    public static char ZERO_WIDTH_SPACE      = '\u200B';  // zero width space
    public static char ZERO_WIDTH_NON_JOINER = '\u200C';  // zero width non-joiner
    public static char ZERO_WIDTH_JOINER     = '\u200D';  // zero width joiner
    public static char LINE_SEPARATOR        = '\u2028';  // line separator
    public static char PARAGRAPH_SEPARATOR   = '\u2029';  // paragraph separator
    public static char NARROW_NO_BREAK_SPACE = '\u202F';  // narrow no-break space
    public static char MEDIUM_MATH_SPACE     = '\u205F';  // medium mathematical space
    public static char WORD_JOINER           = '\u2060';  // word joiner


    // Varia
    public static char PILCROW               = '\u00B6';  // check mark '¶'
    public static char MIDDLE_DOT            = '\u00B7';  // check mark '·'
    public static char RIGHT_GUILLEMET       = '\u00BB';  // check mark '»'
    public static char LEFT_GUILLEMET        = '\u00AB';  // check mark '«'
    public static char COPYRIGHT             = '\u00A9';  // check mark '©'
    public static char BULLET                = '\u2022';  // bullet '•'
    public static char HORZ_ELLIPSIS         = '\u2026';  // horizontal ellipsis '…'
    public static char PER_MILLE_SIGN        = '\u2030';  // per mille sign '‰'
    public static char DIAMETER_SIGN         = '\u2300';  // diameter sign '⌀'
    public static char CHECK_MARK            = '\u2713';  // check mark '✓'
    public static char CROSS_MARK            = '\u2717';  // cross mark '✗'

    // Greek letters
    public static char ALPHA                 = '\u03B1';  // alpha 'α'
    public static char BETA                  = '\u03B2';  // beta 'β'
    public static char GAMMA                 = '\u03B3';  // gamma 'γ'
    public static char DELTA                 = '\u03B4';  // delta 'δ'
    public static char EPSILON               = '\u03B5';  // epsilon 'ε'
    public static char LAMDA                 = '\u03BB';  // lamda 'λ'
    public static char MU                    = '\u03BC';  // mu 'μ'
    public static char PI                    = '\u03C0';  // pi 'π'
    public static char SIGMA                 = '\u03C3';  // sigma 'σ'
    public static char TAU                   = '\u03C4';  // tau 'τ'

    public static char DELTA_UPPER           = '\u0394';  // uppercase greek delta 'Δ'


    public static String TABLE               = "┌───┬───┐"
                                             + "│   │   │"
                                             + "├───┼───┤"
                                             + "│   │   │"
                                             + "└───┴───┘";
}
