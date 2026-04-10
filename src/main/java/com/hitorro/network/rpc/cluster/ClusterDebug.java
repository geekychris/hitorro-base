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
package com.hitorro.network.rpc.cluster;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.ColumnGroup;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.RespColumn;
import com.hitorro.util.commandandcontrol.ano.ResponseDefinition;
import com.hitorro.util.json.keys.BooleanProperty;

import java.util.Date;

@CommandDef(command = "cluster.list", description = "Dump cluster info")
public class ClusterDebug extends Command {
	@CommandArgument(required = false)
	private final BooleanProperty All = new BooleanProperty("all", "Return the whole apply true|false", false);
	@CommandArgument(required = false)
	private final BooleanProperty Group = new BooleanProperty("group", "Return the whole node group info true|false", false);

	@ResponseDefinition(command = "clusterlist",
			rowname = "instDef",
			columns = {@RespColumn(name = "IP", lName = "ip"),
					@RespColumn(name = "Server ID", lName = "serverid"),
					@RespColumn(name = "Touched", lName = "touched", type = Date.class),
					@RespColumn(name = "Last Known Good", lName = "lastknowngood", type = Date.class),
					@RespColumn(name = "Nack Count", lName = "nackcount"),
					@RespColumn(name = "Software Version", lName = "softver"),
					@RespColumn(name = "Port Name", lName = "portname"),
					@RespColumn(name = "Port Number", lName = "portnum", type = Integer.class),
					@RespColumn(name = "Capability", lName = "capability"),
					@RespColumn(name = "Segment", lName = "segment"),
					@RespColumn(name = "Segment Version", lName = "segversion"),
					@RespColumn(name = "Segment Comment", lName = "segcomment"),
					@RespColumn(name = "DBID", lName = "dbid"),
					@RespColumn(name = "Node Comment", lName = "nodecomment")},
			groups = {@ColumnGroup(start = 6, size = 2, name = "portmaps"),
					@ColumnGroup(start = 8, size = 4, name = "capabilities")})
	private final ResponseShape shape = new ResponseShape();

	public boolean execute(String rawValue, JsonNode args, Response response, CommandSession session, RestOperations operation) throws Exception {
		ClusterService cs = ClusterService.getService();
		boolean all = All.apply(args);
		boolean group = Group.apply(args);
		//XXX to implement
		return true;
	}
}
