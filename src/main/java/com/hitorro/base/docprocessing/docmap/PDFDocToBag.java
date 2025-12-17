/*
 * Copyright (c) 2006 - 2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.hitorro.base.docprocessing.docmap;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFile;
import com.hitorro.util.core.string.Fmt;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

/**
 * Handle extraction of pdf fields from a pdf document.
 */
public class PDFDocToBag implements DocToBag {
	private PDDocument doc;
	private PDDocumentInformation d;
	private File localFile;
	private PDFTextStripper stripper = null;

	public String getExtension() {
		return "pdf";
	}

	public String getSourceURL() {
		return Fmt.S("file://%s", localFile.getAbsolutePath());
	}

	public boolean setDocument(BaseFile f) throws IOException {
		reset();
		if (f instanceof FileFile) {
			localFile = ((FileFile) f).getJavaFile();
			if (!localFile.exists()) {
				return false;
			}
		} else {
			return false;
		}
		doc = Loader.loadPDF(new RandomAccessReadBufferedFile(localFile));
		d = doc.getDocumentInformation();
		return true;
	}

	public String getTitle() {
		return d.getTitle();
	}

	public String getAuthor() {
		return d.getAuthor();
	}

	public String getKeywords() {
		return d.getKeywords();
	}

	public Date getCreationDate() {

		Calendar c = d.getCreationDate();
		if (c != null) {
			return c.getTime();
		}
		return null;
	}

	public String getContent() throws IOException {
		StringBuilder sb = new StringBuilder();

		StringWriter writer = new StringWriter();

		stripper = new PDFTextStripper();

		stripper.writeText(doc, writer);

		String contents = writer.getBuffer().toString();

		StringReader reader = new StringReader(contents);

		char[] buf = new char[1024];
		int size = reader.read(buf);
		while (size > 0) {
			sb.append(buf, 0, size);
			size = reader.read(buf);
		}
		return sb.toString();
	}

	public DocToBag getCopy() {
		return new PDFDocToBag();
	}

	public void reset() throws IOException {
		if (doc != null) {
			doc.close();
		}
		doc = null;
		d = null;
	}
}
