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

public class StatData {
	protected boolean swigCMemOwn;
	private long swigCPtr;

	protected StatData(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
	}

	public StatData() {
		this(SwigUtilsJNI.new_StatData(), true);
	}

	protected static long getCPtr(StatData obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}

	protected void finalize() {
		delete();
	}

	public synchronized void delete() {
		if (swigCPtr != 0 && swigCMemOwn) {
			swigCMemOwn = false;
			SwigUtilsJNI.delete_StatData(swigCPtr);
		}
		swigCPtr = 0;
	}

	public int getDeviceInodeResidesOn() {
		return SwigUtilsJNI.StatData_deviceInodeResidesOn_get(swigCPtr, this);
	}

	public void setDeviceInodeResidesOn(int value) {
		SwigUtilsJNI.StatData_deviceInodeResidesOn_set(swigCPtr, this, value);
	}

	public int getInodesNumber() {
		return SwigUtilsJNI.StatData_inodesNumber_get(swigCPtr, this);
	}

	public void setInodesNumber(int value) {
		SwigUtilsJNI.StatData_inodesNumber_set(swigCPtr, this, value);
	}

	public int getInodeProtectionMode() {
		return SwigUtilsJNI.StatData_inodeProtectionMode_get(swigCPtr, this);
	}

	public void setInodeProtectionMode(int value) {
		SwigUtilsJNI.StatData_inodeProtectionMode_set(swigCPtr, this, value);
	}

	public int getNumberOfHardlinks() {
		return SwigUtilsJNI.StatData_numberOfHardlinks_get(swigCPtr, this);
	}

	public void setNumberOfHardlinks(int value) {
		SwigUtilsJNI.StatData_numberOfHardlinks_set(swigCPtr, this, value);
	}

	public int getUserId() {
		return SwigUtilsJNI.StatData_userId_get(swigCPtr, this);
	}

	public void setUserId(int value) {
		SwigUtilsJNI.StatData_userId_set(swigCPtr, this, value);
	}

	public int getGroupId() {
		return SwigUtilsJNI.StatData_groupId_get(swigCPtr, this);
	}

	public void setGroupId(int value) {
		SwigUtilsJNI.StatData_groupId_set(swigCPtr, this, value);
	}

	public int getDeviceType() {
		return SwigUtilsJNI.StatData_deviceType_get(swigCPtr, this);
	}

	public void setDeviceType(int value) {
		SwigUtilsJNI.StatData_deviceType_set(swigCPtr, this, value);
	}

	public long getLastAccessTime() {
		return SwigUtilsJNI.StatData_lastAccessTime_get(swigCPtr, this);
	}

	public void setLastAccessTime(long value) {
		SwigUtilsJNI.StatData_lastAccessTime_set(swigCPtr, this, value);
	}

	public long getLastDateModified() {
		return SwigUtilsJNI.StatData_lastDateModified_get(swigCPtr, this);
	}

	public void setLastDateModified(long value) {
		SwigUtilsJNI.StatData_lastDateModified_set(swigCPtr, this, value);
	}

	public long getLastFileStatusChange() {
		return SwigUtilsJNI.StatData_lastFileStatusChange_get(swigCPtr, this);
	}

	public void setLastFileStatusChange(long value) {
		SwigUtilsJNI.StatData_lastFileStatusChange_set(swigCPtr, this, value);
	}

	public int getFileSizeInBytes() {
		return SwigUtilsJNI.StatData_fileSizeInBytes_get(swigCPtr, this);
	}

	public void setFileSizeInBytes(int value) {
		SwigUtilsJNI.StatData_fileSizeInBytes_set(swigCPtr, this, value);
	}

	public int getBlocksAllocatedToFile() {
		return SwigUtilsJNI.StatData_blocksAllocatedToFile_get(swigCPtr, this);
	}

	public void setBlocksAllocatedToFile(int value) {
		SwigUtilsJNI.StatData_blocksAllocatedToFile_set(swigCPtr, this, value);
	}

	public int getOptimalIOOpsBlockSize() {
		return SwigUtilsJNI.StatData_optimalIOOpsBlockSize_get(swigCPtr, this);
	}

	public void setOptimalIOOpsBlockSize(int value) {
		SwigUtilsJNI.StatData_optimalIOOpsBlockSize_set(swigCPtr, this, value);
	}

	public int getUserDefinedFlags() {
		return SwigUtilsJNI.StatData_userDefinedFlags_get(swigCPtr, this);
	}

	public void setUserDefinedFlags(int value) {
		SwigUtilsJNI.StatData_userDefinedFlags_set(swigCPtr, this, value);
	}

	public int getFileGenerationNumber() {
		return SwigUtilsJNI.StatData_fileGenerationNumber_get(swigCPtr, this);
	}

	public void setFileGenerationNumber(int value) {
		SwigUtilsJNI.StatData_fileGenerationNumber_set(swigCPtr, this, value);
	}

}
