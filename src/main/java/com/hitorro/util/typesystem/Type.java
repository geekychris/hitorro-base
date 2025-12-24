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

import com.hitorro.language.Iso639Table;
import com.hitorro.language.IsoLanguage;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.classes.BeanStyleMethodAnnotation;
import com.hitorro.util.core.classes.ClassAnnotations;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.map.MapList;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.typesystem.annotation.AdapterClassMeta;
import com.hitorro.util.typesystem.annotation.ImplClassMeta;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;
import com.hitorro.util.typesystem.annotation.UiTypeProperties;
import com.hitorro.util.typesystem.annotation.ViewClassReference;
import com.hitorro.util.typesystem.btadapter.BaseTypeAdapter;
import com.hitorro.util.typesystem.valuesource.ValueSourceForClass;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Type implements TypeIntf {
	public static final String SoftFieldSeperator = "|";
	protected int m_isVersionable = 0;
	private Type m_superType = null;
	private Class m_implementationClass = null;
	private String m_shortName = null;
	private final MapList<OnTrigger.TriggerType, OnTrigger> m_triggers = new MapList<OnTrigger.TriggerType, OnTrigger>();
	private final Map<String, BaseTypeAdapter> m_adapters = new HashMap<String, BaseTypeAdapter>();
	// cached copy including all super type fields.
	private TypeFieldIntf[] m_typeFields = null;
	// just our local fields
	private final Map<String, TypeFieldIntf> m_myTypeFields = new HashMap<String, TypeFieldIntf>();
	private TypeClassMetaInfo m_classMeta = null;
	private UiTypeProperties m_uiProperties;
	private Accessor m_guidAccessor;
	private boolean isBag;
	private TypeFieldIntf languageField;
	private String[] m_softLinkFields;
	/**
	 * view name to type object
	 */
	private final Map<String, Type> m_viewToTypeMap = new HashMap<String, Type>();
	/**
	 * If this is a view, this field points to the owner of the view
	 */
	private Type m_viewOwner = null;
	private String m_viewType;
	private ValueSourceForClass m_valueSource;
	private String subclassString = null;

	public Type() {

	}

	public int getIsVersionable() {
		return m_isVersionable;
	}

	public void setIsVersionable(int ver) {
		m_isVersionable = ver;
	}

	public boolean equals(Object o) {
		if (o instanceof com.hitorro.util.typesystem.Type other) {

			String otherName = other.getName();
			String name = getName();
			return name.equals(otherName);
		}
		return false;
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public String toString() {
		if (m_implementationClass != null) {
			return Fmt.S("%s.%s", m_implementationClass.getCanonicalName(), this.m_shortName);
		}

		return Constants.EmptyString;
	}

	public boolean extendsType(Type t) {
		if (m_superType == null) {
			return false;
		}
		if (m_superType == t) {
			return true;
		}
		return m_superType.extendsType(t);
	}

	public TypeFieldIntf getLanguageField() {
		return languageField;
	}

	public IsoLanguage getIsoLanguage(Object o) {
		IsoLanguage ret = null;
		TypeFieldIntf tf = getLanguageField();
		if (tf != null) {
			Object oL = tf.getValue(o);
			if (oL != null) {
				ret = Iso639Table.getInstance().getRow(oL.toString());
			}
		}
		if (ret == null) {
			ret = Iso639Table.english;
		}
		return ret;
	}

	public String getSubclassString() {
		if (subclassString == null) {
			StringBuilder sb = new StringBuilder();
			addSubclassString(sb);
			subclassString = sb.toString();
		}
		return subclassString;
	}

	private void addSubclassString(StringBuilder sb) {
		sb.append(getName());

		if (this.m_superType != null) {
			sb.append(" ");
			m_superType.addSubclassString(sb);
		}
	}


	public boolean isBag() {
		return isBag;
	}

	public String getShortClassName() {
		if (m_implementationClass != null) {
			return ClassUtil.getBareName(m_implementationClass);
		}
		return "Unknown";
	}

	public ValueSourceForClass getValueSource() {
		return m_valueSource;
	}

	public String getViewName() {
		return m_viewType;
	}

	public boolean isPersisted() {
		if (m_classMeta == null) {
			return false;
		}
		return m_classMeta.isPersisted();
	}

	public boolean isView() {
		if (m_classMeta == null) {
			return false;
		}
		return m_classMeta.isView();
	}

	/**
	 * If this is a view, it has an type that it belongs to (for example blogedit has an owner of blog)
	 *
	 * @return
	 */
	public Type getViewsOwner() {
		return m_viewOwner;
	}

	/**
	 * If this object has a ui view by name, return that view type
	 *
	 * @param view
	 * @return
	 */
	public TypeIntf getView(String view) {
		return m_viewToTypeMap.get(view);
	}

	/**
	 * Compute a soft guid from the type meta fields
	 *
	 * @param pt
	 * @return
	 */
	public String getSoftGuid(HTSerializable pt) {
		if (m_softLinkFields == null) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		builder.append(this.m_classMeta.shortTypeName());
		builder.append(":s:");

		boolean secondField = false;
		for (String field : m_softLinkFields) {
			if (secondField) {
				builder.append(SoftFieldSeperator);
			}
			builder.append(m_valueSource.getValue(pt, field).toString());
			secondField = true;
		}
		return builder.toString();
	}

	/**
	 * Construct a representation of a type.
	 *
	 * @param clazz The java class of the persisted type
	 * @param ano   ClassAnnotations representing gathered class, field and method information about the type
	 */
	public void init(Class clazz, ClassAnnotations ano, TypeManager manager, Type parentType) {
		m_implementationClass = clazz;
		List<BeanStyleMethodAnnotation> fas = ano.getBeanStyleMethodAnnotations();
		extractClassLevelAnnotation(manager, ano);
		for (BeanStyleMethodAnnotation fa : fas) {
			TypeFieldIntf parentField = null;
			if (parentType != null) {
				parentField = parentType.getLocalField(fa.getFieldName());
			}
			if (parentField != null) {
				addTypeField(new TypeField((TypeField) parentField, fa));
			} else {
				TypeField tf = new TypeField(fa);
				addTypeField(tf);
			}
		}

        /*
        todo chris - put a TypeMethod to handle the bare method annotations
        List<MethodAnnotation> m = ano.getMethodAnnotations();
        for (MethodAnnotation ma : m)
        {
            addTypeField(new TypeField(ma.getMethod(), ma.getAnnotations()));
        }
        */

		initViews(manager);


	}

	void postfix() {
		// XXX:  Currently hard coded assumption of this field.  This should not be!
		languageField = this.getField("lang639");
	}


	private void initViews(TypeManager mgr) {
		if (m_uiProperties == null) {
			return;
		}
		ViewClassReference[] refs = m_uiProperties.views();
		if (refs == null) {
			return;
		}
		for (ViewClassReference ref : refs) {
			Type t = mgr.addType(ref.viewClass(), this);
			if (t != null) {
				t.m_viewOwner = this;
				t.m_viewType = ref.name();
				m_viewToTypeMap.put(ref.name(), t);
			}
		}
	}

	/**
	 * Create an instance from the class
	 *
	 * @return
	 */
	public HTSerializable getNewInstance() {

		try {
			return (HTSerializable) this.m_implementationClass.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	public void addTrigger(OnTrigger.TriggerType key, OnTrigger trigger) {
		m_triggers.add(key, trigger);
	}

	/**
	 * Given a trigger type and an object to process the triggers against, run some triggers!
	 *
	 * @param key
	 * @param recurse
	 * @param bt
	 */
	public void executeTriggers(OnTrigger.TriggerType key, boolean recurse, Object bt) {
		executeTriggers(key, recurse, bt, this);
	}

	/**
	 * get an adapter for by a given name for this object.
	 *
	 * @param adapterName
	 * @return an adapter that can be used to conjur up whatever the adapter supposed todo.
	 */
	public BaseTypeAdapter getBTAdapter(String adapterName) {
		BaseTypeAdapter bta = m_adapters.get(adapterName);
		if (bta != null) {
			return bta;
		}
		if (this.m_superType != null) {
			return m_superType.getBTAdapter(adapterName);
		}
		return null;
	}

	protected void executeTriggers(OnTrigger.TriggerType key, boolean recurse, Object bt, Type type) {
		List<OnTrigger> triggers = m_triggers.get(key);

		if (triggers != null) {
			for (OnTrigger trigger : triggers) {
				if (!trigger.execute(key, type, bt)) {
					// dont process anymore
					Log.typemanager.debug("Trigger %s, returned false for key %s",
							trigger.getName(), key);
					return;
				}
			}
		}
		if (recurse && this.m_superType != null) {
			m_superType.executeTriggers(key, recurse, bt, type);
		}
	}

	public Class getImplementationClass() {
		return m_implementationClass;
	}

	public TypeClassMetaInfo getTypeMeta() {
		return m_classMeta;
	}

	void setTypeMeta(TypeClassMetaInfo meta) {
		if (meta == null) {
			return;
		}
		m_classMeta = meta;
	}

	public void setValueSourceClass(ValueSourceForClass cls) {
		m_valueSource = cls;
	}

	void setUITypeMeta(UiTypeProperties props) {
		m_uiProperties = props;
	}

	public UiTypeProperties getTypeUiProperties() {
		return m_uiProperties;
	}

	public String getName() {
		if (m_classMeta != null) {
			String s = m_classMeta.shortTypeName();
			if (s == null) {
				return m_shortName;
			}
			return s;
		}
		return null;
	}

	public int getSchemaVersion() {
		if (m_classMeta != null) {
			return m_classMeta.schemaVersion();
		}
		return -1;
	}

	public void voidCache() {
		m_typeFields = null;
	}

	public void addTypeField(TypeFieldIntf tf) {
		m_myTypeFields.put(tf.getName().toLowerCase(), tf);
	}

	/**
	 * Kinda expensive function to lookup the name of a type field.  Used primarily to find TF's referred to in a
	 * property file key
	 *
	 * @param name
	 * @return
	 */
	public TypeFieldIntf getFieldScanning(String name) {
		for (TypeFieldIntf tf : this.m_myTypeFields.values()) {
			if (tf.getName().equalsIgnoreCase(name)) {
				return tf;
			}
		}
		if (this.m_superType != null) {
			return this.m_superType.getFieldScanning(name);
		}
		return null;
	}

	public void initFromPropsRead(TypeManager tm, Class c, String shortName, Type superType) {
		isBag = true;
		this.m_implementationClass = c;
		this.m_shortName = shortName;
		this.m_superType = superType;
	}

	public void init(TypeManager tm) {
		if (m_superType != null) {
			// we got initialized from a path that already gave us the supertype such as props load
			return;
		}
		// computeFromSubjectArea the super type
		m_superType = tm.getTypeForClass(m_implementationClass.getSuperclass());
	}

	/**
	 * Get a field by name, in this class or superclasses.
	 *
	 * @param fieldName
	 * @return the TypeField information, or null if the field doesn't exist
	 */
	public TypeFieldIntf getField(String fieldName) {
		TypeFieldIntf result = m_myTypeFields.get(fieldName.toLowerCase());
		if (result == null && m_superType != null) {
			return m_superType.getField(fieldName);
		} else {
			return result;
		}
	}

	/**
	 * Get a field by name, in this class only.
	 *
	 * @param fieldName
	 * @return the TypeField information, or null if the field doesn't exist
	 */
	public TypeFieldIntf getLocalField(String fieldName) {
		TypeFieldIntf result = m_myTypeFields.get(fieldName.toLowerCase());
		return result;
	}

	/**
	 * Get all the fields within this type.
	 *
	 * @return
	 */
	public TypeFieldIntf[] getTypeFields() {

		if (m_typeFields == null) {
			// rebuild the cache
			TypeFieldIntf[] temp = null;
			int index = 0;
			if (m_superType != null) {
				TypeFieldIntf[] superTfs = m_superType.getTypeFields();
				if (superTfs != null) {
					temp = new TypeFieldIntf[superTfs.length + m_myTypeFields.size()];
					for (; index < superTfs.length; index++) {
						temp[index] = superTfs[index];
					}
				}
			}
			if (temp == null) {
				temp = new TypeField[m_myTypeFields.size()];
			}
			for (TypeFieldIntf tf : m_myTypeFields.values()) {
				temp[index++] = tf;
			}
			m_typeFields = temp;

		}
		return m_typeFields;
	}

	/**
	 * Visitor to return a listFiles of fields that test a criteria.
	 *
	 * @param constraint
	 * @return
	 */
	public List<TypeFieldIntf> getTypeFieldsByConstraint(HTPredicate<TypeFieldIntf> constraint) {
		List<TypeFieldIntf> list = new ArrayList<TypeFieldIntf>();
		TypeFieldIntf[] fields = getTypeFields();
		Set<TypeFieldIntf> set = new HashSet();
		for (TypeFieldIntf tf : fields) {
			if (constraint.test(tf)) {
				set.add(tf);
			}
		}
		list.addAll(set);
		return list;
	}

	public Object getGuidAccessor() {
		if (m_guidAccessor == null) {
			if (m_superType != null) {
				return m_superType.getGuidAccessor();
			}
		}
		return m_guidAccessor;
	}

	public boolean isInstanceOf(HTSerializable pts) {
		return m_implementationClass.isInstance(pts);
	}

	private void extractClassLevelAnnotation(TypeManager manager, ClassAnnotations classAnnotations) {
		List<Annotation> annos = classAnnotations.getClassAnnotations();
		Class typeInfoClass = TypeClassMetaInfo.class;
		Class uiClass = UiTypeProperties.class;
		TypeManager typeManager = TypeManager.getTypeManager();
		for (Annotation a : annos) {
			Class aClass = a.getClass();
			Class[] interfaces = aClass.getInterfaces();
			if (interfaces == null || interfaces.length == 0) {
				continue;
			}
			aClass = interfaces[0];

			// handle the TypeClassMetaInfo annotation
			if (aClass.equals(typeInfoClass)) {
				extractTypeClassMetaInfo(a, manager);
			}
			// handle the UiTypeProperties
			else if (aClass.equals(uiClass)) {
				m_uiProperties = (UiTypeProperties) a;
				// views are pulled out in the initViews method
			}
		}
	}

	private void extractTypeClassMetaInfo(Annotation a, TypeManager manager) {
		m_classMeta = (TypeClassMetaInfo) a;
		Class vsClass = m_classMeta.valueSourceClass();
		// load value source
		m_valueSource = (ValueSourceForClass) ClassUtil.getInstanceSwallowError(vsClass, ValueSourceForClass.class);
		if (m_valueSource != null) {
			m_valueSource.setType(this);
		}
		String l = m_classMeta.softLinkField();
		if (l != null) {
			m_softLinkFields = StringUtil.tokenizeFromSingleChar(l, ":");
		}
		// and read the internal trigger annotation
		ImplClassMeta[] impls = m_classMeta.onTriggers();
		for (ImplClassMeta impl : impls) {
			Object o = ClassUtil.getInstanceSwallowError(impl.className(), OnTrigger.class);
			if (o != null) {
				OnTrigger trigger = (OnTrigger) o;
				this.addTrigger(impl.trigger(), trigger);
			} else {
				Log.typemanager.error("Unable to initialize OnTrigger %s", impl.className());
			}
		}

		AdapterClassMeta[] adapters = m_classMeta.adapters();
		for (AdapterClassMeta cm : adapters) {
			String group = cm.adapterGroup();
			Class clazz = cm.className();
			Object o = ClassUtil.getInstanceSwallowError(clazz, BaseTypeAdapter.class);
			if (o == null) {
				Log.typemanager.fatal("Adapter %s could not be instantiated, %s %e", clazz.getCanonicalName());
			} else {
				m_adapters.put(group, (BaseTypeAdapter) o);
			}
		}

		Class guidAcc = m_classMeta.guidAccessor();
		if (guidAcc != null) {
			if (guidAcc == Object.class) {
				guidAcc = TypeManager.getTypeManager().getDefaultAccessor();
				if (guidAcc == null) {
					return;
				}
			}
			try {
				m_guidAccessor = (Accessor) guidAcc.newInstance();
				m_guidAccessor.init(manager, this, this.m_softLinkFields);
			} catch (InstantiationException e) {
				Log.typemanager.fatal("Unable to initialize guidAccessor %s for type %s %s %e",
						guidAcc.getCanonicalName(),
						this.m_implementationClass.getCanonicalName(),
						e, e);
			} catch (IllegalAccessException e) {
				Log.typemanager.fatal("Unable to initialize guidAccessor %s for type %s %s %e",
						guidAcc.getCanonicalName(),
						this.m_implementationClass.getCanonicalName(),
						e, e);
			}

		}
	}

}

