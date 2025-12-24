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
package com.hitorro.base.filesetmanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.filters.FileEndsWith;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.opers.AlwaysTrueOperator;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.StaticVarProperty;
import com.hitorro.util.json.keys.StringListFromDelimitedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the access to a set of files, but usually a set of files representing a named queue: - what are its
 * expected file extensions
 * <p/>
 * It is assumed that T is the basic object that the queue is persisted in.  So for example in raw queue this maybe a
 * JSON wrapper In a processed queue its possibly a Bag or other HTSerializable. The MAPPED type is a potential target
 * format.  You may access a disk queue and want to apply to a bag from a JSON tweet.  You would then be using a
 * FileSet<JSONElement, Bag>
 * <p/>
 * Its general usage is to represent a queue, but it can be used for the more basic access of sets of files with
 * specific format and mappings.
 */
public class FileSet<T, MAPPED> {
	public static final BooleanProperty IsQueue = new BooleanProperty("isqueue", "Is it to be managed as a queue", false);
	public static final StringListFromDelimitedKey ExtensionsKey = new StringListFromDelimitedKey("extensions", "filename extensions", ",", null);
	public final StaticVarProperty<BaseMapper<BaseFile, AbstractIterator<T>>> IteratorMapperKey = new StaticVarProperty("iterator", "basefile to target iterator", false, null, Mapper.class);
	public final StaticVarProperty<BaseMapper<T, MAPPED>> MapperKey = new StaticVarProperty("mapper", "apply from one object type to another", false, null, Mapper.class);
	protected BaseMapper<BaseFile, AbstractIterator<T>> iterMapper;

	protected String name;
	protected HTPredicate<BaseFile> fileNameFilter;
	protected List<String> extension = new ArrayList();
	protected Map<String, BaseMapper<T, MAPPED>> mapTo = new HashMap();

	public FileSet(String name, JsonNode map) {
		this.name = name;
		iterMapper = IteratorMapperKey.apply(map);
		extension = ExtensionsKey.apply(map);
		fileNameFilter = AlwaysTrueOperator.oper;
		if (extension != null) {
			FileEndsWith[] arr = new FileEndsWith[extension.size()];
			for (int i = 0; i < extension.size(); i++) {
				// ignore compression settings (assume that all mappers handle compression formats)
				arr[i] = new FileEndsWith(extension.get(i), true, true);
			}
			fileNameFilter = new LogicalOrOperator(arr);
		}

		JsonNode maps = map.get("mapto");
		if (JSONUtil.nullOrEmpty(maps)) {
			return;
		}
		Iterator<Map.Entry<String, JsonNode>> iter = maps.fields();
		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> km = iter.next();
			addMapTo(km.getKey(), km.getValue());
		}
	}

	/**
	 * get the me that will take the underlying type T to the desired target type
	 *
	 * @param to
	 * @return
	 */
	public BaseMapper<T, MAPPED> getMapTo(String to) {
		return mapTo.get(to.toLowerCase());
	}

	public String getName() {
		return name;
	}

	/**
	 * Take the input file format and apply it to some target format.
	 *
	 * @param bf
	 * @param to
	 * @return
	 */
	public AbstractIterator<MAPPED> getIterator(BaseFile bf, String to) {
		BaseMapper<T, MAPPED> mapper = getMapTo(to);
		if (mapper == null) {
			return null;
		}
		return iterMapper.apply(bf).map(mapper);
	}

	/**
	 * Convenience method if you wish to access files of this queue without a queue operation
	 *
	 * @param bf
	 * @return
	 */
	public AbstractIterator<T> getIterator(BaseFile bf) {
		return iterMapper.apply(bf);
	}

	/**
	 * the iterator me for going from a basefile to target T
	 *
	 * @return
	 */
	public BaseMapper<BaseFile, AbstractIterator<T>> getIteratorMapper() {
		return iterMapper;
	}

	private void addMapTo(String key, JsonNode map) {
		BaseMapper m = MapperKey.apply(map);
		mapTo.put(key.toLowerCase(), m);
	}
}
