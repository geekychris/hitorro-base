package com.hitorro.util.typesystem.annotation;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.ClassProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.typesystem.valuesource.ValueSourceForClassStandardImpl;

import java.lang.annotation.Annotation;

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
public class TypeClassMetaInfoImpl implements TypeClassMetaInfo {
	public static final ClassProperty GuidAccessorKey = new ClassProperty("guidaccessor", "", null);
	public static final StringProperty ShortTypeNameKey = new StringProperty("shortname", "", null);
	public static final StringProperty SoftLinksFieldKey = new StringProperty("softlinksfield", "", null);
	public static final IntegerProperty SchemeVersionKey = new IntegerProperty("schemeversion", "", 1);
	public static final ClassProperty ValueSourceClassKey = new ClassProperty("vsourceclass", "", null);
	public static final BooleanProperty isViewKey = new BooleanProperty("isview", "", false);
	public static final BooleanProperty isPersistedKey = new BooleanProperty("ispersisted", "", false);
	public static final BooleanProperty isDynamicKey = new BooleanProperty("isdynamic", "", false);
	public static final BooleanProperty isDisableFastAccessorsKey = new BooleanProperty("isableFastAccessors", "", false);
	private String m_shortTypeName;
	private ImplClassMeta[] m_classMeta;
	private AdapterClassMeta[] m_adapterClassMeta;
	private int m_schemaVersion;
	private boolean m_isPersisted;
	private boolean m_isView;
	private boolean m_isDynamic;
	private boolean m_disableFastAccessors;
	private String m_softLinkField;
	private Class m_guidAccessor;
	private Class m_valueSourceClass = ValueSourceForClassStandardImpl.class;

	public TypeClassMetaInfoImpl(JsonNode map) {
		initFromMap(map);
	}

	public TypeClassMetaInfoImpl() {

	}

	public TypeClassMetaInfoImpl(TypeClassMetaInfo info) {
		init(info);
	}

	public static TypeClassMetaInfo getFromMap(JsonNode map) {
		TypeClassMetaInfoImpl metaImpl = new TypeClassMetaInfoImpl();
		//TypeClassMetaInfoImpl metaImpl = (TypeClassMetaInfoImpl) TypeManager.TypeClassEnhancer.create();
		metaImpl.initFromMap(map);
		return metaImpl;
	}

	public void initFromMap(final JsonNode map) {
		this.setGuidAccessor(GuidAccessorKey.apply(map));
		this.setShortTypeName(ShortTypeNameKey.apply(map));
		//this.setOnTriggers(info.onTriggers());
		//this.setAdapters(info.adapters());
		this.setSoftLinkField(SoftLinksFieldKey.apply(map));
		this.setIsView(isViewKey.apply(map));
		this.setIsPersisted(isPersistedKey.apply(map));
		this.setSchemaVersion(SchemeVersionKey.apply(map));
		this.setValueSourceClass(ValueSourceClassKey.apply(map));
		this.setIsDynamic(isDynamicKey.apply(map));
		this.setDisableFastAccessors(isDisableFastAccessorsKey.apply(map));
	}

	public void init(final TypeClassMetaInfo info) {
		this.setGuidAccessor(info.guidAccessor());
		this.setShortTypeName(info.shortTypeName());
		this.setOnTriggers(info.onTriggers());
		this.setAdapters(info.adapters());
		this.setSoftLinkField(info.softLinkField());
		this.setIsView(info.isView());
		this.setIsPersisted(info.isPersisted());
		this.setSchemaVersion(info.schemaVersion());
		this.setValueSourceClass(info.valueSourceClass());
		this.setIsDynamic(info.isDynamic());
		this.setDisableFastAccessors(info.disableFastAccessors());
	}

	/**
	 * @return short type name
	 */
	public String shortTypeName() {
		return m_shortTypeName;
	}

	public void setShortTypeName(String name) {
		m_shortTypeName = name;
	}

	public void setDisableFastAccessors(boolean flag) {
		m_disableFastAccessors = flag;
	}

	public boolean disableFastAccessors() {
		return m_disableFastAccessors;
	}

	/**
	 * listFiles of triggers that get
	 *
	 * @return Triggers registered with this class.
	 */
	public ImplClassMeta[] onTriggers() {
		return m_classMeta;
	}

	public void setOnTriggers(ImplClassMeta[] classMeta) {
		m_classMeta = classMeta;
	}

	/**
	 * listFiles of adapters
	 *
	 * @return Adapters associated with this class.
	 */
	public AdapterClassMeta[] adapters() {
		return m_adapterClassMeta;
	}

	public void setAdapters(AdapterClassMeta[] adapterClassMeta) {
		m_adapterClassMeta = adapterClassMeta;
	}

	/**
	 * The version of the schema that this object represents.  This is a place holder for cases where we do schema
	 * versioning and want something to auto-update the db schema.
	 *
	 * @return schema version
	 */
	public int schemaVersion() {
		return m_schemaVersion;
	}

	public void setSchemaVersion(int version) {
		m_schemaVersion = version;
	}

	/**
	 * Class is one that is persisted in hibernate.
	 *
	 * @return true if persisted in hibernate
	 */
	public boolean isPersisted() {
		return m_isPersisted;
	}

	public void setIsPersisted(boolean flag) {
		m_isPersisted = flag;
	}

	/**
	 * Indicates that this class is really a view
	 *
	 * @return
	 */
	public boolean isView() {
		return m_isView;
	}

	public boolean isDynamic() {
		return m_isDynamic;
	}

	public void setIsDynamic(boolean flag) {
		m_isDynamic = flag;
	}

	public void setIsView(boolean flag) {
		m_isView = flag;
	}

	public String softLinkField() {
		return m_softLinkField;
	}

	public void setSoftLinkField(String field) {
		m_softLinkField = field;
	}

	public Class guidAccessor() {
		return m_guidAccessor;
	}

	public void setGuidAccessor(Class clazz) {
		m_guidAccessor = clazz;
	}

	public Class valueSourceClass() {
		return m_valueSourceClass;
	}

	public void setValueSourceClass(Class classV) {
		m_valueSourceClass = classV;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return null;
	}
}
