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
package com.hitorro.swig;

public class SwigUtils {
	public static final int makeHardLink(String oldpath, String newpath) {
		return SwigUtilsJNI.makeHardLink(oldpath, newpath);
	}

	public static final int makeSymboliLink(String oldpath, String newpath) {
		return SwigUtilsJNI.makeSymboliLink(oldpath, newpath);
	}

	public static final int unlink(String path) {
		return SwigUtilsJNI.unlink(path);
	}

	public static StatFSData statFS(String path) {
		long cPtr = SwigUtilsJNI.statFS(path);
		return (cPtr == 0) ? null : new StatFSData(cPtr, false);
	}

	public static StatData stat(String path) {
		long cPtr = SwigUtilsJNI.stat(path);
		return (cPtr == 0) ? null : new StatData(cPtr, false);
	}

	public static final int getProcessId() {
		return SwigUtilsJNI.getProcessId();
	}

	public static final int getParentProcessId() {
		return SwigUtilsJNI.getParentProcessId();
	}

	public static final int setUMask(int mask) {
		return SwigUtilsJNI.setUMask(mask);
	}

	public static final int getProcessGroupId() {
		return SwigUtilsJNI.getProcessGroupId();
	}

	public static final int setProcessGroupId(int processID, int pgrp) {
		return SwigUtilsJNI.setProcessGroupId(processID, pgrp);
	}

	public static final int sendSignal(int processId, int signal) {
		return SwigUtilsJNI.sendSignal(processId, signal);
	}

	public static final int getGroupId() {
		return SwigUtilsJNI.getGroupId();
	}

	public static final int getEffectiveGroupId() {
		return SwigUtilsJNI.getEffectiveGroupId();
	}

	public static final int setGroupId(int gid) {
		return SwigUtilsJNI.setGroupId(gid);
	}

	public static final int setEffectiveGroupID(int gid) {
		return SwigUtilsJNI.setEffectiveGroupID(gid);
	}

	public static final int setEngrent() {
		return SwigUtilsJNI.setEngrent();
	}

	public static final int getGrent() {
		return SwigUtilsJNI.getGrent();
	}

	public static final int getLimit(int resource, int isHard) {
		return SwigUtilsJNI.getLimit(resource, isHard);
	}

	public static final int setEffectiveLimit(int resource, int isHard, int limit) {
		return SwigUtilsJNI.setEffectiveLimit(resource, isHard, limit);
	}

	public static final int getNice(int which, int whom) {
		return SwigUtilsJNI.getNice(which, whom);
	}

	public static final int setNice(int which, int whom, int nice) {
		return SwigUtilsJNI.setNice(which, whom, nice);
	}

}
