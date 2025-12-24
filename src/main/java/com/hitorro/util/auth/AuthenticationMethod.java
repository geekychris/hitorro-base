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

public interface AuthenticationMethod {
	boolean canSetPassord();

	AuthResponse changePassword(String userId, String password, String newPassword);

	AuthResponse changePasswordSU(String userId, String newPassword);

	/**
	 * Name of the authentication method.  Some auth calls may call for a specific auth technique.
	 *
	 * @return authentication method.
	 */
	String getAuthenticationMethod();

	/**
	 * Authenticate a user
	 *
	 * @param userId
	 * @param passwordToken
	 * @return
	 */
	AuthResponse authenticate(String userId, String passwordToken);


	/**
	 * If the method supports it, provide basic user information for such things as organic growth.
	 *
	 * @param userId
	 * @return
	 */
	AbstractUser getAbstractUserInfo(String userId);

	/**
	 * Some methods are "token generating", meaning one asks for a token (we have assumed they are authenticated), the
	 * token is given, the token can be validated against and the token can be revoked.
	 * <p/>
	 * Tokens can have a limit to their life.  That is up to the Authentication Method to decide how long they last.
	 *
	 * @return
	 */
	boolean isTokenGenerating();
}
