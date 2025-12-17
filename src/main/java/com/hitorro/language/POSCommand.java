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


package com.hitorro.language;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.events.cache.PoolContainer;
import com.hitorro.util.json.keys.StringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a sentence, print out its Penn and Treebank parts of speech.
 */
@CommandDef(command = "analysis.pos", description = "Penn and Treebank parts of speech for a given sentence")
public class POSCommand extends Command {
	@CommandArgument(required = true)
	public static final StringProperty Sentence = new StringProperty("sentence", "Sentence to compute parts of speech", null);

	public String interactiveArgument() {
		return Sentence.getKey();
	}

	public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
		PoolContainer<IsoLanguage, PartOfSpeech> posPool = PartOfSpeechSingletonMapper.singleton.get(Iso639Table.english);
		PartOfSpeech pos = posPool.get();
		try {
			String sentence = Sentence.apply(args);
			POS p = pos.getPOS(sentence);
			String[] text = p.getTokenizedText();
			List<String>[] tags = p.getTags();
			String[] pe0 = p.getTagsEnglish(0);
			String[] pe1 = p.getTagsEnglish(1);
			String[] pe2 = p.getTagsEnglish(2);
			List<GenericKeyValue> list = new ArrayList();
			for (int i = 0; i < text.length; i++) {
				String vals = getVals(pe0[i], pe1[i], pe2[i]);
				GenericKeyValue kv = new GenericKeyValue(text[i], vals);
				list.add(kv);
			}
			this.writeKeyValue(response, getKVShape(), list);
			return true;
		} finally {
			posPool.returnIt(pos);
		}
	}

	private String getVals(String... parts) {
		StringBuilder sb = new StringBuilder();
		String v = parts[0];
		sb.append(v);
		for (int i = 0; i < parts.length; i++) {
			if (v.equals(parts[i])) {
				continue;
			}
			v = parts[i];
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(v);
		}
		return sb.toString();
	}
}
