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

import com.hitorro.util.core.events.cache.PoolContainer;
import com.hitorro.util.core.events.cache.PooledObjectIntf;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;

import java.io.IOException;

/**
 *
 */
public class PartOfSpeech implements PooledObjectIntf<IsoLanguage> {
	public static final String PartOfSpeechDetectKey = "posdetect";
	/**
	 * Return mechanism so we can use java 1.7 close
	 */
	protected PoolContainer pc;
	IsoLanguage le;
	int generation;
	TokenizerME tokenizerME;
	POSTaggerME postagger;
	POS pos;
	private Parser parser = null;
	private ChunkerModel chunkerModel = null;
	private boolean hasNameFinders = false;
	private final NameFinderME[] nameFinders = new NameFinderME[IsoLanguage.NameFinderIntent.values().length];
	private final NameFinder[] nf = new NameFinder[IsoLanguage.NameFinderIntent.values().length];

	public PartOfSpeech(IsoLanguage le) {
		this.le = le;
		pos = new POS(this, null);
	}

	public Parser getParser() {
		if (parser == null) {
			ParserModel pm = le.getParserModel();
			if (pm != null) {
				parser = ParserFactory.create(pm);
			}
		}
		return parser;
	}

	public POSTaggerME getPOSTagger() {
		return postagger;
	}

	public ChunkerModel getChunker() {
		if (chunkerModel == null) {
			chunkerModel = le.getChunkerModel();
		}
		return chunkerModel;
	}

	/**
	 * Reset the name finder, apparently quality of retrieval drops after a few docs if you dont reset it.
	 */
	public void resetFinders() {
		if (hasNameFinders) {
			for (NameFinderME nfme : nameFinders) {
				if (nfme != null) {
					nfme.clearAdaptiveData();
				}
			}
		}
	}

	public String[] getModelNames() {
		String[] names = new String[IsoLanguage.NameFinderIntent.values().length];
		int i = 0;
		for (IsoLanguage.NameFinderIntent intent : IsoLanguage.NameFinderIntent.values()) {
			names[i++] = intent.name();
		}
		return names;
	}

	public NameFinder[] getAllNameFinders() {
		NameFinder[] results = new NameFinder[IsoLanguage.NameFinderIntent.values().length];
		int i = 0;
		for (IsoLanguage.NameFinderIntent intent : IsoLanguage.NameFinderIntent.values()) {
			results[i++] = getNameFinder(intent);
		}
		return results;
	}

	/**
	 * Provides a single namefinder (we re-use it and just give it new tokens each time). We will not give you a
	 * namefinder IF we dont have a model for the language you want
	 *
	 * @param intent
	 * @return
	 */
	public NameFinder getNameFinder(IsoLanguage.NameFinderIntent intent) {
		int index = intent.ordinal();
		if (nameFinders[index] == null) {
			nameFinders[index] = le.getNameFinder(intent);
		}
		if (nameFinders[index] == null) {
			return null;
		}
		if (nf[intent.ordinal()] == null) {
			hasNameFinders = true;
			nf[index] = new NameFinder(nameFinders[index]);
		}
		return nf[index];
	}

	public IsoLanguage getLanguage() {
		return le;
	}

	/**
	 * Reset anything that needs reseting following processing a document.
	 */
	public void reset() {
		if (pos != null) {
			resetFinders();
		}
	}

	public boolean init(IsoLanguage val) throws IOException {
		tokenizerME = val.getTokenizer();

		postagger = val.getPOSTagger();

		return true;
	}

	public POS getPOS(String[] content) {
		pos.setContent(content);
		return pos;
	}

	public POS getPOS(String content) {
		pos.setContent(content);
		return pos;
	}

	@Override
	public int getGenerationId() {
		return generation;
	}

	@Override
	public void setGenerationId(final int id) {
		generation = id;
	}

	@Override
	public void passivate() {
	}

	@Override
	public void activate() {
	}

	@Override
	public boolean reInit(final IsoLanguage key) {
		return false;
	}

	@Override
	public void setPoolContainer(final PoolContainer pc) {
		this.pc = pc;
	}

	@Override
	public void close() {
		pc.returnIt(this);
	}
}