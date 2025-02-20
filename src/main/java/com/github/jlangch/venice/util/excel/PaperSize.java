/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.util.excel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.PrintSetup;


public enum PaperSize {

    /** Whatever the printer's default paper size is */
    PRINTER_DEFAULT(PrintSetup.PRINTER_DEFAULT_PAPERSIZE),

    /** US Letter 8 1/2 x 11 in */
    LETTER(PrintSetup.LETTER_PAPERSIZE),

    /** US Letter Small 8 1/2 x 11 in */
    LETTER_SMALL_PAGESIZE(PrintSetup.LETTER_SMALL_PAGESIZE),

    /** US Tabloid 11 x 17 in */
    TABLOID(PrintSetup.TABLOID_PAPERSIZE),

    /** US Ledger 17 x 11 in */
    LEDGER(PrintSetup.LEDGER_PAPERSIZE),

    /** US Legal 8 1/2 x 14 in */
    LEGAL(PrintSetup.LEGAL_PAPERSIZE),

    /** US Statement 5 1/2 x 8 1/2 in */
    STATEMENT(PrintSetup.STATEMENT_PAPERSIZE),

    /** US Executive 7 1/4 x 10 1/2 in */
    EXECUTIVE(PrintSetup.EXECUTIVE_PAPERSIZE),

    /** A3 - 297x420 mm */
    A3(PrintSetup.A3_PAPERSIZE),

    /** A4 - 210x297 mm */
    A4(PrintSetup.A4_PAPERSIZE),

    /** A4 Small - 210x297 mm */
    A4_SMALL(PrintSetup.A4_SMALL_PAPERSIZE),

    /** A5 - 148x210 mm */
    A5(PrintSetup.A5_PAPERSIZE),

    /** B4 (JIS) 250x354 mm */
    B4(PrintSetup.B4_PAPERSIZE),

    /** B5 (JIS) 182x257 mm */
    B5(PrintSetup.B5_PAPERSIZE),

    /** Folio 8 1/2 x 13 in */
    FOLIO8(PrintSetup.FOLIO8_PAPERSIZE),

    /** Quarto 215x275 mm */
    QUARTO(PrintSetup.QUARTO_PAPERSIZE),

    /** 10 x 14 in */
    TEN_BY_FOURTEEN(PrintSetup.TEN_BY_FOURTEEN_PAPERSIZE),

    /** 11 x 17 in */
    ELEVEN_BY_SEVENTEEN(PrintSetup.ELEVEN_BY_SEVENTEEN_PAPERSIZE),

    /** US Note 8 1/2 x 11 in */
    NOTE8(PrintSetup.NOTE8_PAPERSIZE),

    /** US Envelope #9 3 7/8 x 8 7/8 */
    ENVELOPE_9(PrintSetup.ENVELOPE_9_PAPERSIZE),

    /** US Envelope #10 4 1/8 x 9 1/2 */
    ENVELOPE_10(PrintSetup.ENVELOPE_10_PAPERSIZE),

    /** Envelope DL 110x220 mm */
    ENVELOPE_DL(PrintSetup.ENVELOPE_DL_PAPERSIZE),

    /** Envelope C5 162x229 mm */
    ENVELOPE_CS(PrintSetup.ENVELOPE_CS_PAPERSIZE),
    ENVELOPE_C5(PrintSetup.ENVELOPE_C5_PAPERSIZE),

    /** Envelope C3 324x458 mm */
    ENVELOPE_C3(PrintSetup.ENVELOPE_C3_PAPERSIZE),

    /** Envelope C4 229x324 mm */
    ENVELOPE_C4(PrintSetup.ENVELOPE_C4_PAPERSIZE),

    /** Envelope C6 114x162 mm */
    ENVELOPE_C6(PrintSetup.ENVELOPE_C6_PAPERSIZE),
    ENVELOPE_MONARCH(PrintSetup.ENVELOPE_MONARCH_PAPERSIZE),

    /** A4 Extra - 9.27 x 12.69 in */
    A4_EXTRA(PrintSetup.A4_EXTRA_PAPERSIZE),

    /** A4 Transverse - 210x297 mm */
    A4_TRANSVERSE(PrintSetup.A4_TRANSVERSE_PAPERSIZE),

    /** A4 Plus - 210x330 mm */
    A4_PLUS(PrintSetup.A4_PLUS_PAPERSIZE),

    /** US Letter Rotated 11 x 8 1/2 in */
    LETTER_ROTATED(PrintSetup.LETTER_ROTATED_PAPERSIZE),

    /** A4 Rotated - 297x210 mm */
    A4_ROTATED(PrintSetup.A4_ROTATED_PAPERSIZE);




    private static final Map<Short, PaperSize> PAPER_SIZE_BY_LEGACY_API_VALUE;

    static {
        PAPER_SIZE_BY_LEGACY_API_VALUE = new HashMap<>();

        for (PaperSize size : values()) {
            PAPER_SIZE_BY_LEGACY_API_VALUE.put(size.legacyApiValue, size);
        }
    }

    private final short legacyApiValue;



    PaperSize(short legacyApiValue) {
        this.legacyApiValue = legacyApiValue;
    }

    public short getLegacyApiValue() {
        return legacyApiValue;
    }

	public static PaperSize getByShortValue(short legacyApiValue) {
        return PAPER_SIZE_BY_LEGACY_API_VALUE.get(legacyApiValue);
    }

}
