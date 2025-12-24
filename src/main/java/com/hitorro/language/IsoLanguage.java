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

import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.iterator.IsoLanguageIntf;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.FileProperty;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.ml.maxent.io.GISModelReader;
import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.tartarus.snowball.SnowballProgram;

import java.io.File;
import java.io.IOException;

/**
 * Represents a single language code.  Uses the Iso639 code set for the codification. This class includes helper methods
 * used to construct NLP models for doing NLP processing by language.  These methods should not be used directly, but
 * instead the singletons:
 * <p/>
 * SentenceDetectorSingleton (gives you a SentenceDetector) PartOfSpeechSingletong (gives you a POS tagger and also
 * access to name finding)
 */
public class IsoLanguage implements IsoLanguageIntf {

	public static final FileProperty OpenNLPRootPath = new FileProperty("opennlp.rootpath", "", "${ht_bin}/data/opennlpmodels1.5");
	private String three = "";
	private String terminilogic = "";
	private String two = "";
	private String english = "";
	private String french = "";
	private int ordinal;
	private Class stemmerClass;
	private PennAndTreebankBase patb = null;

	public IsoLanguage(String[] row) {
		switch (row.length) {
			case 6:
				String s = row[5].trim();
				this.ordinal = Integer.parseInt(s);
			case 5:
				this.french = row[4].trim();
			case 4:
				this.english = row[3].trim();
			case 3:
				this.two = row[2].trim();
			case 2:
				terminilogic = row[1].trim();
			case 1:
				this.three = row[0].trim();
		}
	}

	public String toString() {
		return two;
	}

	/**
	 * Provide the PennAndTreebank equivalent in the target language.  If the target language does not have a PAT then
	 * english is used.
	 *
	 * @return
	 */
	public synchronized PennAndTreebankBase getPennAndTreebank() {
		if (patb == null) {
			File file = new File(Env.getBin(), Fmt.S("data/pennandtreebank/%s.csv", two));
			if (file.exists()) {
				patb = new PennAndTreebankBase(two);
				try {
					patb.loadFromFile(file);
				} catch (IOException e) {
					Log.lang.error("Unable to load penn and treebank table for %s", two);
				}
			} else {
				patb = Iso639Table.english.getPennAndTreebank();
			}
		}

		return patb;
	}

	public void setStemmer(Class c) {
		this.stemmerClass = c;
	}

	public SimpleStemmer getSimpleSnowballStemmer() {
		if (stemmerClass == null) {
			return null;
		}
		SnowballProgram sp = (SnowballProgram) ClassUtil.getInstanceSwallowError(stemmerClass, SnowballProgram.class);
		if (sp == null) {
			return null;
		}
		return new SnowballSimpleStemmer(sp);
	}

	public String getThree() {
		return three;
	}

	public String getTerminologic() {
		return terminilogic;
	}

	public String getTwo() {
		return two;
	}

	public String getEnglish() {
		return english;
	}

	public String getFrench() {
		return french;
	}

	public int getOrdinal() {
		return ordinal;
	}

	SentenceDetectorME getSentenceDetector() {
		return new SentenceDetectorME(getSentenceModel());
	}

	TokenizerME getTokenizer() {
		return new TokenizerME(getTokenizerModel());
	}

	TokenizerModel getTokenizerModel() {
		File f = new File(OpenNLPRootPath.apply(), Fmt.S("%s-%s.bin", this.two, "token"));
		if (FileUtil.notNullAndExists(f)) {
			try {
				return new TokenizerModel(FileUtil.getBufferedFileInputStream(f));
			} catch (IOException e) {
				Log.lang.error("Unable to read sentence model %s %s %e", f, e, e);
			}
		}
		return null;
	}

	POSTaggerME getPOSTagger() {
		return new POSTaggerME(POSModelSingleton.singleton.get(this));
	}

	/**
	 * for chunking
	 *
	 * @return
	 */
	ParserModel getParserModel() {
		File f = new File(OpenNLPRootPath.apply(), Fmt.S("%s-parser-chunking.bin", this.two));
		if (FileUtil.notNullAndExists(f)) {
			try {
				return new ParserModel(FileUtil.getBufferedFileInputStream(f));
			} catch (IOException e) {
				Log.lang.error("Unable to read sentence model %s %s %e", f, e, e);
			}
		}
		return null;
	}

	ChunkerModel getChunkerModel() {
		File f = new File(OpenNLPRootPath.apply(), Fmt.S("%s-chunker.bin", this.two));
		if (FileUtil.notNullAndExists(f)) {
			try {
				return new ChunkerModel(FileUtil.getBufferedFileInputStream(f));
			} catch (IOException e) {
				Log.lang.error("Unable to read chunker model %s %s %e", f, e, e);
			}
		}
		return null;
	}

	TokenNameFinderModel getTokenNameFinderModel(String intent) {
		File f = new File(OpenNLPRootPath.apply(), Fmt.S("%s-%s.bin", this.two, Fmt.S("ner-%s", intent)));
		if (FileUtil.notNullAndExists(f)) {
			try {
				return new TokenNameFinderModel(FileUtil.getBufferedFileInputStream(f));
			} catch (IOException e) {
				Log.lang.error("Unable to read sentence model %s %s %e", f, e, e);
			}
		}
		return null;
	}

	POSModel getPOSModel() {
		File f = new File(OpenNLPRootPath.apply(), Fmt.S("%s-%s.bin", this.two, "pos-maxent"));
		if (FileUtil.notNullAndExists(f)) {
			try {
				return new POSModel(FileUtil.getBufferedFileInputStream(f));
			} catch (IOException e) {
				Log.lang.error("Unable to read sentence model %s %s %e", f, e, e);
			}
		}
		return null;
	}

	/**
	 * Construct a sentence model from disk.
	 *
	 * @return
	 */
	SentenceModel getSentenceModel() {
		File f = new File(OpenNLPRootPath.apply(), Fmt.S("%s-%s.bin", this.two, "sent"));
		if (FileUtil.notNullAndExists(f)) {
			try {
				return new SentenceModel(FileUtil.getBufferedFileInputStream(f));
			} catch (IOException e) {
				Log.lang.error("Unable to read sentence model %s %s %e", f, e, e);
			}
		}
		return null;
	}

	DoccatModel getDocumentCatModel(String name) {
		File f = new File(OpenNLPRootPath.apply(), Fmt.S("%s-%s.bin", this.two, name));
		if (FileUtil.notNullAndExists(f)) {
			try {
				return new DoccatModel(FileUtil.getBufferedFileInputStream(f));
			} catch (IOException e) {
				Log.lang.error("Unable to read document model %s %s %e", f, e, e);
			}
		}
		return null;
	}

	MaxentModel getMaxentModel(String name) {
		File f = new File(OpenNLPRootPath.apply(), Fmt.S("%s-%s.bin", this.two, name));
		if (FileUtil.notNullAndExists(f)) {
			try {
				return new GISModelReader(f).constructModel();
			} catch (IOException e) {
				Log.lang.error("Unable to read document model %s %s %e", f, e, e);
			}
		}
		return null;
	}

	NameFinderME getNameFinder(NameFinderIntent intent) {
		TokenNameFinderModel nfm = null;
		switch (intent) {
			case Person:
				nfm = NameFinderModelSingletonMapper.person.get(this);
				break;
			case Organization:
				nfm = NameFinderModelSingletonMapper.organization.get(this);
				break;
			case Location:
				nfm = NameFinderModelSingletonMapper.location.get(this);
				break;
			case Date:
				nfm = NameFinderModelSingletonMapper.date.get(this);
				break;
			case Money:
				nfm = NameFinderModelSingletonMapper.money.get(this);
			case Percentage:
				nfm = NameFinderModelSingletonMapper.money.get(this);
			case Time:
				nfm = NameFinderModelSingletonMapper.money.get(this);
				break;
		}
		if (nfm != null) {
			return new NameFinderME(nfm);
		}
		return null;
	}


	public enum NameFinderIntent {
		Person, Organization, Location, Date, Money, Time, Percentage
	}


}
