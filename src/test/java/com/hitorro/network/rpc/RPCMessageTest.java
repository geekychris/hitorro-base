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


package com.hitorro.network.rpc;

import com.hitorro.util.typesystem.HTSerializable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for RPCMessage class
 */
@DisplayName("RPCMessage Tests")
class RPCMessageTest {

	private RPCMessage rpcMessage;

	@Mock
	private HTSerializable mockPayload;

	@Mock
	private RPCHandler mockHandler;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		rpcMessage = new RPCMessage();
	}

	@Test
	@DisplayName("Should create RPCMessage with default constructor")
	void testDefaultConstructor() {
		RPCMessage msg = new RPCMessage();

		assertThat(msg).isNotNull();
		assertThat(msg.isRequest()).isTrue(); // Default is request
	}

	@Test
	@DisplayName("Should set and get endpoint information")
	void testSetEndPoints() {
		rpcMessage.setEndPoints("192.168.1.1", 8080, "192.168.1.2", 9090, "testMethod", true);

		assertThat(rpcMessage.getFromIPAddress()).isEqualTo("192.168.1.1");
		assertThat(rpcMessage.getFromPortNumber()).isEqualTo(8080);
		assertThat(rpcMessage.getToIPAddress()).isEqualTo("192.168.1.2");
		assertThat(rpcMessage.getToPortNumber()).isEqualTo(9090);
		assertThat(rpcMessage.getMethod()).isEqualTo("testMethod");
		assertThat(rpcMessage.getSynchronousRequest()).isTrue();
	}

	@Test
	@DisplayName("Should set and get synchronous request flag")
	void testSetSynchronousRequest() {
		rpcMessage.setSynchronousRequest(true);
		assertThat(rpcMessage.getSynchronousRequest()).isTrue();

		rpcMessage.setSynchronousRequest(false);
		assertThat(rpcMessage.getSynchronousRequest()).isFalse();
	}

	@Test
	@DisplayName("Should set and get payload")
	void testSetAndGetPayload() {
		rpcMessage.setPayload(mockPayload);

		assertThat(rpcMessage.getPayload()).isEqualTo(mockPayload);
	}

	@Test
	@DisplayName("Should handle null payload")
	void testNullPayload() {
		rpcMessage.setPayload(null);

		assertThat(rpcMessage.getPayload()).isNull();
	}

	@Test
	@DisplayName("Should set and get status code")
	void testSetAndGetStatusCode() {
		rpcMessage.setStatusCode(RPCMessage.StatusOk);

		assertThat(rpcMessage.getStatusCode()).isEqualTo(RPCMessage.StatusOk);
		assertThat(rpcMessage.getStatusCode()).isEqualTo(1);
	}

	@ParameterizedTest
	@ValueSource(ints = {0, 1, 100, 404, 500, -1})
	@DisplayName("Should handle various status codes")
	void testVariousStatusCodes(int statusCode) {
		rpcMessage.setStatusCode(statusCode);

		assertThat(rpcMessage.getStatusCode()).isEqualTo(statusCode);
	}

	@Test
	@DisplayName("Should set and get status message")
	void testSetAndGetStatusMessage() {
		String message = "Operation completed successfully";
		rpcMessage.setStatusMessage(message);

		assertThat(rpcMessage.getStatusMessage()).isEqualTo(message);
	}

	@Test
	@DisplayName("Should handle null status message")
	void testNullStatusMessage() {
		rpcMessage.setStatusMessage(null);

		assertThat(rpcMessage.getStatusMessage()).isNull();
	}

	@Test
	@DisplayName("Should handle empty status message")
	void testEmptyStatusMessage() {
		rpcMessage.setStatusMessage("");

		assertThat(rpcMessage.getStatusMessage()).isEmpty();
	}

	@Test
	@DisplayName("Should set and get clear arguments")
	void testSetAndGetClearArguments() {
		String args = "arg1=value1&arg2=value2";
		rpcMessage.setClearArguments(args);

		assertThat(rpcMessage.getClearArguments()).isEqualTo(args);
	}

	@Test
	@DisplayName("Should set and get sequence number")
	void testSetAndGetSequenceNumber() {
		long sequenceNumber = 123456789L;
		rpcMessage.setSequenceNumber(sequenceNumber);

		assertThat(rpcMessage.getSequenceNumber()).isEqualTo(sequenceNumber);
	}

	@Test
	@DisplayName("Should handle large sequence numbers")
	void testLargeSequenceNumber() {
		long largeNumber = Long.MAX_VALUE;
		rpcMessage.setSequenceNumber(largeNumber);

		assertThat(rpcMessage.getSequenceNumber()).isEqualTo(largeNumber);
	}

	@Test
	@DisplayName("Should handle negative sequence numbers")
	void testNegativeSequenceNumber() {
		long negativeNumber = -12345L;
		rpcMessage.setSequenceNumber(negativeNumber);

		assertThat(rpcMessage.getSequenceNumber()).isEqualTo(negativeNumber);
	}

	@Test
	@DisplayName("Should default to request message")
	void testIsRequestDefault() {
		assertThat(rpcMessage.isRequest()).isTrue();
	}

	@Test
	@DisplayName("Should set and get cluster name")
	void testSetAndGetClusterName() {
		String clusterName = "production-cluster";
		rpcMessage.setClusterName(clusterName);

		assertThat(rpcMessage.getClusterName()).isEqualTo(clusterName);
	}

	@Test
	@DisplayName("Should handle null cluster name")
	void testNullClusterName() {
		rpcMessage.setClusterName(null);

		assertThat(rpcMessage.getClusterName()).isNull();
	}

	@Test
	@DisplayName("Should set and get requires DMS session flag")
	void testSetAndGetRequiresDMSSession() {
		rpcMessage.setRequiresDMSSession(true);
		assertThat(rpcMessage.getRequiresDMSSession()).isTrue();

		rpcMessage.setRequiresDMSSession(false);
		assertThat(rpcMessage.getRequiresDMSSession()).isFalse();
	}

	@Test
	@DisplayName("Should return correct serialization version")
	void testGetSerializationVersion() {
		int version = rpcMessage.getSerializationVersion();

		assertThat(version).isEqualTo(RPCMessage.SerializationVersion);
		assertThat(version).isEqualTo(1);
	}

	@Test
	@DisplayName("Should return false for isPersisted")
	void testIsPersisted() {
		assertThat(rpcMessage.isPersisted()).isFalse();
	}

	@Test
	@DisplayName("Should return false for hasGuid")
	void testHasGuid() {
		assertThat(rpcMessage.hasGuid()).isFalse();
	}

	@Test
	@DisplayName("Should return false for hasSoftGuid")
	void testHasSoftGuid() {
		assertThat(rpcMessage.hasSoftGuid()).isFalse();
	}

	@Test
	@DisplayName("Should set and get RPC handler")
	void testSetAndGetHandler() {
		rpcMessage.setHandler(mockHandler);

		assertThat(rpcMessage.getHandler()).isEqualTo(mockHandler);
	}

	@Test
	@DisplayName("Should handle null handler")
	void testNullHandler() {
		rpcMessage.setHandler(null);

		assertThat(rpcMessage.getHandler()).isNull();
	}

	@Test
	@DisplayName("Should have correct magic number constant")
	void testMagicRPCNumber() {
		assertThat(RPCMessage.MagicRPCNumber).isEqualTo(31068788);
	}

	@Test
	@DisplayName("Should have correct status OK constant")
	void testStatusOkConstant() {
		assertThat(RPCMessage.StatusOk).isEqualTo(1);
	}

	@Test
	@DisplayName("Should handle IPv4 addresses")
	void testIPv4Addresses() {
		rpcMessage.setEndPoints("192.168.1.100", 8080, "10.0.0.50", 9090, "test", true);

		assertThat(rpcMessage.getFromIPAddress()).isEqualTo("192.168.1.100");
		assertThat(rpcMessage.getToIPAddress()).isEqualTo("10.0.0.50");
	}

	@Test
	@DisplayName("Should handle IPv6 addresses")
	void testIPv6Addresses() {
		rpcMessage.setEndPoints("2001:0db8::1", 8080, "fe80::1", 9090, "test", true);

		assertThat(rpcMessage.getFromIPAddress()).isEqualTo("2001:0db8::1");
		assertThat(rpcMessage.getToIPAddress()).isEqualTo("fe80::1");
	}

	@Test
	@DisplayName("Should handle localhost")
	void testLocalhostAddress() {
		rpcMessage.setEndPoints("localhost", 8080, "127.0.0.1", 9090, "test", true);

		assertThat(rpcMessage.getFromIPAddress()).isEqualTo("localhost");
		assertThat(rpcMessage.getToIPAddress()).isEqualTo("127.0.0.1");
	}

	@Test
	@DisplayName("Should handle port number edge cases")
	void testPortNumberEdgeCases() {
		rpcMessage.setEndPoints("localhost", 1, "localhost", 65535, "test", true);

		assertThat(rpcMessage.getFromPortNumber()).isEqualTo(1);
		assertThat(rpcMessage.getToPortNumber()).isEqualTo(65535);
	}

	@Test
	@DisplayName("Should handle method names with special characters")
	void testMethodNamesWithSpecialCharacters() {
		rpcMessage.setEndPoints("localhost", 8080, "localhost", 9090, "test.method_name-v2", true);

		assertThat(rpcMessage.getMethod()).isEqualTo("test.method_name-v2");
	}

	@Test
	@DisplayName("Should allow updating endpoints multiple times")
	void testMultipleEndpointUpdates() {
		rpcMessage.setEndPoints("host1", 8080, "host2", 9090, "method1", true);
		assertThat(rpcMessage.getFromIPAddress()).isEqualTo("host1");

		rpcMessage.setEndPoints("host3", 7070, "host4", 6060, "method2", false);
		assertThat(rpcMessage.getFromIPAddress()).isEqualTo("host3");
		assertThat(rpcMessage.getFromPortNumber()).isEqualTo(7070);
		assertThat(rpcMessage.getSynchronousRequest()).isFalse();
	}

	@Test
	@DisplayName("Should handle very long method names")
	void testLongMethodName() {
		String longMethodName = "a".repeat(1000);
		rpcMessage.setEndPoints("localhost", 8080, "localhost", 9090, longMethodName, true);

		assertThat(rpcMessage.getMethod()).hasSize(1000);
	}

	@Test
	@DisplayName("Should handle status message with special characters")
	void testStatusMessageWithSpecialCharacters() {
		String specialMessage = "Error: file not found @ /path/to/file.txt (Code: #404)";
		rpcMessage.setStatusMessage(specialMessage);

		assertThat(rpcMessage.getStatusMessage()).isEqualTo(specialMessage);
	}
}
