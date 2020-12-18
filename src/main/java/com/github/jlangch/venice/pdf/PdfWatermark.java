/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;


/**
 * Decorates PDF with watermarks
 */
public class PdfWatermark {

	public PdfWatermark() {
	}
	
	public ByteBuffer addWatermarkImage(
			final ByteBuffer pdf, 
			final String imgResourceName,
			final int skipTopPages, 
			final int skipBottomPages
	) {
		if (pdf == null) {
			throw new IllegalArgumentException("A pdf must not be null");
		}
		
		try {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			final PdfReader reader = new PdfReader(pdf.array());
			final int numPages = reader.getNumberOfPages();
			final PdfStamper stamper = new PdfStamper(reader, os);
			final Image watermark_image = Image.getInstance(imgResourceName);
			watermark_image.setAbsolutePosition(200, 400);

			final int startPage = skipTopPages;
			final int endPage = numPages - skipBottomPages;

			for(int page=startPage; page<endPage; page++) {
				PdfContentByte under = stamper.getUnderContent(page);
				under.addImage(watermark_image);
			}
			
			stamper.close();
			
			return ByteBuffer.wrap(os.toByteArray());
		}
		catch(Exception ex) {
			throw new RuntimeException("Failed to add image watermark to the PDF", ex);
		}		
	}

	public ByteBuffer addWatermarkText(
			final ByteBuffer pdf, 
			final String text,
			final float fontSize,
			final float fontCharacterSpacing,
			final Color color,
			final float opacity,
			final Color colorOutline,
			final float colorOutlineOpacity,
			final float outlineWidth,
			final float angle,
			final boolean overContent,
			final int skipTopPages, 
			final int skipBottomPages
	) {
		if (pdf == null) {
			throw new IllegalArgumentException("A pdf must not be null");
		}
		
		try {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			final PdfReader reader = new PdfReader(pdf.array());
			final PdfStamper stamper = new PdfStamper(reader, os);
			final int numPages = reader.getNumberOfPages();
			final int startPage = 1 + skipTopPages;
			final int endPage = numPages - skipBottomPages;

			final BaseFont baseFont = BaseFont.createFont("Helvetica", BaseFont.WINANSI, false);

			final Color textColor = withOpacity(nullToBlack(color), opacity);
			final Color strokeColor = withOpacity(nullToBlack(colorOutline), colorOutlineOpacity);

			for(int page=startPage; page<=endPage; page++) {
				final PdfContentByte cb = overContent 
											? stamper.getOverContent(page) 
											: stamper.getUnderContent(page);
				
				cb.saveState();
				cb.setColorFill(textColor);
				cb.setColorStroke(strokeColor);
				cb.beginText();
				cb.setFontAndSize(baseFont, fontSize);
				cb.setCharacterSpacing(fontCharacterSpacing);			
				cb.setLineWidth(Math.max(0.0F, outlineWidth));
				cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE); 
				
				cb.showTextAligned(
						Element.ALIGN_CENTER, 
						text,
						cb.getPdfDocument().getPageSize().getWidth() / 2,
						cb.getPdfDocument().getPageSize().getHeight() / 2,
						angle);
				
				cb.endText();
				cb.restoreState();
			}
			
			stamper.close();
			
			return ByteBuffer.wrap(os.toByteArray());
		}
		catch(Exception ex) {
			throw new RuntimeException("Failed to add text watermark to the PDF", ex);
		}		
	}

	private Color nullToBlack(final Color color) {
		return color == null ? Color.BLACK : color;
	}

	private Color withOpacity(final Color color, final float opacity) {
		return new Color(
					color.getRed(), 
					color.getGreen(), 
					color.getBlue(),
					toIntOpacity(opacity));
	}
	
	private int toIntOpacity(final float opacity) {
		return Math.max(0, Math.min(255, (int)(opacity * 255)));
	}
}
