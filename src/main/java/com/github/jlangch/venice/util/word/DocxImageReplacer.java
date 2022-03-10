package com.github.jlangch.venice.util.word;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.CollectionUtil;


public class DocxImageReplacer {
	
	public static DocxImageReplacer of(final byte[] mainDoc) throws Docx4JException, IOException {
		if (mainDoc == null) {
			throw new IllegalArgumentException("A mainDoc must not be null!");
		}

		try(InputStream is = new ByteArrayInputStream(mainDoc)) {
			return new DocxImageReplacer(WordprocessingMLPackage.load(is));
		}
	}
	
	public static DocxImageReplacer of(final ByteBuffer mainDoc) throws Docx4JException, IOException {
		if (mainDoc == null) {
			throw new IllegalArgumentException("A mainDoc must not be null!");
		}

		return of(mainDoc.array());
	}

	public static DocxImageReplacer of(final InputStream mainDocIs) throws Docx4JException {
		if (mainDocIs == null) {
			throw new IllegalArgumentException("A mainDocIs must not be null!");
		}

		try {
			return new DocxImageReplacer(WordprocessingMLPackage.load(mainDocIs));
		}
		finally {
			try {
				mainDocIs.close();
			} 
			catch (Exception e) {}
		}
	}

	public static DocxImageReplacer of(final File mainDocFile) throws Docx4JException {
		if (mainDocFile == null) {
			throw new IllegalArgumentException("A mainDocFile must not be null!");
		}

		return new DocxImageReplacer(WordprocessingMLPackage.load(mainDocFile));
	}

	
	private DocxImageReplacer(final WordprocessingMLPackage mainDoc) {
		if (mainDoc == null) {
			throw new IllegalArgumentException("A mainDoc must not be null!");
		}

		this.mainDoc = mainDoc;
	}
	
	
	public WordprocessingMLPackage doc() {
		return mainDoc;
	}
	

	public byte[] saveToBytes() throws Docx4JException, IOException {
		try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			mainDoc.save(os);
			os.close();
			return os.toByteArray();
		}
	}

	public ByteBuffer saveToByteBuffer() throws Docx4JException, IOException {
		return ByteBuffer.wrap(saveToBytes());
	}

	public void saveTo(final File file) throws Docx4JException {
		if (file == null) {
			throw new IllegalArgumentException("A file must not be null!");
		}

		mainDoc.save(file);
	}
	
	public void saveTo(final OutputStream os) throws Docx4JException, IOException {
		if (os == null) {
			throw new IllegalArgumentException("An os must not be null!");
		}
		
		try {
			mainDoc.save(os);
		}
		finally {
			try {
				os.close();
			}
			catch (Exception e) {}
		}
	}

	public DocxImageReplacer replaceEmbeddedImageReferencedByImageName(
			final String imageName,
			final File img
	) throws Docx4JException, JAXBException, FileNotFoundException {
		if (imageName == null) {
			throw new IllegalArgumentException("An imageName must not be null!");
		}
		if (img == null) {
			throw new IllegalArgumentException("An img must not be null!");
		}

		final String embedId = findImageEmbedIdByImageName(imageName);
		if (embedId == null) {
			throw new VncException(String.format(
						"The Docx 'embedId' for the image name '%s' could not be found",
						imageName));
		}

		return replaceEmbeddedImage(embedId, new FileInputStream(img));	
	}
	
	public DocxImageReplacer replaceEmbeddedImageReferencedByImageName(
			final String imageName,
			final byte[] img
	) throws Docx4JException, JAXBException, IOException {
		if (imageName == null) {
			throw new IllegalArgumentException("An imageName must not be null!");
		}
		if (img == null) {
			throw new IllegalArgumentException("An img must not be null!");
		}

		final String embedId = findImageEmbedIdByImageName(imageName);
		if (embedId == null) {
			throw new VncException(String.format(
						"The Docx 'embedId' for the image name '%s' could not be found",
						imageName));
		}
				
		try(ByteArrayInputStream is = new ByteArrayInputStream(img)) {
			return replaceEmbeddedImage(embedId, is);
		}
	}
	
	public DocxImageReplacer replaceEmbeddedImageReferencedByImageName(
			final String imageName,
			final InputStream img
	) throws Docx4JException, JAXBException, FileNotFoundException {
		if (imageName == null) {
			throw new IllegalArgumentException("An imageName must not be null!");
		}
		if (img == null) {
			throw new IllegalArgumentException("An img must not be null!");
		}

		final String embedId = findImageEmbedIdByImageName(imageName);
		if (embedId == null) {
			throw new VncException(String.format(
						"The Docx 'embedId' for the image name '%s' could not be found",
						imageName));
		}

		return replaceEmbeddedImage(embedId, img);
	}

	public DocxImageReplacer replaceEmbeddedImageReferencedByImageDescr(
			final String imgDescr,
			final File img
	) throws Docx4JException, JAXBException, FileNotFoundException {
		if (imgDescr == null) {
			throw new IllegalArgumentException("An imgDescr must not be null!");
		}
		if (img == null) {
			throw new IllegalArgumentException("An img must not be null!");
		}

		final String embedId = findImageEmbedIdByImageDescr(imgDescr);
		if (embedId == null) {
			throw new VncException(String.format(
						"The Docx 'embedId' for the image descr '%s' could not be found",
						imgDescr));
		}
		
		return replaceEmbeddedImage(embedId, new FileInputStream(img));	
	}

	public DocxImageReplacer replaceEmbeddedImageReferencedByImageDescr(
			final String imgDescr,
			final byte[] img
	) throws Docx4JException, JAXBException, FileNotFoundException {
		if (imgDescr == null) {
			throw new IllegalArgumentException("An imgDescr must not be null!");
		}
		if (img == null) {
			throw new IllegalArgumentException("An img must not be null!");
		}

		final String embedId = findImageEmbedIdByImageDescr(imgDescr);
		if (embedId == null) {
			throw new VncException(String.format(
						"The Docx 'embedId' for the image descr '%s' could not be found",
						imgDescr));
		}

		return replaceEmbeddedImage(embedId, new ByteArrayInputStream(img));	
	}

	public DocxImageReplacer replaceEmbeddedImageReferencedByImageDescr(
			final String imgDescr,
			final InputStream imgIS
	) throws Docx4JException, JAXBException, FileNotFoundException {
		if (imgDescr == null) {
			throw new IllegalArgumentException("An imgDescr must not be null!");
		}
		if (imgIS == null) {
			throw new IllegalArgumentException("An imgIS must not be null!");
		}

		final String embedId = findImageEmbedIdByImageDescr(imgDescr);
		if (embedId == null) {
			throw new VncException(String.format(
						"The Docx 'embedId' for the image descr '%s' could not be found",
						imgDescr));
		}

		try {
			return replaceEmbeddedImage(embedId, imgIS);
		}
		finally {
			try {
				imgIS.close();
			} 
			catch (Exception e) {}
		}
	}

	private String findImageEmbedIdByImageName(
			final String imgName
	) throws Docx4JException, JAXBException {
		final List<Object> drawings = mainDoc.getMainDocumentPart()
											 .getJAXBNodesViaXPath(
												   String.format("//wp:docPr[@name=\"%s\"]", imgName),
												   false);
		return findImageEmbedId(drawings);
	}

	private String findImageEmbedIdByImageDescr(
			final String imgDescr
	) throws Docx4JException, JAXBException {
		final List<Object> drawings = mainDoc.getMainDocumentPart()
									  		 .getJAXBNodesViaXPath(
												   String.format("//wp:docPr[@descr=\"%s\"]", imgDescr),
												   false);
		
		return findImageEmbedId(drawings);
	}

	private DocxImageReplacer replaceEmbeddedImage(
			final String embedId,
			final InputStream imgIS
	) throws Docx4JException, JAXBException, FileNotFoundException {
		final Relationship rel = findRelationshipById(embedId);
		if (rel == null) {
			throw new VncException(String.format(
					"The Docx relationship for the embedId '%s' could not be found",
					embedId));
		}

		final String partName = String.format("/word/media/document_image_%s.png", embedId);

		final BinaryPartAbstractImage imagePart = new ImagePngPart(new PartName(partName));

		imagePart.setBinaryData(imgIS);
		imagePart.setContentType(new ContentType(ContentTypes.IMAGE_PNG));
		imagePart.setRelationshipType(Namespaces.IMAGE);

		final Relationship relNew = mainDoc.getMainDocumentPart().addTargetPart(imagePart);
		relNew.setId(embedId);

		return this;
	}

	private Relationship findRelationshipById(final String rID) {
		return mainDoc.getMainDocumentPart()
					  .getRelationshipsPart()
					  .getRelationshipByID(rID);
	}

	private String findImageEmbedId(final List<Object> drawings) {
		final Object drawing = CollectionUtil.first(drawings);
		if (drawing instanceof CTNonVisualDrawingProps) {
			final Object parent = ((CTNonVisualDrawingProps)drawing).getParent();
			if (parent instanceof Inline) {
				final Inline inline = (Inline)parent;
				
				final CTBlip blip = inline.getGraphic()
										  .getGraphicData()
										  .getPic()
										  .getBlipFill()
										  .getBlip();
				
				return blip.getEmbed();
			}
		}

		return null;
	}


	private final WordprocessingMLPackage mainDoc;
}
