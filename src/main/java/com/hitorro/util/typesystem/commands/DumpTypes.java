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
package com.hitorro.util.typesystem.commands;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.RespColumn;
import com.hitorro.util.commandandcontrol.ano.ResponseDefinition;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.opers.AlwaysTrueOperator;
import com.hitorro.util.typesystem.Type;
import com.hitorro.util.typesystem.TypeManager;

import java.util.List;

@CommandDef(command = "type.listtypes", description = "Dump all the types known to the type system")
public class DumpTypes extends Command {
	@ResponseDefinition(command = "types",
			rowname = "type",
			columns = {@RespColumn(name = "ShortName", lName = "shortname"),
					@RespColumn(name = "Class", lName = "class"),
					@RespColumn(name = "IsPersisted", lName = "ispersisted"),
					@RespColumn(name = "IsView", lName = "id", type = Boolean.class),
					@RespColumn(name = "ViewsOwningShortName", lName = "viewshortname"),
					@RespColumn(name = "Parent", lName = "parent")})

	private final ResponseShape header = new ResponseShape();

	public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
		List<Type> types = TypeManager.getTypeManager().getTypesMatchingConstraint(new AlwaysTrueOperator());
		if (ListUtil.nullOrEmpty(types)) {
			this.writeSimpleError(response, "No types registered with the system");
			return false;
		}
		response.setResponseShape(header);
		for (Type type : types) {
			String parent = "";
			if (type.isView()) {
				parent = type.getViewsOwner().getName();
			}
			response.addRow(type.getName(),
					type.getImplementationClass().getCanonicalName(),
					type.isPersisted(),
					type.isView(),
					type.getName(),
					parent);
		}
		response.end();
		return true;
	}
}
