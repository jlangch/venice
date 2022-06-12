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
package com.github.jlangch.venice.impl.util.xchart;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;


/**
 * Export a XChart in-memory to a PNG, JPG, ... and returns it as binary.
 *
 * <p>The original XChart BitmapDecoder does not support exporting a chart to
 * a binary when using customized DPI values (e.g. for high-res charts)
 */
public final class XChartEncoder {

    private XChartEncoder() {}

    public static byte[] exportToBitmapWithDPI(
            final Object xchart,
            final String bitmapFormat, // png, jpg, gif
            final int DPI
    ) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        exportToBitmapStreamWithDPI(xchart, os, bitmapFormat, DPI);
        os.flush();

        return os.toByteArray();
    }

    public static void exportToBitmapStreamWithDPI(
            final Object xchart,
            final OutputStream os,
            final String bitmapFormat, // png, jpg, gif
            final int DPI
    ) throws IOException {
        final double scaleFactor = DPI / 72.0;

        final int chartWidth = (Integer)ReflectionAccessor.getBeanProperty(xchart, "width").getValue();
        final int chartHeight = (Integer)ReflectionAccessor.getBeanProperty(xchart, "height").getValue();

        final BufferedImage image = new BufferedImage(
                                        (int)(chartWidth * scaleFactor),
                                        (int)(chartHeight * scaleFactor),
                                        BufferedImage.TYPE_INT_RGB);

        final Graphics2D graphics2D = image.createGraphics();

        final AffineTransform at = graphics2D.getTransform();
        at.scale(scaleFactor, scaleFactor);
        graphics2D.setTransform(at);

        ReflectionAccessor.invokeInstanceMethod(
                xchart,
                "paint",
                new Object[] { graphics2D, chartWidth, chartHeight });

        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(bitmapFormat.toLowerCase());

        if (writers.hasNext()) {
            final ImageWriter writer = writers.next();

            // instantiate an ImageWriteParam object with default compression options
            final ImageWriteParam iwp = writer.getDefaultWriteParam();

            final ImageTypeSpecifier typeSpecifier =
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

            final IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, iwp);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                throw new IllegalArgumentException(
                        "It is not possible to set the DPI on a bitmap with "
                                + bitmapFormat
                                + " format!! Try another format.");
            }

            setDPI(metadata, DPI);

            writer.setOutput(new MemoryCacheImageOutputStream(os));
            writer.write(null, new IIOImage(image, null, metadata), iwp);
            writer.dispose();
            os.flush();
        }
    }

    private static void setDPI(
            final IIOMetadata metadata,
            final int DPI
    ) throws IIOInvalidTreeException {
        // for PNG, it's dots per millimeter
        final double dotsPerMilli = 1.0 * DPI / 10 / 2.54;

        final IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        final IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        final IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        final IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }
}
