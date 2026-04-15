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
package com.hitorro.util.typesystem.valuesource;

import com.hitorro.util.core.string.StringUtil;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Set;


public enum FieldType {
	byteType(0, "byte", 1, Byte.class, "B", false, 2, 3, "java/lang/Byte", 3, Opcodes.ILOAD),
	shortType(1, "short", 2, Short.class, "S", false, 2, 3, "java/lang/Short", 3, Opcodes.ILOAD),
	longType(2, "long", 8, Long.class, "J", false, 3, 4, "java/lang/Long", 4, Opcodes.LLOAD),
	integerType(3, "int", 4, Integer.class, "I", false, 2, 3, "java/lang/Integer", 3, Opcodes.ILOAD),
	floatType(4, "float", 4, Float.class, "F", false, 2, 3, "java/lang/Float", 3, Opcodes.FLOAD),
	doubleType(5, "double", 8, Double.class, "D", false, 3, 3, "java/lang/Double", 4, Opcodes.DLOAD),
	booleanType(6, "boolean", 1, Boolean.class, "Z", false, 2, 3, "java/lang/Boolean", 3, Opcodes.ILOAD),
	objectType(7, "object", -1, Object.class, "V", true, 2, 3, null, 4, Opcodes.ALOAD);

	private static HashMap<String, FieldType> s_byShortName;
	private static TIntObjectHashMap<FieldType> byOrd;
	private final String name;
	private final String javaShortname;
	private final int width;
	private final Class classType;
	private final int ord;
	private final boolean fullyQualified;
	private final int visitParamA;
	private final int visitParamB;
	private final String classPath;
	private final String valueString;
	private final int getStackSize;
	private final int opcodeLoad;

	FieldType(int ord, String name, int width, Class classType,
			  String javaShortName, boolean fullyQualified, int visitParamA,
			  int visitParamB,
			  String classPath,
			  int getStackSize,
			  int opcodeLoad) {
		this.ord = ord;
		this.name = name;
		this.width = width;
		this.classType = classType;
		this.javaShortname = javaShortName;
		this.fullyQualified = fullyQualified;
		this.visitParamA = visitParamA;
		this.visitParamB = visitParamB;
		this.classPath = classPath;
		this.valueString = StringUtil.strcat(name, "Value");
		this.getStackSize = getStackSize;
		this.opcodeLoad = opcodeLoad;
		setMapEntry(this);
	}

	public static Set<String> getValidValues() {
		return s_byShortName.keySet();
	}

	public static final int size() {
		return s_byShortName.size();
	}

	public static FieldType getByName(String name) {
		FieldType ft = s_byShortName.get(name.toLowerCase());
		if (ft == null) {
			return objectType;
		}
		return ft;
	}

	public static FieldType getByStableOrdinal(int ordinal) {
		return byOrd.get(ordinal);
	}

	private static void setMapEntry(FieldType filter) {
		if (s_byShortName == null) {
			s_byShortName = new HashMap<String, FieldType>();
		}
		if (byOrd == null) {
			byOrd = new TIntObjectHashMap();
		}
		s_byShortName.put(filter.getName(), filter);
		byOrd.put(filter.getStableOrdinal(), filter);
	}

	public int getWidth() {
		return width;
	}

	public String getName() {
		return name;
	}

	public Class getClassType() {
		return classType;
	}

	public String getJavaShortName() {
		return javaShortname;
	}

	public boolean getFullyQualified() {
		return fullyQualified;
	}

	public int getVisitParam() {
		return visitParamA;
	}

	public int getVisitBParam() {
		return visitParamB;
	}

	public String getClassPath() {
		return classPath;
	}

	public String getValueString() {
		return valueString;
	}

	public int getGetStackSize() {
		return getStackSize;
	}

	public int getOpcodeLoad() {
		return opcodeLoad;
	}

	public int getStableOrdinal() {
		return ord;
	}
}
