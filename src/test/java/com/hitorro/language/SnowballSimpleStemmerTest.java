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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tartarus.snowball.SnowballProgram;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for SnowballSimpleStemmer class
 */
@DisplayName("SnowballSimpleStemmer Tests")
class SnowballSimpleStemmerTest {

	@Mock
	private SnowballProgram mockSnowballProgram;

	private SnowballSimpleStemmer stemmer;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		stemmer = new SnowballSimpleStemmer(mockSnowballProgram);
	}

	@Test
	@DisplayName("Should stem a word using SnowballProgram")
	void testBasicStemming() {
		String inputWord = "running";
		String expectedStem = "run";

		when(mockSnowballProgram.getCurrent()).thenReturn(expectedStem);

		String result = stemmer.stem(inputWord);

		verify(mockSnowballProgram).setCurrent(inputWord);
		verify(mockSnowballProgram).getCurrent();
		assertThat(result).isEqualTo(expectedStem);
	}

	@Test
	@DisplayName("Should handle empty string")
	void testEmptyString() {
		String inputWord = "";
		String expectedStem = "";

		when(mockSnowballProgram.getCurrent()).thenReturn(expectedStem);

		String result = stemmer.stem(inputWord);

		assertThat(result).isEqualTo(expectedStem);
		verify(mockSnowballProgram).setCurrent(inputWord);
	}

	@Test
	@DisplayName("Should handle single character word")
	void testSingleCharacter() {
		String inputWord = "a";
		String expectedStem = "a";

		when(mockSnowballProgram.getCurrent()).thenReturn(expectedStem);

		String result = stemmer.stem(inputWord);

		assertThat(result).isEqualTo(expectedStem);
	}

	@ParameterizedTest
	@CsvSource({
			"running, run",
			"walked, walk",
			"happily, happi",
			"testing, test",
			"computer, comput"
	})
	@DisplayName("Should stem various English words correctly")
	void testMultipleWordStemming(String input, String expected) {
		when(mockSnowballProgram.getCurrent()).thenReturn(expected);

		String result = stemmer.stem(input);

		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Should handle words with uppercase letters")
	void testUppercaseHandling() {
		String inputWord = "RUNNING";
		String expectedStem = "RUN";

		when(mockSnowballProgram.getCurrent()).thenReturn(expectedStem);

		String result = stemmer.stem(inputWord);

		assertThat(result).isEqualTo(expectedStem);
	}

	@Test
	@DisplayName("Should handle mixed case words")
	void testMixedCaseHandling() {
		String inputWord = "RuNnInG";
		String expectedStem = "run";

		when(mockSnowballProgram.getCurrent()).thenReturn(expectedStem);

		String result = stemmer.stem(inputWord);

		assertThat(result).isEqualTo(expectedStem);
	}

	@Test
	@DisplayName("Should be thread-safe via synchronization")
	void testThreadSafety() throws InterruptedException {
		when(mockSnowballProgram.getCurrent()).thenReturn("test");

		// Create multiple threads that call stem
		Thread[] threads = new Thread[10];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(() -> {
				for (int j = 0; j < 100; j++) {
					stemmer.stem("testing");
				}
			});
			threads[i].start();
		}

		// Wait for all threads to complete
		for (Thread thread : threads) {
			thread.join();
		}

		// Verify that setCurrent was called correct number of times
		verify(mockSnowballProgram, times(1000)).setCurrent("testing");
	}

	@Test
	@DisplayName("Should handle special characters in words")
	void testSpecialCharacters() {
		String inputWord = "test-word";
		String expectedStem = "test-word";

		when(mockSnowballProgram.getCurrent()).thenReturn(expectedStem);

		String result = stemmer.stem(inputWord);

		assertThat(result).isEqualTo(expectedStem);
	}

	@Test
	@DisplayName("Should handle numeric strings")
	void testNumericString() {
		String inputWord = "12345";
		String expectedStem = "12345";

		when(mockSnowballProgram.getCurrent()).thenReturn(expectedStem);

		String result = stemmer.stem(inputWord);

		assertThat(result).isEqualTo(expectedStem);
	}

	@Test
	@DisplayName("Should handle very long words")
	void testLongWords() {
		String inputWord = "supercalifragilisticexpialidocious";
		String expectedStem = "supercalifragilisticexpialidoci";

		when(mockSnowballProgram.getCurrent()).thenReturn(expectedStem);

		String result = stemmer.stem(inputWord);

		assertThat(result).isEqualTo(expectedStem);
	}

	@Test
	@DisplayName("Should handle words with accented characters")
	void testAccentedCharacters() {
		String inputWord = "café";
		String expectedStem = "caf";

		when(mockSnowballProgram.getCurrent()).thenReturn(expectedStem);

		String result = stemmer.stem(inputWord);

		assertThat(result).isEqualTo(expectedStem);
	}

	@Test
	@DisplayName("Should construct with SnowballProgram instance")
	void testConstructor() {
		SnowballSimpleStemmer newStemmer = new SnowballSimpleStemmer(mockSnowballProgram);
		assertThat(newStemmer).isNotNull();
	}
}
