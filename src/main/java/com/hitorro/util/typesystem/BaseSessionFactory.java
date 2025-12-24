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

/**
 * Abstract factory for creating and managing sessions. Concrete implementations
 * should be provided by persistence frameworks (e.g., DMSSessionFactory).
 * 
 * The factory uses a singleton pattern with runtime registration, allowing
 * modules to use session management without depending on the concrete implementation.
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
