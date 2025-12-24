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
package com.hitorro.network.monitor;

import com.hitorro.util.core.Constants;
import com.hitorro.util.io.DiskUsage;

import java.util.ArrayList;
import java.util.List;

public class LowDiskSpaceValidator extends MonitorValidator {
	private final String devicesStartingWith;
	private final int minPercentage;

	public LowDiskSpaceValidator(String devicesStartingWith, int minPercentage) {
		this.devicesStartingWith = devicesStartingWith;
		this.minPercentage = minPercentage;
	}

	public boolean validate(MonitorAction ma) {
		List<DiskUsage.DFRow> rows = DiskUsage.getDFCached(Constants.MillisInSecond * 60);
		for (DiskUsage.DFRow row : rows) {
			String s = row.getFileSystem();
			if (s.startsWith(devicesStartingWith)) {
				if (row.getPercentCapacity() > minPercentage) {
					List<String> args = new ArrayList<String>();
					args.add(row.getFileSystem());
					args.add(row.getMountedOn());
					args.add(Integer.toString(row.getPercentCapacity()));

					ma.execute("lowdiskspace on %s mounted %s percentage used: %s%%", args);
				}
			}
		}
		return false;
	}
}
