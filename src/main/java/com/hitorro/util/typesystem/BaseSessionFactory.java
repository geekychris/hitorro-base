package com.hitorro.util.typesystem;

/**
 * Abstract factory for creating and managing sessions. Concrete implementations
 * should be provided by persistence frameworks (e.g., DMSSessionFactory).
 * 
 * The factory uses a singleton pattern with runtime registration, allowing
 * modules to use session management without depending on the concrete implementation.
 * 
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. 
 * User: chris Date: Feb 3, 2008 Time: 6:27:49 AM
 */
public abstract class BaseSessionFactory<S extends BaseSession> {
    protected static BaseSessionFactory factory;

    /**
     * Get the registered factory instance.
     * @return the factory instance, or null if not registered
     */
    public static BaseSessionFactory getFactory() {
        return factory;
    }

    /**
     * Register a concrete factory implementation. This should be called
     * during application initialization by the module providing the
     * concrete implementation.
     * 
     * @param factoryIn the factory to register
     */
    public static void setFactory(BaseSessionFactory factoryIn) {
        factory = factoryIn;
    }

    /**
     * Convenience method to close a session using the registered factory.
     * @param closeMe the session to close
     */
    public static final void closeSession(BaseSession closeMe) {
        if (factory != null) {
            factory.close(closeMe);
        }
    }

    /**
     * Convenience method to commit and close a session using the registered factory.
     * @param closeMe the session to commit and close
     */
    public final void commitAndCloseSession(BaseSession closeMe) {
        if (factory != null) {
            getFactory().commitAndClose(closeMe);
        }
    }

    public abstract int getSessionCount();

    public abstract S getSession();

    public abstract S getCachedDMSSession();

    public abstract S getCachedDMSSession(String key) throws SessionException;

    public abstract S getDMSSession(String key) throws SessionException;

    public abstract void rollbackCloseSession(S closeMe);

    public abstract void close(S closeMe);

    public abstract void disconnectSession(S closeMe);

    public abstract void rollbackClose(S closeMe);

    public abstract void commitAndClose(S closeMe);
}
