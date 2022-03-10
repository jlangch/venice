package com.github.jlangch.venice.util.word;


import static org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour.RENAME_IF_NAME_EXISTS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.docx4j.dml.CTBlip;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;

	
public class DocxMerge {

	public static DocxMerge of(final byte[] mainDoc) throws Exception {
		if (mainDoc == null) {
			throw new IllegalArgumentException("A mainDoc must not be null!");
		}

		try(InputStream is = new ByteArrayInputStream(mainDoc)) {
			return new DocxMerge(WordprocessingMLPackage.load(is));
		}
	}

	public static DocxMerge of(final ByteBuffer mainDoc) throws Exception {
		if (mainDoc == null) {
			throw new IllegalArgumentException("A mainDoc must not be null!");
		}
		
		return of(mainDoc.array());
	}

	public static DocxMerge of(final InputStream mainDocIs) throws Exception {
		if (mainDocIs == null) {
			throw new IllegalArgumentException("A mainDocIs must not be null!");
		}

		try {
			return new DocxMerge(WordprocessingMLPackage.load(mainDocIs));
		}
		finally {
			try {
				mainDocIs.close();
			}
			catch (Exception e) {}
		}
	}

	public static DocxMerge of(final File mainDocFile) throws Exception {
		if (mainDocFile == null) {
			throw new IllegalArgumentException("A mainDocFile must not be null!");
		}

		return new DocxMerge(WordprocessingMLPackage.load(mainDocFile));
	}

	
	private DocxMerge(final WordprocessingMLPackage mainDoc) {
		this.mainDoc = mainDoc;
	}
	
	
	public WordprocessingMLPackage doc() {
		return mainDoc;
	}
	

	public byte[] saveToBytes() throws Exception {
		try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			mainDoc.save(os);
			os.close();
			return os.toByteArray();
		}
	}

	public ByteBuffer saveToByteBuffer() throws Exception {
		return ByteBuffer.wrap(saveToBytes());
	}

	public void saveTo(final File file) throws Exception {
		if (file == null) {
			throw new IllegalArgumentException("A file must not be null!");
		}
		
		mainDoc.save(file);
	}
	
	public void saveTo(final OutputStream os) throws Exception {
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

	
	public DocxMerge mergeBody(final byte[] data) throws Exception {
		if (data == null) {
			throw new IllegalArgumentException("A data must not be null!");
		}

		try(InputStream is = new ByteArrayInputStream(data)) {
			return mergeBody(WordprocessingMLPackage.load(is));
		}
	}
	
	public DocxMerge mergeBody(final InputStream is) throws Exception {
		if (is == null) {
			throw new IllegalArgumentException("An is must not be null!");
		}
		
		try {
			return mergeBody(WordprocessingMLPackage.load(is));
		}
		finally {
			try {
				is.close();
			} 
			catch (Exception e) {}
		}
	}

	public DocxMerge mergeBody(final File f) throws Exception {
		if (f == null) {
			throw new IllegalArgumentException("A file f must not be null!");
		}

		return mergeBody(WordprocessingMLPackage.load(f));
	}

	public DocxMerge mergeBody(final WordprocessingMLPackage d) throws Exception {
		if (d == null) {
			throw new IllegalArgumentException("A WordprocessingMLPackage d must not be null!");
		}

		final List<Object> body = d.getMainDocumentPart().getJAXBNodesViaXPath("//w:body", false);
		for(Object b : body){
			final List<Object> filhos = ((org.docx4j.wml.Body)b).getContent();
			for(Object k : filhos) {
				mainDoc.getMainDocumentPart().addObject(k);
			}
		}

		return this;
	}

	public DocxMerge mergeBodyWithBlip(final byte[] data) throws Exception {
		if (data == null) {
			throw new IllegalArgumentException("A data must not be null!");
		}

		try(InputStream is = new ByteArrayInputStream(data)) {
			return mergeBodyWithBlip(WordprocessingMLPackage.load(is));
		}
	}
	
	public DocxMerge mergeBodyWithBlip(final InputStream is) throws Exception {
		if (is == null) {
			throw new IllegalArgumentException("An is must not be null!");
		}

		try {
			return mergeBodyWithBlip(WordprocessingMLPackage.load(is));
		}
		finally {
			try {
				is.close();
			}
			catch (Exception e) {}
		}
	}

	public DocxMerge mergeBodyWithBlip(final File f) throws Exception {
		if (f == null) {
			throw new IllegalArgumentException("A file f must not be null!");
		}

		return mergeBodyWithBlip(WordprocessingMLPackage.load(f));
	}
	
	public DocxMerge mergeBodyWithBlip(final WordprocessingMLPackage e) throws Exception {
		if (e == null) {
			throw new IllegalArgumentException("A WordprocessingMLPackage e must not be null!");
		}

		final List<Object> body2 = e.getMainDocumentPart().getJAXBNodesViaXPath("//w:body", false);
		for(Object b : body2){
			final List<Object> filhos = ((org.docx4j.wml.Body)b).getContent();
			for(Object k : filhos) {
				mainDoc.getMainDocumentPart().addObject(k);
			}
		}

		final List<Object> blips = e.getMainDocumentPart().getJAXBNodesViaXPath("//a:blip", false);
		for(Object el : blips) {
			final CTBlip blip = (CTBlip) el;
			final RelationshipsPart parts = e.getMainDocumentPart().getRelationshipsPart();
			final Relationship rel = parts.getRelationshipByID(blip.getEmbed());
			final Part part = parts.getPart(rel);
			
			final Relationship newrel = mainDoc.getMainDocumentPart()
											   .addTargetPart(part, RENAME_IF_NAME_EXISTS);
			
			blip.setEmbed(newrel.getId());
			
			mainDoc.getMainDocumentPart()
				   .addTargetPart(
						e.getParts()
						 .getParts()
						 .get(new PartName("/word/"+rel.getTarget())));
		}
		
		return this;
	}
	

	private final WordprocessingMLPackage mainDoc;
}
