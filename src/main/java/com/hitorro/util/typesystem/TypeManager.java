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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.classes.ClassAnnotations;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.events.EventListener;
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalAndOperator;
import com.hitorro.util.core.opers.LogicalNotOperator;
import com.hitorro.util.core.params.JsonKeyMap;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.keys.ClassProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.typesystem.annotation.AnnotationDefaults;
import com.hitorro.util.typesystem.annotation.UiProperties;
import com.hitorro.util.typesystem.constraint.IsPersistedClassConstraint;
import com.hitorro.util.typesystem.constraint.IsViewConstraint;
import com.hitorro.util.typesystem.valuesource.ValueMapMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeManager extends TypeManagerBase implements EventListener {
	//TODO Update turned off proxy
	public static final StringProperty DefaultAccessor = new StringProperty("typemanager.defaultaccessor", "Class used as the default guid accessor", "ht.base.typesystem.accessors.GuidAccessor");
	public static final ClassProperty FieldKey = new ClassProperty("class", "", String.class);
	public static final ClassProperty MapperClassKey = new ClassProperty("class", "", ValueMapMapper.class);
	public static final StringProperty MapperKey = new StringProperty("mapper", "me associated with a field", null);
	public static final StringProperty TypeKey = new StringProperty("type", "", null);
	public static final StringProperty ExtendsKey = new StringProperty("extends", "", null);
	public static final String EventTopic = "TypeManager";
    /*public static Enhancer FullTextEnhancer = CGLibUtil.get(FullTextAttributeMetaInfoImpl.class,
            FullTextAttributeMetaInfo.class,
            true,
            null);
    public static Enhancer UIPropsEnhancer = CGLibUtil.get(UiPropertiesImpl.class,
            UiProperties.class,
            true,
            null);
    public static Enhancer UITypePropsEnhancer = CGLibUtil.get(UiTypePropertiesImpl.class,
            UiTypeProperties.class,
            true,
            null);
    public static Enhancer ViewClassReferenceEnhancer = CGLibUtil.get(ViewClassReferenceImpl.class,
            ViewClassReference.class,
            true,
            null);
    public static Enhancer DBEnhancer = CGLibUtil.get(DBSearchableAttributeMetaInfoImpl.class,
            DBSearchableAttributeMetaInfo.class,
            true,
            null);
    public static Enhancer TypeClassEnhancer = CGLibUtil.get(TypeClassMetaInfoImpl.class,
            TypeClassMetaInfo.class,
            true,
            null);
            */


	public static TypeMap s_typeMap = null;
	private static final HTPredicate<Type> constraint =
			new LogicalAndOperator<Type>(new IsPersistedClassConstraint(),
					new LogicalNotOperator<Type>(
							new IsViewConstraint()));
	private static final TypeManager s_tm = getTM();
	private static final ClassAnnotations s_annotationConstraint = new ClassAnnotations(AnnotationDefaults.class, null);
	private final List<Type> m_types = new ArrayList<Type>();
	private final Map<String, Type> m_classToType = new HashMap<String, Type>();
	private final Map<String, Type> m_shortNameToType = new HashMap<String, Type>();
	// DERIVED / CACHED values
	// same as   m_classToType except it includes all subclasses that did not register a Type object
	// to replace its super type.
	private final Map<String, Type> m_classToTypeCached = new HashMap<String, Type>();

	private boolean m_computed = false;

	private Class defaultAccessor = null;

	public TypeManager() {

		LocalEventHub.get().addEventListener(this, EventTopic);
		String da = DefaultAccessor.apply();
		if (!StringUtil.nullOrEmptyString(da)) {
			defaultAccessor = ClassUtil.getClassForName(da, Accessor.class);
		}
	}

	public static TypeManager getTypeManager() {
		return s_tm;
	}

	private static TypeManager getTM() {
		TypeManager t = new TypeManager();
		TypeManagerBase.set(t);
		return t;
	}

	public static final void executeTrigger(OnTrigger.TriggerType key, Object o, boolean recurse) {
		if (o == null) {
			Log.typemanager.warn("Null Object passed to executeTrigger %s", key);
		}
		TypeManager tm = TypeManager.getTypeManager();
		if (tm != null) {
			Type t = tm.getTypeForClass(o.getClass());
			if (t != null) {
				t.executeTriggers(key, recurse, o);
			} else {
				Log.typemanager.warn("No type found for %s trying to execute trigger %s",
						o.getClass(), key);
			}
		}
	}

	public Class getDefaultAccessor() {
		return defaultAccessor;
	}

	public void setDefaultAccessor(Class acc) {
		defaultAccessor = acc;
	}

	public void loadTypesFromProps(JVS props) throws PropaccessError {
		List<JsonKeyMap> types = JVSProperties.getProperties().getSubMaps("types");
		int processed;
		do {
			int left = types.size();
			List<JsonKeyMap> tmp = new ArrayList();
			for (JsonKeyMap km : types) {
				loadTypeFromProps(km, tmp);
			}
			if (tmp.size() == types.size()) {
				for (JsonKeyMap km : types) {
					Log.typemanager.error("unresolvable super type for:  %s", km.getKey());
					return;
				}
			}
			types = tmp;
		}
		while (types.size() > 0);
	}


	public void loadTypeFromProps(JsonKeyMap km, List<JsonKeyMap> redo) throws PropaccessError {
		boolean alreadyExists = false;
		String typeName = km.getKey();
		String tmp = TypeKey.apply(km.getValue());
		if (!StringUtil.nullOrEmptyString(tmp)) {
			typeName = tmp;
		}
		Type t = (Type) this.getTypeByShortName(typeName);
		if (t == null) {
			String extendsTypeKey = ExtendsKey.apply(km.getValue());
			Type extendsType = null;
			if (!StringUtil.nullOrEmptyString(extendsTypeKey)) {
				extendsType = (Type) this.getTypeByShortName(extendsTypeKey);
				if (extendsType == null) {
					// we dont know enough at the moment, lets try it again in the next rep
					redo.add(km);
					return;
				}
			}
			t = new Type();
			t.initFromPropsRead(this, com.hitorro.util.typesystem.Bag.class, typeName, extendsType);
		} else {
			alreadyExists = true;
		}

		JsonNode map = km.getValue().get("classmeta");
		t.setTypeMeta(com.hitorro.util.typesystem.annotation.TypeClassMetaInfoImpl.getFromMap(map));
		List<JsonKeyMap> fields = JSONUtil.getSubMaps(km.getValue(), "fields");

		for (JsonKeyMap field : fields) {
			Class c = FieldKey.apply(field.getValue());

			TypeFieldIntf tf = t.getFieldScanning(field.getKey());
			String fieldName = StringUtil.replace(field.getKey(), "---", ".");
			if (tf == null) {
				if (alreadyExists) {
					Log.typemanager.error("Unable to find field %s for type %s", typeName, field.getKey());
					continue;
				}

				tf = new TypeField(fieldName, null, null, c);
				t.addTypeField(tf);
			}
			// field doesnt exist


			tf.setDbSearchableMeta(null);
			tf.setUiProperties(getUIPropsFromProps(field));
			tf.setValueMapMapper(getValueSourceMapperFromProps(field, tf));

		}
		if (!alreadyExists) {
			this.addTypeFromProps(typeName, t);
		}
	}

	private UiProperties getUIPropsFromProps(final JsonKeyMap field) {
		JsonNode uiMap = field.getValue().get("ui");
		return null;
	}

	public HTSerializable createInstanceFromGUID(String guid) {
		String shortName = StringUtil.firstPart(guid, ':');
		Type t = m_shortNameToType.get(shortName);
		if (t == null) {
			return null;
		}
		return t.getNewInstance();
	}

	public Type getTypeFromGuid(String guid) {
		String[] shortName = StringUtil.splitByToken(guid, ':');
		return m_shortNameToType.get(shortName[0]);
	}

	public TypeIntf getTypeByShortName(String name) {
		return m_shortNameToType.get(name);
	}

	private ValueMapMapper getValueSourceMapperFromProps(final JsonKeyMap field, final TypeFieldIntf tf) {
		JsonNode mapper = field.getValue().get("mapper");
		if (!JSONUtil.nullOrEmpty(mapper)) {
			Object o = MapperClassKey.apply(mapper);
			Class vmmClass = MapperClassKey.apply(mapper);
			if (vmmClass != null) {
				ValueMapMapper vmm = (ValueMapMapper) ClassUtil.getInstanceSwallowError(vmmClass, ValueMapMapper.class);

				vmm.init(mapper);
				return vmm;
			}

		}
		return null;
	}

	/**
	 * Get a type from the base type
	 *
	 * @param bt
	 * @return
	 */
	public TypeIntf getTypeForBaseType(Object bt) {
		Class clazz = bt.getClass();
		if (clazz == Bag.class) {
			return ((Bag) bt).getType();
		}
		return getTypeForClass(clazz);
	}

	public HTSerializable getCopy(HTSerializable orig) {
		Type t = getTypeForClass(orig.getClass());
		HTSerializable pt = null;
		try {
			pt = (HTSerializable) t.getImplementationClass().newInstance();
			if (pt instanceof HTSerializableClone) {
				((HTSerializableClone) pt).copy(orig);
				return pt;
			}
			return null;
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	public List<Type> getTypes() {
		return m_types;
	}

	public List<Type> getListOfPersistedTypes() {
		return getTypesMatchingConstraint(constraint);
	}

	/**
	 * Query the type system for types that test the constraint criteria.
	 *
	 * @param constraint
	 * @return list of types matching criteria
	 */
	public List<Type> getTypesMatchingConstraint(HTPredicate<Type> constraint) {
		List<Type> types = new ArrayList<Type>();

		for (Type t : m_types) {
			if (constraint.test(t)) {
				types.add(t);
			}
		}
		return types;
	}


	/**
	 * Find the Type object for the specified class.  If we couldnt find one then null is returned. This will cache a
	 * class->Type mapping.
	 *
	 * @param c
	 * @return Type info
	 */
	public Type getTypeForClass(Class c) {
		String canon = c.getCanonicalName();
		Type t = m_classToTypeCached.get(canon);
		if (t != null) {
			return t;
		}

		Class superType = c;
		t = m_classToType.get(superType.getCanonicalName());
		// we have not got a cached type for this, lets go up the super type chain for a value;
		while (t == null && superType != null) {
			superType = superType.getSuperclass();
			if (superType == null) {
				// no super no type info  booo hooo
				return null;
			}
			t = m_classToType.get(superType.getCanonicalName());

		}
		if (t != null) {
			// now cache it
			m_classToTypeCached.put(c.getCanonicalName(), t);
		}
		return t;
	}

	public Type addType(Class c) {
		return addType(c, null);
	}

	public Type addTypeIfAbsent(Class c) {
		Type t = this.getTypeForClass(c);
		if (t == null) {
			return addType(c);
		}
		return t;
	}

	/**
	 * Add a new type to the system, must be a type that is a view, hence the owning type being passed in
	 *
	 * @param c
	 * @param owningType
	 * @return
	 */
	Type addType(Class c, Type owningType) {
		ClassAnnotations ca = new ClassAnnotations(c, s_annotationConstraint);
		Type type = new Type();
		type.init(c, ca, this, owningType);
		m_types.add(type);
		addClassTypeMapping(type);
		addShortNameMapping(type);
		voidCache();
		type.postfix();
		return type;
	}

	Type addTypeFromProps(String shortName, Type t) {
		m_types.add(t);
		String clazz = Fmt.S("Bag.%s", t.getName());
		Log.typemanager.debug("Adding type mapping for BaseType %s", clazz);
		m_classToType.put(clazz, t);
		addShortNameMapping(t);
		t.postfix();
		voidCache();
		return t;
	}

	private void addClassTypeMapping(Type t) {
		String clazz = t.getImplementationClass().getCanonicalName();
		Log.typemanager.debug("Adding type mapping for BaseType %s", clazz);
		m_classToType.put(clazz, t);
	}

	private void addShortNameMapping(Type t) {
		Type old = m_shortNameToType.get(t.getName());
		if (old != null) {
			if (!old.equals(t)) {
				Log.typemanager.fatal("Type %s is using a duplicate shortName of %s, used by type %s",
						t.getImplementationClass().getCanonicalName(),
						t.getName(),
						old.getImplementationClass().getCanonicalName());
			}
		}

		m_shortNameToType.put(t.getName(), t);
	}

	private void voidCache() {
		m_computed = false;
		voidCacheForTypeObjects();
		m_classToTypeCached.clear();
	}

	private void voidCacheForTypeObjects() {
		for (Type t : m_types) {
			t.voidCache();
		}
	}


	public void initTypeObjects() {
		for (Type t : m_types) {
			t.init(this);
		}
		s_typeMap = new TypeMap();
		s_typeMap.init();
	}

	public boolean event(String topic, String subTopic, Object args) {
		return false;
	}

	public String eventName() {
		return "TypeSystem";
	}

	public boolean runAsync() {
		return false;
	}
}

