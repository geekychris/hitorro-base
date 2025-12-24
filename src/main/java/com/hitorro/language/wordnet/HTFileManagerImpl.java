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
package com.hitorro.language.wordnet;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Env;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryCatalog;
import net.sf.extjwnl.dictionary.file_manager.FileManagerImpl;
import net.sf.extjwnl.util.factory.NameValueParam;
import net.sf.extjwnl.util.factory.Param;

import java.util.Map;

public class HTFileManagerImpl extends FileManagerImpl {
	public HTFileManagerImpl(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
		super(dictionary, augmentParams(dictionary, params));
	}

	public static Map<String, Param> augmentParams(Dictionary dictionary, Map<String, Param> params) {
		BaseFile wnDir = Env.getWordnetDirectory();
		params.put(DictionaryCatalog.DICTIONARY_PATH_KEY,
				new NameValueParam(dictionary,
						DictionaryCatalog.DICTIONARY_PATH_KEY,
						wnDir.getAbsolutePath()));
		return params;
	}
}