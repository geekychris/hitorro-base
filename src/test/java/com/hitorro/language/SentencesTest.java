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


package com.hitorro.language;

import opennlp.tools.util.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for Sentences class
 */
@DisplayName("Sentences Tests")
class SentencesTest {

	@Mock
	private SentenceSegmenter mockSegmenter;

	@Mock
	private opennlp.tools.sentdetect.SentenceDetectorME mockDetector;

	private String testContent;
	private Span[] testOffsets;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		testContent = "This is the first sentence. This is the second sentence. This is the third.";
		// String length is 75 (indices 0-74). Span end is exclusive.
		testOffsets = new Span[]{
				new Span(0, 27),   // "This is the first sentence."
				new Span(28, 56),  // "This is the second sentence."
				new Span(57, 75)   // "This is the third."
		};

		mockSegmenter.impl = mockDetector;
	}

	@Test
	@DisplayName("Should extract sentences using provided offsets")
	void testSentencesWithProvidedOffsets() {
		Sentences sentences = new Sentences(mockSegmenter, testContent, testOffsets);

		List<String> result = sentences.getSentences();

		assertThat(result).hasSize(3);
		assertThat(result.get(0)).isEqualTo("This is the first sentence.");
		assertThat(result.get(1)).isEqualTo("This is the second sentence.");
		assertThat(result.get(2)).isEqualTo("This is the third.");
	}

	@Test
	@DisplayName("Should compute offsets from segmenter when not provided")
	void testSentencesWithComputedOffsets() {
		when(mockDetector.sentPosDetect(testContent)).thenReturn(testOffsets);

		Sentences sentences = new Sentences(mockSegmenter, testContent);
		List<String> result = sentences.getSentences();

		assertThat(result).hasSize(3);
		verify(mockDetector).sentPosDetect(testContent);
	}

	@Test
	@DisplayName("Should cache sentence list after first computation")
	void testSentenceCaching() {
		when(mockDetector.sentPosDetect(testContent)).thenReturn(testOffsets);

		Sentences sentences = new Sentences(mockSegmenter, testContent);

		// First call
		List<String> result1 = sentences.getSentences();
		// Second call
		List<String> result2 = sentences.getSentences();

		assertThat(result1).isSameAs(result2);
		verify(mockDetector, times(1)).sentPosDetect(testContent); // Only called once
	}

	@Test
	@DisplayName("Should cache offsets after first computation")
	void testOffsetCaching() {
		when(mockDetector.sentPosDetect(testContent)).thenReturn(testOffsets);

		Sentences sentences = new Sentences(mockSegmenter, testContent);

		// First call
		Span[] offsets1 = sentences.getOffsets();
		// Second call
		Span[] offsets2 = sentences.getOffsets();

		assertThat(offsets1).isSameAs(offsets2);
		verify(mockDetector, times(1)).sentPosDetect(testContent);
	}

	@Test
	@DisplayName("Should trim whitespace from extracted sentences")
	void testWhitespaceTrimming() {
		String contentWithSpaces = "  First sentence.  \n  Second sentence.  ";
		Span[] spacedOffsets = new Span[]{
				new Span(0, 19),   // "  First sentence.  "
				new Span(20, 40)   // "  Second sentence.  " (content length is 41, indices 0-40)
		};

		Sentences sentences = new Sentences(mockSegmenter, contentWithSpaces, spacedOffsets);
		List<String> result = sentences.getSentences();

		assertThat(result.get(0)).isEqualTo("First sentence.");
		assertThat(result.get(1)).isEqualTo("Second sentence.");
	}

	@Test
	@DisplayName("Should filter out empty or blank sentences")
	void testEmptySentenceFiltering() {
		String contentWithBlanks = "Real sentence.     \n\n   ";
		Span[] offsetsWithBlanks = new Span[]{
				new Span(0, 14),
				new Span(14, 19),
				new Span(19, 24)
		};

		Sentences sentences = new Sentences(mockSegmenter, contentWithBlanks, offsetsWithBlanks);
		List<String> result = sentences.getSentences();

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo("Real sentence.");
	}

	@Test
	@DisplayName("Should write sentences to PrintWriter")
	void testWriteToPrintWriter() {
		Sentences sentences = new Sentences(mockSegmenter, testContent, testOffsets);

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);

		sentences.writeToPrintWriter(printWriter);

		String output = stringWriter.toString();
		assertThat(output).contains("This is the first sentence.");
		assertThat(output).contains("This is the second sentence.");
		assertThat(output).contains("This is the third.");
	}

	@Test
	@DisplayName("Should call getSentencesDirect without caching")
	void testGetSentencesDirect() {
		String[] expectedSentences = {"Sentence 1.", "Sentence 2."};
		when(mockDetector.sentDetect(testContent)).thenReturn(expectedSentences);

		Sentences sentences = new Sentences(mockSegmenter, testContent);
		String[] result = sentences.getSentencesDirect();

		assertThat(result).isEqualTo(expectedSentences);
		verify(mockDetector).sentDetect(testContent);
	}

	@Test
	@DisplayName("Should visit sentences with visitor pattern")
	void testVisitSentences() {
		Sentences sentences = new Sentences(mockSegmenter, testContent, testOffsets);

		TestSentenceVisitor visitor = new TestSentenceVisitor();
		sentences.visitSentences(visitor);

		assertThat(visitor.startCalled).isTrue();
		assertThat(visitor.endCalled).isTrue();
		assertThat(visitor.visitCount).isEqualTo(3);
	}

	@Test
	@DisplayName("Should stop visiting when visitor returns false")
	void testVisitSentencesEarlyTermination() {
		Sentences sentences = new Sentences(mockSegmenter, testContent, testOffsets);

		TestSentenceVisitor visitor = new TestSentenceVisitor(1); // Stop after 1 sentence
		sentences.visitSentences(visitor);

		assertThat(visitor.visitCount).isEqualTo(1);
		assertThat(visitor.endCalled).isFalse(); // Should not call end when terminated early
	}

	@Test
	@DisplayName("Should get probabilities from detector")
	void testGetProbabilities() {
		double[] expectedProbs = {0.95, 0.87, 0.92};
		when(mockDetector.getSentenceProbabilities()).thenReturn(expectedProbs);

		Sentences sentences = new Sentences(mockSegmenter, testContent);
		double[] result = sentences.getProbabilities();

		assertThat(result).isEqualTo(expectedProbs);
	}

	@Test
	@DisplayName("Should reset content and offsets with set method")
	void testSetMethod() {
		when(mockDetector.sentPosDetect(testContent)).thenReturn(testOffsets);

		Sentences sentences = new Sentences(mockSegmenter, "Initial content");
		sentences.getSentences(); // Cache initial sentences

		// Reset with new content
		sentences.set(mockSegmenter, testContent, null);

		// Should recompute with new content
		List<String> newSentences = sentences.getSentences();
		assertThat(newSentences).hasSize(3);
	}

	// Helper class for testing visitor pattern
	private static class TestSentenceVisitor implements SentenceVisitor {
		boolean startCalled = false;
		boolean endCalled = false;
		int visitCount = 0;
		int stopAfter = -1;

		TestSentenceVisitor() {
		}

		TestSentenceVisitor(int stopAfter) {
			this.stopAfter = stopAfter;
		}

		@Override
		public boolean init() {
			return true;
		}

		@Override
		public boolean start() {
			startCalled = true;
			return true;
		}

		@Override
		public boolean visit(String content, Span span, int sentenceNumber) {
			visitCount++;
			return stopAfter < 0 || visitCount < stopAfter;
		}

		@Override
		public boolean end() {
			endCalled = true;
			return true;
		}

		@Override
		public boolean finishUse() {
			return true;
		}
	}
}
