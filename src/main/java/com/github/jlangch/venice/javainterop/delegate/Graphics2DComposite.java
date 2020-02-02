/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.javainterop.delegate;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;


/**
 * Starting with Java 9 reflection on JDK internal classes result in warnings.
 * 
 * WARNING: An illegal reflective access operation has occurred
 * WARNING: All illegal access operations will be denied in a future release
 *
 * BufferedImage::createGraphics() returns and object of Graphics2D (abstract class)
 * that is actually a sun.java2d.SunGraphics2D. Reflection calls on the internal
 * class SunGraphics2D is not possible anymore.
 * 
 * Wrapping the Graphics2D in Graphics2DComposite as a delegate and doing the 
 * reflection calls on Graphics2DComposite solves the problem. It's an ugly solution.
 */
public class Graphics2DComposite {

	public Graphics2DComposite(final Graphics2D g2d) {
		this.g2d = g2d;
	}

	public int hashCode() {
		return g2d.hashCode();
	}

	public boolean equals(Object obj) {
		return g2d.equals(obj);
	}

	public Graphics create() {
		return g2d.create();
	}

	public Graphics create(int x, int y, int width, int height) {
		return g2d.create(x, y, width, height);
	}

	public Color getColor() {
		return g2d.getColor();
	}

	public void setColor(Color c) {
		g2d.setColor(c);
	}

	public void setPaintMode() {
		g2d.setPaintMode();
	}

	public void setXORMode(Color c1) {
		g2d.setXORMode(c1);
	}

	public Font getFont() {
		return g2d.getFont();
	}

	public void setFont(Font font) {
		g2d.setFont(font);
	}

	public FontMetrics getFontMetrics() {
		return g2d.getFontMetrics();
	}

	public FontMetrics getFontMetrics(Font f) {
		return g2d.getFontMetrics(f);
	}

	public Rectangle getClipBounds() {
		return g2d.getClipBounds();
	}

	public void clipRect(int x, int y, int width, int height) {
		g2d.clipRect(x, y, width, height);
	}

	public void setClip(int x, int y, int width, int height) {
		g2d.setClip(x, y, width, height);
	}

	public Shape getClip() {
		return g2d.getClip();
	}

	public void setClip(Shape clip) {
		g2d.setClip(clip);
	}

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		g2d.copyArea(x, y, width, height, dx, dy);
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		g2d.drawLine(x1, y1, x2, y2);
	}

	public void fillRect(int x, int y, int width, int height) {
		g2d.fillRect(x, y, width, height);
	}

	public void drawRect(int x, int y, int width, int height) {
		g2d.drawRect(x, y, width, height);
	}

	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		g2d.draw3DRect(x, y, width, height, raised);
	}

	public void clearRect(int x, int y, int width, int height) {
		g2d.clearRect(x, y, width, height);
	}

	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		g2d.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		g2d.fill3DRect(x, y, width, height, raised);
	}

	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		g2d.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void draw(Shape s) {
		g2d.draw(s);
	}

	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		return g2d.drawImage(img, xform, obs);
	}

	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		g2d.drawImage(img, op, x, y);
	}

	public void drawOval(int x, int y, int width, int height) {
		g2d.drawOval(x, y, width, height);
	}

	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		g2d.drawRenderedImage(img, xform);
	}

	public void fillOval(int x, int y, int width, int height) {
		g2d.fillOval(x, y, width, height);
	}

	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		g2d.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		g2d.drawRenderableImage(img, xform);
	}

	public void drawString(String str, int x, int y) {
		g2d.drawString(str, x, y);
	}

	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		g2d.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	public void drawString(String str, float x, float y) {
		g2d.drawString(str, x, y);
	}

	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		g2d.drawPolyline(xPoints, yPoints, nPoints);
	}

	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		g2d.drawString(iterator, x, y);
	}

	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		g2d.drawPolygon(xPoints, yPoints, nPoints);
	}

	public void drawString(AttributedCharacterIterator iterator, float x, float y) {
		g2d.drawString(iterator, x, y);
	}

	public void drawPolygon(Polygon p) {
		g2d.drawPolygon(p);
	}

	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		g2d.fillPolygon(xPoints, yPoints, nPoints);
	}

	public void drawGlyphVector(GlyphVector g, float x, float y) {
		g2d.drawGlyphVector(g, x, y);
	}

	public void fillPolygon(Polygon p) {
		g2d.fillPolygon(p);
	}

	public void fill(Shape s) {
		g2d.fill(s);
	}

	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return g2d.hit(rect, s, onStroke);
	}

	public void drawChars(char[] data, int offset, int length, int x, int y) {
		g2d.drawChars(data, offset, length, x, y);
	}

	public GraphicsConfiguration getDeviceConfiguration() {
		return g2d.getDeviceConfiguration();
	}

	public void setComposite(Composite comp) {
		g2d.setComposite(comp);
	}

	public void drawBytes(byte[] data, int offset, int length, int x, int y) {
		g2d.drawBytes(data, offset, length, x, y);
	}

	public void setPaint(Paint paint) {
		g2d.setPaint(paint);
	}

	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return g2d.drawImage(img, x, y, observer);
	}

	public void setStroke(Stroke s) {
		g2d.setStroke(s);
	}

	public void setRenderingHint(Key hintKey, Object hintValue) {
		g2d.setRenderingHint(hintKey, hintValue);
	}

	public Object getRenderingHint(Key hintKey) {
		return g2d.getRenderingHint(hintKey);
	}

	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
		return g2d.drawImage(img, x, y, width, height, observer);
	}

	public void setRenderingHints(Map<?, ?> hints) {
		g2d.setRenderingHints(hints);
	}

	public void addRenderingHints(Map<?, ?> hints) {
		g2d.addRenderingHints(hints);
	}

	public RenderingHints getRenderingHints() {
		return g2d.getRenderingHints();
	}

	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
		return g2d.drawImage(img, x, y, bgcolor, observer);
	}

	public void translate(int x, int y) {
		g2d.translate(x, y);
	}

	public void translate(double tx, double ty) {
		g2d.translate(tx, ty);
	}

	public void rotate(double theta) {
		g2d.rotate(theta);
	}

	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
		return g2d.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	public void rotate(double theta, double x, double y) {
		g2d.rotate(theta, x, y);
	}

	public void scale(double sx, double sy) {
		g2d.scale(sx, sy);
	}

	public void shear(double shx, double shy) {
		g2d.shear(shx, shy);
	}

	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
			ImageObserver observer) {
		return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
	}

	public void transform(AffineTransform Tx) {
		g2d.transform(Tx);
	}

	public void setTransform(AffineTransform Tx) {
		g2d.setTransform(Tx);
	}

	public AffineTransform getTransform() {
		return g2d.getTransform();
	}

	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
			Color bgcolor, ImageObserver observer) {
		return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
	}

	public Paint getPaint() {
		return g2d.getPaint();
	}

	public Composite getComposite() {
		return g2d.getComposite();
	}

	public void setBackground(Color color) {
		g2d.setBackground(color);
	}

	public Color getBackground() {
		return g2d.getBackground();
	}

	public Stroke getStroke() {
		return g2d.getStroke();
	}

	public void clip(Shape s) {
		g2d.clip(s);
	}

	public FontRenderContext getFontRenderContext() {
		return g2d.getFontRenderContext();
	}

	public void dispose() {
		g2d.dispose();
	}

	public void finalize() {
		g2d.dispose();
	}

	public String toString() {
		return g2d.toString();
	}

	public Rectangle getClipRect() {
		return g2d.getClipBounds();
	}

	public boolean hitClip(int x, int y, int width, int height) {
		return g2d.hitClip(x, y, width, height);
	}

	public Rectangle getClipBounds(Rectangle r) {
		return g2d.getClipBounds(r);
	}
	
	private final Graphics2D g2d;
}
