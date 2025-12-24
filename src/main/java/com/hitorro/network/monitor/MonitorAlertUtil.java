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

import com.hitorro.network.netevent.AlertNetEventHandler;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;

public class MonitorAlertUtil {
	public static final StringProperty DevicePrefix = new StringProperty("monitor.deviceprefix",
			"", "/dev/");
	public static final IntegerProperty MinPercent =
			new IntegerProperty("monitor.minpercent",
					"",
					90);

	public static void addLowDiskAlert() {
		addLowDiskAlert(DevicePrefix.apply(), MinPercent.apply());
	}

	public static void addLowDiskAlert(String deviceStartsWith, int minPercent) {
		MonitorService.addWatch(new LowDiskSpaceValidator(deviceStartsWith, minPercent),
				new AlertMonitorAction(AlertNetEventHandler.AlertKey));
	}


}
