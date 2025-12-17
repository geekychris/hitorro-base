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


package com.hitorro.integration;

import com.hitorro.base.docprocessing.docmap.DocToBag;
import com.hitorro.base.docprocessing.docmap.PDFDocToBag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for document processing components
 */
@DisplayName("Document Processing Integration Tests")
class DocumentProcessingIntegrationTest {

	@Test
	@DisplayName("Should create PDFDocToBag instance and verify interface implementation")
	void testPDFDocToBagImplementation() {
		DocToBag docToBag = new PDFDocToBag();

		assertThat(docToBag).isNotNull();
		assertThat(docToBag).isInstanceOf(PDFDocToBag.class);
		assertThat(docToBag.getExtension()).isEqualTo("pdf");
	}

	@Test
	@DisplayName("Should create multiple DocToBag instances")
	void testMultipleDocToBagInstances() {
		PDFDocToBag pdf1 = new PDFDocToBag();
		PDFDocToBag pdf2 = new PDFDocToBag();
		PDFDocToBag pdf3 = new PDFDocToBag();

		assertThat(pdf1).isNotSameAs(pdf2);
		assertThat(pdf2).isNotSameAs(pdf3);
		assertThat(pdf1.getExtension()).isEqualTo(pdf2.getExtension());
	}

	@Test
	@DisplayName("Should create copies of DocToBag instances")
	void testDocToBagCopying() {
		PDFDocToBag original = new PDFDocToBag();
		DocToBag copy = original.getCopy();

		assertThat(copy).isNotNull();
		assertThat(copy).isNotSameAs(original);
		assertThat(copy).isInstanceOf(PDFDocToBag.class);
		assertThat(copy.getExtension()).isEqualTo(original.getExtension());
	}

	@Test
	@DisplayName("Should handle multiple reset operations")
	void testMultipleResetOperations() {
		PDFDocToBag docToBag = new PDFDocToBag();

		// Should not throw exceptions
		assertThatCode(() -> {
			docToBag.reset();
			docToBag.reset();
			docToBag.reset();
		}).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Should maintain state after creating multiple copies")
	void testStateMaintenance() {
		PDFDocToBag original = new PDFDocToBag();

		DocToBag copy1 = original.getCopy();
		DocToBag copy2 = original.getCopy();
		DocToBag copy3 = original.getCopy();

		assertThat(copy1.getExtension()).isEqualTo("pdf");
		assertThat(copy2.getExtension()).isEqualTo("pdf");
		assertThat(copy3.getExtension()).isEqualTo("pdf");
	}
}
