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
package com.hitorro.util.typesystem;

import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;
import com.hitorro.util.typesystem.annotation.UiProperties;
import com.hitorro.util.typesystem.valuesource.ValueMapMapper;
import com.hitorro.util.typesystem.valuesource.ValueSourceForClass;

import java.io.IOException;
import java.util.TreeMap;

/**
 * Bag is a collection of key=>object values.  One can do get and set operations on the bag to access the values.
 * Understands ValueSource machinery so can be driven like other BaseType objects (such as with indexing or feature
 * extraction)
 */
@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.Bag,
		isView = false,
		isPersisted = false,
		schemaVersion = Bag.SerializationVersion)
public class Bag extends BaseType implements HTSerializable, ValueSourceForClass {
	public static final int SerializationVersion = 1;

	private final TreeMap<String, Object> map = new TreeMap();

	private TypeIntf t;
	private String[] fieldNames = null;

	public static Bag getBagForType(String shortName) {
		Type t = (Type) TypeManager.getTypeManager().getTypeByShortName(shortName);
		if (t == null) {
			Log.typemanager.error("Unable to contruct bag for type:%s, type does not exist", shortName);
			return null;
		}
		Bag b = new Bag();
		b.setType(t);
		return b;
	}

	public TypeIntf getType() {
		return t;
	}

	public void setType(TypeIntf type) {
		t = type;
	}

	public int size() {
		return map.size();
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		os.writeString(t.getName());
		TypeFieldIntf[] tfs = t.getTypeFields();
		for (TypeFieldIntf tf : tfs) {
			String fieldName = tf.getName();
			Object o = map.get(fieldName);
			if (o != null) {
				os.writeString(fieldName);
				TypeFieldDataType tfdt = ((TypeField) tf).getIOMapper();
				tfdt.writeObject(os, o);
			}
		}
		os.writeString(null);

	}

	/**
	 * Get the value for the requested field.  If it was not set and there is a value me associated with it compute
	 * the value.  Note this computation is "demand based", only when you ask for it AND it will place the resultant
	 * value back into the bag.  If you persist this new bag, the new computed field will go along for the ride.  The
	 * current me impl will not recompute if there is a value.
	 *
	 * @param o
	 * @param k
	 * @return value of the field, computed if necessary.
	 */
	public Object getValue(Object o, String k) {
		Object res = map.get(k);
		if (res == null) {
			TypeFieldIntf tf = t.getField(k);
			if (tf != null) {
				ValueMapMapper vmm = tf.getValueMapMapper();
				if (vmm != null) {
					if (vmm.compute(k, this)) {
						return map.get(k);
					}
				}
			}
		}
		return res;
	}

	@Override
	public Object getValue(final String fieldName) {
		return getValue(this, fieldName);
	}

	public String getValueAsString(String k) {
		return getValueAsString(this, k);
	}

	public String getValueAsString(Object o, String k) {
		Object out = getValue(o, k);
		if (out != null) {
			return out.toString();
		}
		return null;
	}

	public void setValue(Object o, String k, Object v) {
		setValue(o, k, v, false);
	}

	@Override
	public void setValue(final Object o, final String k, final Object v, final boolean ignoreTypeCheck) {
		map.put(k, v);
	}

	public void setValue(String k, Object v) {
		setValue(this, k, v);
	}

	public void setValueIfNotNull(String k, Object v) {
		if (v != null) {
			setValue(this, k, v);
		}
	}

	public void setFirstNonNullValue(String k, Object... arr) {
		for (Object v : arr) {
			if (v != null) {
				setValue(this, k, v);
				return;
			}
		}
	}

	/**
	 * Convenience method for those times where we have a string form of the bags field and need to convertToPdf it to
	 * the correct type.
	 *
	 * @param o
	 * @param k
	 * @param s
	 * @param includeUnknown
	 * @return
	 */
	public boolean setFromString(Object o, String k, String s, boolean includeUnknown) {
		TypeFieldIntf tf = t.getField(k);
		if (tf == null) {
			if (includeUnknown) {
				setValue(o, k, s);
			} else {
				return false;
			}
		}
		setValue(o, k, ((TypeField) tf).getIOMapper().convertFromString(s));
		return true;
	}

	public String[] getFieldNames() {
		if (fieldNames != null) {
			return fieldNames;
		}

		TypeFieldIntf[] tf = t.getTypeFields();
		String[] fieldNames = new String[tf.length];
		for (int i = 0; i < tf.length; i++) {
			fieldNames[i] = tf[i].getName();
		}
		this.fieldNames = fieldNames;
		return this.fieldNames;
	}

	public UiProperties getUiProperties(Object obj, String fieldName) {
		TypeFieldIntf tf = t.getField(fieldName);
		if (tf == null) {
			return null;
		}
		return tf.getUiProperties();
	}

	public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		String tName = os.readString();
		t = TypeManager.getTypeManager().getTypeByShortName(tName);
		String fieldName = os.readString();
		while (fieldName != null) {
			TypeFieldIntf tf = t.getField(fieldName);
			if (tf == null) {
				Log.typemanager.error("unable to deserialize type %s as field %s is unknown", tName, fieldName);
				return;
			}
			TypeFieldDataType tfdt = ((TypeField) tf).getIOMapper();

			Object o = tfdt.readObject(os);

			if (o != null) {
				map.put(fieldName, o);
			}
			fieldName = os.readString();
		}

	}

	public int getSerializationVersion() {
		return SerializationVersion;
	}

	public boolean isPersisted() {
		return false;
	}

	public boolean hasGuid() {
		return false;
	}

	public boolean hasSoftGuid() {
		return false;
	}
}
