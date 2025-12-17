package com.hitorro.util.auth;

import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.map.LRUHashMap;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

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
public class TokenAuthenticationMethodImpl implements TokenAuthenticationMethod {
	public static final String Key = "token";
	private LRUHashMap<String, TokenDetails> m_map = null;

	public TokenAuthenticationMethodImpl(int tokenMapSize) {
		m_map = new LRUHashMap<String, TokenDetails>(tokenMapSize);
	}

	public boolean canSetPassord() {
		return false;
	}

	public AuthResponse changePassword(String userId, String password, String newPassword) {
		return null;
	}

	public AuthResponse changePasswordSU(String userId, String newPassword) {
		return null;
	}

	public String getAuthenticationMethod() {
		return Key;
	}

	public AuthResponse authenticate(String userId, String passwordToken) {

		String key = getKey(userId, passwordToken);
		TokenDetails details = m_map.get(key);
		if (details != null) {
			details.touch();
			return new AuthResponse(
					AuthResponse.AuthResponseState.Valid, userId, true);
		}
		return new AuthResponse(AuthResponse.AuthResponseState.Invalid, userId, false);
	}

	public AbstractUser getAbstractUserInfo(String userId) {
		return null;
	}

	public boolean isTokenGenerating() {
		return true;
	}

	public String getToken(String userId) {
		TokenDetails details = new TokenDetails(userId);

		m_map.put(details.getKey(), details);
		return details.getToken();
	}

	public void revokeToken(String userId, String token) {
		String key = getKey(userId, token);
		m_map.remove(key);
	}

	private String getKey(String userId, String token) {
		return TokenDetails.getKeyFromUserIdToken(userId, token);
	}
}

class TokenDetails {
	private final String m_token;
	private final String m_key;
	private long m_lastTouchted;

	public TokenDetails(String userId) {
		String temp = Fmt.S("%s%s", userId, System.currentTimeMillis());
		long hash = FPHash64.getFP(temp);
		m_token = Long.toString(hash);
		m_key = getKeyFromUserIdToken(userId, m_token);
	}

	public static final String getKeyFromUserIdToken(String userId, String token) {
		return StringUtil.strcat(userId, "|", token);
	}

	public String getToken() {
		return m_token;
	}

	public String getKey() {
		return m_key;
	}

	public long getTouched() {
		return m_lastTouchted;
	}

	public void touch() {
		m_lastTouchted = System.currentTimeMillis();
	}
}