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
import com.hitorro.util.commandandcontrol.MultiRowResponse;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.ColumnGroup;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.RespColumn;
import com.hitorro.util.commandandcontrol.ano.ResponseDefinition;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.typesystem.Type;
import com.hitorro.util.typesystem.TypeFieldIntf;
import com.hitorro.util.typesystem.TypeManager;
import com.hitorro.util.typesystem.annotation.FullTextAttributeMetaInfo;
import com.hitorro.util.typesystem.annotation.UiProperties;

@CommandDef(command = "type.fields", description = "Dump the fields of a type")
public class DumpType extends Command {
	@CommandArgument(required = true)
	private final StringProperty TypeName = new StringProperty("type", "Name of type to dump", "");

	@ResponseDefinition(command = "types",
			rowname = "type",
			columns = {@RespColumn(name = "Name", lName = "name"),
					@RespColumn(name = "Defined in", lName = "definedin"),
					@RespColumn(name = "UIPropertiesField", lName = "uipropfield"),
					@RespColumn(name = "UIPropertiesValue", lName = "uipropval"),
					@RespColumn(name = "FullTextInfoField", lName = "vtinfofield"),
					@RespColumn(name = "FullTextInfoValue", lName = "ftinfoval")},
			groups = {@ColumnGroup(start = 2, size = 2, name = "uiprops"),
					@ColumnGroup(start = 4, size = 2, name = "ftprops")})
	private final ResponseShape shape = new ResponseShape();

	public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
		String name = TypeName.apply(args);
		Type type = (Type)
				TypeManager.getTypeManager().getTypeByShortName(name);
		if (type == null) {
			this.writeSimpleError(response, "No type found by the short name of :%", name);
			return false;
		}
		TypeFieldIntf[] fields = type.getTypeFields();
		response.setResponseShape(shape);
		MultiRowResponse mrr = response.getMultiRowResponse();
		for (TypeFieldIntf field : fields) {
			mrr.add(0, field.getName());
			mrr.add(1, field.getDefinedIn().getName());
			addFTInfo(field, mrr);
			UiProperties uip = field.getUiProperties();
			if (uip != null) {
				mrr.addTuple(2, "name", uip.displayName());
				mrr.addTuple(2, "type", uip.displayType());
			}
			response.addMultiRowResponse(mrr);
		}
		return true;
	}

	private void addFTInfo(TypeFieldIntf field, MultiRowResponse mrr) {
		FullTextAttributeMetaInfo ft = field.getFullTextMeta();
		if (ft != null) {
			mrr.addTuple(4, "display name", ft.displayName());
			mrr.addTuple(4, "ft indexable", Boolean.toString(ft.isFullTextIndexable()));
			mrr.addTuple(4, "lucene field name", ft.luceneFieldName());
			mrr.addTuple(4, "Index Filters", ft.luceneIndexingFilters());
			mrr.addTuple(4, "Search Filters", ft.luceneSearchingFilters());
			mrr.addTuple(4, "stored", Boolean.toString(ft.stored()));
			mrr.addTuple(4, "literal", Boolean.toString(ft.stringLiteral()));
			mrr.addTuple(4, "allfield", Boolean.toString(ft.allField()));
		}
	}
}

/*
Just a perfect day,
Drink Sangria in the park,
And then later, when it gets dark,
We go home.
Just a perfect day,
Feed animals in the zoo
Then later, a movie, too,
And then home.

Oh it's such a perfect day,
I'm glad I spent it with you.
Oh such a perfect day,
You just keep me hanging on,
You just keep me hanging on.

Just a perfect day,
Problems all left alone,
Weekenders on our own.
It's such fun.
Just a perfect day,
You made me forget myself.
I thought I was someone else,
Someone good.

Oh it's such a perfect day,
I'm glad I spent it with you.
Oh such a perfect day,
You just keep me hanging on,
You just keep me hanging on.

You're going to reap just what you sow,
You're going to reap just what you sow,
You're going to reap just what you sow,
You're going to reap just what you sow...

-- Lou Reed --
 */
