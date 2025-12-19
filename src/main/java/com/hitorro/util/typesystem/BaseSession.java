package com.hitorro.util.typesystem;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Abstract base class for session management. Concrete implementations
 * should be provided by persistence frameworks (e.g., DMS).
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. 
 * User: chris Date: Jan 31, 2008 Time: 8:42:03 AM
 */
public abstract class BaseSession {
    public abstract boolean isObjectPartOfSession(Object o);

    public abstract void addToCache(String guid, BaseType pts);

    public abstract void delete(Object o);

    public abstract BaseSessionFactory getSessionFactory();

    public abstract HTSerializable getHTSerializableFromGUID(String guild);

    public abstract void doJdbcWork(Function<Connection, Object> work);

    public abstract Object createQuery(String query);

    public abstract Object refresh(Object o);

    public abstract void enableCache(boolean enableCache);

    public abstract String getObjectGuidFromHash(long linkHash, boolean ensureNotProxy);

    public abstract void deleteObjectIfExists(String guid);

    public abstract HTSerializable getNonProxyObject(HTSerializable pts);

    public abstract Blob createBlob(InputStream is, int size);

    public abstract HTSerializable getBySoftReference(Class c, String key);

    public abstract void commitList(List list);

    // Should not be called by general populous
    public abstract void close();

    public abstract void commit();

    public abstract void disconnectSession();

    public abstract void flush();

    public abstract void saveOrUpdate(Object o);

    public abstract void rollbackAndClose();

    public abstract long getCreateTime();

    public abstract Date getCreateDate();

    public abstract String getReadableCreateTime();

    public abstract long getTableRowCount(String table);

    public abstract Object getSingleObject(Class cls, String constraint);

    public abstract String getThreadName();

    public abstract String getGroupName();

    public abstract Throwable getThrowable();

    public abstract void setThreadInfo(boolean recordStack);

    public abstract void fetch(Object o);

    public abstract void persist(Object o);

    public abstract void update(Object o);

    /**
     * Fetch objects with an HQL query.
     *
     * @param clazz       The class to be queried
     * @param query       a constraint (query without the classname).  May have JDBC-style ? parameters.
     * @param list        Resulting objects are placed in this listFiles.
     * @param paramValues values, in order, to be placed in parameters
     */
    public abstract void getObjects(Class clazz, String query, List list, Object... paramValues);

    /**
     * Fetch objects with an HQL query.
     *
     * @param clazz The class to be queried
     * @param query a constraint (query without the classname).  No parameters allowed.
     * @param list  Resulting objects are placed in this listFiles.
     */
    public abstract void getObjects(Class clazz, String query, List list);

    /**
     * Fetch objects with an HQL query.
     *
     * @param query The full HQL query (including classname) to use.  No parameters allowed.
     * @param list  Resulting objects are placed in this listFiles.
     */
    public abstract void getObjects(String query, List list);

    /**
     * Fetch objects with an HQL query.
     *
     * @param query       The full HQL query (including classname) to use.  May have JDBC-style ? parameters.
     * @param list        Resulting objects are placed in this listFiles.
     * @param paramValues values, in order, to be placed in parameters
     */
    public abstract void getObjects(String query, List list, Object... paramValues);

    public abstract Object getObject(Class clazz, String query, Object... paramValues);

    /**
     * Fetch a single object with an HQL query.
     *
     * @param query       The full HQL query (including classname) to use.  May have JDBC-style ? parameters.
     * @param paramValues values, in order, to be placed in parameters
     * @return the found object or null if none, or more than one, object is found
     */
    public abstract Object getObjectElipses(String query, Object... paramValues);

    public abstract Object getObject(String query, boolean allowDupes, boolean warn, Object... paramValues);

    public abstract Iterator getIteratorFromQuery(String query);

    /**
     * Run a query with parameters.
     *
     * @param query The query string, with ? parameters (jdbc style)
     * @param vals  Values to be placed in the parameters, in order.
     * @return An iterator containing the query results, null if there is a problem
     */
    public abstract Iterator getIteratorFromQueryArgs(String query, Object... vals);

    public abstract void setName(String name);

    public abstract String dumpStats();

    public abstract void rollback();

    public abstract void clearCaches();

    public abstract HTSerializable getObjectFromGuid(String guid);

}
