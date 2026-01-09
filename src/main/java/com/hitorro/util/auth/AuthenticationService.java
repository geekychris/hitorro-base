/*
 * Copyright (c) 2006-2025 Chris Collins
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
package com.hitorro.util.auth;

import com.hitorro.util.core.Audit;
import com.hitorro.util.core.Audit.AuditStatus;
import com.hitorro.util.core.Auditable;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ServiceDefinition(dependentService = {},
		shortName = "auth",
		description = "Basic mechanism for validating user login credentials",
		debugCommands = {},
		typeManagedClasses = {},
		uiDirectories = {},
		dependentServiceInterfaces = {})
public class AuthenticationService implements Auditable {
	private static AuthenticationService s_service = null;
 	private final List<AuthenticationMethod> m_defaultList = new ArrayList<AuthenticationMethod>();
	private final Map<String, AuthenticationMethod> m_methods = new HashMap<String, AuthenticationMethod>();

	public static final AuthenticationService getService() {
		return s_service;
	}


	public void registerAuthMethod(AuthenticationMethod method, boolean partOfDefaultSearchList) {
		m_methods.put(method.getAuthenticationMethod().toLowerCase(), method);
		m_defaultList.add(method);
	}

	public AuthResponse changeUserPasswordSU(String userId, String password) {
		String[] parts = StringUtil.tokenizeFromSingleChar(userId, ":");
		if (parts.length == 2) {
			AuthenticationMethod method = getMethod(parts[0]);
			if (method == null) {
				return null;
			}
			return method.authenticate(parts[1], password);
		}
		AuthResponse resp = null;
		for (AuthenticationMethod method : m_defaultList) {
			if (method.canSetPassord()) {
				resp = method.changePasswordSU(userId, password);
				switch (resp.getState()) {
					case Valid:
						return resp;
					default:
				}
			}
		}
		return null;
	}


	public AuthResponse changeUserPassword(String userId, String password, String newPassword) {
		String[] parts = StringUtil.tokenizeFromSingleChar(userId, ":");
		if (parts.length == 2) {
			AuthenticationMethod method = getMethod(parts[0]);
			if (method == null) {
				return null;
			}
			return method.authenticate(parts[1], password);
		}
		AuthResponse resp = null;
		for (AuthenticationMethod method : m_defaultList) {
			if (method.canSetPassord()) {
				resp = method.changePassword(userId, password, newPassword);
				switch (resp.getState()) {
					case Valid:
						audit(Audit.AuthenticationTopic, userId, AuditStatus.Success, "Password change");
						return resp;
					default:
				}
			}
		}
		audit(Audit.AuthenticationTopic, userId, AuditStatus.Failure, "Password change");
		return null;
	}


	/**
	 * Authenticate a user.  If the userid is of the form: <method>:<userid> then the specific authentication method is
	 * used rather than using the cascading method.
	 *
	 * @param userId
	 * @param password
	 * @return true if valid.
	 */
	public AuthResponse authenticateUser(String userId, String password) {
		String[] parts = StringUtil.tokenizeFromSingleChar(userId, ":");
		if (parts.length == 2) {
			AuthenticationMethod method = getMethod(parts[0]);
			if (method == null) {
				return null;
			}
			return method.authenticate(parts[1], password);
		}
		AuthResponse resp = null;
		for (AuthenticationMethod method : m_defaultList) {
			resp = method.authenticate(userId, password);
			switch (resp.getState()) {
				case Valid:
					return resp;
				default:
			}
		}
		return resp;
	}


	public AuthenticationMethod getMethod(String method) {
		return m_methods.get(method.toLowerCase());
	}


	public void audit(String topic, String userName, AuditStatus status, String message, Object... args) {
		Audit.audit(_ipAddress, userName, topic, status, message, args);
	}


	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		registerAuthMethod(new TokenAuthenticationMethodImpl(200), false);
		s_service = this;
		return null;
	}

	public String start(boolean dbInit) {
		return null;
	}

	public String deInit() {
		s_service = null;
		return null;
	}
}
