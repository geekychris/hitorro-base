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
package com.hitorro.language;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.cache.SingletonCache;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.params.PropertiesUtil;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Pointer;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;
import net.sf.extjwnl.util.factory.NameValueParam;
import opennlp.tools.parser.Parse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HTJWNLDictionary {
	public static SingletonCache<HTJWNLDictionary> me = new SingletonCache("hwnl dictionary",
			new HTJWNLDictionaryMapper());
	private static final String[] empty = new String[0];
	private final net.sf.extjwnl.dictionary.Dictionary dict;
	private final MorphologicalProcessor morphy;

	HTJWNLDictionary() throws JWNLException, IOException {
		String xmlConfig = Env.getWordnetDirectory().getChild("file-properties.xml").readString();
		ObjectNode on = JsonNodeFactory.instance.objectNode();
		on.put("path", Env.getWordnetDirectory().getAbsolutePath());
		String resolved = PropertiesUtil.resolveJsonVariable(xmlConfig, on);
		ByteArrayInputStream bais = new ByteArrayInputStream(resolved.getBytes());
		dict = Dictionary.getInstance(bais);
		morphy = dict.getMorphologicalProcessor();
	}

	private net.sf.extjwnl.util.factory.Param get(Dictionary dict, String[] arr) {
		return new NameValueParam(dict, arr[0], arr[1]);
	}

	public Dictionary getDictionary() {
		return dict;
	}


	public String[] getLemmas(Parse np) {
		// make sure we're getting a single word.
		String word = np.getHead().toString().toLowerCase();
		if (word.length() > 40) {
			return new String[0];
		}
		synchronized (WordnetPool.class) {
			return getLemmas(word, "NN");
		}
	}

	public Set<String> getSynsetSet(Parse np) {
		synchronized (WordnetPool.class) {
			Set<String> synsetSet = new HashSet<String>();
			String[] lemmas = getLemmas(np);
			for (int li = 0; li < lemmas.length; li++) {
				String[] synsets = getParentSenseKeys(lemmas[li], "NN", 0);
				for (int si = 0, sn = synsets.length; si < sn; si++) {
					synsetSet.add(synsets[si]);
				}
			}
			return (synsetSet);
		}
	}

	public void generateWordNetFeatures(Parse focusNoun, List<String> features) {

		Parse[] toks = focusNoun.getTagNodes();
		if (toks[toks.length - 1].getType().startsWith("NNP")) {
			return;
		}
		//check wordnet
		Set<String> synsets = getSynsetSet(focusNoun);

		for (String synset : synsets) {
			features.add("s=" + synset);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized String[] getLemmas(String word, String tag) {
		try {
			POS pos;
			if (tag.startsWith("N") || tag.startsWith("n")) {
				pos = POS.NOUN;
			} else if (tag.startsWith("N") || tag.startsWith("v")) {
				pos = POS.VERB;
			} else if (tag.startsWith("J") || tag.startsWith("a")) {
				pos = POS.ADJECTIVE;
			} else if (tag.startsWith("R") || tag.startsWith("r")) {
				pos = POS.ADVERB;
			} else {
				pos = POS.NOUN;
			}
			List<String> lemmas = morphy.lookupAllBaseForms(pos, word);
			return lemmas.toArray(new String[lemmas.size()]);
		} catch (JWNLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public synchronized String getSenseKey(String lemma, String pos, int sense) {
		try {
			IndexWord iw = dict.getIndexWord(POS.NOUN, lemma);
			if (iw == null) {
				return null;
			}
			return String.valueOf(iw.getSynsetOffsets()[sense]);
		} catch (JWNLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public synchronized int getNumSenses(String lemma, String pos) {
		try {
			IndexWord iw = dict.getIndexWord(POS.NOUN, lemma);
			if (iw == null) {
				return 0;
			}
			return iw.getSenses().size();
		} catch (JWNLException e) {
			return 0;
		}
	}

	private void getParents(Synset synset, List<String> parents) throws JWNLException {
		List<Pointer> pointers = synset.getPointers();
		for (Pointer p : pointers) {
			if (p.getType() == PointerType.HYPERNYM) {
				Synset parent = p.getTargetSynset();
				parents.add(String.valueOf(parent.getOffset()));
				getParents(parent, parents);
			}
		}
	}

	public synchronized String[] getParentSenseKeys(String lemma, String pos, int sense) {
		//System.err.println("JWNLDictionary.getParentSenseKeys: lemma="+lemma);
		try {
			IndexWord iw = dict.getIndexWord(POS.NOUN, lemma);
			if (iw != null) {
				if (iw.getSenses().size() > 0) {
					Synset synset = iw.getSenses().get(sense);
					List<String> parents = new ArrayList<String>();
					getParents(synset, parents);
					return parents.toArray(new String[parents.size()]);
				}
			}
			return empty;
		} catch (JWNLException e) {
			Log.util.error("getParentSenseKey exploded %s %e", e, e);
			return null;
		}
	}
}

class HTJWNLDictionaryMapper extends BaseMapper<Object, HTJWNLDictionary> {
	@Override
	public HTJWNLDictionary apply(final Object o) {
		try {
			return new HTJWNLDictionary();
		} catch (JWNLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
}