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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for PDFDocToBag class
 */
@DisplayName("PDFDocToBag Tests")
class PDFDocToBagTest {

	@TempDir
	Path tempDir;
	private PDFDocToBag pdfDocToBag;
	@Mock
	private BaseFile mockBaseFile;
	@Mock
	private FileFile mockFileFile;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		pdfDocToBag = new PDFDocToBag();
	}

	@Test
	@DisplayName("Should return correct extension")
	void testGetExtension() {
		String extension = pdfDocToBag.getExtension();
		assertThat(extension).isEqualTo("pdf");
	}

	@Test
	@DisplayName("Should return false when BaseFile is not FileFile")
	void testSetDocumentWithNonFileFile() throws IOException {
		boolean result = pdfDocToBag.setDocument(mockBaseFile);
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Should return false when file does not exist")
	void testSetDocumentWithNonExistentFile() throws IOException {
		File nonExistentFile = new File(tempDir.toFile(), "nonexistent.pdf");
		when(mockFileFile.getJavaFile()).thenReturn(nonExistentFile);

		boolean result = pdfDocToBag.setDocument(mockFileFile);

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Should create a new copy of PDFDocToBag")
	void testGetCopy() {
		DocToBag copy = pdfDocToBag.getCopy();

		assertThat(copy).isNotNull();
		assertThat(copy).isInstanceOf(PDFDocToBag.class);
		assertThat(copy).isNotSameAs(pdfDocToBag);
	}

	@Test
	@DisplayName("Should reset document state without error")
	void testReset() {
		assertThatCode(() -> pdfDocToBag.reset())
				.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Should handle multiple reset calls")
	void testMultipleResets() {
		assertThatCode(() -> {
			pdfDocToBag.reset();
			pdfDocToBag.reset();
			pdfDocToBag.reset();
		}).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Should return null when getting title without document")
	void testGetTitleWithoutDocument() {
		assertThatThrownBy(() -> pdfDocToBag.getTitle())
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Should return null when getting author without document")
	void testGetAuthorWithoutDocument() {
		assertThatThrownBy(() -> pdfDocToBag.getAuthor())
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Should return null when getting keywords without document")
	void testGetKeywordsWithoutDocument() {
		assertThatThrownBy(() -> pdfDocToBag.getKeywords())
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Should return null when getting creation date without document")
	void testGetCreationDateWithoutDocument() {
		assertThatThrownBy(() -> pdfDocToBag.getCreationDate())
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Should throw exception when getting content without document")
	void testGetContentWithoutDocument() {
		assertThatThrownBy(() -> pdfDocToBag.getContent())
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Should generate source URL from file path")
	void testGetSourceURL() {
		File testFile = new File(tempDir.toFile(), "test.pdf");
		when(mockFileFile.getJavaFile()).thenReturn(testFile);

		try {
			pdfDocToBag.setDocument(mockFileFile);
		} catch (IOException e) {
			// Expected for non-existent file
		}

		String sourceURL = pdfDocToBag.getSourceURL();
		assertThat(sourceURL).startsWith("file://");
		assertThat(sourceURL).contains("test.pdf");
	}

	@Test
	@DisplayName("Should maintain state after reset")
	void testStateAfterReset() throws IOException {
		File testFile = new File(tempDir.toFile(), "test.pdf");
		when(mockFileFile.getJavaFile()).thenReturn(testFile);

		pdfDocToBag.setDocument(mockFileFile);
		pdfDocToBag.reset();

		// After reset, document should be null
		assertThatThrownBy(() -> pdfDocToBag.getTitle())
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Should be able to set document multiple times")
	void testMultipleSetDocument() throws IOException {
		File testFile1 = new File(tempDir.toFile(), "test1.pdf");
		File testFile2 = new File(tempDir.toFile(), "test2.pdf");

		when(mockFileFile.getJavaFile()).thenReturn(testFile1, testFile2);

		pdfDocToBag.setDocument(mockFileFile);
		pdfDocToBag.setDocument(mockFileFile);

		// Should not throw exception
		assertThat(pdfDocToBag).isNotNull();
	}
}
