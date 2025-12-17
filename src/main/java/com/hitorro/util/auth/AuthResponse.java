package com.hitorro.util.auth;

import java.util.ArrayList;
import java.util.List;

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
public class AuthResponse {
	private final AuthResponseState state;
	private final String userId;
	private final boolean userExists;

	public AuthResponse(AuthResponseState state, String userId, boolean userExists) {
		this.state = state;
		this.userId = userId;
		this.userExists = userExists;
	}

	public boolean isValid() {
		return state.equals(AuthResponseState.Valid);
	}

	public AuthResponseState getState() {
		return state;
	}

	public String getUserId() {
		return userId;
	}

	public List<PermissionInterface> getPermissions() {
		return new ArrayList<PermissionInterface>();
	}

	public boolean getUserExists() {
		return userExists;
	}

	public enum AuthResponseState {
		Valid,
		Invalid,
		Skip
	}
}
