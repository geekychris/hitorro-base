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

import com.hitorro.language.IsoLanguage;
import com.hitorro.util.GenericAnalyzerBase;
import com.hitorro.util.core.classes.BeanStyleMethodAnnotation;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.typesystem.annotation.DBSearchableAttributeMetaInfo;
import com.hitorro.util.typesystem.annotation.FullTextAttributeMetaInfo;
import com.hitorro.util.typesystem.annotation.UiProperties;
import com.hitorro.util.typesystem.valuesource.ValueMapMapper;
import com.hitorro.util.typesystem.valuesource.ValueSourceField;
import org.apache.lucene.analysis.Analyzer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TypeField implements TypeFieldIntf {

	private String m_fieldName = null;
	private Type definedIn = null;
	private Method m_getter = null;
	private Method m_setter = null;
	private List<Annotation> m_annos;
	private Class m_fieldType = null;

	private DBSearchableAttributeMetaInfo dbSearchableMeta = null;
	private FullTextAttributeMetaInfo fullTextMeta = null;
	private UiProperties uiProperties = null;

	private final ValueSourceField vsf = null;
	private ValueMapMapper valueMapMapper = null;

	private BaseMapper storedFieldMapper;
	private BaseMapper indexedFieldMapper;

	// wire this up to where we use the mappers for transforming data in fields for indexing.

	private TypeFieldDataType tfdt;

	private TypeField() {

	}

	public TypeField(String fieldName, Method getter,
					 Method setter, Class fieldType) {
		m_fieldName = fieldName;
		m_getter = getter;
		m_setter = setter;
		m_annos = new ArrayList();
		setImplementingClass(fieldType);
	}

	public TypeField(BeanStyleMethodAnnotation fa) {
		m_fieldName = fa.getFieldName();
		m_getter = fa.getGetterMethod();
		m_setter = fa.getSetterMethod();
		m_annos = fa.getAnnotations();
		this.setImplementingClass(fa.getFieldType());
		extractFieldLevelAnnotation(this);

	}

	/**
	 * Constructor for the case this field is on a view and we have a real parent view.
	 *
	 * @param parentField
	 * @param fa
	 */
	public TypeField(TypeField parentField, BeanStyleMethodAnnotation fa) {
		m_fieldName = parentField.m_fieldName;
		m_getter = parentField.m_getter;
		m_setter = parentField.m_setter;

		m_annos = fa.getAnnotations();
		setImplementingClass(fa.getFieldType());

		// attempt to apply the parents annotations first then replace them with anything we have
		// that overides.
		parentField.extractFieldLevelAnnotation(this);
		extractFieldLevelAnnotation(this);
	}

	public String toString() {
		return m_fieldName;
	}

	public boolean equals(Object o) {
		if (o instanceof TypeField) {
			String fName = ((TypeField) o).m_fieldName;
			return m_fieldName.equals(fName);
		}
		return false;
	}

	public int hashcode() {
		return m_fieldName.hashCode();
	}

	public TypeFieldDataType getIOMapper() {
		return this.tfdt;
	}

	public Class getImplementingClass() {
		return m_fieldType;
	}

	private void setImplementingClass(Class c) {
		m_fieldType = c;
		tfdt = TypeFieldDataType.getFromClass(c);
	}

	public Class getReturnType() {
		return m_getter.getReturnType();
	}

	public ValueMapMapper getValueMapMapper() {
		return valueMapMapper;
	}

	public void setValueMapMapper(ValueMapMapper mapper) {
		valueMapMapper = mapper;
	}

	public TypeField clone() {
		TypeField clone = new TypeField();
		clone.m_fieldName = m_fieldName;
		clone.m_getter = m_getter;
		clone.m_setter = m_setter;
		clone.m_annos = m_annos;
		clone.m_fieldType = m_fieldType;
		clone.tfdt = tfdt;
		clone.fullTextMeta = fullTextMeta;
		clone.uiProperties = uiProperties;
		clone.dbSearchableMeta = dbSearchableMeta;
		clone.definedIn = definedIn;
		return clone;
	}

	public String getName() {
		return m_fieldName;
	}

	public Object getValue(Object obj) {
		if ((obj == null || m_getter == null)) {
			if (this.definedIn.isBag()) {

				return ((Bag) obj).getValue(obj, m_fieldName);
			}
			return null;
		}
		if (vsf != null) {
			return vsf.getValue(obj);
		}

		try {
			Object oo = m_getter.invoke(obj);
			return oo;
		} catch (IllegalAccessException iae) {
			Log.typemanager.error("GetValue failure %s", iae);
		} catch (InvocationTargetException ite) {
			Log.typemanager.error("GetValue failure %s", ite);
		}

		return null;
	}

	public void setValue(Object obj, Object value) {
		if (obj == null || m_setter == null) {
			return;
		}
		if (vsf != null) {
			vsf.setValue(obj, value);
			return;
		}
		try {
			m_setter.invoke(obj, value);
		} catch (IllegalAccessException iae) {
			Log.typemanager.error("SetValue failure %s", iae);
		} catch (InvocationTargetException ite) {
			Log.typemanager.error("SetValue failure %s", ite);
		}

	}

	public Analyzer getIndexingAnalyzer(IsoLanguage language) {
		String indexingFilter = fullTextMeta.luceneIndexingFilters();
		if (StringUtil.nullOrEmptyOrBlankString(indexingFilter)) {
			indexingFilter = GenericAnalyzerBase.Standard.apply();
		}
		if (!StringUtil.nullOrEmptyOrBlankString(indexingFilter)) {
			if (HTAnalyzerFactory.getFactory() == null) {
				Log.typemanager.info("Field %s has indexing filters that could not be initialized as the filter factory is not defined: %s", this.m_fieldName, indexingFilter);
			} else {
				return HTAnalyzerFactory.getFactory().get(indexingFilter, language, GenericAnalyzerBase.Mode.Index);
			}
		}
		return getSearchAnalyzer(language);
	}

	public Analyzer getSearchAnalyzer(IsoLanguage language) {

		String searchFilter = fullTextMeta.luceneSearchingFilters();
		if (StringUtil.nullOrEmptyOrBlankString(searchFilter)) {
			searchFilter = GenericAnalyzerBase.Standard.apply();
		}
		if (!StringUtil.nullOrEmptyOrBlankString(searchFilter)) {
			if (HTAnalyzerFactory.getFactory() == null) {
				Log.typemanager.info("Field %s has indexing filters that could not be initialized as the filter factory is not defined: %s", this.m_fieldName, searchFilter);
			} else {
				return HTAnalyzerFactory.getFactory().get(searchFilter, language, GenericAnalyzerBase.Mode.Query);
			}
		}
		return null;
	}

	public DBSearchableAttributeMetaInfo getDbSearchableMeta() {
		return dbSearchableMeta;
	}

	public void setDbSearchableMeta(DBSearchableAttributeMetaInfo meta) {
		if (meta == null) {
			return;
		}
		dbSearchableMeta = meta;
	}

	public FullTextAttributeMetaInfo getFullTextMeta() {
		return fullTextMeta;
	}

	public void setFullTextMeta(FullTextAttributeMetaInfo meta) {
		if (meta == null) {
			return;
		}
		fullTextMeta = meta;
	}

	public UiProperties getUiProperties() {
		return uiProperties;
	}

	public void setUiProperties(UiProperties prop) {
		if (prop == null) {
			return;
		}
		uiProperties = prop;
	}

	private void addAnnos(TypeField tf,
						  FullTextAttributeMetaInfo ft,
						  DBSearchableAttributeMetaInfo db,
						  UiProperties uiprops) {
		tf.fullTextMeta = ft;
		tf.dbSearchableMeta = db;
		tf.uiProperties = uiprops;
	}

	private void extractFieldLevelAnnotation(TypeField tf) {
		for (Annotation a : m_annos) {
			Class aClass = a.getClass();
			Class[] interfaces = aClass.getInterfaces();
			if (interfaces == null || interfaces.length == 0) {
				continue;
			}
			aClass = interfaces[0];
			if (aClass.equals(FullTextAttributeMetaInfo.class)) {
				tf.fullTextMeta = (FullTextAttributeMetaInfo) a;
			} else if (aClass.equals(DBSearchableAttributeMetaInfo.class)) {
				tf.dbSearchableMeta = (DBSearchableAttributeMetaInfo) a;
			} else if (aClass.equals(UiProperties.class)) {
				tf.uiProperties = (UiProperties) a;
			}
		}
	}

	public TypeIntf getDefinedIn() {
		return definedIn;
	}
}
