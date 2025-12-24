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

class SwigUtilsJNI {
	public final static native int makeHardLink(String jarg1, String jarg2);

	public final static native int makeSymboliLink(String jarg1, String jarg2);

	public final static native int unlink(String jarg1);

	public final static native void StatFSData_fsType_set(long jarg1, StatFSData jarg1_, int jarg2);

	public final static native int StatFSData_fsType_get(long jarg1, StatFSData jarg1_);

	public final static native void StatFSData_txfrBlockSize_set(long jarg1, StatFSData jarg1_, int jarg2);

	public final static native int StatFSData_txfrBlockSize_get(long jarg1, StatFSData jarg1_);

	public final static native void StatFSData_totalBlocks_set(long jarg1, StatFSData jarg1_, int jarg2);

	public final static native int StatFSData_totalBlocks_get(long jarg1, StatFSData jarg1_);

	public final static native void StatFSData_freeBlocks_set(long jarg1, StatFSData jarg1_, int jarg2);

	public final static native int StatFSData_freeBlocks_get(long jarg1, StatFSData jarg1_);

	public final static native void StatFSData_availableBlocks_set(long jarg1, StatFSData jarg1_, int jarg2);

	public final static native int StatFSData_availableBlocks_get(long jarg1, StatFSData jarg1_);

	public final static native void StatFSData_totalFileNodes_set(long jarg1, StatFSData jarg1_, int jarg2);

	public final static native int StatFSData_totalFileNodes_get(long jarg1, StatFSData jarg1_);

	public final static native void StatFSData_freeFileNodes_set(long jarg1, StatFSData jarg1_, int jarg2);

	public final static native int StatFSData_freeFileNodes_get(long jarg1, StatFSData jarg1_);

	public final static native void StatFSData_fsID_set(long jarg1, StatFSData jarg1_, int jarg2);

	public final static native int StatFSData_fsID_get(long jarg1, StatFSData jarg1_);

	public final static native void delete_StatFSData(long jarg1);

	public final static native long new_StatFSData();

	public final static native void StatData_deviceInodeResidesOn_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_deviceInodeResidesOn_get(long jarg1, StatData jarg1_);

	public final static native void StatData_inodesNumber_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_inodesNumber_get(long jarg1, StatData jarg1_);

	public final static native void StatData_inodeProtectionMode_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_inodeProtectionMode_get(long jarg1, StatData jarg1_);

	public final static native void StatData_numberOfHardlinks_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_numberOfHardlinks_get(long jarg1, StatData jarg1_);

	public final static native void StatData_userId_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_userId_get(long jarg1, StatData jarg1_);

	public final static native void StatData_groupId_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_groupId_get(long jarg1, StatData jarg1_);

	public final static native void StatData_deviceType_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_deviceType_get(long jarg1, StatData jarg1_);

	public final static native void StatData_lastAccessTime_set(long jarg1, StatData jarg1_, long jarg2);

	public final static native long StatData_lastAccessTime_get(long jarg1, StatData jarg1_);

	public final static native void StatData_lastDateModified_set(long jarg1, StatData jarg1_, long jarg2);

	public final static native long StatData_lastDateModified_get(long jarg1, StatData jarg1_);

	public final static native void StatData_lastFileStatusChange_set(long jarg1, StatData jarg1_, long jarg2);

	public final static native long StatData_lastFileStatusChange_get(long jarg1, StatData jarg1_);

	public final static native void StatData_fileSizeInBytes_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_fileSizeInBytes_get(long jarg1, StatData jarg1_);

	public final static native void StatData_blocksAllocatedToFile_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_blocksAllocatedToFile_get(long jarg1, StatData jarg1_);

	public final static native void StatData_optimalIOOpsBlockSize_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_optimalIOOpsBlockSize_get(long jarg1, StatData jarg1_);

	public final static native void StatData_userDefinedFlags_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_userDefinedFlags_get(long jarg1, StatData jarg1_);

	public final static native void StatData_fileGenerationNumber_set(long jarg1, StatData jarg1_, int jarg2);

	public final static native int StatData_fileGenerationNumber_get(long jarg1, StatData jarg1_);

	public final static native void delete_StatData(long jarg1);

	public final static native long new_StatData();

	public final static native long statFS(String jarg1);

	public final static native long stat(String jarg1);

	public final static native int getProcessId();

	public final static native int getParentProcessId();

	public final static native int setUMask(int jarg1);

	public final static native int getProcessGroupId();

	public final static native int setProcessGroupId(int jarg1, int jarg2);

	public final static native int sendSignal(int jarg1, int jarg2);

	public final static native int getGroupId();

	public final static native int getEffectiveGroupId();

	public final static native int setGroupId(int jarg1);

	public final static native int setEffectiveGroupID(int jarg1);

	public final static native int setEngrent();

	public final static native int getGrent();

	public final static native int getLimit(int jarg1, int jarg2);

	public final static native int setEffectiveLimit(int jarg1, int jarg2, int jarg3);

	public final static native int getNice(int jarg1, int jarg2);

	public final static native int setNice(int jarg1, int jarg2, int jarg3);
}
