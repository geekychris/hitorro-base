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


package com.hitorro.util.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for DomainValue class
 */
@DisplayName("DomainValue Tests")
class DomainValueTest {

	private DomainValue domainValue;

	@BeforeEach
	void setUp() {
		domainValue = new DomainValue();
	}

	@Test
	@DisplayName("Should create empty DomainValue with default constructor")
	void testDefaultConstructor() {
		DomainValue dv = new DomainValue();

		assertThat(dv).isNotNull();
		assertThat(dv.getDomain()).isNull();
		assertThat(dv.getValue()).isNull();
	}

	@Test
	@DisplayName("Should create DomainValue with domain and value")
	void testParameterizedConstructor() {
		DomainValue dv = new DomainValue("testDomain", "testValue");

		assertThat(dv.getDomain()).isEqualTo("testDomain");
		assertThat(dv.getValue()).isEqualTo("testValue");
	}

	@Test
	@DisplayName("Should set and get domain")
	void testSetAndGetDomain() {
		domainValue.setDomain("myDomain");

		assertThat(domainValue.getDomain()).isEqualTo("myDomain");
	}

	@Test
	@DisplayName("Should set and get value")
	void testSetAndGetValue() {
		domainValue.setValue("myValue");

		assertThat(domainValue.getValue()).isEqualTo("myValue");
	}

	@Test
	@DisplayName("Should handle null domain")
	void testNullDomain() {
		domainValue.setDomain(null);

		assertThat(domainValue.getDomain()).isNull();
	}

	@Test
	@DisplayName("Should handle null value")
	void testNullValue() {
		domainValue.setValue(null);

		assertThat(domainValue.getValue()).isNull();
	}

	@Test
	@DisplayName("Should handle empty domain")
	void testEmptyDomain() {
		domainValue.setDomain("");

		assertThat(domainValue.getDomain()).isEmpty();
	}

	@Test
	@DisplayName("Should handle empty value")
	void testEmptyValue() {
		domainValue.setValue("");

		assertThat(domainValue.getValue()).isEmpty();
	}

	@Test
	@DisplayName("Should generate concatenated string from domain and value")
	void testGetConcatenated() {
		String result = DomainValue.getConcatinated("domain", "value");

		assertThat(result).isEqualTo("domain?value");
	}

	@ParameterizedTest
	@CsvSource({
			"domain1, value1, domain1?value1",
			"user, admin, user?admin",
			"category, electronics, category?electronics",
			"status, active, status?active"
	})
	@DisplayName("Should concatenate various domain-value pairs correctly")
	void testGetConcatenatedWithVariousInputs(String domain, String value, String expected) {
		String result = DomainValue.getConcatinated(domain, value);

		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Should generate index tuple from domain and value")
	void testGetAsIndexTuple() {
		domainValue.setDomain("testDomain");
		domainValue.setValue("testValue");

		String tuple = domainValue.getAsIndexTuple();

		assertThat(tuple).isEqualTo("testDomain?testValue");
	}

	@Test
	@DisplayName("Should handle null values in index tuple")
	void testGetAsIndexTupleWithNulls() {
		domainValue.setDomain(null);
		domainValue.setValue(null);

		String tuple = domainValue.getAsIndexTuple();

		assertThat(tuple).contains("?");
	}

	@Test
	@DisplayName("Should return correct serialization version")
	void testGetSerializationVersion() {
		int version = domainValue.getSerializationVersion();

		assertThat(version).isEqualTo(DomainValue.SerializationVersion);
		assertThat(version).isEqualTo(1);
	}

	@Test
	@DisplayName("Should return false for hasGuid")
	void testHasGuid() {
		boolean hasGuid = domainValue.hasGuid();

		assertThat(hasGuid).isFalse();
	}

	@Test
	@DisplayName("Should have correct separator constant")
	void testSeparatorConstant() {
		assertThat(DomainValue.Seperator).isEqualTo("?");
	}

	@Test
	@DisplayName("Should handle special characters in domain")
	void testSpecialCharactersInDomain() {
		domainValue.setDomain("domain@#$%");

		assertThat(domainValue.getDomain()).isEqualTo("domain@#$%");
	}

	@Test
	@DisplayName("Should handle special characters in value")
	void testSpecialCharactersInValue() {
		domainValue.setValue("value!@#$%^&*()");

		assertThat(domainValue.getValue()).isEqualTo("value!@#$%^&*()");
	}

	@Test
	@DisplayName("Should handle unicode characters")
	void testUnicodeCharacters() {
		domainValue.setDomain("域名");
		domainValue.setValue("值");

		assertThat(domainValue.getDomain()).isEqualTo("域名");
		assertThat(domainValue.getValue()).isEqualTo("值");
	}

	@Test
	@DisplayName("Should handle very long strings")
	void testLongStrings() {
		String longDomain = "a".repeat(1000);
		String longValue = "b".repeat(1000);

		domainValue.setDomain(longDomain);
		domainValue.setValue(longValue);

		assertThat(domainValue.getDomain()).hasSize(1000);
		assertThat(domainValue.getValue()).hasSize(1000);
	}

	@Test
	@DisplayName("Should allow updating domain multiple times")
	void testMultipleDomainUpdates() {
		domainValue.setDomain("domain1");
		assertThat(domainValue.getDomain()).isEqualTo("domain1");

		domainValue.setDomain("domain2");
		assertThat(domainValue.getDomain()).isEqualTo("domain2");

		domainValue.setDomain("domain3");
		assertThat(domainValue.getDomain()).isEqualTo("domain3");
	}

	@Test
	@DisplayName("Should allow updating value multiple times")
	void testMultipleValueUpdates() {
		domainValue.setValue("value1");
		assertThat(domainValue.getValue()).isEqualTo("value1");

		domainValue.setValue("value2");
		assertThat(domainValue.getValue()).isEqualTo("value2");

		domainValue.setValue("value3");
		assertThat(domainValue.getValue()).isEqualTo("value3");
	}

	@Test
	@DisplayName("Should handle whitespace in domain and value")
	void testWhitespace() {
		domainValue.setDomain("  domain with spaces  ");
		domainValue.setValue("  value with spaces  ");

		assertThat(domainValue.getDomain()).isEqualTo("  domain with spaces  ");
		assertThat(domainValue.getValue()).isEqualTo("  value with spaces  ");
	}

	@Test
	@DisplayName("Should generate different tuples for different values")
	void testUniqueTuples() {
		DomainValue dv1 = new DomainValue("domain1", "value1");
		DomainValue dv2 = new DomainValue("domain1", "value2");
		DomainValue dv3 = new DomainValue("domain2", "value1");

		assertThat(dv1.getAsIndexTuple()).isNotEqualTo(dv2.getAsIndexTuple());
		assertThat(dv1.getAsIndexTuple()).isNotEqualTo(dv3.getAsIndexTuple());
		assertThat(dv2.getAsIndexTuple()).isNotEqualTo(dv3.getAsIndexTuple());
	}
}
